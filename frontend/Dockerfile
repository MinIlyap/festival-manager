FROM node:24-alpine AS builder

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build:docker

FROM nginx:1.27-alpine AS runtime

COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY docker/api-proxy.conf /etc/nginx/api-proxy.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
