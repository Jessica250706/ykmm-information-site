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
    // ---------- 剧情 ----------
    {
      path: 'story/browse/:type',
      name: 'UserStoryBrowse',
      component: () => import('@/views/userStoryPage/Browse.vue'),
      props: true,
      meta: { title: '剧情浏览' },
    },
    {
      path: 'story/browse/:type/:kind/:id',
      name: 'UserStoryBrowseDetail',
      component: () => import('@/views/userStoryPage/Browse.vue'),
      props: true,
      meta: { title: '剧情浏览' },
    },
    {
      path: 'profile',
      name: 'UserProfile',
      component: () => import('@/views/userProfilePage/Index.vue'),
      meta: { title: '个人中心', requiresAuth: true },
    },
  ],
}
