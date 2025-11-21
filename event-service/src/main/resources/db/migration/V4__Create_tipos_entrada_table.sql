-- Eliminar columnas de tipos de entrada fijos y precio_entrada
ALTER TABLE eventos DROP COLUMN IF EXISTS precio_entrada;
ALTER TABLE eventos DROP COLUMN IF EXISTS precio_general;
ALTER TABLE eventos DROP COLUMN IF EXISTS precio_vip;
ALTER TABLE eventos DROP COLUMN IF EXISTS precio_platinum;
ALTER TABLE eventos DROP COLUMN IF EXISTS entradas_general;
ALTER TABLE eventos DROP COLUMN IF EXISTS entradas_vip;
ALTER TABLE eventos DROP COLUMN IF EXISTS entradas_platinum;

-- Crear tabla de tipos de entrada dinámicos
CREATE TABLE IF NOT EXISTS tipos_entrada (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    evento_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    precio DECIMAL(12, 2) NOT NULL,
    cantidad_total INT NOT NULL,
    cantidad_disponible INT NOT NULL,
    orden INT DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (evento_id) REFERENCES eventos(id) ON DELETE CASCADE,
    INDEX idx_evento_id (evento_id),
    INDEX idx_evento_activo (evento_id, activo),
    INDEX idx_orden (evento_id, orden),
    UNIQUE KEY uk_evento_nombre (evento_id, nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
