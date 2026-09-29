<template>
  <section class="rounded-xl bg-white p-5 shadow-sm">
    <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
      <h2 class="text-base font-semibold">人物管理</h2>
      <div class="flex items-center gap-2">
        <input
          v-model="keyword"
          type="search"
          placeholder="搜索人物"
          class="w-52 rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none transition focus:border-indigo-400 focus:ring-2 focus:ring-indigo-100"
        />
        <button
          type="button"
          class="rounded-lg bg-indigo-600 px-3 py-2 text-sm text-white transition hover:bg-indigo-700"
        >
          新建人物
        </button>
      </div>
    </div>

    <div class="overflow-x-auto">
      <table class="w-full text-left text-sm">
        <thead class="border-b border-slate-200 text-slate-500">
          <tr>
            <th class="px-3 py-2 font-medium">ID</th>
            <th class="px-3 py-2 font-medium">名称</th>
            <th class="px-3 py-2 font-medium">标识</th>
            <th class="px-3 py-2 font-medium">状态</th>
            <th class="px-3 py-2 text-right font-medium">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="row in filtered"
            :key="row.id"
            class="border-b border-slate-100 last:border-0 hover:bg-slate-50"
          >
            <td class="px-3 py-2.5 text-slate-500">{{ row.id }}</td>
            <td class="px-3 py-2.5 font-medium">{{ row.name }}</td>
            <td class="px-3 py-2.5 text-slate-500">{{ row.code }}</td>
            <td class="px-3 py-2.5">
              <span
                class="rounded-full px-2 py-0.5 text-xs"
                :class="
                  row.status === '启用'
                    ? 'bg-emerald-50 text-emerald-600'
                    : 'bg-slate-100 text-slate-500'
                "
              >
                {{ row.status }}
              </span>
            </td>
            <td class="px-3 py-2.5 text-right">
              <button class="text-indigo-600 hover:underline">编辑</button>
              <button class="ml-3 text-rose-500 hover:underline">删除</button>
            </td>
          </tr>
        </tbody>
      </table>

      <p v-if="!filtered.length" class="py-10 text-center text-sm text-slate-400">暂无数据</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

interface Character {
  id: number
  name: string
  code: string
  status: '启用' | '停用'
}

const keyword = ref('')

const rows = ref<Character[]>([
  { id: 1, name: '星野爱', code: 'HOSHINO_AI', status: '启用' },
  { id: 2, name: '神乐光', code: 'KAGURA_HIKARI', status: '启用' },
  { id: 3, name: '西条克洛迪娜', code: 'SAIJO_CLAUDINE', status: '停用' },
])

const filtered = computed(() =>
  rows.value.filter((r) => r.name.includes(keyword.value) || r.code.includes(keyword.value)),
)
</script>

<style lang="scss" scoped></style>
