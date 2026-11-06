#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SQL="$ROOT/scripts/seed-insights-demo.sql"

if [[ ! -f "$SQL" ]]; then
  echo "Arquivo não encontrado: $SQL" >&2
  exit 1
fi

echo "Inserindo dados dummy de insights..."

if docker compose -f "$ROOT/compose.yaml" exec -T postgres psql -U myuser -d mydatabase < "$SQL"; then
  echo "Seed de insights concluído (observation = INSIGHTS_DEMO)."
  exit 0
fi

if command -v psql >/dev/null 2>&1; then
  PGPASSWORD="${DB_PASSWORD:-secret}" psql \
    -h "${DB_HOST:-localhost}" \
    -p "${DB_PORT:-5432}" \
    -U "${DB_USERNAME:-myuser}" \
    -d "${DB_NAME:-mydatabase}" \
    -f "$SQL"
  echo "Seed de insights concluído (observation = INSIGHTS_DEMO)."
  exit 0
fi

echo "Não foi possível conectar no Postgres (docker compose exec ou psql)." >&2
exit 1
