# Decisiones Técnicas (XML/JSON, SOAP/REST)

## Área: Formato de datos
- **Decisión:** JSON para REST / XML para SOAP
- **Motivo Principal:** Ligero y compatible / interoperabilidad.

## Área: Protocolos
- **Decisión:** REST para nuevos servicios / SOAP para legado.
- **Motivo Principal:** REST es más ligero, escalabilidad / integración con sistemas legados.

## Área: Modelado
- **Decisión:** BPMN 2.0 (Bizagi)
- **Motivo Principal:** Claridad y trazabilidad de procesos.

## Área: Tecnología
- **Decisión:** Node.js + Express (backend) + PostgreSQL
- **Motivo Principal:** Escalabilidad y robustez.

## Área: Seguridad
- **Decisión:** OAuth 2.0 para autenticación de usuarios.
- **Motivo Principal:** Control de acceso moderno, flexible y seguro.

---

## Detalles de las decisiones técnicas

### 1. Formato de Datos: JSON para REST / XML para SOAP
- Se adopta JSON como formato principal en los servicios REST, debido a su ligereza, legibilidad y compatibilidad con aplicaciones web y móviles modernas.
- Para los servicios SOAP, se utiliza XML ya que es el formato estándar en este protocolo, permite validación con esquemas (XSD), y es ampliamente utilizado en entornos empresariales y sistemas legados.
- Estos formatos permiten optimizar la comunicación cliente-servidor en contextos modernos (JSON), y mantener interoperabilidad con sistemas antiguos (XML).

### 2. Protocolo de comunicación: REST y SOAP
- Se implementa REST como principal arquitectura para los servicios nuevos del sistema por su simplicidad, ligereza y compatibilidad con HTTP, ideal para aplicaciones web y móviles.
- Se incluye SOAP solo para servicios de interoperabilidad, por ejemplo, sistemas externos que aún lo utilizan como algunas pasarelas de pago, sistemas gubernamentales o tickets de terceros.
- Esta combinación híbrida permite aprovechar lo mejor de ambos mundos: la escalabilidad y simplicidad de REST, y la robustez e interoperabilidad de SOAP.

### 3. Modelado de Procesos: BPMN 2.0
- Se utiliza BPMN 2.0 (Business Process Model and Notation) para modelar los procesos del sistema (compra de entradas, validación de procesos, etc.) por ser un estándar gráfico comprensible tanto por técnicos como por stakeholders del negocio.
- Su trazabilidad permite mapear fácilmente cada tarea con un servicio específico, alineando procesos con la arquitectura orientada a servicios.
- BPMN mejora la comunicación entre el área técnica y de negocio, facilitando el diseño de procesos orientados a servicios.

### 4. Tecnologías para Implementación: Node.js + Express + PostgreSQL
- Node.js se elige por su eficiencia en operaciones I/O y por su arquitectura orientada a eventos, ideal para manejar múltiples solicitudes simultáneas (como en un sistema de venta de entradas).
- Express.js, como framework minimalista sobre Node, permite construir APIs REST de forma ágil y estructurada.
- PostgreSQL se selecciona como base de datos por su robustez, soporte para integridad referencial, transacciones y extensibilidad (ideal para operaciones financieras como compras).
- Esta stack tecnológica es moderna, escalable y ampliamente soportada por la comunidad, ideal para sistemas basados en microservicios y SOA.

### 5. Seguridad: OAuth 2.0 para Autenticación
- Se adopta OAuth 2.0 como protocolo de autorización, por ser un estándar ampliamente utilizado en sistemas modernos.
- Permite una separación clara entre autenticación y autorización, y facilita la integración futura con redes sociales, apps móviles o APIs de terceros.
- La seguridad es fundamental en la venta de entradas y gestión de usuarios. OAuth 2.0 permite implementar controles de acceso robustos y escalables.