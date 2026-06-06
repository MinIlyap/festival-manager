import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const rewrite = (path: string) => path.replace(/^\/api/, '')

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/tests/setup.ts'],
  },
  server: {
    port: 3000,
    proxy: {
      '/api/auth':           { target: 'http://localhost:8082', changeOrigin: true, rewrite },
      '/api/oauth2':         { target: 'http://localhost:8082', changeOrigin: true, rewrite },
      '/api/application':    { target: 'http://localhost:8081', changeOrigin: true, rewrite },
      '/api/company':        { target: 'http://localhost:8083', changeOrigin: true, rewrite },
      '/api/qr/events':      { target: 'http://localhost:8084', changeOrigin: true, rewrite },
      '/api/events':         { target: 'http://localhost:8084', changeOrigin: true, rewrite },
      '/api/qr/festivals':   { target: 'http://localhost:8085', changeOrigin: true, rewrite },
      '/api/ticket':         { target: 'http://localhost:8085', changeOrigin: true, rewrite },
      '/api/festival':       { target: 'http://localhost:8085', changeOrigin: true, rewrite },
      '/api/file':           { target: 'http://localhost:8086', changeOrigin: true, rewrite },
      '/api/location':       { target: 'http://localhost:8087', changeOrigin: true, rewrite },
      '/api/notification':   { target: 'http://localhost:8088', changeOrigin: true, rewrite },
      '/api/user':           { target: 'http://localhost:8089', changeOrigin: true, rewrite },
    },
  },
})
