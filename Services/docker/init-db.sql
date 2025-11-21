-- ═══════════════════════════════════════════════════════════════════════════
-- Script de Inicialización de Bases de Datos - Sistema SOA Ticketing
-- ═══════════════════════════════════════════════════════════════════════════
--
-- Este script se ejecuta AUTOMÁTICAMENTE al iniciar el contenedor MySQL
-- y crea las 3 bases de datos necesarias para el sistema.
--
-- BASES DE DATOS:
--   1. ticketing        - Compartida por user, event, payment services
--   2. ticket_db        - Exclusiva para ticket-service
--   3. orchestration_db - Para orchestration-service (no se usa, pero se crea)
--
-- ═══════════════════════════════════════════════════════════════════════════

-- Base de datos principal (ya creada por MYSQL_DATABASE, pero la recreamos por si acaso)
CREATE DATABASE IF NOT EXISTS `ticketing` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Base de datos exclusiva para ticket-service
CREATE DATABASE IF NOT EXISTS `ticket_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Base de datos para orchestration-service (no se usa porque el orchestrator solo coordina)
CREATE DATABASE IF NOT EXISTS `orchestration_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verificar que las bases de datos fueron creadas
SHOW DATABASES;

-- Mensaje de confirmación
SELECT 'Bases de datos creadas exitosamente: ticketing, ticket_db, orchestration_db' AS Status;
