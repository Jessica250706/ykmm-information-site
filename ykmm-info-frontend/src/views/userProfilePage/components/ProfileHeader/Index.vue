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
        <span v-if="userInfo?.email">{{ userInfo.email }}</span>
      </div>
      <div v-if="userInfo?.createdAt" class="mt-0.5 text-xs text-slate-400">
        加入于 {{ userInfo.createdAt }}
      </div>
    </div>

    <div class="flex items-center gap-2">
      <el-button plain @click="handleEdit">
        <el-icon class="mr-1"><Edit /></el-icon>
        编辑资料
      </el-button>

      <el-button plain @click="handleRebind">
        <el-icon class="mr-1"><Message /></el-icon>
        换绑邮箱
      </el-button>

      <el-button plain @click="handleChangePassword">
        <el-icon class="mr-1"><Lock /></el-icon>
        修改密码
      </el-button>

      <el-button v-if="isAdmin" plain @click="router.push('/admin')">进入管理端</el-button>
    </div>

    <EditProfileDialog ref="editDialogRef" />
    <RebindEmailDialog ref="rebindDialogRef" />
    <ChangePasswordDialog ref="changePasswordDialogRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Edit, Lock, Message } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import ChangePasswordDialog from './components/ChangePasswordDialog.vue'
import EditProfileDialog from './components/EditProfileDialog.vue'
import RebindEmailDialog from './components/RebindEmailDialog.vue'

const router = useRouter()
const userStore = useUserStore()

const { isAdmin, userInfo } = storeToRefs(userStore)

const displayName = computed(() => userInfo.value?.nickname || userInfo.value?.email || '个人中心')

const avatarText = computed(() => {
  const name = userInfo.value?.nickname || userInfo.value?.email || ''
  return name ? name.charAt(0).toUpperCase() : '我'
})

const editDialogRef = ref<InstanceType<typeof EditProfileDialog> | null>(null)
const rebindDialogRef = ref<InstanceType<typeof RebindEmailDialog> | null>(null)
const changePasswordDialogRef = ref<InstanceType<typeof ChangePasswordDialog> | null>(null)

function handleEdit() {
  editDialogRef.value?.open()
}

function handleRebind() {
  rebindDialogRef.value?.open()
}

function handleChangePassword() {
  changePasswordDialogRef.value?.open()
}
</script>
