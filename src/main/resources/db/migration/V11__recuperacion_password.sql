-- Tokens para recuperacion de contraseña (idempotente)

CREATE TABLE IF NOT EXISTS tokens_recuperacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    token VARCHAR(64) NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    usado TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE INDEX uk_tokens_recuperacion_token (token),
    INDEX idx_tokens_recuperacion_username (username)
);
