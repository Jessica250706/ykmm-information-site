<template>
  <div class="card-manage flex h-full flex-col">
    <!-- 顶部操作栏 -->
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="卡面名称"
          style="width: 180px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="系列">
        <el-select
          v-model="query.seriesId"
          placeholder="全部"
          style="width: 180px"
          clearable
          filterable
        >
          <el-option v-for="s in seriesOptions" :key="s.id" :label="s.name" :value="s.id!" />
        </el-select>
      </el-form-item>

      <el-form-item label="最高等级">
        <el-select v-model="query.maxRarity" placeholder="全部" style="width: 120px" clearable>
          <el-option
            v-for="opt in CARD_MAX_RARITY_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="属性">
        <el-select v-model="query.attribute" placeholder="全部" style="width: 120px" clearable>
          <el-option
            v-for="opt in CARD_ATTRIBUTE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="人物">
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

      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增卡面
        </el-button>
      </template>
    </TableToolbar>

    <!-- 表格 -->
    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id" stripe>
        <!-- 封面图 -->
        <template #cover="{ row }">
          <el-image
            v-if="getCoverImage(row)"
            :preview-src-list="getImageUrls(row)"
            :src="getCoverImage(row)"
            fit="cover"
            style="width: 60px; height: 60px; border-radius: 4px"
            preview-teleported
          />
          <span v-else class="text-slate-300">-</span>
        </template>

        <!-- 名称 -->
        <template #name="{ row }">
          <div class="leading-tight flex items-center">
            <div class="font-medium">{{ row.name || '-' }}</div>
            <div v-if="row.seriesName">[{{ row.seriesName }}]</div>
          </div>
        </template>

        <!-- 最高等级 -->
        <template #maxRarity="{ row }">
          <el-tag
            :type="row.maxRarity === CARD_MAX_RARITY.UR ? 'danger' : 'warning'"
            effect="plain"
          >
            {{ row.maxRarityLabel ?? cardMaxRarityLabel(row.maxRarity) }}
          </el-tag>
        </template>

        <!-- 属性 -->
        <template #attribute="{ row }">
          <el-tag v-if="row.attribute" :type="cardAttributeTagType(row.attribute)" effect="plain">
            {{ row.attributeLabel ?? cardAttributeLabel(row.attribute) }}
          </el-tag>
          <span v-else class="text-slate-300">-</span>
        </template>

        <!-- 人物 -->
        <template #persons="{ row }">
          <div v-if="row.persons?.length" class="flex flex-wrap justify-center gap-1">
            <el-tag
              v-for="p in row.persons"
              :key="p.personId"
              :style="personTagStyle(p.themeColor)"
              effect="plain"
              size="small"
            >
              {{ p.nameCn }}
            </el-tag>
          </div>
          <span v-else class="text-slate-300">-</span>
        </template>

        <!-- 附属剧情 -->
        <template #attachedStoryType="{ row }">
          <el-tag
            v-if="row.attachedStoryType"
            :type="attachedStoryTypeTag(row.attachedStoryType)"
            effect="plain"
            size="small"
          >
            {{ row.attachedStoryTypeLabel ?? cardAttachedStoryTypeLabel(row.attachedStoryType) }}
          </el-tag>
          <span v-else class="text-slate-300">无</span>
        </template>

        <!-- 状态 -->
        <template #status="{ row }">
          <el-tag :type="statusTagType(row.status)" effect="plain">
            {{ row.statusLabel ?? cardStatusLabel(row.status) }}
          </el-tag>
        </template>

        <!-- 操作 -->
        <template #action="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-dropdown trigger="click">
            <el-button size="small" type="primary" link>
              更多
              <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleManageRc(row)">管理 RC</el-dropdown-item>
                <el-dropdown-item @click="handleManageRtv(row)">管理 RTV</el-dropdown-item>
                <el-dropdown-item divided @click="handleDelete(row)">删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </ProTable>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ArrowDown, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteCardAPI, pageCardAPI } from '@/api/card'
import { listCardSeriesOptionsAPI } from '@/api/cardSeries'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import {
  CARD_ATTACHED_STORY_TYPE,
  CARD_ATTRIBUTE,
  CARD_ATTRIBUTE_OPTIONS,
  CARD_IMAGE_TYPE,
  CARD_MAX_RARITY,
  CARD_MAX_RARITY_OPTIONS,
  CARD_STATUS,
  cardAttachedStoryTypeLabel,
  cardAttributeLabel,
  cardMaxRarityLabel,
  cardStatusLabel,
} from '@/constants/card'
import { usePersonStore } from '@/stores/personStore'
import type { CardPageQueryDTO, CardVO } from '@/types/card'
import type { CardSeriesVO } from '@/types/cardSeries'
import { personTagStyle } from '@/utils'

const router = useRouter()
const personStore = usePersonStore()

