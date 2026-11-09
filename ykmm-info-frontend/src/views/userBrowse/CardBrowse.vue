<template>
  <div
    :class="{ 'justify-center': !showEditPanel }"
    class="card-dialogue-editor flex h-full gap-4 overflow-hidden"
  >
    <!-- 左侧：话数列表 -->
    <CardEpisodeList
      v-show="showLeft"
      :card-info="cardInfo"
      :current-id="currentEpisodeId"
      :episodes="episodes"
      :source-label="sourceLabel"
      class="shrink-0 overflow-hidden"
      @back="handleBack"
      @select="handleSelectEpisode"
    />

    <!-- 中间：对话内容 -->
    <BrowseCenter
      v-model:current-version-id="currentVersionId"
      :can-edit="canEditCurrent"
      :card-detail="episodeDetail"
      :card-name="cardName"
      :class="showEditPanel ? 'flex-1 max-w-1/2' : 'flex-1 max-w-3/7'"
      :color="color"
      :current-category="null"
      :editing-line-id="editingLineId"
      :editing-mode="editingMode"
      :loading-content="loadingDetail"
      :loading-stories="false"
      :selected-version-key="selectedVersionKey"
      :show-left="showLeft"
      :show-right="showRight"
      :source-type="sourceType as SourceTypeValue"
      :stories="[]"
      :type-label="sourceLabel"
      :version-options="versionOptions"
      :version-select-items="versionSelectItems"
      class="h-full min-w-0 overflow-hidden"
      @go-back="handleBack"
      @go-category="() => {}"
      @go-story="() => {}"
      @review-success="reloadCurrentEpisode"
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
      :source-type="sourceType as SourceTypeValue"
      :story-detail="episodeDetail"
      class="h-full shrink-0 overflow-hidden"
      @add-line="handleAddLine"
      @clear-line="editingLineId = null"
      @create-version="handleCreateVersion"
      @delete-line="handleDeleteLine"
      @refresh="reloadCurrentEpisode"
      @save-line="handleSaveLine"
      @select-line="handleSelectLine"
    />
  </div>
</template>

<script setup lang="ts">
import { watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createDialogueVersionAPI } from '@/api/dialogueVersion'
import { type SourceTypeValue } from '@/constants'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import { useBrowseEditPermission } from '@/views/userBrowse/composables/useBrowseEditPermission.ts'
import { useBrowseLayout } from '@/views/userBrowse/composables/useBrowseLayout.ts'
import { useDialogueEdit } from '@/views/userBrowse/composables/useDialogueEdit.ts'
import { useVersionSelection } from '@/views/userBrowse/composables/useVersionSelection.ts'
import { useVersionSelectionSync } from '@/views/userBrowse/composables/useVersionSelectionSync.ts'
import BrowseCenter from './components/BrowseCenter/Index.vue'
import CardEpisodeList from './components/BrowseLeft/CardEpisodeList.vue'
import BrowseRight from './components/BrowseRight/Index.vue'
import { useCardEpisodeDetail } from './composables/useCardEpisodeDetail.ts'
import { useCardInfo } from './composables/useCardInfo.ts'
import { useEpisodeLoader } from './composables/useEpisodeLoader.ts'
import { useEpisodeRoute } from './composables/useEpisodeRoute.ts'

interface EpisodeItem {
  id?: number
  episodeNo?: number
  title?: string
}

/* -------- Props -------- */
const props = defineProps<{
  /** 来源类型：1-RC 2-RTV 3-Rabitter 4-story */
  sourceType: number
  /** 卡面ID */
  cardId: number
  /** 话数列表 */
  episodes: EpisodeItem[]
  /** 来源标签：RC / RTV / Rabitter */
  sourceLabel: string
}>()

const emit = defineEmits<{
  back: []
}>()

/* -------- ★ 卡面信息（composable） -------- */
const { color, cardInfo, cardName, loadCardInfo } = useCardInfo(() => props.cardId)

/* -------- ★ 当前话数 + URL 同步（composable） -------- */
const { currentEpisodeId, syncEpisodeToUrl } = useEpisodeRoute()

/* -------- 话数对话详情 -------- */
const {
  episodeDetail,
  loading: loadingDetail,
  versionOptions,
  reset: resetEpisodeDetail,
  fetchEpisodeDetail,
  fetchVersionOptions,
} = useCardEpisodeDetail()

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
  selectLine,
  applyPendingInsert,
} = useDialogueEdit(episodeDetail)

/* -------- 版本选中 -------- */
const {
  selectedVersionKey,
  versionSelectItems,
  versionItemMap,
  currentOptionVersion,
  selectByVersionId,
} = useVersionSelection(episodeDetail, versionOptions)

