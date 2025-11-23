/**
 * Rotas de Grupos - GROUP-001
 */

const express = require('express');
const { body, query, param, validationResult } = require('express-validator');
const groupService = require('../services/groupService');
const { authenticateToken } = require('../middleware/auth');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

// Configurar Multer
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    const uploadDir = path.join(__dirname, '../uploads/groups');
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: function (req, file, cb) {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, 'group-' + uniqueSuffix + path.extname(file.originalname));
  }
});

const upload = multer({
  storage: storage,
  limits: { fileSize: 5 * 1024 * 1024 }, // 5MB
  fileFilter: (req, file, cb) => {
    if (file.mimetype.startsWith('image/')) {
      cb(null, true);
    } else {
      cb(new Error('Apenas imagens são permitidas'));
    }
  }
});

const router = express.Router();

/**
 * Middleware de validação
 */
const validate = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    // Se houve upload, deletar o arquivo
    if (req.file) {
        fs.unlink(req.file.path, (err) => {
            if (err) console.error('Erro ao deletar arquivo após falha de validação:', err);
        });
    }

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
 * POST /api/groups
 * Cria grupo e adiciona criador automaticamente
 * 
 * Headers: Authorization: Bearer <token>
 * Body: { nome, descricao } (Multipart/form-data)
 * Response: { success: true, data: {...} }
 */
