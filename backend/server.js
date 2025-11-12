const express = require('express');
const cors = require('cors');
const path = require('path');

const app = express();
const PORT = 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Importar rotas
const authRoutes = require('./routes/auth');
const reportsRoutes = require('./routes/reports');
const groupsRoutes = require('./routes/groups');
const feedRoutes = require('./routes/feed');

// Usar rotas
app.use('/api/auth', authRoutes);
app.use('/api/reports', reportsRoutes);
app.use('/api/groups', groupsRoutes);
app.use('/api/feed', feedRoutes);

// Rota de health check
app.get('/health', (req, res) => {
  res.json({ status: 'OK', message: 'CrimeTracker Backend está rodando' });
});

// Iniciar servidor
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 Servidor CrimeTracker rodando em http://0.0.0.0:${PORT}`);
  console.log(`📱 Acesso Android: http://10.0.2.2:${PORT}`);
});