/* -------- ★ 权限 + 布局 + 版本同步 -------- */
const { canEditCurrent } = useBrowseEditPermission(currentOptionVersion)
const { showLeft, showRight, dataLoading, showEditPanel } = useBrowseLayout(canEditCurrent)
const { handleSelectedVersionKeyChange } = useVersionSelectionSync({
  currentVersionId,
  selectedVersionKey,
  versionItemMap,
  editingLineId,
  editingMode,
})

/* -------- ★ 话数加载（composable） -------- */
const { loadCurrentEpisode, reloadCurrentEpisode } = useEpisodeLoader({
  sourceType: () => props.sourceType,
  currentEpisodeId,
  episodeDetail,
  dataLoading,
  resetEpisodeDetail,
  fetchEpisodeDetail,
  fetchVersionOptions,
  syncVersionSelection,
  syncEditingLine,
  applyPendingInsert,
})

/* -------- 切换话数 -------- */
function handleSelectEpisode(id: number) {
  if (currentEpisodeId.value === id) return
  currentEpisodeId.value = id
  syncEpisodeToUrl(id)
}

function handleBack() {
  emit('back')
}

/* -------- 交互 -------- */
function handleSelectLine(line: DialogueLineVO) {
  selectLine(line)
}

async function handleAddLine(payload: { afterId: number | null; atStart: boolean }) {
  pendingInsertAfterId.value = payload.atStart ? null : payload.afterId
  await loadCurrentEpisode()

  const lines = currentVersion.value?.lines ?? []
  if (!lines.length) return

  const newLineId = payload.atStart
    ? (lines[0]?.id ?? null)
    : payload.afterId == null
      ? (lines[lines.length - 1]?.id ?? null)
      : (() => {
          const idx = lines.findIndex((l) => l.id === payload.afterId)
          return idx >= 0 && idx + 1 < lines.length ? (lines[idx + 1]?.id ?? null) : null
        })()

  if (newLineId != null) editingLineId.value = newLineId
}

async function handleDeleteLine(payload: {
  deletedId: number
  prevId: number | null
  nextId: number | null
}) {
  editingLineId.value = null
  await loadCurrentEpisode()

  const lines = currentVersion.value?.lines ?? []
  if (!lines.length) return

  const targetId =
    (payload.prevId != null && lines.some((l) => l.id === payload.prevId)
      ? payload.prevId
      : null) ??
    (payload.nextId != null && lines.some((l) => l.id === payload.nextId) ? payload.nextId : null)

  if (targetId != null) editingLineId.value = targetId
}

async function handleCreateVersion(payload: {
  option: DialogueVersionOptionVO
  contributorUserId: number | null
  contributorName: string | null
}) {
  if (currentEpisodeId.value == null) {
    ElMessage.error('请先选择话数')
    return
  }
  try {
    const res = await createDialogueVersionAPI({
      sourceType: props.sourceType,
      sourceId: currentEpisodeId.value,
      format: payload.option.format,
      language: payload.option.language,
      scope: payload.option.scope,
      contributorUserId: payload.contributorUserId ?? undefined,
      contributorName: payload.contributorName ?? undefined,
    })

    const newId = res?.data
    if (newId == null) {
      ElMessage.error('创建成功但未拿到版本 ID')
      await loadCurrentEpisode()
      return
    }
    ElMessage.success('创建成功')
    await loadCurrentEpisode()

    currentVersionId.value = newId
    selectByVersionId(newId)
  } catch (err) {
    console.error('创建版本失败', err)
    ElMessage.error('创建失败')
  }
}

function handleSaveLine() {
  void loadCurrentEpisode()
}

/* -------- 生命周期 -------- */

/**
 * episodes 列表变化时：
 * - 数据未加载完（空数组）：保留 currentEpisodeId（可能来自 URL）
 * - 当前选中不在列表里：回退到第一话，并同步 URL
 * - 否则保持
 */
watch(
  () => props.episodes,
  (list) => {
    if (!list.length) return
    const exists =
      currentEpisodeId.value != null && list.some((e) => e.id === currentEpisodeId.value)
    if (exists) return
    const firstId = list[0]?.id ?? null
    currentEpisodeId.value = firstId
    if (firstId != null) syncEpisodeToUrl(firstId)
  },
  { immediate: true, deep: false },
)

/**
 * 话数变化：清空旧数据 + 加载新话数。
 * immediate: true 保证 URL 初始值触发首次加载。
 */
watch(
  currentEpisodeId,
  () => {
    resetEpisodeDetail()
    void loadCurrentEpisode(true)
  },
  { immediate: true },
)

/** 来源类型切换：重置话数（episodes 由父组件重新加载） */
watch(
  () => props.sourceType,
  () => {
    currentEpisodeId.value = null
    syncEpisodeToUrl(null)
  },
)

/** 卡面切换：重新加载卡面信息 */
watch(
  () => props.cardId,
  () => {
    void loadCardInfo()
  },
)

void loadCardInfo()
</script>
