<template>
  <div
    :class="{ 'justify-center': !showEditPanel }"
    class="story-browse flex h-full gap-4 overflow-hidden"
  >
    <!-- 左侧：分类树 -->
    <BrowseLeft
      v-show="showLeft"
      :category-tree="categoryTree"
      :current-id="currentHighlightId"
      :expanded-keys="expandedKeys"
      :type-label="typeLabel"
      :types="storyCategoryTypeStore.types"
      class="shrink-0 overflow-hidden"
      @node-click="handleNodeClick"
      @switch-type="handleSwitchType"
    />

    <!-- 中间：内容 -->
    <BrowseCenter
      v-model:current-version-id="currentVersionId"
      :can-edit="effectiveCanEdit"
      :class="showEditPanel ? 'flex-1 max-w-1/2' : 'flex-1 max-w-3xl'"
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
      :show-left="showLeft"
      :show-right="showRight"
      :source-type="SOURCE_TYPE.STORY"
      :stories="stories"
      :story-detail="storyDetail"
      :type-label="typeLabel"
      :version-options="versionOptions"
      :version-select-items="versionSelectItems"
      class="h-full min-w-0 overflow-hidden"
      @go-back="handleGoBack"
      @go-category="(id: number) => goCategory(type, id)"
      @go-story="(id: number) => goStory(type, id)"
      @review-success="reloadCurrent"
      @select-line="handleSelectLine"
      @toggle-left="showLeft = !showLeft"
      @toggle-right="showRight = !showRight"
      @update:selected-version-key="handleSelectedVersionKeyChange"
    />

    <!-- 右侧：编辑区（仅登录用户可见） -->
    <BrowseRight
      v-if="showEditPanel"
      v-model:editing-mode="editingMode"
      :all-lines="currentVersion?.lines ?? []"
      :current-option-version="currentOptionVersion"
      :current-version="currentVersion"
      :editing-line="editingLine"
      :editing-line-index="editingLineIndex"
      :pending-insert-after-id="pendingInsertAfterId"
      :source-type="SOURCE_TYPE.STORY"
      :story-detail="storyDetail"
      class="h-full shrink-0 overflow-hidden"
      @add-line="handleAddLine"
      @create-version="handleCreateVersion"
      @delete-line="handleDeleteLine"
      @refresh="reloadCurrent"
      @save-line="handleSaveLine"
      @select-line="handleSelectLine"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createDialogueVersionAPI } from '@/api/dialogueVersion'
import { SOURCE_TYPE } from '@/constants'
import { useStoryCategoryTypeStore } from '@/stores'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import type { StoryCategoryVO } from '@/types/storyCategory'
import BrowseCenter from './components/BrowseCenter/Index.vue'
import BrowseLeft from './components/BrowseLeft/BrowseLeft.vue'
import BrowseRight from './components/BrowseRight/Index.vue'
import { useBrowseEditPermission } from './composables/useBrowseEditPermission.ts'
import { useBrowseLayout } from './composables/useBrowseLayout.ts'
import { useBrowseNavigation } from './composables/useBrowseNavigation.ts'
import { useBrowseRoute } from './composables/useBrowseRoute.ts'
import { useCategoryTree } from './composables/useCategoryTree.ts'
import { useDialogueEdit } from './composables/useDialogueEdit.ts'
import { useStoryDetail } from './composables/useStoryDetail.ts'
import { useStoryLoader } from './composables/useStoryLoader.ts'
import { useVersionSelection } from './composables/useVersionSelection.ts'
import { useVersionSelectionSync } from './composables/useVersionSelectionSync.ts'

const storyCategoryTypeStore = useStoryCategoryTypeStore()

/* -------- 路由 -------- */
const { type, kind, nodeId, typeLabel } = useBrowseRoute()

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

/** story 模式下左侧树要高亮的分类 id：取 storyDetail.categoryId */
const storyCategoryId = computed(() => storyDetail.value?.categoryId ?? null)

/* -------- 分类树 -------- */
const { categoryTree, expandedKeys, currentHighlightId, currentCategory, loadTree } =
  useCategoryTree(type, kind, nodeId, storyCategoryId)

