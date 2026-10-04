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
      @go-back="goBack"
      @go-category="goCategory"
      @go-story="goStory"
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
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { sortDialogueLinesAPI } from '@/api/dialogueLine'
import { listTextDialogueVersionOptionsAPI } from '@/api/dialogueVersion.ts'
import { getUserStoryDetailAPI, listUserStoryCategoryTreeAPI, pageUserStoryAPI } from '@/api/story'
import { SOURCE_TYPE } from '@/constants/index'
import {
  STORY_CATEGORY_TYPE_LABEL,
  STORY_STATUS,
  type StoryCategoryTypeValue,
} from '@/constants/story'
import { useStoryCategoryTypeStore } from '@/stores/storyCategoryTypeStore'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO, DialogueVersionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO, StoryVO } from '@/types/story'
import type { StoryCategoryVO } from '@/types/storyCategory'
import BrowseCenter from './components/BrowseCenter.vue'
import BrowseLeft from './components/BrowseLeft.vue'
import BrowseRight from './components/BrowseRight.vue'

const route = useRoute()
const router = useRouter()
const storyCategoryTypeStore = useStoryCategoryTypeStore()

const type = computed(() => Number(route.params.type) as StoryCategoryTypeValue)
const kind = computed(() => (route.params.kind as string) ?? null)
const nodeId = computed(() => (route.params.id ? Number(route.params.id) : null))

const typeLabel = computed(
  () => STORY_CATEGORY_TYPE_LABEL[type.value as StoryCategoryTypeValue] ?? '剧情',
)

/* -------- 树 -------- */
const categoryTree = ref<StoryCategoryVO[]>([])
const expandedKeys = ref<number[]>([])

const currentHighlightId = computed(() => {
  if (kind.value === 'category') return nodeId.value
  return null
})

async function loadTree() {
  const res = await listUserStoryCategoryTreeAPI({ categoryType: type.value })
  categoryTree.value = res.data ?? []
  expandedKeys.value = collectIds(categoryTree.value).slice(0, 5)
}

function collectIds(list: StoryCategoryVO[]): number[] {
  const ids: number[] = []
  const walk = (arr: StoryCategoryVO[]) => {
    arr.forEach((n) => {
      if (n.id != null) ids.push(n.id)
      if (n.children?.length) walk(n.children)
    })
  }
  walk(list)
  return ids
}

function findCategory(list: StoryCategoryVO[], id: number): StoryCategoryVO | null {
  for (const n of list) {
    if (n.id === id) return n
    if (n.children?.length) {
      const found = findCategory(n.children, id)
      if (found) return found
    }
  }
  return null
}

/* -------- 当前节点 -------- */
const currentCategory = computed(() => {
  if (kind.value !== 'category' || nodeId.value == null) return null
  return findCategory(categoryTree.value, nodeId.value)
})

const storyDetail = ref<StoryDetailVO | null>(null)
const stories = ref<StoryVO[]>([])
const versionOptions = ref<DialogueVersionOptionVO[] | null>(null)

const loadingContent = ref(false)
const loadingStories = ref(false)

/* -------- 编辑相关状态 -------- */
const editingMode = ref(false)
const currentVersionId = ref<number | null>(null)
const editingLineId = ref<number | null>(null)

/** 新增句子时要插到哪一句后面，null 表示追加到末尾 */
const pendingInsertAfterId = ref<number | null>(null)

/** 当前选中的版本 */
const currentVersion = computed<DialogueVersionVO | null>(() => {
  if (!storyDetail.value?.versions?.length || currentVersionId.value == null) return null
  return storyDetail.value.versions.find((v) => v.id === currentVersionId.value) ?? null
})

/** 当前编辑的句子 */
const editingLine = computed<DialogueLineVO | null>(() => {
  if (!currentVersion.value?.lines?.length || editingLineId.value == null) return null
  return currentVersion.value.lines.find((l) => l.id === editingLineId.value) ?? null
})

/** 当前编辑的句子在版本中的下标 */
const editingLineIndex = computed(() => {
  if (!currentVersion.value?.lines?.length || editingLineId.value == null) return -1
  return currentVersion.value.lines.findIndex((l) => l.id === editingLineId.value)
})

