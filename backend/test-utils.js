/**
 * Testes das funções utilitárias
 * Execute: node test-utils.js
 */

const utils = require('./utils');

console.log('🧪 Testando funções utilitárias do CrimeTracker\n');

// ========================================
// Teste 1: generateUUID
// ========================================
console.log('1️⃣ Testando generateUUID()');
const uuid1 = utils.generateUUID();
const uuid2 = utils.generateUUID();
console.log('   UUID 1:', uuid1);
console.log('   UUID 2:', uuid2);
console.log('   ✓ São diferentes?', uuid1 !== uuid2);
console.log('   ✓ Formato válido?', /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(uuid1));
console.log('');

// ========================================
// Teste 2: getCurrentTimestamp
// ========================================
console.log('2️⃣ Testando getCurrentTimestamp()');
const timestamp = utils.getCurrentTimestamp();
console.log('   Timestamp:', timestamp);
console.log('   ✓ Formato ISO?', /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/.test(timestamp));
console.log('   ✓ Data válida?', !isNaN(new Date(timestamp).getTime()));
console.log('');

// ========================================
// Teste 3: isValidEmail
// ========================================
console.log('3️⃣ Testando isValidEmail()');
const emailTests = [
  { email: 'joao@example.com', expected: true },
  { email: 'maria.silva@gmail.com', expected: true },
  { email: 'usuario+tag@dominio.com.br', expected: true },
  { email: 'invalido', expected: false },
  { email: '@example.com', expected: false },
  { email: 'sem-arroba.com', expected: false },
  { email: '', expected: false },
  { email: null, expected: false }
];

emailTests.forEach(test => {
  const result = utils.isValidEmail(test.email);
  const status = result === test.expected ? '✓' : '✗';
  console.log(`   ${status} "${test.email}" → ${result} (esperado: ${test.expected})`);
});
console.log('');

// ========================================
// Teste 4: isValidCrimeType
// ========================================
console.log('4️⃣ Testando isValidCrimeType()');
const crimeTypeTests = [
  { tipo: 'roubo', expected: true },
  { tipo: 'furto', expected: true },
  { tipo: 'assalto', expected: true },
  { tipo: 'vandalismo', expected: true },
  { tipo: 'ROUBO', expected: true }, // Case insensitive
  { tipo: ' furto ', expected: true }, // Com espaços
  { tipo: 'invalido', expected: false },
  { tipo: '', expected: false },
  { tipo: null, expected: false }
];

crimeTypeTests.forEach(test => {
  const result = utils.isValidCrimeType(test.tipo);
  const status = result === test.expected ? '✓' : '✗';
  console.log(`   ${status} "${test.tipo}" → ${result} (esperado: ${test.expected})`);
});

console.log('\n   Tipos válidos:', utils.getCrimeTypes().join(', '));
console.log('');

// ========================================
// Teste 5: calculateDistance (Haversine)
// ========================================
console.log('5️⃣ Testando calculateDistance() - Fórmula de Haversine');

// Teste 1: São Paulo ↔ Rio de Janeiro (~357 km)
const distSP_RJ = utils.calculateDistance(-23.5505, -46.6333, -22.9068, -43.1729);
console.log('   São Paulo → Rio de Janeiro:');
console.log('     Distância:', distSP_RJ, 'metros');
console.log('     Distância:', (distSP_RJ / 1000).toFixed(2), 'km');
console.log('     ✓ Aproximadamente 357 km?', distSP_RJ > 350000 && distSP_RJ < 365000);

// Teste 2: Mesmo ponto (0 metros)
const distSame = utils.calculateDistance(-23.5505, -46.6333, -23.5505, -46.6333);
console.log('\n   Mesmo ponto:');
console.log('     Distância:', distSame, 'metros');
console.log('     ✓ Zero?', distSame === 0);

// Teste 3: Pontos próximos (< 1 km)
const distClose = utils.calculateDistance(-23.5505, -46.6333, -23.5515, -46.6343);
console.log('\n   Pontos próximos:');
console.log('     Distância:', distClose, 'metros');
console.log('     ✓ Menos de 1 km?', distClose < 1000);

