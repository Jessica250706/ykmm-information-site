<template>
  <section class="space-y-5">
    <h1 class="text-xl font-semibold">个人中心</h1>

    <div class="flex items-center gap-4 rounded-2xl bg-white p-5 shadow-sm">
      <span class="grid h-14 w-14 place-items-center rounded-full bg-slate-200 text-lg">我</span>
      <div class="flex-1">
        <p class="font-medium">测试用户</p>
        <p class="mt-0.5 text-sm text-slate-500">当前身份：{{ isAdmin ? '管理员' : '普通用户' }}</p>
      </div>
      <RouterLink
        v-if="isAdmin"
        class="rounded-lg border border-slate-200 px-3 py-1.5 text-sm text-slate-600 transition hover:border-indigo-300 hover:text-indigo-600"
        to="/admin"
      >
        进入管理端
      </RouterLink>
    </div>

    <button
      class="rounded-lg bg-slate-900 px-4 py-2 text-sm text-white transition hover:bg-slate-800"
      type="button"
      @click="handleLogout"
    >
      退出登录
    </button>
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { isAdmin, logout } = useAuth()

function handleLogout() {
  logout()
  router.replace({ name: 'Login' })
}
</script>

<style lang="scss" scoped></style>
