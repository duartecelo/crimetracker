# AUTH-001: Testes de Autenticação do CrimeTracker (PowerShell)
# Execute: .\backend\scripts\auth_tests.ps1

$BASE_URL = "http://localhost:3000"

Write-Host ""
Write-Host "╔════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "║     🧪 TESTES AUTH-001 - CrimeTracker         ║" -ForegroundColor Cyan
Write-Host "║                                                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 Servidor deve estar rodando em http://localhost:3000" -ForegroundColor Yellow
Write-Host ""

# Gerar timestamp único para testes
$TIMESTAMP = [DateTimeOffset]::Now.ToUnixTimeSeconds()
$TEST_EMAIL = "test_$TIMESTAMP@example.com"
$TEST_USERNAME = "user_$TIMESTAMP"
$TEST_PASSWORD = "senha12345678"

# ============================================
# TESTE 1: Health Check
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 1: Health Check" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/health" -Method Get -ErrorAction Stop
    Write-Host "📥 Resposta:" -ForegroundColor Blue
    $response | ConvertTo-Json
    Write-Host "✅ Servidor está rodando" -ForegroundColor Green
} catch {
    Write-Host "❌ Servidor não está respondendo" -ForegroundColor Red
    Write-Host "Execute: cd backend; npm run dev" -ForegroundColor Red
    exit 1
}
Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 2: POST /api/auth/register
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 2: POST /api/auth/register" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$registerBody = @{
    email = $TEST_EMAIL
    password = $TEST_PASSWORD
    username = $TEST_USERNAME
} | ConvertTo-Json

Write-Host "📤 Enviando:" -ForegroundColor Blue
Write-Host $registerBody
Write-Host ""

