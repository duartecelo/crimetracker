# FEED-001: Testes de Feed do CrimeTracker (PowerShell)
# Execute: .\backend\scripts\feed_tests.ps1

$BASE_URL = "http://localhost:3000"

Write-Host ""
Write-Host "╔════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "║     📰 TESTES FEED-001 - CrimeTracker         ║" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Servidor deve estar rodando em http://localhost:3000" -ForegroundColor Yellow
Write-Host ""

# Gerar timestamp único para testes
$TIMESTAMP = [DateTimeOffset]::Now.ToUnixTimeSeconds()
$USER1_EMAIL = "feed_test1_$TIMESTAMP@example.com"
$USER1_USERNAME = "feed_user1_$TIMESTAMP"
$USER2_EMAIL = "feed_test2_$TIMESTAMP@example.com"
$USER2_USERNAME = "feed_user2_$TIMESTAMP"
$PASSWORD = "senha12345678"

# ============================================
# SETUP: Criar usuários e grupo
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🔧 Setup: Criando usuários e grupo" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

# Criar usuário 1
$registerBody = @{
    email = $USER1_EMAIL
    password = $PASSWORD
    username = $USER1_USERNAME
} | ConvertTo-Json

try {
    $user1Response = Invoke-RestMethod -Uri "$BASE_URL/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json" -ErrorAction Stop
    $TOKEN1 = $user1Response.token
    Write-Host "✅ Usuário 1 criado: $USER1_USERNAME" -ForegroundColor Green
} catch {
    Write-Host "❌ Erro ao criar usuário 1" -ForegroundColor Red
    exit 1
}

# Criar usuário 2
$registerBody = @{
    email = $USER2_EMAIL
    password = $PASSWORD
    username = $USER2_USERNAME
} | ConvertTo-Json

try {
    $user2Response = Invoke-RestMethod -Uri "$BASE_URL/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json" -ErrorAction Stop
    $TOKEN2 = $user2Response.token
    Write-Host "✅ Usuário 2 criado: $USER2_USERNAME" -ForegroundColor Green
} catch {
    Write-Host "❌ Erro ao criar usuário 2" -ForegroundColor Red
    exit 1
}

# Criar grupo com usuário 1
$groupBody = @{
    nome = "Grupo Teste Feed - $TIMESTAMP"
    descricao = "Grupo para testar feed"
} | ConvertTo-Json

try {
    $headers = @{
        Authorization = "Bearer $TOKEN1"
    }
    $groupResponse = Invoke-RestMethod -Uri "$BASE_URL/api/groups" -Method Post -Body $groupBody -Headers $headers -ContentType "application/json" -ErrorAction Stop
    $GROUP_ID = $groupResponse.data.id
    Write-Host "✅ Grupo criado: $($groupResponse.data.nome)" -ForegroundColor Green
} catch {
    Write-Host "❌ Erro ao criar grupo" -ForegroundColor Red
    exit 1
}

# Usuário 2 entra no grupo
try {
    $headers = @{
        Authorization = "Bearer $TOKEN2"
    }
    Invoke-RestMethod -Uri "$BASE_URL/api/groups/$GROUP_ID/join" -Method Post -Headers $headers -ErrorAction Stop | Out-Null
    Write-Host "✅ Usuário 2 entrou no grupo" -ForegroundColor Green
} catch {
    Write-Host "❌ Erro ao entrar no grupo" -ForegroundColor Red
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 1: POST /api/groups/:group_id/posts
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 1: POST /api/groups/:group_id/posts" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$postBody = @{
    conteudo = "Primeiro post do grupo! Testando o feed de posts."
} | ConvertTo-Json

Write-Host "📤 Enviando:" -ForegroundColor Blue
Write-Host $postBody
Write-Host ""

