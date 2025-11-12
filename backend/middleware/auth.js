/**
 * Middleware de autenticação
 */

const { verifyToken, extractTokenFromHeader, errorResponse } = require('../utils');

/**
 * Middleware para verificar autenticação JWT
 */
function authenticateToken(req, res, next) {
  try {
    const authHeader = req.headers['authorization'];
    const token = extractTokenFromHeader(authHeader);

    if (!token) {
      return res.status(401).json(
        errorResponse('Token de autenticação não fornecido')
      );
    }

    const decoded = verifyToken(token);
    req.user = decoded;
    next();
  } catch (error) {
    return res.status(403).json(
      errorResponse('Token inválido ou expirado', error.message)
    );
  }
}

/**
 * Middleware para verificar se usuário é admin de um grupo
 */
function requireGroupAdmin(req, res, next) {
  // Implementar verificação de admin
  // Por enquanto, apenas passa adiante
  next();
}

/**
 * Middleware para verificar se usuário é dono do recurso
 */
function requireOwnership(req, res, next) {
  // Implementar verificação de ownership
  // Por enquanto, apenas passa adiante
  next();
}

module.exports = {
  authenticateToken,
  requireGroupAdmin,
  requireOwnership
};

