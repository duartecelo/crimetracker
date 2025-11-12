const express = require('express');
const { body, validationResult } = require('express-validator');
const db = require('../database/db');
const { authenticateToken } = require('./auth');

const router = express.Router();

// Middleware de validação
const validate = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  next();
};

// Criar denúncia
router.post('/',
  authenticateToken,
  [
    body('title').trim().notEmpty().withMessage('Título é obrigatório'),
    body('description').trim().notEmpty().withMessage('Descrição é obrigatória'),
    body('category').trim().notEmpty().withMessage('Categoria é obrigatória'),
    body('latitude').isFloat().withMessage('Latitude inválida'),
    body('longitude').isFloat().withMessage('Longitude inválida')
  ],
  validate,
  (req, res) => {
    try {
      const { title, description, category, latitude, longitude, address, image_path } = req.body;
      const userId = req.user.userId;

      const stmt = db.prepare(`
        INSERT INTO reports (user_id, title, description, category, latitude, longitude, address, image_path)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      `);
      const result = stmt.run(userId, title, description, category, latitude, longitude, address || null, image_path || null);

      res.status(201).json({
        message: 'Denúncia criada com sucesso',
        reportId: result.lastInsertRowid
      });
    } catch (error) {
      console.error('Erro ao criar denúncia:', error);
      res.status(500).json({ error: 'Erro ao criar denúncia' });
    }
  }
);

// Listar denúncias (com filtros opcionais)
router.get('/', authenticateToken, (req, res) => {
  try {
    const { category, status, latitude, longitude, radius } = req.query;
    let query = `
      SELECT r.*, u.username, u.full_name
      FROM reports r
      JOIN users u ON r.user_id = u.id
      WHERE 1=1
    `;
    const params = [];

    if (category) {
      query += ' AND r.category = ?';
      params.push(category);
    }

    if (status) {
      query += ' AND r.status = ?';
      params.push(status);
    }

    query += ' ORDER BY r.created_at DESC LIMIT 100';

    const reports = db.prepare(query).all(...params);
    res.json(reports);
  } catch (error) {
    console.error('Erro ao listar denúncias:', error);
    res.status(500).json({ error: 'Erro ao listar denúncias' });
  }
});

// Obter denúncia por ID
router.get('/:id', authenticateToken, (req, res) => {
  try {
    const report = db.prepare(`
      SELECT r.*, u.username, u.full_name
      FROM reports r
      JOIN users u ON r.user_id = u.id
      WHERE r.id = ?
    `).get(req.params.id);

    if (!report) {
      return res.status(404).json({ error: 'Denúncia não encontrada' });
    }

    res.json(report);
  } catch (error) {
    console.error('Erro ao buscar denúncia:', error);
    res.status(500).json({ error: 'Erro ao buscar denúncia' });
  }
});

// Atualizar status da denúncia
router.patch('/:id/status', authenticateToken, (req, res) => {
  try {
    const { status } = req.body;
    const reportId = req.params.id;

    // Verificar se o usuário é o dono da denúncia
    const report = db.prepare('SELECT user_id FROM reports WHERE id = ?').get(reportId);
    if (!report) {
      return res.status(404).json({ error: 'Denúncia não encontrada' });
    }

    if (report.user_id !== req.user.userId) {
      return res.status(403).json({ error: 'Sem permissão para atualizar esta denúncia' });
    }

    db.prepare('UPDATE reports SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?').run(status, reportId);

    res.json({ message: 'Status atualizado com sucesso' });
  } catch (error) {
    console.error('Erro ao atualizar status:', error);
    res.status(500).json({ error: 'Erro ao atualizar status' });
  }
});

module.exports = router;

