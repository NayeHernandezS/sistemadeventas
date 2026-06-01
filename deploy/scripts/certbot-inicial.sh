#!/usr/bin/env bash
# Obtiene certificado Let's Encrypt con certbot (servidor con dominio publico).
# Requisitos: DNS apuntando al servidor, puertos 80/443 abiertos, docker compose en marcha.
#
# Uso:
#   export DOMAIN=ventas.tudominio.com
#   export CERTBOT_EMAIL=admin@tudominio.com
#   ./deploy/scripts/certbot-inicial.sh
set -euo pipefail

DOMAIN="${DOMAIN:?Define DOMAIN (ej. ventas.tudominio.com)}"
EMAIL="${CERTBOT_EMAIL:?Define CERTBOT_EMAIL}"
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
CERT_DIR="$ROOT/deploy/certs"

mkdir -p "$CERT_DIR"

# Certbot standalone temporal (detener nginx si usa el puerto 80)
docker run --rm -it \
  -v "$CERT_DIR:/etc/letsencrypt" \
  -v "$ROOT/deploy/certbot-www:/var/www/certbot" \
  -p 80:80 \
  certbot/certbot certonly --standalone \
  -d "$DOMAIN" \
  --email "$EMAIL" \
  --agree-tos --no-eff-email

# Certbot guarda en live/DOMAIN/ — enlazar a deploy/certs
LIVE="$CERT_DIR/live/$DOMAIN"
if [[ -f "$LIVE/fullchain.pem" ]]; then
  cp "$LIVE/fullchain.pem" "$CERT_DIR/fullchain.pem"
  cp "$LIVE/privkey.pem" "$CERT_DIR/privkey.pem"
  echo "Certificados copiados a deploy/certs/"
  echo "Actualiza .env: APP_BASE_URL=https://${DOMAIN}"
  echo "Reinicia: docker compose restart nginx app"
else
  echo "No se encontraron certificados en $LIVE" >&2
  exit 1
fi
