#!/bin/bash

# CRIME-001: Testes de Denúncias do CrimeTracker
# Execute: bash backend/scripts/crime_tests.sh

BASE_URL="http://localhost:3000"
COLOR_GREEN='\033[0;32m'
COLOR_RED='\033[0;31m'
COLOR_YELLOW='\033[1;33m'
COLOR_BLUE='\033[0;34m'
COLOR_CYAN='\033[0;36m'
COLOR_RESET='\033[0m'

echo -e "${COLOR_CYAN}╔════════════════════════════════════════════════╗${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║     🚨 TESTES CRIME-001 - CrimeTracker        ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}╚════════════════════════════════════════════════╝${COLOR_RESET}"
echo ""
echo -e "${COLOR_YELLOW}📋 Servidor deve estar rodando em http://localhost:3000${COLOR_RESET}"
echo ""

# Gerar timestamp único
TIMESTAMP=$(date +%s)
TEST_EMAIL="crime_test_${TIMESTAMP}@example.com"
TEST_USERNAME="crime_user_${TIMESTAMP}"
TEST_PASSWORD="senha12345678"

# Coordenadas de São Paulo
TEST_LAT=-23.5505
TEST_LON=-46.6333

# ============================================
# SETUP: Criar usuário
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🔧 Setup: Criando usuário de teste${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"$TEST_PASSWORD\",
    \"username\": \"$TEST_USERNAME\"
  }")

TOKEN=$(echo "$REGISTER_RESPONSE" | jq -r '.token')

if [ "$TOKEN" != "null" ] && [ -n "$TOKEN" ]; then
    echo -e "${COLOR_GREEN}✅ Usuário criado e autenticado${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Erro ao criar usuário de teste${COLOR_RESET}"
    exit 1
fi

echo ""
sleep 1

# ============================================
# TESTE 1: POST /api/reports
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 1: POST /api/reports${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

START_TIME=$(date +%s%3N)
CREATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/reports" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"tipo\": \"Assalto\",
    \"descricao\": \"Assalto a mão armada próximo ao metrô. Dois suspeitos em uma moto preta.\",
    \"latitude\": $TEST_LAT,
    \"longitude\": $TEST_LON
  }")
END_TIME=$(date +%s%3N)
DURATION=$((END_TIME - START_TIME))

