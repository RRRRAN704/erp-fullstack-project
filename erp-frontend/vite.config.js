import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

//element-plus
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vite.dev/config/
export default defineConfig(({ mode }) =>({
  plugins: [
    vue(),
    vueDevTools(),
    // ...
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver({importStyle: "sass"})],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {

    //This Vite configuration sets up a reverse proxy. 
    // Any frontend request starting with /api is forwarded to http://localhost:8090. 
    // The changeOrigin option makes the request header look 
    // like it comes from the backend domain.
    // The rewrite removes the /api prefix so that /api/employee/list is sent to the backend 
    // as /employee/list. This solves CORS issues during local development.
    proxy: {
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, '')
      }
    }
  },
  // base: mode === 'production' ? '/management/' : '/', 
  base: '/',
}))
