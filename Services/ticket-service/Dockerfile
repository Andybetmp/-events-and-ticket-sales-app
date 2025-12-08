# ═══════════════════════════════════════════════════════════════════════════
# Dockerfile - Ticket Service
# ═══════════════════════════════════════════════════════════════════════════
# 
# Servicio de gestión de tickets - Dueño del dominio Ticket.
# Base de datos: ticket_db (exclusiva)
# Puerto: 8086
#
# ═══════════════════════════════════════════════════════════════════════════

FROM eclipse-temurin:17-jdk-jammy

LABEL maintainer="SOA Ticketing Team"
LABEL service="ticket-service"
LABEL version="1.0"

WORKDIR /app

# Copiar el JAR compilado
COPY target/ticket-service-*.jar app.jar

# Exponer puerto 8086
EXPOSE 8086

# Configuración JVM
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Ejecutar aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
