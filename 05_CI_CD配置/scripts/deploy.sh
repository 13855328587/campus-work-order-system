#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEPLOY_BRANCH="${DEPLOY_BRANCH:-master}"

cd "$PROJECT_DIR"

if [[ ! -f .env ]]; then
  echo "Deployment aborted: $PROJECT_DIR/.env does not exist."
  echo "Copy .env.example to .env and configure production secrets first."
  exit 1
fi

git fetch origin "$DEPLOY_BRANCH"
git checkout "$DEPLOY_BRANCH"
git pull --ff-only origin "$DEPLOY_BRANCH"

docker compose config --quiet
docker compose up -d --build --remove-orphans
docker compose ps

echo "Deployment completed successfully."

