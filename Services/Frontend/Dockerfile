# ═══════════════════════════════════════════════════════════════════════════
# Dockerfile - Frontend React + Vite
# ═══════════════════════════════════════════════════════════════════════════
# Build en 2 etapas:
#   1. Build stage: Compila la aplicación React con Vite
#   2. Production stage: Sirve los archivos estáticos con Nginx
# ═══════════════════════════════════════════════════════════════════════════

# ─────────────────────────────────────────────────────────────────────────
# Etapa 1: Build - Compilar la aplicación React
# ─────────────────────────────────────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

# Copiar package.json y package-lock.json
COPY package*.json ./

# Instalar dependencias
RUN npm ci

# Copiar el resto del código fuente
COPY . .

# Compilar la aplicación para producción
RUN npm run build

# ─────────────────────────────────────────────────────────────────────────
# Etapa 2: Production - Servir con Nginx
# ─────────────────────────────────────────────────────────────────────────
FROM nginx:alpine

# Copiar archivos compilados desde la etapa de build
COPY --from=build /app/dist /usr/share/nginx/html

# Copiar configuración personalizada de Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Exponer puerto 80
EXPOSE 80

# Nginx se inicia automáticamente con la imagen base
CMD ["nginx", "-g", "daemon off;"]
