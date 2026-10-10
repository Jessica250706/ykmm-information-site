<template>
  <div class="rc-row__inner">
    <!-- 左侧：显示头像；右侧：不显示头像 -->
    <el-avatar
      v-if="!right"
      :size="36"
      :src="line.personAvatar || undefined"
      :style="!line.personAvatar ? avatarBubbleStyle(line.personThemeColor) : undefined"
      class="rc-avatar"
    >
      {{ line.speakerName?.slice(-1) || '?' }}
    </el-avatar>

    <div class="rc-body">
      <!-- 左侧才显示说话人名字 -->
      <div v-if="!right" class="rc-name">
        {{ line.speakerName }}
      </div>

      <!-- 纯图片表情包：无气泡，只显示大图 -->
      <div v-if="isStickerImageOnly" class="rc-sticker">
        <DialogueLineContent :line="line" />
      </div>

      <!-- 文字 / 混合内容：正常气泡 -->
      <div
        v-else
        :class="right ? 'rc-bubble--right' : 'rc-bubble--left'"
        :style="right ? chatBubbleStyle(line.personThemeColor) : undefined"
        class="rc-bubble"
      >
        <DialogueLineContent :line="line" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { DIALOGUE_SEGMENT_TYPE } from '@/constants'
import { isRightSide } from '@/constants/dialogueLine'
import type { DialogueLineVO } from '@/types/dialogueLine'
import { avatarBubbleStyle, chatBubbleStyle } from '@/utils'
import DialogueLineContent from './DialogueLineContent.vue'

const props = defineProps<{
  line: DialogueLineVO
}>()

const right = computed(() => isRightSide(props.line.side))

/**
 * 是否为“纯图片表情包”行：
 * - 至少有一个 segment
 * - 所有 segment 都是 STICKER 类型
 * - 所有 STICKER 都带 stickerImageUrl
 *
 * 满足时去掉气泡，直接渲染大图。
 */
const isStickerImageOnly = computed(() => {
  const segs = props.line.segments ?? []
  if (!segs.length) return false
  return segs.every((s) => s.segmentType === DIALOGUE_SEGMENT_TYPE.STICKER && !!s.stickerImageUrl)
})
</script>

<style lang="scss" scoped>
.rc-row__inner {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.rc-avatar {
  flex-shrink: 0;
}

.rc-body {
  max-width: 70%;
  min-width: 0;
}

.rc-name {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.rc-bubble {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-line;
  word-break: break-word;
  text-align: left;
  color: var(--el-text-color-primary);
  background: var(--el-fill-color-light);
}

.rc-bubble--left {
  border-top-left-radius: 2px;
}

.rc-bubble--right {
  border-top-right-radius: 2px;
  background: var(--el-color-primary-light-9);
}

/* 纯图片表情包容器：无背景、无内边距，让图片直接呈现 */
.rc-sticker {
  display: inline-block;
  line-height: 0;
  background: transparent;
  padding: 0;
  border: none;
}
</style>
