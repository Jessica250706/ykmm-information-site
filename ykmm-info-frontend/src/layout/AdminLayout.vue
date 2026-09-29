<template>
  <div class="flex h-screen bg-slate-100 text-slate-800">
    <!-- 侧边栏 -->
    <aside class="flex w-60 shrink-0 flex-col bg-slate-900">
      <div class="flex h-16 items-center gap-2 border-b border-slate-800 px-5">
        <span
          class="grid h-8 w-8 place-items-center rounded-lg bg-indigo-500 text-sm font-bold text-white"
        >
          A
        </span>
        <span class="font-semibold text-white">管理后台</span>
      </div>

      <nav class="flex-1 space-y-1 overflow-y-auto p-3">
        <AdminMenuItem v-for="item in adminMenu" :key="item.title" :item="item" />
      </nav>

      <div class="border-t border-slate-800 p-3">
        <RouterLink
          to="/cards"
          class="flex items-center justify-center gap-1 rounded-lg bg-slate-800 px-3 py-2 text-sm text-slate-300 transition hover:bg-slate-700 hover:text-white"
        >
          前往用户端 →
        </RouterLink>
      </div>
    </aside>

    <!-- 主区域 -->
    <div class="flex min-w-0 flex-1 flex-col">
      <header
        class="flex h-16 shrink-0 items-center justify-between border-b border-slate-200 bg-white px-6"
      >
        <nav class="flex items-center gap-2 text-sm text-slate-500">
          <template v-for="(item, i) in breadcrumbs" :key="item">
            <span v-if="i > 0" class="text-slate-300">/</span>
            <span :class="i === breadcrumbs.length - 1 ? 'font-medium text-slate-900' : ''">
              {{ item }}
            </span>
          </template>
        </nav>

        <div class="flex items-center gap-3">
          <span class="text-sm text-slate-500">管理员</span>
          <button
            type="button"
            class="text-sm text-slate-500 transition hover:text-indigo-600"
            @click="handleLogout"
          >
            退出
          </button>
        </div>
      </header>

      <main class="flex-1 overflow-y-auto p-6">
        <RouterView v-slot="{ Component }">
          <Transition name="fade" mode="out-in">
            <component :is="Component" />
          </Transition>
        </RouterView>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import AdminMenuItem from './components/AdminMenuItem.vue'
import { adminMenu } from '@/config/adminMenu'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const { logout } = useAuth()

const breadcrumbs = computed(() =>
  route.matched.map((r) => r.meta?.title).filter((t): t is string => !!t),
)

function handleLogout() {
  logout()
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
</style>
