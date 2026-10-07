import type { RouteRecordRaw } from 'vue-router'

export const userRoutes: RouteRecordRaw = {
  path: '/',
  component: () => import('@/layout/UserLayout.vue'),
  redirect: '/card',
  children: [
    // ---------- 卡面 ----------
    {
      path: 'card',
      name: 'UserCardList',
      component: () => import('@/views/userCardPage/Index.vue'),
      meta: { title: '卡面' },
    },
    {
      path: 'card/:id',
      name: 'UserCardDetail',
      component: () => import('@/views/userCardPage/CardDetail.vue'),
      meta: { title: '卡面详情' },
    },
    {
      path: 'card-rc/edit',
      name: 'UserCardRcEdit',
      component: () => import('@/views/userCardPage/components/CardRCEdit.vue'),
      meta: { title: 'RC 对话编辑' },
    },
    {
      path: 'card-rtv/edit',
      name: 'UserCardRtvEdit',
      component: () => import('@/views/userCardPage/components/CardRTVEdit.vue'),
      meta: { title: 'RTV 对话编辑' },
    },
    // ---------- 剧情 ----------
    {
      path: 'story',
      name: 'UserStory',
      component: () => import('@/views/userStoryPage/Index.vue'),
      meta: { title: '剧情' },
    },
    {
      path: 'story/browse/:type',
      name: 'UserStoryBrowse',
      component: () => import('@/views/userBrowse/StoryBrowse.vue'),
      props: true,
      meta: { title: '剧情浏览' },
    },
    {
      path: 'story/browse/:type/:kind/:id',
      name: 'UserStoryBrowseDetail',
      component: () => import('@/views/userBrowse/StoryBrowse.vue'),
      props: true,
      meta: { title: '剧情浏览' },
    },
    // ---------- 个人中心 ----------
    {
      path: 'profile',
      name: 'UserProfile',
      component: () => import('@/views/userProfilePage/Index.vue'),
      meta: { title: '个人中心', requiresAuth: true },
    },
  ],
}
