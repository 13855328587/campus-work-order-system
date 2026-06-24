import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue') },

      // 用户信息页面：所有登录用户都可以访问
      { path: 'profile', component: () => import('../views/Profile.vue') },

      { path: 'student/create-order', component: () => import('../views/student/CreateOrder.vue'), meta: { roles: ['STUDENT'] } },
      { path: 'student/my-orders', component: () => import('../views/student/MyOrders.vue'), meta: { roles: ['STUDENT'] } },

      { path: 'admin/orders', component: () => import('../views/admin/OrderManage.vue'), meta: { roles: ['ADMIN', 'SUPER_ADMIN'] } },
      { path: 'admin/users', component: () => import('../views/admin/UserManage.vue'), meta: { roles: ['ADMIN', 'SUPER_ADMIN'] } },

      { path: 'worker/tasks', component: () => import('../views/worker/MyTasks.vue'), meta: { roles: ['WORKER'] } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }

  if (!userStore.token) {
    next('/login')
    return
  }

  const roles = to.meta.roles
  if (roles && !roles.includes(userStore.role)) {
    next('/dashboard')
    return
  }

  next()
})

export default router