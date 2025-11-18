/**
 * Testes unitários para middleware/auth.js
 */

const { authenticateToken } = require('../../../middleware/auth');
const { generateToken } = require('../../../utils');

describe('Auth Middleware - authenticateToken', () => {
  let req, res, next;

  beforeEach(() => {
    // Mock do request, response e next
    req = {
      headers: {}
    };
    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn().mockReturnThis()
    };
    next = jest.fn();
  });

  test('deve autenticar token válido com sucesso', () => {
    const token = generateToken({
      user_id: 'user-123',
      username: 'usuario',
      email: 'usuario@exemplo.com'
    });

    req.headers['authorization'] = `Bearer ${token}`;

    authenticateToken(req, res, next);

    expect(next).toHaveBeenCalled();
    expect(req.user).toBeDefined();
    expect(req.user.user_id).toBe('user-123');
    expect(req.user.username).toBe('usuario');
  });

  test('deve rejeitar requisição sem header authorization', () => {
    authenticateToken(req, res, next);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      message: 'Token de autenticação não fornecido'
    });
    expect(next).not.toHaveBeenCalled();
  });

  test('deve rejeitar header authorization sem Bearer', () => {
    req.headers['authorization'] = 'InvalidFormat token123';

    authenticateToken(req, res, next);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      message: expect.stringContaining('Formato de token inválido')
    });
    expect(next).not.toHaveBeenCalled();
  });

  test('deve rejeitar token inválido', () => {
    req.headers['authorization'] = 'Bearer token.invalido.aqui';

    authenticateToken(req, res, next);

    expect(res.status).toHaveBeenCalledWith(403);
    expect(res.json).toHaveBeenCalledWith({
      success: false,
      message: 'Token inválido'
    });
    expect(next).not.toHaveBeenCalled();
  });

  test('deve rejeitar token vazio após Bearer', () => {
    req.headers['authorization'] = 'Bearer ';

    authenticateToken(req, res, next);

    expect(res.status).toHaveBeenCalledWith(401);
    expect(next).not.toHaveBeenCalled();
  });

  test('deve extrair dados do usuário do token', () => {
    const userData = {
      user_id: 'user-456',
      username: 'testuser',
      email: 'test@exemplo.com'
    };

    const token = generateToken(userData);
    req.headers['authorization'] = `Bearer ${token}`;

    authenticateToken(req, res, next);

    expect(req.user.user_id).toBe('user-456');
    expect(req.user.username).toBe('testuser');
    expect(req.user.email).toBe('test@exemplo.com');
  });

  test('deve aceitar header authorization com Bearer maiúsculo/minúsculo', () => {
    const token = generateToken({ user_id: 'user-123' });
    req.headers['authorization'] = `Bearer ${token}`;

    authenticateToken(req, res, next);

    expect(next).toHaveBeenCalled();
  });

  test('deve anexar objeto user ao request', () => {
    const token = generateToken({
      user_id: 'user-789',
      username: 'usuario789',
      email: 'usuario789@exemplo.com'
    });

    req.headers['authorization'] = `Bearer ${token}`;

    authenticateToken(req, res, next);

    expect(req.user).toBeDefined();
    expect(typeof req.user).toBe('object');
    expect(req.user).toHaveProperty('user_id');
    expect(req.user).toHaveProperty('username');
    expect(req.user).toHaveProperty('email');
  });

  test('deve incluir timestamp de expiração no token decodificado', () => {
    const token = generateToken({ user_id: 'user-123' });
    req.headers['authorization'] = `Bearer ${token}`;

    authenticateToken(req, res, next);

    expect(req.user).toHaveProperty('iat'); // issued at
    expect(req.user).toHaveProperty('exp'); // expiration
  });
});
