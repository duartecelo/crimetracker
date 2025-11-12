# 🧪 Guia de Testes do CrimeTracker Backend

## 📋 Visão Geral

O CrimeTracker possui uma suíte completa de testes automatizados que validam todas as funcionalidades do sistema. Existem dois tipos de testes:

1. **Testes por Módulo**: Scripts individuais para cada módulo (AUTH, CRIME, GROUP, FEED)
2. **Teste Integrado**: Script único que valida todo o fluxo da aplicação

---

## 🚀 Como Executar

### Pré-requisitos

1. **Servidor deve estar rodando:**
   ```bash
   cd backend
   npm run dev
   ```

2. **Aguarde a mensagem:**
   ```
   ✅ Pronto para receber requisições!
   ```

---

## 🔬 Teste Integrado Completo

O teste integrado valida o fluxo completo da aplicação em uma única execução.

### PowerShell (Windows)

```powershell
.\backend\scripts\test_all.ps1
```

### Bash (Linux/Mac/Git Bash)

```bash
bash backend/scripts/test_all.sh
```

### O que é testado:

1. **Health Check** - Verifica se o servidor está funcionando
2. **Registro de Usuário (AUTH-001)** - Cria novo usuário
3. **Login (AUTH-001)** - Autentica usuário
4. **Perfil (AUTH-001)** - Busca dados do usuário
5. **Criar Denúncia (CRIME-001)** - Registra crime
6. **Buscar Denúncias Próximas (CRIME-001)** - Filtra por raio
7. **Buscar Denúncia por ID (CRIME-001)** - Detalhes específicos
8. **Criar Grupo (GROUP-001)** - Cria grupo de bairro
9. **Buscar Grupos (GROUP-001)** - Lista e filtra grupos
10. **Criar Post (FEED-001)** - Posta no grupo
11. **Listar Posts (FEED-001)** - Feed do grupo paginado
12. **Feed Geral (FEED-001)** - Feed personalizado do usuário
13. **Deletar Post (FEED-001)** - Remove post

### Saída Esperada:

```
╔════════════════════════════════════════════════╗
║                                                ║
║     🧪 TESTE INTEGRADO - CrimeTracker         ║
║                                                ║
╚════════════════════════════════════════════════╝

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🧪 Registro de Usuário (AUTH-001)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⏱️  Tempo: 145ms
✅ Status: 201 (sucesso)
✅ Teste passou!

... (mais testes) ...

╔════════════════════════════════════════════════╗
║                                                ║
║           📊 RESUMO DOS TESTES                 ║
║                                                ║
╚════════════════════════════════════════════════╝

📊 Estatísticas:
   Total de testes: 13
   ✅ Passou: 13
   ❌ Falhou: 0
   ⏱️  Tempo total: 1250ms
   ⏱️  Tempo médio: 96ms por teste

📝 Módulos testados:
   ✅ AUTH-001 - Autenticação (3 testes)
   ✅ CRIME-001 - Denúncias (3 testes)
   ✅ GROUP-001 - Grupos (2 testes)
   ✅ FEED-001 - Feed (4 testes)

🎉 Todos os testes passaram com sucesso!
✨ Sistema CrimeTracker 100% funcional!
```

---

## 📦 Testes por Módulo

### 1. Testes de Autenticação (AUTH-001)

**10 testes** que validam registro, login e autenticação JWT.

```powershell
# PowerShell
.\backend\scripts\auth_tests.ps1

# Bash
bash backend/scripts/auth_tests.sh
```

**Testes incluídos:**
- Registro com dados válidos
- Login com credenciais corretas
- Token JWT válido
- Sem token (401)
- Token inválido (403)
- Token expirado
- Email duplicado (409)
- Senha fraca (400)
- Email inválido (400)
- Senha incorreta (401)

---

### 2. Testes de Denúncias (CRIME-001)

**7 testes** que validam criação, busca e filtros de denúncias.

```powershell
# PowerShell
.\backend\scripts\crime_tests.ps1

# Bash
bash backend/scripts/crime_tests.sh
```

**Testes incluídos:**
- Criar denúncia
- Buscar denúncias próximas (raio + 30 dias)
- Buscar denúncia por ID
- Tipo de crime inválido (400)
- Descrição > 500 chars (400)
- Sem autenticação (401)
- Cálculo de distância (Haversine)

---

### 3. Testes de Grupos (GROUP-001)

**7 testes** que validam criação, membership e busca de grupos.

```powershell
# PowerShell
.\backend\scripts\group_tests.ps1

# Bash
bash backend/scripts/group_tests.sh
```

**Testes incluídos:**
- Criar grupo (criador adicionado automaticamente)
- Buscar grupos
- Buscar com filtro (search)
- Entrar no grupo (join)
- Listar membros (com joined_at)
- Sair do grupo (leave)
- Nome duplicado (409)

---

### 4. Testes de Feed (FEED-001)

**7 testes** que validam posts, paginação e permissões.

```powershell
# PowerShell
.\backend\scripts\feed_tests.ps1

# Bash
bash backend/scripts/feed_tests.sh
```

**Testes incluídos:**
- Criar post no grupo
- Listar posts (paginado, ordem DESC)
- Feed geral do usuário
- Deletar post (somente autor)
- Não-membro tenta postar (403)
- Conteúdo > 1000 chars (400)
- Validação de membership

---

## ⏱️ Tempo de Resposta

Todos os testes medem o tempo de execução em **milissegundos** para cada operação.

### Benchmarks Esperados:

| Operação | Meta | Real Esperado |
|----------|------|---------------|
| Registro | < 2s | ~150ms |
| Login | < 2s | ~100ms |
| Criar denúncia | < 3s | ~120ms |
| Buscar nearby | < 3s | ~85ms |
| Criar grupo | < 1s | ~85ms |
| Join/Leave | < 1s | ~45ms |
| Criar post | < 2s | ~95ms |
| Listar posts | < 2s | ~75ms |

---

## 🔍 Logs de Inicialização

Quando o servidor inicia, você verá logs detalhados:

```
📦 Inicializando banco de dados...
📦 Criando tabelas do banco de dados...
  ✓ Tabela users criada
  ✓ Tabela crime_reports criada
  ✓ Tabela groups criada
  ✓ Tabela group_members criada
  ✓ Tabela posts criada
  ✓ Índices criados
✅ Todas as tabelas foram criadas com sucesso!

╔════════════════════════════════════════════╗
║                                            ║
║   🚀 CrimeTracker Backend Rodando!       ║
║                                            ║
╚════════════════════════════════════════════╝

🌐 Servidor: http://0.0.0.0:3000
📱 Android: http://10.0.2.2:3000
🔧 Ambiente: development

💾 Banco de Dados:
   Caminho: ./database/crimetracker.db
   👤 Usuários: 5
   🚨 Denúncias: 12
   👥 Grupos: 3
   📰 Posts: 24

📡 Endpoints disponíveis:
   GET  /health

   🔐 AUTH-001:
   POST /api/auth/register
   POST /api/auth/login
   GET  /api/auth/profile

   🚨 CRIME-001:
   POST /api/reports
   GET  /api/reports/nearby
   GET  /api/reports/:id

   👥 GROUP-001:
   POST /api/groups
   GET  /api/groups
   POST /api/groups/:id/join
   POST /api/groups/:id/leave

   📰 FEED-001:
   POST /api/groups/:group_id/posts
   GET  /api/groups/:group_id/posts
   DELETE /api/posts/:id
   GET  /api/feed

✅ Pronto para receber requisições!
```

---

## 📊 Estrutura dos Scripts de Teste

```
backend/scripts/
├── test_all.ps1          ✅ Teste integrado (PowerShell)
├── test_all.sh           ✅ Teste integrado (Bash)
├── auth_tests.ps1        ✅ Testes AUTH (PowerShell)
├── auth_tests.sh         ✅ Testes AUTH (Bash)
├── crime_tests.ps1       ✅ Testes CRIME (PowerShell)
├── crime_tests.sh        ✅ Testes CRIME (Bash)
├── group_tests.ps1       ✅ Testes GROUP (PowerShell)
├── group_tests.sh        ✅ Testes GROUP (Bash)
├── feed_tests.ps1        ✅ Testes FEED (PowerShell)
└── feed_tests.sh         ✅ Testes FEED (Bash)
```

---

## ✅ Checklist de Testes

### Antes de Executar
- [ ] Servidor está rodando (`npm run dev`)
- [ ] Porta 3000 está livre
- [ ] Mensagem "Pronto para receber requisições" apareceu

### Testes Básicos
- [ ] Teste integrado passa (test_all)
- [ ] Todos os 13 testes passam
- [ ] Tempo total < 2 segundos

### Testes por Módulo
- [ ] AUTH-001: 10/10 testes passam
- [ ] CRIME-001: 7/7 testes passam
- [ ] GROUP-001: 7/7 testes passam
- [ ] FEED-001: 7/7 testes passam

### Performance
- [ ] Nenhuma operação leva mais que 2s
- [ ] Tempo médio < 100ms
- [ ] Logs mostram tempos em milissegundos

---

## 🐛 Troubleshooting

### Erro: "Connection refused"
**Solução:** Servidor não está rodando. Execute `npm run dev`.

### Erro: "EADDRINUSE: address already in use"
**Solução:** Porta 3000 já está em uso. Mate o processo ou mude a porta em `config.js`.

### Testes falhando aleatoriamente
**Solução:** 
1. Verifique se o banco de dados não está corrompido
2. Delete `database/crimetracker.db` e reinicie o servidor
3. Aguarde alguns segundos entre execuções de teste

### PowerShell: "cannot be loaded because running scripts is disabled"
**Solução:**
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Bash: "Permission denied"
**Solução:**
```bash
chmod +x backend/scripts/*.sh
```

---

## 🎯 Casos de Uso

### 1. Desenvolvimento Contínuo
Execute o teste integrado após cada mudança:
```bash
npm run dev &
sleep 5
bash backend/scripts/test_all.sh
```

### 2. CI/CD Pipeline
Adicione ao seu pipeline:
```yaml
- name: Test Backend
  run: |
    cd backend
    npm run dev &
    sleep 10
    bash scripts/test_all.sh
```

### 3. Validação Pré-Deploy
Antes de fazer deploy, execute todos os testes:
```bash
# Teste integrado
bash backend/scripts/test_all.sh

# Testes individuais
bash backend/scripts/auth_tests.sh
bash backend/scripts/crime_tests.sh
bash backend/scripts/group_tests.sh
bash backend/scripts/feed_tests.sh
```

---

## 📈 Métricas de Sucesso

Para considerar o sistema pronto para produção:

✅ **100% dos testes passam**  
✅ **Tempo médio < 100ms**  
✅ **Nenhuma operação > 2s**  
✅ **Zero erros no console**  
✅ **Logs de inicialização completos**

---

## 🎉 Resumo

- **31 testes** automatizados no total
- **13 testes** no script integrado
- **8 scripts** diferentes (PowerShell + Bash)
- **Cobertura** de todos os 4 módulos
- **Performance** monitorada em milissegundos
- **Logs** detalhados de inicialização

**Sistema 100% testado e pronto para produção! 🚀**

