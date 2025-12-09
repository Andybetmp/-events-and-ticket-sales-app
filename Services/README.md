# 🎫 SOA Ticketing - Sistema de Venta de Entradas

Sistema completo de venta de entradas basado en arquitectura SOA (Service-Oriented Architecture) con 7 microservicios implementados usando Java + Spring Boot.

## 🏗️ Arquitectura

### Diagrama de Servicios

```
                           ┌─────────────┐
                           │   Cliente   │
                           └──────┬──────┘
                                  │
                                  ▼
                    ┌─────────────────────────┐
                    │   Gateway :8080         │
                    │  (JWT + Enrutamiento)   │
                    └──────┬──────────────────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
   Flujos Directos   Flujos Orquestados   Health Checks
          │                │
          │                ▼
          │    ┌──────────────────────┐
          │    │    Camunda :8083     │ ◄── Coordina flujos complejos
          │    │  (Solo coordina)     │     (Saga + Compensación)
          │    └──────┬───────────────┘
          │           │
          │           ├─────────┬─────────┬─────────┬─────────┐
          │           │         │         │         │         │
          └───────┬───┴─────────┴─────────┴─────────┴─────────┤
                  ▼         ▼         ▼         ▼         ▼         ▼
            ┌─────────┐ ┌────────┐ ┌────────┐ ┌──────────┐ ┌────────┐
            │  User   │ │ Event  │ │ Ticket │ │ Payment  │ │ Notif  │
            │  :8081  │ │ :8082  │ │ :8086  │ │  :8084   │ │ :8085  │
            └────┬────┘ └───┬────┘ └───┬────┘ └────┬─────┘ └───┬────┘
                 │          │          │           │            │
                 ▼          ▼          ▼           ▼            ▼
            ┌──────────────────────────────────────────────────────┐
            │              MySQL :3306                             │                  
            └──────────────────────────────────────────────────────┘
```

### Flujos de Ejemplo

**1. Flujo Directo (Login):**
```
Cliente → Gateway → User-Service → BD → Respuesta
```

**2. Flujo Orquestado (Comprar Ticket - Patrón Saga):**
```
Cliente → Gateway → Camunda-Service
                         ├→ User-Service (validar usuario)
                         ├→ Event-Service (validar evento + stock)
                         ├→ Event-Service (decrementar stock) ◄─┐
                         ├→ Payment-Service (procesar pago)     │ Compensación
                         │    └─ Si falla ──────────────────────┘ (rollback)
                         ├→ Ticket-Service (crear ticket)
                         └→ Notification-Service (enviar email)
                    → Respuesta al Cliente
```

### Microservicios Implementados

- ✅ **Gateway** (puerto 8080): API Gateway con validación JWT centralizada
- ✅ **user-service** (puerto 8081): Autenticación JWT, gestión de usuarios, perfil y cambio de contraseña
- ✅ **event-service** (puerto 8082): CRUD de eventos, tipos de entrada, finalización y validaciones de negocio
- ✅ **camunda-service** (puerto 8083): Orquestación con patrón Saga (sin lógica de negocio)
- ✅ **payment-service** (puerto 8084): Mock de pasarela de pago
- ✅ **notification-service** (puerto 8085): Emails reales vía Gmail SMTP con fallback a logs
- ✅ **ticket-service** (puerto 8086): Gestión de tickets y CRUD completo
- ✅ **Frontend React** (puerto 5173): SPA con gestión de eventos, tickets, perfil y autenticación

## 🚀 Tecnologías

**Backend:**
- Java 17
- Spring Boot 3.1.4 - 3.2.12
- Spring Cloud Gateway 4.0.7
- Spring Security + JWT
- Spring Data JPA + Hibernate
- Spring Mail (Gmail SMTP)
- BCrypt (encriptación de contraseñas)

**Frontend:**
- React 18
- Vite
- React Router DOM
- Axios
- LocalStorage (gestión de sesión)

**Arquitectura:**
- Microservicios con patrón Saga (compensación automática)
- API Gateway centralizado
- Database per Service (ticket_db separada)
- Comunicación REST entre servicios

