/**
 * Middleware de autenticação JWT - AUTH-001
 * Valida Authorization: Bearer <token>
 */

const { verifyToken, extractTokenFromHeader } = require('../utils');

/**
 * Middleware para validar Authorization: Bearer <token>
 * Rejeita com 401 se token ausente ou expirado
 * 
 * @param {Object} req - Request object
 * @param {Object} res - Response object
 * @param {Function} next - Next middleware
 */
function authenticateToken(req, res, next) {
  try {
    const authHeader = req.headers['authorization'];
    
    // Rejeitar com 401 se header ausente
    if (!authHeader) {
      return res.status(401).json({
        success: false,
        message: 'Token de autenticação não fornecido'
      });
    }

    // Extrair token do formato "Bearer <token>"
    const token = extractTokenFromHeader(authHeader);

    if (!token) {
      return res.status(401).json({
        success: false,
        message: 'Formato de token inválido. Use: Authorization: Bearer <token>'
      });
    }

    // Verificar e decodificar token JWT
    const decoded = verifyToken(token);
    
    // Anexar dados do usuário ao request
    req.user = decoded;
    
    next();

  } catch (error) {
    // Rejeitar com 401 se expirado, 403 para outros erros
    const statusCode = error.message.includes('jwt expired') ? 401 : 403;
    const message = error.message.includes('jwt expired') 
      ? 'Token expirado. Faça login novamente' 
      : 'Token inválido';

    return res.status(statusCode).json({
      success: false,
      message: message
    });
  }
}

/**
 * Middleware para verificar se usuário é admin de um grupo
 */
function requireGroupAdmin(req, res, next) {
  // TODO: Implementar verificação de admin
  next();
}

/**
 * Middleware para verificar se usuário é dono do recurso
 */
function requireOwnership(req, res, next) {
  // TODO: Implementar verificação de ownership
  next();
}

module.exports = {
  authenticateToken,
  requireGroupAdmin,
  requireOwnership
};
