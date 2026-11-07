<template>
  <div class="question-manage flex h-full flex-col">
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="题干"
          style="width: 220px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="题型">
        <el-select v-model="query.questionType" placeholder="全部" style="width: 120px" clearable>
          <el-option
            v-for="opt in QUESTION_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="题目类型">
        <el-select v-model="query.isFixed" placeholder="全部" style="width: 120px" clearable>
          <el-option
            v-for="opt in QUESTION_FIXED_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" style="width: 120px" clearable>
          <el-option
            v-for="opt in QUESTION_STATUS_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增题目
        </el-button>
        <el-button @click="router.push({ name: AdminRouteName.QUIZ_CONFIG_MANAGE })">
          答题配置
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
        @page-change="handlePageChange"
      >
        <!-- 题干 -->
        <template #content="{ row }">
          <span class="line-clamp-2 text-left">{{ row.content || '-' }}</span>
        </template>

        <!-- 题型 -->
        <template #questionType="{ row }">
          <el-tag
            :type="row.questionType === QUESTION_TYPE.MULTI ? 'warning' : 'primary'"
            effect="plain"
            size="small"
          >
            {{ row.questionTypeLabel || questionTypeLabel(row.questionType) }}
          </el-tag>
        </template>

        <!-- 固定/随机 -->
        <template #isFixed="{ row }">
          <el-tag
            :type="row.isFixed === QUESTION_FIXED.YES ? 'danger' : 'info'"
            effect="plain"
            size="small"
          >
            {{ questionFixedLabel(row.isFixed) }}
          </el-tag>
        </template>

        <!-- 状态 -->
        <template #status="{ row }">
          <el-tag :type="QUESTION_STATUS_TAG_TYPE[row.status ?? 0]" effect="plain" size="small">
            {{ row.statusLabel || questionStatusLabel(row.status) }}
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
import { nextTick, reactive, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { deleteQuestionAPI, pageQuestionsAPI } from '@/api/quiz'
import {
  type PageChangePayload,
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import {
  AdminRouteName,
  QUESTION_FIXED,
  QUESTION_FIXED_OPTIONS,
  QUESTION_STATUS_OPTIONS,
  QUESTION_STATUS_TAG_TYPE,
  QUESTION_TYPE,
  QUESTION_TYPE_OPTIONS,
  questionFixedLabel,
  questionStatusLabel,
  questionTypeLabel,
} from '@/constants'
import type { QuestionListVO, QuestionPageQueryDTO } from '@/types/quiz'

const route = useRoute()
const router = useRouter()

type TableInstance = ProTableExpose<QuestionListVO>
const tableRef = ref<TableInstance>()

/* -------- 从 URL 解析初始值 -------- */
function parsePositiveInt(raw: unknown, fallback: number): number {
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? Math.floor(n) : fallback
}

function parseOptionalInt(raw: unknown): number | undefined {
  if (raw == null || raw === '') return undefined
  const n = Number(raw)
  return Number.isFinite(n) ? Math.floor(n) : undefined
}

const initialPageNum = parsePositiveInt(route.query.pageNum, 1)
const initialPageSize = parsePositiveInt(route.query.pageSize, 20)

const initialParams: QuestionPageQueryDTO = {
  pageNum: initialPageNum,
  pageSize: initialPageSize,
  keyword: typeof route.query.keyword === 'string' ? route.query.keyword : undefined,
  questionType: parseOptionalInt(route.query.questionType),
  isFixed: parseOptionalInt(route.query.isFixed),
  status: parseOptionalInt(route.query.status),
}

/* -------- 搜索参数 -------- */
const query = reactive<{
  keyword?: string
  questionType?: number
  isFixed?: number
  status?: number
}>({
  keyword: initialParams.keyword ?? '',
  questionType: initialParams.questionType,
  isFixed: initialParams.isFixed,
  status: initialParams.status,
})

const currentPageNum = ref(initialPageNum)
const currentPageSize = ref(initialPageSize)

/* -------- URL 同步 -------- */
let isSelfUpdating = false

function buildQuery(pageNum: number, pageSize: number, includeSearch = true) {
  const q: Record<string, string> = {
    pageNum: String(pageNum),
    pageSize: String(pageSize),
  }
  if (includeSearch) {
    if (query.keyword) q.keyword = query.keyword
    if (query.questionType != null) q.questionType = String(query.questionType)
    if (query.isFixed != null) q.isFixed = String(query.isFixed)
    if (query.status != null) q.status = String(query.status)
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
const columns: ProTableColumn<QuestionListVO>[] = [
  { label: 'ID', prop: 'id', width: 80, align: 'center' },
  { label: '题干', minWidth: 320, slot: 'content' },
  { label: '题型', width: 90, align: 'center', slot: 'questionType' },
  { label: '类型', width: 100, align: 'center', slot: 'isFixed' },
  { label: '选项数', prop: 'optionCount', width: 90, align: 'center' },
  { label: '状态', width: 90, align: 'center', slot: 'status' },
  { label: '排序', prop: 'sort', width: 80, align: 'center' },
  {
    label: '创建时间',
    prop: 'createdAt',
    width: 170,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', width: 160, align: 'center', fixed: 'right', slot: 'action', displayFlex: true },
]

/* -------- 请求适配 -------- */
async function fetchList(
  params: QuestionPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<QuestionListVO>> {
  const res = await pageQuestionsAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    keyword: query.keyword || undefined,
    questionType: query.questionType ?? undefined,
    isFixed: query.isFixed ?? undefined,
    status: query.status ?? undefined,
  })
  syncUrl(1, currentPageSize.value)
}

function handleReset() {
  query.keyword = ''
  query.questionType = undefined
  query.isFixed = undefined
  query.status = undefined
  tableRef.value?.search({
    keyword: undefined,
    questionType: undefined,
    isFixed: undefined,
    status: undefined,
  })
  syncUrl(1, currentPageSize.value, false)
}

/* -------- 分页变化 -------- */
function handlePageChange({ pageNum, pageSize }: PageChangePayload) {
  currentPageNum.value = pageNum
  currentPageSize.value = pageSize
  syncUrl(pageNum, pageSize)
}

/* -------- 浏览器前进/后退 -------- */
watch(
  () => route.query,
  (newQuery) => {
    if (isSelfUpdating) return

    const pageNum = parsePositiveInt(newQuery.pageNum, 1)
    const pageSize = parsePositiveInt(newQuery.pageSize, 20)
    const keyword = typeof newQuery.keyword === 'string' ? newQuery.keyword : ''
    const questionType = parseOptionalInt(newQuery.questionType)
    const isFixed = parseOptionalInt(newQuery.isFixed)
    const status = parseOptionalInt(newQuery.status)

    query.keyword = keyword
    query.questionType = questionType
    query.isFixed = isFixed
    query.status = status
    currentPageNum.value = pageNum
    currentPageSize.value = pageSize

    void tableRef.value?.goToPage(pageNum, pageSize, {
      keyword: keyword || undefined,
      questionType,
      isFixed,
      status,
    })
  },
  { deep: true },
)

/* -------- 新增 / 编辑 -------- */
function handleCreate() {
  router.push({
    name: AdminRouteName.QUESTION_CREATE,
    query: { redirect: route.fullPath },
  })
}

function handleEdit(row: QuestionListVO) {
  router.push({
    name: AdminRouteName.QUESTION_EDIT,
    params: { id: String(row.id) },
    query: { redirect: route.fullPath },
  })
}

/* -------- 删除 -------- */
async function handleDelete(row: QuestionListVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除该题目吗？题干：${(row.content ?? '').slice(0, 30)}...`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger' },
    )
  } catch {
    return
  }
  await deleteQuestionAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}
</script>