**Otras Tecnologías:**
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
├── camunda-service/       # Orquestador Saga (puerto 8083)
│   ├── orchestrator/      # TicketPurchaseOrchestrator, EventCreationOrchestrator
│   ├── client/            # Clientes REST a otros servicios
│   └── controller/        # Register, create-event, purchase-ticket, my-tickets
├── payment-service/       # Pasarela de pago mock (puerto 8084)
│   ├── model/             # Payment
│   ├── service/           # PaymentService (rechaza monto > 1000)
│   └── controller/        # POST /api/payments/authorize
├── notification-service/  # Emails + Logs (puerto 8085)
│   ├── service/           # NotificationService (Gmail SMTP + fallback)
│   ├── controller/        # POST /api/notifications/send
│   └── resources/
│       └── application.properties  # Config Gmail SMTP
├── ticket-service/        # Gestión de tickets (puerto 8086)
│   ├── model/             # Ticket
│   ├── repository/        # TicketRepository
│   ├── service/           # TicketService
│   ├── controller/        # CRUD tickets
│   └── dto/               # CreateTicketRequest, TicketResponse
├── start-services-camunda.ps1     # Inicia todos los servicios como jobs
├── stop-services.ps1      # Detiene todos los servicios
├── test-e2e.ps1          # Prueba end-to-end completa
└── pom.xml               # POM padre multi-módulo
```

## 🚀 Inicio Rápido

Existen **2 formas** de ejecutar el sistema:

### Opción 1: Docker Compose (Recomendado) 🐳

**Ventajas:** Setup automático, un solo comando, portátil, incluye frontend.

```bash
# 1. Compilar todos los servicios (backend + frontend)
.\build-docker.ps1

# 2. Acceder a la aplicación
# Abrir navegador en: http://localhost

# 3. Ver logs
docker-compose logs -f

# 4. Detener
docker-compose down
```

**Servicios incluidos:**
- ✅ 8 microservicios backend (puertos 8080-8087)
- ✅ Frontend React (puerto 80)
- ✅ MySQL (puerto 3306)

**Ver documentación completa:** [DOCKER.md](DOCKER.md)

### Opción 2: Ejecución Local (XAMPP + PowerShell)

**Ventajas:** Control directo, debugging fácil.

#### Pre-requisitos

1. **Java 17** instalado - Verifica: `java -version`
2. **Maven** instalado - Verifica: `mvn -version`
3. **Node.js** instalado - Verifica: `node -version`
4. **MySQL** corriendo en XAMPP (puerto 3306, usuario: `root`, password: `root`)

#### Iniciar Servicios Backend

```powershell
cd 'd:\Tareas de programacion\SOA'
.\start-services-camunda.ps1
```

El script:
- Inicia los 8 servicios backend como PowerShell background jobs
- Orden: user → event → camunda → payment → notification → ticket → image → gateway
- Verifica que los 8 puertos estén escuchando (8080-8087)

#### Iniciar Frontend

En una terminal separada:

```powershell
cd Frontend
npm install  # Solo la primera vez
npm run dev
```

El frontend estará disponible en: http://localhost:5173

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

Backend:
```powershell
.\stop-services.ps1
```

Frontend:
```powershell
# Presionar Ctrl+C en la terminal donde corre npm run dev
```

## 🌐 URLs de los Servicios

### Con Docker Compose
| Service | URL | Swagger UI |
|----------|-----|---------|
| **Frontend** | **http://localhost** | - |
| Gateway | http://localhost:8080 | - |
| User Service | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| Event Service | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| Camunda | http://localhost:8083 | http://localhost:8083/swagger-ui.html |
| Payment | http://localhost:8084 | http://localhost:8084/swagger-ui.html |
| Notification | http://localhost:8085 | http://localhost:8085/swagger-ui.html |
| Ticket | http://localhost:8086 | http://localhost:8086/swagger-ui.html |
| Image | http://localhost:8087 | http://localhost:8087/swagger-ui.html |

### Con Ejecución Local
| Service | URL | Swagger UI |
|----------|-----|---------|
| **Frontend** | **http://localhost:5173** | - |
| Gateway | http://localhost:8080 | - |
| User Service | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| Event Service | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| Camunda | http://localhost:8083 | http://localhost:8083/swagger-ui.html |
| Payment | http://localhost:8084 | http://localhost:8084/swagger-ui.html |
| Notification | http://localhost:8085 | http://localhost:8085/swagger-ui.html |
| Ticket | http://localhost:8086 | http://localhost:8086/swagger-ui.html |
| Image | http://localhost:8087 | http://localhost:8087/swagger-ui.html |

**⚠️ Importante**: 
- En **Docker**: Acceder al frontend en `http://localhost` (puerto 80)
- En **Local**: Acceder al frontend en `http://localhost:5173`
- El frontend se comunica automáticamente con el Gateway

## 📝 Funcionalidades Principales

