import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    // element plus plugins
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],

  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  

  //This Vite configuration sets up a reverse proxy. 
  // Any frontend request starting with /api is forwarded to http://localhost:8090.
  // The changeOrigin option makes the request header look like it comes from the backend domain. 
  // The rewrite removes the /api prefix so that /api/employee/list is sent to the backend as /employee/list. 
  // This solves CORS--Cross-Origin Resource Sharing--issues during local development.
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        //get rid of /api in the request
        rewrite: path => path.replace(/^\/api/, '')
      }
    }
  }
})
