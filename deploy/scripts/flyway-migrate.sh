#!/usr/bin/env bash
# Ejecuta Flyway migrate contra la BD del .env (sin arrancar la app).
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$DIR/../.." && pwd)"

if [[ -f "$ROOT/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT/.env"
  set +a
fi

export DB_URL="${DB_URL:-jdbc:mysql://localhost:3306/java_curso?serverTimezone=America/Mexico_City}"
export DB_USER="${DB_USER:-root}"
export DB_PASSWORD="${DB_PASSWORD:-}"

if [[ -z "$DB_PASSWORD" ]]; then
  echo "Define DB_PASSWORD en .env"
  exit 1
fi

cd "$ROOT"
echo "=== Flyway migrate ==="
echo "URL: $DB_URL"
mvn -q flyway:migrate
echo "Listo. Revisa flyway_schema_history en java_curso."
