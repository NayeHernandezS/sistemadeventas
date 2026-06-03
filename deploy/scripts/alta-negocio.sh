#!/usr/bin/env bash
# Guia interactiva para dar de alta un negocio piloto (no crea usuarios automaticamente).
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$DIR/../.." && pwd)"

if [[ -f "$ROOT/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT/.env"
  set +a
fi

APP_URL="${APP_BASE_URL:-http://localhost:8080}"
APP_URL="${APP_URL%/}"

echo "=============================================="
echo "  Alta de negocio — Sistema de Ventas"
echo "=============================================="
echo ""
echo "URL base: $APP_URL"
echo ""

# Comprobar que la app responde
HTTP_CODE=""
if command -v curl >/dev/null 2>&1; then
  HTTP_CODE=$(curl -k -s -o /dev/null -w "%{http_code}" "$APP_URL/login" || true)
  if [[ "$HTTP_CODE" == "200" ]]; then
    echo "[OK] Login accesible ($APP_URL/login)"
  else
    echo "[!!] Login respondio HTTP $HTTP_CODE — revisa que la app este arriba"
  fi
else
  echo "[--] curl no instalado; omite prueba de URL"
fi

echo ""
echo "Checklist (marca en deploy/CHECKLIST_PILOTO.md):"
echo ""
echo "  1. Base de datos lista (schema.sql o migraciones Fase 1)"
echo "  2. App en ejecucion (docker compose o spring-boot:run)"
echo "  3. Registro del ADMIN: $APP_URL/registro"
echo "  4. Login y configuracion: categorias, productos, perfil"
echo "  5. Venta de prueba + (opcional) cliente y factura"
echo "  6. Entregar GUIA: docs/GUIA_OPERATIVA_NEGOCIO.md"
echo ""
read -r -p "Nombre del negocio (opcional): " NEGOCIO_NOMBRE || true
read -r -p "Usuario ADMIN sugerido (opcional): " NEGOCIO_USER || true

if [[ -n "${NEGOCIO_USER:-}" ]]; then
  echo ""
  echo "Siguiente paso: abre en el navegador"
  echo "  $APP_URL/registro"
  echo "  y crea la cuenta con usuario: $NEGOCIO_USER"
fi

if [[ -n "${NEGOCIO_NOMBRE:-}" ]]; then
  echo ""
  echo "Negocio: $NEGOCIO_NOMBRE"
fi

echo ""
echo "Documentacion:"
echo "  - Checklist completo: deploy/CHECKLIST_PILOTO.md"
echo "  - Despliegue:         deploy/DEPLOY.md"
echo "  - Guia del cliente:   docs/GUIA_OPERATIVA_NEGOCIO.md"
echo ""
echo "Migraciones en BD existente:"
echo "  ./deploy/scripts/aplicar-migraciones-fase1.sh"
echo ""
