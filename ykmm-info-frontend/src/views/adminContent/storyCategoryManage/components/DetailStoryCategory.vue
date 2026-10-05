<template>
  <div class="story-category-detail flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">剧情分类详情</span>
          <div>
            <el-button :disabled="!detail" type="primary" @click="handleEditCurrent">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button v-if="isLeafCategory" type="primary" @click="handleCreateStory()">
              <el-icon><Plus /></el-icon>
              新增剧情
            </el-button>
            <el-button type="primary" @click="handleCreate()">
              <el-icon><Plus /></el-icon>
              新增子分类
            </el-button>
            <el-button @click="handleBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="4" animated />

      <template v-else-if="detail">
        <!-- 基本信息 -->
        <el-descriptions :column="2" class="mb-4" label-width="80px" border>
          <el-descriptions-item label="分类名">{{ detail.name ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="分类类型">
            <el-tag :type="categoryTypeTag(detail.categoryType)" effect="plain">
              {{ detail.categoryTypeLabel ?? '未知' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="父分类">
            {{ detail.parentName ?? '顶级分类' }}
          </el-descriptions-item>
          <el-descriptions-item label="排序">{{ detail.sort ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ detail.createdAt ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ detail.updatedAt ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="简介">
            <span class="whitespace-pre-line">{{ detail.description ?? '-' }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 叶子分类：剧情列表 -->
        <ProTable
          v-if="isLeafCategory"
          ref="tableRef"
          :columns="columns"
          :request="fetchTableData"
          row-key="id"
        >
          <template #storyTitle="{ row }">
            <span class="font-medium">{{ row.title || `剧情 #${row.id}` }}</span>
          </template>

          <template #storyDescription="{ row }">
            <span class="text-slate-500 line-clamp-1">{{ row.description ?? '-' }}</span>
          </template>

          <template #storyStatus="{ row }">
            <el-tag :type="storyStatusTag(row.status)" effect="plain">
              {{ row.statusLabel ?? '未知' }}
            </el-tag>
          </template>

          <template #storyAction="{ row }">
            <el-button size="small" type="primary" link @click="handleStoryEdit(row)">
              编辑
            </el-button>
            <el-button size="small" type="primary" link @click="handleStoryView(row)">
              查看对话
            </el-button>
            <el-button size="small" type="danger" link @click="handleStoryDelete(row)">
              删除
            </el-button>
          </template>
        </ProTable>

        <!-- 非叶子分类：子分类表格 -->
        <ProTable
          v-else
          ref="tableRef"
          :columns="columns"
          :request="fetchTableData"
          :tree-props="{ children: 'children' }"
          row-key="id"
          default-expand-all
        >
          <template #name="{ row }">
            <span class="font-medium">{{ row.name }}</span>
          </template>

          <template #categoryType="{ row }">
            <el-tag :type="categoryTypeTag(row.categoryType)" effect="plain">
              {{ row.categoryTypeLabel ?? '未知' }}
            </el-tag>
          </template>

          <template #categoryAction="{ row }">
            <el-button size="small" type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button size="small" type="primary" link @click="handleCreate(row)">
              新增子分类
            </el-button>
            <el-button
              v-if="row.children?.length === 0"
              size="small"
              type="primary"
              link
              @click="handleCreateStory(row)"
            >
              新增剧情
            </el-button>
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </ProTable>
      </template>

      <el-empty v-else description="分类不存在" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Edit, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { deleteStoryAPI, listStoryByCategoryAPI } from '@/api/story'
import {
  deleteStoryCategoryAPI,
  getStoryCategoryDetailTreeAPI,
  pageStoryCategoryTreeAPI,
} from '@/api/storyCategory'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import { STORY_CATEGORY_TYPE, STORY_STATUS } from '@/constants/story'
import type { StoryVO } from '@/types/story'
import type { StoryCategoryPageQueryDTO, StoryCategoryVO } from '@/types/storyCategory'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<StoryCategoryVO | null>(null)

const detailId = computed(() => (route.params.id ? Number(route.params.id) : null))

/** 当前分类是否为叶子节点：没有子分类时，展示剧情列表 */
const isLeafCategory = computed(() => (detail.value?.children?.length ?? -1) === 0)

type TableInstance = ProTableExpose<StoryCategoryVO | StoryVO>
const tableRef = ref<TableInstance>()

/* -------- 列配置：分类表 -------- */
const categoryColumns: ProTableColumn<StoryCategoryVO>[] = [
  { prop: 'name', label: '分类名', minWidth: 240, slot: 'name' },
  { prop: 'categoryType', label: '分类类型', width: 140, align: 'center', slot: 'categoryType' },
  { prop: 'sort', label: '排序', width: 100, align: 'center' },
  {
    prop: 'createdAt',
    label: '创建时间',
    minWidth: 180,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', minWidth: 300, align: 'center', fixed: 'right', slot: 'categoryAction' },
]

/* -------- 列配置：剧情表 -------- */
const storyColumns: ProTableColumn<StoryVO>[] = [
  { prop: 'title', label: '标题', minWidth: 240, slot: 'storyTitle' },
  { prop: 'description', label: '描述', minWidth: 240, slot: 'storyDescription' },
  { prop: 'status', label: '审核状态', width: 120, align: 'center', slot: 'storyStatus' },
  { prop: 'sort', label: '排序', width: 100, align: 'center' },
  {
    prop: 'createdAt',
    label: '创建时间',
    minWidth: 180,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', minWidth: 220, align: 'center', fixed: 'right', slot: 'storyAction' },
]

/** 根据当前分类是否是叶子切换列配置 */
const columns = computed<ProTableColumn<StoryCategoryVO | StoryVO>[]>(() =>
  isLeafCategory.value ? storyColumns : categoryColumns,
)

/* -------- tag 类型 -------- */
function categoryTypeTag(type?: number): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  switch (type) {
    case STORY_CATEGORY_TYPE.MAIN:
      return 'danger'
    case STORY_CATEGORY_TYPE.RAINBOW_CITY:
      return 'warning'
    case STORY_CATEGORY_TYPE.SPECIAL:
      return 'success'
    case STORY_CATEGORY_TYPE.ACTIVITY:
      return 'primary'
    case STORY_CATEGORY_TYPE.DRAMA:
      return 'info'
    default:
      return 'info'
  }
}

function storyStatusTag(status?: number): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  switch (status) {
    case STORY_STATUS.PUBLISHED:
      return 'success'
    case STORY_STATUS.PENDING:
      return 'warning'
    case STORY_STATUS.REJECTED:
      return 'danger'
    default:
      return 'info'
  }
}

/* -------- 详情 -------- */
async function loadDetail() {
  if (!detailId.value) return
  loading.value = true
  try {
    const res = await getStoryCategoryDetailTreeAPI(detailId.value)
    detail.value = res.data ?? null
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

/* -------- 表格数据源：根据类型走不同接口 -------- */
async function fetchTableData(params: {
  pageNum: number
  pageSize: number
  [k: string]: unknown
}): Promise<PageResult<StoryCategoryVO | StoryVO>> {
  if (!detailId.value) {
    return { records: [], total: 0 }
  }

  // 叶子分类：查剧情
  if (isLeafCategory.value) {
    const res = await listStoryByCategoryAPI(detailId.value)
    const records = res.data ?? []
    return { records, total: records.length }
  }

  // 非叶子：查子分类
  const res = await pageStoryCategoryTreeAPI({
    ...(params as StoryCategoryPageQueryDTO),
    parentId: detailId.value,
  })
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 分类相关操作 -------- */
function handleCreate(parent?: StoryCategoryVO) {
  router.push({
    name: 'AdminStoryCategoryCreate',
    query: {
      parentId: String(parent?.id ?? detailId.value),
      categoryType: String(parent?.categoryType ?? detail.value?.categoryType ?? ''),
      from: 'detail',
      detailId: String(detailId.value),
    },
  })
}

/* -------- 跳到某个子节点的详情 -------- */
function handleDetail(row: StoryCategoryVO) {
  router.push({
    name: 'AdminStoryCategoryDetail',
    params: { id: String(row.id) },
  })
}

function handleEdit(row: StoryCategoryVO) {
  router.push({
    name: 'AdminStoryCategoryEdit',
    params: { id: String(row.id) },
    query: { from: 'detail', detailId: String(detailId.value) },
  })
}

/** 编辑当前正在查看的分类 */
function handleEditCurrent() {
  if (!detail.value?.id) return
  router.push({
    name: 'AdminStoryCategoryEdit',
    params: { id: String(detail.value.id) },
    query: { from: 'detail', detailId: String(detail.value.id) },
  })
}

async function handleDelete(row: StoryCategoryVO) {
  try {
    await ElMessageBox.confirm(`确定要删除分类「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger',
    })
  } catch {
    return
  }
  await deleteStoryCategoryAPI(row.id!)
  ElMessage.success('删除成功')
  await loadDetail()
  await tableRef.value?.refresh()
}

/* -------- 剧情相关操作 -------- */
function handleCreateStory(row?: StoryCategoryVO) {
  router.push({
    name: 'AdminStoryCreate',
    query: {
      categoryId: String(row?.id ?? detail.value?.id),
      categoryType: String(row?.categoryType ?? detail.value?.categoryType ?? ''),
      from: 'storyCategoryDetail',
      detailId: String(detailId.value),
    },
  })
}

function handleStoryEdit(row: StoryVO) {
  router.push({
    name: 'AdminStoryEdit',
    params: { id: String(row.id) },
    query: { from: 'storyCategoryDetail', detailId: String(detailId.value) },
  })
}

function handleStoryView(row: StoryVO) {
  // 用户端浏览路由，新标签页打开
  const { href } = router.resolve({
    name: 'UserStoryBrowseDetail',
    params: { type: String(row.categoryType ?? 1), kind: 'story', id: String(row.id) },
  })
  window.open(href, '_blank', 'noopener,noreferrer')
}

async function handleStoryDelete(row: StoryVO) {
  try {
    await ElMessageBox.confirm(`确定要删除剧情「${row.title ?? row.id}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger',
    })
  } catch {
    return
  }
  await deleteStoryAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}

function handleBack() {
  router.push({ name: 'AdminStoryCategoryManage' })
}

watch(
  () => route.params.id,
  async (val) => {
    if (!val) return
    detail.value = null
    await loadDetail()
  },
  { immediate: true },
)
</script>

<style lang="scss" scoped>
.story-category-detail {
  /* 让卡片本身成为 flex 纵向容器 */
  :deep(.el-card) {
    display: flex;
    flex-direction: column;
    min-height: 0;
  }

  /* 关键：card body 变成可伸缩的 flex 容器 */
  :deep(.el-card__body) {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0; /* 允许子元素收缩，避免撑破 */
    overflow: hidden; /* 关键：不让 body 出滚动条 */
    padding: 16px; /* 按你项目实际 padding 调整 */
  }
}
</style>
