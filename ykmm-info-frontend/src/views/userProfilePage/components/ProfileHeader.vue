<template>
  <div class="flex flex-wrap items-center gap-4 rounded-2xl bg-white p-5 shadow-sm">
    <el-avatar :size="64" :src="userInfo?.avatar" class="bg-slate-200 text-xl">
      {{ avatarText }}
    </el-avatar>

    <div class="min-w-0 flex-1">
      <div class="flex flex-wrap items-center gap-2">
        <span class="text-lg font-semibold">{{ displayName }}</span>
        <el-tag :type="isAdmin ? 'danger' : 'primary'" effect="plain" size="small">
          {{ isAdmin ? '管理员' : '普通用户' }}
        </el-tag>
      </div>
      <div class="mt-1 text-sm text-slate-500">
        <span v-if="userInfo?.email">邮箱：{{ userInfo.email }}</span>
        <!-- <span v-if="userInfo?.uid" class="ml-3">UID: {{ userInfo.uid }}</span> -->
      </div>
      <div v-if="userInfo?.createdAt" class="mt-0.5 text-xs text-slate-400">
        加入于 {{ userInfo.createdAt }}
      </div>
    </div>

    <el-button v-if="isAdmin" plain @click="router.push('/admin')">进入管理端</el-button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'

const router = useRouter()
const { isAdmin, userInfo } = useUserStore()

const displayName = computed(() => userInfo?.nickname || userInfo?.email || '个人中心')
const avatarText = computed(() => {
  const name = userInfo?.value?.nickname || userInfo?.value?.email || ''
  return name ? name.charAt(0).toUpperCase() : '我'
})
</script>
