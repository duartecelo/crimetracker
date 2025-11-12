/**
 * Rotas de Autenticação - AUTH-001
 */

const express = require('express');
const { body, validationResult } = require('express-validator');
const authService = require('../services/authService');
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
 * POST /api/auth/register
 * Cria usuário com email, senha (hash bcryptjs), username
 * 
 * Body: { email, password, username }
 * Response: { success, user_id, username, email, token }
 */
router.post(
  '/register',
  [
    body('email')
      .trim()
      .notEmpty().withMessage('Email é obrigatório')
      .isEmail().withMessage('Email inválido')
      .normalizeEmail(),
    
    body('password')
      .notEmpty().withMessage('Senha é obrigatória')
      .isLength({ min: 8 }).withMessage('Senha deve ter no mínimo 8 caracteres'),
    
    body('username')
      .trim()
      .notEmpty().withMessage('Nome de usuário é obrigatório')
      .isLength({ min: 3, max: 30 }).withMessage('Nome de usuário deve ter entre 3 e 30 caracteres'),
    
    validate
  ],
  async (req, res) => {
    try {
      const { email, password, username } = req.body;

      const result = await authService.registerUser(email, password, username);

      res.status(201).json(result);

    } catch (error) {
      console.error('❌ Erro no registro:', error.message);

      const statusCode = error.message.includes('já cadastrado') ? 409 : 
                         error.message.includes('inválido') ? 400 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * POST /api/auth/login
 * Retorna { success, user_id, username, email, token }
 * 
 * Body: { email, password }
 * Response: { success, user_id, username, email, token }
 */
router.post(
  '/login',
  [
    body('email')
      .trim()
      .notEmpty().withMessage('Email é obrigatório')
      .isEmail().withMessage('Email inválido')
      .normalizeEmail(),
    
    body('password')
      .notEmpty().withMessage('Senha é obrigatória'),
    
    validate
  ],
  async (req, res) => {
    try {
      const { email, password } = req.body;

      const result = await authService.loginUser(email, password);

      res.json(result);

    } catch (error) {
      console.error('❌ Erro no login:', error.message);

      const statusCode = error.message.includes('incorretos') ? 401 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * GET /api/auth/profile
 * Rota protegida para testar middleware de autenticação
 * 
 * Headers: Authorization: Bearer <token>
 * Response: Dados do usuário
 */
router.get('/profile', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.user_id;
    
    const user = await require('../database').get(
      'SELECT id, username, email, created_at FROM users WHERE id = ?',
      [userId]
    );

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'Usuário não encontrado'
      });
    }

    res.json({
      success: true,
      user: user
    });

  } catch (error) {
    console.error('❌ Erro ao buscar perfil:', error.message);
    res.status(500).json({
      success: false,
      message: 'Erro ao buscar perfil'
    });
  }
});

module.exports = router;
