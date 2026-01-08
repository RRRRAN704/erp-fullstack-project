import { createRouter, createWebHistory } from 'vue-router'
import LayoutEmployee from '@/views/LayoutEmployee/index.vue'
import LayoutAdmin from '@/views/LayoutAdmin/index.vue'
import Login from '@/views/Login/index.vue'
import Reset from '@/views/Reset/index.vue'

import Employee from '@/views/Employee/index.vue'
import Department from '@/views/Department/index.vue'
import Position from '@/views/Position/index.vue'

import Contract from '@/views/Contract/index.vue'
import Customer from '@/views/Customer/index.vue'
import Dashboard from '@/views/Dashboard/index.vue'
import Data from '@/views/Data/index.vue'

import { useUserStore } from '@/stores/user'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      component: Login
    },
    {
      path: '/reset',
      component: Reset
    },
    {
      path: '/admin',
      component: LayoutAdmin,
      children: [
        {
          path: 'employee',
          component: Employee
        },
        {
          path: 'department',
          component: Department
        },
        {
          path: 'position',
          component: Position
        }
      ]
    },
    {
      path: '/employee',
      component: LayoutEmployee,
      children: [
        {
          path: 'dashboard',
          component: Dashboard
        },
        {
          path: 'contract',
          component: Contract
        },
        {
          path: 'customer',
          component: Customer
        },
        {
          path: 'chart',
          component: Data
        }
      ]
    },

  ],
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isLoggedIn = !!userStore.userInfo?.token
  const position = userStore.userInfo?.position
  const publicPages = ['/login', '/reset']
  const isPublic = publicPages.includes(to.path)

  if (!isPublic && !isLoggedIn) {
    // if the user is not logged in and trying to access a protected route
    next('/login')
  }

  // if the user is logged in, we can check their role
  if (isLoggedIn) {
    // admin cannot access employee routes
    if (position === 'System Admin' && to.path.startsWith('/employee')) {
      return next('/admin/employee')
    }

    // employee cannot access admin routes
    if (position !== 'System Admin' && to.path.startsWith('/admin')) {
      return next('/employee/dashboard')
    }
  }

  next()

})

export default router
