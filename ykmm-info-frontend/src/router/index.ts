import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      // component: () => import(''),
      redirect: '/home',
      children: [],
    },
    // {
    //   path: '/login',
    //   component: () => import('@/views/Login/Index.vue'),
    // },
  ],
  // 路由滚动行为定制
  scrollBehavior() {
    return {
      top: 0,
    }
  },
})

export default router
