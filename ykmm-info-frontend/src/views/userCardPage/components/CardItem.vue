<template>
  <article
    class="group cursor-pointer overflow-hidden rounded-xl bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
    @click="emit('click')"
  >
    <div class="aspect-3/4 overflow-hidden bg-slate-100 relative">
      <img
        v-if="coverImage"
        :alt="card.name"
        :src="coverImage"
        class="h-full w-full object-cover transition duration-300 group-hover:scale-105"
      />
      <div v-else class="h-full w-full bg-linear-to-br from-slate-200 to-slate-300" />
      <el-tag
        v-if="showStatusBadge"
        :type="statusTagType(card.status)"
        class="absolute right-2 top-2"
        effect="dark"
        size="small"
      >
        {{ statusLabel(card.status) }}
      </el-tag>
    </div>
    <div class="p-3">
      <div class="flex items-center justify-between gap-1 truncate">
        <div class="flex items-center gap-1 truncate">
          <el-tag
            :type="card.maxRarity === CARD_MAX_RARITY.UR ? 'danger' : 'warning'"
            effect="plain"
            size="small"
          >
            {{ card.maxRarityLabel ?? cardMaxRarityLabel(card.maxRarity) }}
          </el-tag>
          <p class="truncate font-medium">{{ card.name || '-' }}</p>
        </div>
        <el-tag
          v-if="card.attribute"
          :style="getAttributeTagStyle(card.attribute)"
          effect="plain"
          size="small"
        >
          {{ card.attributeLabel ?? cardAttributeLabel(card.attribute) }}
        </el-tag>
        <span v-else class="text-slate-300">-</span>
      </div>
      <p class="mt-1 truncate text-xs text-slate-500">{{ card.seriesName || '-' }}</p>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  CARD_MAX_RARITY,
  cardAttributeLabel,
  cardMaxRarityLabel,
  STATUS,
  statusLabel,
  statusTagType,
} from '@/constants'
import type { CardVO } from '@/types/card'
import { getAttributeTagStyle, getCoverImage } from '@/utils'

const props = defineProps<{ card: CardVO }>()

const emit = defineEmits<{ (e: 'click'): void }>()

const coverImage = computed(() => getCoverImage(props.card))
const showStatusBadge = computed(
  () => props.card.status === STATUS.PENDING || props.card.status === STATUS.REJECTED,
)
</script>