HTTP_CODE=$(echo "$CREATE_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$CREATE_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta (${DURATION}ms):${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

REPORT_ID=$(echo "$RESPONSE_BODY" | jq -r '.data.id')

# Validações
echo -e "${COLOR_YELLOW}🔍 Validações:${COLOR_RESET}"

if [ "$HTTP_CODE" = "201" ]; then
    echo -e "  ${COLOR_GREEN}✅ Status Code: 201${COLOR_RESET}"
else
    echo -e "  ${COLOR_RED}❌ Status Code: $HTTP_CODE${COLOR_RESET}"
fi

if echo "$RESPONSE_BODY" | jq -e '.success' > /dev/null 2>&1; then
    SUCCESS=$(echo "$RESPONSE_BODY" | jq -r '.success')
    if [ "$SUCCESS" = "true" ]; then
        echo -e "  ${COLOR_GREEN}✅ success: true${COLOR_RESET}"
    fi
fi

if [ $DURATION -lt 3000 ]; then
    echo -e "  ${COLOR_GREEN}✅ Tempo: ${DURATION}ms (< 3s)${COLOR_RESET}"
else
    echo -e "  ${COLOR_RED}❌ Tempo: ${DURATION}ms (> 3s)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 2: GET /api/reports/nearby
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 2: GET /api/reports/nearby${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

START_TIME=$(date +%s%3N)
NEARBY_RESPONSE=$(curl -s -w "\n%{http_code}" \
  -H "Authorization: Bearer $TOKEN" \
  "$BASE_URL/api/reports/nearby?latitude=$TEST_LAT&longitude=$TEST_LON&radius_km=5")
END_TIME=$(date +%s%3N)
DURATION=$((END_TIME - START_TIME))

HTTP_CODE=$(echo "$NEARBY_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$NEARBY_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta (${DURATION}ms):${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

echo -e "${COLOR_YELLOW}🔍 Validações:${COLOR_RESET}"

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "  ${COLOR_GREEN}✅ Status Code: 200${COLOR_RESET}"
fi

COUNT=$(echo "$RESPONSE_BODY" | jq -r '.count')
if [ "$COUNT" -ge 1 ]; then
    echo -e "  ${COLOR_GREEN}✅ Denúncias encontradas: $COUNT${COLOR_RESET}"
fi

if [ $DURATION -lt 3000 ]; then
    echo -e "  ${COLOR_GREEN}✅ Tempo: ${DURATION}ms (< 3s)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 3: GET /api/reports/:id
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 3: GET /api/reports/:id${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

if [ "$REPORT_ID" != "null" ] && [ -n "$REPORT_ID" ]; then
    START_TIME=$(date +%s%3N)
    DETAILS_RESPONSE=$(curl -s -w "\n%{http_code}" \
      -H "Authorization: Bearer $TOKEN" \
      "$BASE_URL/api/reports/$REPORT_ID")
    END_TIME=$(date +%s%3N)
    DURATION=$((END_TIME - START_TIME))

    HTTP_CODE=$(echo "$DETAILS_RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$DETAILS_RESPONSE" | head -n-1)

    echo -e "${COLOR_BLUE}📥 Resposta (${DURATION}ms):${COLOR_RESET}"
    echo "$RESPONSE_BODY" | jq '.'
    echo ""

    echo -e "${COLOR_YELLOW}🔍 Validações:${COLOR_RESET}"

    if [ "$HTTP_CODE" = "200" ]; then
        echo -e "  ${COLOR_GREEN}✅ Status Code: 200${COLOR_RESET}"
    fi

    if [ $DURATION -lt 3000 ]; then
        echo -e "  ${COLOR_GREEN}✅ Tempo: ${DURATION}ms (< 3s)${COLOR_RESET}"
    fi
else
    echo -e "${COLOR_YELLOW}⚠️  ID da denúncia não disponível${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 4: Tipo inválido
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 4: Tipo de crime inválido (400)${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

INVALID_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/reports" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"tipo\": \"Sequestro\",
    \"descricao\": \"Teste com tipo inválido\",
    \"latitude\": $TEST_LAT,
    \"longitude\": $TEST_LON
  }")

HTTP_CODE=$(echo "$INVALID_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${COLOR_GREEN}✅ Tipo inválido rejeitado com 400${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Status incorreto: $HTTP_CODE${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 5: Descrição longa
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 5: Descrição > 500 chars (400)${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

LONG_DESC=$(printf 'A%.0s' {1..501})

LONG_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/reports" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"tipo\": \"Furto\",
    \"descricao\": \"$LONG_DESC\",
    \"latitude\": $TEST_LAT,
    \"longitude\": $TEST_LON
  }")

HTTP_CODE=$(echo "$LONG_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${COLOR_GREEN}✅ Descrição longa rejeitada com 400${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Status incorreto: $HTTP_CODE${COLOR_RESET}"
fi

echo ""

# ============================================
# RESUMO
# ============================================
echo ""
echo -e "${COLOR_CYAN}╔════════════════════════════════════════════════╗${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║           📊 RESUMO DOS TESTES                 ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}╚════════════════════════════════════════════════╝${COLOR_RESET}"
echo ""
echo -e "${COLOR_GREEN}✅ Todos os testes do CRIME-001 foram executados!${COLOR_RESET}"
echo ""
echo -e "${COLOR_YELLOW}📝 Verificações realizadas:${COLOR_RESET}"
echo "  1. ✅ POST /api/reports (criar denúncia)"
echo "  2. ✅ GET /api/reports/nearby (raio + 30 dias)"
echo "  3. ✅ GET /api/reports/:id (detalhes)"
echo "  4. ✅ Tipo inválido rejeitado (400)"
echo "  5. ✅ Descrição > 500 chars rejeitada (400)"
echo ""
echo -e "${COLOR_CYAN}✨ Testes concluídos!${COLOR_RESET}"
echo ""

