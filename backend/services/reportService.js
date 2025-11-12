/**
 * Serviço de Denúncias - CRIME-001
 */

const db = require('../database');
const { 
  generateUUID, 
  getCurrentTimestamp, 
  isValidCrimeType,
  calculateDistance 
} = require('../utils');

/**
 * Cria uma nova denúncia de crime
 * @param {string} userId - ID do usuário
 * @param {string} tipo - Tipo de crime
 * @param {string} descricao - Descrição da denúncia
 * @param {number} latitude - Latitude
 * @param {number} longitude - Longitude
 */
async function createReport(userId, tipo, descricao, latitude, longitude) {
  const startTime = Date.now();

  // Validar tipo de crime
  if (!isValidCrimeType(tipo)) {
    throw new Error('Tipo de crime inválido. Use: Assalto, Furto, Agressão, Vandalismo, Roubo, Outro');
  }

  // Validar descrição (até 500 caracteres)
  if (!descricao || descricao.trim().length === 0) {
    throw new Error('Descrição é obrigatória');
  }

  if (descricao.length > 500) {
    throw new Error('Descrição deve ter no máximo 500 caracteres');
  }

  // Validar latitude e longitude
  if (typeof latitude !== 'number' || latitude < -90 || latitude > 90) {
    throw new Error('Latitude inválida (deve estar entre -90 e 90)');
  }

  if (typeof longitude !== 'number' || longitude < -180 || longitude > 180) {
    throw new Error('Longitude inválida (deve estar entre -180 e 180)');
  }

  // Gerar UUID para a denúncia
  const reportId = generateUUID();
  const timestamp = getCurrentTimestamp();

  // Inserir denúncia no banco
  await db.run(
    `INSERT INTO crime_reports (id, user_id, tipo, descricao, lat, lon, created_at, updated_at)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
    [reportId, userId, tipo, descricao.trim(), latitude, longitude, timestamp, timestamp]
  );

  // Buscar denúncia criada com dados do usuário
  const report = await db.get(
    `SELECT 
      cr.id, 
      cr.tipo, 
      cr.descricao, 
      cr.lat, 
      cr.lon, 
      cr.created_at,
      u.username as author_username
     FROM crime_reports cr
     JOIN users u ON cr.user_id = u.id
     WHERE cr.id = ?`,
    [reportId]
  );

  const duration = Date.now() - startTime;
  console.log(`✅ Denúncia criada em ${duration}ms`);

  return report;
}

/**
 * Busca denúncias próximas (últimos 30 dias)
 * @param {number} latitude - Latitude de referência
 * @param {number} longitude - Longitude de referência
 * @param {number} radiusKm - Raio em quilômetros (padrão: 5)
 */
async function getNearbyReports(latitude, longitude, radiusKm = 5) {
  const startTime = Date.now();

  // Validar parâmetros
  if (typeof latitude !== 'number' || latitude < -90 || latitude > 90) {
    throw new Error('Latitude inválida (deve estar entre -90 e 90)');
  }

  if (typeof longitude !== 'number' || longitude < -180 || longitude > 180) {
    throw new Error('Longitude inválida (deve estar entre -180 e 180)');
  }

  if (typeof radiusKm !== 'number' || radiusKm <= 0) {
    throw new Error('Raio deve ser um número positivo');
  }

  // Calcular data de 30 dias atrás
  const thirtyDaysAgo = new Date();
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
  const dateThreshold = thirtyDaysAgo.toISOString();

  // Buscar todas as denúncias dos últimos 30 dias
  const allReports = await db.all(
    `SELECT 
      cr.id, 
      cr.tipo, 
      cr.descricao, 
      cr.lat, 
      cr.lon, 
      cr.created_at,
      u.username as author_username
     FROM crime_reports cr
     JOIN users u ON cr.user_id = u.id
     WHERE cr.created_at >= ?
     ORDER BY cr.created_at DESC`,
    [dateThreshold]
  );

  // Filtrar por distância usando calculateDistance()
  const radiusMeters = radiusKm * 1000;
  const nearbyReports = allReports.filter(report => {
    const distance = calculateDistance(latitude, longitude, report.lat, report.lon);
    return distance <= radiusMeters;
  });

  // Adicionar distância a cada denúncia
  const reportsWithDistance = nearbyReports.map(report => ({
    ...report,
    distance_meters: calculateDistance(latitude, longitude, report.lat, report.lon),
    distance_km: (calculateDistance(latitude, longitude, report.lat, report.lon) / 1000).toFixed(2)
  }));

  const duration = Date.now() - startTime;
  console.log(`✅ ${reportsWithDistance.length} denúncias encontradas em ${duration}ms`);

  return reportsWithDistance;
}

/**
 * Busca denúncia por ID
 * @param {string} reportId - ID da denúncia
 */
async function getReportById(reportId) {
  const startTime = Date.now();

  const report = await db.get(
    `SELECT 
      cr.id, 
      cr.tipo, 
      cr.descricao, 
      cr.lat, 
      cr.lon, 
      cr.created_at,
      cr.updated_at,
      cr.user_id,
      u.username as author_username,
      u.email as author_email
     FROM crime_reports cr
     JOIN users u ON cr.user_id = u.id
     WHERE cr.id = ?`,
    [reportId]
  );

  if (!report) {
    throw new Error('Denúncia não encontrada');
  }

  const duration = Date.now() - startTime;
  console.log(`✅ Denúncia recuperada em ${duration}ms`);

  return report;
}

/**
 * Busca denúncias do usuário
 * @param {string} userId - ID do usuário
 */
async function getUserReports(userId) {
  const reports = await db.all(
    `SELECT 
      id, 
      tipo, 
      descricao, 
      lat, 
      lon, 
      created_at
     FROM crime_reports
     WHERE user_id = ?
     ORDER BY created_at DESC`,
    [userId]
  );

  return reports;
}

/**
 * Atualiza denúncia
 * @param {string} reportId - ID da denúncia
 * @param {string} userId - ID do usuário (para verificar ownership)
 * @param {Object} updates - Campos a atualizar
 */
async function updateReport(reportId, userId, updates) {
  // Verificar se denúncia existe e pertence ao usuário
  const report = await db.get(
    'SELECT * FROM crime_reports WHERE id = ? AND user_id = ?',
    [reportId, userId]
  );

  if (!report) {
    throw new Error('Denúncia não encontrada ou você não tem permissão para editá-la');
  }

  const { tipo, descricao } = updates;
  const fieldsToUpdate = [];
  const values = [];

  if (tipo) {
    if (!isValidCrimeType(tipo)) {
      throw new Error('Tipo de crime inválido');
    }
    fieldsToUpdate.push('tipo = ?');
    values.push(tipo);
  }

  if (descricao) {
    if (descricao.length > 500) {
      throw new Error('Descrição deve ter no máximo 500 caracteres');
    }
    fieldsToUpdate.push('descricao = ?');
    values.push(descricao.trim());
  }

  if (fieldsToUpdate.length === 0) {
    throw new Error('Nenhum campo para atualizar');
  }

  fieldsToUpdate.push('updated_at = ?');
  values.push(getCurrentTimestamp());
  values.push(reportId);

  await db.run(
    `UPDATE crime_reports SET ${fieldsToUpdate.join(', ')} WHERE id = ?`,
    values
  );

  return await getReportById(reportId);
}

/**
 * Deleta denúncia
 * @param {string} reportId - ID da denúncia
 * @param {string} userId - ID do usuário (para verificar ownership)
 */
async function deleteReport(reportId, userId) {
  // Verificar se denúncia existe e pertence ao usuário
  const report = await db.get(
    'SELECT * FROM crime_reports WHERE id = ? AND user_id = ?',
    [reportId, userId]
  );

  if (!report) {
    throw new Error('Denúncia não encontrada ou você não tem permissão para excluí-la');
  }

  await db.run('DELETE FROM crime_reports WHERE id = ?', [reportId]);

  console.log(`✅ Denúncia ${reportId} deletada`);
  return true;
}

module.exports = {
  createReport,
  getNearbyReports,
  getReportById,
  getUserReports,
  updateReport,
  deleteReport
};
