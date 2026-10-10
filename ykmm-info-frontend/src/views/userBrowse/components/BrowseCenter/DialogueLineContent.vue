<template>
  <template v-for="(seg, i) in line.segments ?? []" :key="seg.id ?? i">
    <template v-if="seg.segmentType === DIALOGUE_SEGMENT_TYPE.TEXT">
      <span :class="{ 'dialogue-line--inner': line.monologue === MONOLOGUE.INNER }">
        {{ seg.content }}
      </span>
    </template>
    <template v-else-if="seg.segmentType === DIALOGUE_SEGMENT_TYPE.STICKER">
      <!-- 图片表情包：类似微信的大图样式 -->
      <img
        v-if="seg.stickerImageUrl"
        :src="seg.stickerImageUrl"
        alt="sticker"
        class="sticker-image"
        draggable="false"
      />
      <!-- emoji / 文本兜底 -->
      <span v-else class="sticker-emoji mx-1 align-middle">
        {{ seg.stickerEmoji || seg.stickerLabel }}
      </span>
    </template>
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

/**
 * 图片表情包：
 * - block 布局，避免受父级 line-height / inline 排版影响
 * - 限制最大边长，保持比例
 */
.sticker-image {
  display: block;
  max-width: 140px;
  max-height: 140px;
  width: auto;
  height: auto;
  object-fit: contain;
  user-select: none;
  -webkit-user-drag: none;
}

.sticker-emoji {
  display: inline-block;
  font-size: 26px;
  line-height: 1;
  vertical-align: middle;
}
</style>
