import type { RouteRecordRaw } from 'vue-router'

export const userRoutes: RouteRecordRaw = {
  path: '/',
  component: () => import('@/layout/UserLayout.vue'),
  redirect: '/cards',
  children: [
    {
      path: 'cards',
      name: 'UserCards',
      component: () => import('@/views/userCardPage/Index.vue'),
      meta: { title: '卡面' },
    },
    {
      path: 'stories',
      name: 'UserStories',
      component: () => import('@/views/userStoryPage/Index.vue'),
      meta: { title: '剧情' },
    },
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
