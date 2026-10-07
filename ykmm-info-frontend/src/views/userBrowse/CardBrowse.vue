<template>
  <div class="card-dialogue-editor flex h-full gap-4 overflow-hidden">
    <!-- 左侧：话数列表 -->
    <CardEpisodeList
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
      :color="color"
      :current-category="null"
      :editing-line-id="editingLineId"
      :editing-mode="editingMode"
      :loading-content="loadingDetail"
      :loading-stories="false"
      :selected-version-key="selectedVersionKey"
      :stories="[]"
      :story-detail="currentDetail"
      :type-label="sourceLabel"
      :version-options="versionOptions"
      :version-select-items="versionSelectItems"
      class="h-full flex-1 min-w-0 overflow-hidden"
      @go-back="handleBack"
      @go-category="() => {}"
      @go-story="() => {}"
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
      :story-detail="currentDetail"
      class="h-full shrink-0 overflow-hidden"
      @add-line="handleAddLine"
      @create-version="handleCreateVersion"
      @delete-line="handleDeleteLine"
      @refresh="loadCurrentEpisode"
      @save-line="loadCurrentEpisode"
      @select-line="handleSelectLine"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getCardDetailAPI } from '@/api/card'
import { createDialogueVersionAPI } from '@/api/dialogueVersion'
import type { CardVO } from '@/types/card'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'
import { useDialogueEdit } from '@/views/userBrowse/composables/useDialogueEdit.ts'
import { useVersionSelection } from '@/views/userBrowse/composables/useVersionSelection.ts'
import BrowseCenter from './components/BrowseCenter.vue'
import BrowseRight from './components/BrowseRight/Index.vue'
import CardEpisodeList from './components/CardEpisodeList.vue'

interface EpisodeItem {
  id?: number
  episodeNo?: number
  title?: string
}

/* -------- Props -------- */
const props = defineProps<{
  /** 来源类型：2-RTV 3-RC */
  sourceType: number
  /** 卡面ID */
  cardId: number
  /** 话数列表 */
  episodes: EpisodeItem[]
  /** 来源标签：RTV / RC */
  sourceLabel: string
}>()

const emit = defineEmits<{
  back: []
}>()

/* -------- 卡面信息 -------- */
const color = ref('blue')
const cardInfo = ref<CardVO>({})

async function loadCardInfo() {
  if (!props.cardId) return
  try {
    const res = await getCardDetailAPI(props.cardId)
    cardInfo.value = res.data ?? {}
    // 用卡面属性对应的颜色
    const attrColorMap: Record<number, string> = { 1: 'red', 2: 'lime', 3: 'blue' }
    color.value = cardInfo.value.attribute
      ? (attrColorMap[cardInfo.value.attribute] ?? 'blue')
      : 'blue'
  } catch {
    // ignore
  }
}

/* -------- 当前话数 -------- */
const currentEpisodeId = ref<number | null>(null)

/**
 * 当前话数的详情（含 versions）。
 * 后端需提供按 RC/RTV id 拉取含对话版本详情的接口。
 * 这里用 StoryDetailVO 的形状承接，字段一致。
 */
const currentDetail = ref<StoryDetailVO | null>(null)
const loadingDetail = ref(false)

async function loadCurrentEpisode() {
  if (currentEpisodeId.value == null) {
    currentDetail.value = null
    return
  }
  loadingDetail.value = true
  try {
    // ★ 替换为真实接口：按 sourceType + sourceId 查询对话详情
    // const res = await getDialogueSourceDetailAPI(props.sourceType, currentEpisodeId.value)
    // currentDetail.value = res.data ?? null
    currentDetail.value = null // 占位
  } catch {
    currentDetail.value = null
  } finally {
    loadingDetail.value = false
  }
}

function handleSelectEpisode(id: number) {
  if (currentEpisodeId.value === id) return
  currentEpisodeId.value = id
}

function handleBack() {
  emit('back')
}

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
} = useDialogueEdit(currentDetail)

/* -------- 版本选中 -------- */
const versionOptions = ref<DialogueVersionOptionVO[] | null>(null)

const {
  selectedVersionKey,
  versionSelectItems,
  versionItemMap,
  currentOptionVersion,
  selectByVersionId,
} = useVersionSelection(currentDetail, versionOptions)

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

/* -------- 加载当前视图 -------- */
async function loadCurrent() {
  currentDetail.value = null
  await loadCurrentEpisode()
  if (!currentDetail.value) return

  syncVersionSelection()
  syncEditingLine()
  // ★ 拉版本选项：listDialogueVersionOptionsAPI(props.sourceType, currentEpisodeId.value)
  versionOptions.value = null
  await applyPendingInsert()
}

watch(currentEpisodeId, () => {
  void loadCurrent()
})

/* -------- 交互 -------- */
function handleSelectLine(line: DialogueLineVO) {
  selectLine(line)
}

async function handleAddLine(payload: { afterId: number | null; atStart: boolean }) {
  pendingInsertAfterId.value = payload.atStart ? null : payload.afterId
  await loadCurrent()
  // 定位新行
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
  await loadCurrent()
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
      await loadCurrent()
      return
    }
    ElMessage.success('创建成功')
    await loadCurrent()
    currentVersionId.value = newId
    selectByVersionId(newId)
  } catch (err) {
    console.error('创建版本失败', err)
    ElMessage.error('创建失败')
  }
}

/* -------- 初始化 -------- */
watch(
  () => props.episodes,
  (list) => {
    // 默认选中第一话
    if (list.length && currentEpisodeId.value == null) {
      currentEpisodeId.value = list[0]!.id ?? null
    }
  },
  { immediate: true },
)

void loadCardInfo()
</script>
