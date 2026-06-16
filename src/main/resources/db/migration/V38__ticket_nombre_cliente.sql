-- Nombre opcional del cliente al finalizar venta (ticket)

ALTER TABLE tickets_venta
    ADD COLUMN nombre_cliente VARCHAR(200) NULL AFTER total;
