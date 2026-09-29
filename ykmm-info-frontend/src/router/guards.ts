import type { Router } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

export function setupRouterGuards(router: Router) {
  // 全局前置守卫：登录校验 + 角色校验
  router.beforeEach((to) => {
    const { token, roles } = useAuth()

    if (to.meta.requiresAuth && !token.value) {
      return {
        name: 'Login',
        query: { redirect: to.fullPath },
      }
    }

    const requiredRoles = to.meta.roles
    if (requiredRoles?.length && !requiredRoles.some((r) => roles.value.includes(r))) {
      return { name: 'Forbidden' }
    }

    return true
  })

  // 全局后置守卫：标题
  router.afterEach((to) => {
    document.title = to.meta.title ? `${to.meta.title} · 我的应用` : '我的应用'
  })
}
