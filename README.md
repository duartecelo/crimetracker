# 🛡️ CrimeTracker 🛡️
Fortalecer comunidades através da segurança colaborativa.

O CrimeTracker é uma solução completa de segurança comunitária desenjada para capacitar cidadãos a monitorizar e reportar incidentes nos seus bairros. Com uma arquitetura robusta e foco na privacidade, o sistema opera com um backend local e uma aplicação Android nativa, utilizando mapas open-source para garantir que a informação crítica permanece acessível.

---

## 📑 Sumário
1. [Visão Geral](#-visão-geral)
2. [Funcionalidades Principais](#-funcionalidades-principais)
3. [Arquitetura e Tecnologia](#-arquitetura-e-tecnologia)
   - [Aplicação Android](#-aplicação-android-cliente)
   - [Backend API](#️-backend-api-servidor)
4. [Como Configurar e Executar](#-como-configurar-e-executar)
5. [Testes e Qualidade](#-testes-e-qualidade)
6. [Estrutura do Projeto](#-estrutura-do-projeto)

---

## 📱 Visão Geral
A aplicação permite que vizinhos criem redes de confiança, reportem atividades suspeitas em tempo real e visualizem ocorrências num mapa interativo sem custos de licenciamento de APIs proprietárias.

---

## 🌟 Funcionalidades Principais

### 🚨 Reporte de Crimes em Tempo Real
Registo de incidentes (Assalto, Furto, Vandalismo, etc.) com geolocalização precisa.

### 🗺️ Mapeamento Interativo (OpenStreetMap)
Visualização dinâmica de ocorrências utilizando OSMDroid. Inclui modos padrão e satélite, clusterização de marcadores e filtros por tipo de crime.

### 👥 Comunidades e Grupos
Criação de grupos de bairro para comunicação focada. Permite entrar, sair e gerir membros.

### 📰 Feed Social
Partilha de alertas, notícias e atualizações com texto e imagens dentro dos grupos.

### 👍 Sistema de Feedback
Validação comunitária de denúncias com botões “Útil/Não Útil” e cálculo de reputação.

### 🔒 Autenticação Segura
Registo, login e recuperação de palavra-passe via e-mail.

---

## 🏗️ Arquitetura e Tecnologia
O projeto segue uma abordagem Full Stack composta por Android + Node.js.

---

## 📱 Aplicação Android (Cliente)
- Desenvolvida 100% em **Kotlin + Jetpack Compose**.  
- **Clean Architecture + MVVM**  
- UI com Material Design 3 e suporte Claro/Escuro.

### Mapas
- **OSMDroid (OpenStreetMap)** → solução gratuita, sem API key.

### Injeção de Dependências
- **Hilt**

### Rede
- **Retrofit + OkHttp**

### Persistência
- **Room** (cache offline)  
- **DataStore** (tokens e preferências)

### Multimédia
- **Coil** e **Android Image Cropper**

---

## 🖥️ Backend API (Servidor)
- Construído com **Node.js + Express**
- Base de dados **SQLite (better-sqlite3)**

### Segurança
- **JWT**  
- **Bcrypt**  
- **Express-Validator**

### Uploads
- **Multer** para imagens

---

## 🚀 Como Configurar e Executar

### ✔ Pré‑requisitos
- Node.js 18+
- Android Studio Ladybug/Hedgehog+
- JDK 17

---

## 🔧 1. Configuração do Backend

```bash
cd backend
npm install
npm run dev
```

Servidor em **http://localhost:3000**

---

## 📲 2. Configuração da Aplicação Android
- Projeto localizado na pasta `android/`
- Não precisa de API key
- No emulador, usa **http://10.0.2.2:3000**
- Num dispositivo físico, alterar BASE_URL para o IP da máquina

---

## 🧪 Testes e Qualidade

### Testes de Integração
```bash
# PowerShell
.\scripts	est_all.ps1

# Bash
bash scripts/test_all.sh
```

### Testes Unitários
```bash
npm test
```

---

## 📂 Estrutura do Projeto
```
CrimeTracker/
├── android/
│   ├── app/src/main/java/com/crimetracker/app/
│   │   ├── data/
│   │   ├── di/
│   │   ├── ui/
│   │   │   ├── map/
│   │   │   └── ...
│   │   └── util/
│   └── build.gradle.kts
│
├── backend/
│   ├── database/
│   ├── routes/
│   ├── services/
│   ├── middleware/
│   └── scripts/
```
