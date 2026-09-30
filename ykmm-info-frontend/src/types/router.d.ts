import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    /** 页面标题，用于 document.title 和面包屑 */
    title?: string
    /** 是否需要登录 */
    requiresAuth?: boolean
    /** 允许的角色：1-管理员 2-普通用户 */
    roles?: number[]
  }
}

export {}
