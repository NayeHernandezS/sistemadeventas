-- Planes de suscripcion: EMPRENDEDOR, NEGOCIO, PRO (ver PlanSuscripcion.java)
-- Si plan_codigo ya existe (error 1060), no ejecutes los ALTER comentados abajo.
USE java_curso;

-- Solo en instalacion nueva (descomenta una vez):
-- ALTER TABLE suscripciones ADD COLUMN plan_codigo VARCHAR(30) NOT NULL DEFAULT 'EMPRENDEDOR';
-- ALTER TABLE pagos_suscripcion ADD COLUMN plan_codigo VARCHAR(30) NOT NULL DEFAULT 'EMPRENDEDOR';

-- Vacios o nulos (compatible con safe updates: WHERE usa id)
UPDATE suscripciones
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0 AND (plan_codigo IS NULL OR TRIM(plan_codigo) = '');

UPDATE pagos_suscripcion
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0 AND (plan_codigo IS NULL OR TRIM(plan_codigo) = '');

-- Codigos que no existen en la app (typos, planes viejos, etc.)
UPDATE suscripciones
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0
  AND UPPER(TRIM(plan_codigo)) NOT IN ('EMPRENDEDOR', 'NEGOCIO', 'PRO');

UPDATE pagos_suscripcion
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0
  AND UPPER(TRIM(plan_codigo)) NOT IN ('EMPRENDEDOR', 'NEGOCIO', 'PRO');

-- --- Verificacion (debe devolver 0 filas en "invalidos") ---
SELECT plan_codigo, COUNT(*) AS total
FROM suscripciones
GROUP BY plan_codigo;

SELECT plan_codigo, COUNT(*) AS total
FROM pagos_suscripcion
GROUP BY plan_codigo;

SELECT id, username, plan_codigo AS invalido
FROM suscripciones
WHERE plan_codigo IS NULL OR TRIM(plan_codigo) = ''
   OR UPPER(TRIM(plan_codigo)) NOT IN ('EMPRENDEDOR', 'NEGOCIO', 'PRO');

SELECT id, username, plan_codigo AS invalido
FROM pagos_suscripcion
WHERE plan_codigo IS NULL OR TRIM(plan_codigo) = ''
   OR UPPER(TRIM(plan_codigo)) NOT IN ('EMPRENDEDOR', 'NEGOCIO', 'PRO');
