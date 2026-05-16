FROM node:20-alpine AS builder

WORKDIR /app

# Tăng memory tránh fail build
ENV NODE_OPTIONS="--max-old-space-size=4096"

# Copy dependency trước để tận dụng cache
COPY package*.json ./

# Cài dependency (nhẹ hơn)
RUN npm ci --legacy-peer-deps --no-audit --no-fund

# Copy source
COPY . .

# Build
RUN npm run build


FROM nginx:alpine

# Xóa config default (tránh conflict)
RUN rm -rf /etc/nginx/conf.d/*

# Copy build
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy config nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]