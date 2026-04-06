#!/bin/bash
# integration-test.sh
# Runs all services with docker-compose and verifies they communicate correctly.

set -e

echo "=== Building and starting all services ==="
docker-compose up -d --build

echo "=== Waiting for services to start ==="
sleep 30

BASE_URL="http://localhost:8080"

check_status() {
    local url=$1
    local expected=$2
    local desc=$3
    local status
    status=$(curl -s -o /dev/null -w "%{http_code}" "$url")
    if [ "$status" = "$expected" ]; then
        echo "PASS: $desc (HTTP $status)"
    else
        echo "FAIL: $desc (expected $expected, got $status)"
        FAILED=1
    fi
}

post_json() {
    local url=$1
    local data=$2
    curl -s -X POST -H "Content-Type: application/json" -d "$data" "$url"
}

FAILED=0

echo ""
echo "=== Testing API Gateway Health ==="
check_status "$BASE_URL/api/health" "200" "Gateway health check"

echo ""
echo "=== Testing Player Registration ==="
REGISTER_RESP=$(post_json "$BASE_URL/api/players/register" '{"username":"testplayer","password":"pass123"}')
echo "Register response: $REGISTER_RESP"
PLAYER_ID=$(echo $REGISTER_RESP | python3 -c "import sys,json; print(int(json.load(sys.stdin).get('id',0)))" 2>/dev/null || echo "0")
echo "Player ID: $PLAYER_ID"

echo ""
echo "=== Testing Player Login ==="
LOGIN_RESP=$(post_json "$BASE_URL/api/players/login" '{"username":"testplayer","password":"pass123"}')
echo "Login response: $LOGIN_RESP"

echo ""
echo "=== Testing Party Creation ==="
PARTY_RESP=$(post_json "$BASE_URL/api/parties" "{\"ownerId\":$PLAYER_ID}")
echo "Party response: $PARTY_RESP"
PARTY_ID=$(echo $PARTY_RESP | python3 -c "import sys,json; print(int(json.load(sys.stdin).get('id',0)))" 2>/dev/null || echo "0")
echo "Party ID: $PARTY_ID"

echo ""
echo "=== Testing Hero Creation ==="
HERO_RESP=$(post_json "$BASE_URL/api/heroes/party/$PARTY_ID" '{"name":"TestHero","heroClass":"WARRIOR"}')
echo "Hero response: $HERO_RESP"

echo ""
echo "=== Testing Campaign Start ==="
CAMPAIGN_RESP=$(post_json "$BASE_URL/api/campaigns" "{\"playerId\":$PLAYER_ID,\"partyId\":$PARTY_ID}")
echo "Campaign response: $CAMPAIGN_RESP"

echo ""
echo "=== Testing Leaderboard ==="
check_status "$BASE_URL/api/players/leaderboard" "200" "Leaderboard endpoint"

echo ""
echo "=== Testing League Standings ==="
check_status "$BASE_URL/api/players/league" "200" "League standings endpoint"

echo ""
echo "=== Stopping services ==="
docker-compose down

if [ "$FAILED" = "1" ]; then
    echo ""
    echo "SOME TESTS FAILED"
    exit 1
else
    echo ""
    echo "ALL INTEGRATION TESTS PASSED"
    exit 0
fi
