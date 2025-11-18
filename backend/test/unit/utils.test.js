/**
 * Testes unitários para utils.js
 */

const {
  generateUUID,
  getCurrentTimestamp,
  hashPassword,
  comparePassword,
  generateToken,
  verifyToken,
  isValidEmail,
  isValidCrimeType,
  getCrimeTypes,
  validatePassword,
  isValidCoordinates,
  calculateDistance,
  successResponse,
  errorResponse,
  formatDate,
  sanitizeString,
  generateSlug,
  extractTokenFromHeader,
  paginate,
  delay
} = require('../../utils');

describe('Utils - ID e Timestamp', () => {
  test('generateUUID deve gerar UUID válido', () => {
    const uuid = generateUUID();
    expect(uuid).toMatch(/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i);
  });

  test('getCurrentTimestamp deve retornar timestamp ISO 8601', () => {
    const timestamp = getCurrentTimestamp();
    expect(timestamp).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/);
    expect(new Date(timestamp).toISOString()).toBe(timestamp);
  });
});

describe('Utils - Autenticação', () => {
  const testPassword = 'senha123456';

  test('hashPassword deve gerar hash válido', async () => {
    const hash = await hashPassword(testPassword);
    expect(hash).toBeDefined();
    expect(hash.length).toBeGreaterThan(20);
    expect(hash).toContain('$2');
  });

  test('comparePassword deve validar senha correta', async () => {
    const hash = await hashPassword(testPassword);
    const isValid = await comparePassword(testPassword, hash);
    expect(isValid).toBe(true);
  });

  test('comparePassword deve rejeitar senha incorreta', async () => {
    const hash = await hashPassword(testPassword);
    const isValid = await comparePassword('senhaerrada', hash);
    expect(isValid).toBe(false);
  });

  test('generateToken deve criar token JWT', () => {
    const payload = { user_id: '123', username: 'teste' };
    const token = generateToken(payload);
    expect(token).toBeDefined();
    expect(typeof token).toBe('string');
    expect(token.split('.').length).toBe(3);
  });

  test('verifyToken deve decodificar token válido', () => {
    const payload = { user_id: '123', username: 'teste' };
    const token = generateToken(payload);
    const decoded = verifyToken(token);
    expect(decoded.user_id).toBe('123');
    expect(decoded.username).toBe('teste');
  });

  test('verifyToken deve rejeitar token inválido', () => {
    expect(() => verifyToken('token.invalido.aqui')).toThrow();
  });
});

describe('Utils - Validação', () => {
  test('isValidEmail deve validar emails corretos', () => {
    expect(isValidEmail('usuario@exemplo.com')).toBe(true);
    expect(isValidEmail('test.user@domain.co.uk')).toBe(true);
    expect(isValidEmail('user+tag@email.com')).toBe(true);
  });

  test('isValidEmail deve rejeitar emails inválidos', () => {
    expect(isValidEmail('email-invalido')).toBe(false);
    expect(isValidEmail('@exemplo.com')).toBe(false);
    expect(isValidEmail('usuario@')).toBe(false);
    expect(isValidEmail('')).toBe(false);
    expect(isValidEmail(null)).toBe(false);
  });

  test('isValidCrimeType deve validar tipos corretos', () => {
    expect(isValidCrimeType('Assalto')).toBe(true);
    expect(isValidCrimeType('furto')).toBe(true);
    expect(isValidCrimeType('AGRESSÃO')).toBe(true);
    expect(isValidCrimeType('vandalismo')).toBe(true);
    expect(isValidCrimeType('Roubo')).toBe(true);
    expect(isValidCrimeType('Outro')).toBe(true);
  });

  test('isValidCrimeType deve rejeitar tipos inválidos', () => {
    expect(isValidCrimeType('Homicídio')).toBe(false);
    expect(isValidCrimeType('')).toBe(false);
    expect(isValidCrimeType(null)).toBe(false);
  });

  test('getCrimeTypes deve retornar array de tipos', () => {
    const types = getCrimeTypes();
    expect(Array.isArray(types)).toBe(true);
    expect(types.length).toBe(6);
    expect(types).toContain('Assalto');
    expect(types).toContain('Furto');
  });

  test('validatePassword deve aceitar senhas válidas', () => {
    const result = validatePassword('senha12345678');
    expect(result.valid).toBe(true);
  });

  test('validatePassword deve rejeitar senhas curtas', () => {
    const result = validatePassword('curta');
    expect(result.valid).toBe(false);
    expect(result.message).toContain('8 caracteres');
  });

  test('isValidCoordinates deve validar coordenadas corretas', () => {
    expect(isValidCoordinates(-23.5505, -46.6333)).toBe(true);
    expect(isValidCoordinates(0, 0)).toBe(true);
    expect(isValidCoordinates(90, 180)).toBe(true);
    expect(isValidCoordinates(-90, -180)).toBe(true);
  });

  test('isValidCoordinates deve rejeitar coordenadas inválidas', () => {
    expect(isValidCoordinates(91, 0)).toBe(false);
    expect(isValidCoordinates(0, 181)).toBe(false);
    expect(isValidCoordinates(-91, 0)).toBe(false);
    expect(isValidCoordinates('text', 0)).toBe(false);
  });
});

