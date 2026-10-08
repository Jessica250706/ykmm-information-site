import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getMenuTreeAPI } from '@/api/menu'
import { MENU_TYPE } from '@/constants'
import type { MenuVO } from '@/types/menu'

/** 只保留 visible === 1 的节点，空 children 转 undefined */
function normalize(nodes: MenuVO[]): MenuVO[] {
  return nodes
    .filter((n) => n.visible === 1)
    .map((n) => {
      const children = n.children ? normalize(n.children) : []
      return {
        ...n,
        children: children.length > 0 ? children : undefined,
      }
    })
}

export const useMenuStore = defineStore(
  'menu',
  () => {
    const adminMenu = ref<MenuVO[]>([])
    const userMenu = ref<MenuVO[]>([])
    const loading = ref(false)

    /** 加载管理端菜单 */
    async function loadAdminMenu() {
      loading.value = true
      try {
        const res = await getMenuTreeAPI(MENU_TYPE.ADMIN)
        adminMenu.value = normalize(res.data ?? [])
      } finally {
        loading.value = false
      }
    }

    /** 加载用户端菜单 */
    async function loadUserMenu() {
      loading.value = true
      try {
        const res = await getMenuTreeAPI(MENU_TYPE.USER)
        userMenu.value = normalize(res.data ?? [])
      } finally {
        loading.value = false
      }
    }

    /** 加载所有（登录后调用） */
    async function loadAll() {
      await Promise.all([loadAdminMenu(), loadUserMenu()])
    }

    /** 清空（退出登录时） */
    function clear() {
      adminMenu.value = []
      userMenu.value = []
    }

    /** 用户端导航拍平成一级（顶部导航不带嵌套） */
    const userNavFlat = computed(() =>
      userMenu.value.flatMap((n) => (n.path ? [{ name: n.name, path: n.path }] : [])),
    )

    return {
      adminMenu,
      userMenu,
      userNavFlat,
      loading,
      loadAdminMenu,
      loadUserMenu,
      loadAll,
      clear,
    }
  },
  { persist: true },
)
