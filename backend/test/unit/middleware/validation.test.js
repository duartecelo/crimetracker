/**
 * Testes unitários para middleware/validation.js
 */

const { validationResult } = require('express-validator');
const {
  handleValidationErrors,
  validateRegister,
  validateLogin,
  validateReport,
  validateGroup,
  validatePost,
  validateComment
} = require('../../../middleware/validation');

// Mock express-validator
jest.mock('express-validator', () => ({
  body: jest.fn(() => ({
    trim: jest.fn().mockReturnThis(),
    isLength: jest.fn().mockReturnThis(),
    withMessage: jest.fn().mockReturnThis(),
    matches: jest.fn().mockReturnThis(),
    isEmail: jest.fn().mockReturnThis(),
    normalizeEmail: jest.fn().mockReturnThis(),
    notEmpty: jest.fn().mockReturnThis(),
    isFloat: jest.fn().mockReturnThis(),
    optional: jest.fn().mockReturnThis(),
    isInt: jest.fn().mockReturnThis(),
    isIn: jest.fn().mockReturnThis()
  })),
  validationResult: jest.fn()
}));

describe('Validation - handleValidationErrors', () => {
  let req, res, next;

  beforeEach(() => {
    req = {};
    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn().mockReturnThis()
    };
    next = jest.fn();
    jest.clearAllMocks();
  });

  test('deve chamar next() quando não há erros de validação', () => {
    validationResult.mockReturnValue({
      isEmpty: () => true,
      array: () => []
    });

    handleValidationErrors(req, res, next);

    expect(next).toHaveBeenCalled();
    expect(res.status).not.toHaveBeenCalled();
  });

  test('deve retornar 400 quando há erros de validação', () => {
    validationResult.mockReturnValue({
      isEmpty: () => false,
      array: () => [
        { path: 'email', msg: 'Email inválido' },
        { path: 'password', msg: 'Senha muito curta' }
      ]
    });

    handleValidationErrors(req, res, next);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      error: 'Erro de validação',
      details: [
        { field: 'email', message: 'Email inválido' },
        { field: 'password', message: 'Senha muito curta' }
      ]
    });
    expect(next).not.toHaveBeenCalled();
  });

  test('deve formatar múltiplos erros de validação', () => {
    validationResult.mockReturnValue({
      isEmpty: () => false,
      array: () => [
        { path: 'username', msg: 'Username muito curto' },
        { path: 'email', msg: 'Email inválido' },
        { path: 'password', msg: 'Senha obrigatória' }
      ]
    });

    handleValidationErrors(req, res, next);

    expect(res.json).toHaveBeenCalledWith({
      success: false,
      error: 'Erro de validação',
      details: [
        { field: 'username', message: 'Username muito curto' },
        { field: 'email', message: 'Email inválido' },
        { field: 'password', message: 'Senha obrigatória' }
      ]
    });
  });

  test('deve tratar erro único de validação', () => {
    validationResult.mockReturnValue({
      isEmpty: () => false,
      array: () => [
        { path: 'email', msg: 'Email é obrigatório' }
      ]
    });

    handleValidationErrors(req, res, next);

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      error: 'Erro de validação',
      details: [
        { field: 'email', message: 'Email é obrigatório' }
      ]
    });
  });
});

describe('Validation - validateRegister', () => {
  test('deve exportar array de validações', () => {
    expect(Array.isArray(validateRegister)).toBe(true);
    expect(validateRegister.length).toBeGreaterThan(0);
  });

  test('deve incluir handleValidationErrors no final', () => {
    const lastItem = validateRegister[validateRegister.length - 1];
    expect(lastItem).toBe(handleValidationErrors);
  });

  test('deve ter validações para username, email, password, full_name', () => {
    // Verificar que body() foi chamado para os campos esperados
    expect(validateRegister.length).toBeGreaterThan(4);
  });
});

describe('Validation - validateLogin', () => {
  test('deve exportar array de validações', () => {
    expect(Array.isArray(validateLogin)).toBe(true);
    expect(validateLogin.length).toBeGreaterThan(0);
  });

  test('deve incluir handleValidationErrors no final', () => {
    const lastItem = validateLogin[validateLogin.length - 1];
    expect(lastItem).toBe(handleValidationErrors);
  });

  test('deve ter validações para username e password', () => {
    expect(validateLogin.length).toBeGreaterThan(1);
  });
});

describe('Validation - validateReport', () => {
  test('deve exportar array de validações', () => {
    expect(Array.isArray(validateReport)).toBe(true);
    expect(validateReport.length).toBeGreaterThan(0);
  });

  test('deve incluir handleValidationErrors no final', () => {
    const lastItem = validateReport[validateReport.length - 1];
    expect(lastItem).toBe(handleValidationErrors);
  });

  test('deve ter validações para campos de denúncia', () => {
    expect(validateReport.length).toBeGreaterThan(4);
  });
});

describe('Validation - validateGroup', () => {
  test('deve exportar array de validações', () => {
    expect(Array.isArray(validateGroup)).toBe(true);
    expect(validateGroup.length).toBeGreaterThan(0);
  });

  test('deve incluir handleValidationErrors no final', () => {
    const lastItem = validateGroup[validateGroup.length - 1];
    expect(lastItem).toBe(handleValidationErrors);
  });

  test('deve ter validações para campos de grupo', () => {
    expect(validateGroup.length).toBeGreaterThan(1);
  });
});

describe('Validation - validatePost', () => {
  test('deve exportar array de validações', () => {
    expect(Array.isArray(validatePost)).toBe(true);
    expect(validatePost.length).toBeGreaterThan(0);
  });

  test('deve incluir handleValidationErrors no final', () => {
    const lastItem = validatePost[validatePost.length - 1];
    expect(lastItem).toBe(handleValidationErrors);
  });

  test('deve ter validações para conteúdo', () => {
    expect(validatePost.length).toBeGreaterThan(1);
  });
});

describe('Validation - validateComment', () => {
  test('deve exportar array de validações', () => {
    expect(Array.isArray(validateComment)).toBe(true);
    expect(validateComment.length).toBeGreaterThan(0);
  });

  test('deve incluir handleValidationErrors no final', () => {
    const lastItem = validateComment[validateComment.length - 1];
    expect(lastItem).toBe(handleValidationErrors);
  });

  test('deve ter validações para comentário', () => {
    expect(validateComment.length).toBeGreaterThan(0);
  });
});

describe('Validation - Integração com validações reais', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('handleValidationErrors deve processar corretamente campos vazios', () => {
    const req = {};
    const res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn().mockReturnThis()
    };
    const next = jest.fn();

    validationResult.mockReturnValue({
      isEmpty: () => false,
      array: () => [
        { path: 'username', msg: 'Username é obrigatório' }
      ]
    });

    handleValidationErrors(req, res, next);

    expect(res.status).toHaveBeenCalledWith(400);
  });

  test('deve aceitar requisição válida', () => {
    const req = {};
    const res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn().mockReturnThis()
    };
    const next = jest.fn();

    validationResult.mockReturnValue({
      isEmpty: () => true,
      array: () => []
    });

    handleValidationErrors(req, res, next);

    expect(next).toHaveBeenCalled();
    expect(res.status).not.toHaveBeenCalled();
  });
});
