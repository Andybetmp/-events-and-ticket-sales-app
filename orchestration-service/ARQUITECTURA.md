# Orchestration Service - Arquitectura

## 🎯 Propósito

Este servicio **NO tiene lógica de negocio**. Es un **orquestador puro** que coordina flujos complejos entre múltiples microservicios siguiendo el patrón SAGA.

## ✅ Qué SÍ tiene

- **Orquestadores** (`orchestrator/`):
  - `TicketPurchaseOrchestrator`: Coordina compra de tickets (7 pasos)
  - `EventCreationOrchestrator`: Coordina creación de eventos
  - `UserRegistrationOrchestrator`: Coordina registro de usuarios

- **Clientes REST** (`client/`):
  - `UserServiceClient` → user-service:8081
  - `EventServiceClient` → event-service:8082
  - `PaymentServiceClient` → payment-service:8084
  - `NotificationServiceClient` → notification-service:8085
  - `TicketServiceClient` → ticket-service:8086

- **Controladores** (`controller/`):
  - Exponen endpoints que usan los orquestadores
  - Ejemplo: `/api/orchestration/purchase-ticket`

## ❌ Qué NO tiene

- ❌ Modelos de dominio (Ticket, User, Event, etc.)
- ❌ Repositorios JPA
- ❌ Servicios de negocio
- ❌ Acceso directo a bases de datos (JPA deshabilitado)
- ❌ Lógica de validación de negocio

## 🔄 Patrón SAGA

### ¿Qué es SAGA?

En microservicios, NO podemos usar transacciones ACID tradicionales porque cada servicio tiene su propia base de datos. SAGA divide una transacción distribuida en pasos secuenciales.

### Ejemplo: Compra de Ticket

```
PASO 1: Obtener tipo de entrada (event-service)
PASO 2: Validar stock disponible
PASO 3: Obtener información del evento (event-service)
PASO 4: RESERVAR entradas - decrementar stock (event-service) ← COMPENSABLE
PASO 5: Procesar pago (payment-service) ← PUNTO CRÍTICO
        └─ Si FALLA → COMPENSACIÓN: incrementar stock
PASO 6: Crear ticket (ticket-service)
PASO 7: Enviar notificación (notification-service)
```

### Compensación

Si el pago falla después de decrementar stock:

```java
try {
    eventClient.decrementarCantidad(tipoEntradaId, cantidad); // Reserva
    payment = paymentClient.procesarPago(...); // FALLA AQUÍ
} catch (Exception e) {
    // COMPENSACIÓN: Deshacer la reserva
    eventClient.incrementarCantidad(tipoEntradaId, cantidad);
}
```

## 📁 Estructura

```
orchestration-service/
├── orchestrator/          # Lógica de coordinación (SAGA)
│   ├── TicketPurchaseOrchestrator.java
│   ├── EventCreationOrchestrator.java
│   └── UserRegistrationOrchestrator.java
├── client/                # Clientes REST para otros servicios
│   ├── UserServiceClient.java
│   ├── EventServiceClient.java
│   ├── TicketServiceClient.java
│   ├── PaymentServiceClient.java
│   └── NotificationServiceClient.java
├── controller/            # Endpoints REST
│   └── OrchestrationController.java
├── config/                # Configuración de URLs y RestTemplate
│   ├── ServiceUrlsConfig.java
│   ├── RestTemplateConfig.java
│   └── GatewaySecretInterceptor.java
└── dto/                   # DTOs para requests/responses
    ├── PurchaseTicketRequest.java
    └── CreateEventRequest.java
```

## 🔐 Comunicación entre Servicios

Todos los clientes REST añaden el header `X-Gateway-Secret` para autenticación:

```java
HttpHeaders headers = new HttpHeaders();
headers.set("X-Gateway-Secret", gatewaySecret);
HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
restTemplate.postForEntity(url, entity, Map.class);
```

## 📊 Base de Datos

- **orchestration_db**: Base de datos mínima, NO se utiliza
- **JPA**: Deshabilitado (`spring.jpa.hibernate.ddl-auto=none`)
- **Razón**: El orchestrator solo coordina, no persiste datos

Los datos se persisten en los servicios correspondientes:
- Tickets → ticket-service (ticket_db)
- Usuarios → user-service (ticketing)
- Eventos → event-service (ticketing)
- Pagos → payment-service (ticketing)

## 🎓 Principios Aplicados

1. **Separation of Concerns**: Orquestación separada de lógica de negocio
2. **Database per Service**: Cada servicio tiene su propia BD
3. **SAGA Pattern**: Transacciones distribuidas con compensación
4. **Service Mesh**: Comunicación HTTP REST entre servicios
5. **Circuit Breaker Ready**: Preparado para timeout y retry patterns

## 🚀 Ejemplo de Uso

```bash
# Comprar ticket (coordina 5 servicios)
POST http://localhost:8080/api/orchestration/purchase-ticket
Authorization: Bearer <JWT>
{
  "tipoEntradaId": 1,
  "cantidad": 2,
  "paymentMethod": {
    "cardNumber": "4532123456789012",
    "cvv": "123",
    "expiryDate": "12/28",
    "cardHolder": "JUAN PEREZ"
  }
}
```

**Flujo interno:**
1. Gateway valida JWT
2. OrchestrationController.purchaseTicket()
3. TicketPurchaseOrchestrator.orchestratePurchase()
4. Llamadas a: event → payment → ticket → notification
5. Si pago falla → Compensación automática

## 📝 Logs

Los orquestadores tienen logging detallado:

```
═══════════════════════════════════════════════════════════
INICIANDO ORQUESTACIÓN DE COMPRA DE TICKET (CON SAGA)
═══════════════════════════════════════════════════════════
PASO 1: Obteniendo información del tipo de entrada
  ✓ Tipo: VIP, Precio: $150, Disponibles: 50
PASO 2: Validando stock
  ✓ Stock suficiente
PASO 3: Obteniendo información del evento
  ✓ Evento: Rock Fest, Fecha: 2026-06-20
PASO 4: RESERVANDO 2 entradas
  ✓ Entradas RESERVADAS - Stock decrementado
PASO 5: Procesando pago por $300
  ✓ Pago aprobado. Payment ID: PAY-A1B2C3D4
PASO 6: Creando registro de ticket
  ✓ Ticket creado: TKT-X1Y2Z3W4
PASO 7: Enviando notificación
  ✓ Notificación enviada
═══════════════════════════════════════════════════════════
✓ ORQUESTACIÓN COMPLETADA EXITOSAMENTE
═══════════════════════════════════════════════════════════
```

## ⚠️ Casos de Fallo

### Pago Rechazado (Compensación)

```
PASO 4: RESERVANDO 2 entradas
  ✓ Entradas RESERVADAS
PASO 5: Procesando pago por $1500
  ✗ Pago rechazado: Fondos insuficientes
⚠️ Iniciando COMPENSACIÓN - Liberando reserva de 2 entradas
  ✓ Reserva liberada - Stock restaurado
✗ ORQUESTACIÓN FALLIDA: Pago rechazado
```

## 🔗 Referencias

- [Patrón SAGA](https://microservices.io/patterns/data/saga.html)
- [Database per Service](https://microservices.io/patterns/data/database-per-service.html)
- [Service Mesh Pattern](https://microservices.io/patterns/deployment/service-mesh.html)