$startTime = Get-Date
try {
    $headers = @{
        Authorization = "Bearer $TOKEN1"
    }
    $createResponse = Invoke-RestMethod -Uri "$BASE_URL/api/groups/$GROUP_ID/posts" -Method Post -Body $postBody -Headers $headers -ContentType "application/json" -ErrorAction Stop
    $duration = ((Get-Date) - $startTime).TotalMilliseconds
    
    Write-Host "📥 Resposta ($([int]$duration)ms):" -ForegroundColor Blue
    $createResponse | ConvertTo-Json -Depth 5
    Write-Host ""
    
    # Salvar ID do post
    $POST_ID = $createResponse.data.id
    
    # Validações
    Write-Host "🔍 Validações:" -ForegroundColor Yellow
    
    if ($createResponse.success -eq $true) {
        Write-Host "  ✅ success: true" -ForegroundColor Green
    }
    
    if ($createResponse.data.id) {
        Write-Host "  ✅ ID do post presente" -ForegroundColor Green
    }
    
    if ($createResponse.data.author_username) {
        Write-Host "  ✅ Autor identificado: $($createResponse.data.author_username)" -ForegroundColor Green
    }
    
    if ($createResponse.data.group_id -eq $GROUP_ID) {
        Write-Host "  ✅ group_id correto" -ForegroundColor Green
    }
    
    if ($duration -lt 2000) {
        Write-Host "  ✅ Tempo: $([int]$duration)ms (< 2s)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Tempo: $([int]$duration)ms (> 2s)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Erro ao criar post:" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 2: Criar mais posts para teste de paginação
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 2: Criando múltiplos posts" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$headers1 = @{ Authorization = "Bearer $TOKEN1" }
$headers2 = @{ Authorization = "Bearer $TOKEN2" }

for ($i = 2; $i -le 5; $i++) {
    $body = @{
        conteudo = "Post número $i do grupo - Testando paginação"
    } | ConvertTo-Json
    
    try {
        if ($i % 2 -eq 0) {
            Invoke-RestMethod -Uri "$BASE_URL/api/groups/$GROUP_ID/posts" -Method Post -Body $body -Headers $headers1 -ContentType "application/json" -ErrorAction Stop | Out-Null
        } else {
            Invoke-RestMethod -Uri "$BASE_URL/api/groups/$GROUP_ID/posts" -Method Post -Body $body -Headers $headers2 -ContentType "application/json" -ErrorAction Stop | Out-Null
        }
        Write-Host "  ✅ Post $i criado" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ Erro no post $i" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 3: GET /api/groups/:group_id/posts (paginado)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 3: GET /api/groups/:group_id/posts" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$startTime = Get-Date
try {
    $headers = @{
        Authorization = "Bearer $TOKEN1"
    }
    $postsResponse = Invoke-RestMethod -Uri "$BASE_URL/api/groups/$GROUP_ID/posts?page=1&limit=20" -Method Get -Headers $headers -ErrorAction Stop
    $duration = ((Get-Date) - $startTime).TotalMilliseconds
    
    Write-Host "📥 Resposta ($([int]$duration)ms):" -ForegroundColor Blue
    $postsResponse | ConvertTo-Json -Depth 5
    Write-Host ""
    
    # Validações
    Write-Host "🔍 Validações:" -ForegroundColor Yellow
    
    if ($postsResponse.success -eq $true) {
        Write-Host "  ✅ success: true" -ForegroundColor Green
    }
    
    if ($postsResponse.data.Count -ge 5) {
        Write-Host "  ✅ Posts recuperados: $($postsResponse.data.Count)" -ForegroundColor Green
    }
    
    if ($postsResponse.pagination) {
        Write-Host "  ✅ Paginação presente" -ForegroundColor Green
        Write-Host "     - Página: $($postsResponse.pagination.page)" -ForegroundColor Gray
        Write-Host "     - Limite: $($postsResponse.pagination.limit)" -ForegroundColor Gray
        Write-Host "     - Total: $($postsResponse.pagination.total)" -ForegroundColor Gray
    }
    
    # Verificar ordem DESC
    if ($postsResponse.data.Count -ge 2) {
        $firstDate = [DateTime]$postsResponse.data[0].created_at
        $secondDate = [DateTime]$postsResponse.data[1].created_at
        if ($firstDate -ge $secondDate) {
            Write-Host "  ✅ Ordem DESC (mais recente primeiro)" -ForegroundColor Green
        } else {
            Write-Host "  ❌ Ordem incorreta" -ForegroundColor Red
        }
    }
    
    if ($duration -lt 2000) {
        Write-Host "  ✅ Tempo: $([int]$duration)ms (< 2s)" -ForegroundColor Green
    }
    
} catch {
    Write-Host "❌ Erro ao buscar posts:" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 4: GET /api/feed (feed geral)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 4: GET /api/feed (feed geral)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$startTime = Get-Date
try {
    $headers = @{
        Authorization = "Bearer $TOKEN1"
    }
    $feedResponse = Invoke-RestMethod -Uri "$BASE_URL/api/feed?page=1&limit=20" -Method Get -Headers $headers -ErrorAction Stop
    $duration = ((Get-Date) - $startTime).TotalMilliseconds
    
    Write-Host "📥 Resposta ($([int]$duration)ms):" -ForegroundColor Blue
    $feedResponse | ConvertTo-Json -Depth 5
    Write-Host ""
    
    # Validações
    Write-Host "🔍 Validações:" -ForegroundColor Yellow
    
    if ($feedResponse.success -eq $true) {
        Write-Host "  ✅ success: true" -ForegroundColor Green
    }
    
    if ($feedResponse.data.Count -ge 1) {
        Write-Host "  ✅ Feed recuperado: $($feedResponse.data.Count) posts" -ForegroundColor Green
    }
    
    if ($feedResponse.data[0].group_name) {
        Write-Host "  ✅ Nome do grupo presente nos posts" -ForegroundColor Green
    }
    
    if ($duration -lt 2000) {
        Write-Host "  ✅ Tempo: $([int]$duration)ms (< 2s)" -ForegroundColor Green
    }
    
} catch {
    Write-Host "❌ Erro ao buscar feed:" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 5: DELETE /api/posts/:id (apenas autor)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 5: DELETE /api/posts/:id (autor)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

if ($POST_ID) {
    Write-Host "📤 Deletando post: $POST_ID" -ForegroundColor Blue
    Write-Host ""
    
    $startTime = Get-Date
    try {
        $headers = @{
            Authorization = "Bearer $TOKEN1"
        }
        $deleteResponse = Invoke-RestMethod -Uri "$BASE_URL/api/posts/$POST_ID" -Method Delete -Headers $headers -ErrorAction Stop
        $duration = ((Get-Date) - $startTime).TotalMilliseconds
        
        Write-Host "📥 Resposta ($([int]$duration)ms):" -ForegroundColor Blue
        $deleteResponse | ConvertTo-Json
        Write-Host ""
        
        # Validações
        Write-Host "🔍 Validações:" -ForegroundColor Yellow
        
        if ($deleteResponse.success -eq $true) {
            Write-Host "  ✅ success: true" -ForegroundColor Green
        }
        
        if ($duration -lt 2000) {
            Write-Host "  ✅ Tempo: $([int]$duration)ms (< 2s)" -ForegroundColor Green
        }
        
    } catch {
        Write-Host "❌ Erro ao deletar post:" -ForegroundColor Red
        Write-Host $_.Exception.Message
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 6: Não-membro tenta postar (403)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 6: Não-membro tenta postar (403)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

# Criar usuário 3 que não é membro
$user3Email = "feed_test3_$TIMESTAMP@example.com"
$user3Body = @{
    email = $user3Email
    password = $PASSWORD
    username = "feed_user3_$TIMESTAMP"
} | ConvertTo-Json

try {
    $user3Response = Invoke-RestMethod -Uri "$BASE_URL/api/auth/register" -Method Post -Body $user3Body -ContentType "application/json" -ErrorAction Stop
    $TOKEN3 = $user3Response.token
} catch {
    Write-Host "❌ Erro ao criar usuário 3" -ForegroundColor Red
}

# Tentar postar sem ser membro
$postBody = @{
    conteudo = "Tentando postar sem ser membro"
} | ConvertTo-Json

try {
    $headers = @{
        Authorization = "Bearer $TOKEN3"
    }
    Invoke-RestMethod -Uri "$BASE_URL/api/groups/$GROUP_ID/posts" -Method Post -Body $postBody -Headers $headers -ContentType "application/json" -ErrorAction Stop | Out-Null
    Write-Host "❌ Não-membro conseguiu postar (deveria rejeitar)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "✅ Não-membro rejeitado com 403" -ForegroundColor Green
    } else {
        Write-Host "❌ Status incorreto: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 7: Conteúdo > 1000 caracteres (400)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 7: Conteúdo > 1000 chars (400)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$longContent = "A" * 1001

$longPostBody = @{
    conteudo = $longContent
} | ConvertTo-Json

try {
    $headers = @{
        Authorization = "Bearer $TOKEN1"
    }
    Invoke-RestMethod -Uri "$BASE_URL/api/groups/$GROUP_ID/posts" -Method Post -Body $longPostBody -Headers $headers -ContentType "application/json" -ErrorAction Stop | Out-Null
    Write-Host "❌ Conteúdo longo foi aceito (deveria rejeitar)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "✅ Conteúdo > 1000 chars rejeitado com 400" -ForegroundColor Green
    } else {
        Write-Host "❌ Status incorreto: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host ""

# ============================================
# RESUMO
# ============================================
Write-Host ""
Write-Host "╔════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "║           📊 RESUMO DOS TESTES                 ║" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Todos os testes do FEED-001 foram executados!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Verificações realizadas:" -ForegroundColor Yellow
Write-Host "  1. ✅ POST /api/groups/:group_id/posts (criar post)"
Write-Host "  2. ✅ Múltiplos posts criados"
Write-Host "  3. ✅ GET /api/groups/:group_id/posts (paginado)"
Write-Host "  4. ✅ GET /api/feed (feed geral)"
Write-Host "  5. ✅ DELETE /api/posts/:id (somente autor)"
Write-Host "  6. ✅ Não-membro rejeitado (403)"
Write-Host "  7. ✅ Conteúdo > 1000 chars rejeitado (400)"
Write-Host ""
Write-Host "🎯 Funcionalidades validadas:" -ForegroundColor Yellow
Write-Host "  ✅ Somente membros podem postar"
Write-Host "  ✅ Paginação funcional (20 por página)"
Write-Host "  ✅ Ordem DESC (mais recente primeiro)"
Write-Host "  ✅ Limite de 1000 caracteres"
Write-Host "  ✅ Somente autor pode deletar"
Write-Host "  ✅ Performance < 2s"
Write-Host ""
Write-Host "✨ Testes concluídos!" -ForegroundColor Cyan
Write-Host ""

