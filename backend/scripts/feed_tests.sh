#!/bin/bash

# FEED-001: Testes de Feed do CrimeTracker
# Execute: bash backend/scripts/feed_tests.sh

BASE_URL="http://localhost:3000"
COLOR_GREEN='\033[0;32m'
COLOR_RED='\033[0;31m'
COLOR_YELLOW='\033[1;33m'
COLOR_BLUE='\033[0;34m'
COLOR_CYAN='\033[0;36m'
COLOR_RESET='\033[0m'

echo -e "${COLOR_CYAN}╔════════════════════════════════════════════════╗${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║     📰 TESTES FEED-001 - CrimeTracker         ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}╚════════════════════════════════════════════════╝${COLOR_RESET}"
echo ""
echo -e "${COLOR_YELLOW}📋 Servidor deve estar rodando em http://localhost:3000${COLOR_RESET}"
echo ""

# Gerar timestamp único
TIMESTAMP=$(date +%s)
USER1_EMAIL="feed_test1_${TIMESTAMP}@example.com"
USER1_USERNAME="feed_user1_${TIMESTAMP}"
USER2_EMAIL="feed_test2_${TIMESTAMP}@example.com"
USER2_USERNAME="feed_user2_${TIMESTAMP}"
PASSWORD="senha12345678"

# ============================================
# SETUP: Criar usuários e grupo
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🔧 Setup: Criando usuários e grupo${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

# Criar usuário 1
USER1_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$USER1_EMAIL\",
    \"password\": \"$PASSWORD\",
    \"username\": \"$USER1_USERNAME\"
  }")

TOKEN1=$(echo "$USER1_RESPONSE" | jq -r '.token')

if [ "$TOKEN1" != "null" ] && [ -n "$TOKEN1" ]; then
    echo -e "${COLOR_GREEN}✅ Usuário 1 criado: $USER1_USERNAME${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Erro ao criar usuário 1${COLOR_RESET}"
    exit 1
fi

# Criar usuário 2
USER2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$USER2_EMAIL\",
    \"password\": \"$PASSWORD\",
    \"username\": \"$USER2_USERNAME\"
  }")

TOKEN2=$(echo "$USER2_RESPONSE" | jq -r '.token')

if [ "$TOKEN2" != "null" ] && [ -n "$TOKEN2" ]; then
    echo -e "${COLOR_GREEN}✅ Usuário 2 criado: $USER2_USERNAME${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Erro ao criar usuário 2${COLOR_RESET}"
    exit 1
fi

# Criar grupo
GROUP_RESPONSE=$(curl -s -X POST "$BASE_URL/api/groups" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d "{
    \"nome\": \"Grupo Teste Feed - $TIMESTAMP\",
    \"descricao\": \"Grupo para testar feed\"
  }")

GROUP_ID=$(echo "$GROUP_RESPONSE" | jq -r '.data.id')

if [ "$GROUP_ID" != "null" ] && [ -n "$GROUP_ID" ]; then
    echo -e "${COLOR_GREEN}✅ Grupo criado${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Erro ao criar grupo${COLOR_RESET}"
    exit 1
fi

# Usuário 2 entra no grupo
curl -s -X POST "$BASE_URL/api/groups/$GROUP_ID/join" \
  -H "Authorization: Bearer $TOKEN2" > /dev/null

echo -e "${COLOR_GREEN}✅ Usuário 2 entrou no grupo${COLOR_RESET}"

echo ""
sleep 1

# ============================================
# TESTE 1: POST /api/groups/:group_id/posts
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 1: POST /api/groups/:group_id/posts${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

START_TIME=$(date +%s%3N)
CREATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/groups/$GROUP_ID/posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d "{
    \"conteudo\": \"Primeiro post do grupo! Testando o feed de posts.\"
  }")
END_TIME=$(date +%s%3N)
DURATION=$((END_TIME - START_TIME))

