<template>
  <div class="mb-4 shrink-0">
    <div class="flex justify-between items-center">
      <h2 class="text-lg font-semibold">{{ title }}</h2>

      <!-- 右侧操作区：默认返回按钮，可通过 slot 覆盖 -->
      <slot name="actions">
        <div class="flex items-center gap-1">
          <el-button v-if="showReview" type="warning" plain @click="emit('review')">
            <el-icon class="mr-1"><Checked /></el-icon>
            审核
          </el-button>
          <el-button v-if="showBack" @click="emit('goBack')">← 返回</el-button>
        </div>
      </slot>
    </div>

    <!-- 标签行 -->
    <div
      v-if="tag || subtitle"
      class="mt-1 flex flex-wrap items-center gap-2 text-xs text-slate-400"
    >
      <el-tag v-if="tag" :style="categoryTagStyle(color)" effect="plain" size="small">
        {{ tag }}
      </el-tag>
      <span v-if="subtitle">{{ subtitle }}</span>
    </div>

    <!-- 简介 -->
    <p v-if="description" class="mt-3 whitespace-pre-line text-sm text-slate-600">
      {{ description }}
    </p>

    <!-- 额外内容插槽 -->
    <slot />
  </div>
</template>

<script setup lang="ts">
import { categoryTagStyle } from '@/utils'

withDefaults(
  defineProps<{
    /** 标题 */
    title?: string
    /** 主题色，用于标签边框与文字颜色 */
    color?: string
    /** 标签文本，如"主线"、"戏剧篇" */
    tag?: string
    /** 副标题，如分类名 */
    subtitle?: string
    /** 简介，可选 */
    description?: string
    /** 是否显示返回按钮 */
    showBack?: boolean
    /** 是否显示审核按钮（仅管理员 + 待审核版本时传 true） */
    showReview?: boolean
  }>(),
  {
    title: '',
    color: 'blue',
    tag: '',
    subtitle: '',
    description: '',
    showBack: true,
    showReview: false,
  },
)

const emit = defineEmits<{
  goBack: []
  review: []
}>()
</script>