// Teste 4: Coordenadas inválidas (deve lançar erro)
console.log('\n   Teste de erro (coordenadas inválidas):');
try {
  utils.calculateDistance(91, 0, 0, 0); // Latitude > 90
  console.log('     ✗ Deveria lançar erro!');
} catch (error) {
  console.log('     ✓ Erro capturado:', error.message);
}

console.log('');

// ========================================
// Teste 6: isValidCoordinates
// ========================================
console.log('6️⃣ Testando isValidCoordinates()');
const coordTests = [
  { lat: -23.5505, lon: -46.6333, expected: true },
  { lat: 0, lon: 0, expected: true },
  { lat: 90, lon: 180, expected: true },
  { lat: -90, lon: -180, expected: true },
  { lat: 91, lon: 0, expected: false }, // Latitude inválida
  { lat: 0, lon: 181, expected: false }, // Longitude inválida
  { lat: 'string', lon: 0, expected: false } // Tipo inválido
];

coordTests.forEach(test => {
  const result = utils.isValidCoordinates(test.lat, test.lon);
  const status = result === test.expected ? '✓' : '✗';
  console.log(`   ${status} (${test.lat}, ${test.lon}) → ${result} (esperado: ${test.expected})`);
});
console.log('');

// ========================================
// Teste 7: Respostas Formatadas
// ========================================
console.log('7️⃣ Testando formatação de respostas');
const successResp = utils.successResponse({ id: 1, nome: 'João' }, 'Operação bem-sucedida');
console.log('   Success Response:', JSON.stringify(successResp, null, 2));
console.log('   ✓ Tem campo success?', successResp.success === true);
console.log('   ✓ Tem campo data?', successResp.data !== undefined);
console.log('   ✓ Tem campo message?', successResp.message !== undefined);

const errorResp = utils.errorResponse('Erro de validação', { campo: 'email' });
console.log('\n   Error Response:', JSON.stringify(errorResp, null, 2));
console.log('   ✓ Tem campo success?', errorResp.success === false);
console.log('   ✓ Tem campo error?', errorResp.error !== undefined);
console.log('');

// ========================================
// Teste 8: Hash de Senha
// ========================================
console.log('8️⃣ Testando hash de senha');
(async () => {
  const senha = 'minhaSenhaSegura123';
  const hash = await utils.hashPassword(senha);
  console.log('   Senha original:', senha);
  console.log('   Hash gerado:', hash);
  console.log('   ✓ Hash diferente da senha?', hash !== senha);
  console.log('   ✓ Hash tem tamanho adequado?', hash.length > 50);
  
  const senhaCorreta = await utils.comparePassword(senha, hash);
  const senhaIncorreta = await utils.comparePassword('senhaErrada', hash);
  console.log('   ✓ Senha correta valida?', senhaCorreta === true);
  console.log('   ✓ Senha incorreta falha?', senhaIncorreta === false);
  console.log('');
  
  // ========================================
  // Teste 9: JWT Token
  // ========================================
  console.log('9️⃣ Testando geração e verificação de JWT');
  const payload = { userId: uuid1, username: 'joao' };
  const token = utils.generateToken(payload);
  console.log('   Payload:', payload);
  console.log('   Token gerado:', token.substring(0, 50) + '...');
  console.log('   ✓ Token gerado?', token.length > 0);
  
  try {
    const decoded = utils.verifyToken(token);
    console.log('   Payload decodificado:', { userId: decoded.userId, username: decoded.username });
    console.log('   ✓ userId correto?', decoded.userId === uuid1);
    console.log('   ✓ username correto?', decoded.username === 'joao');
    console.log('   ✓ Tem exp?', decoded.exp !== undefined);
  } catch (error) {
    console.log('   ✗ Erro ao verificar token:', error.message);
  }
  
  console.log('\n✅ Todos os testes concluídos!\n');
})();

