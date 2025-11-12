/**
 * Middleware de tratamento de erros
 */

const { errorResponse } = require('../utils');
const config = require('../config');

/**
 * Middleware de tratamento de erros 404
 */
function notFoundHandler(req, res, next) {
  res.status(404).json(
    errorResponse(`Rota não encontrada: ${req.method} ${req.path}`)
  );
}

/**
 * Middleware de tratamento de erros gerais
 */
function errorHandler(err, req, res, next) {
  console.error('❌ Erro não tratado:', err);

  const statusCode = err.statusCode || 500;
  const message = err.message || 'Erro interno do servidor';

  res.status(statusCode).json(
    errorResponse(
      message,
      config.server.environment === 'development' ? err.stack : undefined
    )
  );
}

module.exports = {
  notFoundHandler,
  errorHandler
};

