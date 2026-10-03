<template>
  <div class="story-manage flex h-full flex-col">
    <!-- 搜索区 -->
    <TableToolbar :model="query" align="start" @reset="handleReset" @search="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="标题"
          style="width: 200px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="分类类型">
        <el-select v-model="query.categoryType" placeholder="全部" style="width: 140px" clearable>
          <el-option
            v-for="opt in storyCategoryTypeStore.types"
            :key="opt.id"
            :label="opt.name"
            :value="opt.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="所属分类">
        <el-tree-select
          v-model="query.categoryId"
          :data="categoryTree"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="全部"
          style="width: 200px"
          check-strictly
          clearable
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" style="width: 140px" clearable>
          <el-option
            v-for="opt in STORY_STATUS_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增剧情
        </el-button>
      </template>
    </TableToolbar>

    <!-- 表格区 -->
    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id" stripe>
        <!-- 标题 -->
        <template #title="{ row }">
          <div class="leading-tight">
            <div class="font-medium">{{ row.title || '-' }}</div>
            <div class="line-clamp-1 text-xs text-slate-400">
              {{ row.description || '' }}
            </div>
          </div>
        </template>

        <!-- 所属分类 -->
        <template #category="{ row }">
          <el-tag effect="plain" size="small" type="info">
            {{ row.categoryName || '-' }}
          </el-tag>
        </template>

        <!-- 分类类型 -->
        <template #categoryType="{ row }">
          <el-tag :type="categoryTypeTag(row.categoryType)" effect="plain">
            {{ row.categoryTypeLabel ?? '未知' }}
          </el-tag>
        </template>

        <!-- 状态 -->
        <template #status="{ row }">
          <el-tag :type="STORY_STATUS_TAG_TYPE[row.status as StoryStatusValue] ?? 'info'">
            {{ row.statusLabel ?? STORY_STATUS_LABEL[row.status as StoryStatusValue] ?? '未知' }}
          </el-tag>
        </template>

        <!-- 操作 -->
        <template #action="{ row }">
          <el-button
            v-if="row.status === STORY_STATUS.PENDING"
            size="small"
            type="warning"
            link
            @click="handleAudit(row)"
          >
            审核
          </el-button>
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </ProTable>
    </el-card>

    <!-- 审核弹窗 -->
    <AuditDialog v-model="auditVisible" :story="auditingStory" @success="handleAuditSuccess" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteStoryAPI, listStoryAPI } from '@/api/story'
import { listStoryCategoryTreeAPI } from '@/api/storyCategory'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import {
  STORY_CATEGORY_TYPE,
  STORY_STATUS,
  STORY_STATUS_LABEL,
  STORY_STATUS_OPTIONS,
  STORY_STATUS_TAG_TYPE,
  type StoryCategoryTypeValue,
  type StoryStatusValue,
} from '@/constants/story'
import { useStoryCategoryTypeStore } from '@/stores/storyCategoryTypeStore'
import type { StoryPageQueryDTO, StoryVO } from '@/types/story'
import type { StoryCategoryVO } from '@/types/storyCategory'
import AuditDialog from './components/AuditDialog.vue'

const router = useRouter()
const storyCategoryTypeStore = useStoryCategoryTypeStore()

type TableInstance = ProTableExpose<StoryVO>
const tableRef = ref<TableInstance>()

const query = reactive<{
  keyword?: string
  categoryType?: StoryCategoryTypeValue
  categoryId?: number
  status?: StoryStatusValue
}>({
  keyword: '',
  categoryType: undefined,
  categoryId: undefined,
  status: undefined,
})

const categoryTree = ref<StoryCategoryVO[]>([])

const columns: ProTableColumn<StoryVO>[] = [
  { label: '标题', minWidth: 200, slot: 'title' },
  { label: '所属分类', minWidth: 240, align: 'center', slot: 'category' },
  { label: '分类类型', minWidth: 120, align: 'center', slot: 'categoryType' },
  { prop: 'sort', label: '排序', width: 80, align: 'center' },
  { label: '状态', width: 100, align: 'center', slot: 'status' },
  {
    prop: 'createdAt',
    label: '创建时间',
    minWidth: 180,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', width: 180, align: 'center', fixed: 'right', slot: 'action' },
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

async function fetchList(
  params: StoryPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<StoryVO>> {
  const res = await listStoryAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

function handleSearch() {
  tableRef.value?.search({
    keyword: query.keyword || undefined,
    categoryType: query.categoryType ?? undefined,
    categoryId: query.categoryId ?? undefined,
    status: query.status ?? undefined,
  })
}

function handleReset() {
  query.keyword = ''
  query.categoryType = undefined
  query.categoryId = undefined
  query.status = undefined
  tableRef.value?.reset()
}

function handleCreate() {
  router.push({ name: 'AdminStoryCreate' })
}

function handleEdit(row: StoryVO) {
  router.push({
    name: 'AdminStoryEdit',
    params: { id: String(row.id) },
  })
}

async function handleDelete(row: StoryVO) {
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

/* -------- 审核 -------- */
const auditVisible = ref(false)
const auditingStory = ref<StoryVO | null>(null)

function handleAudit(row: StoryVO) {
  auditingStory.value = row
  auditVisible.value = true
}

function handleAuditSuccess() {
  void tableRef.value?.refresh()
}

/* -------- 初始化 -------- */
onMounted(async () => {
  const res = await listStoryCategoryTreeAPI({})
  categoryTree.value = res.data ?? []
})
</script>
