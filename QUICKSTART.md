# 🚀 Quick Start Guide - CrimeTracker

Guia rápido para colocar o CrimeTracker funcionando em minutos.

## 📋 Pré-requisitos

### Backend
- Node.js 16+ instalado
- npm ou yarn

### Android
- Android Studio Hedgehog ou superior
- JDK 17
- Android SDK (API 24+)

## ⚡ Passo a Passo

### 1️⃣ Configurar o Backend

```bash
# Navegue até a pasta do backend
cd backend

# Instale as dependências
npm install

# Inicie o servidor
npm run dev
```

✅ O servidor deve estar rodando em `http://localhost:3000`

Teste com:
```bash
curl http://localhost:3000/health
```

Resposta esperada:
```json
{"status":"OK","message":"CrimeTracker Backend está rodando"}
```

### 2️⃣ Configurar o Android

1. **Abra o Android Studio**
   - File → Open
   - Selecione a pasta `android/`

2. **Configure a Google Maps API Key** (opcional para teste)
   - Abra `android/app/src/main/AndroidManifest.xml`
   - Substitua `YOUR_GOOGLE_MAPS_API_KEY` por uma chave válida
   - Ou deixe temporariamente para testar sem mapas

3. **Sincronize o Gradle**
   - O Android Studio fará isso automaticamente
   - Ou clique em "Sync Now" se aparecer

4. **Execute o App**
   - Conecte um dispositivo ou inicie um emulador
   - Clique no botão "Run" (▶️)

## 🔧 Configurações Importantes

### Emulador Android
O app já está configurado para conectar ao backend via:
```
http://10.0.2.2:3000
```
✅ Não precisa alterar nada!

### Dispositivo Físico
Se for usar um celular real:

1. Descubra o IP da sua máquina:
   ```bash
   # Windows
   ipconfig
   
   # Linux/Mac
   ifconfig
   ```

2. Edite `android/app/build.gradle.kts`:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"http://SEU_IP_AQUI:3000\"")
   ```
   Exemplo: `"http://192.168.1.10:3000"`

3. Certifique-se de que o celular está na mesma rede Wi-Fi

## 🧪 Teste Rápido

### 1. Registrar um usuário

```bash
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao",
    "email": "joao@example.com",
    "password": "senha123",
    "full_name": "João Silva"
  }'
```

### 2. Fazer login

```bash
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao",
    "password": "senha123"
  }'
```

Você receberá um `token` na resposta. Use-o nas próximas requisições.

### 3. Criar uma denúncia

```bash
curl -X POST http://localhost:3000/api/reports \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -d '{
    "title": "Roubo na Rua A",
    "description": "Assalto próximo ao mercado",
    "category": "roubo",
    "latitude": -23.5505,
    "longitude": -46.6333,
    "address": "Rua A, 123"
  }'
```

## 📱 Usando o App

1. **Tela de Login**
   - Na primeira vez, clique em "Criar nova conta"
   - Preencha os dados e registre-se
   - Faça login

2. **Navegação**
   - **Feed:** Veja posts da comunidade
   - **Denúncias:** Liste e crie denúncias
   - **Grupos:** Entre em grupos de bairro
   - **Perfil:** Veja seus dados

3. **Criar Denúncia**
   - Clique no botão flutuante (+)
   - Preencha título, descrição e categoria
   - A localização é capturada automaticamente
   - Adicione uma foto (opcional)
   - Envie

## 🐛 Solução de Problemas

### Backend não inicia
```bash
# Verifique se a porta 3000 está em uso
# Windows
netstat -ano | findstr :3000

# Linux/Mac
lsof -i :3000

# Se estiver em uso, mate o processo ou mude a porta em server.js
```

### Android não conecta ao backend
1. ✅ Certifique-se de que o backend está rodando
2. ✅ Verifique o IP/URL correto
3. ✅ Emulador: use `10.0.2.2:3000`
4. ✅ Dispositivo físico: use o IP local da máquina
5. ✅ Verifique se estão na mesma rede Wi-Fi
6. ✅ Desative firewall temporariamente para testar

### Erro de permissões no Android
- Aceite as permissões de localização quando solicitado
- Vá em Configurações → Apps → CrimeTracker → Permissões
- Habilite Localização e Câmera

### Erro de API Key do Google Maps
- Se não tiver a chave, as telas de mapa não funcionarão
- Outras funcionalidades funcionam normalmente
- Para obter uma chave: https://console.cloud.google.com/

## 📊 Dados de Teste

Para popular o banco com dados de teste:

```bash
# Crie alguns usuários
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"maria","email":"maria@test.com","password":"123456","full_name":"Maria Santos"}'

curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"pedro","email":"pedro@test.com","password":"123456","full_name":"Pedro Costa"}'

# Depois faça login com cada um e crie denúncias, grupos e posts
```

## 🎯 Próximos Passos

Depois de tudo funcionando:

1. ✅ Explore as funcionalidades básicas
2. 📸 Teste upload de imagens
3. 🗺️ Configure Google Maps
4. 👥 Crie grupos e convide amigos
5. 📝 Faça denúncias de teste
6. 💬 Poste no feed

## 📚 Documentação Completa

- **Geral:** [README.md](./README.md)
- **Backend:** [backend/README.md](./backend/README.md)
- **Android:** [android/README.md](./android/README.md)

## 💡 Dicas

- Use o modo desenvolvedor do Android Studio para ver logs
- Monitore os logs do backend no terminal
- Use o Postman ou Insomnia para testar a API
- O banco SQLite fica em `backend/database/crimetracker.db`

---

**Pronto!** 🎉 Você já pode começar a usar o CrimeTracker!

Se tiver problemas, consulte a seção de troubleshooting ou abra uma issue.

