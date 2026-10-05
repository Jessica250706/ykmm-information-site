<template>
  <div class="story-browse flex h-full gap-4 overflow-hidden">
    <!-- 左侧：分类树 -->
    <BrowseLeft
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
      class="h-full flex-1 min-w-0 overflow-hidden"
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
      class="h-full shrink-0 overflow-hidden"
      @add-line="handleAddLine"
      @create-version="handleCreateVersion"
      @delete-line="handleDeleteLine"
      @refresh="loadCurrent"
      @save-line="handleSaveLine"
      @select-line="handleSelectLine"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createDialogueVersionAPI } from '@/api/dialogueVersion'
import { SOURCE_TYPE } from '@/constants/index'
import { useStoryCategoryTypeStore } from '@/stores/storyCategoryTypeStore'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import type { StoryCategoryVO } from '@/types/storyCategory'
import BrowseCenter from './components/BrowseCenter.vue'
import BrowseLeft from './components/BrowseLeft.vue'
import BrowseRight from './components/BrowseRight/Index.vue'
import { useBrowseNavigation } from './composables/useBrowseNavigation'
import { useBrowseRoute } from './composables/useBrowseRoute'
import { useCategoryTree } from './composables/useCategoryTree'
import { useDialogueEdit } from './composables/useDialogueEdit'
import { useStoryDetail } from './composables/useStoryDetail'
import { useVersionSelection } from './composables/useVersionSelection'

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

/**
 * 处理新增：
 * 1. 记录目标插入位置
 * 2. 刷新数据（applyPendingInsert 会在需要时调 sort）
 * 3. 刷新完成后自动选中新行
 */
async function handleAddLine(payload: { afterId: number | null; atStart: boolean }) {
  // 1. 记录插入目标
  pendingInsertAtStart.value = payload.atStart
  pendingInsertAfterId.value = payload.atStart ? null : payload.afterId

  // 2. 刷新（applyPendingInsert 会按需重排）
  await loadCurrent()

  // 3. 定位新行
  const lines = currentVersion.value?.lines ?? []
  if (lines.length === 0) return

  let newLineId: number | null = null
  if (payload.atStart) {
    newLineId = lines[0]?.id ?? null // 重排后新行在第一行
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

    const newId = res?.data

    if (newId == null) {
      console.error('创建版本返回结果异常：', res)
      ElMessage.error('创建成功但未拿到版本 ID，请刷新页面查看')
      await loadCurrent()
      return
    }

    ElMessage.success('创建成功')

    // 1. 关键：先刷新 storyDetail，让 versions 里有新版本
    await fetchStoryDetail(storyId)

    // 2. versionOptions 能刷新更好，刷不了也不影响（第二遍会补上）
    await fetchVersionOptions().catch(() => {})

    // 3. 数据源就绪后，再切选中
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
  // 先清掉当前选中，避免刷新期间引用已删除行
  editingLineId.value = null

  await loadCurrent()

  // 优先选中上一条，其次下一条
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
