<template>
  <div class="grid min-h-screen place-items-center bg-slate-100">
    <div class="w-80 rounded-2xl bg-white p-6 shadow-sm">
      <h1 class="mb-1 text-lg font-semibold">登录</h1>
      <p class="mb-6 text-sm text-slate-500">选择一个身份进入系统</p>

      <div class="space-y-3">
        <button
          type="button"
          class="w-full rounded-lg bg-indigo-600 py-2.5 text-sm font-medium text-white transition hover:bg-indigo-700"
          @click="loginAs('admin')"
        >
          以管理员身份进入
        </button>
        <button
          type="button"
          class="w-full rounded-lg border border-slate-200 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50"
          @click="loginAs('user')"
        >
          以普通用户身份进入
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const { login } = useAuth()

function loginAs(role: 'admin' | 'user') {
  login('mock-token', role === 'admin' ? ['admin', 'user'] : ['user'])

  const redirect = route.query.redirect as string | undefined
  if (redirect) {
    router.replace(redirect)
    return
  }
  router.replace(role === 'admin' ? '/admin' : '/cards')
}
</script>

<style lang="scss" scoped></style>
