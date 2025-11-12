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

// Criar post no feed
router.post('/',
  authenticateToken,
  [
    body('content').trim().notEmpty().withMessage('Conteúdo é obrigatório')
  ],
  validate,
  (req, res) => {
    try {
      const { content, group_id, image_path } = req.body;
      const userId = req.user.userId;

      // Se for post em grupo, verificar se é membro
      if (group_id) {
        const membership = db.prepare('SELECT id FROM group_members WHERE group_id = ? AND user_id = ?').get(group_id, userId);
        if (!membership) {
          return res.status(403).json({ error: 'Você não é membro deste grupo' });
        }
      }

      const stmt = db.prepare(`
        INSERT INTO feed_posts (user_id, group_id, content, image_path)
        VALUES (?, ?, ?, ?)
      `);
      const result = stmt.run(userId, group_id || null, content, image_path || null);

      res.status(201).json({
        message: 'Post criado com sucesso',
        postId: result.lastInsertRowid
      });
    } catch (error) {
      console.error('Erro ao criar post:', error);
      res.status(500).json({ error: 'Erro ao criar post' });
    }
  }
);

// Listar posts do feed
router.get('/', authenticateToken, (req, res) => {
  try {
    const { group_id } = req.query;
    const userId = req.user.userId;

    let query = `
      SELECT fp.*, u.username, u.full_name,
             (SELECT COUNT(*) FROM comments WHERE post_id = fp.id) as comment_count
      FROM feed_posts fp
      JOIN users u ON fp.user_id = u.id
      WHERE 1=1
    `;
    const params = [];

    if (group_id) {
      query += ' AND fp.group_id = ?';
      params.push(group_id);
      
      // Verificar se é membro do grupo
      const membership = db.prepare('SELECT id FROM group_members WHERE group_id = ? AND user_id = ?').get(group_id, userId);
      if (!membership) {
        return res.status(403).json({ error: 'Você não é membro deste grupo' });
      }
    } else {
      // Feed geral - posts públicos (sem grupo) ou de grupos que o usuário participa
      query += ` AND (fp.group_id IS NULL OR fp.group_id IN (
        SELECT group_id FROM group_members WHERE user_id = ?
      ))`;
      params.push(userId);
    }

    query += ' ORDER BY fp.created_at DESC LIMIT 50';

    const posts = db.prepare(query).all(...params);
    res.json(posts);
  } catch (error) {
    console.error('Erro ao listar posts:', error);
    res.status(500).json({ error: 'Erro ao listar posts' });
  }
});

// Obter post por ID
router.get('/:id', authenticateToken, (req, res) => {
  try {
    const post = db.prepare(`
      SELECT fp.*, u.username, u.full_name
      FROM feed_posts fp
      JOIN users u ON fp.user_id = u.id
      WHERE fp.id = ?
    `).get(req.params.id);

    if (!post) {
      return res.status(404).json({ error: 'Post não encontrado' });
    }

    // Verificar permissão se for post de grupo
    if (post.group_id) {
      const membership = db.prepare('SELECT id FROM group_members WHERE group_id = ? AND user_id = ?').get(post.group_id, req.user.userId);
      if (!membership) {
        return res.status(403).json({ error: 'Você não tem permissão para ver este post' });
      }
    }

    // Buscar comentários
    const comments = db.prepare(`
      SELECT c.*, u.username, u.full_name
      FROM comments c
      JOIN users u ON c.user_id = u.id
      WHERE c.post_id = ?
      ORDER BY c.created_at ASC
    `).all(req.params.id);

    post.comments = comments;
    res.json(post);
  } catch (error) {
    console.error('Erro ao buscar post:', error);
    res.status(500).json({ error: 'Erro ao buscar post' });
  }
});

// Adicionar comentário a um post
router.post('/:id/comments',
  authenticateToken,
  [
    body('content').trim().notEmpty().withMessage('Conteúdo do comentário é obrigatório')
  ],
  validate,
  (req, res) => {
    try {
      const postId = req.params.id;
      const { content } = req.body;
      const userId = req.user.userId;

      // Verificar se o post existe
      const post = db.prepare('SELECT group_id FROM feed_posts WHERE id = ?').get(postId);
      if (!post) {
        return res.status(404).json({ error: 'Post não encontrado' });
      }

      // Verificar permissão se for post de grupo
      if (post.group_id) {
        const membership = db.prepare('SELECT id FROM group_members WHERE group_id = ? AND user_id = ?').get(post.group_id, userId);
        if (!membership) {
          return res.status(403).json({ error: 'Você não tem permissão para comentar neste post' });
        }
      }

      const stmt = db.prepare('INSERT INTO comments (post_id, user_id, content) VALUES (?, ?, ?)');
      const result = stmt.run(postId, userId, content);

      res.status(201).json({
        message: 'Comentário adicionado com sucesso',
        commentId: result.lastInsertRowid
      });
    } catch (error) {
      console.error('Erro ao adicionar comentário:', error);
      res.status(500).json({ error: 'Erro ao adicionar comentário' });
    }
  }
);

module.exports = router;

