# CrimeTracker Backend

Backend local para o aplicativo CrimeTracker usando Node.js, Express e SQLite.

## Instalação

```bash
npm install
```

## Inicialização

```bash
# Modo desenvolvimento (com auto-reload)
npm run dev

# Modo produção
npm start
```

O servidor estará disponível em:
- Local: `http://localhost:3000`
- Android Emulator: `http://10.0.2.2:3000`

## Estrutura

```
backend/
├── database/
│   ├── db.js              # Configuração do banco de dados
│   └── crimetracker.db    # Banco SQLite (gerado automaticamente)
├── routes/
│   ├── auth.js            # Rotas de autenticação
│   ├── reports.js         # Rotas de denúncias
│   ├── groups.js          # Rotas de grupos
│   └── feed.js            # Rotas do feed
├── uploads/               # Arquivos enviados (imagens)
├── server.js              # Servidor principal
└── package.json           # Dependências
```

## API Endpoints

### Autenticação
- `POST /api/auth/register` - Registrar novo usuário
- `POST /api/auth/login` - Login
- `GET /api/auth/profile` - Obter perfil (autenticado)

### Denúncias
- `POST /api/reports` - Criar denúncia (autenticado)
- `GET /api/reports` - Listar denúncias (autenticado)
- `GET /api/reports/:id` - Obter denúncia por ID (autenticado)
- `PATCH /api/reports/:id/status` - Atualizar status (autenticado)

### Grupos
- `POST /api/groups` - Criar grupo (autenticado)
- `GET /api/groups` - Listar grupos (autenticado)
- `GET /api/groups/:id` - Obter grupo por ID (autenticado)
- `POST /api/groups/:id/join` - Entrar em grupo (autenticado)
- `POST /api/groups/:id/leave` - Sair de grupo (autenticado)

### Feed
- `POST /api/feed` - Criar post (autenticado)
- `GET /api/feed` - Listar posts (autenticado)
- `GET /api/feed/:id` - Obter post por ID (autenticado)
- `POST /api/feed/:id/comments` - Adicionar comentário (autenticado)

## Health Check

```bash
curl http://localhost:3000/health
```

