require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('node:path');

const { authMiddleware } = require('./src/auth');
const authRoutes = require('./src/routes/authRoutes');
const licenseRoutes = require('./src/routes/licenseRoutes');
const sellerRoutes = require('./src/routes/sellerRoutes');
const partnerRoutes = require('./src/routes/partnerRoutes');
const adminRoutes = require('./src/routes/adminRoutes');
const systemRoutes = require('./src/routes/systemRoutes');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors({ origin: '*' }));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(authMiddleware);

// API Routes
app.use('/api/auth', authRoutes);
app.use('/api/license', licenseRoutes);
app.use('/api/sellers', sellerRoutes);
app.use('/api/partner', partnerRoutes);
app.use('/api/admin', adminRoutes);
app.use('/api', systemRoutes);

// Serve Partner Panel Static Web App
app.use(express.static(path.join(__dirname, 'public')));

// Catch-all route to serve the Partner Panel web client
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// Error handling
app.use((err, req, res, next) => {
  console.error('Unhandled server error:', err);
  res.status(500).json({
    success: false,
    error: {
      code: 'INTERNAL_SERVER_ERROR',
      message: err.message || 'An internal server error occurred.'
    }
  });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`=======================================================`);
  console.log(`  FZ ENGINE UNIFIED BACKEND & PARTNER PANEL ONLINE    `);
  console.log(`  Listening on: http://0.0.0.0:${PORT}                  `);
  console.log(`=======================================================`);
});
