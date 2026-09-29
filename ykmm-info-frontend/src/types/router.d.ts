import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    /** 页面标题，用于 document.title 和面包屑 */
    title?: string
    /** 是否需要登录 */
    requiresAuth?: boolean
    /** 需要的角色，命中任意一个即可 */
    roles?: string[]
  }
}

export {}
