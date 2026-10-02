<template>
  <div class="story-category-detail flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">剧情分类详情</span>
          <div>
            <el-button
              v-if="detail?.children?.length === 0"
              type="primary"
              @click="handleCreateStory()"
            >
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
        <el-descriptions :column="2" class="mb-4" border>
          <el-descriptions-item label="分类名">
            {{ detail.name ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="分类类型">
            <el-tag :type="categoryTypeTag(detail.categoryType)" effect="plain">
              {{ detail.categoryTypeLabel ?? '未知' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="父分类">
            {{ detail.parentName ?? '顶级分类' }}
          </el-descriptions-item>
          <el-descriptions-item label="排序">
            {{ detail.sort ?? 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ detail.createdAt ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ detail.updatedAt ?? '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <ProTable
          ref="tableRef"
          :columns="columns"
          :request="fetchChildren"
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

          <template #action="{ row }">
            <el-button size="small" type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button size="small" type="primary" link @click="handleCreate(row)">
              新增子分类
            </el-button>
            <el-button
              v-if="row.children.length === 0"
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
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
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
import { STORY_CATEGORY_TYPE } from '@/constants/story'
import type { StoryCategoryPageQueryDTO, StoryCategoryVO } from '@/types/storyCategory'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const detail = ref<StoryCategoryVO | null>(null)

const detailId = computed(() => (route.params.id ? Number(route.params.id) : null))

type TableInstance = ProTableExpose<StoryCategoryVO>
const tableRef = ref<TableInstance>()

/* -------- 列配置（和列表页保持一致） -------- */
const columns: ProTableColumn<StoryCategoryVO>[] = [
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
  { label: '操作', minWidth: 300, align: 'center', fixed: 'right', slot: 'action' },
]

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

/* -------- 子节点分页请求：带 parentId -------- */
async function fetchChildren(
  params: StoryCategoryPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<StoryCategoryVO>> {
  const res = await pageStoryCategoryTreeAPI({
    ...params,
    parentId: detailId.value ?? undefined,
  })
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 新增子分类：带上回跳信息 -------- */
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

/** 从详情页跳去新增剧情，回来时回详情页 */
function handleCreateStory(row?: StoryCategoryVO) {
  router.push({
    name: 'AdminStoryCreate',
    query: {
      categoryId: String(row?.id ?? detail.value?.id),
      categoryType: String(row?.categoryType ?? detail.value?.categoryType),
      from: 'storyCategoryDetail',
      detailId: String(detailId.value),
    },
  })
}

/* -------- 删除 -------- */
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
