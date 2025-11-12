# 🔄 Instalação Alternativa - Sem Compilação Nativa

Se você está tendo problemas com a instalação do `better-sqlite3` no Windows, aqui está uma solução alternativa usando um banco SQLite puro em JavaScript.

## 📦 Opção: sql.js (SQLite sem compilação)

`sql.js` é uma versão do SQLite compilada para WebAssembly/JavaScript, que funciona sem precisar de ferramentas de compilação nativas.

### Instalação

```bash
cd backend
npm uninstall better-sqlite3
npm install sql.js
```

### Modificar `package.json`

```json
{
  "dependencies": {
    "express": "^4.18.2",
    "cors": "^2.8.5",
    "body-parser": "^1.20.2",
    "sql.js": "^1.8.0",
    "bcryptjs": "^2.4.3",
    "jsonwebtoken": "^9.0.2",
    "multer": "^1.4.5-lts.1",
    "express-validator": "^7.0.1"
  }
}
```

### Modificar `database.js`

Substitua o conteúdo de `database.js` por:

```javascript
const initSqlJs = require('sql.js');
const fs = require('fs');
const path = require('path');
const config = require('./config');

let db = null;

async function initDatabase() {
  const SQL = await initSqlJs();
  const dbPath = path.resolve(config.database.path);
  
  // Carregar banco existente ou criar novo
  let buffer;
  if (fs.existsSync(dbPath)) {
    buffer = fs.readFileSync(dbPath);
    db = new SQL.Database(buffer);
  } else {
    db = new SQL.Database();
  }
  
  // Habilitar foreign keys
  db.run('PRAGMA foreign_keys = ON');
  
  // Criar tabelas
  createTables();
  
  // Salvar no disco
  saveDatabaseToFile();
  
  console.log('✅ Banco de dados inicializado');
  return db;
}

function createTables() {
  // ... mesmo código de criação de tabelas
}

function saveDatabaseToFile() {
  const dbPath = path.resolve(config.database.path);
  const data = db.export();
  const buffer = Buffer.from(data);
  fs.writeFileSync(dbPath, buffer);
}

function run(sql, params = []) {
  return new Promise((resolve) => {
    db.run(sql, params);
    saveDatabaseToFile();
    resolve({ lastID: db.exec('SELECT last_insert_rowid()')[0].values[0][0] });
  });
}

function get(sql, params = []) {
  return new Promise((resolve) => {
    const result = db.exec(sql, params);
    resolve(result[0] ? result[0].values[0] : null);
  });
}

function all(sql, params = []) {
  return new Promise((resolve) => {
    const result = db.exec(sql, params);
    resolve(result[0] ? result[0].values : []);
  });
}

module.exports = {
  initDatabase,
  getDatabase: () => db,
  closeDatabase: async () => {
    saveDatabaseToFile();
    db.close();
  },
  run,
  get,
  all
};
```

## ⚡ Vantagens

- ✅ Não requer compilação nativa
- ✅ Funciona em qualquer sistema operacional
- ✅ Não precisa de Visual Studio Build Tools
- ✅ Instalação rápida

## ⚠️ Desvantagens

- ❌ Performance um pouco menor que better-sqlite3
- ❌ Arquivo de banco em memória (precisa salvar manualmente)
- ❌ Menos eficiente para grandes volumes de dados

## 🎯 Para Produção

Para ambiente de produção, recomendamos:
1. Usar `better-sqlite3` (melhor performance)
2. Instalar no Linux/WSL2 (evita problemas de compilação)
3. Ou usar Docker

---

**Esta é uma solução temporária. Para melhor performance, instale as Build Tools conforme [INSTALL_WINDOWS.md](./INSTALL_WINDOWS.md)**

