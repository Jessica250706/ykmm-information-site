<template>
  <el-card class="min-w-0 flex-1 overflow-auto bg-slate-50 p-5">
    <el-skeleton v-if="loadingContent" :rows="6" animated />

    <!-- Story 详情 -->
    <BrowseCenterStory
      v-else-if="storyDetail"
      v-model:current-version-id="currentVersionIdComputed"
      :color="color"
      :editing-line-id="editingLineId"
      :editing-mode="editingMode"
      :story-detail="storyDetail"
      :version-options="versionOptions"
      @go-back="emit('goBack')"
      @select-line="(line) => emit('select-line', line)"
    />

    <!-- 分类：子分类 + 剧情列表 -->
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
}>()

const emit = defineEmits<{
  /** 点击子分类 */
  goCategory: [id: number]
  /** 点击剧情 */
  goStory: [id: number]
  /** 返回 */
  goBack: []
  'update:currentVersionId': [id: number | null]
  'select-line': [line: DialogueLineVO]
}>()

/** 桥接 v-model:currentVersionId，子组件内部使用 update:currentVersionId */
const currentVersionIdComputed = computed({
  get: () => props.currentVersionId,
  set: (val) => emit('update:currentVersionId', val),
})
</script>
