# GitHub Actions - Automação de Testes

## 📚 O que foi configurado?

Foi criado um workflow do GitHub Actions que executa automaticamente os testes unitários sempre que houver:
- ✅ Push (commit) na branch `main`
- ✅ Pull Request para a branch `main`
- ✅ Execução manual através da interface do GitHub

## 📁 Estrutura de Arquivos

```
.github/
└── workflows/
    └── tests.yml    # Configuração do workflow de testes
```

## 🔧 Como Funciona?

### 1. **Triggers (Gatilhos)**

```yaml
on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:
```

- **push**: Executa quando você faz commit na branch main
- **pull_request**: Executa quando cria/atualiza um PR para main
- **workflow_dispatch**: Permite executar manualmente

### 2. **Ambiente de Execução**

```yaml
runs-on: ubuntu-latest
```

O workflow roda em um servidor Ubuntu (Linux) fornecido gratuitamente pelo GitHub.

### 3. **Matriz de Versões**

```yaml
strategy:
  matrix:
    node-version: [18.x, 20.x]
```

Os testes são executados em **duas versões do Node.js** (18 e 20) para garantir compatibilidade.

### 4. **Passos do Workflow**

1. **Checkout**: Baixa o código do repositório
2. **Setup Node**: Instala o Node.js
3. **Install**: Instala as dependências (`npm ci`)
4. **Test**: Executa os testes (`npm test`)
5. **Coverage**: Gera relatório de cobertura (`npm run test:coverage`)
6. **Upload**: Salva o relatório como artefato
7. **Summary**: Mostra resumo na interface do GitHub

## 🚀 Como Ativar no GitHub

### Passo 1: Enviar os arquivos para o GitHub

```powershell
# Adicionar os arquivos
git add .github/

# Commit
git commit -m "ci: adicionar GitHub Actions para testes automatizados"

# Push para o repositório
git push origin main
```

### Passo 2: Verificar a execução

1. Vá até seu repositório no GitHub
2. Clique na aba **"Actions"**
3. Você verá o workflow "Testes Unitários" em execução
4. Clique nele para ver os detalhes

## 📊 O que acontece após o push?

```
┌─────────────────────────────────────┐
│  Você faz git push origin main      │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  GitHub detecta o commit            │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Inicia o workflow automaticamente  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Executa em paralelo:               │
│  • Node.js 18.x                     │
│  • Node.js 20.x                     │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Para cada versão:                  │
│  1. Instala dependências            │
│  2. Executa npm test                │
│  3. Gera relatório de cobertura     │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│  Resultados aparecem no GitHub      │
│  ✅ Sucesso ou ❌ Falha             │
└─────────────────────────────────────┘
```

## 🎯 Visualizando Resultados

### Na aba Actions:
- ✅ **Check verde**: Todos os testes passaram
- ❌ **X vermelho**: Algum teste falhou
- 🟡 **Círculo amarelo**: Em execução

### Nos Pull Requests:
- Status dos testes aparece automaticamente
- Você pode configurar para **bloquear merge** se os testes falharem

### Artefatos Gerados:
- Relatório de cobertura fica disponível para download por 30 dias
- Acesse em: Actions > Workflow Run > Artifacts

## ⚙️ Configurações Avançadas (Opcionais)

### 1. Bloquear merge se testes falharem

No GitHub:
1. Vá em **Settings** > **Branches**
2. Em **Branch protection rules**, adicione regra para `main`
3. Marque: ✅ **Require status checks to pass before merging**
4. Selecione: ✅ **Executar Testes**

### 2. Notificações

Por padrão, você receberá email se:
- Os testes falharem em um commit seu
- Um workflow falhar

Configure em: **Settings** > **Notifications** > **Actions**

### 3. Badge no README

Adicione um badge mostrando o status dos testes:

```markdown
![Testes](https://github.com/duartecelo/crimetracker/workflows/Testes%20Unitários/badge.svg)
```

Isso mostra: ![Passing](https://img.shields.io/badge/tests-passing-brightgreen)

## 💰 Custos

GitHub Actions é **GRATUITO** para repositórios públicos!

Para repositórios privados:
- 2.000 minutos/mês grátis
- Seu workflow usa ~2-3 minutos por execução
- Isso dá ~600-1000 execuções grátis/mês

## 🔍 Logs e Debugging

Se um teste falhar:

1. Vá em **Actions** > Clique no workflow falhado
2. Clique no job "Executar Testes"
3. Expanda o passo que falhou
4. Veja o log completo do erro

Exemplo de log:
```
Run npm test
 FAIL  test/unit/services/authService.test.js
  ● AuthService - loginUser › deve fazer login com sucesso
    
    Email ou senha incorretos
```

## 📝 Customizações Comuns

### Executar apenas em commits específicos

```yaml
on:
  push:
    branches: [ main ]
    paths:
      - 'services/**'
      - 'test/**'
      - 'package.json'
```

### Adicionar mais jobs

```yaml
jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
      - run: npm ci
      - run: npm run lint

  test:
    runs-on: ubuntu-latest
    # ... resto do job
```

### Usar cache mais agressivo

```yaml
- name: Cache node_modules
  uses: actions/cache@v3
  with:
    path: node_modules
    key: ${{ runner.os }}-node-${{ hashFiles('package-lock.json') }}
```

## 🎓 Comandos Úteis

```powershell
# Ver status do último workflow
gh run list --limit 1

# Ver logs do último workflow
gh run view --log

# Re-executar workflow falhado
gh run rerun

# Listar todos os workflows
gh workflow list
```

## ✅ Checklist de Ativação

- [ ] Arquivos criados em `.github/workflows/`
- [ ] Commit feito com os arquivos
- [ ] Push para o GitHub realizado
- [ ] Verificado execução na aba Actions
- [ ] Configurado proteção de branch (opcional)
- [ ] Badge adicionado ao README (opcional)

## 🆘 Troubleshooting

### Erro: "npm ci can only install packages when package-lock.json is present"

**Solução**: Use `npm install` ao invés de `npm ci`, ou gere o package-lock.json:
```yaml
- run: npm install
```

### Erro: Testes passam localmente mas falham no GitHub

**Causas comuns**:
- Diferença de timezone (use UTC nos testes)
- Dependências de sistema operacional
- Variáveis de ambiente faltando

**Solução**: Use variáveis de ambiente:
```yaml
env:
  NODE_ENV: test
  TZ: America/Sao_Paulo
```

### Workflow não está executando

**Verificar**:
1. Arquivo está em `.github/workflows/` (com ponto)
2. Extensão é `.yml` ou `.yaml`
3. Sintaxe YAML está correta (use validator online)

## 🔗 Recursos Úteis

- [Documentação GitHub Actions](https://docs.github.com/actions)
- [Marketplace de Actions](https://github.com/marketplace?type=actions)
- [Validador YAML](https://www.yamllint.com/)
- [GitHub Actions Workflow Syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)

---

**Pronto!** Assim que você fizer o push, os testes rodarão automaticamente em cada commit na branch main! 🚀
