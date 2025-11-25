# 🛡️ CrimeTracker

**Fortalecendo comunidades através da segurança colaborativa.**

---

## 📱 Visão Geral

O **CrimeTracker** é uma solução completa de segurança comunitária projetada para capacitar cidadãos a monitorar e reportar incidentes em seus bairros. Com uma arquitetura robusta e foco na privacidade, o sistema opera com um backend local e um aplicativo Android nativo, garantindo que as informações críticas permaneçam sob o controle da comunidade.

### 🌟 Principais Funcionalidades

-   🚨 **Reporte de Crimes em Tempo Real**: Registre incidentes com precisão de localização GPS.
-   👥 **Grupos de Vigilância**: Crie e gerencie grupos de bairro para comunicação focada.
-   📰 **Feed Social Comunitário**: Compartilhe alertas, notícias e atualizações com seus vizinhos.
-   🗺️ **Mapeamento Interativo**: Visualize zonas de risco e ocorrências recentes em um mapa dinâmico.
-   🔒 **Privacidade em Primeiro Lugar**: Arquitetura descentralizada e local.

---

## 🏗️ Arquitetura e Tecnologia

O CrimeTracker foi construído seguindo as melhores práticas de engenharia de software para garantir escalabilidade, manutenibilidade e performance.

### 📱 Android App (Cliente)

Desenvolvido com **Kotlin** e **Jetpack Compose**, o aplicativo segue os princípios da **Clean Architecture** e padrão **MVVM (Model-View-ViewModel)**.

-   **UI Moderna**: Interface declarativa construída 100% em Jetpack Compose com Material Design 3.
-   **Injeção de Dependência**: Utilização do **Hilt** para gerenciamento robusto de dependências e testabilidade.
-   **Gerenciamento de Estado**: Uso de `StateFlow` e `Coroutines` para uma experiência reativa e fluida.
-   **Rede e Dados**:
    -   **Retrofit**: Cliente HTTP tipado para comunicação com a API.
    -   **Room**: Persistência de dados local para suporte offline (cache).
    -   **DataStore**: Armazenamento seguro de preferências e tokens de sessão.
-   **Mapas**: Integração com Google Maps SDK para visualização geoespacial.

### 🖥️ Backend API (Servidor)

Uma API RESTful performante construída com **Node.js** e **Express**.

-   **Banco de Dados**: **SQLite** para uma solução leve, rápida e serverless, ideal para implantações locais.
-   **Segurança**:
    -   **JWT (JSON Web Tokens)**: Autenticação stateless segura.
    -   **Bcrypt**: Hashing robusto de senhas.
    -   **Validação**: Middlewares de validação rigorosa de dados de entrada.

---

## ✅ Garantia de Qualidade (QA)

Como garantimos que o sistema funciona de forma confiável? Nossa estratégia de qualidade abrange múltiplas camadas:

### 1. Arquitetura Testável
A adoção da **Clean Architecture** no Android não é apenas estética; ela desacopla a lógica de negócios da interface do usuário e frameworks. Isso significa que:
-   **Regras de Negócio** são isoladas e podem ser testadas independentemente.
-   **Repositórios** abstraem a fonte de dados, permitindo que testes usem dados falsos (mocks) sem necessidade de rede ou banco de dados real.

### 2. Tratamento de Erros Robusto
O aplicativo é projetado para ser resiliente:
-   **Network Resilience**: O `NetworkModule` configura o Retrofit para lidar com timeouts e falhas de conexão graciosamente.
-   **Safe API Calls**: Wrappers de chamada de API capturam exceções e as convertem em estados de erro amigáveis para a UI (`Result.Success` / `Result.Error`), garantindo que o app nunca feche inesperadamente por falhas de rede.

### 3. Tipagem e Segurança de Código
-   **Kotlin**: O uso de Kotlin garante *Null Safety*, eliminando uma classe inteira de erros comuns (NullPointerException).
-   **Validação no Backend**: O backend não confia cegamente no cliente. Todas as entradas são validadas e sanitizadas antes de tocar no banco de dados, prevenindo injeção de SQL e dados corrompidos.

---

## 🚀 Como Executar

### Pré-requisitos
-   **Node.js** (v18 ou superior)
-   **Android Studio** (Hedgehog ou superior)
-   **JDK 17**

### 1. Configurando o Backend

```bash
cd backend
npm install
npm run dev
```
*O servidor iniciará em `http://localhost:3000`.*

### 2. Configurando o Android App

1.  Abra o projeto na pasta `android` com o Android Studio.
2.  Aguarde a sincronização do Gradle.
3.  Crie um arquivo `local.properties` na raiz do projeto android (se não existir) e adicione sua chave do Google Maps:
    ```properties
    MAPS_API_KEY=SUA_CHAVE_AQUI
    ```
4.  Execute o app em um emulador ou dispositivo físico.
    *Nota: Se usar o emulador, o app já está configurado para conectar em `10.0.2.2:3000`.*

---

## 📂 Estrutura do Projeto

```
CrimeTracker/
├── android/              # Código fonte do App Android
│   ├── app/src/main/java/com/crimetracker/app/
│   │   ├── data/         # Repositórios, Fontes de Dados, Modelos
│   │   ├── di/           # Módulos Hilt (Injeção de Dependência)
│   │   ├── domain/       # Casos de Uso (Regras de Negócio)
│   │   ├── ui/           # Telas (Compose), ViewModels, Temas
│   │   └── util/         # Utilitários e Helpers
├── backend/              # Código fonte da API Node.js
│   ├── database/         # Configuração SQLite
│   ├── routes/           # Rotas da API
│   ├── middleware/       # Autenticação e Validação
│   └── server.js         # Ponto de entrada
└── README.md             # Documentação do Projeto
```