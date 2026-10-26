<template>
  <div class="dialogue-version-manage flex h-full flex-col">
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="来源类型">
        <el-select v-model="query.sourceType" placeholder="全部" style="width: 140px" clearable>
          <el-option
            v-for="opt in SOURCE_TYPE_FILTER_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="语言">
        <el-select v-model="query.language" placeholder="全部" style="width: 120px" clearable>
          <el-option
            v-for="opt in VERSION_LANGUAGE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="格式">
        <el-select v-model="query.format" placeholder="全部" style="width: 120px" clearable>
          <el-option
            v-for="opt in VERSION_FORMAT_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="范围">
        <el-select v-model="query.scope" placeholder="全部" style="width: 120px" clearable>
          <el-option
            v-for="opt in VERSION_SCOPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" style="width: 120px" clearable>
          <el-option
            v-for="opt in STATUS_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
    </TableToolbar>

    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id">
        <!-- 来源 -->
        <template #source="{ row }">
          <div class="min-w-0 leading-tight">
            <div class="flex items-center gap-1">
              <el-tag effect="plain" size="small" type="info">
                {{ row.sourceTypeLabel || sourceTypeLabel(row.sourceType) }}
              </el-tag>
              <span class="truncate font-medium">
                {{ sourceTitle(row) }}
              </span>
            </div>
            <div v-if="sourceSub(row)" class="mt-0.5 truncate text-xs text-slate-400">
              {{ sourceSub(row) }}
            </div>
          </div>
        </template>

        <!-- 规格 -->
        <template #spec="{ row }">
          <span class="text-sm">
            {{
              row.label ||
              [row.languageLabel, row.formatLabel, row.scopeLabel].filter(Boolean).join(' · ')
            }}
          </span>
        </template>

        <!-- 状态 -->
        <template #status="{ row }">
          <el-tag :type="getStatusTagType(row.status)" effect="plain">
            {{ row.statusLabel ?? statusLabel(row.status) }}
          </el-tag>
        </template>

        <!-- 操作 -->
        <template #action="{ row }">
          <el-button
            v-if="row.status === STATUS.PENDING"
            size="small"
            type="warning"
            link
            @click="handleReview(row)"
          >
            审核
          </el-button>
          <el-button size="small" type="primary" link @click="handleOpen(row)">查看</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </ProTable>
    </el-card>

    <ReviewDialogueVersionDialog ref="reviewDialogRef" @success="handleReviewSuccess" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteDialogueVersionAPI, pageDialogueVersionsAPI } from '@/api/dialogueVersion'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import {
  SOURCE_TYPE,
  SOURCE_TYPE_FILTER_OPTIONS,
  sourceTypeLabel,
  STATUS,
  STATUS_OPTIONS,
  statusLabel,
  UserRouteName,
  VERSION_FORMAT_OPTIONS,
  VERSION_LANGUAGE_OPTIONS,
  VERSION_SCOPE_OPTIONS,
} from '@/constants'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import { getStatusTagType } from '@/utils'
import ReviewDialogueVersionDialog from './components/ReviewDialogueVersionDialog.vue'

const router = useRouter()

const tableRef = ref<ProTableExpose<DialogueVersionVO>>()
const reviewDialogRef = ref<InstanceType<typeof ReviewDialogueVersionDialog> | null>(null)

const query = reactive<{
  sourceType?: number
  language?: number
  format?: number
  scope?: number
  status?: number
}>({
  sourceType: undefined,
  language: undefined,
  format: undefined,
  scope: undefined,
  status: undefined,
})

const columns: ProTableColumn<DialogueVersionVO>[] = [
  { label: '来源', minWidth: 260, slot: 'source' },
  { label: '规格', minWidth: 180, slot: 'spec' },
  { label: '状态', width: 100, align: 'center', slot: 'status' },
  {
    label: '创建时间',
    prop: 'createdAt',
    width: 170,
    align: 'center',
    showOverflowTooltip: true,
  },
  {
    label: '操作',
    width: 180,
    align: 'center',
    fixed: 'right',
    slot: 'action',
    displayFlex: true,
  },
]

/* -------- 来源展示辅助 -------- */
function sourceTitle(row: DialogueVersionVO): string {
  const src = row.source
  if (!src) return row.sourceTypeLabel || `#${row.sourceId}`
  if (row.sourceType === SOURCE_TYPE.STORY) {
    return src.title || `剧情 #${src.sourceId}`
  }
  return src.title || `卡面 #${src.cardId}`
}

function sourceSub(row: DialogueVersionVO): string {
  const src = row.source
  if (!src) return ''
  if (row.sourceType === SOURCE_TYPE.STORY) {
    return src.subtitle || ''
  }
  const parts: string[] = []
  if (src.subtitle) parts.push(`[${src.subtitle}]`)
  if (src.episodeNo != null) {
    parts.push(`第${src.episodeNo}话${src.episodeTitle ? ` · ${src.episodeTitle}` : ''}`)
  }
  return parts.join(' ')
}

/* -------- 请求适配 -------- */
async function fetchList(
  params: { pageNum: number; pageSize: number } & typeof query,
): Promise<PageResult<DialogueVersionVO>> {
  const res = await pageDialogueVersionsAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    sourceType: query.sourceType ?? undefined,
    language: query.language ?? undefined,
    format: query.format ?? undefined,
    scope: query.scope ?? undefined,
    status: query.status ?? undefined,
  })
}

function handleReset() {
  query.sourceType = undefined
  query.language = undefined
  query.format = undefined
  query.scope = undefined
  query.status = undefined
  tableRef.value?.reset()
}

/* -------- 审核 -------- */
function handleReview(row: DialogueVersionVO) {
  reviewDialogRef.value?.open(row)
}

async function handleReviewSuccess() {
  await tableRef.value?.refresh()
}

/* -------- 查看：新窗口打开用户端浏览页 -------- */
function handleOpen(row: DialogueVersionVO) {
  const src = row.source
  if (!src || src.sourceId == null || row.sourceType == null) {
    ElMessage.warning('来源信息缺失，无法查看')
    return
  }

  // 剧情：需要 categoryType 拼路由参数
  if (row.sourceType === SOURCE_TYPE.STORY) {
    if (src.categoryType == null) {
      ElMessage.warning('剧情分类信息缺失，无法跳转')
      return
    }
    const { href } = router.resolve({
      name: UserRouteName.STORY_BROWSE_DETAIL,
      params: {
        type: String(src.categoryType),
        kind: 'story',
        id: String(src.sourceId),
      },
      query: { versionId: String(row.id) },
    })
    window.open(href, '_blank')
    return
  }

  // 卡面话数：需要 cardId
  if (src.cardId == null) {
    ElMessage.warning('该话数所属卡面信息缺失')
    return
  }
  const routeName =
    row.sourceType === SOURCE_TYPE.RC
      ? UserRouteName.CARD_RC_BROWSE
      : row.sourceType === SOURCE_TYPE.RTV
        ? UserRouteName.CARD_RTV_BROWSE
        : UserRouteName.CARD_RABITTER_BROWSE

  const { href } = router.resolve({
    name: routeName,
    query: {
      cardId: String(src.cardId),
      sourceId: String(src.sourceId),
      versionId: String(row.id),
    },
  })
  window.open(href, '_blank')
}

/* -------- 删除 -------- */
async function handleDelete(row: DialogueVersionVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除版本 #${row.id} 吗？删除后该版本下的所有对话内容将一并清除。`,
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

  await deleteDialogueVersionAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}
</script>
