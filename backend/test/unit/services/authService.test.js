/**
 * Testes unitários para authService.js
 */

const authService = require('../../../services/authService');
const db = require('../../../database');
const { verifyToken, hashPassword } = require('../../../utils');

// Mock do banco de dados
jest.mock('../../../database');

describe('AuthService - registerUser', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve registrar usuário com sucesso', async () => {
    // Mock do banco indicando que email e username não existem
    db.get.mockResolvedValueOnce(null); // email não existe
    db.get.mockResolvedValueOnce(null); // username não existe
    db.run.mockResolvedValueOnce({}); // insert bem-sucedido

    const result = await authService.registerUser(
      'teste@exemplo.com',
      'senha12345678',
      'usuarioteste'
    );

    expect(result.success).toBe(true);
    expect(result.user_id).toBeDefined();
    expect(result.username).toBe('usuarioteste');
    expect(result.email).toBe('teste@exemplo.com');
    expect(result.token).toBeDefined();
    expect(db.run).toHaveBeenCalledTimes(1);
  });

  test('deve normalizar email para lowercase', async () => {
    db.get.mockResolvedValueOnce(null);
    db.get.mockResolvedValueOnce(null);
    db.run.mockResolvedValueOnce({});

    const result = await authService.registerUser(
      'TESTE@EXEMPLO.COM',
      'senha12345678',
      'usuarioteste'
    );

    expect(result.email).toBe('teste@exemplo.com');
  });

  test('deve rejeitar email inválido', async () => {
    await expect(
      authService.registerUser('email-invalido', 'senha12345678', 'usuario')
    ).rejects.toThrow('Email inválido');
  });

  test('deve rejeitar senha curta', async () => {
    await expect(
      authService.registerUser('teste@exemplo.com', 'curta', 'usuario')
    ).rejects.toThrow('no mínimo 8 caracteres');
  });

  test('deve rejeitar email já cadastrado', async () => {
    db.get.mockResolvedValueOnce({ id: 'existing-id' }); // email existe

    await expect(
      authService.registerUser('teste@exemplo.com', 'senha12345678', 'usuario')
    ).rejects.toThrow('Email já cadastrado');
  });

  test('deve rejeitar username já cadastrado', async () => {
    db.get.mockResolvedValueOnce(null); // email não existe
    db.get.mockResolvedValueOnce({ id: 'existing-id' }); // username existe

    await expect(
      authService.registerUser('teste@exemplo.com', 'senha12345678', 'usuario')
    ).rejects.toThrow('Nome de usuário já cadastrado');
  });

  test('deve gerar token JWT válido', async () => {
    db.get.mockResolvedValueOnce(null);
    db.get.mockResolvedValueOnce(null);
    db.run.mockResolvedValueOnce({});

    const result = await authService.registerUser(
      'teste@exemplo.com',
      'senha12345678',
      'usuarioteste'
    );

    // Verificar se o token pode ser decodificado
    const decoded = verifyToken(result.token);
    expect(decoded.user_id).toBe(result.user_id);
    expect(decoded.username).toBe('usuarioteste');
    expect(decoded.email).toBe('teste@exemplo.com');
  });
});

describe('AuthService - loginUser', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('deve fazer login com sucesso', async () => {
    // Gerar hash real da senha para o teste
    const passwordHash = await hashPassword('senha12345678');
    
    // Mock de usuário existente com senha hasheada
    const mockUser = {
      id: 'user-123',
      username: 'usuarioteste',
      email: 'teste@exemplo.com',
      password_hash: passwordHash
    };

    db.get.mockResolvedValueOnce(mockUser);

    const result = await authService.loginUser('teste@exemplo.com', 'senha12345678');

    expect(result.success).toBe(true);
    expect(result.user_id).toBe('user-123');
    expect(result.username).toBe('usuarioteste');
    expect(result.email).toBe('teste@exemplo.com');
    expect(result.token).toBeDefined();
  });

  test('deve normalizar email para lowercase no login', async () => {
    // Gerar hash real da senha para o teste
    const passwordHash = await hashPassword('senha12345678');
    
    const mockUser = {
      id: 'user-123',
      username: 'usuarioteste',
      email: 'teste@exemplo.com',
      password_hash: passwordHash
    };

    db.get.mockResolvedValueOnce(mockUser);

    await authService.loginUser('TESTE@EXEMPLO.COM', 'senha12345678');

    expect(db.get).toHaveBeenCalledWith(
      expect.any(String),
      ['teste@exemplo.com']
    );
  });

  test('deve rejeitar email não cadastrado', async () => {
    db.get.mockResolvedValueOnce(null); // usuário não encontrado

    await expect(
      authService.loginUser('naoexiste@exemplo.com', 'senha12345678')
    ).rejects.toThrow('Email ou senha incorretos');
  });

  test('deve rejeitar senha incorreta', async () => {
    // Gerar hash real da senha correta
    const passwordHash = await hashPassword('senha12345678');
    
    const mockUser = {
      id: 'user-123',
      username: 'usuarioteste',
      email: 'teste@exemplo.com',
      password_hash: passwordHash
    };

    db.get.mockResolvedValueOnce(mockUser);

    await expect(
      authService.loginUser('teste@exemplo.com', 'senhaerrada')
    ).rejects.toThrow('Email ou senha incorretos');
  });

  test('deve gerar token JWT válido no login', async () => {
    // Gerar hash real da senha para o teste
    const passwordHash = await hashPassword('senha12345678');
    
    const mockUser = {
      id: 'user-123',
      username: 'usuarioteste',
      email: 'teste@exemplo.com',
      password_hash: passwordHash
    };

    db.get.mockResolvedValueOnce(mockUser);

    const result = await authService.loginUser('teste@exemplo.com', 'senha12345678');

    // Verificar se o token pode ser decodificado
    const decoded = verifyToken(result.token);
    expect(decoded.user_id).toBe('user-123');
    expect(decoded.username).toBe('usuarioteste');
    expect(decoded.email).toBe('teste@exemplo.com');
  });
});
