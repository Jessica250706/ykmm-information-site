<template>
  <el-card
    body-class="flex-1 min-h-0 flex flex-col p-5"
    class="min-w-0 flex-1 flex flex-col overflow-hidden bg-slate-50"
  >
    <!-- 顶部：左右栏开关（保持不变） -->
    <div class="mb-3 flex shrink-0 items-center justify-between border-b pb-2">
      <el-button size="small" text @click="emit('toggleLeft')">
        <span class="mr-1">{{ showLeft ? '◀' : '▶' }}</span>
        {{ showLeft ? '收起目录' : '展开目录' }}
      </el-button>
      <el-button size="small" text @click="emit('toggleRight')">
        {{ showRight ? '收起编辑区' : '展开编辑区' }}
        <span class="ml-1">{{ showRight ? '▶' : '◀' }}</span>
      </el-button>
    </div>

    <!-- ★ 关键改动：只在「从未加载过任何数据」时显示 skeleton -->
    <el-skeleton v-if="loadingContent && !hasAnyData" :rows="6" animated />

    <template v-else>
      <BrowseCenterStory
        v-if="storyDetail && sourceType === SOURCE_TYPE.STORY"
        v-model:current-version-id="currentVersionIdComputed"
        v-model:selected-version-key="selectedVersionKeyComputed"
        :color="color"
        :editing-line-id="editingLineId"
        :editing-mode="editingMode"
        :source-type="sourceType"
        :story-detail="storyDetail"
        :version-options="versionOptions"
        :version-select-items="versionSelectItems"
        @go-back="emit('goBack')"
        @select-line="(line) => emit('select-line', line)"
      />

      <BrowseCenterCategory
        v-else-if="currentCategory && sourceType === SOURCE_TYPE.STORY"
        :color="color"
        :current-category="currentCategory"
        :loading-stories="loadingStories"
        :stories="stories"
        :type-label="typeLabel"
        @go-back="emit('goBack')"
        @go-category="(id) => emit('goCategory', id)"
        @go-story="(id) => emit('goStory', id)"
      />

      <BrowseCenterStory
        v-else-if="
          cardDetail &&
          (sourceType === SOURCE_TYPE.RC ||
            sourceType === SOURCE_TYPE.RTV ||
            sourceType === SOURCE_TYPE.RABITTER)
        "
        v-model:current-version-id="currentVersionIdComputed"
        v-model:selected-version-key="selectedVersionKeyComputed"
        :card-detail="cardDetail"
        :card-name="cardName"
        :color="color"
        :editing-line-id="editingLineId"
        :editing-mode="editingMode"
        :source-type="sourceType"
        :version-options="versionOptions"
        :version-select-items="versionSelectItems"
        @go-back="emit('goBack')"
        @select-line="(line) => emit('select-line', line)"
      />

      <el-empty v-else description="请从左侧选择一个分类" />
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { SOURCE_TYPE, type SourceTypeValue } from '@/constants'
import type { CardEpisodeVO } from '@/types/card'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO, StoryVO } from '@/types/story'
import type { StoryCategoryVO } from '@/types/storyCategory'
import BrowseCenterCategory from './BrowseCenterCategory.vue'
import BrowseCenterStory from './BrowseCenterStory.vue'
import type { VersionSelectItem } from '../../composables/useVersionSelection.ts'

const props = defineProps<{
  /** 详情加载中 */
  loadingContent: boolean
  /** 当前剧情详情 */
  storyDetail?: StoryDetailVO | null
  /** 当前剧情详情 */
  cardDetail?: CardEpisodeVO | null
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
  /** 来源类型：1-RC 2-RTV 3-Rabitter 4-story */
  sourceType: SourceTypeValue
  /** 卡片名称 */
  cardName?: string
  /** 左侧目录是否展开 */
  showLeft?: boolean
  /** 右侧编辑区是否展开 */
  showRight?: boolean
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
  /** 切换左侧目录 */
  toggleLeft: []
  /** 切换右侧编辑区 */
  toggleRight: []
}>()

const currentVersionIdComputed = computed({
  get: () => props.currentVersionId,
  set: (val) => emit('update:currentVersionId', val),
})

const selectedVersionKeyComputed = computed({
  get: () => props.selectedVersionKey,
  set: (key) => emit('update:selectedVersionKey', key),
})

/**
 * 是否已经加载过数据。
 * 只要曾经有过数据，就不再用 skeleton 顶掉内容 ——
 * 否则 BrowseCenterStory 会被 v-if 卸载重挂，导致滚动位置丢失。
 */
const hasAnyData = computed(
  () => !!(props.storyDetail || props.cardDetail || props.currentCategory),
)
</script>
