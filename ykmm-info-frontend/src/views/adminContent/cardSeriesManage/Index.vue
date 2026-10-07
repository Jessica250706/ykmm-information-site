<template>
  <div class="card-series-manage flex h-full flex-col">
    <!-- 顶部操作栏 -->
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="系列名"
          style="width: 200px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增系列
        </el-button>
      </template>
    </TableToolbar>

    <!-- 表格 -->
    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id" stripe>
        <!-- 封面 -->
        <!-- <template #cover="{ row }">
          <el-image
            v-if="row.coverImage"
            :preview-src-list="[row.coverImage]"
            :src="row.coverImage"
            fit="cover"
            style="width: 60px; height: 60px; border-radius: 4px"
            preview-teleported
          />
          <span v-else class="text-slate-300">-</span>
        </template> -->

        <!-- 系列名 -->
        <template #name="{ row }">
          <span class="font-medium">{{ row.name || '-' }}</span>
        </template>

        <!-- 描述 -->
        <template #description="{ row }">
          <span class="line-clamp-1 text-slate-600">{{ row.description || '-' }}</span>
        </template>

        <!-- 卡面数量 -->
        <template #cardCount="{ row }">
          <el-tag :type="row.cardCount > 0 ? 'primary' : 'info'" effect="plain">
            {{ row.cardCount ?? 0 }}
          </el-tag>
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
import { reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteCardSeriesAPI, pageCardSeriesAPI } from '@/api/cardSeries'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import type { CardSeriesPageQueryDTO, CardSeriesVO } from '@/types/cardSeries'

const router = useRouter()

type TableInstance = ProTableExpose<CardSeriesVO>
const tableRef = ref<TableInstance>()

const query = reactive<{
  keyword?: string
}>({
  keyword: '',
})

const columns: ProTableColumn<CardSeriesVO>[] = [
  // { label: '封面', width: 100, align: 'center', slot: 'cover', fixed: 'left' },
  { label: '系列名', minWidth: 200, slot: 'name' },
  { label: '描述', minWidth: 260, slot: 'description', showOverflowTooltip: true },
  { label: '卡面数量', width: 100, align: 'center', slot: 'cardCount' },
  {
    label: '创建时间',
    prop: 'createdAt',
    minWidth: 180,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', width: 140, align: 'center', fixed: 'right', slot: 'action' },
]

async function fetchList(
  params: CardSeriesPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<CardSeriesVO>> {
  const res = await pageCardSeriesAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

function handleSearch() {
  tableRef.value?.search({
    keyword: query.keyword || undefined,
  })
}

function handleReset() {
  query.keyword = ''
  tableRef.value?.reset()
}

function handleCreate() {
  router.push({ name: 'AdminCardSeriesCreate' })
}

function handleEdit(row: CardSeriesVO) {
  router.push({
    name: 'AdminCardSeriesEdit',
    params: { id: String(row.id) },
  })
}

async function handleDelete(row: CardSeriesVO) {
  try {
    await ElMessageBox.confirm(`确定要删除系列「${row.name ?? row.id}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger',
    })
  } catch {
    return
  }

  await deleteCardSeriesAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}
</script>
