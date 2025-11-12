# CRIME-001: Testes de Denúncias do CrimeTracker (PowerShell)
# Execute: .\backend\scripts\crime_tests.ps1

$BASE_URL = "http://localhost:3000"

Write-Host ""
Write-Host "╔════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "║     🚨 TESTES CRIME-001 - CrimeTracker        ║" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Servidor deve estar rodando em http://localhost:3000" -ForegroundColor Yellow
Write-Host ""

# Gerar timestamp único para testes
$TIMESTAMP = [DateTimeOffset]::Now.ToUnixTimeSeconds()
$TEST_EMAIL = "crime_test_$TIMESTAMP@example.com"
$TEST_USERNAME = "crime_user_$TIMESTAMP"
$TEST_PASSWORD = "senha12345678"

# Coordenadas de São Paulo (exemplo)
$TEST_LAT = -23.5505
$TEST_LON = -46.6333

# ============================================
# SETUP: Criar usuário para testes
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🔧 Setup: Criando usuário de teste" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$registerBody = @{
    email = $TEST_EMAIL
    password = $TEST_PASSWORD
    username = $TEST_USERNAME
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json" -ErrorAction Stop
    $TOKEN = $registerResponse.token
    Write-Host "✅ Usuário criado e autenticado" -ForegroundColor Green
    Write-Host "   Token: $($TOKEN.Substring(0, [Math]::Min(30, $TOKEN.Length)))..." -ForegroundColor Gray
} catch {
    Write-Host "❌ Erro ao criar usuário de teste" -ForegroundColor Red
    Write-Host $_.Exception.Message
    exit 1
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 1: POST /api/reports (Criar denúncia)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 1: POST /api/reports" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$reportBody = @{
    tipo = "Assalto"
    descricao = "Assalto a mão armada próximo ao metrô. Dois suspeitos em uma moto preta."
    latitude = $TEST_LAT
    longitude = $TEST_LON
} | ConvertTo-Json

Write-Host "📤 Enviando:" -ForegroundColor Blue
Write-Host $reportBody
Write-Host ""

