-- Agregar columna estado a tabla eventos
ALTER TABLE eventos ADD COLUMN estado VARCHAR(20) DEFAULT 'ACTIVO';

-- Migrar datos existentes: si activo=true entonces ACTIVO, si activo=false entonces CANCELADO
UPDATE eventos SET estado = CASE WHEN activo = true THEN 'ACTIVO' ELSE 'CANCELADO' END;

-- Hacer la columna NOT NULL después de migrar datos (sintaxis compatible con MySQL 5.5)
ALTER TABLE eventos MODIFY COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO';
