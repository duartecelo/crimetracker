# 🧪 Guia de Testes - CrimeTracker Android

## 📋 Pré-requisitos

### **Backend Rodando**

```bash
cd backend
npm run dev
```

Aguarde:
```
✅ Pronto para receber requisições!
🌐 http://localhost:3000
```

### **Android App**

```bash
# Abrir Android Studio
# File > Open > android/
# Aguardar sync do Gradle
# Run (▶️)
```

---

## ✅ Checklist de Testes Manuais

### **1. Fluxo de Autenticação** 🔐

#### **Teste 1.1: Registro**
1. Abrir app → Splash Screen (1.5s)
2. Tela de Login aparece
3. Clicar em "Não tem uma conta? Cadastre-se"
4. Preencher:
   - Username: `testefinal`
   - Email: `testefinal@exemplo.com`
   - Senha: `senha12345678`
5. Clicar em "Cadastrar"

**✅ Resultado esperado:**
- Loading aparece no botão
- Snackbar: "Cadastro realizado com sucesso!"
- Navega para HomeScreen automaticamente
- **Tempo:** < 2s

#### **Teste 1.2: Logout e Login**
1. Na HomeScreen, menu (⋮) → "Sair"
2. Volta para LoginScreen
3. Preencher:
   - Email: `testefinal@exemplo.com`
   - Senha: `senha12345678`
4. Clicar em "Entrar"

**✅ Resultado esperado:**
- Loading no botão
- Snackbar: "Login realizado com sucesso!"
- Navega para HomeScreen
- **Tempo:** < 2s

#### **Teste 1.3: Sessão Persistente**
1. Com app autenticado, fechar app (swipe up)
2. Reabrir app

**✅ Resultado esperado:**
- Splash Screen → diretamente para HomeScreen
- Token JWT ainda válido
- Sem necessidade de fazer login novamente

---

### **2. Fluxo de Denúncias** 🚨

#### **Teste 2.1: Criar Denúncia**
1. Na HomeScreen, ir para aba "Denúncias"
2. Clicar no FAB (+)
3. Preencher:
   - Tipo: "Assalto"
   - Descrição: "Teste de denúncia automática. Validando integração com backend."
4. Clicar em "Reportar"
5. Se solicitar permissão de localização → Permitir

**✅ Resultado esperado:**
- Loading no botão
- Snackbar: "Denúncia criada com sucesso!"
- Volta para aba Denúncias
- **Tempo:** < 3s

#### **Teste 2.2: Listar Denúncias Próximas**
1. Na aba Denúncias
2. Verificar se a denúncia criada aparece

**✅ Resultado esperado:**
- Lista mostra denúncias (ou mensagem vazia)
- Se há denúncias → cards com tipo, descrição, distância
- **Tempo:** < 3s

#### **Teste 2.3: Validação de Campos**
1. Aba Denúncias → FAB (+)
2. Deixar descrição em branco
3. Tentar reportar

**✅ Resultado esperado:**
- Botão "Reportar" desabilitado
- Ou Snackbar: "Descrição é obrigatória"

#### **Teste 2.4: Limite de Caracteres**
1. Digitar mais de 500 caracteres

**✅ Resultado esperado:**
- Não permite digitar além de 500
- Contador mostra "500/500"

---

### **3. Fluxo de Grupos** 👥

#### **Teste 3.1: Criar Grupo**
1. Aba "Grupos" → FAB (+)
2. Preencher:
   - Nome: "Bairro Teste Final"
   - Descrição: "Grupo para validar integração completa"
3. Clicar em "Criar Grupo"

**✅ Resultado esperado:**
- Loading no botão
- Snackbar: "Grupo criado com sucesso!"
- Volta para aba Grupos
- Grupo aparece na lista "Meus Grupos"
- **Tempo:** < 1s

#### **Teste 3.2: Nome Duplicado**
1. Tentar criar outro grupo com nome "Bairro Teste Final"

**✅ Resultado esperado:**
- Snackbar: "Já existe um grupo com este nome"
- Não cria o grupo

#### **Teste 3.3: Buscar Grupos**
1. Na aba Grupos, barra de busca (se implementada)
2. Digitar "Teste"

**✅ Resultado esperado:**
- Filtra grupos com "Teste" no nome
- **Tempo:** < 1s

---

### **4. Fluxo de Posts** 📰

