import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    // 打包直接输出到后端静态目录,由 Spring Boot 一起托管(单服务部署)
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
    },
  },
})
