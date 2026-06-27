#!/usr/bin/env bash
# Migration one-shot des images locales vers Cloudflare R2 (prod tuuma.openbangre.com)
set -euo pipefail

cd "$(dirname "$0")/.."

if [[ ! -f .env ]]; then
  echo "Erreur : fichier .env absent. Copiez env.production.example vers .env et renseignez R2_*."
  exit 1
fi

# shellcheck disable=SC1091
source .env

if [[ "${MEDIA_BACKEND:-}" != "r2" ]]; then
  echo "Erreur : MEDIA_BACKEND doit être 'r2' dans .env"
  exit 1
fi

for var in R2_BUCKET R2_ENDPOINT R2_ACCESS_KEY_ID R2_SECRET_ACCESS_KEY; do
  if [[ -z "${!var:-}" ]]; then
    echo "Erreur : $var non défini dans .env"
    exit 1
  fi
done

echo "==> Rebuild + redémarrage avec migration local→R2..."
export MEDIA_MIGRATE_LOCAL_TO_R2=true
docker compose up -d --build app

echo "==> Attente logs migration..."
sleep 8
docker compose logs app --tail=40 | grep -E "R2|Migration|media" || true

echo ""
echo "Migration lancée. Vérifiez les logs : docker compose logs app | grep Migration"
echo "Puis dans .env : MEDIA_MIGRATE_LOCAL_TO_R2=false  et  docker compose up -d app"
