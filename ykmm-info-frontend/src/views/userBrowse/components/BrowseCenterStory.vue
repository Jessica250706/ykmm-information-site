<template>
  <div class="flex flex-col h-full min-h-0">
    <!-- 标题区域 -->
    <div class="mb-4 shrink-0">
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
    <div class="mb-4 flex items-center gap-2 shrink-0">
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
    <div class="relative flex-1 min-h-0">
      <!-- 滚动区 -->
      <div
        ref="scrollerRef"
        :class="{
          'overflow-y-auto': currentVersion?.format === VERSION_FORMAT.TEXT,
          'overflow-hidden': currentVersion?.format === VERSION_FORMAT.IMAGE,
        }"
        class="h-full app-scrollbar"
      >
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

      <!-- 顶部内阴影（滚动离开顶部时显示） -->
      <div
        :class="showTopShadow ? 'opacity-100' : 'opacity-0'"
        class="scroll-shadow-top pointer-events-none absolute inset-x-0 top-0 h-6 transition-opacity duration-200"
      />

      <!-- 底部内阴影（未滚到底时显示） -->
      <div
        :class="showBottomShadow ? 'opacity-100' : 'opacity-0'"
        class="scroll-shadow-bottom pointer-events-none absolute inset-x-0 bottom-0 h-6 transition-opacity duration-200"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useScrollShadow } from '@/composables/useScrollShadow'
import { VERSION_FORMAT } from '@/constants/index'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO, DialogueVersionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'
import DialogueView from './DialogueView.vue'
import type { VersionSelectItem } from '../composables/useVersionSelection.ts'

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

/* -------- 阴影 -------- */
const scrollerRef = ref<HTMLElement | null>(null)
const { atTop, atBottom, canScroll } = useScrollShadow(scrollerRef)

// 有内容可滚 且 已经离开顶部 → 显示顶部阴影
const showTopShadow = computed(() => canScroll.value && !atTop.value)
// 有内容可滚 且 未到底 → 显示底部阴影
const showBottomShadow = computed(() => canScroll.value && !atBottom.value)
</script>

<style lang="scss" scoped>
.scroll-shadow-top {
  background: linear-gradient(to bottom, var(--el-fill-color-darker) 0%, transparent 100%);
}
.scroll-shadow-bottom {
  background: linear-gradient(to top, var(--el-fill-color-darker) 0%, transparent 100%);
}
</style>
