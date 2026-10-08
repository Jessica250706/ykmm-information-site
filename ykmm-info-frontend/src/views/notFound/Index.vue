<template>
  <div class="grid min-h-screen place-items-center bg-slate-100 text-center">
    <div>
      <p class="text-5xl font-bold text-slate-300">404</p>
      <p class="mt-3 text-slate-500">页面不存在</p>
      <button
        class="mt-5 rounded-lg bg-indigo-600 px-4 py-2 text-sm text-white"
        @click="router.replace({ name: UserRouteName.CARD_LIST })"
      >
        返回首页
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { UserRouteName } from '@/constants'

const router = useRouter()
</script>

<style lang="scss" scoped></style>
