/**
 * Funções auxiliares do CrimeTracker
 */

const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const config = require('./config');

/**
 * Hash de senha usando bcrypt
 * @param {string} password - Senha em texto plano
 * @returns {Promise<string>} Hash da senha
 */
async function hashPassword(password) {
  return bcrypt.hash(password, config.security.bcryptRounds);
}

/**
 * Compara senha com hash
 * @param {string} password - Senha em texto plano
 * @param {string} hash - Hash da senha
 * @returns {Promise<boolean>} True se a senha corresponde
 */
async function comparePassword(password, hash) {
  return bcrypt.compare(password, hash);
}

/**
 * Gera token JWT
 * @param {Object} payload - Dados a serem incluídos no token
 * @returns {string} Token JWT
 */
function generateToken(payload) {
  return jwt.sign(payload, config.jwt.secret, {
    expiresIn: config.jwt.expiresIn,
    algorithm: config.jwt.algorithm
  });
}

/**
 * Verifica e decodifica token JWT
 * @param {string} token - Token JWT
 * @returns {Object} Payload decodificado
 */
function verifyToken(token) {
  try {
    return jwt.verify(token, config.jwt.secret, {
      algorithms: [config.jwt.algorithm]
    });
  } catch (error) {
    throw new Error('Token inválido ou expirado');
  }
}

/**
 * Formata resposta de sucesso
 * @param {*} data - Dados a retornar
 * @param {string} message - Mensagem opcional
 * @returns {Object} Resposta formatada
 */
function successResponse(data, message = null) {
  const response = { success: true };
  
  if (message) {
    response.message = message;
  }
  
  if (data !== undefined && data !== null) {
    response.data = data;
  }
  
  return response;
}

/**
 * Formata resposta de erro
 * @param {string} message - Mensagem de erro
 * @param {*} details - Detalhes adicionais do erro
 * @returns {Object} Resposta formatada
 */
function errorResponse(message, details = null) {
  const response = {
    success: false,
    error: message
  };
  
  if (details) {
    response.details = details;
  }
  
  return response;
}

/**
 * Valida email
 * @param {string} email - Email a validar
 * @returns {boolean} True se o email é válido
 */
function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Valida senha
 * @param {string} password - Senha a validar
 * @returns {Object} {valid: boolean, message: string}
 */
function validatePassword(password) {
  if (!password || password.length < 6) {
    return {
      valid: false,
      message: 'Senha deve ter no mínimo 6 caracteres'
    };
  }
  
  return { valid: true };
}

/**
 * Sanitiza string (remove caracteres perigosos)
 * @param {string} str - String a sanitizar
 * @returns {string} String sanitizada
 */
function sanitizeString(str) {
  if (!str) return '';
  return str.trim().replace(/[<>]/g, '');
}

/**
 * Calcula distância entre dois pontos geográficos (fórmula de Haversine)
 * @param {number} lat1 - Latitude do ponto 1
 * @param {number} lon1 - Longitude do ponto 1
 * @param {number} lat2 - Latitude do ponto 2
 * @param {number} lon2 - Longitude do ponto 2
 * @returns {number} Distância em metros
 */
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371e3; // Raio da Terra em metros
  const φ1 = (lat1 * Math.PI) / 180;
  const φ2 = (lat2 * Math.PI) / 180;
  const Δφ = ((lat2 - lat1) * Math.PI) / 180;
  const Δλ = ((lon2 - lon1) * Math.PI) / 180;

  const a =
    Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
    Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c; // Distância em metros
}

/**
 * Formata data para string legível
 * @param {Date|string} date - Data a formatar
 * @returns {string} Data formatada
 */
function formatDate(date) {
  const d = new Date(date);
  return d.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * Extrai token do header Authorization
 * @param {string} authHeader - Header de autorização
 * @returns {string|null} Token extraído ou null
 */
function extractTokenFromHeader(authHeader) {
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return null;
  }
  return authHeader.substring(7);
}

/**
 * Gera slug a partir de string
 * @param {string} str - String original
 * @returns {string} Slug gerado
 */
function generateSlug(str) {
  return str
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

/**
 * Paginação de resultados
 * @param {number} page - Página atual (começa em 1)
 * @param {number} limit - Itens por página
 * @returns {Object} {offset, limit}
 */
function paginate(page = 1, limit = config.pagination.defaultLimit) {
  const parsedPage = Math.max(1, parseInt(page));
  const parsedLimit = Math.min(
    config.pagination.maxLimit,
    Math.max(1, parseInt(limit))
  );
  
  return {
    offset: (parsedPage - 1) * parsedLimit,
    limit: parsedLimit,
    page: parsedPage
  };
}

/**
 * Trata erros e retorna resposta apropriada
 * @param {Error} error - Erro capturado
 * @param {Object} res - Objeto response do Express
 * @returns {Object} Resposta de erro
 */
function handleError(error, res) {
  console.error('❌ Erro:', error);
  
  // Erro de validação
  if (error.message.includes('UNIQUE constraint failed')) {
    return res.status(409).json(
      errorResponse('Registro já existe', error.message)
    );
  }
  
  // Erro de foreign key
  if (error.message.includes('FOREIGN KEY constraint failed')) {
    return res.status(400).json(
      errorResponse('Referência inválida', error.message)
    );
  }
  
  // Erro de token
  if (error.message.includes('Token') || error.message.includes('token')) {
    return res.status(401).json(
      errorResponse('Não autorizado', error.message)
    );
  }
  
  // Erro genérico
  return res.status(500).json(
    errorResponse('Erro interno do servidor', 
      config.server.environment === 'development' ? error.message : undefined
    )
  );
}

/**
 * Delay assíncrono (útil para testes)
 * @param {number} ms - Milissegundos para aguardar
 * @returns {Promise} Promise que resolve após o delay
 */
function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

module.exports = {
  hashPassword,
  comparePassword,
  generateToken,
  verifyToken,
  successResponse,
  errorResponse,
  isValidEmail,
  validatePassword,
  sanitizeString,
  calculateDistance,
  formatDate,
  extractTokenFromHeader,
  generateSlug,
  paginate,
  handleError,
  delay
};

