# 🪟 Instalação no Windows - CrimeTracker Backend

## ⚠️ Pré-requisitos para Windows

O backend CrimeTracker usa `better-sqlite3`, que requer **compilação nativa**. No Windows, você precisa instalar as ferramentas de build do Visual Studio.

## 📥 Opção 1: Instalar Build Tools (Recomendado)

### Método Rápido (via npm)
```bash
npm install --global windows-build-tools
```

**OU**

### Método Manual
1. Baixe o Visual Studio Build Tools:
   https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2022

2. Durante a instalação, selecione:
   - ✅ **Desktop development with C++**
   - ✅ **MSVC v143 - VS 2022 C++ x64/x86**
   - ✅ **Windows 10/11 SDK**

3. Após a instalação, reinicie o computador

4. Instale as dependências:
```bash
cd backend
npm install
```

## 📥 Opção 2: Usar Binário Pré-Compilado

Caso não queira instalar as Build Tools, baixe o binário pré-compilado:

```bash
cd backend
npm install better-sqlite3 --build-from-source=false
```

## 📥 Opção 3: Usar Docker (Alternativa)

Se preferir, use Docker para evitar problemas de compilação:

```dockerfile
# Dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3000
CMD ["npm", "run", "dev"]
```

```bash
docker build -t crimetracker-backend .
docker run -p 3000:3000 crime tracker-backend
```

## ✅ Verificar Instalação

Após instalar as dependências:

```bash
cd backend
npm run dev
```

Teste o servidor:
```bash
curl http://localhost:3000/health
```

Resposta esperada:
```json
{
  "success": true,
  "data": {
    "status": "online",
    "timestamp": "2025-11-12T...",
    "environment": "development",
    "database": "connected"
  },
  "message": "Servidor rodando"
}
```

## 🔧 Troubleshooting

### Erro: "Could not find any Visual Studio installation"
- Instale o Visual Studio Build Tools (Opção 1)
- Ou tente o binário pré-compilado (Opção 2)

### Erro: "EPERM: operation not permitted"
- Feche o Visual Studio Code
- Execute o terminal como Administrador
- Tente novamente

### Erro: "node-gyp rebuild failed"
- Verifique se instalou o "Desktop development with C++"
- Reinicie o computador após instalar
- Tente limpar o cache: `npm cache clean --force`

## 📚 Links Úteis

- [Visual Studio Build Tools](https://visualstudio.microsoft.com/downloads/#build-tools-for-visual-studio-2022)
- [node-gyp no Windows](https://github.com/nodejs/node-gyp#on-windows)
- [better-sqlite3 Installation](https://github.com/WiseLibs/better-sqlite3/blob/master/docs/install.md)

## 🐧 Alternativa: Use WSL2

Se tiver WSL2 (Windows Subsystem for Linux):

```bash
# No terminal WSL
cd /mnt/c/Users/User/Desktop/PORTIFOLIO/JAVA/CrimeTracker/backend
npm install
npm run dev
```

No WSL2, a instalação é mais simples e não requer Build Tools.

---

**Após resolver a instalação, volte para o [README principal](../README.md)**