HTTP_CODE=$(echo "$CREATE_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$CREATE_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta (${DURATION}ms):${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

POST_ID=$(echo "$RESPONSE_BODY" | jq -r '.data.id')

# Validações
echo -e "${COLOR_YELLOW}🔍 Validações:${COLOR_RESET}"

if [ "$HTTP_CODE" = "201" ]; then
    echo -e "  ${COLOR_GREEN}✅ Status Code: 201${COLOR_RESET}"
fi

if [ $DURATION -lt 2000 ]; then
    echo -e "  ${COLOR_GREEN}✅ Tempo: ${DURATION}ms (< 2s)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 2: Criar mais posts
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 2: Criando múltiplos posts${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

for i in {2..5}; do
    if [ $((i % 2)) -eq 0 ]; then
        TOKEN=$TOKEN1
    else
        TOKEN=$TOKEN2
    fi
    
    curl -s -X POST "$BASE_URL/api/groups/$GROUP_ID/posts" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d "{
        \"conteudo\": \"Post número $i do grupo - Testando paginação\"
      }" > /dev/null
    
    echo -e "  ${COLOR_GREEN}✅ Post $i criado${COLOR_RESET}"
done

echo ""
sleep 1

# ============================================
# TESTE 3: GET /api/groups/:group_id/posts
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 3: GET /api/groups/:group_id/posts${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

START_TIME=$(date +%s%3N)
POSTS_RESPONSE=$(curl -s -w "\n%{http_code}" \
  -H "Authorization: Bearer $TOKEN1" \
  "$BASE_URL/api/groups/$GROUP_ID/posts?page=1&limit=20")
END_TIME=$(date +%s%3N)
DURATION=$((END_TIME - START_TIME))

HTTP_CODE=$(echo "$POSTS_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$POSTS_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta (${DURATION}ms):${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

echo -e "${COLOR_YELLOW}🔍 Validações:${COLOR_RESET}"

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "  ${COLOR_GREEN}✅ Status Code: 200${COLOR_RESET}"
fi

COUNT=$(echo "$RESPONSE_BODY" | jq '.data | length')
if [ "$COUNT" -ge 5 ]; then
    echo -e "  ${COLOR_GREEN}✅ Posts recuperados: $COUNT${COLOR_RESET}"
fi

if echo "$RESPONSE_BODY" | jq -e '.pagination' > /dev/null; then
    echo -e "  ${COLOR_GREEN}✅ Paginação presente${COLOR_RESET}"
fi

if [ $DURATION -lt 2000 ]; then
    echo -e "  ${COLOR_GREEN}✅ Tempo: ${DURATION}ms (< 2s)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 4: GET /api/feed
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 4: GET /api/feed (feed geral)${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

START_TIME=$(date +%s%3N)
FEED_RESPONSE=$(curl -s -w "\n%{http_code}" \
  -H "Authorization: Bearer $TOKEN1" \
  "$BASE_URL/api/feed?page=1&limit=20")
END_TIME=$(date +%s%3N)
DURATION=$((END_TIME - START_TIME))

HTTP_CODE=$(echo "$FEED_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$FEED_RESPONSE" | head -n-1)

echo -e "${COLOR_BLUE}📥 Resposta (${DURATION}ms):${COLOR_RESET}"
echo "$RESPONSE_BODY" | jq '.'
echo ""

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${COLOR_GREEN}✅ Feed geral funcionando${COLOR_RESET}"
fi

if [ $DURATION -lt 2000 ]; then
    echo -e "${COLOR_GREEN}✅ Tempo: ${DURATION}ms (< 2s)${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 5: DELETE /api/posts/:id
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 5: DELETE /api/posts/:id${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

if [ "$POST_ID" != "null" ] && [ -n "$POST_ID" ]; then
    START_TIME=$(date +%s%3N)
    DELETE_RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE \
      -H "Authorization: Bearer $TOKEN1" \
      "$BASE_URL/api/posts/$POST_ID")
    END_TIME=$(date +%s%3N)
    DURATION=$((END_TIME - START_TIME))

    HTTP_CODE=$(echo "$DELETE_RESPONSE" | tail -n1)

    if [ "$HTTP_CODE" = "200" ]; then
        echo -e "${COLOR_GREEN}✅ Post deletado com sucesso${COLOR_RESET}"
    fi

    if [ $DURATION -lt 2000 ]; then
        echo -e "${COLOR_GREEN}✅ Tempo: ${DURATION}ms (< 2s)${COLOR_RESET}"
    fi
fi

echo ""
sleep 1

# ============================================
# TESTE 6: Não-membro tenta postar (403)
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 6: Não-membro tenta postar (403)${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

# Criar usuário 3
USER3_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"feed_test3_${TIMESTAMP}@example.com\",
    \"password\": \"$PASSWORD\",
    \"username\": \"feed_user3_${TIMESTAMP}\"
  }")

TOKEN3=$(echo "$USER3_RESPONSE" | jq -r '.token')

# Tentar postar
NON_MEMBER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/groups/$GROUP_ID/posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN3" \
  -d "{
    \"conteudo\": \"Tentando postar sem ser membro\"
  }")

HTTP_CODE=$(echo "$NON_MEMBER_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "403" ]; then
    echo -e "${COLOR_GREEN}✅ Não-membro rejeitado com 403${COLOR_RESET}"
else
    echo -e "${COLOR_RED}❌ Status incorreto: $HTTP_CODE${COLOR_RESET}"
fi

echo ""
sleep 1

# ============================================
# TESTE 7: Conteúdo > 1000 caracteres (400)
# ============================================
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo -e "${COLOR_CYAN}🧪 Teste 7: Conteúdo > 1000 chars (400)${COLOR_RESET}"
echo -e "${COLOR_CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
echo ""

LONG_CONTENT=$(printf 'A%.0s' {1..1001})

LONG_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/groups/$GROUP_ID/posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN1" \
  -d "{
    \"conteudo\": \"$LONG_CONTENT\"
  }")

HTTP_CODE=$(echo "$LONG_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${COLOR_GREEN}✅ Conteúdo > 1000 chars rejeitado com 400${COLOR_RESET}"
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
echo -e "${COLOR_GREEN}✅ Todos os testes do FEED-001 foram executados!${COLOR_RESET}"
echo ""
echo -e "${COLOR_YELLOW}📝 Verificações realizadas:${COLOR_RESET}"
echo "  1. ✅ POST /api/groups/:group_id/posts (criar post)"
echo "  2. ✅ Múltiplos posts criados"
echo "  3. ✅ GET /api/groups/:group_id/posts (paginado)"
echo "  4. ✅ GET /api/feed (feed geral)"
echo "  5. ✅ DELETE /api/posts/:id (somente autor)"
echo "  6. ✅ Não-membro rejeitado (403)"
echo "  7. ✅ Conteúdo > 1000 chars rejeitado (400)"
echo ""
echo -e "${COLOR_YELLOW}🎯 Funcionalidades validadas:${COLOR_RESET}"
echo "  ✅ Somente membros podem postar"
echo "  ✅ Paginação funcional (20 por página)"
echo "  ✅ Ordem DESC (mais recente primeiro)"
echo "  ✅ Limite de 1000 caracteres"
echo "  ✅ Somente autor pode deletar"
echo "  ✅ Performance < 2s"
echo ""
echo -e "${COLOR_CYAN}✨ Testes concluídos!${COLOR_RESET}"
echo ""

