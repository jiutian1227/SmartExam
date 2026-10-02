import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // 说明：proxy 只在请求走相对路径时生效。
    // 当前 .env.development 里 VITE_API_URL 填的是绝对地址 http://localhost:8080，
    // 所以下面这组 proxy 实际不生效（保留用于"把 VITE_API_URL 留空"的场景）。
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/captcha-images': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
