<template>
  <div class="story-browse flex h-full gap-4">
    <!-- 左侧：分类树 -->
    <BrowseLeft
      :category-tree="categoryTree"
      :current-id="currentHighlightId"
      :expanded-keys="expandedKeys"
      :type-label="typeLabel"
      :types="storyCategoryTypeStore.types"
      @node-click="handleNodeClick"
      @switch-type="handleSwitchType"
    />

    <!-- 中间：内容 -->
    <BrowseCenter
      v-model:current-version-id="currentVersionId"
      :color="
        storyCategoryTypeStore.getTypeColor(
          currentCategory ? currentCategory?.categoryType : storyDetail?.categoryType,
        )
      "
      :current-category="currentCategory"
      :editing-line-id="editingLineId"
      :editing-mode="editingMode"
      :loading-content="loadingContent"
      :loading-stories="loadingStories"
      :stories="stories"
      :story-detail="storyDetail"
      :type-label="typeLabel"
      :version-options="versionOptions"
      @go-back="handleGoBack"
      @go-category="(id: number) => goCategory(type, id)"
      @go-story="(id: number) => goStory(type, id)"
      @select-line="handleSelectLine"
    />

    <!-- 右侧：编辑区 -->
    <BrowseRight
      v-model:editing-mode="editingMode"
      :all-lines="currentVersion?.lines ?? []"
      :current-version="currentVersion"
      :editing-line="editingLine"
      :editing-line-index="editingLineIndex"
      :pending-insert-after-id="pendingInsertAfterId"
      :story-detail="storyDetail"
      @add-line="handleAddLine"
      @refresh="loadCurrent"
      @save-line="handleSaveLine"
      @select-line="handleSelectLine"
    />
  </div>
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { useStoryCategoryTypeStore } from '@/stores/storyCategoryTypeStore'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { StoryCategoryVO } from '@/types/storyCategory'
import BrowseCenter from './components/BrowseCenter.vue'
import BrowseLeft from './components/BrowseLeft.vue'
import BrowseRight from './components/BrowseRight.vue'
import { useBrowseNavigation } from './composables/useBrowseNavigation'
import { useBrowseRoute } from './composables/useBrowseRoute'
import { useCategoryTree } from './composables/useCategoryTree'
import { useDialogueEdit } from './composables/useDialogueEdit'
import { useStoryDetail } from './composables/useStoryDetail'

const storyCategoryTypeStore = useStoryCategoryTypeStore()

/* -------- 路由 -------- */
const { type, kind, nodeId, typeLabel } = useBrowseRoute()

/* -------- 分类树 -------- */
const { categoryTree, expandedKeys, currentHighlightId, currentCategory, loadTree } =
  useCategoryTree(type, kind, nodeId)

/* -------- 详情 / 列表 -------- */
const {
  storyDetail,
  stories,
  versionOptions,
  loadingContent,
  loadingStories,
  resetStory,
  fetchStoryDetail,
  fetchStories,
  fetchVersionOptions,
} = useStoryDetail()

/* -------- 编辑态 -------- */
const {
  editingMode,
  currentVersionId,
  editingLineId,
  pendingInsertAfterId,
  currentVersion,
  editingLine,
  editingLineIndex,
  syncVersionSelection,
  syncEditingLine,
  resetEditState,
  selectLine,
  applyPendingInsert,
} = useDialogueEdit(storyDetail)

/* -------- 导航 -------- */
const { goCategory, goStory, goBack, handleSwitchType } = useBrowseNavigation()

/* -------- 加载当前视图 -------- */
async function loadCurrent() {
  resetStory()

  if (kind.value === 'story' && nodeId.value != null) {
    await fetchStoryDetail(nodeId.value)
    if (!storyDetail.value) return

    syncVersionSelection()
    syncEditingLine()
    await fetchVersionOptions()
    await applyPendingInsert()
    return
  }

  // 非 story 视图，清理编辑状态
  resetEditState()

  if (kind.value === 'category' && nodeId.value != null) {
    await fetchStories(nodeId.value)
  }
}

/* -------- 交互 -------- */
function handleNodeClick(data: StoryCategoryVO) {
  goCategory(type.value, data.id!)
}

function handleSelectLine(line: DialogueLineVO) {
  selectLine(line)
}

function handleSaveLine() {
  void loadCurrent()
}

async function handleAddLine(payload: { afterId: number | null }) {
  pendingInsertAfterId.value = payload.afterId
  await loadCurrent()
}

function handleGoBack() {
  if (storyDetail.value && storyDetail.value.categoryId) {
    goBack(type.value, storyDetail.value.categoryId)
  } else if (currentCategory.value?.parentId != null) {
    goBack(type.value, currentCategory.value.parentId)
  }
}

/* -------- 生命周期 -------- */
watch(
  () => type.value,
  async () => {
    await loadTree()
    await loadCurrent()
  },
  { immediate: true },
)

watch(
  () => [kind.value, nodeId.value],
  () => {
    void loadCurrent()
  },
)

/** 版本切换时，清空编辑高亮 */
watch(currentVersionId, () => {
  editingLineId.value = null
})

/** 退出编辑模式时，清空高亮 */
watch(editingMode, (val) => {
  if (!val) editingLineId.value = null
})
</script>
