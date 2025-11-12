# 📁 Estrutura Completa do Projeto CrimeTracker

## 🌳 Árvore de Arquivos

```
CrimeTracker/
│
├── 📄 README.md                          # Documentação principal do projeto
├── 📄 QUICKSTART.md                      # Guia rápido de início
├── 📄 PROJECT_STRUCTURE.md               # Este arquivo
├── 📄 .gitignore                         # Arquivos ignorados pelo Git
│
├── 📁 backend/                           # Backend Node.js + Express
│   │
│   ├── 📄 package.json                   # Dependências Node.js
│   ├── 📄 server.js                      # Servidor Express principal
│   ├── 📄 README.md                      # Documentação do backend
│   ├── 📄 .gitignore                     # Ignores específicos do backend
│   │
│   ├── 📁 database/                      # Banco de dados SQLite
│   │   ├── 📄 db.js                      # Configuração e schemas
│   │   └── 📄 crimetracker.db            # Banco SQLite (gerado em runtime)
│   │
│   ├── 📁 routes/                        # Rotas da API REST
│   │   ├── 📄 auth.js                    # Autenticação (register, login, profile)
│   │   ├── 📄 reports.js                 # Denúncias (CRUD)
│   │   ├── 📄 groups.js                  # Grupos de bairro (CRUD)
│   │   └── 📄 feed.js                    # Feed social (posts, comments)
│   │
│   └── 📁 uploads/                       # Arquivos enviados (gerado em runtime)
│       └── (imagens e arquivos do usuário)
│
└── 📁 android/                           # App Android nativo
    │
    ├── 📄 build.gradle.kts               # Configuração Gradle do projeto
    ├── 📄 settings.gradle.kts            # Settings do Gradle
    ├── 📄 gradle.properties              # Propriedades do Gradle
    ├── 📄 README.md                      # Documentação do Android
    ├── 📄 .gitignore                     # Ignores específicos do Android
    │
    └── 📁 app/                           # Módulo principal do app
        │
        ├── 📄 build.gradle.kts           # Configuração do módulo
        ├── 📄 proguard-rules.pro         # Regras ProGuard
        │
        └── 📁 src/main/
            │
            ├── 📄 AndroidManifest.xml    # Manifesto do app
            │
            ├── 📁 kotlin/com/crimetracker/app/
            │   │
            │   ├── 📄 CrimeTrackerApplication.kt    # Application class
            │   ├── 📄 MainActivity.kt               # Activity principal
            │   │
            │   ├── 📁 data/                         # Camada de dados
            │   │   │
            │   │   ├── 📁 model/                    # Modelos de dados
            │   │   │   └── 📄 Models.kt             # Data classes (User, Report, Group, Post, etc)
            │   │   │
            │   │   ├── 📁 network/                  # Networking
            │   │   │   └── 📄 ApiService.kt         # Interface Retrofit
            │   │   │
            │   │   ├── 📁 repository/               # Repositories (a implementar)
            │   │   │   ├── 📄 AuthRepository.kt
            │   │   │   ├── 📄 ReportsRepository.kt
            │   │   │   ├── 📄 GroupsRepository.kt
            │   │   │   └── 📄 FeedRepository.kt
            │   │   │
            │   │   └── 📁 local/                    # Banco local (a implementar)
            │   │       ├── 📄 AppDatabase.kt
            │   │       └── 📄 UserPreferences.kt
            │   │
            │   ├── 📁 di/                           # Injeção de Dependência
            │   │   └── 📄 NetworkModule.kt          # Módulo Hilt para rede
            │   │
            │   ├── 📁 domain/                       # Lógica de negócio (a implementar)
            │   │   ├── 📁 usecase/
            │   │   └── 📁 model/
            │   │
            │   └── 📁 ui/                           # Interface do Usuário
            │       │
            │       ├── 📁 navigation/               # Navegação
            │       │   └── 📄 CrimeTrackerNavHost.kt
            │       │
            │       ├── 📁 theme/                    # Tema Material
            │       │   ├── 📄 Theme.kt
            │       │   └── 📄 Type.kt
            │       │
            │       ├── 📁 components/               # Componentes reutilizáveis (a implementar)
            │       │   ├── 📄 LoadingIndicator.kt
            │       │   ├── 📄 ErrorMessage.kt
            │       │   └── 📄 CustomButton.kt
            │       │
            │       └── 📁 screens/                  # Telas
            │           │
            │           ├── 📁 auth/                 # Autenticação
            │           │   ├── 📄 LoginScreen.kt
            │           │   ├── 📄 RegisterScreen.kt (a implementar)
            │           │   └── 📄 AuthViewModel.kt (a implementar)
            │           │
            │           ├── 📁 home/                 # Home/Dashboard
            │           │   ├── 📄 HomeScreen.kt
            │           │   └── 📄 HomeViewModel.kt (a implementar)
            │           │
            │           ├── 📁 reports/              # Denúncias (a implementar)
            │           │   ├── 📄 ReportsListScreen.kt
            │           │   ├── 📄 ReportDetailScreen.kt
            │           │   ├── 📄 CreateReportScreen.kt
            │           │   └── 📄 ReportsViewModel.kt
            │           │
            │           ├── 📁 groups/               # Grupos (a implementar)
            │           │   ├── 📄 GroupsListScreen.kt
            │           │   ├── 📄 GroupDetailScreen.kt
            │           │   ├── 📄 CreateGroupScreen.kt
            │           │   └── 📄 GroupsViewModel.kt
            │           │
            │           ├── 📁 feed/                 # Feed Social (a implementar)
            │           │   ├── 📄 FeedScreen.kt
            │           │   ├── 📄 PostDetailScreen.kt
            │           │   ├── 📄 CreatePostScreen.kt
            │           │   └── 📄 FeedViewModel.kt
            │           │
            │           ├── 📁 profile/              # Perfil (a implementar)
            │           │   ├── 📄 ProfileScreen.kt
            │           │   ├── 📄 EditProfileScreen.kt
            │           │   └── 📄 ProfileViewModel.kt
            │           │
            │           └── 📁 map/                  # Mapa (a implementar)
            │               ├── 📄 MapScreen.kt
            │               └── 📄 MapViewModel.kt
            │
            └── 📁 res/                              # Recursos Android
                │
                ├── 📁 values/                       # Valores
                │   ├── 📄 strings.xml               # Strings do app
                │   ├── 📄 colors.xml                # Cores
                │   └── 📄 themes.xml                # Temas
                │
                ├── 📁 xml/                          # XMLs de configuração
                │   ├── 📄 data_extraction_rules.xml
                │   └── 📄 backup_rules.xml
                │
                ├── 📁 mipmap/                       # Ícones do app (a adicionar)
                │   └── ic_launcher.png
                │
                └── 📁 drawable/                     # Recursos gráficos (a adicionar)
                    └── (ícones e imagens)
```

