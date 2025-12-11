import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    allowedHosts: [
      'localhost',
      '.ngrok-free.dev',
      '.ngrok.io',
      '.ngrok-free.app'
    ],
    proxy: {
      '/api': {
        target: process.env.VITE_API_URL || 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // Preserve original origin header for CORS
            const origin = req.headers.origin || req.headers.host;
            if (origin) {
              proxyReq.setHeader('Origin', origin.startsWith('http') ? origin : `http://${origin}`);
            }
          });
        }
      },
      '/uploads': {
        target: process.env.VITE_API_URL || 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            const origin = req.headers.origin || req.headers.host;
            if (origin) {
              proxyReq.setHeader('Origin', origin.startsWith('http') ? origin : `http://${origin}`);
            }
          });
        }
      }
    }
  }
})
