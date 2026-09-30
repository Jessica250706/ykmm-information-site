<template>
  <section>
    <h1 class="mb-5 text-xl font-semibold">剧情</h1>

    <ul class="space-y-3">
      <li
        v-for="c in chapters"
        :key="c.id"
        class="flex items-center gap-4 rounded-xl bg-white p-4 shadow-sm transition"
        :class="c.locked ? 'opacity-60' : 'hover:shadow-md'"
      >
        <span
          class="grid h-10 w-10 shrink-0 place-items-center rounded-lg bg-indigo-50 text-indigo-600"
        >
          {{ c.id }}
        </span>
        <div class="min-w-0 flex-1">
          <p class="font-medium">{{ c.title }}</p>
          <p class="mt-0.5 truncate text-sm text-slate-500">{{ c.desc }}</p>
        </div>
        <span v-if="c.locked" class="text-xs text-slate-400">未解锁</span>
        <button
          v-else
          class="rounded-lg bg-indigo-600 px-3 py-1.5 text-sm text-white transition hover:bg-indigo-700"
          type="button"
        >
          阅读
        </button>
      </li>
    </ul>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const chapters = ref([
  { id: 1, title: '第一章 · 相遇', desc: '命运的齿轮开始转动。', locked: false },
  { id: 2, title: '第二章 · 试炼', desc: '舞台之下的汗水与泪水。', locked: false },
  { id: 3, title: '第三章 · 裂痕', desc: '还没有解锁。', locked: true },
])
</script>

<style lang="scss" scoped></style>