describe('Utils - Geográficas', () => {
  test('calculateDistance deve calcular distância corretamente', () => {
    // São Paulo para Rio de Janeiro (~357-361 km)
    const distance = calculateDistance(-23.5505, -46.6333, -22.9068, -43.1729);
    expect(distance).toBeGreaterThan(350000);
    expect(distance).toBeLessThan(365000);
  });

  test('calculateDistance deve retornar 0 para mesmas coordenadas', () => {
    const distance = calculateDistance(-23.5505, -46.6333, -23.5505, -46.6333);
    expect(distance).toBe(0);
  });

  test('calculateDistance deve rejeitar coordenadas inválidas', () => {
    expect(() => calculateDistance(91, 0, 0, 0)).toThrow();
    expect(() => calculateDistance(0, 181, 0, 0)).toThrow();
    expect(() => calculateDistance('text', 0, 0, 0)).toThrow();
  });
});

describe('Utils - Formatação', () => {
  test('successResponse deve formatar resposta de sucesso', () => {
    const response = successResponse({ id: 1 }, 'Sucesso');
    expect(response.success).toBe(true);
    expect(response.message).toBe('Sucesso');
    expect(response.data).toEqual({ id: 1 });
  });

  test('successResponse deve funcionar sem mensagem', () => {
    const response = successResponse({ id: 1 });
    expect(response.success).toBe(true);
    expect(response.data).toEqual({ id: 1 });
    expect(response.message).toBeUndefined();
  });

  test('errorResponse deve formatar resposta de erro', () => {
    const response = errorResponse('Erro', { details: 'info' });
    expect(response.success).toBe(false);
    expect(response.error).toBe('Erro');
    expect(response.details).toEqual({ details: 'info' });
  });

  test('sanitizeString deve remover caracteres perigosos', () => {
    expect(sanitizeString('<script>alert("xss")</script>')).toBe('scriptalert("xss")/script');
    expect(sanitizeString('  texto normal  ')).toBe('texto normal');
    expect(sanitizeString('')).toBe('');
  });

  test('generateSlug deve criar slug válido', () => {
    expect(generateSlug('Grupo do Bairro')).toBe('grupo-do-bairro');
    expect(generateSlug('São Paulo - Centro')).toBe('sao-paulo-centro');
    expect(generateSlug('  Espaços   Múltiplos  ')).toBe('espacos-multiplos');
  });

  test('formatDate deve formatar data corretamente', () => {
    const date = new Date('2024-01-15T10:30:00Z');
    const formatted = formatDate(date);
    expect(formatted).toContain('15');
    expect(formatted).toContain('01');
    expect(formatted).toContain('2024');
  });
});

describe('Utils - Utilitários', () => {
  test('extractTokenFromHeader deve extrair token do header', () => {
    const token = extractTokenFromHeader('Bearer abc123xyz');
    expect(token).toBe('abc123xyz');
  });

  test('extractTokenFromHeader deve retornar null para header inválido', () => {
    expect(extractTokenFromHeader('abc123')).toBe(null);
    expect(extractTokenFromHeader('')).toBe(null);
    expect(extractTokenFromHeader(null)).toBe(null);
  });

  test('paginate deve calcular offset corretamente', () => {
    const result = paginate(1, 20);
    expect(result.offset).toBe(0);
    expect(result.limit).toBe(20);
    expect(result.page).toBe(1);
  });

  test('paginate deve calcular página 2 corretamente', () => {
    const result = paginate(2, 10);
    expect(result.offset).toBe(10);
    expect(result.limit).toBe(10);
    expect(result.page).toBe(2);
  });

  test('paginate deve usar valores padrão', () => {
    const result = paginate();
    expect(result.page).toBe(1);
    expect(result.offset).toBe(0);
  });

  test('delay deve aguardar tempo especificado', async () => {
    const start = Date.now();
    await delay(100);
    const duration = Date.now() - start;
    expect(duration).toBeGreaterThanOrEqual(100);
    expect(duration).toBeLessThan(200);
  });
});
