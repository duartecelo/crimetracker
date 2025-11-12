# CrimeTracker

Sistema local de denúncias de crimes, formação de grupos de bairro e compartilhamento de informações entre usuários.

## 📋 Visão Geral

CrimeTracker é um aplicativo Android nativo que permite aos moradores de uma comunidade reportar crimes, formar grupos de vigilância de bairro e compartilhar informações importantes sobre segurança. Todo o sistema funciona em infraestrutura 100% local — sem dependência de nuvem.

## 🎯 Objetivo

Criar uma plataforma local e segura para:
- Registrar e acompanhar denúncias de crimes com geolocalização
- Formar e gerenciar grupos de vizinhança
- Compartilhar informações e atualizações através de um feed social
- Fortalecer a segurança comunitária através da colaboração

## 🏗️ Arquitetura

### Frontend
- **Plataforma:** Android (API 24+)
- **Linguagem:** Kotlin
- **UI Framework:** Jetpack Compose
- **Arquitetura:** MVVM + Clean Architecture
- **Injeção de Dependência:** Hilt/Dagger
- **Navegação:** Navigation Compose
- **Networking:** Retrofit + OkHttp
- **Mapas:** Google Maps SDK
- **Persistência Local:** Room + DataStore

### Backend
- **Runtime:** Node.js
- **Framework:** Express.js
- **Banco de Dados:** SQLite
- **Autenticação:** JWT (JSON Web Tokens)
- **Validação:** express-validator
- **Upload de Arquivos:** Multer

### Infraestrutura
- **Servidor Local:** http://localhost:3000
- **Acesso Android Emulator:** http://10.0.2.2:3000
- **Acesso Dispositivo Físico:** http://[IP_LOCAL]:3000
- **Sem dependências de nuvem**

## 📁 Estrutura do Projeto

```
CrimeTracker/
├── backend/                    # Backend Node.js + Express
│   ├── database/              # SQLite database e schemas
│   │   └── db.js              # Configuração e inicialização
│   ├── routes/                # Endpoints da API REST
│   │   ├── auth.js            # Autenticação e perfil
│   │   ├── reports.js         # Denúncias
│   │   ├── groups.js          # Grupos de bairro
│   │   └── feed.js            # Posts e comentários
│   ├── uploads/               # Imagens e arquivos
│   ├── server.js              # Servidor principal
│   ├── package.json           # Dependências
│   └── README.md              # Documentação do backend
│
├── android/                   # App Android nativo
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── kotlin/com/crimetracker/app/
│   │   │   │   ├── data/      # Camada de dados
│   │   │   │   │   ├── model/ # Data classes
│   │   │   │   │   ├── network/ # API service
│   │   │   │   │   └── repository/ # Repositories
│   │   │   │   ├── di/        # Injeção de dependência
│   │   │   │   ├── domain/    # Use cases
│   │   │   │   └── ui/        # Interface do usuário
│   │   │   │       ├── screens/ # Telas principais
│   │   │   │       ├── components/ # Componentes reutilizáveis
│   │   │   │       ├── navigation/ # Navegação
│   │   │   │       └── theme/  # Tema Material
│   │   │   ├── res/           # Recursos Android
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts   # Configuração do app
│   ├── build.gradle.kts       # Configuração do projeto
│   └── README.md              # Documentação do Android
│
└── README.md                  # Este arquivo
```

## ✅ Funcionalidades no Escopo

### Autenticação e Perfil
- [x] Registro de novos usuários
- [x] Login com username/email e senha
- [x] Autenticação JWT
- [x] Perfil de usuário com informações de localização
- [ ] Logout
- [ ] Recuperação de senha
- [ ] Edição de perfil

### Denúncias (Reports)
- [x] Criação de denúncias com título, descrição e categoria
- [x] Geolocalização automática ou manual
- [x] Upload de fotos das denúncias
- [x] Listagem de denúncias com filtros (categoria, status)
- [x] Visualização detalhada de denúncia
- [x] Atualização de status (pendente, em andamento, resolvido)
- [ ] Visualização em mapa
- [ ] Notificações de denúncias próximas
- [ ] Comentários em denúncias

