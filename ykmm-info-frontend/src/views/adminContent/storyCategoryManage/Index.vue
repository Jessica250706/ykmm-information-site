<template>
  <div class="story-category-manage flex h-full flex-col">
    <!-- 顶部操作栏 -->
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="分类类型">
        <el-select v-model="query.categoryType" placeholder="全部" style="width: 160px" clearable>
          <el-option
            v-for="opt in STORY_CATEGORY_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate()">
          <el-icon><Plus /></el-icon>
          新增顶级分类
        </el-button>
      </template>
    </TableToolbar>

    <!-- 树形表格（分页） -->
    <el-card class="flex-1" shadow="never">
      <ProTable
        ref="tableRef"
        :columns="columns"
        :request="fetchList"
        :tree-props="{ children: 'children' }"
        row-key="id"
      >
        <!-- 分类名 -->
        <template #name="{ row }">
          <span class="font-medium">{{ row.name }}</span>
        </template>

        <!-- 类型 -->
        <template #categoryType="{ row }">
          <el-tag :type="categoryTypeTag(row.categoryType)" effect="plain">
            {{ STORY_CATEGORY_TYPE_LABEL[row.categoryType as StoryCategoryTypeValue] ?? '未知' }}
          </el-tag>
        </template>

        <!-- 操作 -->
        <template #action="{ row }">
          <el-button size="small" type="primary" link @click="handleDetail(row)">详情</el-button>
          <el-button size="small" type="primary" link @click="handleCreate(row)">
            新增子分类
          </el-button>
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
import { deleteStoryCategoryAPI, pageStoryCategoryTreeAPI } from '@/api/storyCategory'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import {
  STORY_CATEGORY_TYPE,
  STORY_CATEGORY_TYPE_LABEL,
  STORY_CATEGORY_TYPE_OPTIONS,
  type StoryCategoryTypeValue,
} from '@/constants/story'
import type { StoryCategoryPageQueryDTO, StoryCategoryVO } from '@/types/storyCategory'

const router = useRouter()

/* -------- 表格 ref -------- */
type TableInstance = ProTableExpose<StoryCategoryVO>
const tableRef = ref<TableInstance>()

/* -------- 搜索参数 -------- */
const query = reactive<{
  categoryType?: StoryCategoryTypeValue
}>({
  categoryType: undefined,
})

/* -------- 列配置 -------- */
const columns: ProTableColumn<StoryCategoryVO>[] = [
  { prop: 'name', label: '分类名', minWidth: 240, slot: 'name' },
  {
    prop: 'categoryType',
    label: '分类类型',
    minWidth: 140,
    align: 'center',
    slot: 'categoryType',
  },
  { prop: 'sort', label: '排序', width: 100, align: 'center' },
  {
    prop: 'createdAt',
    label: '创建时间',
    minWidth: 180,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', width: 260, align: 'center', fixed: 'right', slot: 'action' },
]

/** 不同分类类型给不同 tag 颜色 */
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

/* -------- 请求适配 -------- */
async function fetchList(
  params: StoryCategoryPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<StoryCategoryVO>> {
  const res = await pageStoryCategoryTreeAPI(params)

  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    categoryType: query.categoryType ?? undefined,
  })
}

function handleReset() {
  query.categoryType = undefined
  tableRef.value?.reset()
}

/* -------- 详情 / 新增 / 编辑 -------- */
function handleDetail(row: StoryCategoryVO) {
  router.push({
    name: 'AdminStoryCategoryDetail',
    params: { id: String(row.id) },
  })
}

function handleCreate(parent?: StoryCategoryVO) {
  router.push({
    name: 'AdminStoryCategoryCreate',
    query: parent ? { parentId: String(parent.id), categoryType: String(parent.categoryType) } : {},
  })
}

function handleEdit(row: StoryCategoryVO) {
  router.push({
    name: 'AdminStoryCategoryEdit',
    params: { id: String(row.id) },
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
</script>
