<template>
  <div class="role-manage flex h-full flex-col">
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

    <el-card class="flex-1" shadow="never">
      <ProTable
        ref="tableRef"
        :columns="columns"
        :default-page-num="initialPageNum"
        :default-page-size="initialPageSize"
        :initial-params="initialParams"
        :request="fetchList"
        row-key="id"
        stripe
        @page-change="handlePageChange"
      >
        <template #name="{ row }">
          <span class="font-medium">{{ row.name || '-' }}</span>
        </template>

        <template #intro="{ row }">
          <span class="line-clamp-1 text-slate-600">{{ row.intro || '-' }}</span>
        </template>

        <template #person="{ row }">
          <el-tag v-if="row.personId" effect="plain" type="primary">
            {{ row.personNameCn || personStore.getPersonName(row.personId) || `#${row.personId}` }}
          </el-tag>
          <span v-else class="text-slate-300">-</span>
        </template>

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

        <template #action="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </ProTable>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { deleteRoleAPI, listRoleAPI } from '@/api/role'
import { listStoryCategoryTreeAPI } from '@/api/storyCategory'
import {
  type PageChangePayload,
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import { AdminRouteName } from '@/constants'
import { usePersonStore } from '@/stores'
import type { RolePageQueryDTO, RoleVO } from '@/types/role'
import type { StoryCategoryVO } from '@/types/storyCategory'

const route = useRoute()
const router = useRouter()
const personStore = usePersonStore()

/* -------- 表格 ref -------- */
type TableInstance = ProTableExpose<RoleVO>
const tableRef = ref<TableInstance>()

/* -------- 从 URL 解析初始值 -------- */
/** 解析正整数，非法时回退到 fallback */
function parsePositiveInt(raw: unknown, fallback: number): number {
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? Math.floor(n) : fallback
}

/** 解析可选的正整数 ID，缺失或非法返回 undefined */
function parseOptionalId(raw: unknown): number | undefined {
  if (raw == null || raw === '') return undefined
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? Math.floor(n) : undefined
}

const initialPageNum = parsePositiveInt(route.query.pageNum, 1)
const initialPageSize = parsePositiveInt(route.query.pageSize, 20)

const initialParams: RolePageQueryDTO = {
  keyword: typeof route.query.keyword === 'string' ? route.query.keyword : undefined,
  personId: parseOptionalId(route.query.personId),
  storyCategoryId: parseOptionalId(route.query.storyCategoryId),
}

/* -------- 搜索参数（从 URL 恢复） -------- */
const query = reactive<{
  keyword?: string
  personId?: number
  storyCategoryId?: number
}>({
  keyword: initialParams.keyword ?? '',
  personId: initialParams.personId,
  storyCategoryId: initialParams.storyCategoryId,
})

/* -------- 当前分页状态（跟随 ProTable） -------- */
const currentPageNum = ref(initialPageNum)
const currentPageSize = ref(initialPageSize)

/* -------- 剧情分类树（搜索用） -------- */
const categoryTree = ref<StoryCategoryVO[]>([])

/* -------- URL 同步 -------- */
/**
 * 防循环标志：
 * 页面自己写 URL 时置为 true，route.query 的 watch 检测到就跳过。
 */
let isSelfUpdating = false

function buildQuery(
  pageNum: number,
  pageSize: number,
  includeSearch = true,
): Record<string, string> {
  const q: Record<string, string> = {
    pageNum: String(pageNum),
    pageSize: String(pageSize),
  }
  if (includeSearch) {
    if (query.keyword) q.keyword = query.keyword
    if (query.personId != null) q.personId = String(query.personId)
    if (query.storyCategoryId != null) q.storyCategoryId = String(query.storyCategoryId)
  }
  return q
}

function syncUrl(pageNum: number, pageSize: number, includeSearch = true) {
  isSelfUpdating = true
  void router.replace({ query: buildQuery(pageNum, pageSize, includeSearch) }).finally(() => {
    void nextTick(() => {
      isSelfUpdating = false
    })
  })
}

/* -------- 列配置 -------- */
const columns: ProTableColumn<RoleVO>[] = [
  { label: '角色名', minWidth: 160, slot: 'name' },
  { label: '对应人物', minWidth: 140, align: 'center', slot: 'person' },
  { label: '简介', minWidth: 220, slot: 'intro', showOverflowTooltip: true },
  { label: '所属剧情分类', minWidth: 220, align: 'center', slot: 'storyCategories' },
  { label: '操作', width: 140, align: 'center', fixed: 'right', slot: 'action' },
]

/* -------- 列表请求 -------- */
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
  // search 会重置 pageNum=1，但 pageSize 保持当前值；
  // 主动写一次 URL 兜底，确保搜索条件一定进 URL。
  syncUrl(1, currentPageSize.value)
}

function handleReset() {
  // 1. 清空本地搜索条件
  query.keyword = ''
  query.personId = undefined
  query.storyCategoryId = undefined

  // 2. 用 search 而非 reset：
  //    reset 会把 pageNum 重置到 defaultPageNum（= initialPageNum，可能是从 URL 恢复的 2、3），
  //    用户期望的"重置"是回到第 1 页，所以用 search 强制 pageNum=1。
  tableRef.value?.search({
    keyword: undefined,
    personId: undefined,
    storyCategoryId: undefined,
  })

  // 3. 写 URL：pageNum=1，pageSize 保持当前值，清空搜索参数
  syncUrl(1, currentPageSize.value, false)
}

/* -------- 分页变化（ProTable 内部变化时回调） -------- */
function handlePageChange({ pageNum, pageSize }: PageChangePayload) {
  currentPageNum.value = pageNum
  currentPageSize.value = pageSize
  syncUrl(pageNum, pageSize)
}

/* -------- 浏览器前进 / 后退：URL → 页面恢复 -------- */
watch(
  () => route.query,
  (newQuery) => {
    if (isSelfUpdating) return

    const pageNum = parsePositiveInt(newQuery.pageNum, 1)
    const pageSize = parsePositiveInt(newQuery.pageSize, 20)
    const keyword = typeof newQuery.keyword === 'string' ? newQuery.keyword : ''
    const personId = parseOptionalId(newQuery.personId)
    const storyCategoryId = parseOptionalId(newQuery.storyCategoryId)

    // 同步本地状态
    query.keyword = keyword
    query.personId = personId
    query.storyCategoryId = storyCategoryId
    currentPageNum.value = pageNum
    currentPageSize.value = pageSize

    // 一次请求恢复（合并搜索参数 + 分页）
    void tableRef.value?.goToPage(pageNum, pageSize, {
      keyword: keyword || undefined,
      personId,
      storyCategoryId,
    })
  },
  { deep: true },
)

/* -------- 新增 / 编辑 -------- */
function handleCreate() {
  router.push({ name: AdminRouteName.ROLE_CREATE, query: { redirect: route.fullPath } })
}

function handleEdit(row: RoleVO) {
  router.push({
    name: AdminRouteName.ROLE_EDIT,
    params: { id: String(row.id) },
    query: { redirect: route.fullPath },
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
  void personStore.loadAll()
  const res = await listStoryCategoryTreeAPI({})
  categoryTree.value = res.data ?? []
})
</script>
