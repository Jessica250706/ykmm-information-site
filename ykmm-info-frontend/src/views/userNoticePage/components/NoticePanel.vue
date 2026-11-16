<template>
  <component
    :is="noCard ? 'div' : 'el-card'"
    v-bind="noCard ? {} : { 'body-class': bodyClass, shadow: 'never' }"
    v-loading="loading"
    :class="noCard ? [containerClass, bodyClass] : containerClass"
  >
    <slot />
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type PanelVariant = 'left' | 'detail'

const props = withDefaults(
  defineProps<{
    /** 面板位置 */
    variant: PanelVariant
    /**
     * 是否跳过 el-card 包裹（手机端 / 抽屉场景用）。
     * true 时用普通 div 渲染，container + body 样式合并应用。
     */
    noCard?: boolean
    /** loading 状态（移动端抽屉场景也生效） */
    loading?: boolean
  }>(),
  { noCard: false, loading: false },
)

/** 容器样式 */
const containerClass = computed(() => {
  if (props.noCard) {
    switch (props.variant) {
      case 'left':
        return 'flex h-full w-full flex-col overflow-hidden bg-white'
      case 'detail':
        return 'flex min-w-0 flex-1 flex-col overflow-hidden'
    }
  }
  switch (props.variant) {
    case 'left':
      return 'flex w-80 flex-col overflow-hidden border-r bg-white'
    case 'detail':
      return 'h-full'
  }
})

/** el-card body 样式 */
const bodyClass = computed(() => {
  switch (props.variant) {
    case 'left':
      return 'flex flex-col h-full overflow-hidden p-0'
    case 'detail':
      return 'flex flex-col h-full overflow-hidden'
  }
})
</script>
