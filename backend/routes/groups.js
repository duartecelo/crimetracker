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

// Criar grupo
router.post('/',
  authenticateToken,
  [
    body('name').trim().notEmpty().withMessage('Nome do grupo é obrigatório'),
    body('description').trim().notEmpty().withMessage('Descrição é obrigatória')
  ],
  validate,
  (req, res) => {
    try {
      const { name, description, latitude, longitude, radius_meters } = req.body;
      const userId = req.user.userId;

      const stmt = db.prepare(`
        INSERT INTO groups (name, description, created_by, latitude, longitude, radius_meters)
        VALUES (?, ?, ?, ?, ?, ?)
      `);
      const result = stmt.run(name, description, userId, latitude || null, longitude || null, radius_meters || 1000);

      // Adicionar criador como admin do grupo
      db.prepare('INSERT INTO group_members (group_id, user_id, role) VALUES (?, ?, ?)').run(result.lastInsertRowid, userId, 'admin');

      res.status(201).json({
        message: 'Grupo criado com sucesso',
        groupId: result.lastInsertRowid
      });
    } catch (error) {
      console.error('Erro ao criar grupo:', error);
      res.status(500).json({ error: 'Erro ao criar grupo' });
    }
  }
);

// Listar grupos
router.get('/', authenticateToken, (req, res) => {
  try {
    const groups = db.prepare(`
      SELECT g.*, u.username as creator_username,
             (SELECT COUNT(*) FROM group_members WHERE group_id = g.id) as member_count
      FROM groups g
      JOIN users u ON g.created_by = u.id
      ORDER BY g.created_at DESC
    `).all();

    res.json(groups);
  } catch (error) {
    console.error('Erro ao listar grupos:', error);
    res.status(500).json({ error: 'Erro ao listar grupos' });
  }
});

// Obter grupo por ID
router.get('/:id', authenticateToken, (req, res) => {
  try {
    const group = db.prepare(`
      SELECT g.*, u.username as creator_username, u.full_name as creator_name
      FROM groups g
      JOIN users u ON g.created_by = u.id
      WHERE g.id = ?
    `).get(req.params.id);

    if (!group) {
      return res.status(404).json({ error: 'Grupo não encontrado' });
    }

    // Buscar membros do grupo
    const members = db.prepare(`
      SELECT gm.role, gm.joined_at, u.id, u.username, u.full_name
      FROM group_members gm
      JOIN users u ON gm.user_id = u.id
      WHERE gm.group_id = ?
      ORDER BY gm.joined_at DESC
    `).all(req.params.id);

    group.members = members;
    res.json(group);
  } catch (error) {
    console.error('Erro ao buscar grupo:', error);
    res.status(500).json({ error: 'Erro ao buscar grupo' });
  }
});

// Entrar em um grupo
router.post('/:id/join', authenticateToken, (req, res) => {
  try {
    const groupId = req.params.id;
    const userId = req.user.userId;

    // Verificar se o grupo existe
    const group = db.prepare('SELECT id FROM groups WHERE id = ?').get(groupId);
    if (!group) {
      return res.status(404).json({ error: 'Grupo não encontrado' });
    }

    // Verificar se já é membro
    const existing = db.prepare('SELECT id FROM group_members WHERE group_id = ? AND user_id = ?').get(groupId, userId);
    if (existing) {
      return res.status(409).json({ error: 'Você já é membro deste grupo' });
    }

    db.prepare('INSERT INTO group_members (group_id, user_id, role) VALUES (?, ?, ?)').run(groupId, userId, 'member');

    res.json({ message: 'Você entrou no grupo com sucesso' });
  } catch (error) {
    console.error('Erro ao entrar no grupo:', error);
    res.status(500).json({ error: 'Erro ao entrar no grupo' });
  }
});

// Sair de um grupo
router.post('/:id/leave', authenticateToken, (req, res) => {
  try {
    const groupId = req.params.id;
    const userId = req.user.userId;

    // Verificar se é membro
    const membership = db.prepare('SELECT role FROM group_members WHERE group_id = ? AND user_id = ?').get(groupId, userId);
    if (!membership) {
      return res.status(404).json({ error: 'Você não é membro deste grupo' });
    }

    // Não permitir que o admin saia se for o único
    if (membership.role === 'admin') {
      const adminCount = db.prepare('SELECT COUNT(*) as count FROM group_members WHERE group_id = ? AND role = ?').get(groupId, 'admin');
      if (adminCount.count <= 1) {
        return res.status(400).json({ error: 'Você é o único admin. Promova outro membro antes de sair.' });
      }
    }

    db.prepare('DELETE FROM group_members WHERE group_id = ? AND user_id = ?').run(groupId, userId);

    res.json({ message: 'Você saiu do grupo' });
  } catch (error) {
    console.error('Erro ao sair do grupo:', error);
    res.status(500).json({ error: 'Erro ao sair do grupo' });
  }
});

module.exports = router;

