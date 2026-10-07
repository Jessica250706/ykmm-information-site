<template>
  <div class="mb-5 flex shrink-0 items-center justify-between">
    <div class="flex items-center gap-3">
      <el-button @click="emit('back')">← 返回</el-button>
      <h1 class="text-xl font-semibold">{{ card?.name || '卡面详情' }}</h1>
      <el-tag
        v-if="card?.maxRarity"
        :type="card.maxRarity === CARD_MAX_RARITY.UR ? 'danger' : 'warning'"
        effect="plain"
      >
        {{ card.maxRarityLabel ?? cardMaxRarityLabel(card.maxRarity) }}
      </el-tag>
    </div>

    <div class="flex items-center gap-2">
      <el-button
        v-if="attachedType === CARD_ATTACHED_STORY_TYPE.RC"
        type="primary"
        @click="emit('addEpisode', 'rc')"
      >
        <el-icon><Plus /></el-icon>
        新增 RC
      </el-button>

      <el-button
        v-if="attachedType === CARD_ATTACHED_STORY_TYPE.RTV"
        type="primary"
        @click="emit('addEpisode', 'rtv')"
      >
        <el-icon><Plus /></el-icon>
        新增 RTV
      </el-button>

      <el-button v-if="card?.attachedStoryTypeLabel" type="primary" @click="emit('goto')">
        查看{{ card.attachedStoryTypeLabel }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import { CARD_ATTACHED_STORY_TYPE, CARD_MAX_RARITY, cardMaxRarityLabel } from '@/constants/card'
import type { CardVO } from '@/types/card'

defineProps<{
  card: CardVO | null
  attachedType: number
}>()

const emit = defineEmits<{
  back: []
  goto: []
  addEpisode: [mode: 'rc' | 'rtv']
}>()
</script>
