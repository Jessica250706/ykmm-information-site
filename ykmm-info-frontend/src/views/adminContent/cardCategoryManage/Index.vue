<template>
  <div class="card-category-manage flex h-full flex-col">
    <!-- 顶部操作栏 -->
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="类别名称"
          style="width: 200px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增类别
        </el-button>
      </template>
    </TableToolbar>

    <!-- 表格 -->
    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id">
        <!-- 排序 -->
        <template #sort="{ row }">
          <el-tag effect="plain" type="info">{{ row.sort ?? 0 }}</el-tag>
        </template>

        <!-- 排序 -->
        <template #cardCount="{ row }">
          <el-tag effect="plain" type="info">{{ row.cardCount ?? 0 }}</el-tag>
        </template>

        <!-- 操作 -->
        <template #action="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </ProTable>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteCardCategoryAPI, pageCardCategoryAPI } from '@/api/cardCategory'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import { AdminRouteName } from '@/constants'
import type { CardCategoryPageQueryDTO, CardCategoryVO } from '@/types/cardCategory'

const router = useRouter()

type TableInstance = ProTableExpose<CardCategoryVO>
const tableRef = ref<TableInstance>()

const query = reactive<{
  keyword?: string
}>({
  keyword: '',
})

const columns: ProTableColumn<CardCategoryVO>[] = [
  { label: 'ID', prop: 'id', minWidth: 100, align: 'center', fixed: 'left' },
  { label: '类别名称', prop: 'name', minWidth: 240 },
  { label: '排序', minWidth: 120, align: 'center', slot: 'sort' },
  { label: '卡面数量', prop: 'cardCount', minWidth: 120, align: 'center', slot: 'cardCount' },
  { label: '操作', width: 180, align: 'center', slot: 'action', displayFlex: true, fixed: 'right' },
]

/* -------- 请求适配 -------- */
async function fetchList(
  params: CardCategoryPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<CardCategoryVO>> {
  const res = await pageCardCategoryAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    keyword: query.keyword || undefined,
  })
}

function handleReset() {
  query.keyword = ''
  tableRef.value?.reset()
}

/* -------- 新增 / 编辑 -------- */
function handleCreate() {
  router.push({ name: AdminRouteName.CARD_CATEGORY_CREATE })
}

function handleEdit(row: CardCategoryVO) {
  router.push({
    name: AdminRouteName.CARD_CATEGORY_EDIT,
    params: { id: String(row.id) },
  })
}

/* -------- 删除 -------- */
async function handleDelete(row: CardCategoryVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除类别「${row.name ?? row.id}」吗？若类别下还有卡面将无法删除。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '删除',
        confirmButtonClass: 'el-button--danger',
      },
    )
  } catch {
    return
  }

  await deleteCardCategoryAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}

/* -------- 初始化 -------- */
onMounted(() => {
  tableRef.value?.refresh()
})
</script>
