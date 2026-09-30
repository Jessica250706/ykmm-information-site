<template>
  <section>
    <div class="mb-5 flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-xl font-semibold">卡面</h1>
      <input
        v-model="keyword"
        class="w-56 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm outline-none transition focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100"
        placeholder="搜索卡面 / 系列"
        type="search"
      />
    </div>

    <div class="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
      <article
        v-for="item in filtered"
        :key="item.id"
        class="group overflow-hidden rounded-xl bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
      >
        <div class="aspect-3/4 bg-linear-to-br from-slate-200 to-slate-300" />
        <div class="p-3">
          <p class="truncate font-medium">{{ item.name }}</p>
          <p class="mt-0.5 truncate text-xs text-slate-500">{{ item.series }}</p>
        </div>
      </article>
    </div>

    <p v-if="!filtered.length" class="py-16 text-center text-sm text-slate-400">没有找到相关卡面</p>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

interface CardItem {
  id: number
  name: string
  series: string
}

const keyword = ref('')

const list = ref<CardItem[]>([
  { id: 1, name: '星夜之约', series: '初雪系列' },
  { id: 2, name: '晨光序曲', series: '初雪系列' },
  { id: 3, name: '海风信笺', series: '夏日系列' },
  { id: 4, name: '月下独舞', series: '夜曲系列' },
  { id: 5, name: '雨后天晴', series: '夏日系列' },
  { id: 6, name: '冬眠日记', series: '初雪系列' },
])

const filtered = computed(() =>
  list.value.filter((i) => i.name.includes(keyword.value) || i.series.includes(keyword.value)),
)
</script>

<style lang="scss" scoped></style>
