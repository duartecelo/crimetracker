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
  console.log('📦 Criando tabelas do banco de dados...');

  // Tabela de usuários
  // Campos: id, email, senha (password_hash), username, created_at
  db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id TEXT PRIMARY KEY,
      email TEXT UNIQUE NOT NULL,
      password_hash TEXT NOT NULL,
      username TEXT UNIQUE NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);
  console.log('  ✓ Tabela users criada');

  // Tabela de denúncias de crimes
  // Campos: id, user_id, tipo, descrição, lat, lon, datas (created_at, updated_at)
  db.exec(`
    CREATE TABLE IF NOT EXISTS crime_reports (
      id TEXT PRIMARY KEY,
      user_id TEXT NOT NULL,
      tipo TEXT NOT NULL,
      descricao TEXT NOT NULL,
      lat REAL NOT NULL,
      lon REAL NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    )
  `);
  console.log('  ✓ Tabela crime_reports criada');

  // Tabela de grupos de bairro
  // Campos: id, nome, descrição, criador (creator_id), data (created_at)
  db.exec(`
    CREATE TABLE IF NOT EXISTS groups (
      id TEXT PRIMARY KEY,
      nome TEXT NOT NULL,
      descricao TEXT,
      criador TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (criador) REFERENCES users(id) ON DELETE CASCADE
    )
  `);
  console.log('  ✓ Tabela groups criada');

  // Tabela de membros dos grupos
  // Campos: id de grupo (group_id) e id de usuário (user_id), joined_at
  db.exec(`
    CREATE TABLE IF NOT EXISTS group_members (
      group_id TEXT NOT NULL,
      user_id TEXT NOT NULL,
      joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      PRIMARY KEY (group_id, user_id),
      FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    )
  `);
  console.log('  ✓ Tabela group_members criada');

  // Tabela de posts do feed
  // Campos: id, grupo (group_id), autor (author_id), conteúdo, created_at
  db.exec(`
    CREATE TABLE IF NOT EXISTS posts (
      id TEXT PRIMARY KEY,
      group_id TEXT,
      author_id TEXT NOT NULL,
      conteudo TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
      FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
    )
  `);
  console.log('  ✓ Tabela posts criada');

  // Criar índices para melhorar performance
  db.exec(`
    CREATE INDEX IF NOT EXISTS idx_crime_reports_user_id ON crime_reports(user_id);
    CREATE INDEX IF NOT EXISTS idx_crime_reports_tipo ON crime_reports(tipo);
    CREATE INDEX IF NOT EXISTS idx_crime_reports_location ON crime_reports(lat, lon);
    CREATE INDEX IF NOT EXISTS idx_groups_criador ON groups(criador);
    CREATE INDEX IF NOT EXISTS idx_posts_group_id ON posts(group_id);
    CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts(author_id);
  `);
  console.log('  ✓ Índices criados');

  console.log('✅ Todas as tabelas foram criadas com sucesso!');
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

