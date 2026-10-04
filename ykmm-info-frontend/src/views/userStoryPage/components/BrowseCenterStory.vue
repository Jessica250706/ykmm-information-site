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

    <!-- 版本选择 -->
    <div class="mb-4 flex items-center gap-2">
      <span class="text-xs text-slate-500 shrink-0">版本：</span>
      <el-select
        :model-value="selectedVersionKey"
        class="flex-1"
        placeholder="请选择对话版本"
        @update:model-value="(key: string) => emit('update:selectedVersionKey', key)"
      >
        <el-option
          v-for="item in versionSelectItems"
          :key="item.key"
          :label="item.label"
          :value="item.key"
        >
          <span>{{ item.label }}</span>
          <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
            {{ item.count }}
          </span>
        </el-option>
      </el-select>
    </div>

    <!-- 对话内容 -->
    <DialogueView
      :current-option-version="currentOptionVersion"
      :current-version="currentVersion"
      :current-version-id="resolvedVersionId"
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
import type { VersionSelectItem } from '../composables/useVersionSelection'

/* -------- Props / Emits -------- */

const props = defineProps<{
  storyDetail: StoryDetailVO
  color: string
  currentVersionId: number | null
  editingLineId: number | null
  editingMode: boolean
  versionOptions: DialogueVersionOptionVO[] | null
  /** 当前选中的下拉 key（由父级统一管理） */
  selectedVersionKey: string
  /** 预计算的下拉项（由父级统一管理） */
  versionSelectItems: VersionSelectItem[]
}>()

const emit = defineEmits<{
  goBack: []
  'update:currentVersionId': [id: number | null]
  'update:selectedVersionKey': [key: string]
  'select-line': [line: DialogueLineVO]
}>()

/* -------- 从 props 派生 -------- */

/** 当前选中的 option（供 DialogueView 使用），无选中时为 null */
const currentOptionVersion = computed<DialogueVersionOptionVO | null>(() => {
  if (!props.selectedVersionKey) return null
  return props.versionSelectItems.find((i) => i.key === props.selectedVersionKey)?.option ?? null
})

/** 当前选中项对应的真实版本；无 versionId 或 storyDetail 里找不到时为 null */
const currentVersion = computed<DialogueVersionVO | null>(() => {
  if (props.currentVersionId == null) return null
  return props.storyDetail.versions?.find((v) => v.id === props.currentVersionId) ?? null
})

/** 传给 DialogueView 的 versionId，保证与 currentVersion 一致 */
const resolvedVersionId = computed<number | null>(() => currentVersion.value?.id ?? null)
</script>
