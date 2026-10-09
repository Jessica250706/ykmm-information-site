import { UserRouteName } from '@/constants'
import type { RouteRecordRaw } from 'vue-router'

export const userRoutes: RouteRecordRaw = {
  path: '/',
  component: () => import('@/layout/UserLayout.vue'),
  redirect: '/card',
  children: [
    // ---------- 卡面 ----------
    {
      path: 'card',
      name: UserRouteName.CARD_LIST,
      component: () => import('@/views/userCardPage/Index.vue'),
      meta: { title: '卡面' },
    },
    {
      path: 'card/:id',
      name: UserRouteName.CARD_DETAIL,
      component: () => import('@/views/userCardPage/CardDetail/Index.vue'),
      meta: { title: '卡面详情' },
    },
    {
      path: 'card/rc/browse',
      name: UserRouteName.CARD_RC_BROWSE,
      component: () => import('@/views/userCardPage/components/CardRCBrowse.vue'),
      meta: { title: 'RC 对话浏览' },
    },
    {
      path: 'card/rtv/browse',
      name: UserRouteName.CARD_RTV_BROWSE,
      component: () => import('@/views/userCardPage/components/CardRTVBrowse.vue'),
      meta: { title: 'RTV 对话浏览' },
    },
    {
      path: 'card/rabitter/browse',
      name: UserRouteName.CARD_RABITTER_BROWSE,
      component: () => import('@/views/userCardPage/components/CardRabitterBrowse.vue'),
      meta: { title: 'Rabitter 对话浏览' },
    },

    // ---------- 剧情 ----------
    {
      path: 'story',
      name: UserRouteName.STORY,
      component: () => import('@/views/userStoryPage/Index.vue'),
      meta: { title: '剧情' },
    },
    {
      path: 'story/browse/:type',
      name: UserRouteName.STORY_BROWSE,
      component: () => import('@/views/userBrowse/StoryBrowse.vue'),
      props: true,
      meta: { title: '剧情浏览' },
    },
    {
      path: 'story/browse/:type/:kind/:id',
      name: UserRouteName.STORY_BROWSE_DETAIL,
      component: () => import('@/views/userBrowse/StoryBrowse.vue'),
      props: true,
      meta: { title: '剧情浏览' },
    },

    // ---------- 个人中心 ----------
    {
      path: 'profile',
      name: UserRouteName.PROFILE,
      component: () => import('@/views/userProfilePage/Index.vue'),
      meta: { title: '个人中心', requiresAuth: true },
    },
  ],
}
