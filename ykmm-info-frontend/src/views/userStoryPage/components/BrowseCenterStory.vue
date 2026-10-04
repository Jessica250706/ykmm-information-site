<template>
  <div>
    <!-- 标题区域 -->
    <div class="mb-4">
      <div class="flex justify-between items-center">
        <h2 class="text-lg font-semibold">
          {{ storyDetail.title || `剧情 #${storyDetail.id}` }}
        </h2>
        <el-button @click="emit('goBack')">← 返回</el-button>
      </div>
      <div class="mt-1 flex flex-wrap items-center gap-2 text-xs text-slate-400">
        <el-tag
          :style="{
            borderColor: `var(--color-${color})`,
            color: `var(--color-${color})`,
          }"
          effect="plain"
          size="small"
        >
          {{ storyDetail.categoryTypeLabel }}
        </el-tag>
        <span>{{ storyDetail.categoryName }}</span>
      </div>
      <p v-if="storyDetail.description" class="mt-3 whitespace-pre-line text-sm text-slate-600">
        {{ storyDetail.description }}
      </p>
    </div>

    <!-- 版本分类选择：列出全部版本，无论是否有内容 -->
    <div class="mb-4 flex items-center gap-2">
      <span class="text-xs text-slate-500 shrink-0">版本：</span>
      <el-select
        v-model="selectedVersionKey"
        class="flex-1"
        placeholder="请选择对话版本"
        @update:model-value="onVersionChange"
      >
        <el-option
          v-for="(v, index) in versionOptions ?? []"
          :key="`${v.versionId ?? 'void'}-${index}`"
          :label="optionLabel(v, index)"
          :value="optionKey(v, index)"
        />
      </el-select>
    </div>

    <!-- 对话内容：只渲染当前版本 -->
    <DialogueView
      :current-option-version="currentOptionVersion"
      :current-version="currentVersion"
      :current-version-id="currentVersionId"
      :detail="storyDetail"
      :editing-line-id="editingLineId ?? null"
      :editing-mode="editingMode"
      @select-line="(line) => emit('select-line', line)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO, DialogueVersionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'
import DialogueView from './DialogueView.vue'

const props = defineProps<{
  storyDetail: StoryDetailVO
  color: string
  currentVersionId: number | null
  editingLineId: number | null
  editingMode: boolean
  versionOptions: DialogueVersionOptionVO[] | null
}>()

const emit = defineEmits<{
  goBack: []
  'update:currentVersionId': [id: number | null]
  'select-line': [line: DialogueLineVO]
}>()

/* -------- 版本选择：字符串 key 桥接 -------- */

function optionKey(v: DialogueVersionOptionVO, index: number): string {
  if (v.versionId != null) return `id:${v.versionId}`
  return `label:${v.label ?? index}`
}

const selectedVersionKey = computed<string>({
  get() {
    const id = props.currentVersionId
    if (id == null) return ''
    if (typeof id === 'number') return `id:${id}`
    return `label:${id}`
  },
  set(key: string) {
    if (!key) {
      emit('update:currentVersionId', null)
      return
    }
    if (key.startsWith('id:')) {
      emit('update:currentVersionId', Number(key.slice(3)))
    } else if (key.startsWith('label:')) {
      emit('update:currentVersionId', Number(key.slice(6)))
    }
  },
})

/** 当前选中的版本（按数字 id 精确匹配） */
const currentVersion = computed<DialogueVersionVO | null>(() => {
  const id = props.currentVersionId
  if (typeof id !== 'number') return null
  const versions = props.storyDetail?.versions
  if (!versions?.length) return null
  return versions.find((v) => v.id === id) ?? null
})

/** 当前选中的版本（无 id 的 option） */
const currentOptionVersion = computed<DialogueVersionOptionVO | null>(() => {
  const id = props.currentVersionId
  if (id == null) return null
  if (typeof id === 'number') {
    return props.versionOptions?.find((v) => v.versionId === id) ?? null
  }
  return props.versionOptions?.find((v) => v.label === id) ?? null
})

function onVersionChange(id: number | null | undefined) {
  emit('update:currentVersionId', id ?? null)
}

/**
 * 版本下拉 label：
 * 优先用 option 自带的 label；否则回退到版本的组合描述。
 */
function optionLabel(v: DialogueVersionOptionVO, index: number): string {
  if (v.label) return v.label
  const version = props.storyDetail.versions?.find((it) => it.id === v.versionId)
  if (version) return versionLabel(version)
  return `版本 ${v.versionId ?? index}`
}

/**
 * 版本标签：语言 · 形式 · 范围（内容数量）
 */
function versionLabel(v: DialogueVersionVO): string {
  const base =
    [v.languageLabel, v.formatLabel, v.scopeLabel].filter(Boolean).join(' · ') || `版本 ${v.id}`

  if (v.format === 2) {
    const count = v.images?.length ?? 0
    return `${base}（${count} 张）`
  }
  const count = v.lines?.length ?? 0
  return `${base}（${count} 句）`
}
</script>
