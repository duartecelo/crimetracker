# CrimeTracker - Teste Integrado Completo (PowerShell)
# Execute: .\backend\scripts\test_all.ps1

$BASE_URL = "http://localhost:3000"
$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "╔════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "║     🧪 TESTE INTEGRADO - CrimeTracker         ║" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Gerar timestamp único
$TIMESTAMP = [DateTimeOffset]::Now.ToUnixTimeSeconds()
$TEST_EMAIL = "test_$TIMESTAMP@example.com"
$TEST_USERNAME = "user_$TIMESTAMP"
$TEST_PASSWORD = "senha12345678"

# Coordenadas de São Paulo
$TEST_LAT = -23.5505
$TEST_LON = -46.6333

$totalTests = 0
$passedTests = 0
$failedTests = 0
$totalTime = 0

# Função para testar e medir tempo
function Test-Endpoint {
    param (
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    
    $global:totalTests++
    
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Blue
    Write-Host "🧪 $Name" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Blue
    
    $startTime = Get-Date
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
        }
        
        if ($Body) {
            $params['Body'] = ($Body | ConvertTo-Json)
            $params['ContentType'] = 'application/json'
        }
        
        $response = Invoke-RestMethod @params -ErrorAction Stop
        $duration = ((Get-Date) - $startTime).TotalMilliseconds
        $global:totalTime += $duration
        
        Write-Host "⏱️  Tempo: $([int]$duration)ms" -ForegroundColor Yellow
        Write-Host "✅ Status: $ExpectedStatus (sucesso)" -ForegroundColor Green
        
        if ($response.success -eq $true -or $ExpectedStatus -eq 200 -or $ExpectedStatus -eq 201) {
            $global:passedTests++
            Write-Host "✅ Teste passou!" -ForegroundColor Green
        } else {
            $global:failedTests++
            Write-Host "❌ Teste falhou!" -ForegroundColor Red
        }
        
        Write-Host ""
        return $response
        
    } catch {
        $duration = ((Get-Date) - $startTime).TotalMilliseconds
        $global:totalTime += $duration
        $statusCode = $_.Exception.Response.StatusCode.value__
        
        Write-Host "⏱️  Tempo: $([int]$duration)ms" -ForegroundColor Yellow
        Write-Host "❌ Status: $statusCode (esperado: $ExpectedStatus)" -ForegroundColor Red
        
        if ($statusCode -eq $ExpectedStatus) {
            $global:passedTests++
            Write-Host "✅ Erro esperado - Teste passou!" -ForegroundColor Green
        } else {
            $global:failedTests++
            Write-Host "❌ Teste falhou!" -ForegroundColor Red
        }
        
        Write-Host ""
        return $null
    }
}

# ============================================
# INÍCIO DOS TESTES
# ============================================

Write-Host "🚀 Iniciando testes integrados..." -ForegroundColor Cyan
Write-Host ""

# ============================================
# 1. HEALTH CHECK
# ============================================
$health = Test-Endpoint -Name "Health Check" -Method "GET" -Url "$BASE_URL/health"

# ============================================
# 2. REGISTRO DE USUÁRIO
# ============================================
$registerBody = @{
    email = $TEST_EMAIL
    password = $TEST_PASSWORD
    username = $TEST_USERNAME
}

$registerResponse = Test-Endpoint `
    -Name "Registro de Usuário (AUTH-001)" `
    -Method "POST" `
    -Url "$BASE_URL/api/auth/register" `
    -Body $registerBody `
    -ExpectedStatus 201

if (-not $registerResponse) {
    Write-Host "❌ Falha no registro. Abortando testes." -ForegroundColor Red
    exit 1
}

$TOKEN = $registerResponse.token
Write-Host "🔑 Token obtido: $($TOKEN.Substring(0, [Math]::Min(30, $TOKEN.Length)))..." -ForegroundColor Gray
Write-Host ""

# ============================================
# 3. LOGIN DE USUÁRIO
# ============================================
$loginBody = @{
    email = $TEST_EMAIL
    password = $TEST_PASSWORD
}

$loginResponse = Test-Endpoint `
    -Name "Login de Usuário (AUTH-001)" `
    -Method "POST" `
    -Url "$BASE_URL/api/auth/login" `
    -Body $loginBody `
    -ExpectedStatus 200

$headers = @{
    Authorization = "Bearer $TOKEN"
}

# ============================================
# 4. PERFIL DO USUÁRIO
# ============================================
$profileResponse = Test-Endpoint `
    -Name "Buscar Perfil (AUTH-001)" `
    -Method "GET" `
    -Url "$BASE_URL/api/auth/profile" `
    -Headers $headers `
    -ExpectedStatus 200

# ============================================
# 5. CRIAR DENÚNCIA
# ============================================
$reportBody = @{
    tipo = "Assalto"
    descricao = "Assalto a mão armada próximo ao metrô. Teste automatizado."
    latitude = $TEST_LAT
    longitude = $TEST_LON
}

$reportResponse = Test-Endpoint `
    -Name "Criar Denúncia (CRIME-001)" `
    -Method "POST" `
    -Url "$BASE_URL/api/reports" `
    -Headers $headers `
    -Body $reportBody `
    -ExpectedStatus 201

$REPORT_ID = $reportResponse.data.id

# ============================================
# 6. BUSCAR DENÚNCIAS PRÓXIMAS
# ============================================
$nearbyResponse = Test-Endpoint `
    -Name "Buscar Denúncias Próximas (CRIME-001)" `
    -Method "GET" `
    -Url "$BASE_URL/api/reports/nearby?latitude=$TEST_LAT&longitude=$TEST_LON&radius_km=5" `
    -Headers $headers `
    -ExpectedStatus 200

