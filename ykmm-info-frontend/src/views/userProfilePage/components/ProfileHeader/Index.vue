<template>
  <div class="flex flex-wrap items-center gap-3 rounded-2xl bg-white p-4 shadow-sm md:gap-4 md:p-5">
    <el-avatar
      :src="userInfo?.avatar"
      class="bg-slate-200 text-lg h-12! w-12! md:text-xl md:h-16! md:w-16!"
    >
      {{ avatarText }}
    </el-avatar>

    <div class="min-w-0 flex-1">
      <div class="flex flex-wrap items-center gap-2">
        <span class="truncate text-base font-semibold md:text-lg">{{ displayName }}</span>
        <el-tag :type="isAdmin ? 'danger' : 'primary'" effect="plain" size="small">
          {{ isAdmin ? '管理员' : '普通用户' }}
        </el-tag>
      </div>
      <div class="mt-1 truncate text-sm text-slate-500">
        <span v-if="userInfo?.email">{{ userInfo.email }}</span>
      </div>
      <div v-if="userInfo?.createdAt" class="mt-0.5 text-xs text-slate-400">
        加入于 {{ userInfo.createdAt }}
      </div>
    </div>

    <!-- ==================== PC 端 ==================== -->
    <div class="hidden shrink-0 md:block">
      <UserActionsDropdown :is-admin="isAdmin" @command="handleActionCommand">
        <el-button plain>
          <el-icon class="mr-1"><Menu /></el-icon>
          操作
        </el-button>
      </UserActionsDropdown>
    </div>

    <!-- ==================== 移动端 ==================== -->
    <div class="shrink-0 md:hidden">
      <UserActionsDropdown :is-admin="isAdmin" @command="handleActionCommand">
        <el-button size="small" title="操作" circle plain>
          <el-icon><MoreFilled /></el-icon>
        </el-button>
      </UserActionsDropdown>
    </div>

    <EditProfileDialog ref="editDialogRef" />
    <RebindEmailDialog ref="rebindDialogRef" />
    <ChangePasswordDialog ref="changePasswordDialogRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { MoreFilled } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import ChangePasswordDialog from './components/ChangePasswordDialog.vue'
import EditProfileDialog from './components/EditProfileDialog.vue'
import RebindEmailDialog from './components/RebindEmailDialog.vue'
import UserActionsDropdown from './components/UserActionsDropdown.vue'

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

/** PC + 移动端共用的下拉命令分发 */
function handleActionCommand(cmd: string) {
  switch (cmd) {
    case 'edit':
      handleEdit()
      break
    case 'rebind':
      handleRebind()
      break
    case 'password':
      handleChangePassword()
      break
    case 'admin':
      void router.push('/admin')
      break
  }
}
</script>
