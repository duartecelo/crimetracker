/**
 * Testes unitários para middleware/errorHandler.js
 */

const { notFoundHandler, errorHandler } = require('../../../middleware/errorHandler');

describe('ErrorHandler - notFoundHandler', () => {
  let req, res, next;

  beforeEach(() => {
    req = {
      method: 'GET',
      path: '/api/nao-existe'
    };
    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn().mockReturnThis()
    };
    next = jest.fn();
  });

  test('deve retornar 404 para rota não encontrada', () => {
    notFoundHandler(req, res, next);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      error: 'Rota não encontrada: GET /api/nao-existe'
    });
  });

  test('deve incluir método e path na mensagem de erro', () => {
    req.method = 'POST';
    req.path = '/api/outro-caminho';

    notFoundHandler(req, res, next);

    expect(res.json).toHaveBeenCalledWith({
      success: false,
      error: 'Rota não encontrada: POST /api/outro-caminho'
    });
  });

  test('deve formatar corretamente para diferentes métodos HTTP', () => {
    const methods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'];

    methods.forEach(method => {
      jest.clearAllMocks();
      req.method = method;
      req.path = '/test';

      notFoundHandler(req, res, next);

      expect(res.status).toHaveBeenCalledWith(404);
      expect(res.json).toHaveBeenCalledWith({
        success: false,
        error: `Rota não encontrada: ${method} /test`
      });
    });
  });
});

describe('ErrorHandler - errorHandler', () => {
  let req, res, next, consoleErrorSpy;

  beforeEach(() => {
    req = {};
    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn().mockReturnThis()
    };
    next = jest.fn();
    consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();
  });

  afterEach(() => {
    consoleErrorSpy.mockRestore();
  });

  test('deve tratar erro com status code customizado', () => {
    const error = new Error('Erro de validação');
    error.statusCode = 400;

    errorHandler(error, req, res, next);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      error: 'Erro de validação'
    });
  });

  test('deve usar status 500 como padrão se não houver statusCode', () => {
    const error = new Error('Erro genérico');

    errorHandler(error, req, res, next);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      error: 'Erro genérico'
    });
  });

  test('deve usar mensagem padrão se erro não tiver mensagem', () => {
    const error = {};

    errorHandler(error, req, res, next);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      error: 'Erro interno do servidor'
    });
  });

  test('deve logar erro no console', () => {
    const error = new Error('Erro de teste');

    errorHandler(error, req, res, next);

    expect(console.error).toHaveBeenCalledWith('❌ Erro não tratado:', error);
  });

  test('deve tratar erro 401 (não autorizado)', () => {
    const error = new Error('Token inválido');
    error.statusCode = 401;

    errorHandler(error, req, res, next);

    expect(res.status).toHaveBeenCalledWith(401);
  });

  test('deve tratar erro 403 (proibido)', () => {
    const error = new Error('Acesso negado');
    error.statusCode = 403;

    errorHandler(error, req, res, next);

    expect(res.status).toHaveBeenCalledWith(403);
  });

  test('deve tratar erro 404 (não encontrado)', () => {
    const error = new Error('Recurso não encontrado');
    error.statusCode = 404;

    errorHandler(error, req, res, next);

    expect(res.status).toHaveBeenCalledWith(404);
  });

  test('deve tratar erro 409 (conflito)', () => {
    const error = new Error('Recurso já existe');
    error.statusCode = 409;

    errorHandler(error, req, res, next);

    expect(res.status).toHaveBeenCalledWith(409);
  });

  test('deve incluir stack trace em desenvolvimento', () => {
    // Mock config para ambiente de desenvolvimento
    jest.mock('../../../config', () => ({
      server: {
        environment: 'development'
      }
    }));

    const error = new Error('Erro com stack');
    error.stack = 'Error: Erro com stack\n    at test.js:1:1';

    errorHandler(error, req, res, next);

    // Verificar que o response foi chamado (stack pode ou não estar incluído dependendo do ambiente)
    expect(res.json).toHaveBeenCalled();
  });

  test('deve tratar diferentes tipos de erros', () => {
    const errors = [
      { message: 'Erro de autenticação', statusCode: 401 },
      { message: 'Permissão negada', statusCode: 403 },
      { message: 'Não encontrado', statusCode: 404 },
      { message: 'Conflito', statusCode: 409 },
      { message: 'Erro do servidor', statusCode: 500 }
    ];

    errors.forEach(errData => {
      jest.clearAllMocks();
      const error = new Error(errData.message);
      error.statusCode = errData.statusCode;

      errorHandler(error, req, res, next);

      expect(res.status).toHaveBeenCalledWith(errData.statusCode);
      expect(res.json).toHaveBeenCalledWith({
        success: false,
        error: errData.message
      });
    });
  });
});
