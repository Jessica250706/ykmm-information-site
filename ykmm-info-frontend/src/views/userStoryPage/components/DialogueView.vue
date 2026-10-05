<template>
  <div class="dialogue-view h-full">
    <!-- 没有选中的版本 -->
    <el-empty v-if="!currentOptionVersion" description="请选择一个对话版本" />
    <!-- 没有选中的版本 -->
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
          <el-avatar :size="36" :src="line.speakerAvatar">
            {{ line.speakerName?.charAt(0) || '?' }}
          </el-avatar>
          <div class="min-w-0 flex-1">
            <div class="text-xs font-medium text-slate-500">{{ line.speakerName }}</div>
            <div class="mt-1 whitespace-pre-line text-sm text-slate-800">
              <template v-for="(seg, i) in line.segments ?? []" :key="seg.id ?? i">
                <template v-if="seg.segmentType === 1">
                  <span :class="{ 'dialogue-line--inner': line.monologue === MONOLOGUE.INNER }">
                    {{ seg.content }}
                  </span>
                </template>
                <span v-else-if="seg.segmentType === 2" class="mx-1 align-middle">
                  <img
                    v-if="seg.stickerUrl"
                    :src="seg.stickerUrl"
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

      <div class="overflow-hidden h-full">
        <ImageView :image-list="imageUrls" :magnifier="false" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { type ComponentPublicInstance, computed, nextTick, watch } from 'vue'
import { VERSION_FORMAT } from '@/constants/index'
import { MONOLOGUE } from '@/constants/index'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import type { DialogueVersionVO, StoryDetailVO } from '@/types/story'

const props = defineProps<{
  detail: StoryDetailVO
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
</style>
