export interface MenuItem {
  title: string
  icon?: string
  path?: string
  children?: MenuItem[]
}

export const adminMenu: MenuItem[] = [
  {
    title: '基础设定',
    icon: '⚙️',
    children: [
      { title: '菜单管理', path: '/admin/settings/menu' },
      { title: '用户管理', path: '/admin/settings/user' },
    ],
  },
  {
    title: '内容管理',
    icon: '📚',
    children: [
      { title: '人物管理', path: '/admin/content/character' },
      { title: '角色管理', path: '/admin/content/role' },
      { title: '卡面管理', path: '/admin/content/card' },
      { title: '卡面所属系列管理', path: '/admin/content/card-series' },
      { title: '偶像小人管理', path: '/admin/content/idol' },
      { title: '造型管理', path: '/admin/content/style' },
      { title: '剧情管理', path: '/admin/content/story' },
    ],
  },
]
