<template>
  <div class="person-manage flex h-full flex-col">
    <!-- 搜索 + 操作栏 -->
    <TableToolbar :model="query" align="start" @reset="handleReset" @search="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="中文名 / 日文名 / 罗马音"
          style="width: 220px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="类型">
        <el-select v-model="query.personType" placeholder="全部" style="width: 140px" clearable>
          <el-option
            v-for="opt in PERSON_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="经纪公司">
        <el-select
          v-model="query.agencyId"
          placeholder="全部"
          style="width: 180px"
          clearable
          filterable
        >
          <el-option v-for="a in agencyStore.agency" :key="a.id" :label="a.name" :value="a.id!" />
        </el-select>
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增人物
        </el-button>
      </template>
    </TableToolbar>

    <!-- 表格区 -->
    <el-card class="flex-1" shadow="never">
      <ProTable
        ref="tableRef"
        :columns="columns"
        :default-page-num="initialPageNum"
        :default-page-size="initialPageSize"
        :initial-params="initialParams"
        :request="fetchList"
        row-key="id"
        @page-change="handlePageChange"
      >
        <!-- 头像 -->
        <template #avatar="{ row }">
          <el-avatar :size="40" :src="row.avatar">
            {{ row.nameCn?.charAt(0) || 'P' }}
          </el-avatar>
        </template>

        <!-- 名称 -->
        <template #name="{ row }">
          <div class="leading-tight">
            <div class="font-medium">{{ row.nameCn || '-' }}</div>
            <div class="text-xs text-slate-400">
              {{ row.nameJp || row.nameRomaji || '' }}
            </div>
          </div>
        </template>

        <!-- 类型 -->
        <template #personType="{ row }">
          <el-tag :type="row.personType === PERSON_TYPE.IDOL ? 'danger' : 'primary'" effect="plain">
            {{ PERSON_TYPE_LABEL[row.personType as PersonTypeValue] ?? '未知' }}
          </el-tag>
        </template>

        <!-- 应援色 -->
        <template #themeColor="{ row }">
          <span v-if="row.themeColor" class="inline-flex items-center gap-2">
            <span
              :style="{ backgroundColor: row.themeColor }"
              class="h-4 w-4 rounded-full border border-slate-200"
            />
            <span class="text-xs text-slate-500">{{ row.themeColor }}</span>
          </span>
          <span v-else class="text-slate-300">-</span>
        </template>

        <!-- 所属（偶像显示团体，经纪人显示公司） -->
        <template #belong="{ row }">
          <div class="flex flex-wrap gap-1 justify-center">
            <template v-if="row.personType === PERSON_TYPE.IDOL">
              <el-tag
                v-for="g in row.groups ?? []"
                :key="g.id"
                effect="plain"
                size="small"
                type="info"
              >
                {{ g.name }}
              </el-tag>
              <span v-if="!row.groups?.length" class="text-slate-300">-</span>
            </template>
            <template v-else>
              <el-tag
                v-for="a in row.agency ?? []"
                :key="a.id"
                effect="plain"
                size="small"
                type="info"
              >
                {{ a.name }}
              </el-tag>
              <span v-if="!row.agency?.length" class="text-slate-300">-</span>
            </template>
          </div>
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
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { deletePersonAPI, listPersonAPI } from '@/api/person'
import {
  type PageChangePayload,
  type PageResult,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import {
  AdminRouteName,
  PERSON_TYPE,
  PERSON_TYPE_LABEL,
  PERSON_TYPE_OPTIONS,
  type PersonTypeValue,
} from '@/constants'
import {} from '@/constants'
import { useAgencyStore } from '@/stores'
import type { PersonPageQueryDTO, PersonVO } from '@/types/person'

const route = useRoute()
const router = useRouter()
const agencyStore = useAgencyStore()

/* -------- 表格 ref -------- */
type TableInstance = ProTableExpose<PersonVO>
const tableRef = ref<TableInstance>()

/* -------- 从 URL 解析初始值 -------- */
/** 解析正整数，非法时回退到 fallback */
function parsePositiveInt(raw: unknown, fallback: number): number {
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? Math.floor(n) : fallback
}

const initialPageNum = parsePositiveInt(route.query.pageNum, 1)
const initialPageSize = parsePositiveInt(route.query.pageSize, 20)

const initialParams: PersonPageQueryDTO = {
  keyword: typeof route.query.keyword === 'string' ? route.query.keyword : undefined,
  personType: route.query.personType
    ? (Number(route.query.personType) as PersonTypeValue)
    : undefined,
  agencyId: route.query.agencyId ? Number(route.query.agencyId) : undefined,
}

/* -------- 搜索参数（从 URL 恢复） -------- */
const query = reactive<{
  keyword?: string
  personType?: PersonTypeValue
  agencyId?: number
}>({
  keyword: initialParams.keyword ?? '',
  personType: initialParams.personType as PersonTypeValue,
  agencyId: initialParams.agencyId,
})

/* -------- 当前分页状态（跟随 ProTable） -------- */
const currentPageNum = ref(initialPageNum)
const currentPageSize = ref(initialPageSize)

/* -------- URL 同步 -------- */
/**
 * 防循环标志：
 * 页面自己写 URL 时置为 true，route.query 的 watch 检测到就跳过，
 * 只有"外部驱动"的 URL 变化（浏览器前进/后退）才进入反向同步分支。
 */
let isSelfUpdating = false

/**
 * 根据当前状态生成 query 对象。
 * pageNum/pageSize 始终写入（符合"刷新后保持分页"需求）。
 */
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
    if (query.personType != null) q.personType = String(query.personType)
    if (query.agencyId != null) q.agencyId = String(query.agencyId)
  }
  return q
}