## 📊 Estatísticas do Projeto

### Backend
- **Linhas de código:** ~1.500
- **Arquivos:** 7
- **Endpoints:** 22
- **Tabelas no banco:** 6

### Android
- **Linhas de código:** ~800 (MVP)
- **Arquivos:** 15
- **Telas implementadas:** 2
- **Dependências:** 20+

## 🎯 Status de Implementação

### ✅ Implementado (MVP)
- Backend completo com API REST
- Banco de dados SQLite com schemas
- Autenticação JWT
- CRUD de denúncias, grupos e feed
- Estrutura Android com Jetpack Compose
- Telas de Login e Home (mockup)
- Configuração de rede com Retrofit
- Modelos de dados completos
- Injeção de dependência com Hilt

### 🚧 Em Desenvolvimento
- Implementação completa das telas
- ViewModels para cada feature
- Repositories
- Integração com Google Maps
- Upload de imagens
- Permissões de localização e câmera

### 📋 Planejado
- Cache local com Room
- Notificações push locais
- Testes unitários e de integração
- Sistema de filtros avançados
- Chat de grupo em tempo real

## 🔍 Arquivos-Chave

### Backend
| Arquivo | Descrição | Importância |
|---------|-----------|-------------|
| `server.js` | Servidor Express principal | ⭐⭐⭐⭐⭐ |
| `database/db.js` | Schema e configuração do banco | ⭐⭐⭐⭐⭐ |
| `routes/auth.js` | Autenticação e JWT | ⭐⭐⭐⭐⭐ |
| `routes/reports.js` | CRUD de denúncias | ⭐⭐⭐⭐ |
| `routes/groups.js` | CRUD de grupos | ⭐⭐⭐⭐ |
| `routes/feed.js` | CRUD de posts e comentários | ⭐⭐⭐⭐ |

