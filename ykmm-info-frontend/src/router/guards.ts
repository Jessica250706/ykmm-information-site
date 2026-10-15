import { CommonRouteName, ROLE } from '@/constants'
import { useUserStore } from '@/stores'
import type { Router } from 'vue-router'

export function setupRouterGuards(router: Router) {
  router.beforeEach((to) => {
    const userStore = useUserStore()

    // 1. 需要认证但未登录 → 去登录页，带上 redirect
    if (to.meta.requiresAuth && !userStore.token) {
      return {
        name: CommonRouteName.LOGIN,
        query: { redirect: to.fullPath },
      }
    }

    // 2. 需要特定角色但角色不匹配 → 无权限页
    const requiredRoles = to.meta.roles as number[] | undefined
    if (requiredRoles?.length) {
      const role = userStore.userInfo?.role ?? -1
      if (!requiredRoles.includes(role)) {
        return { name: CommonRouteName.FORBIDDEN }
      }
    }

    // 3. 已登录用户访问登录页 → 直接去首页（可选，体验更好）
    if (userStore.token && to.name === CommonRouteName.LOGIN) {
      const role = userStore.userInfo?.role ?? ROLE.USER
      return role === ROLE.ADMIN ? '/admin' : '/card'
    }

    return true
  })

  router.afterEach((to) => {
    document.title = to.meta.title ? `${to.meta.title} · 我的应用` : '我的应用'
  })
}
