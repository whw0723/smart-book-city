import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  base: '/smart-book-city/', // GitHub Pages需要的base路径
  plugins: [vue()],
  resolve: {
    alias: {
      '@': '/src'
    }
  },
  server: {
    host: '0.0.0.0',
    port: 3000,
    // 允许所有主机访问，解决ngrok每次生成不同域名的问题
    allowedHosts: true,
    // 解决ngrok 403 Forbidden问题
    origin: '*',
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': '*',
      'Access-Control-Allow-Headers': '*'
    },
    strictPort: true
  }
})
