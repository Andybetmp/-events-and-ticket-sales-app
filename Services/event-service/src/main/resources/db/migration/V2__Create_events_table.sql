CREATE TABLE IF NOT EXISTS eventos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(1000) NOT NULL,
    ubicacion VARCHAR(200) NOT NULL,
    fecha_evento DATETIME NOT NULL,
    capacidad_total INT NOT NULL,
    entradas_disponibles INT NOT NULL,
    precio_entrada DECIMAL(12, 2) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_fecha_evento (fecha_evento),
    INDEX idx_categoria (categoria),
    INDEX idx_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
