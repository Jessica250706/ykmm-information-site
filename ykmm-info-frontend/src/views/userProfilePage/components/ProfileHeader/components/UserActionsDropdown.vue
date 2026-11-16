<template>
  <el-dropdown
    placement="bottom-end"
    trigger="click"
    @command="(cmd: string) => emit('command', cmd)"
  >
    <!-- 触发器由父组件传入 -->
    <slot />

    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="edit">
          <el-icon><Edit /></el-icon>
          编辑资料
        </el-dropdown-item>
        <el-dropdown-item command="rebind">
          <el-icon><Message /></el-icon>
          换绑邮箱
        </el-dropdown-item>
        <el-dropdown-item command="password">
          <el-icon><Lock /></el-icon>
          修改密码
        </el-dropdown-item>
        <el-dropdown-item v-if="isAdmin" command="admin" divided>
          <el-icon><Setting /></el-icon>
          进入管理端
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { Edit, Lock, Message, Setting } from '@element-plus/icons-vue'

defineProps<{
  /** 是否显示「进入管理端」菜单项 */
  isAdmin: boolean
}>()

const emit = defineEmits<{
  command: [cmd: string]
}>()
</script>
