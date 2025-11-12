# 🛠️ Instruções de Build - CrimeTracker Android

## 📋 Pré-requisitos

### **1. Instalações Necessárias**

- ✅ **Android Studio** (versão Hedgehog ou superior)
- ✅ **JDK 17** (incluído com Android Studio)
- ✅ **Android SDK 34**
- ✅ **Kotlin 1.9.20**
- ✅ **Gradle 8.2.0**

### **2. Backend Rodando**

Certifique-se de que o backend Node.js está rodando:

```bash
cd backend
npm run dev
```

Verifique se o servidor está acessível em `http://localhost:3000`.

---

## 🚀 Compilar e Executar

### **Opção 1: Android Studio (Recomendado)**

#### **Passo 1: Abrir o Projeto**

1. Abra o Android Studio
2. Clique em **"Open"**
3. Navegue até a pasta `android/`
4. Clique em **"OK"**

#### **Passo 2: Sincronizar Dependências**

O Android Studio irá automaticamente:
- Baixar dependências do Gradle
- Sincronizar o projeto
- Gerar código Hilt/Kapt

**Se não sincronizar automaticamente:**
```
File > Sync Project with Gradle Files
```

#### **Passo 3: Configurar Emulador**

1. Abra o **Device Manager** (ícone de celular no canto superior direito)
2. Clique em **"Create Device"**
3. Selecione **"Pixel 5"** ou qualquer dispositivo
4. Escolha a system image **Android 14 (API 34)**
5. Clique em **"Finish"**

#### **Passo 4: Executar**

1. Selecione o emulador no dropdown superior
2. Clique no botão **"Run"** (▶️) ou pressione `Shift + F10`
3. Aguarde o build e instalação

**Tempo estimado:**
- Primeiro build: 3-5 minutos
- Builds subsequentes: 30-60 segundos

---

### **Opção 2: Linha de Comando**

#### **Build Debug APK**

```bash
cd android
./gradlew assembleDebug
```

**APK gerado em:**
```
android/app/build/outputs/apk/debug/app-debug.apk
```

#### **Instalar no Emulador/Dispositivo**

```bash
# Listar dispositivos conectados
adb devices

# Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### **Executar Diretamente**

```bash
./gradlew installDebug
adb shell am start -n com.crimetracker.app/.MainActivity
```

---

## 🔧 Troubleshooting

### **Erro 1: "SDK location not found"**

**Problema:** Gradle não encontra o Android SDK.

**Solução:**

Crie o arquivo `android/local.properties`:

```properties
sdk.dir=C\:\\Users\\SEU_USUARIO\\AppData\\Local\\Android\\Sdk
```

(Ajuste o caminho para o seu sistema)

---

### **Erro 2: "Execution failed for task ':app:kaptDebugKotlin'"**

**Problema:** Erro na geração de código Hilt.

**Solução:**

```bash
# Limpar o build
./gradlew clean

# Rebuild
./gradlew build --refresh-dependencies
```

No Android Studio:
```
Build > Clean Project
Build > Rebuild Project
```

---

### **Erro 3: "Unable to resolve dependency for ':app@debug/compileClasspath'"**

**Problema:** Dependências não baixadas corretamente.

**Solução:**

```bash
# Deletar cache do Gradle
rm -rf ~/.gradle/caches/

# No Android Studio:
File > Invalidate Caches > Invalidate and Restart
```

---

### **Erro 4: "Failed to connect to /10.0.2.2:3000"**

**Problema:** App não consegue conectar ao backend.

**Soluções:**

#### **Emulador Android:**
Use `10.0.2.2` (já configurado):

```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:3000/\"")
```

#### **Dispositivo Físico:**

1. Conecte o dispositivo à mesma rede Wi-Fi do computador
2. Descubra o IP local do computador:

**Windows:**
```powershell
ipconfig
```

**Linux/Mac:**
```bash
ifconfig
```

3. Altere o `BASE_URL` em `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://192.168.1.10:3000/\"")
```

(Substitua `192.168.1.10` pelo IP do seu computador)

4. Rebuild o projeto.

---

### **Erro 5: "Cleartext HTTP traffic not permitted"**

**Problema:** Android bloqueia HTTP não seguro por padrão.

**Solução:**

Já está configurado no `AndroidManifest.xml`:

```xml
android:usesCleartextTraffic="true"
```

Se ainda assim ocorrer erro, adicione:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

E crie `res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

---

### **Erro 6: "java.lang.OutOfMemoryError: GC overhead limit exceeded"**