### 1. Autenticación y Autorización
- Registro de usuarios con validación de datos
- Login con JWT (expiración 24h)
- Logout (invalidación del lado del cliente)
- **Restablecimiento de contraseña** vía email con tokens seguros (1 hora de expiración)
- Gestión de perfil de usuario (edición de nombre, apellido, teléfono)
- Cambio de contraseña con validación de contraseña actual
- Sistema de roles (ADMIN, USUARIO) con permisos diferenciados
- Middleware de autenticación en Gateway
- Validación de header secreto entre servicios

### 2. Gestión de Eventos
- CRUD completo de eventos con validaciones de negocio
- Validación de fechas futuras (no permite crear eventos en el pasado)
- Prevención de edición de eventos pasados (excepto para ADMIN)
- Finalización de eventos (cambio de estado ACTIVO → FINALIZADO)
- CRUD completo de tipos de entrada (VIP, General, etc.)
- Validaciones de stock (no permite reducir cantidad por debajo de tickets vendidos)
- Protección contra eliminación de tipos de entrada con ventas existentes
- Control de stock disponible
- Incremento/decremento de cantidad con compensación

### 3. Compra de Tickets (Patrón Saga)
- **Orquestación completa** del proceso de compra
- **Camunda-service NO tiene lógica de negocio**, solo coordina servicios
- **Comunicación vía REST**: Orchestrator → TicketServiceClient → HTTP → Ticket-Service
- **Compensación automática**: Si el pago falla, se restaura el stock
- **Timeout de 30 segundos** para el procesamiento de pago
- **Flujo**: Verificar stock → Decrementar → Procesar pago → Crear ticket (via REST)
- **Rollback**: Si falla, ejecuta `increaseCantidad()` para restaurar

### 4. Procesamiento de Pagos
- Mock de pasarela de pago
- Rechaza automáticamente montos > $1000
- Genera payment_id único
- Registra todos los intentos en base de datos

### 5. Sistema de Notificaciones
- **Emails reales** vía Gmail SMTP (configurable)
- **Fallback a logs** si SMTP falla o no está configurado
- **4 tipos de notificaciones**:
  - BIENVENIDA: Al registrarse
  - EVENTO_CREADO: Al crear un evento
  - TICKET_COMPRADO: Al comprar entradas
  - PASSWORD_RESET: Al solicitar restablecimiento de contraseña (con link único)
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

### Bases de Datos

El sistema utiliza MySQL con separación de bases de datos por servicio:

**`userdb`** - Base de datos H2 (user-service):
- `users` - Autenticación y perfiles de usuario
- `password_reset_tokens` - Tokens de restablecimiento de contraseña (UUID con expiración)

**`ticketing`** - Base de datos compartida MySQL (event, payment):
- `eventos` - Información de eventos
- `tipos_entrada` - Tipos de entrada por evento (VIP, General, etc.)
- `payments` - Registro de todos los intentos de pago

**`ticket_db`** - Base de datos exclusiva (ticket-service):
- `tickets` - Tickets comprados por usuarios

**`orchestration_db`** - Base de datos mínima (orchestration-service):
- No se utiliza - El orchestrator solo coordina servicios vía REST

### Tablas principales

#### `users` (user-service)
- id, email, contrasena, nombre, apellido, telefono, rol, activo
- Gestiona autenticación y perfiles de usuario

#### `password_reset_tokens` (user-service)
- id, user_id, token (UUID único), expiry_date, used
- Tokens seguros de un solo uso para restablecimiento de contraseña
- Expiración automática: 1 hora

#### `eventos` (event-service)
- id, nombre, descripcion, fecha_evento, ubicacion, categoria
- Almacena información de eventos

#### `tipos_entrada` (event-service)
- id, evento_id, nombre, precio, cantidad_disponible
- Define tipos de entrada por evento (VIP, General, etc.)

#### `tickets` (ticket-service)
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
- [x] **User Service** - Registro, login, logout, CRUD usuarios, gestión de perfil con cambio de contraseña
- [x] **Event Service** - CRUD eventos, CRUD tipos de entrada, finalización de eventos, validaciones de negocio
- [x] **Camunda Service** - Patrón Saga con compensación (solo coordina, sin lógica de negocio)
- [x] **Payment Service** - Mock de pasarela (rechaza > $1000)
- [x] **Notification Service** - Gmail SMTP + fallback a logs
- [x] **Ticket Service** - Gestión independiente de tickets con BD propia
- [x] **Frontend React** - SPA completa con autenticación, gestión de eventos, tickets y perfil
- [x] **Comunicación entre servicios** - RestTemplate + REST clients + headers de seguridad
- [x] **Prueba E2E** - Script PowerShell con flujo completo (test-e2e.ps1)
- [x] **Scripts de inicio/parada** - start-services-camunda.ps1, stop-services.ps1, start-frontend.ps1

