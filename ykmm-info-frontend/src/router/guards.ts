import type { Router } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

export function setupRouterGuards(router: Router) {
  router.beforeEach((to) => {
    const { token, userInfo } = useAuth()

    if (to.meta.requiresAuth && !token.value) {
      return { name: 'Login', query: { redirect: to.fullPath } }
    }

    // 管理端路由的 meta.roles 里放数字：roles: [1]
    const requiredRoles = to.meta.roles as number[] | undefined
    if (requiredRoles?.length && !requiredRoles.includes(userInfo.value?.role ?? -1)) {
      return { name: 'Forbidden' }
    }

    return true
  })

  router.afterEach((to) => {
    document.title = to.meta.title ? `${to.meta.title} · 我的应用` : '我的应用'
  })
}
