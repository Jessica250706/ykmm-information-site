<template>
  <div class="story-browse flex h-full">
    <!-- 左侧：分类树 -->
    <aside class="flex w-64 shrink-0 flex-col border-r bg-white">
      <div class="flex items-center justify-between border-b px-4 py-3">
        <div>
          <div class="text-xs text-slate-400">剧情类型</div>
          <div class="font-medium">{{ typeLabel }}</div>
        </div>
        <el-button link @click="goIndex">切换</el-button>
      </div>

      <div class="flex-1 overflow-auto p-2">
        <el-tree
          ref="treeRef"
          :data="categoryTree"
          :default-expanded-keys="expandedKeys"
          :expand-on-click-node="false"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          highlight-current
          @node-click="handleNodeClick"
        >
          <template #default="{ data }">
            <span class="flex items-center gap-2">
              <span>{{ data.name }}</span>
              <el-tag v-if="data.children?.length" effect="plain" size="small" type="info">
                {{ data.children.length }}
              </el-tag>
            </span>
          </template>
        </el-tree>
      </div>
    </aside>

    <!-- 中间：内容 -->
    <main class="min-w-0 flex-1 overflow-auto bg-slate-50 p-5">
      <el-skeleton v-if="loadingContent" :rows="6" animated />

      <!-- Story 详情 -->
      <DialogueView v-else-if="storyDetail" :detail="storyDetail" />

      <!-- 分类：子分类 + 剧情列表 -->
      <template v-else-if="currentCategory">
        <div class="mb-4">
          <h2 class="text-lg font-semibold">{{ currentCategory.name }}</h2>
          <div class="mt-1 flex items-center gap-2 text-xs text-slate-400">
            <el-tag effect="plain" size="small">
              {{ currentCategory.categoryTypeLabel ?? typeLabel }}
            </el-tag>
            <span>ID #{{ currentCategory.id }}</span>
          </div>
        </div>

        <!-- 子分类 -->
        <section v-if="currentCategory.children?.length" class="mb-6">
          <h3 class="mb-2 text-sm font-medium text-slate-500">子分类</h3>
          <div class="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-4">
            <div
              v-for="child in currentCategory.children"
              :key="child.id"
              class="cursor-pointer rounded-lg border bg-white p-3 transition hover:border-indigo-300 hover:shadow-sm"
              @click="goCategory(child.id!)"
            >
              <div class="font-medium">{{ child.name }}</div>
              <div v-if="child.children?.length" class="mt-0.5 text-xs text-slate-400">
                {{ child.children.length }} 个子项
              </div>
            </div>
          </div>
        </section>

        <!-- 剧情列表 -->
        <section>
          <h3 class="mb-2 text-sm font-medium text-slate-500">剧情列表</h3>
          <div v-loading="loadingStories">
            <el-empty v-if="!stories.length" description="该分类下暂无剧情" />
            <div v-else class="space-y-2">
              <div
                v-for="s in stories"
                :key="s.id"
                class="cursor-pointer rounded-lg border bg-white p-3 transition hover:border-indigo-300 hover:shadow-sm"
                @click="goStory(s.id!)"
              >
                <div class="font-medium">{{ s.title || `剧情 #${s.id}` }}</div>
                <div class="mt-0.5 line-clamp-1 text-sm text-slate-500">
                  {{ s.description || '暂无描述' }}
                </div>
              </div>
            </div>
          </div>
        </section>
      </template>

      <!-- 未选节点 -->
      <el-empty v-else description="请从左侧选择一个分类" />
    </main>

    <!-- 右侧：编辑区（占位） -->
    <aside class="w-80 shrink-0 border-l bg-white p-4">
      <div class="text-xs text-slate-400">编辑区（开发中）</div>
    </aside>
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
import type { StoryDetailVO, StoryVO } from '@/types/story'
import type { StoryCategoryVO } from '@/types/storyCategory'
import DialogueView from './components/DialogueView.vue'

const route = useRoute()
const router = useRouter()

const type = computed(() => Number(route.params.type) as StoryCategoryTypeValue)
const kind = computed(() => (route.params.kind as string) ?? null)
const nodeId = computed(() => (route.params.id ? Number(route.params.id) : null))

const typeLabel = computed(
  () => STORY_CATEGORY_TYPE_LABEL[type.value as StoryCategoryTypeValue] ?? '剧情',
)

/* -------- 树 -------- */
const treeRef = ref()
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

function goIndex() {
  router.push({ name: 'UserStoryIndex' })
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