/* -------- 加载当前视图 -------- */
async function loadCurrent() {
  storyDetail.value = null
  stories.value = []

  if (kind.value === 'story' && nodeId.value != null) {
    loadingContent.value = true
    try {
      const res = await getUserStoryDetailAPI(nodeId.value)
      storyDetail.value = res.data ?? null

      const versions = storyDetail.value?.versions ?? []
      // 保留已选版本；如果原版本不存在了，则回退到第一个
      if (versions.length) {
        const stillExists = versions.some((v) => v.id === currentVersionId.value)
        if (!stillExists) {
          currentVersionId.value = versions[0]!.id ?? null
        }
      } else {
        currentVersionId.value = null
      }

      // 如果当前编辑的行在新数据中仍存在，保留；否则清空
      const currentLines = currentVersion.value?.lines ?? []
      if (editingLineId.value != null && !currentLines.some((l) => l.id === editingLineId.value)) {
        editingLineId.value = null
      }

      // 下拉框版本选项
      initDialogueVersionOptions()

      // 新增后处理：如果记录了目标句，需要重排
      await applyPendingInsert()
    } catch {
      ElMessage.error('剧情加载失败')
    } finally {
      loadingContent.value = false
    }
    return
  }

  // 非 story 视图，清理编辑状态
  currentVersionId.value = null
  editingLineId.value = null

  if (kind.value === 'category' && nodeId.value != null) {
    loadingStories.value = true
    try {
      const res = await pageUserStoryAPI({
        categoryId: nodeId.value,
        status: STORY_STATUS.PUBLISHED,
        pageNum: 1,
        pageSize: 50,
      })
      stories.value = res.data.records ?? []
    } catch {
      ElMessage.error('剧情加载失败')
    } finally {
      loadingStories.value = false
    }
  }
}

async function initDialogueVersionOptions() {
  if (!storyDetail.value?.id) {
    ElMessage.error('当前故事不存在')
    return
  }
  const res = await listTextDialogueVersionOptionsAPI(SOURCE_TYPE.STORY, storyDetail.value?.id)
  versionOptions.value = res.data
}

/**
 * 如果存在待插入目标，则把刚追加到末尾的句子移动到目标句之后
 */
async function applyPendingInsert() {
  const targetId = pendingInsertAfterId.value
  pendingInsertAfterId.value = null

  if (targetId == null) return
  const version = currentVersion.value
  if (!version?.id || !version.lines?.length) return

  const lines = version.lines
  const newLine = lines[lines.length - 1]
  if (!newLine?.id) return

  const targetIndex = lines.findIndex((l) => l.id === targetId)
  // 目标句不存在，或新句本来就在末尾，无需重排
  if (targetIndex < 0 || targetIndex >= lines.length - 1) return

  // 重排：新句从末尾移到目标句之后
  const ordered = [...lines]
  const [moved] = ordered.splice(ordered.length - 1, 1)
  if (!moved) return
  ordered.splice(targetIndex + 1, 0, moved)

  try {
    await sortDialogueLinesAPI(version.id, {
      lineIds: ordered.map((l) => l.id!),
    })
    // 排序成功后本地同步顺序，避免再请求一次
    if (currentVersion.value) {
      currentVersion.value.lines = ordered
    }
  } catch {
    ElMessage.error('对话顺序调整失败')
  }
}

/* -------- 交互 -------- */
function handleNodeClick(data: StoryCategoryVO) {
  goCategory(data.id!)
}

function handleSelectLine(line: DialogueLineVO) {
  editingLineId.value = line.id ?? null
}

function handleSaveLine() {
  void loadCurrent()
}

/**
 * 处理新增：记录目标句，刷新后再按需重排
 */
async function handleAddLine(payload: { afterId: number | null }) {
  pendingInsertAfterId.value = payload.afterId
  await loadCurrent()
}

function goBack() {
  if (storyDetail.value) {
    router.push({
      name: 'UserStoryBrowseDetail',
      params: {
        type: String(type.value),
        kind: 'category',
        id: String(storyDetail.value.categoryId),
      },
    })
  } else {
    router.push({
      name: 'UserStoryBrowseDetail',
      params: {
        type: String(type.value),
        kind: 'category',
        id: String(currentCategory.value?.parentId),
      },
    })
  }
}

function goCategory(id: number) {
  router.push({
    name: 'UserStoryBrowseDetail',
    params: { type: String(type.value), kind: 'category', id: String(id) },
  })
}

function goStory(id: number) {
  router.push({
    name: 'UserStoryBrowseDetail',
    params: { type: String(type.value), kind: 'story', id: String(id) },
  })
}

function handleSwitchType(typeId: number) {
  router.push({ name: 'UserStoryBrowse', params: { type: String(typeId) } })
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
