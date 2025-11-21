-- Tabla de tickets (compras realizadas)
CREATE TABLE tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id VARCHAR(50) UNIQUE NOT NULL,
    usuario_id BIGINT NOT NULL,
    tipo_entrada_id BIGINT NOT NULL,
    evento_nombre VARCHAR(200) NOT NULL,
    tipo_entrada_nombre VARCHAR(100) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    total_pagado DECIMAL(10,2) NOT NULL,
    payment_id VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PAGADO' COMMENT 'PAGADO, CANCELADO',
    fecha_compra TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_usuario_id (usuario_id),
    INDEX idx_ticket_id (ticket_id),
    INDEX idx_payment_id (payment_id),
    INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
