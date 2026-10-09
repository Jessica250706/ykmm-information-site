<template>
  <div class="dialogue-view h-full">
    <!-- 没有选中的版本 -->
    <el-empty v-if="!currentOptionVersion" description="请选择一个对话版本" />
    <el-empty v-else-if="!currentVersion" description="当前对话版本暂无对话" />

    <!-- 文字版本 -->
    <template v-else-if="currentVersion?.format === VERSION_FORMAT.TEXT">
      <div v-if="!currentVersion.lines?.length" class="py-8">
        <el-empty description="暂无对话">
          <template v-if="editingMode" #default>
            <div class="mt-2 text-xs text-slate-400">可以在右侧编辑区新增对话</div>
          </template>
        </el-empty>
      </div>

      <!-- RC 聊天样式 -->
      <div v-else-if="isRcChat" class="rc-chat">
        <template v-for="block in rcRenderBlocks" :key="block.key">
          <!-- 普通行 -->
          <div
            v-if="block.type === 'line'"
            :ref="(el) => setLineRef(block.line.id, el)"
            :class="[
              isRightSide(block.line.side) ? 'rc-row--right' : 'rc-row--left',
              {
                'dialogue-line--selected': block.line.id === editingLineId,
                'dialogue-line--editable': editingMode,
              },
            ]"
            class="rc-row"
            @click="onLineClick(block.line)"
          >
            <RcRowInner :line="block.line" />
          </div>

          <!-- 选项组（问句 + 回答） -->
          <div v-else class="rc-option-card">
            <div class="rc-option-card__badge">选项 {{ block.optionNumber }}</div>

            <div
              v-for="line in block.lines"
              :key="line.id"
              :ref="(el) => setLineRef(line.id, el)"
              :class="[
                isRightSide(line.side) ? 'rc-row--right' : 'rc-row--left',
                {
                  'dialogue-line--selected': line.id === editingLineId,
                  'dialogue-line--editable': editingMode,
                },
              ]"
              class="rc-row"
              @click="onLineClick(line)"
            >
              <RcRowInner :line="line" />
            </div>
          </div>
        </template>
      </div>

      <!-- 普通（非 RC）样式 -->
      <div v-else>
        <div
          v-for="line in currentVersion.lines"
          :key="line.id"
          :ref="(el) => setLineRef(line.id, el)"
          :class="{
            'dialogue-line--selected': line.id === editingLineId,
            'dialogue-line--editable': editingMode,
          }"
          class="flex items-start gap-3 rounded-lg p-3 transition"
          @click="onLineClick(line)"
        >
          <el-avatar
            :size="36"
            :src="line.personAvatar || undefined"
            :style="!line.personAvatar ? avatarBubbleStyle(line.personThemeColor) : undefined"
          >
            {{ line.speakerName?.slice(-1) || '?' }}
          </el-avatar>
          <div class="min-w-0 flex-1">
            <div class="text-xs font-medium text-slate-500">{{ line.speakerName }}</div>
            <div class="mt-1 whitespace-pre-line text-sm text-slate-800">
              <DialogueLineContent :line="line" />
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 图片版本 -->
    <template v-else-if="currentVersion?.format === VERSION_FORMAT.IMAGE">
      <div v-if="!imageUrls.length" class="py-8">
        <el-empty description="暂无图片" />
      </div>

      <div v-else class="h-full overflow-hidden">
        <ImageView :image-list="imageUrls" :magnifier="false" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { type ComponentPublicInstance, computed, nextTick, watch } from 'vue'
import { SOURCE_TYPE, VERSION_FORMAT } from '@/constants'
import { DIALOGUE_ROLE, isRightSide } from '@/constants/dialogueLine'
import type { CardEpisodeVO } from '@/types/card'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import type { DialogueVersionVO, StoryDetailVO } from '@/types/story'
import { avatarBubbleStyle } from '@/utils'
import DialogueLineContent from './DialogueLineContent.vue'
import RcRowInner from './RcRowInner.vue'

const props = defineProps<{
  detail?: StoryDetailVO | CardEpisodeVO | null
  /** 当前版本的 id */
  currentVersionId: number | null
  /** 当前版本（已由父组件计算好） */
  currentVersion: DialogueVersionVO | null
  /** 当前版本（无 id 版） */
  currentOptionVersion: DialogueVersionOptionVO | null
  /** 当前编辑的句子 id */
  editingLineId: number | null
  /** 是否编辑模式 */
  editingMode: boolean
}>()

const emit = defineEmits<{
  'select-line': [line: DialogueLineVO]
}>()

/** 当前版本是否为 RC，决定是否启用聊天样式 */
const isRcChat = computed(() => props.currentVersion?.sourceType === SOURCE_TYPE.RC)

/* -------- RC 渲染分组 -------- */

type RcRenderBlock =
  | { type: 'line'; key: string; line: DialogueLineVO }
  | { type: 'option'; key: string; optionNumber: number; lines: DialogueLineVO[] }

