/**
 * Serviço de autenticação - AUTH-001
 */

const db = require('../database');
const { 
  hashPassword, 
  comparePassword, 
  generateToken,
  generateUUID,
  isValidEmail,
  validatePassword
} = require('../utils');

/**
 * Registra um novo usuário
 * @param {string} email - Email do usuário
 * @param {string} password - Senha do usuário
 * @param {string} username - Nome de usuário
 */
async function registerUser(email, password, username) {
  const startTime = Date.now();

  // Validar email
  if (!isValidEmail(email)) {
    throw new Error('Email inválido');
  }

  // Validar senha (≥8 caracteres)
  const passwordValidation = validatePassword(password);
  if (!passwordValidation.valid) {
    throw new Error(passwordValidation.message);
  }

  // Normalizar dados
  const normalizedEmail = email.toLowerCase().trim();
  const normalizedUsername = username.trim();

  // Verificar se email já existe
  const existingEmail = await db.get(
    'SELECT id FROM users WHERE email = ?',
    [normalizedEmail]
  );

  if (existingEmail) {
    throw new Error('Email já cadastrado');
  }

  // Verificar se username já existe
  const existingUsername = await db.get(
    'SELECT id FROM users WHERE username = ?',
    [normalizedUsername]
  );

  if (existingUsername) {
    throw new Error('Nome de usuário já cadastrado');
  }

  // Gerar UUID para o usuário
  const userId = generateUUID();

  // Hash da senha com bcryptjs
  const password_hash = await hashPassword(password);

  // Inserir usuário no banco
  await db.run(
    'INSERT INTO users (id, username, email, password_hash) VALUES (?, ?, ?, ?)',
    [userId, normalizedUsername, normalizedEmail, password_hash]
  );

  // Gerar token JWT (24h)
  const token = generateToken({
    user_id: userId,
    username: normalizedUsername,
    email: normalizedEmail
  });

  const duration = Date.now() - startTime;
  console.log(`✅ Registro em ${duration}ms`);

  return {
    success: true,
    user_id: userId,
    username: normalizedUsername,
    email: normalizedEmail,
    token: token
  };
}

/**
 * Realiza login de usuário
 * @param {string} email - Email do usuário
 * @param {string} password - Senha do usuário
 */
async function loginUser(email, password) {
  const startTime = Date.now();

  // Normalizar email
  const normalizedEmail = email.toLowerCase().trim();

  // Buscar usuário por email
  const user = await db.get(
    'SELECT * FROM users WHERE email = ?',
    [normalizedEmail]
  );

  if (!user) {
    throw new Error('Email ou senha incorretos');
  }

  // Verificar senha
  const validPassword = await comparePassword(password, user.password_hash);

  if (!validPassword) {
    throw new Error('Email ou senha incorretos');
  }

  // Gerar token JWT (24h)
  const token = generateToken({
    user_id: user.id,
    username: user.username,
    email: user.email
  });

  const duration = Date.now() - startTime;
  console.log(`✅ Login em ${duration}ms`);

  return {
    success: true,
    user_id: user.id,
    username: user.username,
    email: user.email,
    token: token
  };
}

module.exports = {
  registerUser,
  loginUser
};
