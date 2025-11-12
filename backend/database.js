/**
 * Inicialização e configuração do banco de dados SQLite
 */

const Database = require('better-sqlite3');
const path = require('path');
const fs = require('fs');
const config = require('./config');

let db = null;

/**
 * Inicializa a conexão com o banco de dados
 */
function initDatabase() {
  return new Promise((resolve, reject) => {
    try {
      const dbPath = path.resolve(config.database.path);
      
      // Criar diretório do banco se não existir
      const dbDir = path.dirname(dbPath);
      if (!fs.existsSync(dbDir)) {
        fs.mkdirSync(dbDir, { recursive: true });
      }
      
      // Criar conexão com o banco
      db = new Database(dbPath, {
        verbose: config.database.verbose ? console.log : null
      });
      
      console.log('✅ Conectado ao banco de dados SQLite:', dbPath);
      
      // Habilitar foreign keys
      db.pragma('foreign_keys = ON');
      
      // Criar tabelas
      createTables();
      console.log('✅ Banco de dados inicializado com sucesso!');
      resolve(db);
      
    } catch (error) {
      console.error('❌ Erro ao conectar com o banco de dados:', error);
      reject(error);
    }
  });
}

/**
 * Cria as tabelas do banco de dados
 */
function createTables() {
  // Tabela de usuários
  db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      username TEXT UNIQUE NOT NULL,
      email TEXT UNIQUE NOT NULL,
      password_hash TEXT NOT NULL,
      full_name TEXT NOT NULL,
      phone TEXT,
      address TEXT,
      latitude REAL,
      longitude REAL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Tabela de denúncias
  db.exec(`
    CREATE TABLE IF NOT EXISTS reports (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      title TEXT NOT NULL,
      description TEXT NOT NULL,
      category TEXT NOT NULL,
      latitude REAL NOT NULL,
      longitude REAL NOT NULL,
      address TEXT,
      status TEXT DEFAULT 'pending',
      image_path TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    )
  `);

  // Tabela de grupos de bairro
  db.exec(`
    CREATE TABLE IF NOT EXISTS groups (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      description TEXT,
      created_by INTEGER NOT NULL,
      latitude REAL,
      longitude REAL,
      radius_meters INTEGER DEFAULT 1000,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
    )
  `);

  // Tabela de membros dos grupos
  db.exec(`
    CREATE TABLE IF NOT EXISTS group_members (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      group_id INTEGER NOT NULL,
      user_id INTEGER NOT NULL,
      role TEXT DEFAULT 'member',
      joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
      UNIQUE(group_id, user_id)
    )
  `);

  // Tabela de posts do feed
  db.exec(`
    CREATE TABLE IF NOT EXISTS feed_posts (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      group_id INTEGER,
      content TEXT NOT NULL,
      image_path TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
      FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
    )
  `);

  // Tabela de comentários
  db.exec(`
    CREATE TABLE IF NOT EXISTS comments (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      post_id INTEGER NOT NULL,
      user_id INTEGER NOT NULL,
      content TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (post_id) REFERENCES feed_posts(id) ON DELETE CASCADE,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    )
  `);
}

/**
 * Retorna a instância do banco de dados
 */
function getDatabase() {
  if (!db) {
    throw new Error('Banco de dados não foi inicializado. Chame initDatabase() primeiro.');
  }
  return db;
}

/**
 * Fecha a conexão com o banco de dados
 */
function closeDatabase() {
  return new Promise((resolve) => {
    if (!db) {
      resolve();
      return;
    }
    
    try {
      db.close();
      console.log('✅ Conexão com banco de dados fechada');
      db = null;
      resolve();
    } catch (error) {
      console.error('❌ Erro ao fechar banco de dados:', error);
      resolve(); // Resolve mesmo com erro
    }
  });
}

/**
 * Executa uma query SQL (INSERT, UPDATE, DELETE)
 */
function run(sql, params = []) {
  return new Promise((resolve, reject) => {
    try {
      const stmt = db.prepare(sql);
      const result = stmt.run(...params);
      resolve({ lastID: result.lastInsertRowid, changes: result.changes });
    } catch (error) {
      reject(error);
    }
  });
}

/**
 * Busca uma única linha
 */
function get(sql, params = []) {
  return new Promise((resolve, reject) => {
    try {
      const stmt = db.prepare(sql);
      const row = stmt.get(...params);
      resolve(row);
    } catch (error) {
      reject(error);
    }
  });
}

/**
 * Busca múltiplas linhas
 */
function all(sql, params = []) {
  return new Promise((resolve, reject) => {
    try {
      const stmt = db.prepare(sql);
      const rows = stmt.all(...params);
      resolve(rows);
    } catch (error) {
      reject(error);
    }
  });
}

module.exports = {
  initDatabase,
  getDatabase,
  closeDatabase,
  run,
  get,
  all
};

