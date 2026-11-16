<template>
  <component
    :is="noCard ? 'div' : 'el-card'"
    v-bind="noCard ? {} : { 'body-class': bodyClass }"
    :class="noCard ? [containerClass, bodyClass] : containerClass"
  >
    <slot />
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type PanelVariant = 'left' | 'center' | 'right'

const props = withDefaults(
  defineProps<{
    /** 面板位置 */
    variant: PanelVariant
    /**
     * 是否跳过 el-card 包裹（手机端 / 特殊布局用）。
     * true 时用普通 div 渲染，container + body 样式合并应用。
     */
    noCard?: boolean
  }>(),
  { noCard: false },
)

/**
 * 容器样式。
 * - noCard = false（PC）：走 el-card 外层样式（left 固定 256px，right 固定 384px）
 * - noCard = true（手机 / 抽屉）：撑满父容器，去边框、去固定宽
 */
const containerClass = computed(() => {
  if (props.noCard) {
    switch (props.variant) {
      case 'left':
        return 'flex h-full w-full flex-col overflow-hidden bg-white'
      case 'center':
        return 'flex min-w-0 flex-1 flex-col overflow-hidden bg-white p-0!'
      case 'right':
        return 'flex min-h-0 flex-col overflow-hidden bg-white p-0!'
    }
  }
  switch (props.variant) {
    case 'left':
      return 'flex w-64 shrink-0 flex-col overflow-hidden border-r bg-white'
    case 'center':
      return 'min-w-0 flex-1 flex flex-col overflow-hidden bg-slate-50'
    case 'right':
      return 'w-96 flex-1 min-h-0 shrink-0 flex flex-col border-l bg-white'
  }
})

/** 内层 body 样式（el-card 的 body-class） */
const bodyClass = computed(() => {
  switch (props.variant) {
    case 'left':
      return 'flex flex-col h-full overflow-hidden p-0'
    case 'center':
      return 'flex-1 min-h-0 flex flex-col p-5'
    case 'right':
      return 'flex flex-col h-full overflow-hidden p-4'
  }
})
</script>
