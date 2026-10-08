<template>
  <div class="role-manage flex h-full flex-col">
    <!-- 顶部操作栏 -->
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="角色名"
          style="width: 180px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="对应人物">
        <el-select
          v-model="query.personId"
          placeholder="全部"
          style="width: 180px"
          clearable
          filterable
        >
          <el-option
            v-for="p in personStore.persons"
            :key="p.id"
            :label="p.nameCn || p.nameJp || `#${p.id}`"
            :value="p.id!"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="剧情分类">
        <el-tree-select
          v-model="query.storyCategoryId"
          :data="categoryTree"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="全部"
          style="width: 200px"
          check-strictly
          clearable
          filterable
        />
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增角色
        </el-button>
      </template>
    </TableToolbar>

    <!-- 表格 -->
    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id" stripe>
        <!-- 角色名 -->
        <template #name="{ row }">
          <span class="font-medium">{{ row.name || '-' }}</span>
        </template>

        <!-- 简介 -->
        <template #intro="{ row }">
          <span class="line-clamp-1 text-slate-600">{{ row.intro || '-' }}</span>
        </template>

        <!-- 对应人物 -->
        <template #person="{ row }">
          <el-tag v-if="row.personId" effect="plain" type="primary">
            {{ row.personNameCn || personStore.getPersonName(row.personId) || `#${row.personId}` }}
          </el-tag>
          <span v-else class="text-slate-300">-</span>
        </template>

        <!-- 所属剧情分类 -->
        <template #storyCategories="{ row }">
          <div v-if="row.storyCategories?.length" class="flex flex-wrap justify-center gap-1">
            <el-tag
              v-for="c in row.storyCategories"
              :key="c.id"
              effect="plain"
              size="small"
              type="info"
            >
              {{ c.name }}
            </el-tag>
          </div>
          <span v-else class="text-slate-300">-</span>
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
import { deleteRoleAPI, listRoleAPI } from '@/api/role'
import { listStoryCategoryTreeAPI } from '@/api/storyCategory'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import { AdminRouteName } from '@/constants'
import { usePersonStore } from '@/stores'
import type { RolePageQueryDTO, RoleVO } from '@/types/role'
import type { StoryCategoryVO } from '@/types/storyCategory'

const router = useRouter()
const personStore = usePersonStore()

/* -------- 表格 ref -------- */
type TableInstance = ProTableExpose<RoleVO>
const tableRef = ref<TableInstance>()

/* -------- 搜索参数 -------- */
const query = reactive<{
  keyword?: string
  personId?: number
  storyCategoryId?: number
}>({
  keyword: '',
  personId: undefined,
  storyCategoryId: undefined,
})

/* -------- 剧情分类树（搜索用） -------- */
const categoryTree = ref<StoryCategoryVO[]>([])

/* -------- 列配置 -------- */
const columns: ProTableColumn<RoleVO>[] = [
  { label: '角色名', minWidth: 160, slot: 'name' },
  { label: '对应人物', minWidth: 140, align: 'center', slot: 'person' },
  { label: '简介', minWidth: 220, slot: 'intro', showOverflowTooltip: true },
  { label: '所属剧情分类', minWidth: 220, align: 'center', slot: 'storyCategories' },
  { label: '操作', width: 140, align: 'center', fixed: 'right', slot: 'action' },
]

/* -------- 请求适配 -------- */
async function fetchList(
  params: RolePageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<RoleVO>> {
  const res = await listRoleAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    keyword: query.keyword || undefined,
    personId: query.personId ?? undefined,
    storyCategoryId: query.storyCategoryId ?? undefined,
  })
}

function handleReset() {
  query.keyword = ''
  query.personId = undefined
  query.storyCategoryId = undefined
  tableRef.value?.reset()
}

/* -------- 新增 / 编辑 -------- */
function handleCreate() {
  router.push({ name: AdminRouteName.ROLE_CREATE })
}

function handleEdit(row: RoleVO) {
  router.push({
    name: AdminRouteName.ROLE_EDIT,
    params: { id: String(row.id) },
  })
}

/* -------- 删除 -------- */
async function handleDelete(row: RoleVO) {
  try {
    await ElMessageBox.confirm(`确定要删除角色「${row.name ?? row.id}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger',
    })
  } catch {
    return
  }

  await deleteRoleAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}

/* -------- 初始化 -------- */
onMounted(async () => {
  // 预加载人物下拉和剧情分类树
  void personStore.loadAll()
  const res = await listStoryCategoryTreeAPI({})
  categoryTree.value = res.data ?? []
})
</script>
