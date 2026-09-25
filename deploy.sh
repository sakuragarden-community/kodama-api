#!/usr/bin/env bash
set -euo pipefail

TAG="${1:?Tag immagine mancante}"
[[ "$TAG" =~ ^[A-Za-z0-9._-]+$ ]] || { echo "Tag non valido" >&2; exit 1; }

cd /var/www/kodama-nest/api
COMPOSE="docker compose -f docker-compose.prod.yml"

sed -i "s/^KODAMA_TAG=.*/KODAMA_TAG=${TAG}/" .env
$COMPOSE pull api
$COMPOSE up -d

for i in {1..30}; do
  if curl -fsS http://127.0.0.1:8080/actuator/health > /dev/null; then
    echo "Kodama API ${TAG} attiva"
    docker image prune -f
    exit 0
  fi
  sleep 2
done

echo "Health check fallito" >&2
$COMPOSE logs --tail=100 api
exit 1
