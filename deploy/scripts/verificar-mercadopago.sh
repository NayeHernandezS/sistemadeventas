#!/usr/bin/env bash
# Verifica variables minimas de Mercado Pago antes de activar cobro en linea.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
ENV_FILE="${ROOT}/.env"

ok() { echo "  [OK] $1"; }
warn() { echo "  [AVISO] $1"; }
fail() { echo "  [FALTA] $1"; ERR=1; }

ERR=0
echo "=== Verificacion Mercado Pago ==="
echo "Proyecto: ${ROOT}"

if [[ ! -f "${ENV_FILE}" ]]; then
  fail "No existe .env (copia desde .env.example)"
  exit 1
fi

# shellcheck disable=SC1090
source "${ENV_FILE}" 2>/dev/null || true

APP_BASE_URL="${APP_BASE_URL:-}"
MP_TOKEN="${MERCADOPAGO_ACCESS_TOKEN:-}"
MP_SECRET="${MERCADOPAGO_WEBHOOK_SECRET:-}"

if [[ -z "${APP_BASE_URL}" ]]; then
  fail "APP_BASE_URL vacia"
elif [[ "${APP_BASE_URL}" == *"tu-dominio"* ]] || [[ "${APP_BASE_URL}" == *"tudominio"* ]]; then
  fail "APP_BASE_URL sigue siendo placeholder"
elif [[ "${APP_BASE_URL}" != https://* ]]; then
  warn "APP_BASE_URL no usa HTTPS"
else
  ok "APP_BASE_URL=${APP_BASE_URL}"
  WEBHOOK="${APP_BASE_URL%/}/api/mercadopago/notificaciones"
  echo "       Webhook MP: ${WEBHOOK}"
fi

if [[ -z "${MP_TOKEN}" ]]; then
  warn "MERCADOPAGO_ACCESS_TOKEN vacio — solo pago manual"
elif [[ "${MP_TOKEN}" == *"tu-token"* ]] || [[ "${#MP_TOKEN}" -lt 30 ]]; then
  fail "MERCADOPAGO_ACCESS_TOKEN invalido o placeholder"
elif [[ "${MP_TOKEN}" == TEST-* ]]; then
  warn "Token de PRUEBA (TEST-). Usa APP_USR- en produccion."
  ok "Token de prueba detectado"
elif [[ "${MP_TOKEN}" == APP_USR-* ]]; then
  ok "Token de produccion (APP_USR-)"
else
  warn "Formato de token no reconocido; revisa el panel de desarrolladores"
fi

if [[ -n "${MP_TOKEN}" && "${MP_TOKEN}" != *"tu-token"* ]]; then
  if [[ -z "${MP_SECRET}" ]]; then
    warn "MERCADOPAGO_WEBHOOK_SECRET vacio — obligatorio en produccion"
  else
    ok "MERCADOPAGO_WEBHOOK_SECRET configurado"
  fi
fi

if [[ -n "${APP_BASE_URL}" && "${APP_BASE_URL}" == https://* ]]; then
  WEBHOOK="${APP_BASE_URL%/}/api/mercadopago/notificaciones"
  if command -v curl >/dev/null 2>&1; then
    CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "${WEBHOOK}" || echo "000")
    if [[ "${CODE}" == "401" ]]; then
      ok "Webhook responde 401 sin firma (esperado)"
    elif [[ "${CODE}" == "000" ]]; then
      warn "No se pudo contactar ${WEBHOOK} (app apagada o DNS)"
    else
      warn "Webhook respondio HTTP ${CODE} (esperado 401 sin firma)"
    fi
  else
    warn "curl no instalado — omitiendo prueba HTTP"
  fi
fi

echo ""
if [[ "${ERR}" -eq 0 ]]; then
  echo "Listo para revisar checklist: deploy/CHECKLIST_MERCADOPAGO.md"
else
  echo "Corrige los puntos [FALTA] antes de cobrar con Mercado Pago en produccion."
  exit 1
fi
