<template>
  <BrowsePanel :no-card="noCard" variant="left">
    <!-- 标题 -->
    <div class="flex shrink-0 items-center justify-between border-b py-3">
      <div class="min-w-0">
        <div class="text-xs text-slate-400">{{ sourceLabel }}</div>
        <div class="truncate font-medium">
          {{ cardInfo.name || '-' }}[{{ cardInfo.seriesName }}]
        </div>
      </div>
    </div>

    <!-- 话数列表 -->
    <div class="flex-1 min-h-0 overflow-y-auto py-2 app-scrollbar">
      <ul>
        <li
          v-for="ep in episodes"
          :key="ep.id"
          :class="[
            'cursor-pointer rounded-lg px-3 py-2 text-sm transition',
            ep.id === currentId
              ? 'bg-(--el-color-primary-light-9) font-medium text-(--el-color-primary)'
              : 'hover:bg-(--el-fill-color-lighter)',
          ]"
          @click="ep.id != null && emit('select', ep.id)"
        >
          <div class="truncate">第 {{ ep.episodeNo }} 话</div>
          <div v-if="ep.title" class="mt-0.5 truncate text-xs text-slate-400">
            {{ ep.title }}
          </div>
        </li>
      </ul>

      <p v-if="!episodes.length" class="py-8 text-center text-xs text-slate-400">暂无话数</p>
    </div>
  </BrowsePanel>
</template>

<script setup lang="ts">
import type { CardVO } from '@/types/card'
import BrowsePanel from '../BrowsePanel.vue'

interface EpisodeItem {
  id?: number
  episodeNo?: number
  title?: string
}

defineProps<{
  /** 来源标签，如"卡面RC" */
  sourceLabel: string
  /** 卡面名 */
  cardInfo: CardVO
  /** 话数列表 */
  episodes: EpisodeItem[]
  /** 当前选中话数 id */
  currentId: number | null
  /** 是否跳过 el-card（手机端） */
  noCard?: boolean
}>()

const emit = defineEmits<{
  select: [id: number]
  back: []
}>()
</script>
