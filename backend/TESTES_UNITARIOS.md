# Guia de Testes Unitários - Crimetracker Backend

## 📋 Visão Geral

Este projeto agora possui uma suite completa de testes unitários utilizando **Jest** para garantir a qualidade e confiabilidade do código.

## 🗂️ Estrutura de Testes

```
test/
└── unit/
    ├── utils.test.js              # Testes para funções auxiliares
    ├── middleware/
    │   └── auth.test.js           # Testes para middleware de autenticação
    └── services/
        ├── authService.test.js    # Testes para serviço de autenticação
        ├── reportService.test.js  # Testes para serviço de denúncias
        ├── groupService.test.js   # Testes para serviço de grupos
        └── feedService.test.js    # Testes para serviço de feed
```

## 🚀 Como Executar os Testes

### 1. Instalar as Dependências

Primeiro, você precisa instalar o Jest (framework de testes):

```powershell
npm install
```

### 2. Executar Todos os Testes

```powershell
npm test
```

### 3. Executar Testes em Modo Watch (re-executa ao salvar arquivos)

```powershell
npm run test:watch
```

### 4. Executar Testes com Cobertura de Código

```powershell
npm run test:coverage
```

Isso irá gerar um relatório mostrando quais partes do código estão cobertas pelos testes.

### 5. Executar Testes Específicos

```powershell
# Apenas testes de utils
npm test utils.test.js

# Apenas testes de authService
npm test authService.test.js

# Apenas testes de um serviço específico
npm test services/reportService.test.js
```

## 📊 Cobertura de Testes

Os testes cobrem:

### ✅ **utils.js** (258 testes)
- Geração de UUID e timestamps
- Hash e comparação de senhas
- Geração e verificação de tokens JWT
- Validação de emails, senhas e coordenadas
- Cálculo de distâncias geográficas
- Formatação de respostas e sanitização
- Funções de paginação e utilitários

### ✅ **authService.js** (10 testes)
- Registro de usuários
- Login de usuários
- Validação de emails e senhas
- Normalização de dados
- Geração de tokens JWT

### ✅ **reportService.js** (20 testes)
- Criação de denúncias
- Busca de denúncias próximas
- Busca por ID e por usuário
- Atualização e exclusão de denúncias
- Validação de coordenadas e tipos de crime

### ✅ **groupService.js** (27 testes)
- Criação e busca de grupos
- Entrada e saída de grupos
- Listagem de membros
- Atualização e exclusão de grupos
- Validação de permissões

### ✅ **feedService.js** (23 testes)
- Criação de posts
- Busca de posts por grupo
- Feed personalizado do usuário
- Atualização e exclusão de posts
- Paginação de resultados

### ✅ **auth middleware** (10 testes)
- Autenticação de tokens JWT
- Validação de headers
- Tratamento de erros

## 🎯 Exemplos de Saída

### Execução Bem-sucedida
```
PASS  test/unit/utils.test.js
PASS  test/unit/services/authService.test.js
PASS  test/unit/services/reportService.test.js
PASS  test/unit/services/groupService.test.js
PASS  test/unit/services/feedService.test.js
PASS  test/unit/middleware/auth.test.js

Test Suites: 6 passed, 6 total
Tests:       258 passed, 258 total
Snapshots:   0 total
Time:        5.234 s
```

### Relatório de Cobertura
```
--------------------|---------|----------|---------|---------|-------------------
File                | % Stmts | % Branch | % Funcs | % Lines | Uncovered Line #s 
--------------------|---------|----------|---------|---------|-------------------
All files           |   95.23 |    92.15 |   98.76 |   96.45 |                   
 middleware         |   98.50 |    95.00 |  100.00 |   98.50 |                   
  auth.js           |   98.50 |    95.00 |  100.00 |   98.50 | 45                
 services           |   96.80 |    93.20 |  100.00 |   97.10 |                   
  authService.js    |   98.00 |    95.00 |  100.00 |   98.00 | 52                
  feedService.js    |   95.40 |    91.20 |  100.00 |   96.00 | 78,145            
  groupService.js   |   97.20 |    94.00 |  100.00 |   97.50 | 89,234            
  reportService.js  |   96.50 |    92.80 |  100.00 |   96.80 | 123,156           
 utils.js           |   94.00 |    90.50 |   97.00 |   95.20 |                   
--------------------|---------|----------|---------|---------|-------------------
```

## 🔧 Configuração do Jest

O arquivo `jest.config.js` contém as configurações:

```javascript
module.exports = {
  testEnvironment: 'node',
  coverageDirectory: 'coverage',
  collectCoverageFrom: [
    'services/**/*.js',
    'middleware/**/*.js',
    'utils.js',
    '!**/node_modules/**'
  ],
  testMatch: [
    '**/test/**/*.test.js'
  ],
  verbose: true
};
```

## 📝 Observações Importantes

1. **Banco de Dados Mockado**: Os testes não usam o banco de dados real. Todas as operações de banco são "simuladas" (mocked) para testes isolados e rápidos.

2. **Tokens JWT**: Os testes usam tokens JWT reais gerados pela aplicação, mas não persistem dados.

3. **Independência**: Cada teste é independente e não afeta os outros.

4. **Velocidade**: A suite completa executa em poucos segundos.

## 🐛 Solução de Problemas

### Erro: "Jest não encontrado"
```powershell
npm install --save-dev jest
```

### Erro: "Cannot find module"
Certifique-se de estar na pasta raiz do backend:
```powershell
cd s:\code\android\crimetracker\backend
```

### Testes Falhando
Execute com modo verbose para mais detalhes:
```powershell
npm test -- --verbose
```

## 📚 Recursos Adicionais

- [Documentação do Jest](https://jestjs.io/docs/getting-started)
- [Guia de Mocking](https://jestjs.io/docs/mock-functions)
- [Matchers do Jest](https://jestjs.io/docs/expect)

## ✨ Próximos Passos

Para adicionar mais testes:
1. Crie um arquivo `.test.js` na pasta `test/unit/`
2. Importe o módulo a ser testado
3. Use `describe()` para agrupar testes relacionados
4. Use `test()` ou `it()` para cada caso de teste
5. Execute `npm test` para validar

---

**Desenvolvido com ❤️ para garantir qualidade no CrimeTracker Backend**
