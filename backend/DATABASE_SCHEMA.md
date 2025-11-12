# 📊 Schema do Banco de Dados - CrimeTracker

Documentação completa do schema SQLite do CrimeTracker.

## 📋 Visão Geral

O banco de dados possui **5 tabelas principais** com relacionamentos bem definidos e índices para otimização de performance.

---

## 🗂️ Tabelas

### 1️⃣ users (Usuários)

Armazena informações dos usuários do sistema.

**Campos:**
- `id` (TEXT, PRIMARY KEY) - UUID único do usuário
- `email` (TEXT, UNIQUE, NOT NULL) - Email do usuário
- `password_hash` (TEXT, NOT NULL) - Senha criptografada com bcrypt
- `username` (TEXT, UNIQUE, NOT NULL) - Nome de usuário único
- `created_at` (DATETIME) - Data de criação (padrão: timestamp atual)

**Constraints:**
- `email` e `username` devem ser únicos
- Todos os campos são obrigatórios exceto timestamps

**SQL:**
```sql
CREATE TABLE users (
  id TEXT PRIMARY KEY,
  email TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  username TEXT UNIQUE NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**Exemplo:**
```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "email": "joao@example.com",
  "password_hash": "$2a$10$...",
  "username": "joao_silva",
  "created_at": "2025-11-12T16:30:00.000Z"
}
```

---

### 2️⃣ crime_reports (Denúncias de Crimes)

Armazena denúncias de crimes feitas pelos usuários.

**Campos:**
- `id` (TEXT, PRIMARY KEY) - UUID único da denúncia
- `user_id` (TEXT, NOT NULL, FK → users.id) - ID do usuário que fez a denúncia
- `tipo` (TEXT, NOT NULL) - Tipo de crime (roubo, furto, assalto, etc)
- `descricao` (TEXT, NOT NULL) - Descrição detalhada do crime
- `lat` (REAL, NOT NULL) - Latitude da localização
- `lon` (REAL, NOT NULL) - Longitude da localização
- `created_at` (DATETIME) - Data de criação
- `updated_at` (DATETIME) - Data da última atualização

**Constraints:**
- `user_id` é chave estrangeira para `users.id`
- Deleção em cascata: se usuário é deletado, suas denúncias também são

**Tipos de Crime Válidos:**
- roubo, furto, assalto, vandalismo, agressao, trafico, homicidio, sequestro, invasao, suspeita, perturbacao, outro

**SQL:**
```sql
CREATE TABLE crime_reports (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  tipo TEXT NOT NULL,
  descricao TEXT NOT NULL,
  lat REAL NOT NULL,
  lon REAL NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Índices:**
```sql
CREATE INDEX idx_crime_reports_user_id ON crime_reports(user_id);
CREATE INDEX idx_crime_reports_tipo ON crime_reports(tipo);
CREATE INDEX idx_crime_reports_location ON crime_reports(lat, lon);
```

**Exemplo:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "user_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "tipo": "roubo",
  "descricao": "Assalto à mão armada próximo ao mercado",
  "lat": -23.5505,
  "lon": -46.6333,
  "created_at": "2025-11-12T16:35:00.000Z",
  "updated_at": "2025-11-12T16:35:00.000Z"
}
```

---

### 3️⃣ groups (Grupos de Bairro)

Armazena informações sobre grupos de vigilância de bairro.

**Campos:**
- `id` (TEXT, PRIMARY KEY) - UUID único do grupo
- `nome` (TEXT, NOT NULL) - Nome do grupo
- `descricao` (TEXT) - Descrição do grupo (opcional)
- `criador` (TEXT, NOT NULL, FK → users.id) - ID do usuário criador
- `created_at` (DATETIME) - Data de criação

**Constraints:**
- `criador` é chave estrangeira para `users.id`
- Deleção em cascata: se usuário criador é deletado, o grupo é deletado

**SQL:**
```sql
CREATE TABLE groups (
  id TEXT PRIMARY KEY,
  nome TEXT NOT NULL,
  descricao TEXT,
  criador TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (criador) REFERENCES users(id) ON DELETE CASCADE
);
```

**Índices:**
```sql
CREATE INDEX idx_groups_criador ON groups(criador);
```

**Exemplo:**
```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "nome": "Vizinhos da Rua A",
  "descricao": "Grupo de moradores da Rua A para vigilância do bairro",
  "criador": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "created_at": "2025-11-12T16:40:00.000Z"
}
```

---

### 4️⃣ group_members (Membros dos Grupos)

Relaciona usuários com grupos (tabela de junção).

**Campos:**
- `group_id` (TEXT, NOT NULL, FK → groups.id, PRIMARY KEY composta)
- `user_id` (TEXT, NOT NULL, FK → users.id, PRIMARY KEY composta)
- `joined_at` (DATETIME) - Data em que entrou no grupo

**Constraints:**
- Chave primária composta: `(group_id, user_id)`
- Garante que um usuário não pode entrar no mesmo grupo duas vezes
- Deleção em cascata de ambas as FK

**SQL:**
```sql
CREATE TABLE group_members (
  group_id TEXT NOT NULL,
  user_id TEXT NOT NULL,
  joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (group_id, user_id),
  FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Exemplo:**
```json
{
  "group_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "user_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "joined_at": "2025-11-12T16:45:00.000Z"
}
```

---

### 5️⃣ posts (Posts do Feed)

Armazena posts/mensagens publicadas nos grupos ou feed geral.

**Campos:**
- `id` (TEXT, PRIMARY KEY) - UUID único do post
- `group_id` (TEXT, FK → groups.id) - ID do grupo (NULL para posts públicos)
- `author_id` (TEXT, NOT NULL, FK → users.id) - ID do autor
- `conteudo` (TEXT, NOT NULL) - Conteúdo/texto do post
- `created_at` (DATETIME) - Data de criação

**Constraints:**
- `group_id` é opcional (NULL = post público)
- `author_id` é obrigatório
- Deleção em cascata para ambas as FK

**SQL:**
```sql
CREATE TABLE posts (
  id TEXT PRIMARY KEY,
  group_id TEXT,
  author_id TEXT NOT NULL,
  conteudo TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
  FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Índices:**
```sql
CREATE INDEX idx_posts_group_id ON posts(group_id);
CREATE INDEX idx_posts_author_id ON posts(author_id);
```

**Exemplo (Post em Grupo):**
```json
{
  "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "group_id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "author_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "conteudo": "Atenção moradores! Houve um incidente na Rua A hoje pela manhã.",
  "created_at": "2025-11-12T16:50:00.000Z"
}
```

**Exemplo (Post Público):**
```json
{
  "id": "d4e5f6a7-b8c9-0123-def1-234567890123",
  "group_id": null,
  "author_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "conteudo": "Alerta geral: aumento de roubos na região central.",
  "created_at": "2025-11-12T16:55:00.000Z"
}
```

---

## 🔗 Diagrama de Relacionamentos

```
users (1) ────────┬─── (N) crime_reports
  │               │
  │ (1)           │
  │               │
  ├─── (N) groups │
  │               │
  │ (N)           │
  │               │
  ├─── (M:N) ─────┤
  │   group_members
  │               │
  │ (1)           │
  │               │
  └─── (N) posts ─┘
       │
       │ (N)
       │
    groups (1) ─── (N) posts
```

**Legenda:**
- (1) = Um
- (N) = Muitos
- (M:N) = Muitos para Muitos

---

## 📈 Índices para Performance

Índices criados automaticamente para otimizar consultas:

```sql
-- Índices em crime_reports
CREATE INDEX idx_crime_reports_user_id ON crime_reports(user_id);
CREATE INDEX idx_crime_reports_tipo ON crime_reports(tipo);
CREATE INDEX idx_crime_reports_location ON crime_reports(lat, lon);

-- Índices em groups
CREATE INDEX idx_groups_criador ON groups(criador);

-- Índices em posts
CREATE INDEX idx_posts_group_id ON posts(group_id);
CREATE INDEX idx_posts_author_id ON posts(author_id);
```

**Benefícios:**
- ✅ Buscas por `user_id` muito rápidas
- ✅ Filtros por `tipo` de crime otimizados
- ✅ Queries geográficas (lat, lon) eficientes
- ✅ Listagem de posts por grupo rápida

---

## 🔐 Integridade Referencial

Todas as tabelas usam **foreign keys com ON DELETE CASCADE**:

- Se um **user** é deletado:
  - ✅ Suas **crime_reports** são deletadas
  - ✅ Seus **groups** criados são deletados
  - ✅ Sua associação em **group_members** é removida
  - ✅ Seus **posts** são deletados

- Se um **group** é deletado:
  - ✅ Todos **group_members** são removidos
  - ✅ Todos **posts** do grupo são deletados

---

## 🧪 Queries Úteis

### Buscar todas as denúncias de um usuário
```sql
SELECT * FROM crime_reports 
WHERE user_id = 'uuid-do-usuario' 
ORDER BY created_at DESC;
```

### Buscar denúncias por tipo
```sql
SELECT * FROM crime_reports 
WHERE tipo = 'roubo' 
ORDER BY created_at DESC;
```

### Buscar membros de um grupo
```sql
SELECT u.id, u.username, u.email, gm.joined_at
FROM group_members gm
JOIN users u ON gm.user_id = u.id
WHERE gm.group_id = 'uuid-do-grupo'
ORDER BY gm.joined_at ASC;
```

### Buscar posts de um grupo
```sql
SELECT p.*, u.username as author_username
FROM posts p
JOIN users u ON p.author_id = u.id
WHERE p.group_id = 'uuid-do-grupo'
ORDER BY p.created_at DESC;
```

### Buscar denúncias próximas (requer cálculo de distância no código)
```sql
SELECT * FROM crime_reports 
WHERE lat BETWEEN ? AND ? 
  AND lon BETWEEN ? AND ?
ORDER BY created_at DESC;
```

---

## 📊 Estatísticas

Para obter estatísticas do banco:

```sql
-- Total de usuários
SELECT COUNT(*) as total_users FROM users;

-- Total de denúncias por tipo
SELECT tipo, COUNT(*) as total 
FROM crime_reports 
GROUP BY tipo 
ORDER BY total DESC;

-- Grupos mais populares
SELECT g.nome, COUNT(gm.user_id) as membros
FROM groups g
LEFT JOIN group_members gm ON g.id = gm.group_id
GROUP BY g.id
ORDER BY membros DESC;

-- Usuários mais ativos (mais denúncias)
SELECT u.username, COUNT(cr.id) as total_denuncias
FROM users u
LEFT JOIN crime_reports cr ON u.id = cr.user_id
GROUP BY u.id
ORDER BY total_denuncias DESC
LIMIT 10;
```

---

## ✅ Checklist de Implementação

- [x] Tabela `users` criada
- [x] Tabela `crime_reports` criada
- [x] Tabela `groups` criada
- [x] Tabela `group_members` criada
- [x] Tabela `posts` criada
- [x] Foreign keys configuradas
- [x] ON DELETE CASCADE configurado
- [x] Índices criados
- [x] UNIQUE constraints aplicados
- [x] Valores DEFAULT configurados
- [x] Criação automática na inicialização

---

**Schema implementado conforme especificações! 🎉**

Todos os campos, relacionamentos e constraints estão configurados corretamente.