#### **Teste 4.1: Criar Post**
1. Aba "Feed"
2. Clicar em um grupo (se houver botão)
3. Ou navegar para CreatePostScreen passando groupId
4. Digitar: "Post de teste para validar feed completo!"
5. Clicar em "Publicar"

**✅ Resultado esperado:**
- Loading no botão
- Snackbar: "Post publicado!"
- Volta para feed
- Post aparece no feed
- **Tempo:** < 2s

#### **Teste 4.2: Validação de Conteúdo**
1. Tentar publicar post vazio

**✅ Resultado esperado:**
- Botão desabilitado

2. Digitar mais de 1000 caracteres

**✅ Resultado esperado:**
- Não permite além de 1000
- Contador: "1000/1000"

#### **Teste 4.3: Ver Feed**
1. Aba "Feed"

**✅ Resultado esperado:**
- Lista de posts dos grupos do usuário
- Ordenados DESC (mais recentes primeiro)
- Mostra: autor, conteúdo, data, nome do grupo
- **Tempo:** < 2s

#### **Teste 4.4: Deletar Post (Autor)**
1. No feed, localizar seu próprio post
2. Menu (⋮) ou botão "Deletar"

**✅ Resultado esperado:**
- Snackbar: "Post deletado"
- Post removido da lista
- **Tempo:** < 1s

#### **Teste 4.5: Deletar Post (Não-Autor)**
1. Tentar deletar post de outro usuário

**✅ Resultado esperado:**
- Botão "Deletar" não aparece
- Ou Snackbar: "Apenas o autor pode deletar este post"

---

## 🌐 Testes de Conectividade

### **Teste 5: Modo Offline (Cache)**

#### **5.1: Criar Dados Online**
1. Com internet, fazer login
2. Criar denúncia
3. Criar grupo
4. Criar post

#### **5.2: Desconectar e Testar Cache**
1. Ativar modo avião no emulador
2. Navegar pelas abas

**✅ Resultado esperado:**
- Denúncias, grupos e posts ainda aparecem (cache)
- Snackbar: "Erro de conexão" ao tentar criar novos
- Dados locais acessíveis

#### **5.3: Reconectar**
1. Desativar modo avião
2. Pull-to-refresh (se implementado)

**✅ Resultado esperado:**
- Dados sincronizam com backend
- Novos dados aparecem

---

## ⚠️ Testes de Erros HTTP

### **Teste 6: Erros de Autenticação**

#### **6.1: Login com Credenciais Inválidas**
1. LoginScreen
2. Email: `invalido@exemplo.com`
3. Senha: `errado123`

**✅ Resultado esperado:**
- Snackbar: "Email ou senha incorretos"
- **Código HTTP:** 401

#### **6.2: Email Já Cadastrado**
1. Tentar registrar com email existente

**✅ Resultado esperado:**
- Snackbar: "Email já cadastrado"
- **Código HTTP:** 409

#### **6.3: Senha Curta**
1. Tentar registrar com senha "123"

**✅ Resultado esperado:**
- Snackbar: "A senha deve ter pelo menos 8 caracteres"
- Validação local (não chama API)

### **Teste 7: Erros de Permissão**

#### **7.1: Postar em Grupo Sem Ser Membro**
1. Tentar criar post em grupo que não é membro

**✅ Resultado esperado:**
- Snackbar: "Você não é membro deste grupo"
- **Código HTTP:** 403

### **Teste 8: Servidor Offline**

#### **8.1: Backend Parado**
1. Parar backend (Ctrl+C no terminal)
2. Tentar criar denúncia

**✅ Resultado esperado:**
- Snackbar: "Erro de conexão: ..."
- Dados em cache ainda acessíveis

---

## ⏱️ Testes de Performance

### **Métricas de Referência:**

| Ação | Meta | Backend | App (total) |
|------|------|---------|-------------|
| Login | < 2s | ~100ms | ~150ms |
| Registro | < 2s | ~150ms | ~200ms |
| Criar denúncia | < 3s | ~120ms | ~180ms |
| Listar denúncias | < 3s | ~85ms | ~130ms |
| Criar grupo | < 1s | ~85ms | ~120ms |
| Criar post | < 2s | ~95ms | ~140ms |
| Listar posts | < 2s | ~75ms | ~110ms |

### **Como Medir:**

1. **Logs do Backend:** Já mostram tempo de cada operação
2. **Logcat do Android:** 
   ```
   TAG: ReportViewModel | Time: 142ms
   ```

