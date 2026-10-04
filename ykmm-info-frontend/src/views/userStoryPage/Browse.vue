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
      :selected-version-key="selectedVersionKey"
      :stories="stories"
      :story-detail="storyDetail"
      :type-label="typeLabel"
      :version-options="versionOptions"
      :version-select-items="versionSelectItems"
      @go-back="handleGoBack"
      @go-category="(id: number) => goCategory(type, id)"
      @go-story="(id: number) => goStory(type, id)"
      @select-line="handleSelectLine"
      @update:selected-version-key="handleSelectedVersionKeyChange"
    />

    <!-- 右侧：编辑区 -->
    <BrowseRight
      v-model:editing-mode="editingMode"
      :all-lines="currentVersion?.lines ?? []"
      :current-option-version="currentOptionVersion"
      :current-version="currentVersion"
      :editing-line="editingLine"
      :editing-line-index="editingLineIndex"
      :pending-insert-after-id="pendingInsertAfterId"
      :story-detail="storyDetail"
      @add-line="handleAddLine"
      @create-version="handleCreateVersion"
      @refresh="loadCurrent"
      @save-line="handleSaveLine"
      @select-line="handleSelectLine"
    />
  </div>
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createDialogueVersionAPI } from '@/api/dialogueVersion'
import { SOURCE_TYPE } from '@/constants/index'
import { useStoryCategoryTypeStore } from '@/stores/storyCategoryTypeStore'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import type { StoryCategoryVO } from '@/types/storyCategory'
import BrowseCenter from './components/BrowseCenter.vue'
import BrowseLeft from './components/BrowseLeft.vue'
import BrowseRight from './components/BrowseRight.vue'
import { useBrowseNavigation } from './composables/useBrowseNavigation'
import { useBrowseRoute } from './composables/useBrowseRoute'
import { useCategoryTree } from './composables/useCategoryTree'
import { useDialogueEdit } from './composables/useDialogueEdit'
import { useStoryDetail } from './composables/useStoryDetail'
import { useVersionSelection } from './composables/useVersionSelection'

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

/* -------- 版本选中 -------- */
const { selectedVersionKey, versionSelectItems, versionItemMap, currentOptionVersion } =
  useVersionSelection(storyDetail, versionOptions)

/**
 * 外部 currentVersionId 变化时同步本地 key。
 * 若本地已选中同一 option，跳过，避免来回覆盖造成循环。
 */
watch(
  currentVersionId,
  (id) => {
    const cur = versionItemMap.value.get(selectedVersionKey.value)
    if (cur && cur.versionId === (id ?? null)) return
    selectedVersionKey.value = id == null ? '' : `id:${id}`
  },
  { immediate: true },
)

/**
 * 用户在下拉里切换：更新 key，并把 versionId（可能为 null）同步给 currentVersionId。
 */
function handleSelectedVersionKeyChange(key: string) {
  selectedVersionKey.value = key
  currentVersionId.value = versionItemMap.value.get(key)?.versionId ?? null
}

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

/**
 * 为「选中但还没有内容」的版本创建内容。
 * 调用创建版本接口 → 重新拉详情 → 把新版本设为选中。
 */
async function handleCreateVersion(option: DialogueVersionOptionVO) {
  const storyId = storyDetail.value?.id
  if (!storyId) {
    ElMessage.error('当前故事不存在')
    return
  }

  try {
    const res = await createDialogueVersionAPI({
      sourceType: SOURCE_TYPE.STORY,
      sourceId: storyId,
      format: option.format,
      language: option.language,
      scope: option.scope,
    })
    ElMessage.success('创建成功')

    // 重新拉详情，让 storyDetail.versions 里有新版本
    await fetchStoryDetail(storyId)

    const newId = res.data ?? null
    if (newId != null) {
      currentVersionId.value = newId
      selectedVersionKey.value = `id:${newId}`
    }
  } catch {
    ElMessage.error('创建失败')
  }
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
