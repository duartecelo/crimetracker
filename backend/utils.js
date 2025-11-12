/**
 * Funções auxiliares do CrimeTracker
 */

const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');
const config = require('./config');

// ========================================
// FUNÇÕES DE ID E TIMESTAMP
// ========================================

/**
 * Gera UUID único (versão 4)
 * @returns {string} UUID no formato xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
 */
function generateUUID() {
  return crypto.randomUUID();
}

/**
 * Retorna timestamp atual no formato ISO 8601
 * @returns {string} Data e hora no formato 'YYYY-MM-DDTHH:mm:ss.sssZ'
 */
function getCurrentTimestamp() {
  return new Date().toISOString();
}

// ========================================
// FUNÇÕES DE VALIDAÇÃO
// ========================================

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
  if (!email || typeof email !== 'string') return false;
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email.trim());
}

/**
 * Valida tipo de crime
 * @param {string} tipo - Tipo de crime a validar
 * @returns {boolean} True se o tipo é válido
 */
function isValidCrimeType(tipo) {
  const validTypes = [
    'roubo',
    'furto',
    'assalto',
    'vandalismo',
    'agressao',
    'trafico',
    'homicidio',
    'sequestro',
    'invasao',
    'suspeita',
    'perturbacao',
    'outro'
  ];
  
  if (!tipo || typeof tipo !== 'string') return false;
  return validTypes.includes(tipo.toLowerCase().trim());
}

/**
 * Lista todos os tipos de crimes válidos
 * @returns {Array<string>} Array com tipos de crimes válidos
 */
function getCrimeTypes() {
  return [
    'roubo',
    'furto',
    'assalto',
    'vandalismo',
    'agressao',
    'trafico',
    'homicidio',
    'sequestro',
    'invasao',
    'suspeita',
    'perturbacao',
    'outro'
  ];
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

// ========================================
// FUNÇÕES GEOGRÁFICAS
// ========================================

/**
 * Calcula distância entre dois pontos geográficos usando fórmula de Haversine
 * @param {number} lat1 - Latitude do ponto 1 (em graus decimais)
 * @param {number} lon1 - Longitude do ponto 1 (em graus decimais)
 * @param {number} lat2 - Latitude do ponto 2 (em graus decimais)
 * @param {number} lon2 - Longitude do ponto 2 (em graus decimais)
 * @returns {number} Distância em metros
 * 
 * @example
 * // Distância entre São Paulo e Rio de Janeiro
 * const distancia = calculateDistance(-23.5505, -46.6333, -22.9068, -43.1729);
 * console.log(distancia); // ~357000 metros (357 km)
 */
function calculateDistance(lat1, lon1, lat2, lon2) {
  // Validar entradas
  if (typeof lat1 !== 'number' || typeof lon1 !== 'number' ||
      typeof lat2 !== 'number' || typeof lon2 !== 'number') {
    throw new Error('Coordenadas devem ser números');
  }

  if (lat1 < -90 || lat1 > 90 || lat2 < -90 || lat2 > 90) {
    throw new Error('Latitude deve estar entre -90 e 90 graus');
  }

  if (lon1 < -180 || lon1 > 180 || lon2 < -180 || lon2 > 180) {
    throw new Error('Longitude deve estar entre -180 e 180 graus');
  }

  // Raio da Terra em metros
  const R = 6371e3;
  
  // Converter graus para radianos
  const φ1 = (lat1 * Math.PI) / 180;
  const φ2 = (lat2 * Math.PI) / 180;
  const Δφ = ((lat2 - lat1) * Math.PI) / 180;
  const Δλ = ((lon2 - lon1) * Math.PI) / 180;

  // Fórmula de Haversine
  const a =
    Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
    Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  // Distância em metros
  const distanceInMeters = R * c;
  
  return Math.round(distanceInMeters); // Arredondar para inteiro
}

/**
 * Valida coordenadas geográficas
 * @param {number} lat - Latitude
 * @param {number} lon - Longitude
 * @returns {boolean} True se as coordenadas são válidas
 */
function isValidCoordinates(lat, lon) {
  return (
    typeof lat === 'number' &&
    typeof lon === 'number' &&
    lat >= -90 && lat <= 90 &&
    lon >= -180 && lon <= 180
  );
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

// ========================================
// EXPORTS
// ========================================

module.exports = {
  // ID e Timestamp
  generateUUID,
  getCurrentTimestamp,
  
  // Autenticação
  hashPassword,
  comparePassword,
  generateToken,
  verifyToken,
  
  // Validação
  isValidEmail,
  isValidCrimeType,
  getCrimeTypes,
  validatePassword,
  isValidCoordinates,
  
  // Geográficas
  calculateDistance,
  
  // Formatação
  successResponse,
  errorResponse,
  formatDate,
  sanitizeString,
  generateSlug,
  
  // Utilitários
  extractTokenFromHeader,
  paginate,
  handleError,
  delay
};

