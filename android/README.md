# CrimeTracker Android

Aplicativo Android nativo para o CrimeTracker usando Kotlin e Jetpack Compose.

## Tecnologias

- **Linguagem:** Kotlin
- **UI:** Jetpack Compose
- **Arquitetura:** MVVM + Clean Architecture
- **Injeção de Dependência:** Hilt
- **Networking:** Retrofit + OkHttp
- **Mapas:** Google Maps SDK
- **Localização:** Google Play Services Location
- **Armazenamento Local:** DataStore + Room
- **Imagens:** Coil

## Requisitos

- Android Studio Hedgehog ou superior
- JDK 17
- Android SDK 24+ (Android 7.0+)
- Gradle 8.2+

## Configuração

### 1. Google Maps API Key

Obtenha uma chave de API do Google Maps no [Google Cloud Console](https://console.cloud.google.com/) e adicione no `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="SUA_CHAVE_AQUI" />
```

### 2. Backend Local

Certifique-se de que o backend está rodando em `http://localhost:3000`. O aplicativo usa `http://10.0.2.2:3000` para acessar o localhost do emulador Android.

Para dispositivos físicos, altere o `BASE_URL` em `app/build.gradle.kts` para o IP da sua máquina na rede local.

## Build e Execução

```bash
# No diretório android/
./gradlew build

# Ou abra o projeto no Android Studio e execute normalmente
```

## Estrutura do Projeto

```
app/src/main/kotlin/com/crimetracker/app/
├── data/
│   ├── model/          # Data classes
│   ├── network/        # API service
│   ├── repository/     # Repositories
│   └── local/          # Local database
├── di/                 # Dependency injection modules
├── domain/             # Use cases
├── ui/
│   ├── screens/        # Screens/Features
│   │   ├── auth/       # Login, Register
│   │   ├── home/       # Home dashboard
│   │   ├── reports/    # Reports list and details
│   │   ├── groups/     # Groups management
│   │   └── feed/       # Social feed
│   ├── navigation/     # Navigation setup
│   ├── components/     # Reusable components
│   └── theme/          # Material Theme
└── util/               # Utilities

```

## Features Implementadas

- [x] Estrutura básica do projeto
- [x] Navegação com Jetpack Navigation Compose
- [x] Telas de Login e Home (mockup)
- [x] Configuração de rede com Retrofit
- [x] Modelos de dados completos
- [x] Injeção de dependência com Hilt
- [ ] Implementação completa das telas
- [ ] Integração com Google Maps
- [ ] Camera e galeria
- [ ] Permissões de localização
- [ ] Cache local com Room

## Próximos Passos

1. Implementar ViewModels para cada feature
2. Criar repositories para abstrair acesso aos dados
3. Implementar telas completas de Reports, Groups e Feed
4. Adicionar gerenciamento de estado com StateFlow
5. Implementar upload de imagens
6. Adicionar Google Maps para visualização de denúncias
7. Implementar sistema de notificações locais
8. Testes unitários e de integração

