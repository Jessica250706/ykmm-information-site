import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { adminRoutes } from './routes/admin'
import { userRoutes } from './routes/user'
import { setupRouterGuards } from './guards'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Index.vue'),
    meta: { title: '登录' },
  },
  adminRoutes,
  userRoutes,
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/forbidden/Index.vue'),
    meta: { title: '无权限' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/notFound/Index.vue'),
    meta: { title: '页面不存在' },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

setupRouterGuards(router)

export default router
