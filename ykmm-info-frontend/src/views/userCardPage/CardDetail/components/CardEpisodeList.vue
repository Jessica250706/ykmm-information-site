<template>
  <div>
    <h2 class="mb-3 text-sm font-medium text-slate-500">{{ mode === 'rc' ? 'RC' : 'RTV' }} 列表</h2>

    <el-empty
      v-if="!list.length"
      :description="`暂无 ${mode === 'rc' ? 'RC' : 'RTV'}`"
      :image-size="60"
    />

    <ul v-else class="space-y-2">
      <li
        v-for="ep in list"
        :key="ep.id"
        class="group flex items-center justify-between gap-2 rounded-lg border border-slate-200 px-3 py-2 transition hover:border-(--el-color-primary) hover:bg-(--el-fill-color-lighter)"
      >
        <div class="min-w-0 flex-1 cursor-pointer" @click="emit('view', ep.id!)">
          <div class="truncate text-sm font-medium">
            第 {{ ep.episodeNo }} 话{{ ep.title ? ` · ${ep.title}` : '' }}
          </div>
          <div v-if="mode === 'rc'" class="mt-0.5 truncate text-xs text-slate-400">
            发起人：{{ (ep as CardRcVO).roleName || '-' }}
          </div>
        </div>

        <div class="flex items-center gap-1 opacity-0 transition group-hover:opacity-100">
          <el-button
            :icon="Edit"
            size="small"
            type="primary"
            circle
            @click.stop="emit('edit', ep)"
          />
          <el-button
            :icon="Delete"
            size="small"
            type="danger"
            circle
            @click.stop="emit('delete', ep)"
          />
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { Delete, Edit } from '@element-plus/icons-vue'
import type { CardRcVO } from '@/types/cardRc'
import type { CardRtvVO } from '@/types/cardRtv'

defineProps<{
  mode: 'rc' | 'rtv'
  list: (CardRcVO | CardRtvVO)[]
}>()

const emit = defineEmits<{
  view: [id: number]
  edit: [ep: CardRcVO | CardRtvVO]
  delete: [ep: CardRcVO | CardRtvVO]
}>()
</script>
