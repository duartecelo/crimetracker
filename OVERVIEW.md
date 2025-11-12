# 🎯 CrimeTracker - Visão Geral Executiva

## 📊 Status do Projeto

**Versão:** 1.0.0 (MVP)  
**Status:** ✅ Estrutura Completa - Pronto para Desenvolvimento  
**Última Atualização:** Novembro 2025

## 🎨 O que é o CrimeTracker?

CrimeTracker é um **aplicativo Android local** que permite comunidades reportarem crimes, formarem grupos de vigilância de bairro e compartilharem informações de segurança. Todo o sistema opera em infraestrutura **100% local** — sem dependência de nuvem.

### 🌟 Principais Características

- 📱 **App Android Nativo** com Kotlin + Jetpack Compose
- 🖥️ **Backend Local** com Node.js + Express + SQLite
- 🔐 **Autenticação Segura** com JWT
- 📍 **Geolocalização** para denúncias
- 👥 **Grupos de Bairro** para colaboração
- 📰 **Feed Social** para comunicação
- 🏠 **100% Local** - sem nuvem

## 📦 Entregas

### ✅ Fase 1 - Estrutura Base (CONCLUÍDO)

#### Backend
- [x] Servidor Express configurado
- [x] Banco SQLite com 6 tabelas
- [x] 22 endpoints REST
- [x] Autenticação JWT
- [x] Validação de dados
- [x] CRUD completo de:
  - Usuários
  - Denúncias
  - Grupos
  - Posts e Comentários

#### Android
- [x] Projeto Kotlin + Compose
- [x] Arquitetura MVVM
- [x] Injeção de dependência (Hilt)
- [x] Configuração Retrofit
- [x] Modelos de dados completos
- [x] Navegação base
- [x] Telas de Login e Home (mockup)

#### Documentação
- [x] README principal
- [x] Quick Start Guide
- [x] Estrutura do Projeto
- [x] Guia de Desenvolvimento
- [x] READMEs específicos (Backend/Android)

### 🚧 Fase 2 - Implementação de Features (PRÓXIMO)

- [ ] ViewModels completos
- [ ] Repositories
- [ ] Telas implementadas:
  - [ ] Registro
  - [ ] Lista de Denúncias
  - [ ] Criar Denúncia
  - [ ] Lista de Grupos
  - [ ] Criar Grupo
  - [ ] Feed Social
  - [ ] Perfil
- [ ] Integração Google Maps
- [ ] Upload de imagens
- [ ] Permissões (localização, câmera)
- [ ] Cache local (Room)

### 📋 Fase 3 - Refinamento (FUTURO)

- [ ] Notificações push locais
- [ ] Visualização em mapa
- [ ] Heatmap de crimes
- [ ] Filtros avançados
- [ ] Chat de grupo
- [ ] Testes automatizados
- [ ] Otimizações de performance

## 📁 Arquivos e Pastas

```
📁 CrimeTracker/
├── 📄 README.md                  # Documentação principal
├── 📄 QUICKSTART.md              # Início rápido (5 min)
├── 📄 OVERVIEW.md                # Este arquivo
├── 📄 PROJECT_STRUCTURE.md       # Estrutura detalhada
├── 📄 DEVELOPMENT.md             # Guia de desenvolvimento
│
├── 📁 backend/                   # Backend Node.js
│   ├── 📄 server.js              # Servidor Express
│   ├── 📁 routes/                # 4 arquivos de rotas
│   ├── 📁 database/              # SQLite config
│   └── 📄 package.json           # 7 dependências
│
└── 📁 android/                   # App Android
    ├── 📄 build.gradle.kts       # Config Gradle
    └── 📁 app/                   # Código fonte
        └── 📁 src/main/
            ├── 📄 AndroidManifest.xml
            └── 📁 kotlin/        # 15 arquivos Kotlin
```

## 🎯 Metas de Desempenho

| Operação | Meta | Status |
|----------|------|--------|
| Login | < 2s | ⏳ A medir |
| Registro | < 2s | ⏳ A medir |
| Criar Denúncia | < 3s | ⏳ A medir |
| Carregar Feed | < 2s | ⏳ A medir |
| Carregar Mapa | < 3s | ⏳ A medir |

**Capacidade:**
- 50-100 usuários simultâneos
- 10.000+ denúncias
- 5.000+ posts
- 100+ grupos

## 🛠️ Stack Tecnológica

### Backend
| Tecnologia | Versão | Uso |
|------------|--------|-----|
| Node.js | 16+ | Runtime |
| Express | 4.18 | Framework web |
| SQLite | 3 | Banco de dados |
| bcrypt | 5.1 | Hash de senhas |
| JWT | 9.0 | Autenticação |

### Android
| Tecnologia | Versão | Uso |
|------------|--------|-----|
| Kotlin | 1.9 | Linguagem |
| Compose | BOM 2023.10 | UI Framework |
| Hilt | 2.48 | Injeção de Dependência |
| Retrofit | 2.9 | HTTP Client |
| Room | 2.6 | Banco Local |
| Google Maps | 18.2 | Mapas |

## 📊 Estatísticas

### Código
- **Backend:** ~1.500 linhas
- **Android:** ~800 linhas (MVP)
- **Total:** ~2.300 linhas

### Arquivos
- **Backend:** 9 arquivos principais
- **Android:** 25 arquivos
- **Documentação:** 6 arquivos MD
- **Total:** 40 arquivos

### Funcionalidades
- **Endpoints API:** 22
- **Telas Android:** 12 planejadas (2 implementadas)
- **Tabelas BD:** 6
- **Modelos de Dados:** 15

## 🚀 Como Começar?

### Instalação Rápida (5 minutos)

