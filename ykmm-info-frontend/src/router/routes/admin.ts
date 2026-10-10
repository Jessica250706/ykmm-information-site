import { AdminRouteName, ROLE } from '@/constants'
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
      name: AdminRouteName.MENU_MANAGE,
      component: () => import('@/views/adminSettings/menuManage/Index.vue'),
      meta: { title: '菜单管理' },
    },
    {
      path: 'settings/menu/create',
      name: AdminRouteName.MENU_CREATE,
      component: () => import('@/views/adminSettings/menuManage/components/EditMenu.vue'),
      meta: { title: '新增菜单' },
    },
    {
      path: 'settings/menu/edit/:id',
      name: AdminRouteName.MENU_EDIT,
      component: () => import('@/views/adminSettings/menuManage/components/EditMenu.vue'),
      meta: { title: '编辑菜单' },
    },
    // ---------- 用户管理 ----------
    {
      path: 'settings/user',
      name: AdminRouteName.USER_MANAGE,
      component: () => import('@/views/adminSettings/userManage/Index.vue'),
      meta: { title: '用户管理' },
    },

    // ---------- 内容管理 ----------
    // ---------- 人物管理 ----------
    {
      path: 'content/person',
      name: AdminRouteName.PERSON_MANAGE,
      component: () => import('@/views/adminContent/personManage/Index.vue'),
      meta: { title: '人物管理' },
    },
    {
      path: 'content/person/create',
      name: AdminRouteName.PERSON_CREATE,
      component: () => import('@/views/adminContent/personManage/components/EditPerson.vue'),
      meta: { title: '新增人物' },
    },
    {
      path: 'content/person/edit/:id',
      name: AdminRouteName.PERSON_EDIT,
      component: () => import('@/views/adminContent/personManage/components/EditPerson.vue'),
      meta: { title: '编辑人物' },
    },
    // ---------- 角色管理 ----------
    {
      path: 'content/role',
      name: AdminRouteName.ROLE_MANAGE,
      component: () => import('@/views/adminContent/roleManage/Index.vue'),
      meta: { title: '角色管理' },
    },
    {
      path: 'content/role/create',
      name: AdminRouteName.ROLE_CREATE,
      component: () => import('@/views/adminContent/roleManage/components/EditRole.vue'),
      meta: { title: '新增角色' },
    },
    {
      path: 'content/role/edit/:id',
      name: AdminRouteName.ROLE_EDIT,
      component: () => import('@/views/adminContent/roleManage/components/EditRole.vue'),
      meta: { title: '编辑角色' },
    },
    // ---------- 卡面系列管理 ----------
    {
      path: 'content/card-series',
      name: AdminRouteName.CARD_SERIES_MANAGE,
      component: () => import('@/views/adminContent/cardSeriesManage/Index.vue'),
      meta: { title: '卡面系列' },
    },
    {
      path: 'content/card-series/create',
      name: AdminRouteName.CARD_SERIES_CREATE,
      component: () =>
        import('@/views/adminContent/cardSeriesManage/components/EditCardSeries.vue'),
      meta: { title: '新增卡面系列' },
    },
    {
      path: 'content/card-series/edit/:id',
      name: AdminRouteName.CARD_SERIES_EDIT,
      component: () =>
        import('@/views/adminContent/cardSeriesManage/components/EditCardSeries.vue'),
      meta: { title: '编辑卡面系列' },
    },
    // ---------- 卡面管理 ----------
    {
      path: 'content/card',
      name: AdminRouteName.CARD_MANAGE,
      component: () => import('@/views/adminContent/cardManage/Index.vue'),
      meta: { title: '卡面管理' },
    },
    {
      path: 'content/card/create',
      name: AdminRouteName.CARD_CREATE,
      component: () => import('@/views/adminContent/cardManage/components/EditCard.vue'),
      meta: { title: '新增卡面' },
    },
    {
      path: 'content/card/edit/:id',
      name: AdminRouteName.CARD_EDIT,
      component: () => import('@/views/adminContent/cardManage/components/EditCard.vue'),
      meta: { title: '编辑卡面' },
    },
    // ---------- 卡面 RC 管理 ----------
    {
      path: 'content/card-rc',
      name: AdminRouteName.CARD_RC_MANAGE,
      component: () => import('@/views/adminContent/cardRCManage/Index.vue'),
      meta: { title: 'RC 管理' },
    },
    // ---------- 卡面 RTV 管理 ----------
    {
      path: 'content/card-rtv',
      name: AdminRouteName.CARD_RTV_MANAGE,
      component: () => import('@/views/adminContent/cardRTVManage/Index.vue'),
      meta: { title: 'RTV 管理' },
    },
    // ---------- 偶像小人管理 ----------
    {
      path: 'content/idol',
      name: AdminRouteName.IDOL_MANAGE,
      component: () => import('@/views/adminContent/idolManage/Index.vue'),
      meta: { title: '偶像小人管理' },
    },
    // ---------- 造型管理 ----------
    {
      path: 'content/style',
      name: AdminRouteName.STYLE_MANAGE,
      component: () => import('@/views/adminContent/styleManage/Index.vue'),
      meta: { title: '造型管理' },
    },
    // ---------- 剧情管理 ----------
    {
      path: 'content/story',
      name: AdminRouteName.STORY_MANAGE,
      component: () => import('@/views/adminContent/storyManage/Index.vue'),
      meta: { title: '剧情管理' },
    },
    {
      path: 'content/story/create',
      name: AdminRouteName.STORY_CREATE,
      component: () => import('@/views/adminContent/storyManage/components/EditStory.vue'),
      meta: { title: '新增剧情' },
    },
    {
      path: 'content/story/edit/:id',
      name: AdminRouteName.STORY_EDIT,
      component: () => import('@/views/adminContent/storyManage/components/EditStory.vue'),
      meta: { title: '编辑剧情' },
    },
    // ---------- 剧情分类管理 ----------
    {
      path: 'content/story-category',
      name: AdminRouteName.STORY_CATEGORY_MANAGE,
      component: () => import('@/views/adminContent/storyCategoryManage/Index.vue'),
      meta: { title: '剧情分类管理' },
    },
    {
      path: 'content/story-category/create',
      name: AdminRouteName.STORY_CATEGORY_CREATE,
      component: () =>
        import('@/views/adminContent/storyCategoryManage/components/EditStoryCategory.vue'),
      meta: { title: '新增剧情分类' },
    },
    {
      path: 'content/story-category/edit/:id',
      name: AdminRouteName.STORY_CATEGORY_EDIT,
      component: () =>
        import('@/views/adminContent/storyCategoryManage/components/EditStoryCategory.vue'),
      meta: { title: '编辑剧情分类' },
    },
    {
      path: 'content/story-category/detail/:id',
      name: AdminRouteName.STORY_CATEGORY_DETAIL,
      component: () =>
        import('@/views/adminContent/storyCategoryManage/components/DetailStoryCategory/Index.vue'),
      meta: { title: '剧情分类详情' },
    },
    // ---------- 剧情分类类型管理 ----------
    {
      path: 'settings/story-category-type',
      name: AdminRouteName.STORY_CATEGORY_TYPE_MANAGE,
      component: () => import('@/views/adminContent/storyCategoryTypeManage/Index.vue'),
      meta: { title: '剧情分类类型' },
    },
    {
      path: 'settings/story-category-type/edit/:id',
      name: AdminRouteName.STORY_CATEGORY_TYPE_EDIT,
      component: () =>
        import('@/views/adminContent/storyCategoryTypeManage/components/EditStoryCategoryType.vue'),
      meta: { title: '编辑剧情分类类型' },
    },
    // ---------- 表情包管理 ----------
    {
      path: 'content/sticker',
      name: AdminRouteName.STICKER_MANAGE,
      component: () => import('@/views/adminContent/stickerManage/Index.vue'),
      meta: { title: '表情包管理' },
    },
    {
      path: 'content/sticker/create',
      name: AdminRouteName.STICKER_CREATE,
      component: () => import('@/views/adminContent/stickerManage/components/EditSticker.vue'),
      meta: { title: '新增表情包' },
    },
    {
      path: 'content/sticker/edit/:id',
      name: AdminRouteName.STICKER_EDIT,
      component: () => import('@/views/adminContent/stickerManage/components/EditSticker.vue'),
      meta: { title: '编辑表情包' },
    },
    // ---------- 表情包分组管理 ----------
    {
      path: 'content/sticker-group',
      name: AdminRouteName.STICKER_GROUP_MANAGE,
      component: () => import('@/views/adminContent/stickerGroupManage/Index.vue'),
      meta: { title: '表情包分组' },
    },
    {
      path: 'content/sticker-group/create',
      name: AdminRouteName.STICKER_GROUP_CREATE,
      component: () =>
        import('@/views/adminContent/stickerGroupManage/components/EditStickerGroup.vue'),
      meta: { title: '新增表情包分组' },
    },
    {
      path: 'content/sticker-group/edit/:id',
      name: AdminRouteName.STICKER_GROUP_EDIT,
      component: () =>
        import('@/views/adminContent/stickerGroupManage/components/EditStickerGroup.vue'),
      meta: { title: '编辑表情包分组' },
    },
    // ---------- 贡献者管理 ----------
    {
      path: 'contributor',
      name: AdminRouteName.CONTRIBUTOR_MANAGE,
      component: () => import('@/views/adminContent/contributorManage/Index.vue'),
      meta: { title: '贡献者管理' },
    },
    {
      path: 'contributor/create',
      name: AdminRouteName.CONTRIBUTOR_CREATE,
      component: () =>
        import('@/views/adminContent/contributorManage/components/EditContributor.vue'),
      meta: { title: '新增贡献者' },
    },
    {
      path: 'contributor/edit/:id',
      name: AdminRouteName.CONTRIBUTOR_EDIT,
      component: () =>
        import('@/views/adminContent/contributorManage/components/EditContributor.vue'),
      meta: { title: '编辑贡献者' },
    },
  ],
}
