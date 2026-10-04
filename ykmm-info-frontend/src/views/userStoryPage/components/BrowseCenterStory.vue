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

    <!-- 版本选择：列出全部版本，无论是否有内容 -->
    <div class="mb-4 flex items-center gap-2">
      <span class="text-xs text-slate-500 shrink-0">版本：</span>
      <el-select
        :model-value="selectedVersionKey"
        class="flex-1"
        placeholder="请选择对话版本"
        @update:model-value="onVersionChange"
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

    <!-- 对话内容：只渲染当前版本 -->
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
import { computed, ref, watch } from 'vue'
import { VERSION_FORMAT } from '@/constants/index'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO, DialogueVersionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'
import DialogueView from './DialogueView.vue'

/* -------- Props / Emits -------- */

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

/* -------- 格式化 -------- */

/** 由真实版本生成完整文案：语言 · 形式 · 范围（数量） */
function formatVersionLabel(v: DialogueVersionVO): string {
  return (
    [v.languageLabel, v.formatLabel, v.scopeLabel].filter(Boolean).join(' · ') || `版本 ${v.id}`
  )
}

/* -------- 预计算：id -> version 索引 -------- */

const versionMapById = computed(() => {
  const map = new Map<number, DialogueVersionVO>()
  for (const v of props.storyDetail.versions ?? []) {
    if (v.id != null) map.set(v.id, v)
  }
  return map
})

/* -------- 预计算：下拉项 -------- */

interface VersionSelectItem {
  /** el-option 的 value */
  key: string
  /** el-option 的展示文案 */
  label: string
  /** 版本 id，无内容时为 null */
  versionId: number | null
  /** 原始 option 数据，透传给 DialogueView */
  option: DialogueVersionOptionVO
  /** 对应的数据 */
  count: string
}

const versionSelectItems = computed<VersionSelectItem[]>(() => {
  const options = props.versionOptions ?? []

  return options.map((opt, index) => {
    const versionId = opt.versionId ?? null
    const key = versionId != null ? `id:${versionId}` : `label:${opt.label ?? index}`
    const label = opt ? formatVersionLabel(opt) : `版本 ${versionId ?? index}`
    const count =
      opt.format === VERSION_FORMAT.IMAGE
        ? `${opt.images?.length ?? 0} 张`
        : `${opt.lines?.length ?? 0} 句`

    return { key, label, versionId, option: opt, count }
  })
})

/** key -> item，O(1) 查找 */
const versionItemMap = computed(() => {
  const map = new Map<string, VersionSelectItem>()
  for (const item of versionSelectItems.value) map.set(item.key, item)
  return map
})

/* -------- 选中态：本地 ref 是 source of truth -------- */

const selectedVersionKey = ref<string>('')

/**
 * 外部 currentVersionId 变化时同步本地 key。
 * 若本地已选中同一 option，跳过，避免来回覆盖造成循环。
 */
watch(
  () => props.currentVersionId,
  (id) => {
    const current = versionItemMap.value.get(selectedVersionKey.value)
    if (current && current.versionId === (id ?? null)) return
    selectedVersionKey.value = id == null ? '' : `id:${id}`
  },
  { immediate: true },
)

/** 当前选中的 option：唯一能同时表达"有版本 / 无版本"的来源 */
const currentOptionVersion = computed<DialogueVersionOptionVO | null>(
  () => versionItemMap.value.get(selectedVersionKey.value)?.option ?? null,
)

/** 当前选中项对应的真实版本；无 versionId 或 storyDetail 里找不到时为 null */
const currentVersion = computed<DialogueVersionVO | null>(() => {
  const versionId = currentOptionVersion.value?.versionId
  if (versionId == null) return null
  return versionMapById.value.get(versionId) ?? null
})

/** 传给 DialogueView 的 versionId，保证与 currentVersion 一致 */
const resolvedVersionId = computed<number | null>(() => currentVersion.value?.id ?? null)

/** 选中变化：同步本地 key，并把 versionId（可能为 null）向上 emit */
function onVersionChange(key: string) {
  selectedVersionKey.value = key
  const versionId = versionItemMap.value.get(key)?.versionId ?? null
  emit('update:currentVersionId', versionId)
}
</script>
