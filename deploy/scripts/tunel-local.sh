#!/usr/bin/env bash
# Tunel HTTPS publico hacia localhost:8080 para Mercado Pago (renovacion automatica y webhooks).
# Uso: ./deploy/scripts/tunel-local.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
ENV_FILE="${ROOT}/.env"
PORT="${APP_PORT:-8080}"
BIN="${ROOT}/deploy/bin/cloudflared"
LOG="/tmp/fusion-cloudflared.log"
PID_FILE="/tmp/fusion-cloudflared.pid"

descargar_cloudflared() {
  mkdir -p "${ROOT}/deploy/bin"
  ARCH="$(uname -m)"
  case "${ARCH}" in
    arm64) PKG="cloudflared-darwin-arm64.tgz" ;;
    x86_64) PKG="cloudflared-darwin-amd64.tgz" ;;
    *) echo "Arquitectura no soportada: ${ARCH}"; exit 1 ;;
  esac
  echo "Descargando cloudflared (${PKG})..."
  curl -fsSL "https://github.com/cloudflare/cloudflared/releases/latest/download/${PKG}" -o /tmp/cloudflared.tgz
  tar -xzf /tmp/cloudflared.tgz -C "${ROOT}/deploy/bin"
  chmod +x "${BIN}"
}

if [[ ! -x "${BIN}" ]]; then
  descargar_cloudflared
fi

if [[ -f "${PID_FILE}" ]] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  echo "Tunel ya en ejecucion (PID $(cat "${PID_FILE}"))."
else
  : > "${LOG}"
  nohup "${BIN}" tunnel --url "http://127.0.0.1:${PORT}" >> "${LOG}" 2>&1 &
  echo $! > "${PID_FILE}"
  echo "Iniciando tunel (PID $(cat "${PID_FILE}"))..."
  sleep 6
fi

URL=""
for _ in 1 2 3 4 5; do
  URL="$(grep -oE 'https://[a-z0-9-]+\.trycloudflare\.com' "${LOG}" | tail -1 || true)"
  [[ -n "${URL}" ]] && break
  sleep 2
done

if [[ -z "${URL}" ]]; then
  echo "No se pudo obtener la URL del tunel. Revisa ${LOG}"
  exit 1
fi

echo ""
echo "URL publica HTTPS: ${URL}"
echo "Webhook MP:        ${URL}/api/mercadopago/notificaciones"
echo ""

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "No existe .env. Copia .env.example y vuelve a ejecutar este script."
  exit 1
fi

if grep -q '^APP_BASE_URL=' "${ENV_FILE}"; then
  if [[ "$(uname)" == "Darwin" ]]; then
    sed -i '' "s|^APP_BASE_URL=.*|APP_BASE_URL=${URL}|" "${ENV_FILE}"
  else
    sed -i "s|^APP_BASE_URL=.*|APP_BASE_URL=${URL}|" "${ENV_FILE}"
  fi
else
  echo "APP_BASE_URL=${URL}" >> "${ENV_FILE}"
fi

echo "APP_BASE_URL actualizada en .env"
echo ""
echo "Siguiente:"
echo "  1. Reinicia la aplicacion (Spring Boot)"
echo "  2. Registra el webhook en Mercado Pago Developers"
echo "  3. Prueba renovacion automatica en /suscripcion"
echo ""
echo "Para detener el tunel: kill \$(cat ${PID_FILE})"
