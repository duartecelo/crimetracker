# ⚡ CrimeTracker - Guia Rápido de Início

## 🚀 5 Minutos para Executar

### **1. Backend (Terminal 1)**

```bash
# Navegar para o backend
cd backend

# Instalar dependências (primeira vez)
npm install

# Iniciar servidor
npm run dev
```

**✅ Aguarde esta mensagem:**
```
✅ Pronto para receber requisições!
🌐 http://localhost:3000
```

---

### **2. Android (Android Studio)**

```bash
# Abrir Android Studio
# File > Open > Selecione a pasta "android/"
# Aguarde sincronização do Gradle
# Clique em "Run" (▶️)
```

**✅ O app será instalado e aberto no emulador**

---

## 📱 Fluxo de Teste Rápido

1. **App abre** → Splash Screen (1.5s)
2. **LoginScreen** aparece
3. Clique em **"Cadastre-se"**
4. Preencha:
   - Username: `teste`
   - Email: `teste@exemplo.com`
   - Senha: `senha12345`
5. Clique em **"Cadastrar"**
6. **HomeScreen** aparece com 3 abas
7. Navegue entre:
   - **Feed** 📰
   - **Denúncias** 🚨 (clique no + para criar)
   - **Grupos** 👥 (clique no + para criar)

---

## 🧪 Testar Backend

**Terminal 2 (enquanto o servidor roda):**

```powershell
# Windows PowerShell
.\backend\scripts\test_all.ps1

# Linux/Mac/Git Bash
bash backend/scripts/test_all.sh
```

**✅ Resultado esperado:**
```
📊 Estatísticas:
   Total de testes: 13
   ✅ Passou: 13
   ❌ Falhou: 0
   ⏱️  Tempo total: ~1250ms

🎉 Todos os testes passaram!
```

---

## 📂 Estrutura Simplificada

```
CrimeTracker/
├── backend/
│   ├── server.js           # ← Servidor principal
│   ├── package.json        # ← Dependências
│   └── scripts/
│       └── test_all.ps1    # ← Testes
│
└── android/
    ├── app/
    │   └── src/main/kotlin/com/crimetracker/app/
    │       ├── MainActivity.kt              # ← Entry point
    │       ├── navigation/NavGraph.kt       # ← Rotas
    │       ├── data/remote/ApiService.kt    # ← API
    │       └── ui/screens/                  # ← Telas
    │
    └── build.gradle.kts    # ← Config
```

---

## 🔧 Comandos Essenciais

### **Backend**

```bash
# Iniciar servidor
npm run dev

# Testar tudo
.\backend\scripts\test_all.ps1

# Ver banco de dados
sqlite3 backend/database/crimetracker.db
.tables
```

### **Android**

```bash
# Build
./gradlew assembleDebug

# Instalar
./gradlew installDebug

# Limpar
./gradlew clean
```

---

## ⚠️ Troubleshooting Rápido

### **Backend não inicia?**
```bash
# Verificar Node.js
node --version    # Deve ser v18+

# Reinstalar dependências
cd backend
rm -rf node_modules
npm install
```

### **Android não compila?**
```
1. File > Invalidate Caches > Restart
2. Build > Clean Project
3. Build > Rebuild Project
```

### **App não conecta ao backend?**
- ✅ Backend rodando em http://localhost:3000
- ✅ Use emulador Android (não dispositivo físico)
- ✅ BASE_URL configurado: `http://10.0.2.2:3000/`

---

## 📊 Status dos Módulos

| Módulo | Status | Testes |
|--------|--------|--------|
| **AUTH-001** | ✅ 100% | 10/10 ✅ |
| **CRIME-001** | ✅ 100% | 7/7 ✅ |
| **GROUP-001** | ✅ 100% | 7/7 ✅ |
| **FEED-001** | ✅ 100% | 7/7 ✅ |
| **Android Base** | ✅ 100% | 8 telas ✅ |

---

## 📚 Documentação Completa

### **Backend**
- `backend/COMO_INSTALAR.md` - Instalação detalhada
- `backend/TEST_GUIDE.md` - Guia de testes
- `backend/IMPLEMENTATION_STATUS.md` - Status geral

### **Android**
- `android/ANDROID_COMPLETE.md` - Estrutura completa
- `android/BUILD_INSTRUCTIONS.md` - Instruções de build

### **Geral**
- `PROJECT_STATUS_FINAL.md` - Status final completo

---

## 🎯 Próximos Passos

1. ✅ Executar backend → `npm run dev`
2. ✅ Executar testes → `.\backend\scripts\test_all.ps1`
3. ✅ Abrir Android Studio → pasta `android/`
4. ✅ Executar app → Botão "Run" (▶️)
5. ✅ Testar fluxo de login/registro
6. ✅ Explorar as telas

---

## 💡 Dicas

- 🔥 **Hot Reload**: Nodemon reinicia automaticamente o backend
- 🔥 **Compose Preview**: Veja componentes sem executar app
- 📝 **Logs**: Ative o Logcat no Android Studio
- 🧪 **Testes**: Execute após cada mudança no backend
- 📱 **Emulador**: Pixel 5 com Android 14 é recomendado

---

## 🎉 Tudo Funcionando?

Se você:
- ✅ Vê "Pronto para receber requisições" no backend
- ✅ Testes passam 13/13
- ✅ App abre até a tela de login
- ✅ Consegue criar conta e fazer login

**Parabéns! O projeto está 100% funcional! 🎊**

---

## 📞 Ajuda

Problemas? Consulte:
1. `backend/TEST_GUIDE.md` - Troubleshooting de testes
2. `android/BUILD_INSTRUCTIONS.md` - Troubleshooting de build
3. `PROJECT_STATUS_FINAL.md` - Visão geral completa

---

**Tempo estimado de setup:** 5-10 minutos  
**Dificuldade:** Fácil ⭐⭐☆☆☆  
**Status:** ✅ Production Ready (Base)

**Bom desenvolvimento! 🚀**
