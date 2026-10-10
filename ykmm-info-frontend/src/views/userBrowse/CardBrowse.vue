<template>
  <div class="card-dialogue-editor flex h-full gap-4 overflow-hidden">
    <!-- 左侧：话数列表 -->
    <CardEpisodeList
      v-show="showLeft"
      :card-info="cardInfo"
      :current-id="currentEpisodeId"
      :episodes="episodes"
      :source-label="sourceLabel"
      @back="handleBack"
      @select="handleSelectEpisode"
    />

    <!-- 中间：对话内容 -->
    <BrowseCenter
      v-model:current-version-id="currentVersionId"
      :card-detail="episodeDetail"
      :card-name="cardName"
      :class="[
        'h-full flex-1 min-w-0 overflow-hidden max-w-1/2',
        { 'mx-auto': !showLeft && !showRight },
      ]"
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
      @go-back="handleBack"
      @go-category="() => {}"
      @go-story="() => {}"
      @select-line="handleSelectLine"
      @toggle-left="showLeft = !showLeft"
      @toggle-right="showRight = !showRight"
      @update:selected-version-key="handleSelectedVersionKeyChange"
    />

    <!-- 右侧：编辑区 -->
    <BrowseRight
      v-show="showRight"
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
      @refresh="loadCurrentEpisode"
      @save-line="handleSaveLine"
      @select-line="handleSelectLine"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getCardDetailAPI } from '@/api/card'
import { createDialogueVersionAPI } from '@/api/dialogueVersion'
import { type SourceTypeValue } from '@/constants'
import type { CardVO } from '@/types/card'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import { useDialogueEdit } from '@/views/userBrowse/composables/useDialogueEdit.ts'
import { useVersionSelection } from '@/views/userBrowse/composables/useVersionSelection.ts'
import BrowseCenter from './components/BrowseCenter/Index.vue'
import CardEpisodeList from './components/BrowseLeft/CardEpisodeList.vue'
import BrowseRight from './components/BrowseRight/Index.vue'
import { useCardEpisodeDetail } from './composables/useCardEpisodeDetail.ts'

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

const route = useRoute()
const router = useRouter()

/** 左侧话数列表是否展开 */
const showLeft = ref(true)
/** 右侧编辑区是否展开 */
const showRight = ref(true)

/* -------- 卡面信息 -------- */
const color = ref('blue')
const cardInfo = ref<CardVO>({})
const cardName = computed(() => cardInfo.value.name + '[' + cardInfo.value.seriesName + ']')

async function loadCardInfo() {
  if (!props.cardId) return
  try {
    const res = await getCardDetailAPI(props.cardId)
    cardInfo.value = res.data ?? {}
    const attrColorMap: Record<number, string> = { 1: 'red', 2: 'lime', 3: 'blue' }
    color.value = cardInfo.value.attribute
      ? (attrColorMap[cardInfo.value.attribute] ?? 'blue')
      : 'blue'
  } catch {}
}

/* -------- 当前话数 -------- */
/**
 * 从 URL 读取初始话数。
 * 若 query.sourceId 非法或不存在，返回 null。
 */
function resolveInitialEpisodeId(): number | null {
  const raw = route.query.sourceId
  if (typeof raw !== 'string' || !raw) return null
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? n : null
}

const currentEpisodeId = ref<number | null>(resolveInitialEpisodeId())

/**
 * 把当前话数写回 URL query。
 * 使用 replace 避免污染浏览器历史；保留其他 query 参数。
 */
function syncEpisodeToUrl(id: number | null) {
  const cur = route.query.sourceId
  const target = id == null ? undefined : String(id)
  if ((cur ?? undefined) === target) return

  const query = { ...route.query }
  if (target == null) {
    delete query.sourceId
  } else {
    query.sourceId = target
  }
  void router.replace({ query })
}

/* -------- 话数对话详情 -------- */
const {
  episodeDetail,
  loading: loadingDetail,
  versionOptions,
  reset: resetEpisodeDetail,
  fetchEpisodeDetail,
  fetchVersionOptions,
} = useCardEpisodeDetail()

/* -------- 编辑态（复用 Browse 的 composables） -------- */
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

/**
 * 外部 currentVersionId 变化时同步本地 key
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

function handleSelectedVersionKeyChange(key: string) {
  selectedVersionKey.value = key
  currentVersionId.value = versionItemMap.value.get(key)?.versionId ?? null
}

/* -------- 加载当前话数对话 -------- */
async function loadCurrentEpisode() {
  if (currentEpisodeId.value == null) {
    resetEpisodeDetail()
    return
  }

  // resetEpisodeDetail()
  await fetchEpisodeDetail(props.sourceType, currentEpisodeId.value)
  if (!episodeDetail.value) return

  syncVersionSelection()
  syncEditingLine()
  await fetchVersionOptions(props.sourceType, currentEpisodeId.value)
  await applyPendingInsert()
}

/** 切换话数：更新状态 + 写回 URL */
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

/**
 * 保存成功后的回调：
 * 1. 清空当前选中行，让编辑区回到"未选中"状态
 * 2. 重新加载当前话数对话
 */
function handleSaveLine() {
  // editingLineId.value = null
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
    void loadCurrentEpisode()
  },
  { immediate: true },
)

watch(
  () => props.sourceType,
  () => {
    currentEpisodeId.value = null
    syncEpisodeToUrl(null)
    // episodes 会由父组件重新加载，watch(episodes) 会自动选第一话
  },
)

watch(
  () => props.cardId,
  () => {
    void loadCardInfo()
  },
)

watch(currentVersionId, () => {
  editingLineId.value = null
})

watch(editingMode, (val) => {
  if (!val) editingLineId.value = null
})

void loadCardInfo()
</script>
