<template>
  <div v-if="items.length">
    <h2 class="mb-3 text-sm font-medium text-slate-500">图片类型</h2>
    <div class="flex flex-wrap gap-2">
      <el-tag v-for="l in items" :key="l.type" effect="plain" size="small" type="info">
        {{ l.label }}
      </el-tag>
    </div>
    <p class="mt-2 text-xs text-slate-400">左侧图片顺序与上方编号一致，点击缩略图切换</p>
  </div>
</template>

<script setup lang="ts">
export interface ImageLegendItem {
  type: number
  label: string
}

defineProps<{
  items: ImageLegendItem[]
}>()
</script>
