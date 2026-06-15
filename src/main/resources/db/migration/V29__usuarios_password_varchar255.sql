-- BD legacy: password suele ser VARCHAR(50); BCrypt + prefijo {bcrypt} necesita ~68+ caracteres.
ALTER TABLE usuarios MODIFY password VARCHAR(255) NOT NULL;
