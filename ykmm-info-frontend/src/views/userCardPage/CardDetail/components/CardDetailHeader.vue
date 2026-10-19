<template>
  <div class="mb-5 flex shrink-0 items-center justify-between">
    <div class="flex items-center gap-3">
      <el-button @click="emit('back')">← 返回</el-button>
      <h1 class="text-xl font-semibold">{{ card?.name || '卡面详情' }}</h1>
      <el-tag v-if="card?.maxRarity" :type="getCardRarityTagType(card.maxRarity)" effect="plain">
        {{ card.maxRarityLabel ?? cardMaxRarityLabel(card.maxRarity) }}
      </el-tag>
      <!-- 状态角标 -->
      <el-tag
        v-if="card?.status === STATUS.PENDING || card?.status === STATUS.REJECTED"
        :type="STATUS_TAG_TYPE[card?.status]"
        effect="dark"
      >
        {{ STATUS_LABEL[card?.status] }}
      </el-tag>
    </div>

    <div class="flex items-center gap-2">
      <div v-if="canOperate" class="flex gap-1">
        <el-button
          v-if="isAdmin && card?.status === STATUS.PENDING"
          type="warning"
          plain
          @click="emit('review')"
        >
          <el-icon class="mr-1"><Checked /></el-icon>
          审核
        </el-button>

        <el-button type="primary" plain @click="emit('edit')">
          <el-icon class="mr-1"><Edit /></el-icon>
          编辑卡面
        </el-button>

        <el-button
          v-if="attachedType === SOURCE_TYPE.RC"
          type="primary"
          @click="emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC])"
        >
          <el-icon class="mr-1"><Plus /></el-icon>
          新增 RC
        </el-button>

        <el-button
          v-if="attachedType === SOURCE_TYPE.RTV"
          type="primary"
          @click="emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RTV])"
        >
          <el-icon class="mr-1"><Plus /></el-icon>
          新增 RTV
        </el-button>

        <el-button
          v-if="attachedType === SOURCE_TYPE.RABITTER"
          type="primary"
          @click="emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RABITTER])"
        >
          <el-icon class="mr-1"><Plus /></el-icon>
          新增 Rabitter
        </el-button>
      </div>

      <el-button v-if="card?.attachedStoryTypeLabel && isList" type="primary" @click="emit('goto')">
        查看{{ card.attachedStoryTypeLabel }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Checked, Edit, Plus } from '@element-plus/icons-vue'
import {
  cardMaxRarityLabel,
  SOURCE_TYPE,
  SOURCE_TYPE_SMALL_LABEL,
  type SourceTypeSmallLabelValue,
  STATUS,
  STATUS_LABEL,
  STATUS_TAG_TYPE,
} from '@/constants'
import type { CardVO } from '@/types/card'
import { getCardRarityTagType } from '@/utils'

defineProps<{
  card: CardVO | null
  attachedType: number
  isList: boolean
  /** 当前用户能否编辑此卡面（管理员或创建者） */
  canOperate: boolean
  /** 当前用户是否为管理员 */
  isAdmin: boolean
}>()

const emit = defineEmits<{
  back: []
  goto: []
  addEpisode: [mode: SourceTypeSmallLabelValue]
  edit: []
  review: []
}>()
</script>
