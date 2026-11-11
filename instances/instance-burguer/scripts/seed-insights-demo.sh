#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
SQL="$SCRIPT_DIR/seed-insights-demo.sql"

if [[ ! -f "$SQL" ]]; then
  echo "Arquivo não encontrado: $SQL" >&2
  exit 1
fi

echo "Inserindo dados dummy de insights no banco ${DB_NAME:-orderly_burger}..."

if docker compose -f "$REPO_ROOT/compose.yaml" exec -T postgres psql -U "${DB_USERNAME:-myuser}" -d "${DB_NAME:-orderly_burger}" < "$SQL"; then
  echo "Seed de insights concluído (observation = INSIGHTS_DEMO)."
  exit 0
fi

if command -v psql >/dev/null 2>&1; then
  PGPASSWORD="${DB_PASSWORD:-secret}" psql \
    -h "${DB_HOST:-localhost}" \
    -p "${DB_PORT:-5432}" \
    -U "${DB_USERNAME:-myuser}" \
    -d "${DB_NAME:-orderly_burger}" \
    -f "$SQL"
  echo "Seed de insights concluído (observation = INSIGHTS_DEMO)."
  exit 0
fi

echo "Não foi possível conectar no Postgres (docker compose exec ou psql)." >&2
exit 1
