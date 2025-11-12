#!/bin/bash

# AUTH-001: Testes de Autenticação do CrimeTracker
# Execute: bash backend/scripts/auth_tests.sh

BASE_URL="http://localhost:3000"
COLOR_GREEN='\033[0;32m'
COLOR_RED='\033[0;31m'
COLOR_YELLOW='\033[1;33m'
COLOR_BLUE='\033[0;34m'
COLOR_CYAN='\033[0;36m'
COLOR_RESET='\033[0m'

echo -e "${COLOR_CYAN}╔════════════════════════════════════════════════╗${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║     🧪 TESTES AUTH-001 - CrimeTracker         ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}╚════════════════════════════════════════════════╝${COLOR_RESET}"
echo ""
echo -e "${COLOR_YELLOW}📋 Servidor deve estar rodando em http://localhost:3000${COLOR_RESET}"
echo ""

# Função para testar resposta
test_response() {
    local test_name=$1
    local response=$2
    local expected_status=$3
    
    echo -e "${COLOR_BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
    echo -e "${COLOR_CYAN}🧪 $test_name${COLOR_RESET}"
    echo -e "${COLOR_BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
    echo ""
    echo -e "${COLOR_BLUE}📤 Resposta:${COLOR_RESET}"
    echo "$response" | jq '.' 2>/dev/null || echo "$response"
    echo ""
}

# Gerar timestamp único para testes
TIMESTAMP=$(date +%s)
TEST_EMAIL="test_${TIMESTAMP}@example.com"
TEST_USERNAME="user_${TIMESTAMP}"
TEST_PASSWORD="senha12345678"

# ============================================
# TESTE 1: Health Check
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 1: Health Check${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

HEALTH_RESPONSE=$(curl -s "$BASE_URL/health")
echo -e "${COLOR_BLUE}📥 Resposta:${COLOR_RESET}"
echo "$HEALTH_RESPONSE" | jq '.'

if echo "$HEALTH_RESPONSE" | grep -q "success"; then
    echo -e "${COLOR_GREEN}✅ Servidor está rodando${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Servidor não está respondendo${COLOR_RESET}"
    echo -e "${COLOR_RED}Execute: cd backend && npm run dev${COLOR_RESET}"
    exit 1
fi
echo ""
sleep 1

# ============================================
# TESTE 2: POST /api/auth/register
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 2: POST /api/auth/register${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

echo -e "${COLOR_BLUE}📤 Enviando:${COLOR_RESET}"
echo "{
  \"email\": \"$TEST_EMAIL\",
  \"password\": \"$TEST_PASSWORD\",
  \"username\": \"$TEST_USERNAME\"
}"
echo ""

START_TIME=$(date +%s%3N)
REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"$TEST_PASSWORD\",
    \"username\": \"$TEST_USERNAME\"
  }")
END_TIME=$(date +%s%3N)
DURATION=$((END_TIME - START_TIME))