### Funcionalidades Implementadas ✅

**Autenticación y Seguridad:**
- [x] Autenticación JWT con expiración de 24h
- [x] Logout (invalidación del lado del cliente)
- [x] **Restablecimiento de contraseña vía email** con tokens UUID seguros
- [x] **Sistema de tokens de un solo uso** con expiración de 1 hora
- [x] **Páginas de recuperación de contraseña** (ForgotPassword.jsx, ResetPassword.jsx)
- [x] **Manejo centralizado de excepciones** (GlobalExceptionHandler)
- [x] **4 excepciones personalizadas** (UserNotFoundException, InvalidTokenException, etc.)
- [x] Sistema de roles (ADMIN, USUARIO) con permisos diferenciados
- [x] Gestión de perfil de usuario (edición de datos personales)
- [x] Cambio de contraseña con validación de contraseña actual (BCrypt)
- [x] Validación de header secreto entre Gateway y servicios
- [x] Encriptación de contraseñas con BCrypt
- [x] **Prevención de enumeración de usuarios** en password reset

**Arquitectura y Patrones:**
- [x] Compensación Saga (rollback automático si falla el pago)
- [x] Separación correcta: Orchestration coordina, Ticket-Service maneja lógica de negocio
- [x] Clientes REST (UserServiceClient, EventServiceClient, PaymentServiceClient, NotificationServiceClient, TicketServiceClient)
- [x] Database per Service: ticket_db para ticket-service, userdb (H2) para user-service
- [x] Timeout de 30s en procesamiento de pago

**Gestión de Eventos:**
- [x] CRUD completo de eventos con validaciones
- [x] Validación de fechas futuras (no permite crear eventos en el pasado)
- [x] Prevención de edición de eventos pasados (excepto ADMIN)
- [x] Finalización de eventos (endpoint POST /api/eventos/{id}/finalizar)
- [x] CRUD completo de tipos de entrada (GET, POST, PUT, DELETE)
- [x] Validación de stock (no permite reducir cantidad por debajo de vendidos)
- [x] Protección contra eliminación de tipos con ventas existentes
- [x] Gestión de stock con incremento/decremento compensado

**Notificaciones y Comunicación:**
- [x] Emails reales vía Gmail SMTP con fallback a logs
- [x] 4 tipos de notificaciones (bienvenida, evento creado, ticket comprado, **password reset**)
- [x] Procesamiento asíncrono de emails
- [x] **Templates de email personalizados** para cada tipo de notificación

**Documentación y Herramientas:**
- [x] Swagger UI en todos los servicios backend
- [x] Scripts de automatización (start-services-camunda.ps1, stop-services.ps1, test-e2e.ps1)
- [x] Diagramas BPMN (proceso de compra, compensación Saga)
- [x] Colección Postman con endpoints documentados

### Backend Completado ✅

El backend del sistema está prácticamente completo con todas las funcionalidades core implementadas. 

## 🐛 Solución de Problemas

### Servicios no inician

**Verificar que MySQL esté corriendo:**
```powershell
Get-Process mysqld -ErrorAction SilentlyContinue
```
Si no aparece, inicia XAMPP y arranca MySQL.

**Ver qué puertos están ocupados:**
```powershell
Get-NetTCPConnection -LocalPort 8080,8081,8082,8083,8084,8085,8086 -State Listen
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
1. Verifica los logs de camunda-service (busca "COMPENSACIÓN")
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
- 8 microservicios independientes (7 backend + 1 frontend)
- Patrón Saga con compensación automática
- Separación correcta: Camunda coordina, servicios manejan su lógica de negocio
- Gateway centralizado con JWT y validación de roles
- Frontend React con rutas protegidas y gestión completa de eventos/tickets
- **Sistema completo de recuperación de contraseña** con tokens seguros vía email
- **Manejo centralizado de excepciones** con respuestas estandarizadas
- Emails reales con Gmail SMTP + fallback automático
- Sistema completo de compra de tickets con validaciones de negocio
- Gestión de perfil de usuario con cambio seguro de contraseña
- Validaciones de fechas y stock para eventos
- CRUD completo de tipos de entrada con protecciones

---

✅ **Sistema funcional y probado**

Última actualización: 2025-12-07
