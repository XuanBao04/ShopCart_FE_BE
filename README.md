# ShopCart - Frontend & Backend Monorepo

E-commerce application with React 19 + Vite frontend and Spring Boot 3 backend.

## 📋 Quick Start

### 1. Setup Environment Variables

```bash
# Copy template to local (never commit .env.local)
cp .env.example .env.local

# Edit .env.local with your local settings
nano .env.local
```

### 2. Backend Setup

```bash
cd backend

# Install dependencies & run
mvn spring-boot:run

# Or run with Docker Compose
docker-compose up -d db
```

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start dev server
npm run dev

# Access at http://localhost:5173
```

## 🐳 Docker Setup (Optional)

```bash
# Run entire stack with Docker
docker-compose up --env-file .env.local

# Services:
# - Backend: http://localhost:8080/api
# - Frontend: http://localhost:5173
# - PostgreSQL: localhost:5432
# - PgAdmin: http://localhost:8888
```

## 📁 Environment Files

| File | Purpose | Committed |
|------|---------|-----------|
| `.env.example` | Template with all variables | ✅ Yes |
| `.env.local` | Local development (your machine) | ❌ No (.gitignore) |
| `frontend/.env` | Frontend Vite variables | ✅ Yes (shared defaults) |
| `backend/src/main/resources/application-dev.yaml` | Backend config (reads env vars) | ✅ Yes |

## 🔧 Environment Variables Reference

See `.env.example` for all available variables:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` - Database connection
- `VITE_API_URL` - Frontend API endpoint
- `JWT_SECRET` - JWT signing key
- `POSTGRES_USER`, `POSTGRES_PASSWORD` - Docker database

## 📚 Architecture

- **Frontend**: React 19 + Vite + TailwindCSS
- **Backend**: Spring Boot 3 + Spring Data JPA + PostgreSQL
- **Testing**: Vitest/RTL (frontend), JUnit5/Mockito (backend)
- **Documentation**: See `BUSINESS_FLOWS.md` and `ARCHITECTURE.md`

## 🧪 Testing

```bash
# Frontend
cd frontend
npm run test        # Unit tests
npm run test:ui     # Interactive UI
npm run test:coverage
npm run e2e         # Playwright E2E

# Backend
cd backend
mvn test
```

## 📖 Documentation

- [ARCHITECTURE.md](./ARCHITECTURE.md) - Backend clean architecture design
- [BUSINESS_FLOWS.md](./BUSINESS_FLOWS.md) - Cart, Checkout, Order flows
- [TDD_GUIDE.md](./TDD_GUIDE.md) - Test-Driven Development approach