type TableInstance = ProTableExpose<CardVO>
const tableRef = ref<TableInstance>()

const query = reactive<{
  keyword?: string
  seriesId?: number
  maxRarity?: number
  attribute?: number
  personId?: number
}>({
  keyword: '',
  seriesId: undefined,
  maxRarity: undefined,
  attribute: undefined,
  personId: undefined,
})

const seriesOptions = ref<CardSeriesVO[]>([])

const columns: ProTableColumn<CardVO>[] = [
  { label: '封面', width: 100, align: 'center', slot: 'cover', fixed: 'left' },
  { label: '名称', minWidth: 200, slot: 'name', fixed: 'left' },
  { label: '最高等级', width: 110, align: 'center', slot: 'maxRarity' },
  { label: '属性', width: 110, align: 'center', slot: 'attribute' },
  { label: '人物', minWidth: 200, align: 'center', slot: 'persons' },
  { label: '附属剧情', width: 120, align: 'center', slot: 'attachedStoryType' },
  { label: '状态', width: 100, align: 'center', slot: 'status' },
  {
    label: '首次入池',
    prop: 'firstPoolTime',
    width: 160,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', width: 180, align: 'center', fixed: 'right', slot: 'action' },
]

/* -------- 按 maxRarity 决定封面 -------- */
function getCoverImage(row: CardVO): string | undefined {
  const images = row.images ?? []
  const byType = (t: number) => images.find((i) => i.imageType === t)?.url

  if (row.maxRarity === CARD_MAX_RARITY.UR) {
    // UR：优先竖卡 → 横卡
    return byType(CARD_IMAGE_TYPE.UR_VERTICAL) ?? byType(CARD_IMAGE_TYPE.UR_HORIZONTAL)
  }
  // 非 UR：优先普通 SSR → SSR隐藏款 → SR
  return (
    byType(CARD_IMAGE_TYPE.SSR) ?? byType(CARD_IMAGE_TYPE.SSR_HIDDEN) ?? byType(CARD_IMAGE_TYPE.SR)
  )
}

function getImageUrls(row: CardVO): string[] {
  return (row.images ?? []).map((i) => i.url!).filter((u) => !!u)
}

/* -------- tag 类型 -------- */
function cardAttributeTagType(attr: number): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  switch (attr) {
    case CARD_ATTRIBUTE.SHOUT:
      return 'danger'
    case CARD_ATTRIBUTE.BEAT:
      return 'success'
    case CARD_ATTRIBUTE.MELODY:
      return 'primary'
    default:
      return 'info'
  }
}

function attachedStoryTypeTag(type: number): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  switch (type) {
    case CARD_ATTACHED_STORY_TYPE.RC:
      return 'success'
    case CARD_ATTACHED_STORY_TYPE.RTV:
      return 'warning'
    case CARD_ATTACHED_STORY_TYPE.RABBITTER:
      return 'info'
    default:
      return 'info'
  }
}

function statusTagType(status?: number): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  switch (status) {
    case CARD_STATUS.PUBLISHED:
      return 'success'
    case CARD_STATUS.PENDING:
      return 'warning'
    case CARD_STATUS.REJECTED:
      return 'danger'
    default:
      return 'info'
  }
}

/* -------- 请求适配 -------- */
async function fetchList(
  params: CardPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<CardVO>> {
  const res = await pageCardAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    keyword: query.keyword || undefined,
    seriesId: query.seriesId ?? undefined,
    maxRarity: query.maxRarity ?? undefined,
    attribute: query.attribute ?? undefined,
    personId: query.personId ?? undefined,
  })
}

function handleReset() {
  query.keyword = ''
  query.seriesId = undefined
  query.maxRarity = undefined
  query.attribute = undefined
  query.personId = undefined
  tableRef.value?.reset()
}

/* -------- 新增 / 编辑 -------- */
function handleCreate() {
  router.push({ name: 'AdminCardCreate' })
}

function handleEdit(row: CardVO) {
  router.push({
    name: 'AdminCardEdit',
    params: { id: String(row.id) },
  })
}

/** 管理 RC */
function handleManageRc(row: CardVO) {
  router.push({
    name: 'AdminCardRcManage',
    query: { cardId: String(row.id) },
  })
}

/** 管理 RTV */
function handleManageRtv(row: CardVO) {
  router.push({
    name: 'AdminCardRtvManage',
    query: { cardId: String(row.id) },
  })
}

/* -------- 删除 -------- */
async function handleDelete(row: CardVO) {
  try {
    await ElMessageBox.confirm(`确定要删除卡面「${row.name ?? row.id}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger',
    })
  } catch {
    return
  }

  await deleteCardAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}

/* -------- 初始化 -------- */
onMounted(async () => {
  void personStore.loadAll()
  const res = await listCardSeriesOptionsAPI()
  seriesOptions.value = res.data ?? []
})
</script>
