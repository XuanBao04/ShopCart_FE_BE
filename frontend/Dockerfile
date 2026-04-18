FROM node:21-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci --legacy-peer-deps
COPY . .
RUN npm run build

FROM node:21-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --legacy-peer-deps
COPY --from=builder /app/dist dist
EXPOSE 5173
CMD ["npx", "vite", "preview", "--host", "0.0.0.0"]
