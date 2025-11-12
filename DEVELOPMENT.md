# 🛠️ Guia de Desenvolvimento - CrimeTracker

Guia para desenvolvedores contribuindo com o projeto.

## 📋 Índice

1. [Ambiente de Desenvolvimento](#ambiente-de-desenvolvimento)
2. [Convenções de Código](#convenções-de-código)
3. [Git Workflow](#git-workflow)
4. [Testes](#testes)
5. [Deployment](#deployment)
6. [Troubleshooting](#troubleshooting)

## 🖥️ Ambiente de Desenvolvimento

### Ferramentas Necessárias

#### Backend
- **Node.js:** v16.x ou superior
- **npm:** v8.x ou superior
- **Editor:** VS Code (recomendado)
- **Extensões VS Code:**
  - ESLint
  - Prettier
  - SQLite Viewer

#### Android
- **Android Studio:** Hedgehog (2023.1.1) ou superior
- **JDK:** 17
- **Android SDK:** API 24-34
- **Emulador:** Pixel 5 API 34 (recomendado)

### Configuração Inicial

```bash
# Clone o repositório
git clone <repository-url>
cd CrimeTracker

# Backend
cd backend
npm install
npm run dev

# Android
# Abra o Android Studio e importe o projeto android/
```

## 📝 Convenções de Código

### Backend (JavaScript/Node.js)

#### Naming Conventions
```javascript
// Arquivos: camelCase
authController.js
userService.js

// Variáveis e funções: camelCase
const userName = 'João';
function getUserById(id) { }

// Constantes: UPPER_SNAKE_CASE
const JWT_SECRET = 'secret';
const MAX_LOGIN_ATTEMPTS = 5;

// Classes: PascalCase (se usar)
class UserManager { }
```

#### Code Style
```javascript
// Use const/let, nunca var
const user = await getUserById(id);
let count = 0;

// Arrow functions para callbacks
users.map(user => user.name);

// Async/await ao invés de promises .then()
try {
  const data = await fetchData();
} catch (error) {
  console.error(error);
}

// Destructuring quando apropriado
const { username, email } = req.body;

// Template literals
const message = `Bem-vindo, ${user.name}!`;
```

#### Error Handling
```javascript
// Sempre use try-catch em rotas async
router.post('/endpoint', async (req, res) => {
  try {
    // código
    res.json({ success: true });
  } catch (error) {
    console.error('Erro:', error);
    res.status(500).json({ error: 'Mensagem amigável' });
  }
});

// Valide entrada de dados
const errors = validationResult(req);
if (!errors.isEmpty()) {
  return res.status(400).json({ errors: errors.array() });
}
```

#### Database Queries
```javascript
// Use prepared statements (já implementado)
const stmt = db.prepare('SELECT * FROM users WHERE id = ?');
const user = stmt.get(userId);

// Evite SQL injection
// ❌ NUNCA faça isso:
db.prepare(`SELECT * FROM users WHERE username = '${username}'`);

// ✅ Sempre faça isso:
db.prepare('SELECT * FROM users WHERE username = ?').get(username);
```

### Android (Kotlin)

#### Naming Conventions
```kotlin
// Arquivos: PascalCase (nome da classe)
LoginScreen.kt
AuthViewModel.kt

// Packages: lowercase
com.crimetracker.app.ui.screens

// Classes: PascalCase
class LoginViewModel

// Funções: camelCase
fun getUserProfile()

// Variáveis: camelCase
val userName = "João"
var isLoading = false

// Constantes: UPPER_SNAKE_CASE (companion object)
companion object {
    const val MAX_RETRIES = 3
}

// Composables: PascalCase
@Composable
fun LoginScreen()
```

#### Code Style
```kotlin
// Use val ao invés de var quando possível
val name = "João"
var count = 0

// Null safety
val length = name?.length ?: 0
val user: User? = getUser()

// Data classes para modelos
data class User(
    val id: Int,
    val name: String,
    val email: String
)

// Sealed classes para estados
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Report>) : UiState()
    data class Error(val message: String) : UiState()
}

// Extension functions quando apropriado
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
```

#### Compose Best Practices
```kotlin
// Composables devem ser pure functions
@Composable
fun LoginScreen(
    state: LoginUiState,
    onLogin: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // UI code
}

// Use remember para estado local
var text by remember { mutableStateOf("") }

// Use rememberCoroutineScope para coroutines
val scope = rememberCoroutineScope()

// Hoisting de estado
// ❌ Evite estado dentro de composables reutilizáveis
@Composable
fun MyButton() {
    var clicked by remember { mutableStateOf(false) }
    // ...
}

// ✅ Faça state hoisting
@Composable
fun MyButton(
    clicked: Boolean,
    onClickChange: (Boolean) -> Unit
) {
    // ...
}
```

#### ViewModel Pattern
```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val result = authRepository.login(username, password)
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Erro desconhecido")
            }
        }
    }
}
```

## 🔀 Git Workflow

### Branch Strategy

```
main
  ├── develop
  │   ├── feature/auth-implementation
  │   ├── feature/reports-screen
  │   ├── bugfix/login-crash
  │   └── hotfix/critical-bug
```

### Branch Naming
- `feature/nome-da-feature` - Novas funcionalidades
- `bugfix/descricao-do-bug` - Correções de bugs
- `hotfix/descricao` - Correções urgentes
- `refactor/descricao` - Refatorações
- `docs/descricao` - Documentação

### Commit Messages

Siga o padrão [Conventional Commits](https://www.conventionalcommits.org/):

```bash
# Formato
<tipo>(<escopo>): <descrição curta>

<corpo opcional>

<rodapé opcional>

# Exemplos
feat(auth): adiciona validação de email no registro
fix(reports): corrige crash ao carregar imagem
docs(readme): atualiza instruções de instalação
refactor(api): reorganiza estrutura de rotas
test(auth): adiciona testes de login
style(ui): ajusta espaçamento dos botões
perf(database): otimiza query de denúncias
chore(deps): atualiza dependências do npm
```

#### Tipos de Commit
- `feat` - Nova funcionalidade
- `fix` - Correção de bug
- `docs` - Documentação
- `style` - Formatação, ponto e vírgula, etc
- `refactor` - Refatoração de código
- `test` - Adiciona ou modifica testes
- `perf` - Melhoria de performance
- `chore` - Tarefas de manutenção

### Pull Request Template

```markdown
## Descrição
Breve descrição das mudanças

## Tipo de mudança
- [ ] Nova funcionalidade
- [ ] Correção de bug
- [ ] Refatoração
- [ ] Documentação
- [ ] Outro (especifique)

## Como testar
1. Passo 1
2. Passo 2
3. Resultado esperado

## Checklist
- [ ] Código segue as convenções do projeto
- [ ] Testes adicionados/atualizados
- [ ] Documentação atualizada
- [ ] Build passa sem erros
- [ ] Sem warnings do linter
- [ ] Testado em emulador/dispositivo

## Screenshots (se aplicável)
```

## 🧪 Testes

### Backend

```bash
# Instalar dependências de teste
npm install --save-dev jest supertest

# Criar estrutura de testes
backend/
  ├── __tests__/
  │   ├── auth.test.js
  │   ├── reports.test.js
  │   └── groups.test.js
```

#### Exemplo de Teste
```javascript
const request = require('supertest');
const app = require('../server');

describe('Auth API', () => {
  test('POST /api/auth/login - sucesso', async () => {
    const response = await request(app)
      .post('/api/auth/login')
      .send({
        username: 'testuser',
        password: 'password123'
      });
    
    expect(response.status).toBe(200);
    expect(response.body).toHaveProperty('token');
  });

  test('POST /api/auth/login - credenciais inválidas', async () => {
    const response = await request(app)
      .post('/api/auth/login')
      .send({
        username: 'invalid',
        password: 'wrong'
      });
    
    expect(response.status).toBe(401);
  });
});
```

### Android

```bash
# Rodar testes unitários
./gradlew test

# Rodar testes instrumentados
./gradlew connectedAndroidTest
```

#### Exemplo de Teste
```kotlin
@Test
fun `login with valid credentials should succeed`() = runTest {
    // Arrange
    val viewModel = LoginViewModel(fakeAuthRepository)
    
    // Act
    viewModel.login("user", "password")
    
    // Assert
    val state = viewModel.uiState.value
    assertThat(state).isInstanceOf(UiState.Success::class.java)
}

@Test
fun `email validation should reject invalid emails`() {
    assertThat("test@example.com".isValidEmail()).isTrue()
    assertThat("invalid-email".isValidEmail()).isFalse()
}
```

## 🚀 Deployment

### Backend (Local)

```bash
# Produção local
cd backend
npm install --production
NODE_ENV=production npm start
```

### Android (APK)

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requer assinatura)
./gradlew assembleRelease
```

#### Assinatura do APK
```bash
# Gerar keystore
keytool -genkey -v -keystore crimetracker.keystore -alias crimetracker -keyalg RSA -keysize 2048 -validity 10000

# Configurar em app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file("crimetracker.keystore")
            storePassword = "password"
            keyAlias = "crimetracker"
            keyPassword = "password"
        }
    }
}
```

## 🐛 Troubleshooting

### Backend

#### Porta em uso
```bash
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :3000
kill -9 <PID>
```

#### Erro de permissões no SQLite
```bash
# Dar permissões corretas
chmod 755 backend/database/
chmod 644 backend/database/crimetracker.db
```

#### Dependências não instaladas
```bash
# Limpar cache e reinstalar
rm -rf node_modules package-lock.json
npm install
```

### Android

#### Gradle sync falha
```bash
# Limpar cache do Gradle
./gradlew clean
./gradlew --stop
rm -rf .gradle

# No Android Studio
File > Invalidate Caches and Restart
```

#### Emulador não conecta ao backend
1. Verificar se backend está rodando
2. Usar `10.0.2.2:3000` para localhost
3. Testar com: `adb shell ping 10.0.2.2`

#### Build falha por falta de memória
```bash
# Aumentar heap do Gradle em gradle.properties
org.gradle.jvmargs=-Xmx4096m
```

#### Erro de API Key do Google Maps
1. Obter chave em: https://console.cloud.google.com/
2. Habilitar Maps SDK for Android
3. Adicionar no AndroidManifest.xml

## 📚 Recursos Úteis

### Documentação Oficial
- [Node.js](https://nodejs.org/docs/)
- [Express](https://expressjs.com/)
- [SQLite](https://www.sqlite.org/docs.html)
- [Kotlin](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose/documentation)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)

### Tutoriais
- [REST API Best Practices](https://restfulapi.net/)
- [Compose Basics](https://developer.android.com/codelabs/jetpack-compose-basics)
- [MVVM Architecture](https://developer.android.com/topic/architecture)

### Ferramentas
- [Postman](https://www.postman.com/) - Testar API
- [DB Browser for SQLite](https://sqlitebrowser.org/) - Visualizar banco
- [Android Profiler](https://developer.android.com/studio/profile/android-profiler) - Performance

## 💡 Boas Práticas

### Segurança
- ✅ Sempre validar entrada do usuário
- ✅ Usar prepared statements
- ✅ Hash de senhas com bcrypt
- ✅ JWT para autenticação
- ✅ HTTPS em produção
- ❌ Nunca commitar senhas ou secrets
- ❌ Nunca logar informações sensíveis

### Performance
- ✅ Usar índices no banco de dados
- ✅ Limitar resultados de queries
- ✅ Cachear dados quando possível
- ✅ Lazy loading de imagens
- ✅ Pagination em listas grandes
- ❌ Evitar N+1 queries
- ❌ Não carregar dados desnecessários

### Código Limpo
- ✅ Nomes descritivos
- ✅ Funções pequenas e focadas
- ✅ DRY (Don't Repeat Yourself)
- ✅ Comentários quando necessário
- ✅ Formatação consistente
- ❌ Código comentado (delete!)
- ❌ Magic numbers (use constantes)

---

**Happy Coding!** 🚀

Mantenha este documento atualizado conforme o projeto evolui.