/** 写 URL（用 replace 避免污染历史，配合 flag 防循环） */
function syncUrl(pageNum: number, pageSize: number, includeSearch = true) {
  isSelfUpdating = true
  void router.replace({ query: buildQuery(pageNum, pageSize, includeSearch) }).finally(() => {
    // nextTick 后重置，保证本轮触发的 route.query watch 已完成
    void nextTick(() => {
      isSelfUpdating = false
    })
  })
}

/* -------- 列配置 -------- */
const columns: ProTableColumn<PersonVO>[] = [
  { label: '头像', minWidth: 80, align: 'center', slot: 'avatar', fixed: 'left' },
  { label: '名称', minWidth: 160, align: 'center', slot: 'name', fixed: 'left' },
  { prop: 'cv', label: '声优', minWidth: 120, align: 'center' },
  { prop: 'age', label: '年龄', minWidth: 80, align: 'center' },
  { prop: 'birthday', label: '生日', minWidth: 80, align: 'center' },
  { prop: 'height', label: '身高(cm)', minWidth: 90, align: 'center' },
  { prop: 'weight', label: '体重(kg)', minWidth: 90, align: 'center' },
  { prop: 'shoeSize', label: '足长(cm)', minWidth: 90, align: 'center' },
  { prop: 'bloodTypeLabel', label: '血型', minWidth: 80, align: 'center' },
  { prop: 'personType', label: '类型', minWidth: 100, align: 'center', slot: 'personType' },
  { label: '应援色', minWidth: 180, align: 'center', slot: 'themeColor' },
  { label: '所属', minWidth: 200, align: 'center', slot: 'belong' },
  { label: '操作', minWidth: 140, align: 'center', fixed: 'right', slot: 'action' },
]

/* -------- 列表请求 -------- */
async function fetchList(
  params: PersonPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<PersonVO>> {
  const res = await listPersonAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    keyword: query.keyword || undefined,
    personType: query.personType ?? undefined,
    agencyId: query.agencyId ?? undefined,
  })
  // search 会重置 pageNum=1（可能触发 page-change，但也可能不触发——比如原来就在第 1 页），
  // 这里主动写一次 URL 兜底，确保搜索条件一定进 URL。
  syncUrl(1, currentPageSize.value)
}

function handleReset() {
  // 1. 清空本地搜索条件
  query.keyword = ''
  query.personType = undefined
  query.agencyId = undefined
  // 2. ProTable 重置（pageNum → defaultPageNum=初始值，pageSize → defaultPageSize=初始值）
  tableRef.value?.reset()
  // 3. 写 URL：保留 pageNum / pageSize，清空搜索参数
  //    重置后 pageSize 是"初始值"（initialPageSize），非当前值
  currentPageNum.value = initialPageNum
  currentPageSize.value = initialPageSize
  syncUrl(initialPageNum, initialPageSize, false)
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

    // 从 URL 解析参数
    const pageNum = parsePositiveInt(newQuery.pageNum, 1)
    const pageSize = parsePositiveInt(newQuery.pageSize, 20)
    const keyword = typeof newQuery.keyword === 'string' ? newQuery.keyword : ''
    const personType = newQuery.personType
      ? (Number(newQuery.personType) as PersonTypeValue)
      : undefined
    const agencyId = newQuery.agencyId ? Number(newQuery.agencyId) : undefined

    // 同步本地状态
    query.keyword = keyword
    query.personType = personType
    query.agencyId = agencyId
    currentPageNum.value = pageNum
    currentPageSize.value = pageSize

    // 一次请求恢复（合并搜索参数 + 分页）
    void tableRef.value?.goToPage(pageNum, pageSize, {
      keyword: keyword || undefined,
      personType,
      agencyId,
    })
  },
  { deep: true },
)

/* -------- 新增 / 编辑 -------- */
function handleCreate() {
  router.push({ name: AdminRouteName.PERSON_CREATE })
}

function handleEdit(row: PersonVO) {
  router.push({
    name: AdminRouteName.PERSON_EDIT,
    params: { id: String(row.id) },
  })
}

/* -------- 删除 -------- */
async function handleDelete(row: PersonVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除人物「${row.nameCn ?? row.nameJp ?? row.id}」吗？此操作不可恢复。`,
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

  await deletePersonAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}

/* -------- 初始化 -------- */
onMounted(() => {
  // 保证经纪公司下拉有数据
  void agencyStore.loadAll()
})
</script>
