<template>
  <template v-for="(seg, i) in line.segments ?? []" :key="seg.id ?? i">
    <template v-if="seg.segmentType === DIALOGUE_SEGMENT_TYPE.TEXT">
      <span :class="{ 'dialogue-line--inner': line.monologue === MONOLOGUE.INNER }">
        {{ seg.content }}
      </span>
    </template>
    <span v-else-if="seg.segmentType === DIALOGUE_SEGMENT_TYPE.STICKER" class="mx-1 align-middle">
      <img
        v-if="seg.stickerImageUrl"
        :src="seg.stickerImageUrl"
        alt="sticker"
        class="inline-block h-6 w-6 align-middle"
      />
      <span v-else>{{ seg.stickerEmoji || seg.stickerLabel }}</span>
    </span>
  </template>
  <template v-if="!line.segments?.length">
    <span :class="{ 'dialogue-line--inner': line.monologue === MONOLOGUE.INNER }">
      {{ line.content }}
    </span>
  </template>
</template>

<script setup lang="ts">
import { DIALOGUE_SEGMENT_TYPE, MONOLOGUE } from '@/constants'
import type { DialogueLineVO } from '@/types/dialogueLine'

defineProps<{
  line: DialogueLineVO
}>()
</script>

<style lang="scss" scoped>
.dialogue-line--inner {
  color: var(--color-blue) !important;
}
</style>
