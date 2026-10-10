<template>
  <div class="sticker-manage flex h-full flex-col">
    <!-- 搜索 + 操作栏 -->
    <TableToolbar :model="query" align="start" @reset="handleReset" @search="handleSearch">
      <el-form-item label="标签">
        <el-input
          v-model="query.label"
          placeholder="标签关键字"
          style="width: 220px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="分组">
        <el-select
          v-model="query.groupId"
          placeholder="全部"
          style="width: 180px"
          clearable
          filterable
        >
          <el-option v-for="g in groupOptions" :key="g.id" :label="g.name" :value="g.id!" />
        </el-select>
      </el-form-item>

      <el-form-item label="类型">
        <el-select v-model="query.stickerType" placeholder="全部" style="width: 140px" clearable>
          <el-option
            v-for="opt in STICKER_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增表情包
        </el-button>
      </template>
    </TableToolbar>

    <!-- 表格区 -->
    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id">
        <!-- 预览 -->
        <template #preview="{ row }">
          <div class="flex items-center justify-center">
            <el-image
              v-if="row.stickerType === STICKER_TYPE.IMAGE && row.imageUrl"
              :preview-src-list="[row.imageUrl]"
              :src="row.imageUrl"
              fit="contain"
              style="width: 48px; height: 48px"
              preview-teleported
            />
            <span v-else-if="row.stickerType === STICKER_TYPE.EMOJI" class="text-3xl leading-none">
              {{ row.emoji || '-' }}
            </span>
            <span v-else class="text-slate-300">-</span>
          </div>
        </template>

        <!-- 分组 -->
        <template #groupName="{ row }">
          <el-tag v-if="row.groupName" effect="plain" size="small" type="info">
            {{ row.groupName }}
          </el-tag>
          <span v-else class="text-slate-300">-</span>
        </template>

        <!-- 类型 -->
        <template #stickerType="{ row }">
          <el-tag
            :type="row.stickerType === STICKER_TYPE.IMAGE ? 'success' : 'warning'"
            effect="plain"
          >
            {{
              row.stickerTypeDesc ||
              STICKER_TYPE_LABEL[row.stickerType as StickerTypeValue] ||
              '未知'
            }}
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
import { onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteStickerAPI, listStickerAPI } from '@/api/sticker'
import { listStickerGroupOptionsAPI } from '@/api/stickerGroup'
import { type PageResult, type ProTableColumn, type ProTableExpose } from '@/components/ProTable'
import {
  AdminRouteName,
  STICKER_TYPE,
  STICKER_TYPE_LABEL,
  STICKER_TYPE_OPTIONS,
  type StickerTypeValue,
} from '@/constants'
import type { StickerGroupVO, StickerPageQueryDTO, StickerVO } from '@/types/sticker'

const router = useRouter()

/* -------- 表格 ref -------- */
type TableInstance = ProTableExpose<StickerVO>
const tableRef = ref<TableInstance>()

/* -------- 分组下拉 -------- */
const groupOptions = ref<StickerGroupVO[]>([])

async function loadGroups() {
  const res = await listStickerGroupOptionsAPI()
  groupOptions.value = res.data ?? []
}

/* -------- 搜索参数 -------- */
const query = reactive<{
  label?: string
  groupId?: number
  stickerType?: StickerTypeValue
}>({
  label: '',
  groupId: undefined,
  stickerType: undefined,
})

/* -------- 列配置 -------- */
const columns: ProTableColumn<StickerVO>[] = [
  { label: '预览', minWidth: 90, align: 'center', slot: 'preview', fixed: 'left' },
  { prop: 'label', label: '标签', minWidth: 160, align: 'center' },
  { label: '分组', minWidth: 140, align: 'center', slot: 'groupName' },
  { label: '类型', minWidth: 120, align: 'center', slot: 'stickerType' },
  { prop: 'emoji', label: 'Emoji', minWidth: 80, align: 'center' },
  { prop: 'imageUrl', label: '图片地址', minWidth: 260, align: 'left', showOverflowTooltip: true },
  { prop: 'createdAt', label: '创建时间', minWidth: 170, align: 'center' },
  { label: '操作', minWidth: 140, align: 'center', fixed: 'right', slot: 'action' },
]

/* -------- 请求适配 -------- */
async function fetchList(
  params: StickerPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<StickerVO>> {
  const res = await listStickerAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    label: query.label || undefined,
    groupId: query.groupId ?? undefined,
    stickerType: query.stickerType ?? undefined,
  })
}

function handleReset() {
  query.label = ''
  query.groupId = undefined
  query.stickerType = undefined
  tableRef.value?.reset()
}

/* -------- 新增 / 编辑 -------- */
function handleCreate() {
  router.push({ name: AdminRouteName.STICKER_CREATE })
}

function handleEdit(row: StickerVO) {
  router.push({
    name: AdminRouteName.STICKER_EDIT,
    params: { id: String(row.id) },
  })
}

/* -------- 删除 -------- */
async function handleDelete(row: StickerVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除表情包「${row.label ?? row.id}」吗？此操作不可恢复。`,
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

  await deleteStickerAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}

/* -------- 初始化 -------- */
onMounted(() => {
  void loadGroups()
})
</script>