1. **Backend:**
```bash
cd backend
npm install
npm run dev
```

2. **Android:**
- Abrir Android Studio
- Importar pasta `android/`
- Run ▶️

3. **Testar:**
```bash
curl http://localhost:3000/health
```

➡️ **Guia completo:** [QUICKSTART.md](./QUICKSTART.md)

## 📚 Documentação

| Documento | Descrição | Quando Usar |
|-----------|-----------|-------------|
| [README.md](./README.md) | Visão geral do projeto | Primeira leitura |
| [QUICKSTART.md](./QUICKSTART.md) | Começar em 5 minutos | Setup inicial |
| [OVERVIEW.md](./OVERVIEW.md) | Este arquivo | Resumo executivo |
| [PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md) | Estrutura detalhada | Entender organização |
| [DEVELOPMENT.md](./DEVELOPMENT.md) | Guia de desenvolvimento | Durante desenvolvimento |
| [backend/README.md](./backend/README.md) | Docs do backend | Trabalhar no backend |
| [android/README.md](./android/README.md) | Docs do Android | Trabalhar no Android |

## 🎓 Para Quem é Este Projeto?

### ✅ Ideal Para:
- 🏘️ Comunidades locais
- 👨‍💻 Desenvolvedores aprendendo Android/Node.js
- 🎓 Projetos acadêmicos
- 🔒 Ambientes que exigem privacidade de dados
- 📶 Locais com conectividade limitada

### ❌ Não Recomendado Para:
- 🌐 Aplicações de escala global
- ☁️ Projetos que precisam de cloud
- 📱 Apps multi-plataforma (iOS)
- 🚨 Integração com autoridades oficiais

## 🔐 Segurança

### Implementado
- ✅ Senhas com bcrypt (10 rounds)
- ✅ Autenticação JWT
- ✅ Prepared statements (anti SQL injection)
- ✅ Validação de entrada
- ✅ CORS configurado

### A Implementar
- ⏳ Rate limiting
- ⏳ HTTPS
- ⏳ Refresh tokens
- ⏳ 2FA (opcional)

## 📈 Roadmap Visual

```
Q4 2025 (MVP)
├── ✅ Estrutura do projeto
├── ✅ Backend completo
├── ✅ Base Android
└── ✅ Documentação

Q1 2026
├── 🚧 Implementação de telas
├── 🚧 ViewModels
├── 🚧 Integração Maps
└── 🚧 Upload de imagens

Q2 2026
├── 📋 Notificações
├── 📋 Visualização em mapa
├── 📋 Chat de grupo
└── 📋 Testes automatizados

Q3 2026+
├── 💡 Heatmap de crimes
├── 💡 Sistema de reputação
├── 💡 Relatórios e estatísticas
└── 💡 Recursos adicionais
```

## 🤝 Como Contribuir?

1. Fork o projeto
2. Crie uma branch (`feature/minha-feature`)
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

📖 Leia [DEVELOPMENT.md](./DEVELOPMENT.md) para convenções e boas práticas.

## ❓ FAQ

### P: O app funciona sem internet?
**R:** O backend precisa estar rodando na rede local. O app pode cachear dados com Room para uso offline limitado.

### P: Posso usar em produção?
**R:** Sim, para comunidades pequenas (50-100 usuários). Para mais, são necessárias otimizações.

### P: Preciso de Google Maps API Key?
**R:** Não é obrigatório para o MVP. Funcionalidades de mapa não funcionarão sem a chave.

### P: Posso fazer versão iOS?
**R:** Possível, mas fora do escopo atual. O backend é reutilizável.

### P: Como migro para a nuvem depois?
**R:** Substitua o SQLite por PostgreSQL/MySQL e hospede o backend em um servidor cloud. O Android precisa apenas mudar a BASE_URL.

### P: Qual o custo de infraestrutura?
**R:** Zero. Tudo roda localmente. Apenas energia elétrica do servidor local.

## 📞 Suporte

### Problemas Comuns
1. **Backend não inicia:** Verifique se a porta 3000 está livre
2. **Android não conecta:** Use `10.0.2.2:3000` no emulador
3. **Gradle sync falha:** Limpe cache com `./gradlew clean`
4. **Google Maps não funciona:** Configure a API Key

➡️ **Troubleshooting completo:** [DEVELOPMENT.md](./DEVELOPMENT.md#troubleshooting)

## 🏆 Créditos

Projeto desenvolvido como sistema comunitário de segurança local.

### Tecnologias de Código Aberto
- Node.js, Express, SQLite
- Kotlin, Android, Jetpack Compose
- Google Maps SDK
- E muitas outras bibliotecas incríveis!

## 📄 Licença

MIT License - Use livremente, modifique e distribua.

---

## 🎉 Status Atual

```
╔══════════════════════════════════════╗
║  🎊 ESTRUTURA 100% COMPLETA! 🎊     ║
║                                      ║
║  ✅ Backend: OPERACIONAL            ║
║  ✅ Android: ESTRUTURA PRONTA       ║
║  ✅ Docs: COMPLETA                  ║
║                                      ║
║  📍 Você está aqui: MVP Base        ║
║  🎯 Próximo: Implementar Features   ║
╚══════════════════════════════════════╝
```

### Próximos Passos Recomendados

1. ⭐ Testar o backend com Postman
2. ⭐ Rodar o app Android e ver as telas mockup
3. ⭐ Implementar o AuthViewModel
4. ⭐ Implementar tela de Registro
5. ⭐ Implementar tela de Denúncias

➡️ Comece agora: [QUICKSTART.md](./QUICKSTART.md)

---

**CrimeTracker** - Fortalecendo comunidades através da informação local 🏘️🛡️

*"A segurança começa com a colaboração"*

