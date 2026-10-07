<template>
  <el-card
    body-class="flex-1 min-h-0 flex flex-col p-5"
    class="min-w-0 flex-1 flex flex-col overflow-hidden bg-slate-50"
  >
    <el-skeleton v-if="loadingContent" :rows="6" animated />

    <BrowseCenterStory
      v-else-if="storyDetail"
      v-model:current-version-id="currentVersionIdComputed"
      v-model:selected-version-key="selectedVersionKeyComputed"
      :color="color"
      :editing-line-id="editingLineId"
      :editing-mode="editingMode"
      :story-detail="storyDetail"
      :version-options="versionOptions"
      :version-select-items="versionSelectItems"
      @go-back="emit('goBack')"
      @select-line="(line) => emit('select-line', line)"
    />

    <BrowseCenterCategory
      v-else-if="currentCategory"
      :color="color"
      :current-category="currentCategory"
      :loading-stories="loadingStories"
      :stories="stories"
      :type-label="typeLabel"
      @go-back="emit('goBack')"
      @go-category="(id) => emit('goCategory', id)"
      @go-story="(id) => emit('goStory', id)"
    />

    <el-empty v-else description="请从左侧选择一个分类" />
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO, StoryVO } from '@/types/story'
import type { StoryCategoryVO } from '@/types/storyCategory'
import BrowseCenterCategory from './BrowseCenterCategory.vue'
import BrowseCenterStory from './BrowseCenterStory.vue'
import type { VersionSelectItem } from '../composables/useVersionSelection.ts'

const props = defineProps<{
  /** 详情加载中 */
  loadingContent: boolean
  /** 当前剧情详情 */
  storyDetail: StoryDetailVO | null
  /** 当前分类 */
  currentCategory: StoryCategoryVO | null
  /** 类型标签，用于分类标签回退显示 */
  typeLabel: string
  /** 剧情列表加载中 */
  loadingStories: boolean
  /** 当前分类下的剧情列表 */
  stories: StoryVO[]
  /** 当前分类色 */
  color: string
  /** 当前选中的版本 id */
  currentVersionId: number | null
  /** 当前编辑的句子 id */
  editingLineId: number | null
  /** 是否处于编辑模式 */
  editingMode: boolean
  /** 所有版本选项 */
  versionOptions: DialogueVersionOptionVO[] | null
  /** 当前选中的下拉 key */
  selectedVersionKey: string
  /** 预计算的下拉项 */
  versionSelectItems: VersionSelectItem[]
}>()

const emit = defineEmits<{
  /** 点击子分类 */
  goCategory: [id: number]
  /** 点击剧情 */
  goStory: [id: number]
  /** 返回 */
  goBack: []
  'update:currentVersionId': [id: number | null]
  'update:selectedVersionKey': [key: string]
  'select-line': [line: DialogueLineVO]
}>()

const currentVersionIdComputed = computed({
  get: () => props.currentVersionId,
  set: (val) => emit('update:currentVersionId', val),
})

const selectedVersionKeyComputed = computed({
  get: () => props.selectedVersionKey,
  set: (key) => emit('update:selectedVersionKey', key),
})
</script>
