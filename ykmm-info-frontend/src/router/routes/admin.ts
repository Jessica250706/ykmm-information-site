import { ROLE } from '@/constants/index'
import type { RouteRecordRaw } from 'vue-router'

export const adminRoutes: RouteRecordRaw = {
  path: '/admin',
  component: () => import('@/layout/AdminLayout.vue'),
  redirect: '/admin/content/person',
  meta: { title: '管理端', requiresAuth: true, roles: [ROLE.ADMIN] },
  children: [
    // ---------- 基础设定 ----------
    // ---------- 菜单管理 ----------
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
    // ---------- 用户管理 ----------
    {
      path: 'settings/user',
      name: 'AdminUserManage',
      component: () => import('@/views/adminSettings/userManage/Index.vue'),
      meta: { title: '用户管理' },
    },

    // ---------- 内容管理 ----------
    // ---------- 人物管理 ----------
    {
      path: 'content/person',
      name: 'AdminPersonManage',
      component: () => import('@/views/adminContent/personManage/Index.vue'),
      meta: { title: '人物管理' },
    },
    {
      path: 'content/person/create',
      name: 'AdminPersonCreate',
      component: () => import('@/views/adminContent/personManage/components/EditPerson.vue'),
      meta: { title: '新增人物' },
    },
    {
      path: 'content/person/edit/:id',
      name: 'AdminPersonEdit',
      component: () => import('@/views/adminContent/personManage/components/EditPerson.vue'),
      meta: { title: '编辑人物' },
    },
    // ---------- 角色管理 ----------
    {
      path: 'content/role',
      name: 'AdminRoleManage',
      component: () => import('@/views/adminContent/roleManage/Index.vue'),
      meta: { title: '角色管理' },
    },
    {
      path: 'content/role/create',
      name: 'AdminRoleCreate',
      component: () => import('@/views/adminContent/roleManage/components/EditRole.vue'),
      meta: { title: '新增角色' },
    },
    {
      path: 'content/role/edit/:id',
      name: 'AdminRoleEdit',
      component: () => import('@/views/adminContent/roleManage/components/EditRole.vue'),
      meta: { title: '编辑角色' },
    },
    // ---------- 卡面管理 ----------
    {
      path: 'content/card',
      name: 'AdminCardManage',
      component: () => import('@/views/adminContent/cardManage/Index.vue'),
      meta: { title: '卡面管理' },
    },
    // ---------- 卡面所属系列管理 ----------
    {
      path: 'content/card-series',
      name: 'AdminCardSeriesManage',
      component: () => import('@/views/adminContent/cardSeriesManage/Index.vue'),
      meta: { title: '卡面所属系列管理' },
    },
    // ---------- 偶像小人管理 ----------
    {
      path: 'content/idol',
      name: 'AdminIdolManage',
      component: () => import('@/views/adminContent/idolManage/Index.vue'),
      meta: { title: '偶像小人管理' },
    },
    // ---------- 造型管理 ----------
    {
      path: 'content/style',
      name: 'AdminStyleManage',
      component: () => import('@/views/adminContent/styleManage/Index.vue'),
      meta: { title: '造型管理' },
    },
    // ---------- 剧情管理 ----------
    {
      path: 'content/story',
      name: 'AdminStoryManage',
      component: () => import('@/views/adminContent/storyManage/Index.vue'),
      meta: { title: '剧情管理' },
    },
    {
      path: 'content/story/create',
      name: 'AdminStoryCreate',
      component: () => import('@/views/adminContent/storyManage/components/EditStory.vue'),
      meta: { title: '新增剧情' },
    },
    {
      path: 'content/story/edit/:id',
      name: 'AdminStoryEdit',
      component: () => import('@/views/adminContent/storyManage/components/EditStory.vue'),
      meta: { title: '编辑剧情' },
    },
    // ---------- 剧情分类管理 ----------
    {
      path: 'content/story-category',
      name: 'AdminStoryCategoryManage',
      component: () => import('@/views/adminContent/storyCategoryManage/Index.vue'),
      meta: { title: '剧情分类管理' },
    },
    {
      path: 'content/story-category/create',
      name: 'AdminStoryCategoryCreate',
      component: () =>
        import('@/views/adminContent/storyCategoryManage/components/EditStoryCategory.vue'),
      meta: { title: '新增剧情分类' },
    },
    {
      path: 'content/story-category/edit/:id',
      name: 'AdminStoryCategoryEdit',
      component: () =>
        import('@/views/adminContent/storyCategoryManage/components/EditStoryCategory.vue'),
      meta: { title: '编辑剧情分类' },
    },
    {
      path: 'content/story-category/detail/:id',
      name: 'AdminStoryCategoryDetail',
      component: () =>
        import('@/views/adminContent/storyCategoryManage/components/DetailStoryCategory/Index.vue'),
      meta: { title: '剧情分类详情' },
    },
    // ---------- 剧情分类类型管理 ----------
    {
      path: 'settings/story-category-type',
      name: 'AdminStoryCategoryTypeManage',
      component: () => import('@/views/adminContent/storyCategoryTypeManage/Index.vue'),
      meta: { title: '剧情分类类型' },
    },
    {
      path: 'settings/story-category-type/edit/:id',
      name: 'AdminStoryCategoryTypeEdit',
      component: () =>
        import('@/views/adminContent/storyCategoryTypeManage/components/EditStoryCategoryType.vue'),
      meta: { title: '编辑剧情分类类型' },
    },
    // ---------- 贡献者管理 ----------
    {
      path: 'contributor',
      name: 'AdminDialogueVersionContributorManage',
      component: () => import('@/views/adminContent/contributorManage/Index.vue'),
      meta: { title: '贡献者管理' },
    },
    {
      path: 'contributor/create',
      name: 'AdminDialogueVersionContributorCreate',
      component: () =>
        import('@/views/adminContent/contributorManage/components/EditContributor.vue'),
      meta: { title: '新增贡献者' },
    },
    {
      path: 'contributor/edit/:id',
      name: 'AdminDialogueVersionContributorEdit',
      component: () =>
        import('@/views/adminContent/contributorManage/components/EditContributor.vue'),
      meta: { title: '编辑贡献者' },
    },
  ],
}
