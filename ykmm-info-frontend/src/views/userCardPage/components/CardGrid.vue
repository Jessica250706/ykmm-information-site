<template>
  <div
    ref="scrollContainerRef"
    v-infinite-scroll="handleLoad"
    v-loading="loading && !list.length"
    :infinite-scroll-disabled="disabled || loading"
    :infinite-scroll-distance="preloadDistance"
    class="min-h-40 flex-1 overflow-y-auto"
  >
    <div v-if="list.length" :class="['grid gap-4', gridClass]">
      <CardItem v-for="item in list" :key="item.id" :card="item" @click="emit('itemClick', item)" />
    </div>

    <p v-else-if="!loading" class="py-16 text-center text-sm text-slate-400">没有找到相关卡面</p>

    <div v-if="loading && list.length" class="py-6 text-center text-sm text-slate-400">
      加载中...
    </div>
    <div v-else-if="disabled && list.length" class="py-6 text-center text-sm text-slate-400">
      没有更多了
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import type { CardVO } from '@/types/card'
import CardItem from './CardItem.vue'

defineProps<{
  list: CardVO[]
  loading: boolean
  disabled: boolean
  gridClass: string
}>()

const emit = defineEmits<{
  (e: 'load'): void
  (e: 'itemClick', card: CardVO): void
}>()

const scrollContainerRef = ref<HTMLElement | null>(null)
const preloadDistance = ref(800)

function updatePreloadDistance() {
  const el = scrollContainerRef.value
  if (!el) return
  preloadDistance.value = el.clientHeight
}

function handleLoad() {
  emit('load')
}

onMounted(async () => {
  await nextTick()
  updatePreloadDistance()
  window.addEventListener('resize', updatePreloadDistance)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updatePreloadDistance)
})
</script>