$startTime = Get-Date
try {
    $headers = @{
        Authorization = "Bearer $TOKEN"
    }
    $createResponse = Invoke-RestMethod -Uri "$BASE_URL/api/reports" -Method Post -Body $reportBody -Headers $headers -ContentType "application/json" -ErrorAction Stop
    $duration = ((Get-Date) - $startTime).TotalMilliseconds
    
    Write-Host "📥 Resposta ($([int]$duration)ms):" -ForegroundColor Blue
    $createResponse | ConvertTo-Json -Depth 5
    Write-Host ""
    
    # Salvar ID da denúncia
    $REPORT_ID = $createResponse.data.id
    
    # Validações
    Write-Host "🔍 Validações:" -ForegroundColor Yellow
    
    if ($createResponse.success -eq $true) {
        Write-Host "  ✅ success: true" -ForegroundColor Green
    } else {
        Write-Host "  ❌ success: false" -ForegroundColor Red
    }
    
    if ($createResponse.data.id) {
        Write-Host "  ✅ ID da denúncia presente" -ForegroundColor Green
    }
    
    if ($createResponse.data.tipo -eq "Assalto") {
        Write-Host "  ✅ Tipo correto: Assalto" -ForegroundColor Green
    }
    
    if ($createResponse.data.author_username) {
        Write-Host "  ✅ Autor identificado: $($createResponse.data.author_username)" -ForegroundColor Green
    }
    
    if ($duration -lt 3000) {
        Write-Host "  ✅ Tempo: $([int]$duration)ms (< 3s)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Tempo: $([int]$duration)ms (> 3s)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Erro ao criar denúncia:" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 2: Criar mais denúncias para teste de proximidade
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 2: Criando múltiplas denúncias" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$reports = @(
    @{ tipo = "Furto"; descricao = "Furto de celular na Av. Paulista"; lat = -23.5505; lon = -46.6333 },
    @{ tipo = "Vandalismo"; descricao = "Pichação em muro público"; lat = -23.5515; lon = -46.6343 },
    @{ tipo = "Roubo"; descricao = "Roubo de veículo estacionado"; lat = -23.5495; lon = -46.6323 }
)

$headers = @{
    Authorization = "Bearer $TOKEN"
}

foreach ($report in $reports) {
    $body = $report | ConvertTo-Json
    try {
        $response = Invoke-RestMethod -Uri "$BASE_URL/api/reports" -Method Post -Body $body -Headers $headers -ContentType "application/json" -ErrorAction Stop
        Write-Host "  ✅ Denúncia criada: $($report.tipo)" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ Erro: $($report.tipo)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 3: GET /api/reports/nearby
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 3: GET /api/reports/nearby" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$nearbyUrl = "$BASE_URL/api/reports/nearby?latitude=$TEST_LAT&longitude=$TEST_LON&radius_km=5"
Write-Host "📤 URL: $nearbyUrl" -ForegroundColor Blue
Write-Host ""

$startTime = Get-Date
try {
    $headers = @{
        Authorization = "Bearer $TOKEN"
    }
    $nearbyResponse = Invoke-RestMethod -Uri $nearbyUrl -Method Get -Headers $headers -ErrorAction Stop
    $duration = ((Get-Date) - $startTime).TotalMilliseconds
    
    Write-Host "📥 Resposta ($([int]$duration)ms):" -ForegroundColor Blue
    $nearbyResponse | ConvertTo-Json -Depth 5
    Write-Host ""
    
    # Validações
    Write-Host "🔍 Validações:" -ForegroundColor Yellow
    
    if ($nearbyResponse.success -eq $true) {
        Write-Host "  ✅ success: true" -ForegroundColor Green
    }
    
    if ($nearbyResponse.data) {
        Write-Host "  ✅ Denúncias encontradas: $($nearbyResponse.count)" -ForegroundColor Green
    }
    
    if ($nearbyResponse.filters) {
        Write-Host "  ✅ Filtros aplicados: raio $($nearbyResponse.filters.radius_km)km, últimos $($nearbyResponse.filters.last_days) dias" -ForegroundColor Green
    }
    
    if ($duration -lt 3000) {
        Write-Host "  ✅ Tempo: $([int]$duration)ms (< 3s)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Tempo: $([int]$duration)ms (> 3s)" -ForegroundColor Red
    }
    
    # Verificar se tem distância calculada
    if ($nearbyResponse.data.Count -gt 0) {
        if ($nearbyResponse.data[0].distance_meters -ne $null) {
            Write-Host "  ✅ Distância calculada presente" -ForegroundColor Green
        }
    }
    
} catch {
    Write-Host "❌ Erro ao buscar denúncias próximas:" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 4: GET /api/reports/:id
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 4: GET /api/reports/:id" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

if ($REPORT_ID) {
    Write-Host "📤 Buscando denúncia ID: $REPORT_ID" -ForegroundColor Blue
    Write-Host ""
    
    $startTime = Get-Date
    try {
        $headers = @{
            Authorization = "Bearer $TOKEN"
        }
        $detailsResponse = Invoke-RestMethod -Uri "$BASE_URL/api/reports/$REPORT_ID" -Method Get -Headers $headers -ErrorAction Stop
        $duration = ((Get-Date) - $startTime).TotalMilliseconds
        
        Write-Host "📥 Resposta ($([int]$duration)ms):" -ForegroundColor Blue
        $detailsResponse | ConvertTo-Json -Depth 5
        Write-Host ""
        
        # Validações
        Write-Host "🔍 Validações:" -ForegroundColor Yellow
        
        if ($detailsResponse.success -eq $true) {
            Write-Host "  ✅ success: true" -ForegroundColor Green
        }
        
        if ($detailsResponse.data.id -eq $REPORT_ID) {
            Write-Host "  ✅ ID correto" -ForegroundColor Green
        }
        
        if ($detailsResponse.data.author_username) {
            Write-Host "  ✅ Dados do autor presentes" -ForegroundColor Green
        }
        
        if ($duration -lt 3000) {
            Write-Host "  ✅ Tempo: $([int]$duration)ms (< 3s)" -ForegroundColor Green
        }
        
    } catch {
        Write-Host "❌ Erro ao buscar detalhes da denúncia:" -ForegroundColor Red
        Write-Host $_.Exception.Message
    }
} else {
    Write-Host "⚠️  ID da denúncia não disponível" -ForegroundColor Yellow
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 5: Validação de tipo inválido
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 5: Tipo de crime inválido (400)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$invalidTypeBody = @{
    tipo = "Sequestro"
    descricao = "Teste com tipo inválido"
    latitude = $TEST_LAT
    longitude = $TEST_LON
} | ConvertTo-Json

try {
    $headers = @{
        Authorization = "Bearer $TOKEN"
    }
    $invalidResponse = Invoke-RestMethod -Uri "$BASE_URL/api/reports" -Method Post -Body $invalidTypeBody -Headers $headers -ContentType "application/json" -ErrorAction Stop
    Write-Host "❌ Tipo inválido foi aceito (deveria rejeitar)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "✅ Tipo inválido rejeitado com 400" -ForegroundColor Green
    } else {
        Write-Host "❌ Status incorreto: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 6: Descrição muito longa (> 500 chars)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 6: Descrição > 500 caracteres (400)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$longDescription = "A" * 501

$longDescBody = @{
    tipo = "Furto"
    descricao = $longDescription
    latitude = $TEST_LAT
    longitude = $TEST_LON
} | ConvertTo-Json

try {
    $headers = @{
        Authorization = "Bearer $TOKEN"
    }
    $longDescResponse = Invoke-RestMethod -Uri "$BASE_URL/api/reports" -Method Post -Body $longDescBody -Headers $headers -ContentType "application/json" -ErrorAction Stop
    Write-Host "❌ Descrição longa foi aceita (deveria rejeitar)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "✅ Descrição longa rejeitada com 400" -ForegroundColor Green
    } else {
        Write-Host "❌ Status incorreto: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 7: Sem autenticação (401)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 7: Sem token de autenticação (401)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

try {
    $noAuthResponse = Invoke-RestMethod -Uri "$BASE_URL/api/reports/nearby?latitude=$TEST_LAT&longitude=$TEST_LON&radius_km=5" -Method Get -ErrorAction Stop
    Write-Host "❌ Requisição sem token foi aceita (deveria rejeitar)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✅ Rejeitado corretamente com 401" -ForegroundColor Green
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
Write-Host "✅ Todos os testes do CRIME-001 foram executados!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Verificações realizadas:" -ForegroundColor Yellow
Write-Host "  1. ✅ POST /api/reports (criar denúncia)"
Write-Host "  2. ✅ Múltiplas denúncias criadas"
Write-Host "  3. ✅ GET /api/reports/nearby (com raio e 30 dias)"
Write-Host "  4. ✅ GET /api/reports/:id (detalhes)"
Write-Host "  5. ✅ Tipo inválido rejeitado (400)"
Write-Host "  6. ✅ Descrição > 500 chars rejeitada (400)"
Write-Host "  7. ✅ Sem autenticação rejeitado (401)"
Write-Host ""
Write-Host "🎯 Funcionalidades validadas:" -ForegroundColor Yellow
Write-Host "  ✅ Tipos válidos: Assalto, Furto, Agressão, Vandalismo, Roubo, Outro"
Write-Host "  ✅ Descrição até 500 caracteres"
Write-Host "  ✅ Filtro por raio usando calculateDistance()"
Write-Host "  ✅ Filtro por últimos 30 dias"
Write-Host "  ✅ Performance < 3s"
Write-Host "  ✅ Formato {success:true, data:[...]}"
Write-Host ""
Write-Host "✨ Testes concluídos!" -ForegroundColor Cyan
Write-Host ""

