/**
 * Configurações do servidor CrimeTracker
 */

module.exports = {
  // Configurações do servidor
  server: {
    port: process.env.PORT || 3000,
    host: '0.0.0.0',
    environment: process.env.NODE_ENV || 'development'
  },

  // Configurações JWT
  jwt: {
    secret: process.env.JWT_SECRET || 'crimetracker-secret-key-change-in-production',
    expiresIn: process.env.JWT_EXPIRES_IN || '7d',
    algorithm: 'HS256'
  },

  // Configurações do banco de dados
  database: {
    path: process.env.DB_PATH || './database/crimetracker.db',
    verbose: process.env.DB_VERBOSE === 'true'
  },

  // Configurações de upload
  upload: {
    path: process.env.UPLOAD_PATH || './uploads',
    maxFileSize: 5 * 1024 * 1024, // 5MB
    allowedMimeTypes: [
      'image/jpeg',
      'image/jpg',
      'image/png',
      'image/gif',
      'image/webp'
    ]
  },

  // Configurações de segurança
  security: {
    bcryptRounds: 10,
    maxLoginAttempts: 5,
    lockoutDuration: 15 * 60 * 1000, // 15 minutos
    rateLimitWindow: 15 * 60 * 1000, // 15 minutos
    rateLimitMax: 100 // máximo de requisições por janela
  },

  // Configurações de paginação
  pagination: {
    defaultLimit: 20,
    maxLimit: 100
  },

  // Configurações CORS
  cors: {
    origin: process.env.CORS_ORIGIN || '*',
    credentials: true
  }
};

