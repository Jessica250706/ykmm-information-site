<template>
  <div class="flex items-center justify-between">
    <span class="font-medium">剧情分类详情</span>
    <div>
      <el-button :disabled="!detail" type="primary" @click="emit('editCurrent')">
        <el-icon><Edit /></el-icon>
        编辑
      </el-button>
      <el-button v-if="isLeaf" type="primary" @click="emit('createStory')">
        <el-icon><Plus /></el-icon>
        新增剧情
      </el-button>
      <el-button type="primary" @click="emit('createCategory')">
        <el-icon><Plus /></el-icon>
        新增子分类
      </el-button>
      <el-button @click="emit('back')">返回</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Edit, Plus } from '@element-plus/icons-vue'
import type { StoryCategoryVO } from '@/types/storyCategory'

defineProps<{
  detail: StoryCategoryVO | null
  /** 是否为叶子分类：决定"新增剧情"按钮是否可见 */
  isLeaf: boolean
}>()

const emit = defineEmits<{
  editCurrent: []
  createStory: []
  createCategory: []
  back: []
}>()
</script>