/* -------- 编辑态 -------- */
const {
  editingMode,
  currentVersionId,
  editingLineId,
  pendingInsertAfterId,
  pendingInsertAtStart,
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
const {
  selectedVersionKey,
  versionSelectItems,
  versionItemMap,
  currentOptionVersion,
  selectByVersionId,
} = useVersionSelection(storyDetail, versionOptions)

/* -------- 数据加载 gate（从 useBrowseLayout 移到组件） -------- */
const dataLoading = ref(false)

/* -------- ★ 权限 + 布局 + 版本同步 -------- */
const { effectiveCanEdit } = useBrowseEditPermission(currentOptionVersion, dataLoading)
const { showLeft, showRight, showEditPanel } = useBrowseLayout(effectiveCanEdit)
const { handleSelectedVersionKeyChange } = useVersionSelectionSync({
  currentVersionId,
  selectedVersionKey,
  versionItemMap,
  editingLineId,
  editingMode,
})

/* -------- 当前视图加载（composable） -------- */
const { loadCurrent, reloadCurrent } = useStoryLoader({
  type,
  kind,
  nodeId,
  dataLoading,
  storyDetail,
  resetStory,
  fetchStoryDetail,
  fetchStories,
  fetchVersionOptions,
  loadTree,
  resetEditState,
  syncVersionSelection,
  syncEditingLine,
  applyPendingInsert,
})

/* -------- 导航 -------- */
const { goCategory, goStory, goBack, handleSwitchType } = useBrowseNavigation()

/* -------- 交互 -------- */
function handleNodeClick(data: StoryCategoryVO) {
  goCategory(type.value, data.id!)
}

function handleSelectLine(line: DialogueLineVO) {
  selectLine(line)
}

/**
 * 保存成功后的回调
 */
function handleSaveLine() {
  void loadCurrent()
}

/**
 * 处理新增：
 * 1. 记录目标插入位置
 * 2. 刷新数据（applyPendingInsert 会在需要时调 sort）
 * 3. 刷新完成后自动选中新行
 */
async function handleAddLine(payload: { afterId: number | null; atStart: boolean }) {
  pendingInsertAtStart.value = payload.atStart
  pendingInsertAfterId.value = payload.atStart ? null : payload.afterId

  await loadCurrent()

  const lines = currentVersion.value?.lines ?? []
  if (lines.length === 0) return

  let newLineId: number | null = null
  if (payload.atStart) {
    newLineId = lines[0]?.id ?? null
  } else if (payload.afterId == null) {
    newLineId = lines[lines.length - 1]?.id ?? null
  } else {
    const idx = lines.findIndex((l) => l.id === payload.afterId)
    if (idx >= 0 && idx + 1 < lines.length) {
      newLineId = lines[idx + 1]?.id ?? null
    }
  }

  if (newLineId != null) {
    editingLineId.value = newLineId
  }
}

/**
 * 为「选中但还没有内容」的版本创建内容。
 */
async function handleCreateVersion(payload: {
  option: DialogueVersionOptionVO
  contributorUserId: number | null
  contributorName: string | null
}) {
  const storyId = storyDetail.value?.id
  if (!storyId) {
    ElMessage.error('当前故事不存在')
    return
  }

  try {
    const res = await createDialogueVersionAPI({
      sourceType: SOURCE_TYPE.STORY,
      sourceId: storyId,
      format: payload.option.format,
      language: payload.option.language,
      scope: payload.option.scope,
      contributorUserId: payload.contributorUserId ?? undefined,
      contributorName: payload.contributorName ?? undefined,
    })

    const newId = res?.data

    if (newId == null) {
      console.error('创建版本返回结果异常：', res)
      ElMessage.error('创建成功但未拿到版本 ID，请刷新页面查看')
      await loadCurrent()
      return
    }

    ElMessage.success('创建成功')

    await fetchStoryDetail(storyId)
    await fetchVersionOptions().catch(() => {})

    currentVersionId.value = newId
    selectByVersionId(newId)
  } catch (err) {
    console.error('创建版本失败：', err)
    ElMessage.error('创建失败')
  }
}

async function handleDeleteLine(payload: {
  deletedId: number
  prevId: number | null
  nextId: number | null
}) {
  editingLineId.value = null

  await loadCurrent()

  const lines = currentVersion.value?.lines ?? []
  if (lines.length === 0) return

  const targetId =
    (payload.prevId != null && lines.some((l) => l.id === payload.prevId)
      ? payload.prevId
      : null) ??
    (payload.nextId != null && lines.some((l) => l.id === payload.nextId) ? payload.nextId : null)

  if (targetId != null) {
    editingLineId.value = targetId
  }
}

function handleGoBack() {
  if (storyDetail.value && storyDetail.value.categoryId) {
    goBack(type.value, storyDetail.value.categoryId)
  } else if (currentCategory.value?.parentId != null) {
    goBack(type.value, currentCategory.value.parentId)
  }
}
</script>
