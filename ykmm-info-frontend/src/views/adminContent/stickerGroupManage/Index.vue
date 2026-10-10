<template>
  <div class="sticker-group-manage flex h-full flex-col">
    <!-- 搜索 + 操作栏 -->
    <TableToolbar :model="query" align="start" @reset="handleReset" @search="handleSearch">
      <el-form-item label="分组名称">
        <el-input
          v-model="query.name"
          placeholder="分组名称关键字"
          style="width: 220px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增分组
        </el-button>
      </template>
    </TableToolbar>

    <!-- 表格区 -->
    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id">
        <!-- 表情包数量 -->
        <template #stickerCount="{ row }">
          <el-tag effect="plain" size="small" type="info">
            {{ row.stickerCount ?? 0 }}
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
import { deleteStickerGroupAPI, listStickerGroupAPI } from '@/api/stickerGroup'
import { type PageResult, type ProTableColumn, type ProTableExpose } from '@/components/ProTable'
import { AdminRouteName } from '@/constants'
import type { StickerGroupPageQueryDTO, StickerGroupVO } from '@/types/sticker'

const router = useRouter()

/* -------- 表格 ref -------- */
type TableInstance = ProTableExpose<StickerGroupVO>
const tableRef = ref<TableInstance>()

/* -------- 搜索参数 -------- */
const query = reactive<{
  name?: string
}>({
  name: '',
})

/* -------- 列配置 -------- */
const columns: ProTableColumn<StickerGroupVO>[] = [
  { prop: 'name', label: '分组名称', minWidth: 160, align: 'center', fixed: 'left' },
  {
    prop: 'description',
    label: '描述',
    minWidth: 240,
    align: 'left',
    showOverflowTooltip: true,
  },
  { label: '表情包数量', minWidth: 110, align: 'center', slot: 'stickerCount' },
  { prop: 'sort', label: '排序', minWidth: 80, align: 'center' },
  { prop: 'createdAt', label: '创建时间', minWidth: 170, align: 'center' },
  { prop: 'updatedAt', label: '更新时间', minWidth: 170, align: 'center' },
  { label: '操作', minWidth: 140, align: 'center', fixed: 'right', slot: 'action' },
]

/* -------- 请求适配 -------- */
async function fetchList(
  params: StickerGroupPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<StickerGroupVO>> {
  const res = await listStickerGroupAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    name: query.name || undefined,
  })
}

function handleReset() {
  query.name = ''
  tableRef.value?.reset()
}

/* -------- 新增 / 编辑 -------- */
function handleCreate() {
  router.push({ name: AdminRouteName.STICKER_GROUP_CREATE })
}

function handleEdit(row: StickerGroupVO) {
  router.push({
    name: AdminRouteName.STICKER_GROUP_EDIT,
    params: { id: String(row.id) },
  })
}

/* -------- 删除 -------- */
async function handleDelete(row: StickerGroupVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除分组「${row.name ?? row.id}」吗？此操作不可恢复。`,
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

  await deleteStickerGroupAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}
</script>
