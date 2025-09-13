# Servicios REST

## UserService (Gestión de Usuarios)
- **Endpoint Base:** `/api/users`
- **Funciones principales:**
  - Registro y autenticación de usuarios.
  - Gestión del perfil (consultar, actualizar y eliminar).
  - Listado de organizadores registrados.

## EventService (Gestión de Eventos)
- **Endpoint Base:** `/api/events`
- **Funciones principales:**
  - Administración del ciclo de vida de eventos: crear, listar, consultar detalles, actualizar o cancelar.
  - Publicación de eventos.
  - Visualización de eventos por organizador.

## TicketService (Gestión de Entradas)
- **Endpoint Base:** `/api/tickets`
- **Funciones principales:**
  - Gestión de entradas: crear tipos de entradas, consultar disponibilidad, reservar y comprar.
  - Consulta de entradas adquiridas por el usuario.

## PaymentService (Gestión de Pagos)
- **Endpoint Base:** `/api/payments`
- **Funciones principales:**
  - Procesamiento de transacciones.
  - Verificación de estado de pagos.
  - Emisión de reembolsos.
  - Listado de métodos de pago disponibles.
  - Webhook para integración con pasarelas de pago externas.

## NotificationService (Gestión de Notificaciones)
- **Endpoint Base:** `/api/notifications`
- **Funciones principales:**
  - Envío de notificaciones individuales y masivas.
  - Consulta de notificaciones por usuario.
  - Marcado de notificaciones como leídas.
  - Acceso a plantillas prediseñadas para distintos tipos de mensajes.

---

# Servicios SOAP

## ReportingService (Reportes y Analytics)
- **WSDL:** `/soap/reporting?wsdl`
- **Funciones principales:**
  - Generación de reportes de ventas y asistencia.
  - Obtención de estadísticas de eventos.
  - Creación de resumen financiero por organizador.
  - Exportación de datos en distintos formatos.

## Enterprise Integration Service (Integración Empresarial)
- **WSDL:** `/soap/enterprise?wsdl`
- **Funciones principales:**
  - Integración con sistemas externos.
  - Sincronización de clientes.
  - Importación masiva de eventos.
  - Generación de facturas electrónicas.
  - Validación de licencias comerciales.
  - Gestión de solicitudes de reembolso.

## AuditSecurityService (Auditoría y Seguridad)
- **WSDL:** `/soap/audit?wsdl`
- **Funciones principales:**
  - Registro de actividades de usuario.
  - Validación de tokens de seguridad.
  - Generación de reportes de auditoría.
  - Detección de actividades fraudulentas.
  - Aplicación de políticas de seguridad sobre usuarios o entidades.
