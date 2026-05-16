# ShopCart Deployment Guide for Render.com

## Overview
Hướng dẫn chi tiết deploy ShopCart app lên Render.com

## Prerequisites
- Tài khoản Render.com
- GitHub repository (public hoặc private)
- Docker knowledge

## Backend Deployment (Java/Spring Boot)

### Bước 1: Tạo PostgreSQL Database trên Render
1. Login vào [Render Dashboard](https://dashboard.render.com/)
2. Chọn **New +** → **PostgreSQL**
3. Điền thông tin:
   - **Name**: `shopcart-db`
   - **Database**: `shopcart`
   - **User**: `shopcart_user`
   - **Region**: Singapore (hoặc gần nhất)
   - **Pricing Plan**: Free (Starter)

4. Copy connection string - sẽ dùng sau

### Bước 2: Deploy Backend Service
1. Chọn **New +** → **Web Service**
2. Kết nối GitHub repository
3. Điền thông tin:
   - **Name**: `shopcart-backend`
   - **Runtime**: Docker
   - **Build Command**: `docker build -f backend/Dockerfile.prod .` (nếu Render không auto-detect)
   - **Start Command**: `java -jar app.jar` (đã config trong Dockerfile)
   - **Region**: Singapore
   - **Pricing Plan**: Free (Starter)

4. **Environment Variables** (sau khi tạo service):
   ```
   SPRING_DATASOURCE_URL=postgresql://shopcart_user:PASSWORD@HOST:5432/shopcart
   SPRING_DATASOURCE_USERNAME=shopcart_user
   SPRING_DATASOURCE_PASSWORD=PASSWORD
   SPRING_PROFILES_ACTIVE=prod
   SERVER_PORT=8080
   ```
   - Thay `PASSWORD` và `HOST` từ PostgreSQL connection string

5. Deploy → chờ build xong (~3-5 phút)

### Bước 3: Deploy Frontend Service
1. Chọn **New +** → **Web Service**
2. Kết nối GitHub repository
3. Điền thông tin:
   - **Name**: `shopcart-frontend`
   - **Runtime**: Docker
   - **Build Command**: `docker build -f frontend/Dockerfile .`
   - **Start Command**: `nginx -g 'daemon off;'` (đã config)
   - **Region**: Singapore
   - **Pricing Plan**: Free (Starter)

4. **Environment Variables**:
   ```
   VITE_API_URL=https://shopcart-backend.onrender.com/api
   ```
   - Thay `shopcart-backend` với tên backend service thực tế

5. Deploy → chờ build xong

## Important Notes

### ⚠️ Free Tier Limitations
- Auto-suspend nếu không có traffic trong 15 phút
- Memory limited: 512MB
- Shared CPU
- → Đối với production, nâng cấp lên paid plan

### 🔐 Security
- `.env` file đã trong `.gitignore` ✓
- Không push secrets lên GitHub
- Set environment variables trong Render Dashboard
- PostgreSQL connection string có mật khẩu mạnh

### 🚀 Performance
- Frontend build sử dụng multi-stage Dockerfile (tối ưu size)
- Backend Dockerfile.prod cũng multi-stage
- Nginx caching static assets
- DB connection pooling (Hikari) configured

### 📝 Troubleshooting
- **Backend không start**: Check logs trên Render Dashboard → View Logs
- **Frontend blank page**: Check VITE_API_URL environment variable
- **Database connection error**: Verify connection string format
- **Port 8080 already in use**: Render handles port automatically

## Local Testing (Optional)
Trước khi deploy, test local với:
```bash
docker-compose -f docker-compose.yml up
```

## File Checklist
- ✅ `backend/Dockerfile.prod` - multi-stage build
- ✅ `backend/src/main/resources/application-prod.yaml` - prod config
- ✅ `backend/.env.example` - environment template
- ✅ `backend/.gitignore` - excludes .env
- ✅ `frontend/Dockerfile` - multi-stage build
- ✅ `frontend/nginx.conf` - SPA routing + proxy API

## Useful Links
- [Render Documentation](https://render.com/docs)
- [Spring Boot on Render](https://render.com/docs/deploy-spring)
- [PostgreSQL on Render](https://render.com/docs/databases)
