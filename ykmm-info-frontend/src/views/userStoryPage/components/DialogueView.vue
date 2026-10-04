<template>
  <div class="dialogue-view">
    <el-empty v-if="!detail.versions?.length" description="暂无对话内容" />

    <el-tabs
      v-else
      :model-value="String(currentVersionId)"
      type="border-card"
      @update:model-value="onTabChange"
    >
      <el-tab-pane
        v-for="v in detail.versions"
        :key="v.id"
        :label="versionLabel(v)"
        :name="String(v.id)"
      >
        <!-- 文字对话 -->
        <template v-if="v.format === 1">
          <div v-if="!v.lines?.length" class="py-6 text-center text-slate-400">暂无内容</div>
          <div v-else class="space-y-3">
            <div
              v-for="line in v.lines"
              :key="line.id"
              :ref="(el) => setLineRef(line.id, el)"
              :class="[
                line.id === editingLineId ? 'bg-indigo-50 ring-2 ring-indigo-300' : 'bg-slate-50',
                editingMode ? 'cursor-pointer hover:bg-indigo-50/50' : '',
              ]"
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
                    <template v-if="seg.segmentType === 1">{{ seg.content }}</template>
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
                    {{ line.content }}
                  </template>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 图片对话 -->
        <template v-else>
          <div v-if="!v.images?.length" class="py-6 text-center text-slate-400">暂无图片</div>
          <div v-else class="grid grid-cols-1 gap-3 md:grid-cols-2">
            <img
              v-for="img in v.images"
              :key="img.id"
              :src="img.url"
              alt="dialogue"
              class="w-full rounded-lg shadow-sm"
            />
          </div>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { type ComponentPublicInstance, nextTick, watch } from 'vue'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'

const props = defineProps<{
  detail: StoryDetailVO
  /** 当前选中的版本 id */
  currentVersionId: number | null
  /** 当前编辑的句子 id */
  editingLineId: number | null
  /** 是否编辑模式，只有编辑模式下点击句子才触发选中 */
  editingMode: boolean
}>()

const emit = defineEmits<{
  'update:currentVersionId': [id: number | null]
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

/* -------- 版本切换 -------- */
function onTabChange(name: string | number) {
  const id = Number(name)
  emit('update:currentVersionId', Number.isNaN(id) ? null : id)
}

/* -------- 句子点击 -------- */
function onLineClick(line: DialogueLineVO) {
  if (!props.editingMode) return
  emit('select-line', line)
}

/* -------- 自动滚动到高亮行 -------- */
watch(
  () => props.editingLineId,
  async (id) => {
    if (id == null) return
    await nextTick()
    const el = lineRefMap.get(id)
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  },
)

function versionLabel(v: DialogueVersionVO): string {
  return (
    [v.languageLabel, v.formatLabel, v.scopeLabel].filter(Boolean).join(' · ') || `版本 ${v.id}`
  )
}
</script>
