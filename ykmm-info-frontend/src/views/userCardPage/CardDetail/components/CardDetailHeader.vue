<template>
  <div class="mb-5 flex shrink-0 items-center justify-between">
    <div class="flex items-center gap-3">
      <el-button @click="emit('back')">← 返回</el-button>
      <h1 class="text-xl font-semibold">{{ card?.name || '卡面详情' }}</h1>
      <el-tag v-if="card?.maxRarity" :type="getCardRarityTagType(card.maxRarity)" effect="plain">
        {{ card.maxRarityLabel ?? cardMaxRarityLabel(card.maxRarity) }}
      </el-tag>
    </div>

    <div class="flex items-center gap-2">
      <el-button
        v-if="attachedType === SOURCE_TYPE.RC"
        type="primary"
        @click="emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC])"
      >
        <el-icon><Plus /></el-icon>
        新增 RC
      </el-button>

      <el-button
        v-if="attachedType === SOURCE_TYPE.RTV"
        type="primary"
        @click="emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RTV])"
      >
        <el-icon><Plus /></el-icon>
        新增 RTV
      </el-button>

      <el-button
        v-if="attachedType === SOURCE_TYPE.RABITTER"
        type="primary"
        @click="emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RABITTER])"
      >
        <el-icon><Plus /></el-icon>
        新增 Rabitter
      </el-button>

      <el-button v-if="card?.attachedStoryTypeLabel && isList" type="primary" @click="emit('goto')">
        查看{{ card.attachedStoryTypeLabel }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import {
  cardMaxRarityLabel,
  SOURCE_TYPE,
  SOURCE_TYPE_SMALL_LABEL,
  type SourceTypeSmallLabelValue,
} from '@/constants'
import type { CardVO } from '@/types/card'
import { getCardRarityTagType } from '@/utils'

defineProps<{
  card: CardVO | null
  attachedType: number
  isList: boolean
}>()

const emit = defineEmits<{
  back: []
  goto: []
  addEpisode: [mode: SourceTypeSmallLabelValue]
}>()
</script>
