# 🎫 SOA Ticketing - Sistema de Venta de Entradas

Sistema completo de venta de entradas basado en arquitectura SOA (Service-Oriented Architecture) con 6 microservicios implementados usando Java + Spring Boot.

## 🏗️ Arquitectura

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │
       ▼
┌──────────────────────┐
│  Gateway :8080       │ ✅ Spring Cloud Gateway + JWT
└──────┬───────────────┘
       │
       ├──────────────────────────────────────────┐
       │                │                │        │
       ▼                ▼                ▼        ▼
┌─────────────┐  ┌──────────────┐  ┌──────────┐  ┌─────────────┐
│User Service │  │Event Service │  │Payment   │  │Notification │
│   :8081     │  │   :8082      │  │ :8084    │  │  :8085      │
└─────────────┘  └──────────────┘  └──────────┘  └─────────────┘
                        │
                        ▼
              ┌──────────────────┐
              │ Orchestration    │
              │   :8083          │
              └──────────────────┘
                        │
                        ▼
                  ┌──────────┐
                  │  MySQL   │
                  │  :3306   │
                  └──────────┘
```

### Microservicios Implementados

- ✅ **Gateway** (puerto 8080): API Gateway con validación JWT centralizada
- ✅ **user-service** (puerto 8081): Autenticación JWT, gestión de usuarios, logout
- ✅ **event-service** (puerto 8082): CRUD de eventos y tipos de entrada
- ✅ **orchestration-service** (puerto 8083): Orquestación con patrón Saga y compensación
- ✅ **payment-service** (puerto 8084): Mock de pasarela de pago
- ✅ **notification-service** (puerto 8085): Emails reales vía Gmail SMTP con fallback a logs

## 🚀 Tecnologías

- **Backend**: Java 17, Spring Boot 3.1.4 - 3.2.12
- **Arquitectura**: Microservicios con patrón Saga (compensación automática)
- **Seguridad**: Spring Security + JWT (validación en Gateway)
- **Gateway**: Spring Cloud Gateway 4.0.7
- **Base de Datos**: MySQL 8.0 (XAMPP)
- **ORM**: Spring Data JPA + Hibernate
- **Email**: Spring Mail + Gmail SMTP
- **Documentación API**: Springdoc OpenAPI (Swagger UI)
- **Async Processing**: @EnableAsync para notificaciones
- **RestTemplate**: Comunicación entre microservicios

## 📦 Estructura del Proyecto

```
SOA/
├── gateway/                # API Gateway (puerto 8080)
│   ├── controller/         # Health endpoints
│   ├── filter/            # Filtro JWT global
│   └── service/           # Validación de tokens
├── user-service/          # Autenticación y usuarios (puerto 8081)
│   ├── model/             # Entidad User
│   ├── repository/        # UserRepository
│   ├── service/           # AuthService, UserService
│   ├── controller/        # Registro, login, logout, CRUD
│   ├── config/            # Security, JWT, GatewayAuthFilter
│   └── resources/
│       └── db/migration/  # Scripts Flyway
├── event-service/         # Gestión de eventos (puerto 8082)
│   ├── model/             # Evento, TipoEntrada
│   ├── repository/        # Repositorios JPA
│   ├── service/           # Lógica de negocio
│   └── controller/        # CRUD eventos y tipos de entrada
├── orchestration-service/ # Orquestador Saga (puerto 8083)
│   ├── model/             # Ticket
│   ├── orchestrator/      # TicketPurchaseOrchestrator con compensación
│   ├── client/            # Clientes REST a otros servicios
│   └── controller/        # Register, create-event, purchase-ticket
├── payment-service/       # Pasarela de pago mock (puerto 8084)
│   ├── model/             # Payment
│   ├── service/           # PaymentService (rechaza monto > 1000)
│   └── controller/        # POST /api/payments/authorize
├── notification-service/  # Emails + Logs (puerto 8085)
│   ├── service/           # NotificationService (Gmail SMTP + fallback)
│   ├── controller/        # POST /api/notifications/send
│   └── resources/
│       └── application.properties  # Config Gmail SMTP
├── start-all.ps1          # Inicia todos los servicios
├── stop-services.ps1      # Detiene todos los servicios
├── test-e2e.ps1          # Prueba end-to-end completa
└── pom.xml               # POM padre multi-módulo
```

## 🚀 Inicio Rápido

### Pre-requisitos

1. **Java 17** instalado - Verifica: `java -version`
2. **Maven** instalado - Verifica: `mvn -version`
3. **MySQL** corriendo en XAMPP (puerto 3306, usuario: `root`, password: `root`)

### Iniciar Todos los Servicios

```powershell
cd 'd:\Tareas de programacion\SOA'
.\start-all.ps1
```

El script:
- Compila todos los servicios
- Inicia en orden: user → event → orchestration → payment → notification → gateway
- Espera entre cada servicio para que se registren correctamente

### Verificar que Todo Funciona

```powershell
.\test-e2e.ps1
```

Esto ejecuta un flujo completo:
1. Registro de usuario → Email de bienvenida
2. Login → Token JWT
3. Creación de evento → Email de evento creado
4. Compra de ticket → Email de confirmación
5. Consulta de tickets del usuario

### Detener Todos los Servicios

```powershell
.\stop-services.ps1
```

## 🌐 URLs de los Servicios

| Servicio | URL | Swagger |
|----------|-----|---------|
| Gateway | http://localhost:8080 | - |
| User Service | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| Event Service | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| Orchestration | http://localhost:8083 | http://localhost:8083/swagger-ui.html |
| Payment | http://localhost:8084 | http://localhost:8084/swagger-ui.html |
| Notification | http://localhost:8085 | http://localhost:8085/swagger-ui.html |

**⚠️ Importante**: Siempre acceder a través del Gateway (puerto 8080), no directamente a los servicios.

## 📝 Funcionalidades Principales

### 1. Autenticación y Autorización
- Registro de usuarios con validación de datos
- Login con JWT (expiración 24h)
- Logout (invalidación del lado del cliente)
- Middleware de autenticación en Gateway
- Validación de header secreto entre servicios

### 2. Gestión de Eventos
- CRUD completo de eventos
- Gestión de tipos de entrada (VIP, General, etc.)
- Control de stock disponible
- Incremento/decremento de cantidad con compensación

### 3. Compra de Tickets (Patrón Saga)
- **Orquestación completa** del proceso de compra
- **Compensación automática**: Si el pago falla, se restaura el stock
- **Timeout de 30 segundos** para el procesamiento de pago
- **Flujo**: Verificar stock → Decrementar → Procesar pago → Crear ticket
- **Rollback**: Si falla, ejecuta `increaseCantidad()` para restaurar

### 4. Procesamiento de Pagos
- Mock de pasarela de pago
- Rechaza automáticamente montos > $1000
- Genera payment_id único
- Registra todos los intentos en base de datos

### 5. Sistema de Notificaciones
- **Emails reales** vía Gmail SMTP (configurable)
- **Fallback a logs** si SMTP falla o no está configurado
- **3 tipos de notificaciones**:
  - BIENVENIDA: Al registrarse
  - EVENTO_CREADO: Al crear un evento
  - TICKET_COMPRADO: Al comprar entradas
- **Procesamiento asíncrono** con @Async

## 🔐 Seguridad

### Flujo de Autenticación

```
Cliente → Gateway (valida JWT) → Servicio (valida X-Gateway-Secret)
```

1. Cliente envía JWT en header `Authorization: Bearer <token>`
2. Gateway valida el token y extrae el email del usuario
3. Gateway añade headers:
   - `X-Gateway-Secret`: Secreto compartido
   - `X-User-Email`: Email extraído del JWT
4. Servicio valida el header secreto y confía en X-User-Email

### Características de Seguridad
- Contraseñas hasheadas con BCrypt
- Tokens JWT firmados con HMAC-SHA256
- Acceso directo a servicios bloqueado (solo via Gateway)
- Header secreto compartido entre Gateway y servicios
- CORS configurado en Gateway

## 🗄️ Base de Datos

### Base de Datos: `ticketing`

**Tablas principales:**

#### `users` (user-service)
- id, email, contrasena, nombre, apellido, telefono, rol, activo
- Gestiona autenticación y perfiles de usuario

#### `eventos` (event-service)
- id, nombre, descripcion, fecha_evento, ubicacion, categoria
- Almacena información de eventos

#### `tipos_entrada` (event-service)
- id, evento_id, nombre, precio, cantidad_disponible
- Define tipos de entrada por evento (VIP, General, etc.)

#### `tickets` (orchestration-service)
- id, ticket_id, usuario_id, tipo_entrada_id, evento_nombre, cantidad, total_pagado, payment_id
- Registra tickets comprados por usuarios

#### `payments` (payment-service)
- id, payment_id, monto, status, card_last_four, mensaje
- Registra todos los intentos de pago

**Configuración:**
- Host: localhost:3306
- Usuario: root
- Contraseña: root
- Las tablas se crean automáticamente con Hibernate (`ddl-auto=create` o `validate`)

## ⚙️ Configuración

### Configurar Emails con Gmail

Para enviar emails reales, edita `notification-service/src/main/resources/application.properties`:

1. **Activa verificación en 2 pasos** en tu Gmail:
   - https://myaccount.google.com/security

2. **Genera contraseña de aplicación**:
   - https://myaccount.google.com/apppasswords
   - Nombre: "SOA Notification Service"
   - Copia la contraseña de 16 caracteres

3. **Actualiza application.properties**:
```properties
spring.mail.username=tu_email@gmail.com
spring.mail.password=xxxx xxxx xxxx xxxx
```

4. **Recompila y reinicia** notification-service

Si no configuras Gmail, los emails se simulan en logs (fallback automático).

### Variables de Entorno Importantes

**Gateway** (`gateway.secret`):
- Secreto compartido: `soa-gateway-secret-key-2024`
- Debe ser igual en Gateway y todos los servicios

**JWT** (`jwt.secret`):
- Clave de firma para tokens JWT
- Por defecto: `mysecretkeymysecretkeymysecretkeymysecretkey`
- Expiración: 24 horas (86400000 ms)

## 📋 Estado del Proyecto

### Completado ✅

- [x] **Gateway** con validación JWT centralizada
- [x] **User Service** - Registro, login, logout, CRUD usuarios
- [x] **Event Service** - CRUD eventos y tipos de entrada
- [x] **Orchestration Service** - Patrón Saga con compensación automática
- [x] **Payment Service** - Mock de pasarela (rechaza > $1000)
- [x] **Notification Service** - Gmail SMTP + fallback a logs
- [x] **Comunicación entre servicios** - RestTemplate + headers de seguridad
- [x] **Prueba E2E** - Script PowerShell con flujo completo
- [x] **Scripts de inicio/parada** - start-all.ps1, stop-services.ps1

### Funcionalidades Implementadas ✅

- [x] Autenticación JWT con expiración de 24h
- [x] Logout (invalidación del lado del cliente)
- [x] Compensación Saga (rollback automático si falla el pago)
- [x] Timeout de 30s en procesamiento de pago
- [x] Emails reales vía Gmail SMTP con fallback a logs
- [x] 3 tipos de notificaciones (bienvenida, evento creado, ticket comprado)
- [x] Procesamiento asíncrono de emails
- [x] Validación de header secreto entre Gateway y servicios
- [x] Swagger UI en todos los servicios
- [x] Gestión de stock con incremento/decremento compensado

### Pendiente ⏳

- [ ] Colección Postman con tests automatizados
- [ ] Observabilidad (Actuator + Prometheus + Grafana)
- [ ] Docker Compose para todos los servicios
- [ ] CI/CD pipeline
- [ ] Tests unitarios y de integración

## 🐛 Solución de Problemas

### Servicios no inician

**Verificar que MySQL esté corriendo:**
```powershell
Get-Process mysqld -ErrorAction SilentlyContinue
```
Si no aparece, inicia XAMPP y arranca MySQL.

**Ver qué puertos están ocupados:**
```powershell
Get-NetTCPConnection -LocalPort 8080,8081,8082,8083,8084,8085 -State Listen
```

**Detener todos los servicios Java:**
```powershell
Get-Process java | Where-Object { $_.Path -notlike "*redhat.java*" } | Stop-Process -Force
```

### Error: "Authentication failed" en emails

Si ves errores de autenticación SMTP en notification-service:
1. Verifica que la contraseña de aplicación de Gmail sea correcta
2. Asegúrate de que la verificación en 2 pasos esté activada
3. El sistema usa fallback automático a logs si SMTP falla

### Compensación Saga no funciona

Si el stock no se restaura cuando el pago falla:
1. Verifica los logs de orchestration-service (busca "COMPENSACIÓN")
2. Asegúrate de que event-service tenga el endpoint PUT /{id}/incrementar
3. Revisa que eventClient esté configurado correctamente

### Gateway devuelve 404

Si el Gateway no encuentra las rutas:
1. Verifica que el servicio destino esté corriendo
2. Revisa gateway/src/main/resources/application.yml
3. Asegúrate de que todos los servicios hayan iniciado correctamente

## 📚 Recursos Adicionales

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Patrón Saga](https://microservices.io/patterns/data/saga.html)
- [JWT.io](https://jwt.io/) - Debugger de tokens JWT

## 👥 Equipo

Proyecto académico - Sistema de Venta de Entradas SOA

**Características principales del proyecto:**
- 6 microservicios independientes
- Patrón Saga con compensación automática
- Gateway centralizado con JWT
- Emails reales con Gmail SMTP
- Sistema completo de compra de tickets

---

✅ **Sistema funcional y probado**

Última actualización: 2025-11-15
