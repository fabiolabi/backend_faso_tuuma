#!/usr/bin/env bash
# Génère des clients fictifs et des avis approuvés sur les commerces déjà publiés.
#
# Prérequis dans .env :
#   SEED_ENABLED=true
#   SEED_TOKEN=dev-seed-change-me
#   AI_ENABLED=true
#   GEMINI_API_KEY=<votre clé>
#
# Usage :
#   ./scripts/seed-ratings.sh
#   BASE_URL=http://localhost:8087 REVIEWS_PER_METIER=5 USER_COUNT=20 ./scripts/seed-ratings.sh

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8087}"
SEED_TOKEN="${SEED_TOKEN:-dev-seed-change-me}"
TOTAL_REVIEWS="${TOTAL_REVIEWS:-20}"
MAX_PER_METIER="${MAX_PER_METIER:-5}"
USER_COUNT="${USER_COUNT:-15}"
PASSWORD="${PASSWORD:-demo1234}"

echo "→ Seed avis aléatoires sur $BASE_URL (totalReviews=$TOTAL_REVIEWS, maxPerMetier=$MAX_PER_METIER)"

curl -sS -X POST "$BASE_URL/api/dev/seed-ratings" \
  -H "Content-Type: application/json" \
  -H "X-Seed-Token: $SEED_TOKEN" \
  -d "{\"totalReviews\":$TOTAL_REVIEWS,\"maxReviewsPerMetier\":$MAX_PER_METIER,\"userCount\":$USER_COUNT,\"password\":\"$PASSWORD\"}"

echo ""
