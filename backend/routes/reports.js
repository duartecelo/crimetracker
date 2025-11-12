/**
 * Rotas de Denúncias - CRIME-001
 */

const express = require('express');
const { body, query, param, validationResult } = require('express-validator');
const reportService = require('../services/reportService');
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
 * POST /api/reports
 * Cria denúncia (tipo, descrição, lat/lon)
 * 
 * Headers: Authorization: Bearer <token>
 * Body: { tipo, descricao, latitude, longitude }
 * Response: { success: true, data: {...} }
 */
router.post(
  '/',
  authenticateToken,
  [
    body('tipo')
      .trim()
      .notEmpty().withMessage('Tipo é obrigatório')
      .isIn(['Assalto', 'Furto', 'Agressão', 'Vandalismo', 'Roubo', 'Outro'])
      .withMessage('Tipo inválido. Use: Assalto, Furto, Agressão, Vandalismo, Roubo, Outro'),
    
    body('descricao')
      .trim()
      .notEmpty().withMessage('Descrição é obrigatória')
      .isLength({ max: 500 }).withMessage('Descrição deve ter no máximo 500 caracteres'),
    
    body('latitude')
      .isFloat({ min: -90, max: 90 }).withMessage('Latitude deve estar entre -90 e 90'),
    
    body('longitude')
      .isFloat({ min: -180, max: 180 }).withMessage('Longitude deve estar entre -180 e 180'),
    
    validate
  ],
  async (req, res) => {
    try {
      const userId = req.user.user_id;
      const { tipo, descricao, latitude, longitude } = req.body;

      const report = await reportService.createReport(
        userId, 
        tipo, 
        descricao, 
        parseFloat(latitude), 
        parseFloat(longitude)
      );

      res.status(201).json({
        success: true,
        data: report
      });

    } catch (error) {
      console.error('❌ Erro ao criar denúncia:', error.message);

      const statusCode = error.message.includes('inválido') || 
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
 * GET /api/reports/nearby
 * Retorna denúncias nos últimos 30 dias, dentro do raio informado
 * 
 * Query: ?latitude=X&longitude=Y&radius_km=5
 * Response: { success: true, data: [...] }
 */
router.get(
  '/nearby',
  authenticateToken,
  [
    query('latitude')
      .notEmpty().withMessage('Latitude é obrigatória')
      .isFloat({ min: -90, max: 90 }).withMessage('Latitude deve estar entre -90 e 90'),
    
    query('longitude')
      .notEmpty().withMessage('Longitude é obrigatória')
      .isFloat({ min: -180, max: 180 }).withMessage('Longitude deve estar entre -180 e 180'),
    
    query('radius_km')
      .optional()
      .isFloat({ min: 0.1, max: 100 }).withMessage('Raio deve estar entre 0.1 e 100 km'),
    
    validate
  ],
  async (req, res) => {
    try {
      const latitude = parseFloat(req.query.latitude);
      const longitude = parseFloat(req.query.longitude);
      const radiusKm = req.query.radius_km ? parseFloat(req.query.radius_km) : 5;

      const reports = await reportService.getNearbyReports(latitude, longitude, radiusKm);

      res.json({
        success: true,
        data: reports,
        count: reports.length,
        filters: {
          latitude,
          longitude,
          radius_km: radiusKm,
          last_days: 30
        }
      });

    } catch (error) {
      console.error('❌ Erro ao buscar denúncias próximas:', error.message);

      const statusCode = error.message.includes('inválido') ? 400 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * GET /api/reports/:id
 * Retorna detalhes da denúncia
 * 
 * Headers: Authorization: Bearer <token>
 * Response: { success: true, data: {...} }
 */
router.get(
  '/:id',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID da denúncia é obrigatório')
      .isUUID().withMessage('ID da denúncia inválido'),
    
    validate
  ],
  async (req, res) => {
    try {
      const reportId = req.params.id;

      const report = await reportService.getReportById(reportId);

      res.json({
        success: true,
        data: report
      });

    } catch (error) {
      console.error('❌ Erro ao buscar denúncia:', error.message);

      const statusCode = error.message.includes('não encontrada') ? 404 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * GET /api/reports/user/me
 * Retorna denúncias do usuário autenticado
 * 
 * Headers: Authorization: Bearer <token>
 * Response: { success: true, data: [...] }
 */
router.get(
  '/user/me',
  authenticateToken,
  async (req, res) => {
    try {
      const userId = req.user.user_id;

      const reports = await reportService.getUserReports(userId);

      res.json({
        success: true,
        data: reports,
        count: reports.length
      });

    } catch (error) {
      console.error('❌ Erro ao buscar denúncias do usuário:', error.message);

      res.status(500).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * PUT /api/reports/:id
 * Atualiza denúncia (somente o dono)
 * 
 * Headers: Authorization: Bearer <token>
 * Body: { tipo?, descricao? }
 * Response: { success: true, data: {...} }
 */
router.put(
  '/:id',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID da denúncia é obrigatório')
      .isUUID().withMessage('ID da denúncia inválido'),
    
    body('tipo')
      .optional()
      .trim()
      .isIn(['Assalto', 'Furto', 'Agressão', 'Vandalismo', 'Roubo', 'Outro'])
      .withMessage('Tipo inválido'),
    
    body('descricao')
      .optional()
      .trim()
      .isLength({ max: 500 }).withMessage('Descrição deve ter no máximo 500 caracteres'),
    
    validate
  ],
  async (req, res) => {
    try {
      const reportId = req.params.id;
      const userId = req.user.user_id;
      const updates = req.body;

      const report = await reportService.updateReport(reportId, userId, updates);

      res.json({
        success: true,
        data: report
      });

    } catch (error) {
      console.error('❌ Erro ao atualizar denúncia:', error.message);

      const statusCode = error.message.includes('não encontrada') || 
                         error.message.includes('permissão') ? 404 : 
                         error.message.includes('inválido') ? 400 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

/**
 * DELETE /api/reports/:id
 * Deleta denúncia (somente o dono)
 * 
 * Headers: Authorization: Bearer <token>
 * Response: { success: true, message: "..." }
 */
router.delete(
  '/:id',
  authenticateToken,
  [
    param('id')
      .notEmpty().withMessage('ID da denúncia é obrigatório')
      .isUUID().withMessage('ID da denúncia inválido'),
    
    validate
  ],
  async (req, res) => {
    try {
      const reportId = req.params.id;
      const userId = req.user.user_id;

      await reportService.deleteReport(reportId, userId);

      res.json({
        success: true,
        message: 'Denúncia deletada com sucesso'
      });

    } catch (error) {
      console.error('❌ Erro ao deletar denúncia:', error.message);

      const statusCode = error.message.includes('não encontrada') || 
                         error.message.includes('permissão') ? 404 : 500;

      res.status(statusCode).json({
        success: false,
        message: error.message
      });
    }
  }
);

module.exports = router;
