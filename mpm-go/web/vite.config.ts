import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/cos': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/geo_json_api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/get_face_img': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
