#!/bin/bash

# CrimeTracker - Teste Integrado Completo (Bash)
# Execute: bash backend/scripts/test_all.sh

BASE_URL="http://localhost:3000"
COLOR_GREEN='\033[0;32m'
COLOR_RED='\033[0;31m'
COLOR_YELLOW='\033[1;33m'
COLOR_BLUE='\033[0;34m'
COLOR_CYAN='\033[0;36m'
COLOR_WHITE='\033[1;37m'
COLOR_RESET='\033[0m'

echo -e "${COLOR_CYAN}╔════════════════════════════════════════════════╗${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║     🧪 TESTE INTEGRADO - CrimeTracker         ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}╚════════════════════════════════════════════════╝${COLOR_RESET}"
echo ""

# Gerar timestamp único
TIMESTAMP=$(date +%s)
TEST_EMAIL="test_${TIMESTAMP}@example.com"
TEST_USERNAME="user_${TIMESTAMP}"
TEST_PASSWORD="senha12345678"

# Coordenadas de São Paulo
TEST_LAT=-23.5505
TEST_LON=-46.6333

total_tests=0
passed_tests=0
failed_tests=0
total_time=0

# Função para testar endpoint
test_endpoint() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    local token=$5
    local expected_status=${6:-200}
    
    ((total_tests++))
    
    echo -e "${COLOR_BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
    echo -e "${COLOR_CYAN}🧪 $name${COLOR_RESET}"
    echo -e "${COLOR_BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${COLOR_RESET}"
    
    start_time=$(date +%s%3N)
    
    if [ "$method" = "GET" ]; then
        if [ -n "$token" ]; then
            response=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $token" "$url")
        else
            response=$(curl -s -w "\n%{http_code}" "$url")
        fi
    else
        if [ -n "$token" ]; then
            response=$(curl -s -w "\n%{http_code}" -X "$method" -H "Content-Type: application/json" -H "Authorization: Bearer $token" -d "$data" "$url")
        else
            response=$(curl -s -w "\n%{http_code}" -X "$method" -H "Content-Type: application/json" -d "$data" "$url")
        fi
    fi
    
    end_time=$(date +%s%3N)
    duration=$((end_time - start_time))
    total_time=$((total_time + duration))
    
    http_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n-1)
    
    echo -e "${COLOR_YELLOW}⏱️  Tempo: ${duration}ms${COLOR_RESET}"
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${COLOR_GREEN}✅ Status: $http_code (sucesso)${COLOR_RESET}"
        ((passed_tests++))
        echo -e "${COLOR_GREEN}✅ Teste passou!${COLOR_RESET}"
    else
        echo -e "${COLOR_RED}❌ Status: $http_code (esperado: $expected_status)${COLOR_RESET}"
        ((failed_tests++))
        echo -e "${COLOR_RED}❌ Teste falhou!${COLOR_RESET}"
    fi
    
    echo ""
    echo "$response_body"
}

# ============================================
# INÍCIO DOS TESTES
# ============================================

echo -e "${COLOR_CYAN}🚀 Iniciando testes integrados...${COLOR_RESET}"
echo ""

# ============================================
# 1. HEALTH CHECK
# ============================================
test_endpoint "Health Check" "GET" "$BASE_URL/health" "" "" 200

# ============================================
# 2. REGISTRO DE USUÁRIO
# ============================================
register_data="{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\",\"username\":\"$TEST_USERNAME\"}"
register_response=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "$register_data")

test_endpoint "Registro de Usuário (AUTH-001)" "POST" "$BASE_URL/api/auth/register" "$register_data" "" 201

TOKEN=$(echo "$register_response" | jq -r '.token')

if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
    echo -e "${COLOR_RED}❌ Falha no registro. Abortando testes.${COLOR_RESET}"
    exit 1
fi

echo -e "${COLOR_WHITE}🔑 Token obtido: ${TOKEN:0:30}...${COLOR_RESET}"
echo ""

# ============================================
# 3. LOGIN DE USUÁRIO
# ============================================
login_data="{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}"
test_endpoint "Login de Usuário (AUTH-001)" "POST" "$BASE_URL/api/auth/login" "$login_data" "" 200

# ============================================
# 4. PERFIL DO USUÁRIO
# ============================================
test_endpoint "Buscar Perfil (AUTH-001)" "GET" "$BASE_URL/api/auth/profile" "" "$TOKEN" 200

# ============================================
# 5. CRIAR DENÚNCIA
# ============================================
report_data="{\"tipo\":\"Assalto\",\"descricao\":\"Assalto a mão armada próximo ao metrô. Teste automatizado.\",\"latitude\":$TEST_LAT,\"longitude\":$TEST_LON}"
report_response=$(curl -s -X POST "$BASE_URL/api/reports" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "$report_data")

test_endpoint "Criar Denúncia (CRIME-001)" "POST" "$BASE_URL/api/reports" "$report_data" "$TOKEN" 201

REPORT_ID=$(echo "$report_response" | jq -r '.data.id')

# ============================================
# 6. BUSCAR DENÚNCIAS PRÓXIMAS
# ============================================
nearby_response=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/reports/nearby?latitude=$TEST_LAT&longitude=$TEST_LON&radius_km=5")
test_endpoint "Buscar Denúncias Próximas (CRIME-001)" "GET" "$BASE_URL/api/reports/nearby?latitude=$TEST_LAT&longitude=$TEST_LON&radius_km=5" "" "$TOKEN" 200

