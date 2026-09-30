import { useUserStore } from '@/stores/user'
import type { Router } from 'vue-router'

export function setupRouterGuards(router: Router) {
  router.beforeEach((to) => {
    const userStore = useUserStore()

    if (to.meta.requiresAuth && !userStore.token) {
      return { name: 'Login', query: { redirect: to.fullPath } }
    }

    const requiredRoles = to.meta.roles as number[] | undefined
    if (requiredRoles?.length && !requiredRoles.includes(userStore.userInfo?.role ?? -1)) {
      return { name: 'Forbidden' }
    }

    return true
  })

  router.afterEach((to) => {
    document.title = to.meta.title ? `${to.meta.title} · 我的应用` : '我的应用'
  })
}
