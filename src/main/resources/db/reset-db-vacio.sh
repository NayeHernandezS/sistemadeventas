#!/usr/bin/env bash
# Recrea la base java_curso con el esquema actual y tablas vacias (sin datos_ejemplo.sql).
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$DIR/../../../.." && pwd)"

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

cd "$DIR"
echo "Eliminando base java_curso..."
"${MYSQL[@]}" -e "DROP DATABASE IF EXISTS java_curso;"
echo "Creando esquema (tablas vacias)..."
"${MYSQL[@]}" < schema.sql
echo "Listo: java_curso recreada sin datos de ejemplo."