**Problema:** Build consome muita memória.

**Solução:**

Edite `android/gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
```

---

### **Erro 7: "Missing Google Maps API Key"**

**Problema:** Chave do Google Maps não configurada.

**Nota:** A chave não é obrigatória para a versão inicial do app. As funcionalidades de mapa serão implementadas posteriormente.

**Se quiser configurar agora:**

1. Obtenha uma API Key no [Google Cloud Console](https://console.cloud.google.com/)
2. Edite `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="SUA_CHAVE_AQUI" />
```

---

## 📱 Testando a Aplicação

### **1. Fluxo Básico de Teste**

1. **Abrir app** → Splash Screen (1.5s)
2. **LoginScreen** aparece (não há usuário logado)
3. Clicar em **"Cadastre-se"**
4. Preencher dados e clicar em **"Cadastrar"**
5. **HomeScreen** aparece com 3 abas
6. Testar navegação entre abas
7. Testar FAB (Floating Action Button) em Denúncias e Grupos
8. Testar navegação para ProfileScreen (menu superior direito)

### **2. Verificar Logs**

No Android Studio, abra o **Logcat** (parte inferior) e filtre por:

```
com.crimetracker.app
```

Você verá logs de:
- Requisições HTTP (OkHttp)
- Navegação (NavController)
- Autenticação (DataStore)

---

## 🔍 Verificações Pós-Build

### **✅ Checklist**

- [ ] App abre sem crash
- [ ] SplashScreen aparece por ~1.5s
- [ ] LoginScreen carrega corretamente
- [ ] RegisterScreen é acessível
- [ ] Botões de navegação funcionam
- [ ] Bottom navigation funciona (3 abas)
- [ ] FABs (Floating Action Buttons) abrem telas corretas
- [ ] Menu superior direito mostra "Perfil" e "Sair"
- [ ] Backend está acessível (verificar logs do Logcat)

---

## 📊 Estrutura de Build

```
android/
├── build/                      # Arquivos de build (gerados)
├── app/
│   ├── build/                  # Build do módulo app
│   │   └── outputs/
│   │       └── apk/
│   │           └── debug/
│   │               └── app-debug.apk  # APK final
│   ├── build.gradle.kts        # Configurações do módulo
│   └── src/                    # Código fonte
├── gradle/                     # Gradle wrapper
├── build.gradle.kts            # Configurações do projeto
├── settings.gradle.kts         # Módulos do projeto
└── gradle.properties           # Propriedades do Gradle
```

---

## ⚡ Dicas de Performance

### **1. Builds Incrementais**

No `gradle.properties`:

```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
kotlin.incremental=true
```

### **2. Build Variants**

```bash
# Debug (rápido, com logs)
./gradlew assembleDebug

# Release (otimizado, sem logs)
./gradlew assembleRelease
```

### **3. Limpar Build Cache**

```bash
# Apenas limpar
./gradlew clean

# Limpar e rebuild
./gradlew clean build
```

---

## 🎯 Próximos Passos

Após o build ser bem-sucedido:

1. ✅ **Testar login/registro** conectando ao backend
2. ✅ **Implementar ViewModels** para cada tela
3. ✅ **Adicionar loading states** e error handling
4. ✅ **Implementar busca de denúncias próximas** com GPS
5. ✅ **Adicionar Google Maps** nas denúncias
6. ✅ **Implementar feed** com LazyColumn

---

## 📝 Comandos Úteis

```bash
# Listar todos os tasks disponíveis
./gradlew tasks

# Limpar + Build
./gradlew clean build

# Gerar APK debug
./gradlew assembleDebug

# Instalar no dispositivo
./gradlew installDebug

# Executar testes
./gradlew test

# Verificar dependências
./gradlew dependencies

# Verificar atualizações de dependências
./gradlew dependencyUpdates
```

---

## 🎉 Resumo

```
╔════════════════════════════════════════════════╗
║                                                ║
║  ✅ BUILD INSTRUCTIONS COMPLETO                ║
║                                                ║
║  🛠️  Setup do Android Studio                   ║
║  📱 Configuração de emulador                   ║
║  🔧 Troubleshooting completo                   ║
║  ⚡ Otimizações de performance                 ║
║  📝 Comandos úteis                             ║
║                                                ║
║  ✨ PRONTO PARA BUILD!                         ║
║                                                ║
╚════════════════════════════════════════════════╝
```

**Siga as instruções acima para compilar e executar o app com sucesso! 🚀**

