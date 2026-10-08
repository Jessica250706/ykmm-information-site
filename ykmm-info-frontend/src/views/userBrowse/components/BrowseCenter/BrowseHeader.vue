<template>
  <div class="mb-4 shrink-0">
    <div class="flex justify-between items-center">
      <h2 class="text-lg font-semibold">{{ title }}</h2>

      <!-- 右侧操作区：默认返回按钮，可通过 slot 覆盖 -->
      <slot name="actions">
        <el-button v-if="showBack" @click="emit('goBack')">← 返回</el-button>
      </slot>
    </div>

    <!-- 标签行 -->
    <div
      v-if="tag || subtitle"
      class="mt-1 flex flex-wrap items-center gap-2 text-xs text-slate-400"
    >
      <el-tag
        v-if="tag"
        :style="{
          borderColor: `var(--color-${color})`,
          color: `var(--color-${color})`,
        }"
        effect="plain"
        size="small"
      >
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
  }>(),
  {
    title: '',
    color: 'blue',
    tag: '',
    subtitle: '',
    description: '',
    showBack: true,
  },
)

const emit = defineEmits<{
  goBack: []
}>()
</script>
