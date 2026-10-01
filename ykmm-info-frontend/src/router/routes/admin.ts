import { ROLE } from '@/constants/index'
import type { RouteRecordRaw } from 'vue-router'

export const adminRoutes: RouteRecordRaw = {
  path: '/admin',
  component: () => import('@/layout/AdminLayout.vue'),
  redirect: '/admin/content/character',
  meta: { title: '管理端', requiresAuth: true, roles: [ROLE.ADMIN] },
  children: [
    // ---------- 基础设定 ----------
    {
      path: 'settings/menu',
      name: 'AdminMenuManage',
      component: () => import('@/views/adminSettings/menuManage/Index.vue'),
      meta: { title: '菜单管理' },
    },
    {
      path: 'settings/menu/create',
      name: 'AdminMenuCreate',
      component: () => import('@/views/adminSettings/menuManage/components/EditMenu.vue'),
      meta: { title: '新增菜单' },
    },
    {
      path: 'settings/menu/edit/:id',
      name: 'AdminMenuEdit',
      component: () => import('@/views/adminSettings/menuManage/components/EditMenu.vue'),
      meta: { title: '编辑菜单' },
    },
    {
      path: 'settings/user',
      name: 'AdminUserManage',
      component: () => import('@/views/adminSettings/userManage/Index.vue'),
      meta: { title: '用户管理' },
    },

    // ---------- 内容管理 ----------
    {
      path: 'content/character',
      name: 'AdminCharacterManage',
      component: () => import('@/views/adminContent/characterManage/Index.vue'),
      meta: { title: '人物管理' },
    },
    {
      path: 'content/role',
      name: 'AdminRoleManage',
      component: () => import('@/views/adminContent/roleManage/Index.vue'),
      meta: { title: '角色管理' },
    },
    {
      path: 'content/card',
      name: 'AdminCardManage',
      component: () => import('@/views/adminContent/cardManage/Index.vue'),
      meta: { title: '卡面管理' },
    },
    {
      path: 'content/card-series',
      name: 'AdminCardSeriesManage',
      component: () => import('@/views/adminContent/cardSeriesManage/Index.vue'),
      meta: { title: '卡面所属系列管理' },
    },
    {
      path: 'content/idol',
      name: 'AdminIdolManage',
      component: () => import('@/views/adminContent/idolManage/Index.vue'),
      meta: { title: '偶像小人管理' },
    },
    {
      path: 'content/style',
      name: 'AdminStyleManage',
      component: () => import('@/views/adminContent/styleManage/Index.vue'),
      meta: { title: '造型管理' },
    },
    {
      path: 'content/story',
      name: 'AdminStoryManage',
      component: () => import('@/views/adminContent/storyManage/Index.vue'),
      meta: { title: '剧情管理' },
    },
  ],
}