### Grupos de Bairro
- [x] Criação de grupos com nome, descrição e área geográfica
- [x] Listagem de grupos disponíveis
- [x] Entrar/sair de grupos
- [x] Visualização de membros do grupo
- [x] Diferentes níveis de permissão (admin, membro)
- [ ] Convites para grupos
- [ ] Grupos privados vs públicos
- [ ] Chat de grupo em tempo real

### Feed Social
- [x] Criação de posts públicos ou em grupos
- [x] Listagem de feed personalizado
- [x] Comentários em posts
- [x] Upload de imagens em posts
- [ ] Reações/likes em posts
- [ ] Compartilhamento de denúncias no feed
- [ ] Menções de usuários

### Mapas e Localização
- [ ] Visualização de denúncias em mapa
- [ ] Filtros geográficos por raio
- [ ] Heatmap de crimes
- [ ] Navegação até o local da denúncia
- [ ] Áreas de cobertura dos grupos

### Notificações
- [ ] Notificações push locais
- [ ] Alertas de novas denúncias na área
- [ ] Atualizações de posts em grupos
- [ ] Mensagens de administradores

## ❌ Fora do Escopo

### Infraestrutura
- ❌ Hospedagem em nuvem (AWS, Google Cloud, Azure)
- ❌ Servidores remotos ou APIs externas
- ❌ CDN para distribuição de conteúdo
- ❌ Serviços de terceiros pagos

### Recursos Avançados
- ❌ Integração com autoridades policiais
- ❌ Sistema de denúncias anônimas verificadas
- ❌ Inteligência artificial para análise de crimes
- ❌ Predição de crimes baseada em dados históricos
- ❌ Integração com câmeras de segurança
- ❌ Sistema de recompensas ou gamificação
- ❌ Transmissão de vídeo ao vivo
- ❌ Chamadas de voz/vídeo

### Plataformas
- ❌ Versão iOS
- ❌ Aplicativo Web
- ❌ Progressive Web App (PWA)
- ❌ Desktop (Windows/Mac/Linux)

### Recursos Sociais Avançados
- ❌ Stories temporários
- ❌ Mensagens privadas entre usuários
- ❌ Sistema de amizades
- ❌ Perfis verificados
- ❌ Sistema de reputação/pontuação

## ⚡ Metas de Desempenho

### Tempos de Resposta
| Operação | Meta | Aceitável | Crítico |
|----------|------|-----------|---------|
| Login | < 2s | < 3s | < 5s |
| Registro | < 2s | < 3s | < 5s |
| Criação de denúncia | < 3s | < 5s | < 8s |
| Listagem de feed | < 2s | < 3s | < 5s |
| Listagem de denúncias | < 2s | < 3s | < 5s |
| Listagem de grupos | < 1.5s | < 2.5s | < 4s |
| Upload de imagem | < 5s | < 8s | < 12s |
| Carregamento de mapa | < 3s | < 5s | < 8s |

### Capacidade
- **Usuários simultâneos:** 50-100 usuários
- **Denúncias totais:** 10.000+ registros
- **Posts no feed:** 5.000+ posts
- **Grupos:** 100+ grupos ativos
- **Tamanho de imagens:** Max 5MB por imagem
- **Banco de dados:** Max 1GB

### Recursos do Sistema
- **RAM do servidor:** Min 512MB, Recomendado 1GB
- **CPU:** 1-2 cores suficientes
- **Armazenamento:** Min 5GB disponíveis
- **Rede:** Min 10Mbps para uso local

### App Android
- **Tamanho do APK:** < 50MB
- **Uso de RAM:** < 200MB em uso normal
- **Consumo de bateria:** < 5% por hora de uso ativo
- **Tempo de inicialização:** < 3s
- **Suporte offline:** Cache de dados recentes

## 🚀 Instalação e Configuração

### Backend

```bash
cd backend
npm install
npm run dev
```

O servidor estará disponível em `http://localhost:3000`

### Android

1. Abra o projeto Android no Android Studio
2. Configure a Google Maps API Key no `AndroidManifest.xml`
3. Sincronize o Gradle
4. Execute no emulador ou dispositivo físico

