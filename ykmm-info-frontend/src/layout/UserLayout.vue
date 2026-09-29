<template>
  <div class="min-h-screen bg-slate-50 text-slate-800">
    <!-- 顶部导航 -->
    <header class="sticky top-0 z-30 border-b border-slate-200 bg-white/85 backdrop-blur">
      <div class="mx-auto flex h-16 max-w-6xl items-center px-4">
        <!-- Logo -->
        <RouterLink to="/cards" class="flex shrink-0 items-center gap-2">
          <span
            class="grid h-8 w-8 place-items-center rounded-lg bg-indigo-500 text-sm font-bold text-white"
          >
            U
          </span>
          <span class="font-semibold">用户端</span>
        </RouterLink>

        <!-- 左侧目录：卡面 / 剧情 -->
        <nav class="ml-8 flex items-center gap-1">
          <RouterLink
            v-for="nav in userNav"
            :key="nav.to"
            :to="nav.to"
            class="rounded-lg px-3 py-2 text-sm transition"
            :class="
              isActive(nav.to)
                ? 'bg-indigo-50 font-medium text-indigo-600'
                : 'text-slate-600 hover:bg-slate-100'
            "
          >
            {{ nav.label }}
          </RouterLink>
        </nav>

        <!-- 最右侧：管理端入口（仅管理员可见） + 个人中心 -->
        <div class="ml-auto flex items-center gap-3">
          <RouterLink
            v-if="isAdmin"
            to="/admin"
            class="hidden rounded-lg border border-slate-200 px-3 py-1.5 text-sm text-slate-600 transition hover:border-indigo-300 hover:text-indigo-600 sm:block"
          >
            管理端
          </RouterLink>

          <RouterLink
            to="/profile"
            class="flex items-center gap-2 rounded-full py-1 pl-1 pr-3 transition"
            :class="isActive('/profile') ? 'bg-indigo-50 text-indigo-600' : 'hover:bg-slate-100'"
          >
            <span class="grid h-7 w-7 place-items-center rounded-full bg-slate-200 text-xs">
              我
            </span>
            <span class="text-sm">个人中心</span>
          </RouterLink>
        </div>
      </div>
    </header>

    <!-- 内容区 -->
    <main class="mx-auto max-w-6xl px-4 py-6">
      <RouterView v-slot="{ Component }">
        <Transition name="fade" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { userNav } from '@/config/userNav'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const { isAdmin } = useAuth()

const isActive = (path: string) => route.path === path || route.path.startsWith(`${path}/`)
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
</style>
