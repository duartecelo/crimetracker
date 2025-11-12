/**
 * CrimeTracker Backend Server
 * Sistema local de denúncias e grupos de bairro
 */

const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');
const fs = require('fs');

// Importar configurações e utilitários
const config = require('./config');
const database = require('./database');
const { successResponse } = require('./utils');

// Importar middleware
const { notFoundHandler, errorHandler } = require('./middleware/errorHandler');

// Importar rotas
const authRoutes = require('./routes/auth');
const reportsRoutes = require('./routes/reports');
const groupsRoutes = require('./routes/groups');
const feedRoutes = require('./routes/feed');

// Criar aplicação Express
const app = express();

// Middleware global
app.use(cors(config.cors));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Criar diretório de uploads se não existir
const uploadDir = path.join(__dirname, config.upload.path);
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
  console.log('✅ Diretório de uploads criado:', uploadDir);
}

// Servir arquivos estáticos (uploads)
app.use('/uploads', express.static(uploadDir));

// Logging de requisições (apenas em desenvolvimento)
if (config.server.environment === 'development') {
  app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
    next();
  });
}

// Rota de health check
app.get('/health', (req, res) => {
  res.json(
    successResponse(
      {
        status: 'online',
        timestamp: new Date().toISOString(),
        environment: config.server.environment,
        database: 'connected'
      },
      'Servidor rodando'
    )
  );
});

// Rotas da API
app.use('/api/auth', authRoutes);
app.use('/api/reports', reportsRoutes);
app.use('/api/groups', groupsRoutes);
app.use('/api', feedRoutes);

// Middleware de tratamento de erros
app.use(notFoundHandler);
app.use(errorHandler);

// Função para iniciar o servidor
async function startServer() {
  try {
    // Inicializar banco de dados
    console.log('📦 Inicializando banco de dados...');
    await database.initDatabase();

    // Buscar estatísticas do banco de dados
    const userCount = await database.get('SELECT COUNT(*) as count FROM users');
    const reportCount = await database.get('SELECT COUNT(*) as count FROM crime_reports');
    const groupCount = await database.get('SELECT COUNT(*) as count FROM groups');
    const postCount = await database.get('SELECT COUNT(*) as count FROM posts');

    // Iniciar servidor HTTP
    const server = app.listen(config.server.port, config.server.host, () => {
      console.log('');
      console.log('╔════════════════════════════════════════════╗');
      console.log('║                                            ║');
      console.log('║   🚀 CrimeTracker Backend Rodando!       ║');
      console.log('║                                            ║');
      console.log('╚════════════════════════════════════════════╝');
      console.log('');
      console.log(`🌐 Servidor: http://${config.server.host}:${config.server.port}`);
      console.log(`📱 Android: http://10.0.2.2:${config.server.port}`);
      console.log(`🔧 Ambiente: ${config.server.environment}`);
      console.log('');
      console.log('💾 Banco de Dados:');
      console.log(`   Caminho: ${config.database.path}`);
      console.log(`   👤 Usuários: ${userCount.count}`);
      console.log(`   🚨 Denúncias: ${reportCount.count}`);
      console.log(`   👥 Grupos: ${groupCount.count}`);
      console.log(`   📰 Posts: ${postCount.count}`);
      console.log('');
      console.log('📡 Endpoints disponíveis:');
      console.log(`   GET  /health`);
      console.log(``);
      console.log(`   🔐 AUTH-001:`);
      console.log(`   POST /api/auth/register`);
      console.log(`   POST /api/auth/login`);
      console.log(`   GET  /api/auth/profile`);
      console.log(``);
      console.log(`   🚨 CRIME-001:`);
      console.log(`   POST /api/reports`);
      console.log(`   GET  /api/reports/nearby`);
      console.log(`   GET  /api/reports/:id`);
      console.log(``);
      console.log(`   👥 GROUP-001:`);
      console.log(`   POST /api/groups`);
      console.log(`   GET  /api/groups`);
      console.log(`   POST /api/groups/:id/join`);
      console.log(`   POST /api/groups/:id/leave`);
      console.log(``);
      console.log(`   📰 FEED-001:`);
      console.log(`   POST /api/groups/:group_id/posts`);
      console.log(`   GET  /api/groups/:group_id/posts`);
      console.log(`   DELETE /api/posts/:id`);
      console.log(`   GET  /api/feed`);
      console.log('');
      console.log('✅ Pronto para receber requisições!');
      console.log('');
    });

    // Tratamento de sinais de encerramento
    process.on('SIGTERM', () => gracefulShutdown(server));
    process.on('SIGINT', () => gracefulShutdown(server));

  } catch (error) {
    console.error('❌ Erro ao iniciar servidor:', error);
    process.exit(1);
  }
}

// Função para encerramento gracioso
async function gracefulShutdown(server) {
  console.log('');
  console.log('🔄 Encerrando servidor...');

  server.close(async () => {
    console.log('✅ Servidor HTTP encerrado');

    try {
      await database.closeDatabase();
      console.log('✅ Banco de dados fechado');
      process.exit(0);
    } catch (error) {
      console.error('❌ Erro ao fechar banco de dados:', error);
      process.exit(1);
    }
  });

  // Forçar encerramento após 10 segundos
  setTimeout(() => {
    console.error('⚠️  Encerramento forçado após timeout');
    process.exit(1);
  }, 10000);
}

// Iniciar servidor
startServer();

module.exports = app;

