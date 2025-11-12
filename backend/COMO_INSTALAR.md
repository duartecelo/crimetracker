# 🚀 Como Instalar e Testar o Backend CrimeTracker

## 📋 Pré-requisitos

1. **Node.js** v16 ou superior (instalado ✅)
2. **npm** (vem com Node.js)
3. **Visual Studio Build Tools** (para compilar better-sqlite3 no Windows)

---

## 🛠️ Passo 1: Instalar Visual Studio Build Tools (Windows)

O `better-sqlite3` precisa de ferramentas de compilação nativas no Windows.

### Opção A: Instalação Automática (Recomendada)

```powershell
# Como Administrador no PowerShell
npm install --global windows-build-tools
```

### Opção B: Instalação Manual

1. Baixe o **Visual Studio 2022 Build Tools**:
   - https://visualstudio.microsoft.com/downloads/
   - Role até "Tools for Visual Studio" → "Build Tools for Visual Studio 2022"

2. Durante a instalação, selecione:
   - ✅ **"Desktop development with C++"**
   - ✅ **"MSVC v143 - VS 2022 C++ x64/x86 build tools"**
   - ✅ **"Windows 11 SDK"**

3. Reinicie o computador após a instalação

### Opção C: Usar SQLite3 (Alternativa sem Build Tools)

Se não quiser instalar Build Tools, pode usar `sqlite3` ao invés de `better-sqlite3`:

```powershell
cd backend
npm uninstall better-sqlite3
npm install sqlite3
```

Depois, modifique `backend/database.js` para usar `sqlite3` em modo assíncrono.

---

## 📦 Passo 2: Instalar Dependências

```powershell
cd backend
npm install
```

Se houver erro no `better-sqlite3`:

```powershell
# Tente recompilar
npm rebuild better-sqlite3

# Ou instale diretamente
npm install better-sqlite3 --build-from-source
```

---

## ✅ Passo 3: Verificar Instalação

```powershell
# Verificar se better-sqlite3 foi instalado corretamente
node -e "console.log('Node:', process.version); try { require('better-sqlite3'); console.log('✅ better-sqlite3 OK'); } catch(e) { console.log('❌ better-sqlite3 ERRO:', e.message); }"
```

**Saída esperada:**
```
Node: v22.14.0
✅ better-sqlite3 OK
```

---

## 🚀 Passo 4: Iniciar o Servidor

```powershell
cd backend
npm run dev
```

**Saída esperada:**
```
🚀 Servidor rodando em http://0.0.0.0:3000
📦 Banco de dados conectado
  ✓ Tabela users criada
  ✓ Tabela crime_reports criada
  ✓ Tabela groups criada
  ✓ Tabela group_members criada
  ✓ Tabela posts criada
  ✓ Índices criados
✅ Todas as tabelas foram criadas com sucesso!
```

---

## 🧪 Passo 5: Testar o Sistema AUTH-001

### Opção A: PowerShell (Windows)

Em um **novo terminal PowerShell**:

```powershell
# Executar script de testes
.\backend\scripts\auth_tests.ps1
```

### Opção B: Bash (Git Bash ou WSL)

```bash
bash backend/scripts/auth_tests.sh
```

### Opção C: Testes Manuais com cURL

#### 1. Health Check
```powershell
curl http://localhost:3000/health
```

#### 2. Registrar Usuário
```powershell
$body = @{
    email = "teste@example.com"
    password = "senha12345678"
    username = "usuario123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:3000/api/auth/register" -Method Post -Body $body -ContentType "application/json"
```

#### 3. Login
```powershell
$body = @{
    email = "teste@example.com"
    password = "senha12345678"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:3000/api/auth/login" -Method Post -Body $body -ContentType "application/json"

# Salvar token
$token = $response.token
Write-Host "Token: $token"
```

#### 4. Testar Rota Protegida
```powershell
$headers = @{
    Authorization = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:3000/api/auth/profile" -Method Get -Headers $headers
```

---

## 📊 Estrutura Esperada

Após instalação bem-sucedida, você deve ter:

```
backend/
├── node_modules/          ✅ Dependências instaladas
│   ├── better-sqlite3/    ✅ SQLite nativo
│   ├── express/           ✅ Framework web
│   ├── bcryptjs/          ✅ Hash de senhas
│   ├── jsonwebtoken/      ✅ JWT tokens
│   └── ...
├── database/
│   └── crimetracker.db    ✅ Banco de dados SQLite (criado automaticamente)
├── routes/
├── services/
├── middleware/
├── scripts/
│   ├── auth_tests.sh      ✅ Testes bash
│   └── auth_tests.ps1     ✅ Testes PowerShell
└── server.js              ✅ Servidor
```

---

## ❌ Troubleshooting

### Erro: "Cannot find module 'better-sqlite3'"

**Solução:**
```powershell
cd backend
npm install better-sqlite3 --build-from-source
```

### Erro: "gyp ERR! find VS" ou "MSBuild.exe"

**Solução:** Instale Visual Studio Build Tools (Passo 1)

### Erro: "EADDRINUSE: address already in use"

**Solução:** Porta 3000 já está em uso. Mude em `backend/config.js`:
```javascript
server: {
  port: 3001, // ou outra porta livre
}
```

### Erro: "Module did not self-register"

**Solução:** Recompile o módulo:
```powershell
cd backend
npm rebuild better-sqlite3
```

### Servidor não inicia

**Solução:** Verifique logs e tente:
```powershell
cd backend
node server.js
```

---

## ✅ Checklist de Instalação

- [ ] Node.js v16+ instalado
- [ ] Visual Studio Build Tools instalado (Windows)
- [ ] `npm install` executado sem erros
- [ ] `better-sqlite3` importado com sucesso
- [ ] Servidor inicia em http://localhost:3000
- [ ] `/health` retorna `{success: true}`
- [ ] Testes AUTH-001 passam
- [ ] Banco de dados `crimetracker.db` criado

---

## 🎯 Próximos Passos

Após instalação bem-sucedida:

1. ✅ AUTH-001 está completo
2. 🔄 Próximo: Implementar **denúncias de crimes** (REPORT-001)
3. 🔄 Depois: Implementar **grupos de bairro** (GROUP-001)
4. 🔄 Depois: Implementar **feed de posts** (FEED-001)

---

## 📚 Documentação Adicional

- **AUTH-001_COMPLETE.md** - Documentação completa do sistema de autenticação
- **DATABASE_SCHEMA.md** - Esquema do banco de dados
- **TEST_SERVER.md** - Guia de testes
- **BACKEND_SETUP_COMPLETE.md** - Resumo do setup

---

## 🆘 Suporte

Se encontrar problemas, verifique:

1. **Node.js** está na versão correta: `node --version`
2. **Build Tools** estão instalados corretamente
3. **Porta 3000** está livre
4. **Antivírus** não está bloqueando o servidor
5. **Firewall** permite conexões locais

---

**Sistema de autenticação AUTH-001 pronto para uso! 🎉**

