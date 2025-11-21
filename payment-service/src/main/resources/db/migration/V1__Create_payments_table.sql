-- Tabla de pagos para auditoría financiera
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id VARCHAR(50) UNIQUE NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL COMMENT 'APPROVED, REJECTED',
    card_last_four VARCHAR(4),
    mensaje VARCHAR(255),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_payment_id (payment_id),
    INDEX idx_status (status),
    INDEX idx_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