/**
 * 把 RC 的 lines 组装成渲染块：
 * - 普通行：独立渲染
 * - 问句/回答：按 optionNumber 聚合，插入到该选项第一行出现的位置
 */
const rcRenderBlocks = computed<RcRenderBlock[]>(() => {
  const lines = props.currentVersion?.lines ?? []
  if (!lines.length) return []

  /** optionNumber -> 该选项下的行 */
  const optionMap = new Map<number, DialogueLineVO[]>()
  for (const line of lines) {
    const isQ = line.dialogueRole === DIALOGUE_ROLE.QUESTION
    const isA = line.dialogueRole === DIALOGUE_ROLE.ANSWER
    if ((isQ || isA) && line.optionNumber != null) {
      const n = line.optionNumber
      if (!optionMap.has(n)) optionMap.set(n, [])
      optionMap.get(n)!.push(line)
    }
  }

  /** 按出现顺序构建渲染块 */
  const blocks: RcRenderBlock[] = []
  const added = new Set<number>()
  for (const line of lines) {
    const isQ = line.dialogueRole === DIALOGUE_ROLE.QUESTION
    const isA = line.dialogueRole === DIALOGUE_ROLE.ANSWER
    if ((isQ || isA) && line.optionNumber != null) {
      const n = line.optionNumber
      if (!added.has(n)) {
        added.add(n)
        blocks.push({
          type: 'option',
          key: `option-${n}`,
          optionNumber: n,
          lines: optionMap.get(n) ?? [],
        })
      }
    } else {
      blocks.push({ type: 'line', key: `line-${line.id}`, line })
    }
  }
  return blocks
})

/* -------- 句子 ref，用于滚动定位 -------- */
const lineRefMap = new Map<number, HTMLElement>()
function setLineRef(id: number | undefined, el: Element | ComponentPublicInstance | null) {
  if (id == null) return
  if (el instanceof HTMLElement) {
    lineRefMap.set(id, el)
  } else {
    lineRefMap.delete(id)
  }
}

/* -------- 句子点击 -------- */
function onLineClick(line: DialogueLineVO) {
  if (!props.editingMode) return
  emit('select-line', line)
}

/* -------- 高亮时自动滚动 -------- */
watch(
  () => props.editingLineId,
  async (id) => {
    if (id == null) return
    await nextTick()
    const el = lineRefMap.get(id)
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  },
)

/* -------- 图片列表 -------- */
const imageUrls = computed(() =>
  (props.currentVersion?.images ?? []).map((img) => img.url).filter((url): url is string => !!url),
)
</script>

<style lang="scss" scoped>
.dialogue-line--inner {
  color: var(--color-blue) !important;
}

/* ================= 普通（非 RC）样式 ================= */
.dialogue-line {
  background: var(--el-fill-color-lighter);
  transition:
    background-color 0.15s,
    box-shadow 0.15s;
}

.dialogue-line--editable {
  cursor: pointer;
}

.dialogue-line--editable:hover {
  background: var(--el-color-primary-light-9);
}

.dialogue-line--selected {
  background: var(--el-color-info-light-9);
  box-shadow: inset 0 0 0 2px var(--el-color-primary-light-5);
}

/* ================= RC 聊天样式 ================= */
.rc-chat {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 4px;
  flex-shrink: 0;
}

.rc-row {
  padding: 6px 8px;
  border-radius: 8px;
  transition:
    background-color 0.15s,
    box-shadow 0.15s;
}

.rc-row--left .rc-row__inner {
  justify-content: flex-start;
}

.rc-row--right .rc-row__inner {
  justify-content: flex-end;
}

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

/* 左侧（对方）：浅灰气泡，气泡角在左上 */
.rc-bubble--left {
  border-top-left-radius: 2px;
}

/* 右侧（自己）：背景色由 chatBubbleStyle 注入，气泡角在右上 */
.rc-bubble--right {
  border-top-right-radius: 2px;
  background: var(--el-color-primary-light-9);
}

/* 编辑模式下气泡可点击 */
.rc-row.dialogue-line--editable {
  cursor: pointer;
}

.rc-row.dialogue-line--editable:hover {
  background: var(--el-color-primary-light-9);
}

.rc-row.dialogue-line--selected {
  background: var(--el-color-info-light-9);
  box-shadow: inset 0 0 0 2px var(--el-color-primary-light-5);
}

/* ================= RC 选项卡片 ================= */
.rc-option-card {
  position: relative;
  margin: 8px 0 4px;
  padding: 16px 8px 8px;
  border-radius: 12px;
  border: 1px dashed var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}

.rc-option-card__badge {
  position: absolute;
  top: -10px;
  left: 12px;
  padding: 2px 10px;
  border-radius: 10px;
  background: var(--el-color-primary);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

/* 选项卡片内的行：略微调整，避免与卡片背景叠加过重 */
.rc-option-card .rc-row {
  padding: 4px 6px;
}

.rc-option-card .rc-row.dialogue-line--selected {
  background: var(--el-color-white);
  box-shadow: inset 0 0 0 2px var(--el-color-primary-light-5);
}
</style>
