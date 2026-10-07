<template>
  <el-container class="h-screen bg-slate-50 text-slate-800">
    <!-- 侧边栏 -->
    <el-aside class="admin-aside flex flex-col" width="240px">
      <div
        :style="{ borderColor: 'var(--menu-border)' }"
        class="flex h-16 shrink-0 items-center gap-2 px-5"
      >
        <el-avatar
          :size="32"
          :style="{ backgroundColor: 'var(--el-color-primary)' }"
          class="text-sm font-bold"
          shape="square"
        >
          A
        </el-avatar>
        <span class="font-semibold text-slate-800">管理后台</span>
      </div>

      <el-scrollbar class="flex-1">
        <el-menu :default-active="defaultActiveMenu" class="admin-menu" router>
          <AdminMenuItem v-for="item in menuStore.adminMenu" :key="item.title" :item="item" />
        </el-menu>
      </el-scrollbar>

      <div class="shrink-0 p-3">
        <RouterLink
          :style="{ backgroundColor: 'var(--menu-hover-bg)', color: 'var(--menu-text)' }"
          class="flex items-center justify-center gap-1 rounded-lg px-3 py-2 text-sm transition hover:brightness-95"
          to="/card"
        >
          前往用户端 →
        </RouterLink>
      </div>
    </el-aside>

    <!-- 主区域 -->
    <el-container class="min-w-0 flex-1 flex-col">
      <el-header
        class="flex shrink-0 items-center justify-between border-b border-slate-200 bg-white px-6"
        height="64px"
      >
        <el-breadcrumb separator="/">
          <el-breadcrumb-item v-for="(item, i) in breadcrumbs" :key="item">
            <span :class="i === breadcrumbs.length - 1 ? 'font-medium text-slate-900' : ''">
              {{ item }}
            </span>
          </el-breadcrumb-item>
        </el-breadcrumb>

        <div class="flex items-center gap-3">
          <span class="text-sm text-slate-500">管理员</span>
          <el-button type="primary" link @click="handleLogout">退出</el-button>
          <ChangeColor />
        </div>
      </el-header>

      <el-main class="p-6!">
        <RouterView v-slot="{ Component }">
          <Transition mode="out-in" name="fade">
            <component :is="Component" />
          </Transition>
        </RouterView>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useMenuStore } from '@/stores/menuStore.ts'
import { useUserStore } from '@/stores/userStore.ts'
import AdminMenuItem from './components/AdminMenuItem.vue'
import ChangeColor from './components/ChangeColor.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()

const isMenuActive = (path: string) => {
  return route.path === path || route.path.startsWith(`${path}/`)
}

const defaultActiveMenu = computed(() => {
  // 递归查找菜单，支持多级子菜单，找到匹配的主菜单path
  function findMatch(items: typeof menuStore.adminMenu): string {
    for (const item of items) {
      if (item.path && isMenuActive(item.path)) {
        return item.path
      }
      // 如果有子菜单，递归
      if (item.children?.length) {
        const childResult = findMatch(item.children)
        if (childResult) return childResult
      }
    }
    return ''
  }
  return findMatch(menuStore.adminMenu)
})

const breadcrumbs = computed(() =>
  route.matched.map((r) => r.meta?.title).filter((t): t is string => !!t),
)

// 兜底：如果 store 里没菜单（比如刷新页面），主动拉一次
onMounted(() => {
  if (!menuStore.adminMenu.length) {
    void menuStore.loadAdminMenu()
  }
})

function handleLogout() {
  userStore.logout()
  router.replace({ name: 'Login' })
}
</script>

<style lang="scss" scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 侧栏背景跟随 --menu-bg */
.admin-aside {
  background: var(--menu-bg);
  border-right: 1px solid var(--menu-border);
}

/* 菜单：全部由 CSS 变量驱动，切换主题时自动变色 */
.admin-menu {
  --el-menu-bg-color: var(--menu-bg);
  --el-menu-text-color: var(--menu-text);
  --el-menu-active-color: var(--menu-text-active);
  --el-menu-hover-bg-color: var(--menu-hover-bg);
  --el-menu-item-height: 44px;
  --el-menu-sub-item-height: 40px;
  --el-menu-border-color: var(--menu-border);
  --el-menu-base-level-padding: 16px;

  border-right: none;

  :deep(.el-menu-item.is-active) {
    background: var(--menu-active-bg);
    color: var(--menu-text-active);
    font-weight: 500;
  }

  :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
    color: var(--menu-text-active);
  }
}
</style>
