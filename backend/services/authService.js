/**
 * Serviço de autenticação
 */

const db = require('../database');
const { hashPassword, comparePassword, generateToken } = require('../utils');

/**
 * Registra um novo usuário
 */
async function registerUser(userData) {
  const { username, email, password, full_name, phone, address, latitude, longitude } = userData;

  // Verificar se usuário já existe
  const existingUser = await db.get(
    'SELECT id FROM users WHERE username = ? OR email = ?',
    [username, email]
  );

  if (existingUser) {
    throw new Error('Usuário ou email já existe');
  }

  // Hash da senha
  const password_hash = await hashPassword(password);

  // Inserir usuário
  const result = await db.run(
    `INSERT INTO users (username, email, password_hash, full_name, phone, address, latitude, longitude)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
    [username, email, password_hash, full_name, phone || null, address || null, latitude || null, longitude || null]
  );

  // Gerar token JWT
  const token = generateToken({
    userId: result.lastID,
    username
  });

  return {
    token,
    user: {
      id: result.lastID,
      username,
      email,
      full_name
    }
  };
}

/**
 * Realiza login de usuário
 */
async function loginUser(username, password) {
  // Buscar usuário por username ou email
  const user = await db.get(
    'SELECT * FROM users WHERE username = ? OR email = ?',
    [username, username]
  );

  if (!user) {
    throw new Error('Credenciais inválidas');
  }

  // Verificar senha
  const validPassword = await comparePassword(password, user.password_hash);

  if (!validPassword) {
    throw new Error('Credenciais inválidas');
  }

  // Gerar token JWT
  const token = generateToken({
    userId: user.id,
    username: user.username
  });

  return {
    token,
    user: {
      id: user.id,
      username: user.username,
      email: user.email,
      full_name: user.full_name
    }
  };
}

/**
 * Busca perfil de usuário
 */
async function getUserProfile(userId) {
  const user = await db.get(
    `SELECT id, username, email, full_name, phone, address, latitude, longitude, created_at
     FROM users WHERE id = ?`,
    [userId]
  );

  if (!user) {
    throw new Error('Usuário não encontrado');
  }

  return user;
}

module.exports = {
  registerUser,
  loginUser,
  getUserProfile
};

