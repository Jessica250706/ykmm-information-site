export interface UserNavItem {
  label: string
  to: string
}

/** 顶部左侧导航（个人中心单独放在最右侧） */
export const userNav: UserNavItem[] = [
  { label: '卡面', to: '/cards' },
  { label: '剧情', to: '/stories' },
]
