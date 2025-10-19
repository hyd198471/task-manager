# Frontend Dockerfile (React + Vite)
# Build static assets with Node, then serve with Nginx
FROM node:20-alpine AS build
WORKDIR /app
# Copy manifests first for dependency caching
COPY package.json ./
RUN npm install
# Copy rest of source
COPY . .
# Build-time API base (override with --build-arg VITE_API_BASE=...)
ARG VITE_API_BASE=http://localhost:8080/api
ENV VITE_API_BASE=$VITE_API_BASE
RUN npm run build

FROM nginx:1.27-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
# Default nginx CMD retained
