# 🐳 Docker Deployment - Sistema SOA Ticketing

Este directorio contiene la configuración para ejecutar **todos los servicios en contenedores Docker** con un solo comando.

## 📋 Pre-requisitos

1. **Docker Desktop** instalado y corriendo
   - Windows: https://docs.docker.com/desktop/install/windows-install/
   - Verificar: `docker --version` y `docker-compose --version`

2. **Puerto 3306 disponible** (MySQL)
   - Detener XAMPP MySQL si está corriendo

## 🚀 Inicio Rápido

### 1. Configurar Gmail (Opcional)

Si quieres enviar emails reales:

```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar .env con tus credenciales de Gmail
# GMAIL_USERNAME=tu_email@gmail.com
# GMAIL_APP_PASSWORD=xxxx xxxx xxxx xxxx
```

**Si NO configuras Gmail:** Los emails se simularán en logs (funciona perfectamente).

### 2. Iniciar Todos los Servicios

```bash
# Construir imágenes y iniciar servicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f gateway
docker-compose logs -f orchestration-service
```

### 3. Esperar a que Todo Inicie

Los servicios tardan ~30-60 segundos en estar completamente listos.

**Verificar que todo está corriendo:**
```bash
docker-compose ps
```

Deberías ver 8 contenedores:
- `soa-mysql` (puerto 3306)
- `soa-gateway` (puerto 8080)
- `soa-user-service` (puerto 8081)
- `soa-event-service` (puerto 8082)
- `soa-orchestration-service` (puerto 8083)
- `soa-payment-service` (puerto 8084)
- `soa-notification-service` (puerto 8085)
- `soa-ticket-service` (puerto 8086)

### 4. Probar el Sistema

```bash
# Ejecutar test E2E (desde el host, no dentro del contenedor)
.\test-e2e.ps1
```

O probar manualmente:
```bash
# Health check del Gateway
curl http://localhost:8080/api/health

# Registrar usuario
curl -X POST http://localhost:8080/api/orchestration/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan",
    "apellido": "Perez",
    "email": "juan@example.com",
    "contrasena": "password123"
  }'
```

## 🛠️ Comandos Útiles

### Ver Estado de Servicios
```bash
docker-compose ps
```

### Ver Logs
```bash
# Todos los servicios
docker-compose logs -f

# Un servicio específico
docker-compose logs -f orchestration-service
docker-compose logs -f mysql

# Últimas 100 líneas
docker-compose logs --tail=100 gateway
```

### Detener Servicios
```bash
# Detener pero mantener datos
docker-compose stop

# Detener y eliminar contenedores (mantiene volúmenes)
docker-compose down

# Detener y ELIMINAR TODO (incluye base de datos)
docker-compose down -v
```

### Reiniciar un Servicio
```bash
docker-compose restart gateway
docker-compose restart orchestration-service
```

### Reconstruir un Servicio
```bash
# Si cambias código, reconstruye la imagen
docker-compose up -d --build gateway
docker-compose up -d --build orchestration-service
```

### Acceder a un Contenedor
```bash
# Shell en el contenedor
docker-compose exec gateway sh
docker-compose exec mysql bash

# Acceder a MySQL
docker-compose exec mysql mysql -uroot -proot ticketing
```

## 🗄️ Bases de Datos

Las 3 bases de datos se crean automáticamente al iniciar:

```bash
# Ver bases de datos
docker-compose exec mysql mysql -uroot -proot -e "SHOW DATABASES;"

# Acceder a ticketing
docker-compose exec mysql mysql -uroot -proot ticketing

# Ver tickets
docker-compose exec mysql mysql -uroot -proot ticket_db -e "SELECT * FROM tickets;"
```

## 📊 Arquitectura Docker

```
┌─────────────────────────────────────────────────────────────┐
│                      Docker Network (soa-network)           │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Gateway  │  │   User   │  │  Event   │  │  Ticket  │   │
│  │  :8080   │  │  :8081   │  │  :8082   │  │  :8086   │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
│       │             │             │             │          │
│  ┌────┴─────────────┴─────────────┴─────────────┴─────┐   │
│  │          Orchestration Service :8083              │   │
│  └────┬─────────────┬─────────────────┬─────────────┘   │
│       │             │                 │                  │
│  ┌────┴─────┐  ┌────┴────┐  ┌─────────┴──────┐          │
│  │ Payment  │  │  Notif  │  │     MySQL      │          │
│  │  :8084   │  │  :8085  │  │     :3306      │          │
│  └──────────┘  └─────────┘  └────────────────┘          │
│                                                           │
└───────────────────────────────────────────────────────────┘
```

## 🔧 Variables de Entorno

Configuradas en `docker-compose.yml`:

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysql:3306/ticketing` | URL de BD |
| `SPRING_DATASOURCE_USERNAME` | `root` | Usuario MySQL |
| `SPRING_DATASOURCE_PASSWORD` | `root` | Contraseña MySQL |
| `JWT_SECRET` | `mysecretkey...` | Clave para firmar JWT |
| `GATEWAY_SECRET` | `soa-gateway-secret-key-2024` | Autenticación entre servicios |
| `SERVICES_*_URL` | `http://service-name:port` | URLs de servicios internos |

## 📝 Dockerfiles

Cada servicio necesita un `Dockerfile` en su directorio:

**Ejemplo para user-service:**
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Para crear Dockerfiles para todos los servicios:**
```bash
# Compilar primero
cd user-service && mvn clean package -DskipTests && cd ..
cd event-service && mvn clean package -DskipTests && cd ..
# ... (todos los servicios)

# Crear Dockerfile en cada servicio (ver ejemplo arriba)
```

## 🐛 Troubleshooting

### Puerto 3306 ya está en uso
```bash
# Detener XAMPP MySQL
# O cambiar el puerto en docker-compose.yml:
# ports:
#   - "3307:3306"
```

### Servicio no inicia
```bash
# Ver logs del servicio
docker-compose logs gateway

# Verificar que MySQL está healthy
docker-compose ps mysql
```

### Base de datos no se crea
```bash
# Eliminar volumen y recrear
docker-compose down -v
docker-compose up -d
```

### Cambié código pero no se refleja
```bash
# Recompilar Maven
cd service-name
mvn clean package -DskipTests

# Reconstruir imagen Docker
cd ..
docker-compose up -d --build service-name
```

## 🎯 Ventajas vs Ejecución Local

| Aspecto | Local (XAMPP + Maven) | Docker Compose |
|---------|----------------------|----------------|
| **Setup inicial** | 30+ minutos | 5 minutos |
| **Comandos para iniciar** | 7+ comandos | 1 comando |
| **Limpieza de entorno** | Manual | `docker-compose down -v` |
| **Portabilidad** | Requiere configurar cada máquina | Funciona en cualquier OS con Docker |
| **Escalabilidad** | Manual | Escalar con `docker-compose up --scale` |

## 📚 Referencias

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)

---

✅ Con Docker Compose, todo el sistema SOA se ejecuta con **un solo comando**.
