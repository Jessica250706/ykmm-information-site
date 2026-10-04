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
import { getUserStoryDetailAPI, listUserStoryCategoryTreeAPI, pageUserStoryAPI } from '@/api/story'
import {
  STORY_CATEGORY_TYPE_LABEL,
  STORY_STATUS,
  type StoryCategoryTypeValue,
} from '@/constants/story'
import { useStoryCategoryTypeStore } from '@/stores/storyCategoryTypeStore'
import type { StoryDetailVO, StoryVO } from '@/types/story'
import type { DialogueLineVO, DialogueVersionVO } from '@/types/story'
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

const loadingContent = ref(false)
const loadingStories = ref(false)

/* -------- 编辑相关状态 -------- */
const editingMode = ref(false)
const currentVersionId = ref<number | null>(null)
const editingLineId = ref<number | null>(null)

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

function handleAddLine(_payload: { afterId: number | null }) {
  void loadCurrent()
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
