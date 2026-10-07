<template>
  <div>
    <h2 class="mb-3 text-sm font-medium text-slate-500">基本信息</h2>
    <dl class="space-y-2 text-sm">
      <div class="flex justify-between gap-3">
        <dt class="shrink-0 text-slate-400">名称</dt>
        <dd class="truncate text-right">{{ card.name || '-' }}[{{ card.seriesName || '-' }}]</dd>
      </div>
      <div class="flex justify-between gap-3">
        <dt class="shrink-0 text-slate-400">系列</dt>
        <dd class="truncate text-right">{{ card.seriesName || '-' }}</dd>
      </div>
      <div class="flex justify-between gap-3">
        <dt class="shrink-0 text-slate-400">属性</dt>
        <dd class="text-right">
          <el-tag
            v-if="card.attribute"
            :style="getAttributeTagStyle(card.attribute)"
            effect="plain"
            size="small"
          >
            {{ card.attributeLabel ?? cardAttributeLabel(card.attribute) }}
          </el-tag>
          <span v-else>-</span>
        </dd>
      </div>
      <div class="flex justify-between gap-3">
        <dt class="shrink-0 text-slate-400">首次入池</dt>
        <dd class="text-right">{{ card.firstPoolTime || '未知' }}</dd>
      </div>
      <div class="flex justify-between gap-3">
        <dt class="shrink-0 text-slate-400">附属剧情</dt>
        <dd class="text-right">
          <el-tag
            v-if="card.attachedStoryType"
            :type="attachedStoryTypeTag(card.attachedStoryType)"
            effect="plain"
            size="small"
          >
            {{ card.attachedStoryTypeLabel ?? cardAttachedStoryTypeLabel(card.attachedStoryType) }}
          </el-tag>
          <span v-else>无</span>
        </dd>
      </div>
      <div class="flex justify-between gap-3">
        <dt class="shrink-0 text-slate-400">服装</dt>
        <dd class="text-right">
          {{ card.costumeTypeLabel ?? cardCostumeTypeLabel(card.costumeType) }}
        </dd>
      </div>
    </dl>
  </div>
</template>

<script setup lang="ts">
import {
  cardAttachedStoryTypeLabel,
  cardAttributeLabel,
  cardCostumeTypeLabel,
} from '@/constants/card'
import type { CardVO } from '@/types/card'
import { attachedStoryTypeTag, getAttributeTagStyle } from '@/utils'

defineProps<{
  card: CardVO
}>()
</script>