**Importante:** Para dispositivos físicos, altere o `BASE_URL` em `app/build.gradle.kts` para o IP local da máquina que está rodando o backend.

## 📊 Modelo de Dados

### Principais Entidades

- **users:** Usuários do sistema
- **reports:** Denúncias de crimes
- **groups:** Grupos de bairro
- **group_members:** Relacionamento usuário-grupo
- **feed_posts:** Publicações no feed
- **comments:** Comentários em posts

Para detalhes completos do schema, consulte `backend/database/db.js`

## 🔒 Segurança

- Senhas armazenadas com bcrypt (10 rounds)
- Autenticação via JWT com expiração de 7 dias
- Validação de entrada em todas as rotas
- Foreign keys habilitadas no SQLite
- Headers CORS configurados
- SQL preparado (proteção contra SQL injection)

## 📝 API REST

### Endpoints Principais

**Autenticação:**
- `POST /api/auth/register` - Registrar usuário
- `POST /api/auth/login` - Login
- `GET /api/auth/profile` - Obter perfil (autenticado)

**Denúncias:**
- `POST /api/reports` - Criar denúncia
- `GET /api/reports` - Listar denúncias
- `GET /api/reports/:id` - Obter denúncia
- `PATCH /api/reports/:id/status` - Atualizar status

**Grupos:**
- `POST /api/groups` - Criar grupo
- `GET /api/groups` - Listar grupos
- `GET /api/groups/:id` - Obter grupo
- `POST /api/groups/:id/join` - Entrar no grupo
- `POST /api/groups/:id/leave` - Sair do grupo

**Feed:**
- `POST /api/feed` - Criar post
- `GET /api/feed` - Listar posts
- `GET /api/feed/:id` - Obter post
- `POST /api/feed/:id/comments` - Adicionar comentário

Para documentação completa, consulte `backend/README.md`

## 🧪 Testes

```bash
# Backend
cd backend
npm test

# Android
cd android
./gradlew test
./gradlew connectedAndroidTest
```

## 🤝 Contribuindo

Este é um projeto local e comunitário. Para contribuir:

1. Faça um fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## 📄 Licença

MIT License - veja o arquivo LICENSE para detalhes.

## 📞 Suporte

Para problemas ou dúvidas:
1. Verifique a documentação em `backend/README.md` e `android/README.md`
2. Consulte os logs do servidor e do app
3. Abra uma issue no repositório

## 🗺️ Roadmap

### Fase 1 (MVP) - ✅ Concluído
- [x] Estrutura do projeto
- [x] Backend com API REST completa
- [x] Modelos de dados e banco SQLite
- [x] Estrutura Android com Jetpack Compose
- [x] Autenticação JWT
- [x] CRUD de denúncias, grupos e feed

### Fase 2 - Em Desenvolvimento
- [ ] Implementação completa das telas Android
- [ ] Integração com Google Maps
- [ ] Upload de imagens
- [ ] Permissões de localização e câmera
- [ ] Cache local com Room

### Fase 3 - Planejado
- [ ] Notificações push locais
- [ ] Visualização de denúncias em mapa
- [ ] Heatmap de crimes
- [ ] Sistema de filtros avançados
- [ ] Testes automatizados

### Fase 4 - Futuro
- [ ] Chat de grupo em tempo real
- [ ] Sistema de convites
- [ ] Grupos privados
- [ ] Reações e likes
- [ ] Sistema de reputação

## 📚 Tecnologias e Bibliotecas

### Backend
- Express.js 4.18+
- better-sqlite3 9.2+
- bcrypt 5.1+
- jsonwebtoken 9.0+
- express-validator 7.0+
- multer 1.4+
- cors 2.8+

### Android
- Kotlin 1.9+
- Jetpack Compose BOM 2023.10
- Hilt 2.48
- Retrofit 2.9
- Room 2.6
- Google Maps SDK 18.2
- Coil 2.5
- Navigation Compose 2.7

## 👥 Equipe

Projeto desenvolvido como sistema comunitário de segurança local.

---

**CrimeTracker** - Fortalecendo comunidades através da informação e colaboração local 🏘️🛡️

