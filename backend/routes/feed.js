/**
 * Rotas de Feed - FEED-001
 */

const express = require('express');
const { body, query, param, validationResult } = require('express-validator');
const feedService = require('../services/feedService');
const { authenticateToken } = require('../middleware/auth');

const router = express.Router();

/**
 * Middleware de validação
 */
const validate = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      success: false,
      message: 'Erro de validação',
      errors: errors.array().map(err => ({
        field: err.path,
        message: err.msg
      }))
    });
  }
  next();
};

/**
 * POST /api/groups/:group_id/posts
 * Cria post se o usuário for membro
 * 
 * Headers: Authorization: Bearer <token>
 * Body: { conteudo }
 * Response: { success: true, data: {...} }
 */
router.post(
  '/groups/:group_id/posts',
  authenticateToken,
  [
    param('group_id')
      .notEmpty().withMessage('ID do grupo é obrigatório')
      .isUUID().withMessage('ID do grupo inválido'),
    
    body('conteudo')
      .trim()
      .notEmpty().withMessage('Conteúdo é obrigatório')
      .isLength({ max: 1000 }).withMessage('Conteúdo deve ter no máximo 1000 caracteres'),
    
    validate
  ],
  async (req, res) => {
    try {
      const userId = req.user.user_id;
      const groupId = req.params.group_id;
      const { conteudo } = req.body;

      const post = await feedService.createPost(userId, groupId, conteudo);

      res.status(201).json({
        success: true,
        data: post
      });

    } catch (error) {
      console.error('❌ Erro ao criar post:', error.message);

      const statusCode = error.message.includes('não encontrado') ? 404 :
                         error.message.includes('precisa ser membro') ? 403 :
                         error.message.includes('obrigatório') ||
                         error.message.includes('máximo') ? 400 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * GET /api/groups/:group_id/posts
 * Lista posts mais recentes (ordem DESC)
 * 
 * Query: ?page=1&limit=20
 * Response: { success: true, data: [...], pagination: {...} }
 */
router.get(
  '/groups/:group_id/posts',
  authenticateToken,
  [
    param('group_id')
      .notEmpty().withMessage('ID do grupo é obrigatório')
      .isUUID().withMessage('ID do grupo inválido'),
    
    query('page')
      .optional()
      .isInt({ min: 1 }).withMessage('Página deve ser um número maior ou igual a 1'),
    
    query('limit')
      .optional()
      .isInt({ min: 1, max: 100 }).withMessage('Limite deve estar entre 1 e 100'),
    
    validate
  ],
  async (req, res) => {
    try {
      const groupId = req.params.group_id;
      const page = parseInt(req.query.page) || 1;
      const limit = parseInt(req.query.limit) || 20;

      const result = await feedService.getGroupPosts(groupId, page, limit);

      res.json({
        success: true,
        data: result.posts,
        pagination: result.pagination
      });

    } catch (error) {
      console.error('❌ Erro ao buscar posts:', error.message);

      const statusCode = error.message.includes('não encontrado') ? 404 :
                         error.message.includes('deve ser') ||
                         error.message.includes('deve estar') ? 400 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * DELETE /api/posts/:id
 * Apenas o autor pode apagar
 * 
 * Headers: Authorization: Bearer <token>
 * Response: { success: true, message: "..." }
 */
router.delete(
  '/posts/:id',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID do post é obrigatório')
      .isUUID().withMessage('ID do post inválido'),
    
    validate
  ],
  async (req, res) => {
    try {
      const postId = req.params.id;
      const userId = req.user.user_id;

      await feedService.deletePost(postId, userId);

      res.json({
        success: true,
        message: 'Post deletado com sucesso'
      });

    } catch (error) {
      console.error('❌ Erro ao deletar post:', error.message);

      const statusCode = error.message.includes('não encontrado') ||
                         error.message.includes('permissão') ? 404 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * GET /api/posts/:id
 * Retorna detalhes de um post específico
 * 
 * Headers: Authorization: Bearer <token>
 * Response: { success: true, data: {...} }
 */
router.get(
  '/posts/:id',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID do post é obrigatório')
      .isUUID().withMessage('ID do post inválido'),
    
    validate
  ],
  async (req, res) => {
    try {
      const postId = req.params.id;

      const post = await feedService.getPostById(postId);

      res.json({
        success: true,
        data: post
      });

    } catch (error) {
      console.error('❌ Erro ao buscar post:', error.message);

      const statusCode = error.message.includes('não encontrado') ? 404 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * PUT /api/posts/:id
 * Atualiza post (somente o autor)
 * 
 * Headers: Authorization: Bearer <token>
 * Body: { conteudo }
 * Response: { success: true, data: {...} }
 */
router.put(
  '/posts/:id',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID do post é obrigatório')
      .isUUID().withMessage('ID do post inválido'),
    
    body('conteudo')
      .trim()
      .notEmpty().withMessage('Conteúdo é obrigatório')
      .isLength({ max: 1000 }).withMessage('Conteúdo deve ter no máximo 1000 caracteres'),
    
    validate
  ],
  async (req, res) => {
    try {
      const postId = req.params.id;
      const userId = req.user.user_id;
      const { conteudo } = req.body;

      const post = await feedService.updatePost(postId, userId, conteudo);

      res.json({
        success: true,
        data: post
      });

    } catch (error) {
      console.error('❌ Erro ao atualizar post:', error.message);

      const statusCode = error.message.includes('não encontrado') ||
                         error.message.includes('permissão') ? 404 :
                         error.message.includes('obrigatório') ||
                         error.message.includes('máximo') ? 400 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * GET /api/feed
 * Feed geral do usuário (posts dos grupos que é membro)
 * 
 * Query: ?page=1&limit=20
 * Response: { success: true, data: [...], pagination: {...} }
 */
router.get(
  '/feed',
  authenticateToken,
  [
    query('page')
      .optional()
      .isInt({ min: 1 }).withMessage('Página deve ser um número maior ou igual a 1'),
    
    query('limit')
      .optional()
      .isInt({ min: 1, max: 100 }).withMessage('Limite deve estar entre 1 e 100'),
    
    validate
  ],
  async (req, res) => {
    try {
      const userId = req.user.user_id;
      const page = parseInt(req.query.page) || 1;
      const limit = parseInt(req.query.limit) || 20;

      const result = await feedService.getUserFeed(userId, page, limit);

      res.json({
        success: true,
        data: result.posts,
        pagination: result.pagination
      });

    } catch (error) {
      console.error('❌ Erro ao buscar feed:', error.message);

      res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * GET /api/posts/user/me
 * Lista posts do usuário autenticado
 * 
 * Headers: Authorization: Bearer <token>
 * Response: { success: true, data: [...] }
 */
router.get(
  '/posts/user/me',
  authenticateToken,
  async (req, res) => {
    try {
      const userId = req.user.user_id;

      const posts = await feedService.getUserPosts(userId);

      res.json({
        success: true,
        data: posts,
        count: posts.length
      });

    } catch (error) {
      console.error('❌ Erro ao buscar posts do usuário:', error.message);

      res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }
);

module.exports = router;
