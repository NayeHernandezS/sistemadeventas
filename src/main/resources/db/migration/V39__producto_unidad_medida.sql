-- Unidad de medida para insumos de restaurante (inventario interno en unidad base: g, ml o pza)

ALTER TABLE productos
    ADD COLUMN unidad_medida VARCHAR(20) NOT NULL DEFAULT 'pza' AFTER existencias;
