#!/usr/bin/env bash
# Certificado autofirmado para probar HTTPS en local o antes de Let's Encrypt.
# Uso: ./deploy/scripts/certificado-local.sh ventas.local
set -euo pipefail

DOMINIO="${1:-ventas.local}"
DIR="$(cd "$(dirname "$0")/.." && pwd)/certs"
mkdir -p "$DIR"

openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout "$DIR/privkey.pem" \
  -out "$DIR/fullchain.pem" \
  -subj "/CN=${DOMINIO}/O=SistemaVentas/C=MX"

echo ""
echo "Certificado generado en deploy/certs/"
echo "  fullchain.pem"
echo "  privkey.pem"
echo ""
echo "Para pruebas locales agrega a /etc/hosts:"
echo "  127.0.0.1  ${DOMINIO}"
echo ""
echo "En .env usa: APP_BASE_URL=https://${DOMINIO}"
