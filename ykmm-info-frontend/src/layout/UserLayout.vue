<template>
  <div class="min-h-screen">
    <el-container class="min-h-screen">
      <el-header
        class="sticky top-0 z-30 border-b border-slate-200 bg-white/85 backdrop-blur"
        height="64px"
      >
        <div class="mx-auto flex h-full max-w-6xl items-center px-4">
          <!-- Logo -->
          <RouterLink class="flex shrink-0 items-center gap-2" to="/cards">
            <el-avatar
              :size="32"
              :style="{ backgroundColor: 'var(--el-color-primary)' }"
              class="text-sm font-bold"
            >
              U
            </el-avatar>
            <span class="font-semibold">用户端</span>
          </RouterLink>

          <!-- 左侧目录：卡面 / 剧情 -->
          <el-menu
            :default-active="activeMenu"
            :ellipsis="false"
            class="ml-8 user-nav-menu"
            mode="horizontal"
            router
          >
            <el-menu-item v-for="nav in menuStore.userNavFlat" :key="nav.path" :index="nav.path">
              {{ nav.name }}
            </el-menu-item>
          </el-menu>

          <!-- 最右侧：管理端入口（仅管理员可见） + 个人中心 + 主题切换 -->
          <div class="ml-auto flex items-center gap-3">
            <el-button
              v-if="isAdmin"
              class="hidden sm:inline-flex"
              size="small"
              plain
              @click="router.push('/admin')"
            >
              前往管理端 →
            </el-button>

            <RouterLink
              :class="isActive('/profile') ? 'theme-pill-active' : 'hover:bg-slate-100'"
              class="flex items-center gap-2 rounded-full py-1 pl-1 pr-3 transition"
              to="/profile"
            >
              <el-avatar :size="28" class="bg-slate-200! text-xs!">我</el-avatar>
              <span class="text-sm">个人中心</span>
            </RouterLink>

            <ChangeColor />
          </div>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="p-0!">
        <div class="userLayout mx-auto w-full max-w-6xl px-4 py-6 overflow-hidden">
          <RouterView v-slot="{ Component }">
            <Transition mode="out-in" name="fade">
              <component :is="Component" />
            </Transition>
          </RouterView>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useMenuStore } from '@/stores/menuStore.ts'
import { useUserStore } from '@/stores/userStore.ts'
import ChangeColor from './components/ChangeColor.vue'

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const { isAdmin } = useUserStore()

const isActive = (path: string) => route.path === path || route.path.startsWith(`${path}/`)

/** 把 isActive 逻辑适配给 el-menu 的 default-active */
const activeMenu = computed(() => {
  const matchedNav = menuStore.userNavFlat.find((nav) => {
    const p = nav.path
    return route.path === p || route.path.startsWith(`${p}/`)
  })
  return matchedNav?.path ?? ''
})

onMounted(() => {
  if (!menuStore.userMenu.length) {
    void menuStore.loadUserMenu()
  }
})
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

/* 个人中心激活态 */
.theme-pill-active {
  background: var(--menu-hover-bg);
  color: var(--menu-text-active);
}

.user-nav-menu {
  border-bottom: none;
  background: transparent;
  display: flex;
  justify-content: center;
  align-items: center;

  :deep(.el-menu-item) {
    height: 40px;
    line-height: 40px;
    margin-left: 24px;
    padding: 0 12px;
    border-bottom: none;
    border-radius: 0.5rem;
    font-size: 14px;
    color: var(--menu-text);

    &:hover {
      background: var(--menu-hover-bg);
      color: var(--menu-text);
    }

    &.is-active {
      background: var(--menu-active-bg);
      color: var(--menu-text-active);
      font-weight: 500;
    }
  }
}

.userLayout {
  height: calc(100dvh - 64px);
}
</style>
