#!/usr/bin/env bash
# Aplica migraciones Fase 1 (clientes, factura.cliente_id, movimientos_inventario).
# Idempotente: seguro reejecutar en bases que ya tienen parte del esquema.
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DB_DIR="$(cd "$DIR/../../src/main/resources/db" && pwd)"
ROOT="$(cd "$DIR/../.." && pwd)"

if [[ -f "$ROOT/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT/.env"
  set +a
fi

DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-}"

MYSQL=(mysql -u"$DB_USER")
if [[ -n "$DB_PASSWORD" ]]; then
  MYSQL+=(-p"$DB_PASSWORD")
else
  MYSQL+=(-p)
fi

MIGRACIONES=(
  migracion_clientes.sql
  migracion_factura_cliente.sql
  migracion_movimientos_inventario.sql
)

echo "=== Migraciones Fase 1 (Sistema de Ventas) ==="
for f in "${MIGRACIONES[@]}"; do
  path="$DB_DIR/$f"
  if [[ ! -f "$path" ]]; then
    echo "ERROR: no existe $path"
    exit 1
  fi
  echo "-> $f"
  "${MYSQL[@]}" < "$path"
done

echo ""
echo "Verificacion rapida:"
"${MYSQL[@]}" -e "
USE java_curso;
SHOW TABLES LIKE 'clientes';
SHOW TABLES LIKE 'movimientos_inventario';
SHOW COLUMNS FROM facturas LIKE 'cliente_id';
"
echo "Listo."
