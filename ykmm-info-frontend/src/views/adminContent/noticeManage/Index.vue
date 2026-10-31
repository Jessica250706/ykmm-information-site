<template>
  <div class="notice-manage flex h-full flex-col">
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="标题"
          style="width: 180px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="分类">
        <el-select v-model="query.categoryId" placeholder="全部" style="width: 140px" clearable>
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" style="width: 120px" clearable>
          <el-option
            v-for="opt in NOTICE_STATUS_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="可见性">
        <el-select v-model="query.minVisibility" placeholder="全部" style="width: 160px" clearable>
          <el-option
            v-for="opt in NOTICE_VISIBILITY_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate">新增公告</el-button>
        <el-button @click="router.push({ name: AdminRouteName.NOTICE_CATEGORY_MANAGE })">
          分类管理
        </el-button>
      </template>
    </TableToolbar>

    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id">
        <template #title="{ row }">
          <div class="flex items-center gap-2">
            <el-tag v-if="row.isTop === 1" effect="plain" size="small" type="danger">置顶</el-tag>
            <span class="font-medium">{{ row.title || '-' }}</span>
          </div>
        </template>

        <template #category="{ row }">
          <el-tag v-if="row.categoryName" :color="row.categoryColor" effect="plain" size="small">
            {{ row.categoryName }}
          </el-tag>
          <span v-else class="text-slate-300">-</span>
        </template>

        <template #status="{ row }">
          <el-tag :type="NOTICE_STATUS_TAG_TYPE[row.status ?? 0]" effect="plain">
            {{ row.statusLabel || '-' }}
          </el-tag>
        </template>

        <template #minVisibility="{ row }">
          <span class="text-xs text-slate-500">{{ row.minVisibilityLabel || '-' }}</span>
        </template>

        <template #publishAt="{ row }">
          <span class="text-xs text-slate-500">{{ row.publishAt || row.createdAt || '-' }}</span>
        </template>

        <template #action="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button
            v-if="row.status === NOTICE_STATUS.DRAFT || row.status === NOTICE_STATUS.OFFLINE"
            size="small"
            type="success"
            link
            @click="handlePublish(row)"
          >
            发布
          </el-button>
          <el-button
            v-if="row.status === NOTICE_STATUS.PUBLISHED"
            size="small"
            type="warning"
            link
            @click="handleOffline(row)"
          >
            下线
          </el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </ProTable>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  deleteNoticeAPI,
  listNoticeCategoriesAPI,
  offlineNoticeAPI,
  pageAdminNoticesAPI,
  publishNoticeAPI,
} from '@/api/notice'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import {
  AdminRouteName,
  NOTICE_STATUS,
  NOTICE_STATUS_OPTIONS,
  NOTICE_STATUS_TAG_TYPE,
  NOTICE_VISIBILITY_OPTIONS,
} from '@/constants'
import type { NoticeCategoryVO, NoticePageQueryDTO, NoticeVO } from '@/types/notice'

const router = useRouter()
const tableRef = ref<ProTableExpose<NoticeVO>>()
const categories = ref<NoticeCategoryVO[]>([])

const query = reactive<NoticePageQueryDTO>({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  categoryId: undefined,
  status: undefined,
  minVisibility: undefined,
})

const columns: ProTableColumn<NoticeVO>[] = [
  { label: '标题', minWidth: 240, slot: 'title' },
  { label: '分类', width: 120, align: 'center', slot: 'category' },
  { label: '状态', width: 100, align: 'center', slot: 'status' },
  { label: '可见性', width: 160, align: 'center', slot: 'minVisibility' },
  { label: '发布时间', width: 170, align: 'center', slot: 'publishAt' },
  { label: '浏览量', prop: 'viewCount', width: 90, align: 'center' },
  { label: '操作', width: 260, align: 'center', fixed: 'right', slot: 'action', displayFlex: true },
]

async function fetchList(params: NoticePageQueryDTO): Promise<PageResult<NoticeVO>> {
  const res = await pageAdminNoticesAPI(params)
  return { records: res.data.records ?? [], total: res.data.total ?? 0 }
}

function handleSearch() {
  tableRef.value?.search({
    keyword: query.keyword || undefined,
    categoryId: query.categoryId,
    status: query.status,
    minVisibility: query.minVisibility,
  })
}

function handleReset() {
  query.keyword = ''
  query.categoryId = undefined
  query.status = undefined
  query.minVisibility = undefined
  tableRef.value?.reset()
}

function handleCreate() {
  router.push({ name: AdminRouteName.NOTICE_CREATE })
}

function handleEdit(row: NoticeVO) {
  router.push({ name: AdminRouteName.NOTICE_EDIT, params: { id: String(row.id) } })
}

async function handlePublish(row: NoticeVO) {
  await ElMessageBox.confirm('确定发布该公告吗？', '提示', { type: 'info' })
  await publishNoticeAPI(row.id!)
  ElMessage.success('发布成功')
  await tableRef.value?.refresh()
}

async function handleOffline(row: NoticeVO) {
  await ElMessageBox.confirm('确定下线该公告吗？下线后用户将无法看到。', '提示', {
    type: 'warning',
  })
  await offlineNoticeAPI(row.id!)
  ElMessage.success('已下线')
  await tableRef.value?.refresh()
}

async function handleDelete(row: NoticeVO) {
  try {
    await ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '删除确认', { type: 'warning' })
  } catch {
    return
  }
  await deleteNoticeAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}

onMounted(async () => {
  const res = await listNoticeCategoriesAPI()
  categories.value = res.data ?? []
})
</script>
