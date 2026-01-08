import { createRouter, createWebHistory } from 'vue-router'

import Home from '@/views/Home/index.vue'
import Service from '@/views/Service/index.vue'
import Request from '@/views/Request/index.vue'
import Layout from '@/views/Layout/index.vue'
import Success from '@/views/FormSuccess/index.vue'





const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: Layout,
      children: [
        {
          path: '',
          component: Home
        },
        {
          path: 'service',
          component: Service
        },
        {
          path: 'request',
          component: Request
        },
      
      ]
    },
    {
      path: '/success',
      component: Success
    }
  ],
})

export default router
