#!/usr/bin/env bash
# End-to-end smoke test against a running stack (default: the local compose stack).
# Logs in through Caddy's /api proxy and uses the token on a protected endpoint, so it exercises
# the packaged jar's real runtime classpath, the Dockerfile, the seed data and the proxy together.
#
#   docker compose up -d --build --wait && ./deploy/e2e-smoke.sh
#
# Uses the seed account (admin / 123456): only for throwaway local/CI databases, never production.
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8081}"
API="$BASE_URL/api"

fail() { echo "FAIL: $*" >&2; exit 1; }

# 1. The ERP portal is served
code=$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/")
[ "$code" = 200 ] || fail "portal returned HTTP $code"
echo "ok   portal is served"

# 2. Login issues a token (business result: code 1 = success)
login=$(curl -s -X POST -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}' "$API/employee/info/login")
token=$(echo "$login" | jq -r '.data.token // empty' 2>/dev/null || true)
[ -n "$token" ] || fail "login did not return a token: $login"
echo "ok   login returns a token"

# 3. The token is accepted on a protected endpoint
code=$(curl -s -o /dev/null -w '%{http_code}' -H "token: $token" \
  "$API/employee/department/page?page=1&pageSize=5")
[ "$code" = 200 ] || fail "protected endpoint with token returned HTTP $code"
echo "ok   token accepted on a protected endpoint"

# 4. A forged token is rejected
code=$(curl -s -o /dev/null -w '%{http_code}' -H "token: forged.token.value" \
  "$API/employee/department/page?page=1&pageSize=5")
[ "$code" = 401 ] || fail "forged token returned HTTP $code, expected 401"
echo "ok   forged token rejected"

echo "E2E smoke test passed"
