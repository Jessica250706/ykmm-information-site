<template>
  <div v-if="text">
    <h2 class="mb-3 text-sm font-medium text-slate-500">魅力技能</h2>
    <p class="whitespace-pre-line text-sm leading-relaxed text-slate-700">{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  text?: string
}>()
</script>