router.post(
  '/',
  authenticateToken,
  upload.single('imagem'),
  [
    body('nome')
      .trim()
      .notEmpty().withMessage('Nome do grupo é obrigatório')
      .isLength({ max: 100 }).withMessage('Nome deve ter no máximo 100 caracteres'),
    
    body('descricao')
      .optional()
      .trim()
      .isLength({ max: 500 }).withMessage('Descrição deve ter no máximo 500 caracteres'),
    
    validate
  ],
  async (req, res) => {
    try {
      const userId = req.user.user_id;
      const { nome, descricao } = req.body;
      const imagem = req.file ? `/uploads/groups/${req.file.filename}` : null;

      const group = await groupService.createGroup(userId, nome, descricao, imagem);

      res.status(201).json({
        success: true,
        data: group
      });

    } catch (error) {
      console.error('❌ Erro ao criar grupo:', error.message);

      // Se houve upload, deletar o arquivo em caso de erro no serviço
      if (req.file) {
        fs.unlink(req.file.path, (err) => {
            if (err) console.error('Erro ao deletar arquivo após erro no serviço:', err);
        });
      }

      const statusCode = error.message.includes('já existe') ? 409 :
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
 * GET /api/groups
 * Busca grupos por nome (search opcional)
 * 
 * Query: ?search=termo
 * Response: { success: true, data: [...] }
 */
router.get(
  '/',
  authenticateToken,
  [
    query('search')
      .optional()
      .trim()
      .isLength({ min: 1, max: 100 }).withMessage('Termo de busca deve ter entre 1 e 100 caracteres'),
    
    validate
  ],
  async (req, res) => {
    try {
      const search = req.query.search || null;

      const groups = await groupService.searchGroups(search);

      res.json({
        success: true,
        data: groups,
        count: groups.length
      });

    } catch (error) {
      console.error('❌ Erro ao buscar grupos:', error.message);

      res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * GET /api/groups/:id
 * Retorna detalhes de um grupo específico
 * 
 * Headers: Authorization: Bearer <token>
 * Response: { success: true, data: {...} }
 */
router.get(
  '/:id',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID do grupo é obrigatório')
      .isUUID().withMessage('ID do grupo inválido'),
    
    validate
  ],
  async (req, res) => {
    try {
      const groupId = req.params.id;
      const userId = req.user.user_id;

      const group = await groupService.getGroupById(groupId, userId);

      res.json({
        success: true,
        data: group
      });

    } catch (error) {
      console.error('❌ Erro ao buscar grupo:', error.message);

      const statusCode = error.message.includes('não encontrado') ? 404 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * POST /api/groups/:id/join
 * Entrar no grupo
 * 
 * Headers: Authorization: Bearer <token>
 * Response: { success: true, data: {...} }
 */
router.post(
  '/:id/join',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID do grupo é obrigatório')
      .isUUID().withMessage('ID do grupo inválido'),
    
    validate
  ],
  async (req, res) => {
    try {
      const groupId = req.params.id;
      const userId = req.user.user_id;

      const group = await groupService.joinGroup(groupId, userId);

      res.json({
        success: true,
        data: group,
        message: 'Você entrou no grupo com sucesso'
      });

    } catch (error) {
      console.error('❌ Erro ao entrar no grupo:', error.message);

      const statusCode = error.message.includes('não encontrado') ? 404 :
                         error.message.includes('já é membro') ? 409 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * POST /api/groups/:id/leave
 * Sair do grupo
 * 
 * Headers: Authorization: Bearer <token>
 * Response: { success: true, data: {...} }
 */
router.post(
  '/:id/leave',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID do grupo é obrigatório')
      .isUUID().withMessage('ID do grupo inválido'),
    
    validate
  ],
  async (req, res) => {
    try {
      const groupId = req.params.id;
      const userId = req.user.user_id;

      const group = await groupService.leaveGroup(groupId, userId);

      res.json({
        success: true,
        data: group,
        message: 'Você saiu do grupo com sucesso'
      });

    } catch (error) {
      console.error('❌ Erro ao sair do grupo:', error.message);

      const statusCode = error.message.includes('não encontrado') ? 404 :
                         error.message.includes('não é membro') ||
                         error.message.includes('criador não pode') ? 400 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * GET /api/groups/:id/members
 * Lista membros do grupo
 * 
 * Headers: Authorization: Bearer <token>
 * Response: { success: true, data: [...] }
 */
router.get(
  '/:id/members',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID do grupo é obrigatório')
      .isUUID().withMessage('ID do grupo inválido'),
    
    validate
  ],
  async (req, res) => {
    try {
      const groupId = req.params.id;

      const members = await groupService.getGroupMembers(groupId);

      res.json({
        success: true,
        data: members,
        count: members.length
      });

    } catch (error) {
      console.error('❌ Erro ao buscar membros:', error.message);

      const statusCode = error.message.includes('não encontrado') ? 404 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * PUT /api/groups/:id
 * Atualiza grupo (somente o criador)
 * 
 * Headers: Authorization: Bearer <token>
 * Body: { nome?, descricao? }
 * Response: { success: true, data: {...} }
 */
router.put(
  '/:id',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID do grupo é obrigatório')
      .isUUID().withMessage('ID do grupo inválido'),
    
    body('nome')
      .optional()
      .trim()
      .isLength({ max: 100 }).withMessage('Nome deve ter no máximo 100 caracteres'),
    
    body('descricao')
      .optional()
      .trim()
      .isLength({ max: 500 }).withMessage('Descrição deve ter no máximo 500 caracteres'),
    
    validate
  ],
  async (req, res) => {
    try {
      const groupId = req.params.id;
      const userId = req.user.user_id;
      const updates = req.body;

      const group = await groupService.updateGroup(groupId, userId, updates);

      res.json({
        success: true,
        data: group
      });

    } catch (error) {
      console.error('❌ Erro ao atualizar grupo:', error.message);

      const statusCode = error.message.includes('não encontrado') ||
                         error.message.includes('permissão') ? 404 :
                         error.message.includes('já existe') ? 409 :
                         error.message.includes('inválido') ? 400 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * DELETE /api/groups/:id
 * Deleta grupo (somente o criador)
 * 
 * Headers: Authorization: Bearer <token>
 * Response: { success: true, message: "..." }
 */
router.delete(
  '/:id',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID do grupo é obrigatório')
      .isUUID().withMessage('ID do grupo inválido'),
    
    validate
  ],
  async (req, res) => {
    try {
      const groupId = req.params.id;
      const userId = req.user.user_id;

      await groupService.deleteGroup(groupId, userId);

      res.json({
        success: true,
        message: 'Grupo deletado com sucesso'
      });

    } catch (error) {
      console.error('❌ Erro ao deletar grupo:', error.message);

      const statusCode = error.message.includes('não encontrado') ||
                         error.message.includes('permissão') ? 404 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

module.exports = router;