nearby_count=$(echo "$nearby_response" | jq '.count')
echo -e "${COLOR_CYAN}📊 Denúncias encontradas: $nearby_count${COLOR_RESET}"
echo ""

# ============================================
# 7. BUSCAR DENÚNCIA POR ID
# ============================================
test_endpoint "Buscar Denúncia por ID (CRIME-001)" "GET" "$BASE_URL/api/reports/$REPORT_ID" "" "$TOKEN" 200

# ============================================
# 8. CRIAR GRUPO
# ============================================
group_data="{\"nome\":\"Grupo Teste - $TIMESTAMP\",\"descricao\":\"Grupo de teste automatizado\"}"
group_response=$(curl -s -X POST "$BASE_URL/api/groups" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "$group_data")

test_endpoint "Criar Grupo (GROUP-001)" "POST" "$BASE_URL/api/groups" "$group_data" "$TOKEN" 201

GROUP_ID=$(echo "$group_response" | jq -r '.data.id')
member_count=$(echo "$group_response" | jq '.data.member_count')
echo -e "${COLOR_CYAN}👥 Membros no grupo: $member_count${COLOR_RESET}"
echo ""

# ============================================
# 9. BUSCAR GRUPOS
# ============================================
search_response=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/groups?search=Teste")
test_endpoint "Buscar Grupos (GROUP-001)" "GET" "$BASE_URL/api/groups?search=Teste" "" "$TOKEN" 200

search_count=$(echo "$search_response" | jq '.count')
echo -e "${COLOR_CYAN}📊 Grupos encontrados: $search_count${COLOR_RESET}"
echo ""

# ============================================
# 10. CRIAR POST NO GRUPO
# ============================================
post_data="{\"conteudo\":\"Post de teste automatizado. Verificando funcionamento do feed.\"}"
post_response=$(curl -s -X POST "$BASE_URL/api/groups/$GROUP_ID/posts" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "$post_data")

test_endpoint "Criar Post no Grupo (FEED-001)" "POST" "$BASE_URL/api/groups/$GROUP_ID/posts" "$post_data" "$TOKEN" 201

POST_ID=$(echo "$post_response" | jq -r '.data.id')

# ============================================
# 11. LISTAR POSTS DO GRUPO
# ============================================
posts_response=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/groups/$GROUP_ID/posts?page=1&limit=20")
test_endpoint "Listar Posts do Grupo (FEED-001)" "GET" "$BASE_URL/api/groups/$GROUP_ID/posts?page=1&limit=20" "" "$TOKEN" 200

posts_total=$(echo "$posts_response" | jq '.pagination.total')
echo -e "${COLOR_CYAN}📊 Posts encontrados: $posts_total${COLOR_RESET}"
echo ""

# ============================================
# 12. FEED GERAL
# ============================================
feed_response=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/feed?page=1&limit=20")
test_endpoint "Feed Geral do Usuário (FEED-001)" "GET" "$BASE_URL/api/feed?page=1&limit=20" "" "$TOKEN" 200

feed_total=$(echo "$feed_response" | jq '.pagination.total')
echo -e "${COLOR_CYAN}📊 Posts no feed: $feed_total${COLOR_RESET}"
echo ""

# ============================================
# 13. DELETAR POST
# ============================================
test_endpoint "Deletar Post (FEED-001)" "DELETE" "$BASE_URL/api/posts/$POST_ID" "" "$TOKEN" 200

# ============================================
# RESUMO FINAL
# ============================================
echo ""
echo -e "${COLOR_CYAN}╔════════════════════════════════════════════════╗${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║           📊 RESUMO DOS TESTES                 ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}║                                                ║${COLOR_RESET}"
echo -e "${COLOR_CYAN}╚════════════════════════════════════════════════╝${COLOR_RESET}"
echo ""

avg_time=$((total_time / total_tests))

echo -e "${COLOR_CYAN}📊 Estatísticas:${COLOR_RESET}"
echo -e "${COLOR_WHITE}   Total de testes: $total_tests${COLOR_RESET}"
echo -e "${COLOR_GREEN}   ✅ Passou: $passed_tests${COLOR_RESET}"
echo -e "${COLOR_RED}   ❌ Falhou: $failed_tests${COLOR_RESET}"
echo -e "${COLOR_YELLOW}   ⏱️  Tempo total: ${total_time}ms${COLOR_RESET}"
echo -e "${COLOR_YELLOW}   ⏱️  Tempo médio: ${avg_time}ms por teste${COLOR_RESET}"
echo ""

echo -e "${COLOR_CYAN}📝 Módulos testados:${COLOR_RESET}"
echo -e "${COLOR_GREEN}   ✅ AUTH-001 - Autenticação (3 testes)${COLOR_RESET}"
echo -e "${COLOR_GREEN}   ✅ CRIME-001 - Denúncias (3 testes)${COLOR_RESET}"
echo -e "${COLOR_GREEN}   ✅ GROUP-001 - Grupos (2 testes)${COLOR_RESET}"
echo -e "${COLOR_GREEN}   ✅ FEED-001 - Feed (4 testes)${COLOR_RESET}"
echo ""

if [ $failed_tests -eq 0 ]; then
    echo -e "${COLOR_GREEN}🎉 Todos os testes passaram com sucesso!${COLOR_RESET}"
    echo -e "${COLOR_CYAN}✨ Sistema CrimeTracker 100% funcional!${COLOR_RESET}"
else
    echo -e "${COLOR_YELLOW}⚠️  Alguns testes falharam. Verifique os logs acima.${COLOR_RESET}"
fi

echo ""

# Retornar código de saída
exit $failed_tests