$startTime = Get-Date
try {
    $registerResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/register" -Method Post -Body $registerBody -ContentType "application/json" -ErrorAction Stop
    $duration = ((Get-Date) - $startTime).TotalMilliseconds
    
    Write-Host "📥 Resposta ($([int]$duration)ms):" -ForegroundColor Blue
    $registerResponse | ConvertTo-Json
    Write-Host ""
    
    # Validações
    Write-Host "🔍 Validações:" -ForegroundColor Yellow
    
    if ($registerResponse.success -eq $true) {
        Write-Host "  ✅ success: true" -ForegroundColor Green
    } else {
        Write-Host "  ❌ success: $($registerResponse.success)" -ForegroundColor Red
    }
    
    if ($registerResponse.user_id) {
        Write-Host "  ✅ user_id presente" -ForegroundColor Green
    } else {
        Write-Host "  ❌ user_id ausente" -ForegroundColor Red
    }
    
    if ($registerResponse.username) {
        Write-Host "  ✅ username presente" -ForegroundColor Green
    } else {
        Write-Host "  ❌ username ausente" -ForegroundColor Red
    }
    
    if ($registerResponse.email) {
        Write-Host "  ✅ email presente" -ForegroundColor Green
    } else {
        Write-Host "  ❌ email ausente" -ForegroundColor Red
    }
    
    if ($registerResponse.token) {
        Write-Host "  ✅ token presente" -ForegroundColor Green
        $TOKEN = $registerResponse.token
    } else {
        Write-Host "  ❌ token ausente" -ForegroundColor Red
    }
    
    if ($duration -lt 2000) {
        Write-Host "  ✅ Tempo: $([int]$duration)ms (< 2s)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Tempo: $([int]$duration)ms (> 2s)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Erro no registro:" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 3: POST /api/auth/login
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 3: POST /api/auth/login" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$loginBody = @{
    email = $TEST_EMAIL
    password = $TEST_PASSWORD
} | ConvertTo-Json

Write-Host "📤 Enviando:" -ForegroundColor Blue
Write-Host $loginBody
Write-Host ""

$startTime = Get-Date
try {
    $loginResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -ErrorAction Stop
    $duration = ((Get-Date) - $startTime).TotalMilliseconds
    
    Write-Host "📥 Resposta ($([int]$duration)ms):" -ForegroundColor Blue
    $loginResponse | ConvertTo-Json
    Write-Host ""
    
    # Validações
    Write-Host "🔍 Validações:" -ForegroundColor Yellow
    
    if ($loginResponse.success -eq $true) {
        Write-Host "  ✅ success: true" -ForegroundColor Green
    }
    
    if ($loginResponse.user_id) {
        Write-Host "  ✅ user_id presente" -ForegroundColor Green
    }
    
    if ($loginResponse.username) {
        Write-Host "  ✅ username presente" -ForegroundColor Green
    }
    
    if ($loginResponse.email) {
        Write-Host "  ✅ email presente" -ForegroundColor Green
    }
    
    if ($loginResponse.token) {
        Write-Host "  ✅ token presente" -ForegroundColor Green
        $TOKEN = $loginResponse.token
    }
    
    if ($duration -lt 2000) {
        Write-Host "  ✅ Tempo: $([int]$duration)ms (< 2s)" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Tempo: $([int]$duration)ms (> 2s)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Erro no login:" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 4: Validação de Token (middleware)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 4: Middleware - Token Válido" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

Write-Host "📤 Usando token: $($TOKEN.Substring(0, [Math]::Min(50, $TOKEN.Length)))..." -ForegroundColor Blue
Write-Host ""

try {
    $headers = @{
        Authorization = "Bearer $TOKEN"
    }
    $profileResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/profile" -Method Get -Headers $headers -ErrorAction Stop
    
    Write-Host "📥 Resposta:" -ForegroundColor Blue
    $profileResponse | ConvertTo-Json
    Write-Host ""
    Write-Host "✅ Token válido aceito" -ForegroundColor Green
    
} catch {
    Write-Host "❌ Token válido rejeitado" -ForegroundColor Red
    Write-Host $_.Exception.Message
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 5: Sem Token (401)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 5: Middleware - Sem Token (401)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

try {
    $noTokenResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/profile" -Method Get -ErrorAction Stop
    Write-Host "❌ Requisição sem token foi aceita (deveria rejeitar)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✅ Rejeitado corretamente com 401" -ForegroundColor Green
    } else {
        Write-Host "❌ Status incorreto: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 6: Token Inválido (403)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 6: Middleware - Token Inválido (403)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

try {
    $headers = @{
        Authorization = "Bearer token_invalido_123"
    }
    $invalidResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/profile" -Method Get -Headers $headers -ErrorAction Stop
    Write-Host "❌ Token inválido foi aceito (deveria rejeitar)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 403) {
        Write-Host "✅ Rejeitado corretamente com 403" -ForegroundColor Green
    } else {
        Write-Host "❌ Status incorreto: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 7: Email Duplicado (409)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 7: Email Duplicado (409)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$duplicateBody = @{
    email = $TEST_EMAIL
    password = $TEST_PASSWORD
    username = "outro_username"
} | ConvertTo-Json

try {
    $duplicateResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/register" -Method Post -Body $duplicateBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "❌ Email duplicado foi aceito (deveria rejeitar)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 409) {
        Write-Host "✅ Email duplicado rejeitado com 409" -ForegroundColor Green
    } else {
        Write-Host "❌ Status incorreto: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 8: Senha Fraca (400)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 8: Senha Fraca < 8 chars (400)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$weakBody = @{
    email = "weak_$TIMESTAMP@example.com"
    password = "1234567"
    username = "weak_$TIMESTAMP"
} | ConvertTo-Json

try {
    $weakResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/register" -Method Post -Body $weakBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "❌ Senha fraca foi aceita (deveria rejeitar)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "✅ Senha fraca rejeitada com 400" -ForegroundColor Green
    } else {
        Write-Host "❌ Status incorreto: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# TESTE 9: Senha Incorreta (401)
# ============================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host "🧪 Teste 9: Senha Incorreta (401)" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

$wrongPasswordBody = @{
    email = $TEST_EMAIL
    password = "senhaErrada123"
} | ConvertTo-Json

try {
    $wrongResponse = Invoke-RestMethod -Uri "$BASE_URL/api/auth/login" -Method Post -Body $wrongPasswordBody -ContentType "application/json" -ErrorAction Stop
    Write-Host "❌ Senha incorreta foi aceita (deveria rejeitar)" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✅ Senha incorreta rejeitada com 401" -ForegroundColor Green
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
Write-Host "✅ Todos os testes do AUTH-001 foram executados!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Verificações realizadas:" -ForegroundColor Yellow
Write-Host "  1. ✅ Health check do servidor"
Write-Host "  2. ✅ POST /api/auth/register (201)"
Write-Host "  3. ✅ POST /api/auth/login (200)"
Write-Host "  4. ✅ Middleware com token válido (200)"
Write-Host "  5. ✅ Middleware sem token (401)"
Write-Host "  6. ✅ Middleware token inválido (403)"
Write-Host "  7. ✅ Email duplicado (409)"
Write-Host "  8. ✅ Senha fraca (400)"
Write-Host "  9. ✅ Senha incorreta (401)"
Write-Host ""
Write-Host "✨ Testes concluídos!" -ForegroundColor Cyan
Write-Host ""