**✅ Todas as operações devem ter loading visible**
**✅ Tempo total (app) deve respeitar metas**

---

## 📝 Casos de Uso Completos

### **Caso 1: Primeiro Uso**

```
1. Abrir app
2. Splash → LoginScreen
3. Cadastrar novo usuário
4. HomeScreen (Feed vazio)
5. Criar denúncia
6. Criar grupo
7. Criar post no grupo
8. Ver feed atualizado
```

**Tempo total:** ~15 segundos

---

### **Caso 2: Uso Diário**

```
1. Abrir app
2. Splash → HomeScreen (token válido)
3. Ver feed de posts
4. Ver denúncias próximas
5. Criar novo post
6. Logout
```

**Tempo total:** ~10 segundos

---

### **Caso 3: Interação Social**

```
1. Criar grupo "Vigilância Noturna"
2. Outro usuário entra no grupo
3. Publicar alerta: "Movimentação suspeita na rua X"
4. Outro usuário vê no feed
5. Outro usuário comenta (se implementado)
```

**Colaboração em tempo real**

---

## 🎯 Checklist Final

### **Funcionalidades Core** ✅
- [ ] Registro funciona
- [ ] Login funciona
- [ ] Token persiste após reiniciar app
- [ ] Logout limpa sessão
- [ ] Denúncias são criadas
- [ ] Denúncias aparecem na lista
- [ ] Grupos são criados
- [ ] Posts são criados
- [ ] Feed é exibido
- [ ] Autor pode deletar próprio post

### **UI/UX** ✅
- [ ] Loading aparece em todas as ações
- [ ] Snackbar mostra erros
- [ ] Snackbar mostra sucessos
- [ ] Botões desabilitam durante loading
- [ ] Validações impedem dados inválidos
- [ ] Navegação fluida entre telas

### **Performance** ✅
- [ ] Login < 2s
- [ ] Registro < 2s
- [ ] Criar denúncia < 3s
- [ ] Criar grupo < 1s
- [ ] Criar post < 2s
- [ ] Listar dados < 2s

### **Offline/Cache** ✅
- [ ] Dados em cache acessíveis offline
- [ ] Snackbar informa erro de conexão
- [ ] Sincroniza ao reconectar

### **Erros HTTP** ✅
- [ ] 401 → "Sessão expirada"
- [ ] 403 → "Sem permissão"
- [ ] 404 → "Não encontrado"
- [ ] 409 → "Já existe"
- [ ] 500 → "Erro no servidor"

---

## 📊 Relatório de Teste

### **Template:**

```markdown
# Relatório de Teste - CrimeTracker

**Data:** [DATA]
**Testador:** [NOME]
**Build:** v1.0.0

## Resultados

### Autenticação
- ✅ Registro: OK (tempo: 180ms)
- ✅ Login: OK (tempo: 120ms)
- ✅ Sessão persistente: OK

### Denúncias
- ✅ Criar: OK (tempo: 150ms)
- ✅ Listar: OK (tempo: 95ms)
- ✅ Validação: OK

### Grupos
- ✅ Criar: OK (tempo: 110ms)
- ✅ Buscar: OK
- ✅ Nome duplicado: OK (erro exibido)

### Posts
- ✅ Criar: OK (tempo: 130ms)
- ✅ Feed: OK (tempo: 100ms)
- ✅ Deletar (autor): OK
- ✅ Deletar (não-autor): Bloqueado

### Performance
- ✅ Todas as operações < metas
- ✅ Loading sempre visível

### Erros
- ✅ 401, 403, 404, 409 tratados
- ✅ Offline com cache funciona

## Problemas Encontrados
- Nenhum

## Conclusão
✅ Sistema 100% funcional e pronto para produção!
```

---

## 🎉 Resumo

```
╔════════════════════════════════════════════════╗
║                                                ║
║     🧪 GUIA DE TESTES COMPLETO                 ║
║                                                ║
║  ✅ 8 seções de testes                         ║
║  ✅ 20+ casos de teste                         ║
║  ✅ Validação de erros HTTP                    ║
║  ✅ Testes de performance                      ║
║  ✅ Testes offline/cache                       ║
║  ✅ Checklist final                            ║
║                                                ║
║  ✨ PRONTO PARA VALIDAÇÃO!                     ║
║                                                ║
╚════════════════════════════════════════════════╝
```

**Execute os testes e valide todas as funcionalidades! 🚀**

**Tempo estimado:** 30-45 minutos

