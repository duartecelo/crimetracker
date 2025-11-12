# 🧪 Testar Servidor Backend

Guia completo para testar o servidor CrimeTracker.

## 🚀 Iniciar Servidor

```bash
cd backend
npm run dev
```

Você deverá ver:

```
╔════════════════════════════════════════════╗
║                                            ║
║   🚀 CrimeTracker Backend Rodando!       ║
║                                            ║
╚════════════════════════════════════════════╝

🌐 Servidor: http://0.0.0.0:3000
📱 Android: http://10.0.2.2:3000
🔧 Ambiente: development
💾 Banco: ./database/crimetracker.db

📡 Endpoints disponíveis:
   GET  /health
   POST /api/auth/register
   POST /api/auth/login
   ...

✅ Pronto para receber requisições!
```

## ✅ Teste 1: Health Check

```bash
curl http://localhost:3000/health
```

**Resposta esperada:**
```json
{
  "success": true,
  "data": {
    "status": "online",
    "timestamp": "2025-11-12T16:30:00.000Z",
    "environment": "development",
    "database": "connected"
  },
  "message": "Servidor rodando"
}
```

## ✅ Teste 2: Registrar Usuário

```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"joao\",
    \"email\": \"joao@example.com\",
    \"password\": \"senha123\",
    \"full_name\": \"João Silva\"
  }"
```

**Resposta esperada:**
```json
{
  "success": true,
  "message": "Usuário registrado com sucesso",
  "data": {
    "token": "eyJhbGc iOiJIUzI1NiIsInR5cCI6Ikp...",
    "user": {
      "id": 1,
      "username": "joao",
      "email": "joao@example.com",
      "full_name": "João Silva"
    }
  }
}
```

**Copie o token!** Você precisará dele nos próximos testes.

## ✅ Teste 3: Login

```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"joao\",
    \"password\": \"senha123\"
  }"
```

## ✅ Teste 4: Obter Perfil (Autenticado)

```bash
curl http://localhost:3000/api/auth/profile \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

## ✅ Teste 5: Criar Denúncia

```bash
curl -X POST http://localhost:3000/api/reports \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d "{
    \"title\": \"Roubo na Rua A\",
    \"description\": \"Assalto à mão armada próximo ao mercado\",
    \"category\": \"roubo\",
    \"latitude\": -23.5505,
    \"longitude\": -46.6333,
    \"address\": \"Rua A, 123 - Centro\"
  }"
```

## ✅ Teste 6: Listar Denúncias

```bash
curl http://localhost:3000/api/reports \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

## ✅ Teste 7: Criar Grupo

```bash
curl -X POST http://localhost:3000/api/groups \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d "{
    \"name\": \"Vizinhos da Rua A\",
    \"description\": \"Grupo de moradores da Rua A para vigilância do bairro\",
    \"latitude\": -23.5505,
    \"longitude\": -46.6333,
    \"radius_meters\": 500
  }"
```

## ✅ Teste 8: Listar Grupos

```bash
curl http://localhost:3000/api/groups \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

## ✅ Teste 9: Criar Post no Feed

```bash
curl -X POST http://localhost:3000/api/feed \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d "{
    \"content\": \"Atenção moradores! Houve um incidente na Rua A hoje pela manhã. Todos fiquem atentos!\"
  }"
```

## ✅ Teste 10: Listar Feed

```bash
curl http://localhost:3000/api/feed \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

## 🧪 Teste com Postman/Insomnia

### Configurar Collection

1. **Base URL:** `http://localhost:3000`

2. **Headers Globais:**
   - `Content-Type: application/json`
   - `Authorization: Bearer {{token}}`

3. **Variáveis:**
   - `token`: (cole o token após login/registro)

### Endpoints para testar

| Método | Endpoint | Auth | Body |
|--------|----------|------|------|
| GET | `/health` | Não | - |
| POST | `/api/auth/register` | Não | username, email, password, full_name |
| POST | `/api/auth/login` | Não | username, password |
| GET | `/api/auth/profile` | Sim | - |
| POST | `/api/reports` | Sim | title, description, category, latitude, longitude |
| GET | `/api/reports` | Sim | - |
| GET | `/api/reports/:id` | Sim | - |
| POST | `/api/groups` | Sim | name, description |
| GET | `/api/groups` | Sim | - |
| GET | `/api/groups/:id` | Sim | - |
| POST | `/api/groups/:id/join` | Sim | - |
| POST | `/api/feed` | Sim | content |
| GET | `/api/feed` | Sim | - |

## ✅ Testes de Erro

### Teste de validação
```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\": \"ab\"}"
```

Deve retornar erro 400 com mensagem de validação.

### Teste de autenticação
```bash
curl http://localhost:3000/api/auth/profile
```

Deve retornar erro 401 (não autorizado).

### Teste de rota inexistente
```bash
curl http://localhost:3000/rota-inexistente
```

Deve retornar erro 404.

## 📊 Resultados Esperados

Todos os testes devem retornar:
- ✅ Status code apropriado (200, 201, 400, 401, 404, 500)
- ✅ Formato JSON válido
- ✅ Estrutura `{ success, data/error, message }`
- ✅ Token JWT válido após login/registro
- ✅ Dados salvos no banco SQLite

## 🔍 Verificar Banco de Dados

```bash
# Instalar DB Browser for SQLite
# Abrir: backend/database/crimetracker.db

# Ou usar CLI do SQLite
sqlite3 backend/database/crimetracker.db

# Ver tabelas
.tables

# Ver usuários
SELECT * FROM users;

# Ver denúncias
SELECT * FROM reports;
```

## 🎯 Métricas de Performance

Verifique se os tempos estão dentro das metas:

| Operação | Meta | Aceitável |
|----------|------|-----------|
| /health | < 50ms | < 100ms |
| /auth/register | < 2s | < 3s |
| /auth/login | < 2s | < 3s |
| /reports (POST) | < 3s | < 5s |
| /reports (GET) | < 2s | < 3s |
| /feed (GET) | < 2s | < 3s |

## ✅ Checklist Final

- [ ] Servidor inicia sem erros
- [ ] Health check retorna 200
- [ ] Registro cria usuário e retorna token
- [ ] Login valida credenciais
- [ ] Token JWT funciona em rotas protegidas
- [ ] CRUD de denúncias funciona
- [ ] CRUD de grupos funciona
- [ ] Feed funciona
- [ ] Validações retornam erros apropriados
- [ ] Banco de dados persiste dados

---

**Se todos os testes passarem, seu backend está funcionando perfeitamente! 🎉**

Próximo passo: [Testar o app Android](../android/README.md)