HTTP_CODE=$(echo "$REGISTER_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$REGISTER_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta (${DURATION}ms):${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

# Extrair token
TOKEN=$(echo "$RESPONSE_BODY" | jq -r '.token')

# Validações
echo -e "${COLOR_YELLOW}🔍 Validações:${COLOR_RESET}"

if [ "$HTTP_CODE" = "201" ]; then
    echo -e "  ${COLOR_GREEN}✅ Status Code: 201${COLOR_RESET}"
else
    echo -e "  ${COLOR_RED}❌ Status Code: $HTTP_CODE (esperado: 201)${COLOR_RESET}"
fi

if echo "$RESPONSE_BODY" | jq -e '.success' > /dev/null 2>&1; then
    SUCCESS=$(echo "$RESPONSE_BODY" | jq -r '.success')
    if [ "$SUCCESS" = "true" ]; then
        echo -e "  ${COLOR_GREEN}✅ success: true${COLOR_RESET}"
    else
        echo -e "  ${COLOR_RED}❌ success: $SUCCESS${COLOR_RESET}"
    fi
else
    echo -e "  ${COLOR_RED}❌ Campo 'success' não encontrado${COLOR_RESET}"
fi

if echo "$RESPONSE_BODY" | jq -e '.user_id' > /dev/null 2>&1; then
    echo -e "  ${COLOR_GREEN}✅ user_id presente${COLOR_RESET}"
else
    echo -e "  ${COLOR_RED}❌ Campo 'user_id' ausente${COLOR_RESET}"
fi

if echo "$RESPONSE_BODY" | jq -e '.username' > /dev/null 2>&1; then
    echo -e "  ${COLOR_GREEN}✅ username presente${COLOR_RESET}"
else
    echo -e "  ${COLOR_RED}❌ Campo 'username' ausente${COLOR_RESET}"
fi

if echo "$RESPONSE_BODY" | jq -e '.email' > /dev/null 2>&1; then
    echo -e "  ${COLOR_GREEN}✅ email presente${COLOR_RESET}"
else
    echo -e "  ${COLOR_RED}❌ Campo 'email' ausente${COLOR_RESET}"
fi

if echo "$RESPONSE_BODY" | jq -e '.token' > /dev/null 2>&1; then
    echo -e "  ${COLOR_GREEN}✅ token presente${COLOR_RESET}"
else
    echo -e "  ${COLOR_RED}❌ Campo 'token' ausente${COLOR_RESET}"
fi

if [ $DURATION -lt 2000 ]; then
    echo -e "  ${COLOR_GREEN}✅ Tempo: ${DURATION}ms (< 2s)${COLOR_RESET}"
else
    echo -e "  ${COLOR_RED}❌ Tempo: ${DURATION}ms (> 2s)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 3: POST /api/auth/login
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 3: POST /api/auth/login${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

echo -e "${COLOR_BLUE}📤 Enviando:${COLOR_RESET}"
echo "{
  \"email\": \"$TEST_EMAIL\",
  \"password\": \"$TEST_PASSWORD\"
}"
echo ""

START_TIME=$(date +%s%3N)
LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"$TEST_PASSWORD\"
  }")
