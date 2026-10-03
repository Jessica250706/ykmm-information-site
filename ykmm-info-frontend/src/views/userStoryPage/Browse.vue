<template>
  <div class="story-browse flex h-full gap-4">
    <!-- 左侧：分类树 -->
    <BrowseLeft
      :category-tree="categoryTree"
      :expanded-keys="expandedKeys"
      :type-label="typeLabel"
      :types="storyCategoryTypeStore.types"
      @node-click="handleNodeClick"
      @switch-type="handleSwitchType"
    />

    <!-- 中间：内容 -->
    <BrowseCenter
      :color="
        storyCategoryTypeStore.getTypeColor(
          currentCategory ? currentCategory?.categoryType : storyDetail?.categoryType,
        )
      "
      :current-category="currentCategory"
      :loading-content="loadingContent"
      :loading-stories="loadingStories"
      :stories="stories"
      :story-detail="storyDetail"
      :type-label="typeLabel"
      @go-back="goBack"
      @go-category="goCategory"
      @go-story="goStory"
    />

    <!-- 右侧：编辑区（占位） -->
    <el-card class="w-80 shrink-0 border-l bg-white p-4">
      <div class="text-xs text-slate-400">编辑区（开发中）</div>
    </el-card>
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
import type { StoryCategoryVO } from '@/types/storyCategory'
import BrowseCenter from './components/BrowseCenter.vue'
import BrowseLeft from './components/BrowseLeft.vue'

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

/* -------- 加载当前视图 -------- */
async function loadCurrent() {
  storyDetail.value = null
  stories.value = []

  if (kind.value === 'story' && nodeId.value != null) {
    loadingContent.value = true
    try {
      const res = await getUserStoryDetailAPI(nodeId.value)
      storyDetail.value = res.data ?? null
    } catch {
      ElMessage.error('剧情加载失败')
    } finally {
      loadingContent.value = false
    }
    return
  }

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

function goBack() {
  router.back()
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
</script>