### Android
| Arquivo | Descrição | Importância |
|---------|-----------|-------------|
| `MainActivity.kt` | Activity principal | ⭐⭐⭐⭐⭐ |
| `CrimeTrackerApplication.kt` | Application class | ⭐⭐⭐⭐⭐ |
| `data/network/ApiService.kt` | Interface Retrofit | ⭐⭐⭐⭐⭐ |
| `data/model/Models.kt` | Modelos de dados | ⭐⭐⭐⭐⭐ |
| `di/NetworkModule.kt` | Injeção de dependência | ⭐⭐⭐⭐ |
| `ui/navigation/CrimeTrackerNavHost.kt` | Navegação | ⭐⭐⭐⭐ |
| `ui/screens/auth/LoginScreen.kt` | Tela de login | ⭐⭐⭐ |
| `ui/screens/home/HomeScreen.kt` | Tela home | ⭐⭐⭐ |

## 🏗️ Arquitetura

### Backend
```
Arquitetura em Camadas
├── Rotas (Routes)
├── Validação (Middleware)
├── Lógica de Negócio (Controllers inline)
└── Banco de Dados (SQLite)
```

### Android
```
Clean Architecture + MVVM
├── UI (Jetpack Compose)
├── ViewModel (StateFlow)
├── Use Cases (Domain)
├── Repository (Data)
└── Data Sources (Network + Local)
```

## 📦 Dependências Principais

### Backend
- `express` - Framework web
- `better-sqlite3` - Banco de dados
- `bcrypt` - Hash de senhas
- `jsonwebtoken` - Autenticação
- `express-validator` - Validação
- `cors` - CORS
- `multer` - Upload de arquivos

### Android
- `compose-bom` - Jetpack Compose
- `hilt` - Injeção de dependência
- `retrofit` - HTTP client
- `room` - Banco local
- `maps-compose` - Google Maps
- `coil` - Carregamento de imagens
- `navigation-compose` - Navegação

## 🔗 Fluxo de Dados

### Criação de Denúncia
```
Android UI → ViewModel → Repository → Retrofit → Backend API
                                                      ↓
                                                 SQLite DB
                                                      ↓
                                            Response com ID
                                                      ↓
Android UI ← ViewModel ← Repository ← Retrofit ← Backend
```

### Autenticação
```
Login Screen → AuthViewModel → AuthRepository → API /auth/login
                                                        ↓
                                                  Valida credenciais
                                                        ↓
                                                  Gera JWT token
                                                        ↓
App recebe token → Salva no DataStore → Usa em todas requisições
```

## 🗄️ Schema do Banco de Dados

### Tabelas
1. **users** - Usuários
2. **reports** - Denúncias
3. **groups** - Grupos de bairro
4. **group_members** - Membros dos grupos
5. **feed_posts** - Posts do feed
6. **comments** - Comentários

### Relacionamentos
```
users 1:N reports
users 1:N feed_posts
users N:M groups (através de group_members)
feed_posts 1:N comments
groups 1:N feed_posts
```

## 📱 Telas do App

### Autenticação
1. Login
2. Registro

### Principal
3. Home/Dashboard
4. Feed Social
5. Lista de Denúncias
6. Detalhes de Denúncia
7. Criar Denúncia
8. Lista de Grupos
9. Detalhes de Grupo
10. Criar Grupo
11. Perfil do Usuário
12. Mapa de Denúncias

## 🎨 Design System

### Cores Principais
- Primary: `#6200EE` (Roxo)
- Secondary: `#03DAC5` (Teal)
- Tertiary: `#3700B3` (Roxo escuro)

### Tipografia
- Font: System Default
- Tamanhos: 11sp, 16sp, 22sp

### Componentes
- Material Design 3
- Bottom Navigation
- Floating Action Button
- Cards
- Text Fields
- Buttons

---

Este documento será atualizado conforme o projeto evolui.

