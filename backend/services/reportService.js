/**
 * Serviço de denúncias
 */

const db = require('../database');
const { paginate } = require('../utils');

/**
 * Cria uma nova denúncia
 */
async function createReport(userId, reportData) {
  const { title, description, category, latitude, longitude, address, image_path } = reportData;

  const result = await db.run(
    `INSERT INTO reports (user_id, title, description, category, latitude, longitude, address, image_path)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?)`,
    [userId, title, description, category, latitude, longitude, address || null, image_path || null]
  );

  return {
    reportId: result.lastID
  };
}

/**
 * Lista denúncias com filtros
 */
async function listReports(filters = {}, page = 1, limit = 20) {
  const { category, status, userId } = filters;
  const pagination = paginate(page, limit);

  let sql = `
    SELECT r.*, u.username, u.full_name
    FROM reports r
    JOIN users u ON r.user_id = u.id
    WHERE 1=1
  `;
  const params = [];

  if (category) {
    sql += ' AND r.category = ?';
    params.push(category);
  }

  if (status) {
    sql += ' AND r.status = ?';
    params.push(status);
  }

  if (userId) {
    sql += ' AND r.user_id = ?';
    params.push(userId);
  }

  sql += ' ORDER BY r.created_at DESC LIMIT ? OFFSET ?';
  params.push(pagination.limit, pagination.offset);

  const reports = await db.all(sql, params);
  return reports;
}

/**
 * Busca denúncia por ID
 */
async function getReportById(reportId) {
  const report = await db.get(
    `SELECT r.*, u.username, u.full_name
     FROM reports r
     JOIN users u ON r.user_id = u.id
     WHERE r.id = ?`,
    [reportId]
  );

  if (!report) {
    throw new Error('Denúncia não encontrada');
  }

  return report;
}

/**
 * Atualiza status de denúncia
 */
async function updateReportStatus(reportId, userId, status) {
  // Verificar se usuário é dono da denúncia
  const report = await db.get(
    'SELECT user_id FROM reports WHERE id = ?',
    [reportId]
  );

  if (!report) {
    throw new Error('Denúncia não encontrada');
  }

  if (report.user_id !== userId) {
    throw new Error('Sem permissão para atualizar esta denúncia');
  }

  await db.run(
    'UPDATE reports SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?',
    [status, reportId]
  );

  return { success: true };
}

module.exports = {
  createReport,
  listReports,
  getReportById,
  updateReportStatus
};

