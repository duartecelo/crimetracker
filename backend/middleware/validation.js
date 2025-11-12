/**
 * Middleware de validação
 */

const { body, validationResult } = require('express-validator');
const { errorResponse, isValidEmail } = require('../utils');

/**
 * Middleware para processar erros de validação
 */
function handleValidationErrors(req, res, next) {
  const errors = validationResult(req);
  
  if (!errors.isEmpty()) {
    const errorMessages = errors.array().map(err => ({
      field: err.path,
      message: err.msg
    }));
    
    return res.status(400).json(
      errorResponse('Erro de validação', errorMessages)
    );
  }
  
  next();
}

/**
 * Validações para registro de usuário
 */
const validateRegister = [
  body('username')
    .trim()
    .isLength({ min: 3, max: 30 })
    .withMessage('Username deve ter entre 3 e 30 caracteres')
    .matches(/^[a-zA-Z0-9_]+$/)
    .withMessage('Username deve conter apenas letras, números e underscore'),
  
  body('email')
    .trim()
    .isEmail()
    .withMessage('Email inválido')
    .normalizeEmail(),
  
  body('password')
    .isLength({ min: 6 })
    .withMessage('Senha deve ter no mínimo 6 caracteres'),
  
  body('full_name')
    .trim()
    .isLength({ min: 3, max: 100 })
    .withMessage('Nome completo deve ter entre 3 e 100 caracteres'),
  
  body('phone')
    .optional()
    .trim()
    .matches(/^[\d\s\-\(\)\+]+$/)
    .withMessage('Telefone inválido'),
  
  handleValidationErrors
];

/**
 * Validações para login
 */
const validateLogin = [
  body('username')
    .trim()
    .notEmpty()
    .withMessage('Username ou email é obrigatório'),
  
  body('password')
    .notEmpty()
    .withMessage('Senha é obrigatória'),
  
  handleValidationErrors
];

/**
 * Validações para criação de denúncia
 */
const validateReport = [
  body('title')
    .trim()
    .isLength({ min: 5, max: 200 })
    .withMessage('Título deve ter entre 5 e 200 caracteres'),
  
  body('description')
    .trim()
    .isLength({ min: 10, max: 2000 })
    .withMessage('Descrição deve ter entre 10 e 2000 caracteres'),
  
  body('category')
    .trim()
    .notEmpty()
    .withMessage('Categoria é obrigatória')
    .isIn(['roubo', 'assalto', 'vandalismo', 'suspeita', 'outro'])
    .withMessage('Categoria inválida'),
  
  body('latitude')
    .isFloat({ min: -90, max: 90 })
    .withMessage('Latitude inválida'),
  
  body('longitude')
    .isFloat({ min: -180, max: 180 })
    .withMessage('Longitude inválida'),
  
  body('address')
    .optional()
    .trim()
    .isLength({ max: 500 })
    .withMessage('Endereço muito longo'),
  
  handleValidationErrors
];

/**
 * Validações para criação de grupo
 */
const validateGroup = [
  body('name')
    .trim()
    .isLength({ min: 3, max: 100 })
    .withMessage('Nome do grupo deve ter entre 3 e 100 caracteres'),
  
  body('description')
    .trim()
    .isLength({ min: 10, max: 1000 })
    .withMessage('Descrição deve ter entre 10 e 1000 caracteres'),
  
  body('latitude')
    .optional()
    .isFloat({ min: -90, max: 90 })
    .withMessage('Latitude inválida'),
  
  body('longitude')
    .optional()
    .isFloat({ min: -180, max: 180 })
    .withMessage('Longitude inválida'),
  
  body('radius_meters')
    .optional()
    .isInt({ min: 100, max: 10000 })
    .withMessage('Raio deve estar entre 100 e 10000 metros'),
  
  handleValidationErrors
];

/**
 * Validações para criação de post
 */
const validatePost = [
  body('content')
    .trim()
    .isLength({ min: 1, max: 2000 })
    .withMessage('Conteúdo deve ter entre 1 e 2000 caracteres'),
  
  body('group_id')
    .optional()
    .isInt()
    .withMessage('ID do grupo inválido'),
  
  handleValidationErrors
];

/**
 * Validações para comentário
 */
const validateComment = [
  body('content')
    .trim()
    .isLength({ min: 1, max: 500 })
    .withMessage('Comentário deve ter entre 1 e 500 caracteres'),
  
  handleValidationErrors
];

module.exports = {
  handleValidationErrors,
  validateRegister,
  validateLogin,
  validateReport,
  validateGroup,
  validatePost,
  validateComment
};