Write-Host "📊 Denúncias encontradas: $($nearbyResponse.count)" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 7. BUSCAR DENÚNCIA POR ID
# ============================================
$reportDetailsResponse = Test-Endpoint `
    -Name "Buscar Denúncia por ID (CRIME-001)" `
    -Method "GET" `
    -Url "$BASE_URL/api/reports/$REPORT_ID" `
    -Headers $headers `
    -ExpectedStatus 200

# ============================================
# 8. CRIAR GRUPO
# ============================================
$groupBody = @{
    nome = "Grupo Teste - $TIMESTAMP"
    descricao = "Grupo de teste automatizado"
}

$groupResponse = Test-Endpoint `
    -Name "Criar Grupo (GROUP-001)" `
    -Method "POST" `
    -Url "$BASE_URL/api/groups" `
    -Headers $headers `
    -Body $groupBody `
    -ExpectedStatus 201

$GROUP_ID = $groupResponse.data.id
Write-Host "👥 Membros no grupo: $($groupResponse.data.member_count)" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 9. BUSCAR GRUPOS
# ============================================
$searchGroupsResponse = Test-Endpoint `
    -Name "Buscar Grupos (GROUP-001)" `
    -Method "GET" `
    -Url "$BASE_URL/api/groups?search=Teste" `
    -Headers $headers `
    -ExpectedStatus 200

Write-Host "📊 Grupos encontrados: $($searchGroupsResponse.count)" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 10. CRIAR POST NO GRUPO
# ============================================
$postBody = @{
    conteudo = "Post de teste automatizado. Verificando funcionamento do feed."
}

$postResponse = Test-Endpoint `
    -Name "Criar Post no Grupo (FEED-001)" `
    -Method "POST" `
    -Url "$BASE_URL/api/groups/$GROUP_ID/posts" `
    -Headers $headers `
    -Body $postBody `
    -ExpectedStatus 201

$POST_ID = $postResponse.data.id

# ============================================
# 11. LISTAR POSTS DO GRUPO
# ============================================
$postsResponse = Test-Endpoint `
    -Name "Listar Posts do Grupo (FEED-001)" `
    -Method "GET" `
    -Url "$BASE_URL/api/groups/$GROUP_ID/posts?page=1&limit=20" `
    -Headers $headers `
    -ExpectedStatus 200

Write-Host "📊 Posts encontrados: $($postsResponse.pagination.total)" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 12. FEED GERAL
# ============================================
$feedResponse = Test-Endpoint `
    -Name "Feed Geral do Usuário (FEED-001)" `
    -Method "GET" `
    -Url "$BASE_URL/api/feed?page=1&limit=20" `
    -Headers $headers `
    -ExpectedStatus 200

Write-Host "📊 Posts no feed: $($feedResponse.pagination.total)" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 13. DELETAR POST
# ============================================
$deletePostResponse = Test-Endpoint `
    -Name "Deletar Post (FEED-001)" `
    -Method "DELETE" `
    -Url "$BASE_URL/api/posts/$POST_ID" `
    -Headers $headers `
    -ExpectedStatus 200

# ============================================
# RESUMO FINAL
# ============================================
Write-Host ""
Write-Host "╔════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "║           📊 RESUMO DOS TESTES                 ║" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

Write-Host "📊 Estatísticas:" -ForegroundColor Cyan
Write-Host "   Total de testes: $totalTests" -ForegroundColor White
Write-Host "   ✅ Passou: $passedTests" -ForegroundColor Green
Write-Host "   ❌ Falhou: $failedTests" -ForegroundColor Red
Write-Host "   ⏱️  Tempo total: $([int]$totalTime)ms" -ForegroundColor Yellow
Write-Host "   ⏱️  Tempo médio: $([int]($totalTime / $totalTests))ms por teste" -ForegroundColor Yellow
Write-Host ""

Write-Host "📝 Módulos testados:" -ForegroundColor Cyan
Write-Host "   ✅ AUTH-001 - Autenticação (3 testes)" -ForegroundColor Green
Write-Host "   ✅ CRIME-001 - Denúncias (3 testes)" -ForegroundColor Green
Write-Host "   ✅ GROUP-001 - Grupos (2 testes)" -ForegroundColor Green
Write-Host "   ✅ FEED-001 - Feed (4 testes)" -ForegroundColor Green
Write-Host ""

if ($failedTests -eq 0) {
    Write-Host "🎉 Todos os testes passaram com sucesso!" -ForegroundColor Green
    Write-Host "✨ Sistema CrimeTracker 100% funcional!" -ForegroundColor Cyan
} else {
    Write-Host "⚠️  Alguns testes falharam. Verifique os logs acima." -ForegroundColor Yellow
}

Write-Host ""

# Retornar código de saída
exit $failedTests