END_TIME=$(date +%s%3N)
DURATION=$((END_TIME - START_TIME))

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$LOGIN_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta (${DURATION}ms):${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

# Extrair token
TOKEN=$(echo "$RESPONSE_BODY" | jq -r '.token')

# Validações
echo -e "${COLOR_YELLOW}🔍 Validações:${COLOR_RESET}"

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "  ${COLOR_GREEN}✅ Status Code: 200${COLOR_RESET}"
else
    echo -e "  ${COLOR_RED}❌ Status Code: $HTTP_CODE (esperado: 200)${COLOR_RESET}"
fi

if echo "$RESPONSE_BODY" | jq -e '.success' > /dev/null 2>&1; then
    SUCCESS=$(echo "$RESPONSE_BODY" | jq -r '.success')
    if [ "$SUCCESS" = "true" ]; then
        echo -e "  ${COLOR_GREEN}✅ success: true${COLOR_RESET}"
    else
        echo -e "  ${COLOR_RED}❌ success: $SUCCESS${COLOR_RESET}"
    fi
fi

if echo "$RESPONSE_BODY" | jq -e '.user_id' > /dev/null 2>&1; then
    echo -e "  ${COLOR_GREEN}✅ user_id presente${COLOR_RESET}"
fi

if echo "$RESPONSE_BODY" | jq -e '.username' > /dev/null 2>&1; then
    echo -e "  ${COLOR_GREEN}✅ username presente${COLOR_RESET}"
fi

if echo "$RESPONSE_BODY" | jq -e '.email' > /dev/null 2>&1; then
    echo -e "  ${COLOR_GREEN}✅ email presente${COLOR_RESET}"
fi

if echo "$RESPONSE_BODY" | jq -e '.token' > /dev/null 2>&1; then
    echo -e "  ${COLOR_GREEN}✅ token presente${COLOR_RESET}"
fi

if [ $DURATION -lt 2000 ]; then
    echo -e "  ${COLOR_GREEN}✅ Tempo: ${DURATION}ms (< 2s)${COLOR_RESET}"
else
    echo -e "  ${COLOR_RED}❌ Tempo: ${DURATION}ms (> 2s)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 4: Validação de Token (middleware)
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 4: Middleware - Token Válido${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

echo -e "${COLOR_BLUE}📤 Usando token: ${TOKEN:0:50}...${COLOR_RESET}"
echo ""

AUTH_RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/auth/profile" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$AUTH_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$AUTH_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta:${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${COLOR_GREEN}✅ Token válido aceito${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Token válido rejeitado (Status: $HTTP_CODE)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 5: Sem Token (401)
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 5: Middleware - Sem Token (401)${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

NO_TOKEN_RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/auth/profile")

HTTP_CODE=$(echo "$NO_TOKEN_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$NO_TOKEN_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta:${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

if [ "$HTTP_CODE" = "401" ]; then
    echo -e "${COLOR_GREEN}✅ Rejeitado corretamente com 401${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Status incorreto: $HTTP_CODE (esperado: 401)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 6: Token Inválido (403)
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 6: Middleware - Token Inválido (403)${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

INVALID_RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/api/auth/profile" \
  -H "Authorization: Bearer token_invalido_123")

HTTP_CODE=$(echo "$INVALID_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$INVALID_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta:${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

if [ "$HTTP_CODE" = "403" ]; then
    echo -e "${COLOR_GREEN}✅ Rejeitado corretamente com 403${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Status incorreto: $HTTP_CODE (esperado: 403)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 7: Email Duplicado (409)
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 7: Email Duplicado (409)${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

DUPLICATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"$TEST_PASSWORD\",
    \"username\": \"outro_username\"
  }")

HTTP_CODE=$(echo "$DUPLICATE_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$DUPLICATE_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta:${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

if [ "$HTTP_CODE" = "409" ]; then
    echo -e "${COLOR_GREEN}✅ Email duplicado rejeitado com 409${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Status incorreto: $HTTP_CODE (esperado: 409)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 8: Senha Fraca (400)
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 8: Senha Fraca < 8 chars (400)${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

WEAK_PASSWORD_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"weak_${TIMESTAMP}@example.com\",
    \"password\": \"1234567\",
    \"username\": \"weak_${TIMESTAMP}\"
  }")

HTTP_CODE=$(echo "$WEAK_PASSWORD_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$WEAK_PASSWORD_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta:${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${COLOR_GREEN}✅ Senha fraca rejeitada com 400${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Status incorreto: $HTTP_CODE (esperado: 400)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 9: Email Inválido (400)
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 9: Email Inválido (400)${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

INVALID_EMAIL_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"email_invalido\",
    \"password\": \"senha12345678\",
    \"username\": \"invalid_${TIMESTAMP}\"
  }")

HTTP_CODE=$(echo "$INVALID_EMAIL_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$INVALID_EMAIL_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta:${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${COLOR_GREEN}✅ Email inválido rejeitado com 400${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Status incorreto: $HTTP_CODE (esperado: 400)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 10: Senha Incorreta (401)
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 10: Senha Incorreta (401)${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

WRONG_PASSWORD_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"senhaErrada123\"
  }")

HTTP_CODE=$(echo "$WRONG_PASSWORD_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$WRONG_PASSWORD_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta:${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

if [ "$HTTP_CODE" = "401" ]; then
    echo -e "${COLOR_GREEN}✅ Senha incorreta rejeitada com 401${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Status incorreto: $HTTP_CODE (esperado: 401)${COLOR_RESET}"
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
echo -e "${COLOR_GREEN}✅ Todos os testes do AUTH-001 foram executados!${COLOR_RESET}"
echo ""
echo -e "${COLOR_YELLOW}📝 Verificações realizadas:${COLOR_RESET}"
echo "  1. ✅ Health check do servidor"
echo "  2. ✅ POST /api/auth/register (201)"
echo "  3. ✅ POST /api/auth/login (200)"
echo "  4. ✅ Middleware com token válido (200)"
echo "  5. ✅ Middleware sem token (401)"
echo "  6. ✅ Middleware token inválido (403)"
echo "  7. ✅ Email duplicado (409)"
echo "  8. ✅ Senha fraca (400)"
echo "  9. ✅ Email inválido (400)"
echo "  10. ✅ Senha incorreta (401)"
echo ""
echo -e "${COLOR_CYAN}✨ Testes concluídos!${COLOR_RESET}"
echo ""

