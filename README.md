# -events-and-ticket-sales-app

#Brief ejecutivo (problema, objetivos, alcance)
# 1.1.1. Descripción del Problema

En la actualidad, la gestión de eventos y venta de entradas presenta múltiples desafíos tanto para organizadores como para asistentes. 

## Problemas para organizadores:
- **Gestión fragmentada:** Uso de herramientas separadas para crear eventos, vender entradas, gestionar asistentes y generar reportes.
- **Falta de integración:** Sistemas aislados que no se comunican entre sí, generando duplicación de datos y procesos ineficientes.
- **Escalabilidad limitada:** Soluciones que no pueden adaptarse al crecimiento del negocio o picos de demanda.
- **Control de acceso deficiente:** Dificultades para validar entradas y controlar el acceso a eventos.
- **Reporting insuficiente:** Falta de herramientas analíticas para tomar decisiones basadas en datos.

## Problemas para usuarios finales:
- **Experiencia de usuario fragmentada:** Procesos de compra deficientes.
- **Falta de información centralizada:** Dificultad para encontrar eventos relevantes.
- **Problemas de pago:** Limitadas opciones de pago y procesos inseguros.
- **Gestión de entradas:** Dificultades para acceder, transferir o gestionar entradas adquiridas.

---

# Planteamiento de Alternativas de Solución

## Aplicación web

Para un sistema de gestión de eventos y venta de entradas basado en Arquitectura Orientada a Servicios (SOA), una aplicación web resulta especialmente adecuada. Permite que los usuarios (asistentes, organizadores y administradores) accedan al sistema desde cualquier dispositivo con navegador, facilitando la publicación de eventos, la consulta de disponibilidad y la compra de entradas sin instalaciones previas.

En un enfoque SOA, los componentes (servicios de catálogo de eventos, pasarela de pagos, gestión de usuarios, notificaciones) se exponen como APIs que la capa web consume, lo que acelera la integración y el despliegue continuo. Además, el mantenimiento y las actualizaciones se realizan de forma centralizada en el servidor, lo que simplifica la gestión operativa de un sistema que debe estar disponible en picos de demanda (ventas masivas en preventa).

**Limitaciones:** La experiencia de uso y ciertas funciones críticas (por ejemplo, ventas en zonas con mala conectividad) pueden verse limitadas por la dependencia de internet y por variaciones en el rendimiento según el navegador.

### Ventajas:
- Accesibilidad inmediata desde cualquier dispositivo con navegador (usuarios y organizadores).
- Facilita la integración y consumo de servicios SOA/REST (pagos, inventario de asientos, reportes).
- Mantenimiento y despliegue centralizados (útil para corregir errores rápidamente durante eventos).
- Escalabilidad para soportar picos de tráfico (preventas y lanzamientos).
- Mejora del posicionamiento y descubrimiento de eventos vía SEO.

## Aplicación móvil

Una aplicación móvil nativa puede complementar o ser alternativa a la web para mejorar la experiencia de asistentes y organizadores. En el contexto de ventas de entradas, una app nativa permite notificaciones push para anunciar preventas, recordatorios y promociones; además puede integrar de forma directa funciones del dispositivo (cámara para escanear códigos QR, geolocalización para eventos cercanos, almacenamiento local para entradas offline).

Si el sistema basado en SOA expone servicios bien documentados, la app móvil consumirá esos servicios de forma eficiente. No obstante, el desarrollo y mantenimiento de apps nativas para múltiples plataformas (iOS/Android) incrementa costos y proceso de actualización (tiempos de aprobación en tiendas), lo que hay que justificar por el valor añadido (p. ej. fidelización de usuarios frecuentes o acceso offline avanzado).

### Ventajas:
- Experiencia de usuario más fluida y optimizada para interacción durante el evento.
- Notificaciones push para engagement (preventas, cambios de horario, promociones).
- Integración con hardware del dispositivo (cámara para escaneo QR, geolocalización).
- Mejor soporte para funcionalidades offline parciales (guardar entradas en caché).

## Aplicación de escritorio

Las aplicaciones de escritorio suelen ser apropiadas para módulos administrativos o de gestión interna dentro del proyecto, por ejemplo, herramientas avanzadas para administración de eventos, generación de reportes masivos, o control de accesos en el backend del organizador.

Ofrecen rendimiento y capacidad de procesamiento superiores para operaciones complejas (exportes, análisis) y trabajo offline completo cuando se requiera. Sin embargo, no son la mejor opción para la interacción del público general por su baja portabilidad y la necesidad de instalar software específico en cada equipo; por ello, en un sistema SOA su uso suele limitarse a clientes administrativos o kioscos locales.

### Ventajas:
- Rendimiento óptimo para tareas administrativas complejas y análisis de datos.
- Funcionalidad offline completa para entornos controlados (p. ej. taquillas en recintos sin conexión fiable).
- Interfaz y flujos familiares para usuarios corporativos (staff de producción y finanzas).

---

# 1.2. Definición de Objetivos

## 1.2.1. Objetivo General

Diseñar e implementar un sistema integral de gestión de eventos y venta de entradas basado en Arquitectura Orientada a Servicios (SOA) que permita a organizadores crear y administrar eventos de manera eficiente, mientras proporciona a los usuarios finales una plataforma intuitiva para descubrir, comprar y gestionar entradas digitales.

## 1.2.2. Objetivos Específicos

- Analizar los requerimientos funcionales y no funcionales del sistema de gestión de eventos identificando las necesidades específicas de organizadores y usuarios finales.
- Diseñar una arquitectura SOA robusta que garantice la modularidad, escalabilidad y reutilización de servicios para el sistema de gestión de eventos.
- Implementar servicios especializados para la gestión de eventos, usuarios, pagos, notificaciones y reportes siguiendo los principios de SOA.
- Desarrollar una interfaz web responsiva que proporcione una experiencia de usuario intuitiva tanto para organizadores como para compradores de entradas.
- Integrar servicios de terceros como pasarelas de pago, servicios de correo electrónico y sistemas de notificaciones para enriquecer la funcionalidad del sistema.
- Implementar mecanismos de seguridad apropiados para proteger la información sensible de usuarios y transacciones financieras.
- Realizar pruebas exhaustivas del sistema para validar el correcto funcionamiento de todos los servicios y la integración entre componentes.
- Documentar la arquitectura y servicios desarrollados para facilitar el mantenimiento y futuras extensiones del sistema.

## 1.2.3. Alcances y Limitaciones

### Alcances:
- Gestión completa de eventos: creación, modificación, cancelación y reportes de eventos.
- Sistema de venta de entradas: compra, pago y entrega digital de entradas.
- Gestión de usuarios: registro, autenticación y perfiles de organizadores y compradores.
- Integración de pagos: procesamiento seguro de transacciones financieras.
- Panel de administración: herramientas completas para organizadores.
- Notificaciones automatizadas: sistema de comunicación con usuarios.
- Reportes y analytics: generación de informes de ventas y asistencia.
- Aplicación web responsiva: compatible con dispositivos móviles y desktop.

### Limitaciones:
- Plataforma única: solo implementación web, sin aplicaciones móviles nativas.
- Idioma: sistema desarrollado únicamente en español.
- Pagos: integración limitada a pasarelas de pago específicas disponibles en Perú.
- Geolocalización: enfocado principalmente en el mercado peruano.
- Capacidad: sistema diseñado para eventos de hasta 10,000 asistentes.
- Tipos de evento: limitado a eventos presenciales (no virtuales o híbridos).
- Integración externa: limitada a servicios específicos identificados en el análisis.
