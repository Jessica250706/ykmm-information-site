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

/** 外层容器样式（el-card 的 class） */
const containerClass = computed(() => {
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
