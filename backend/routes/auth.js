const express = require('express');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { body, validationResult } = require('express-validator');
const db = require('../database/db');

const router = express.Router();
const JWT_SECRET = 'crimetracker-secret-key-change-in-production';

// Middleware de validação de erros
const validate = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  next();
};

// Registro de usuário
router.post('/register',
  [
    body('username').trim().isLength({ min: 3 }).withMessage('Username deve ter no mínimo 3 caracteres'),
    body('email').isEmail().withMessage('Email inválido'),
    body('password').isLength({ min: 6 }).withMessage('Senha deve ter no mínimo 6 caracteres'),
    body('full_name').trim().notEmpty().withMessage('Nome completo é obrigatório')
  ],
  validate,
  async (req, res) => {
    try {
      const { username, email, password, full_name, phone, address, latitude, longitude } = req.body;

      // Verificar se usuário já existe
      const existingUser = db.prepare('SELECT id FROM users WHERE username = ? OR email = ?').get(username, email);
      if (existingUser) {
        return res.status(409).json({ error: 'Usuário ou email já existe' });
      }

      // Hash da senha
      const password_hash = await bcrypt.hash(password, 10);

      // Inserir usuário
      const stmt = db.prepare(`
        INSERT INTO users (username, email, password_hash, full_name, phone, address, latitude, longitude)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
      `);
      const result = stmt.run(username, email, password_hash, full_name, phone || null, address || null, latitude || null, longitude || null);

      // Gerar token JWT
      const token = jwt.sign({ userId: result.lastInsertRowid, username }, JWT_SECRET, { expiresIn: '7d' });

      res.status(201).json({
        message: 'Usuário registrado com sucesso',
        token,
        user: {
          id: result.lastInsertRowid,
          username,
          email,
          full_name
        }
      });
    } catch (error) {
      console.error('Erro no registro:', error);
      res.status(500).json({ error: 'Erro ao registrar usuário' });
    }
  }
);

// Login
router.post('/login',
  [
    body('username').trim().notEmpty().withMessage('Username é obrigatório'),
    body('password').notEmpty().withMessage('Senha é obrigatória')
  ],
  validate,
  async (req, res) => {
    try {
      const { username, password } = req.body;

      // Buscar usuário
      const user = db.prepare('SELECT * FROM users WHERE username = ? OR email = ?').get(username, username);
      if (!user) {
        return res.status(401).json({ error: 'Credenciais inválidas' });
      }

      // Verificar senha
      const validPassword = await bcrypt.compare(password, user.password_hash);
      if (!validPassword) {
        return res.status(401).json({ error: 'Credenciais inválidas' });
      }

      // Gerar token JWT
      const token = jwt.sign({ userId: user.id, username: user.username }, JWT_SECRET, { expiresIn: '7d' });

      res.json({
        message: 'Login realizado com sucesso',
        token,
        user: {
          id: user.id,
          username: user.username,
          email: user.email,
          full_name: user.full_name
        }
      });
    } catch (error) {
      console.error('Erro no login:', error);
      res.status(500).json({ error: 'Erro ao realizar login' });
    }
  }
);

// Middleware de autenticação
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ error: 'Token não fornecido' });
  }

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ error: 'Token inválido' });
    }
    req.user = user;
    next();
  });
};

// Obter perfil do usuário
router.get('/profile', authenticateToken, (req, res) => {
  try {
    const user = db.prepare('SELECT id, username, email, full_name, phone, address, latitude, longitude, created_at FROM users WHERE id = ?').get(req.user.userId);
    if (!user) {
      return res.status(404).json({ error: 'Usuário não encontrado' });
    }
    res.json(user);
  } catch (error) {
    console.error('Erro ao buscar perfil:', error);
    res.status(500).json({ error: 'Erro ao buscar perfil' });
  }
});

module.exports = router;
module.exports.authenticateToken = authenticateToken;

