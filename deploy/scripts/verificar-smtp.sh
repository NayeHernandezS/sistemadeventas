#!/usr/bin/env bash
# Verifica configuracion SMTP minima para correos transaccionales.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
ENV_FILE="${ROOT}/.env"

ok() { echo "  [OK] $1"; }
warn() { echo "  [AVISO] $1"; }
fail() { echo "  [FALTA] $1"; ERR=1; }

ERR=0
echo "=== Verificacion SMTP ==="

if [[ ! -f "${ENV_FILE}" ]]; then
  fail "No existe .env"
  exit 1
fi

# shellcheck disable=SC1090
source "${ENV_FILE}" 2>/dev/null || true

SMTP_HOST="${SMTP_HOST:-}"
SMTP_USER="${SMTP_USER:-}"
SMTP_PASSWORD="${SMTP_PASSWORD:-}"
MAIL_FROM="${MAIL_FROM:-}"
APP_BASE_URL="${APP_BASE_URL:-}"

if [[ -z "${SMTP_HOST}" ]]; then
  warn "SMTP_HOST vacio — correos en modo demo (solo avisos en app)"
else
  ok "SMTP_HOST=${SMTP_HOST}"
fi

if [[ -n "${SMTP_HOST}" && -z "${SMTP_USER}" ]]; then
  warn "SMTP_USER vacio (algunos servidores lo permiten)"
fi

if [[ -n "${SMTP_HOST}" && -z "${SMTP_PASSWORD}" ]]; then
  warn "SMTP_PASSWORD vacio"
elif [[ -n "${SMTP_PASSWORD}" ]]; then
  ok "SMTP_PASSWORD configurado"
fi

if [[ -z "${MAIL_FROM}" ]]; then
  warn "MAIL_FROM vacio — se usara valor por defecto de application.properties"
else
  ok "MAIL_FROM=${MAIL_FROM}"
fi

if [[ -z "${APP_BASE_URL}" ]]; then
  warn "APP_BASE_URL vacia — enlaces en correos de suscripcion seran incompletos"
elif [[ "${APP_BASE_URL}" != https://* ]]; then
  warn "APP_BASE_URL sin HTTPS"
else
  ok "APP_BASE_URL=${APP_BASE_URL}"
fi

echo ""
if [[ -z "${SMTP_HOST}" ]]; then
  echo "Configura SMTP para activar avisos por correo. Checklist: deploy/CHECKLIST_CORREOS.md"
  exit 0
fi

if [[ "${ERR}" -eq 0 ]]; then
  echo "SMTP listo para prueba. Ejecuta migracion 19 y prueba desde /plataforma."
else
  exit 1
fi
