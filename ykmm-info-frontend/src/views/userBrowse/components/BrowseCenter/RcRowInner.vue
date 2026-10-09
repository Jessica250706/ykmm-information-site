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

      <div
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
import { isRightSide } from '@/constants/dialogueLine'
import type { DialogueLineVO } from '@/types/dialogueLine'
import { avatarBubbleStyle, chatBubbleStyle } from '@/utils'
import DialogueLineContent from './DialogueLineContent.vue'

const props = defineProps<{
  line: DialogueLineVO
}>()

const right = computed(() => isRightSide(props.line.side))
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
</style>
