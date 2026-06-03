-- Planes de suscripcion: EMPRENDEDOR, NEGOCIO, PRO (ver PlanSuscripcion.java)
-- Idempotente: se puede ejecutar varias veces sin error 1060

SET @exists_plan_sus := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'suscripciones'
      AND COLUMN_NAME = 'plan_codigo'
);
SET @sql_plan_sus := IF(
    @exists_plan_sus = 0,
    'ALTER TABLE suscripciones ADD COLUMN plan_codigo VARCHAR(30) NOT NULL DEFAULT ''EMPRENDEDOR''',
    'SELECT 1'
);
PREPARE stmt_plan_sus FROM @sql_plan_sus;
EXECUTE stmt_plan_sus;
DEALLOCATE PREPARE stmt_plan_sus;

SET @exists_plan_pagos := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'pagos_suscripcion'
      AND COLUMN_NAME = 'plan_codigo'
);
SET @sql_plan_pagos := IF(
    @exists_plan_pagos = 0,
    'ALTER TABLE pagos_suscripcion ADD COLUMN plan_codigo VARCHAR(30) NOT NULL DEFAULT ''EMPRENDEDOR''',
    'SELECT 1'
);
PREPARE stmt_plan_pagos FROM @sql_plan_pagos;
EXECUTE stmt_plan_pagos;
DEALLOCATE PREPARE stmt_plan_pagos;

UPDATE suscripciones
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0 AND (plan_codigo IS NULL OR TRIM(plan_codigo) = '');

UPDATE pagos_suscripcion
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0 AND (plan_codigo IS NULL OR TRIM(plan_codigo) = '');

UPDATE suscripciones
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0
  AND UPPER(TRIM(plan_codigo)) NOT IN ('EMPRENDEDOR', 'NEGOCIO', 'PRO');

UPDATE pagos_suscripcion
SET plan_codigo = 'EMPRENDEDOR'
WHERE id > 0
  AND UPPER(TRIM(plan_codigo)) NOT IN ('EMPRENDEDOR', 'NEGOCIO', 'PRO');

-- Verificacion (debe devolver 0 filas en "invalidos")
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
