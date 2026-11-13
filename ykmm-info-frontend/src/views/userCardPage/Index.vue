<template>
  <section class="card-list h-full flex flex-col">
    <!-- 标题 + 搜索 -->
    <div class="mb-5 flex flex-wrap items-center justify-between gap-3 shrink-0">
      <div class="flex items-center gap-3">
        <h1 class="text-xl font-semibold">卡面</h1>
        <!-- 卡片大小切换 -->
        <el-radio-group v-model="cardSize" size="small">
          <el-radio-button value="small">
            <el-icon><Grid /></el-icon>
          </el-radio-button>
          <el-radio-button value="normal">
            <el-icon><Menu /></el-icon>
          </el-radio-button>
          <el-radio-button value="big">
            <el-icon><FullScreen /></el-icon>
          </el-radio-button>
        </el-radio-group>
      </div>
      <el-input
        v-model="keyword"
        :prefix-icon="Search"
        placeholder="搜索卡面"
        style="width: 320px"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-button v-if="isPassedUser" type="primary" @click="handleCreate">
        <el-icon class="mr-1"><Plus /></el-icon>
        新增卡面
      </el-button>
    </div>

    <!-- 筛选行 -->
    <div class="mb-5 flex flex-col gap-3 shrink-0">
      <!-- 主筛选行：原四个 + 右侧「更多」按钮 -->
      <div class="flex items-center gap-3 col-4 shrink-0">
        <el-select
          v-model="filter.personIds"
          :max-collapse-tags="2"
          class="w-64"
          placeholder="人物"
          clearable
          collapse-tags
          collapse-tags-tooltip
          filterable
          multiple
        >
          <el-option
            v-for="p in personStore.persons"
            :key="p.id"
            :label="p.nameCn || p.nameJp || `#${p.id}`"
            :value="p.id!"
          />
        </el-select>

        <el-select v-model="filter.seriesId" class="w-48" placeholder="系列" clearable filterable>
          <el-option v-for="s in seriesOptions" :key="s.id" :label="s.name" :value="s.id!" />
        </el-select>

        <el-select v-model="filter.maxRarity" class="w-32" placeholder="最高等级" clearable>
          <el-option
            v-for="opt in CARD_MAX_RARITY_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>

        <!-- 右侧：更多 -->
        <div class="ml-auto">
          <el-button text @click="showMore = !showMore">
            {{ showMore ? '收起' : '更多' }}
            <el-icon
              :class="{ 'rotate-180': showMore }"
              class="ml-1 transition-transform duration-200"
            >
              <ArrowDown />
            </el-icon>
          </el-button>
        </div>
      </div>

      <!-- 更多筛选（折叠） -->
      <el-collapse-transition>
        <div v-show="showMore" class="flex items-center gap-3 col-4 shrink-0">
          <el-select v-model="filter.attribute" class="w-32" placeholder="属性" clearable>
            <el-option
              v-for="opt in CARD_ATTRIBUTE_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>

          <el-select v-model="filter.category" class="w-44" placeholder="类别" clearable filterable>
            <el-option
              v-for="c in categoryOptions"
              :key="c.id"
              :label="c.name ?? `#${c.id}`"
              :value="c.id!"
            />
          </el-select>

          <el-select
            v-model="filter.attachedStoryType"
            class="w-36"
            placeholder="附属剧情"
            clearable
          >
            <el-option
              v-for="opt in SOURCE_TYPE_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>

          <el-select v-model="filter.costumeType" class="w-36" placeholder="服装类型" clearable>
            <el-option
              v-for="opt in CARD_COSTUME_TYPE_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </div>
      </el-collapse-transition>
    </div>

    <!-- 卡片网格：无限滚动 -->
    <div
      ref="scrollContainerRef"
      v-infinite-scroll="load"
      v-loading="loading && !list.length"
      :infinite-scroll-disabled="disabled || loading"
      :infinite-scroll-distance="preloadDistance"
      class="min-h-40 flex-1 overflow-y-auto"
    >
      <div v-if="list.length" :class="['grid gap-4', gridClass]">
        <article
          v-for="item in list"
          :key="item.id"
          class="group cursor-pointer overflow-hidden rounded-xl bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
          @click="handleDetail(item)"
        >
          <div class="aspect-3/4 overflow-hidden bg-slate-100 relative">
            <img
              v-if="getCoverImage(item)"
              :alt="item.name"
              :src="getCoverImage(item)"
              class="h-full w-full object-cover transition duration-300 group-hover:scale-105"
            />
            <div v-else class="h-full w-full bg-linear-to-br from-slate-200 to-slate-300" />
            <!-- 状态角标 -->
            <el-tag
              v-if="item.status === STATUS.PENDING || item.status === STATUS.REJECTED"
              :type="STATUS_TAG_TYPE[item.status]"
              class="absolute right-2 top-2"
              effect="dark"
              size="small"
            >
              {{ STATUS_LABEL[item.status] }}
            </el-tag>
          </div>
          <div class="p-3">
            <div class="flex items-center justify-between gap-1 truncate">
              <div class="flex items-center gap-1 truncate">
                <el-tag
                  :type="item.maxRarity === CARD_MAX_RARITY.UR ? 'danger' : 'warning'"
                  effect="plain"
                  size="small"
                >
                  {{ item.maxRarityLabel ?? cardMaxRarityLabel(item.maxRarity) }}
                </el-tag>
                <p class="truncate font-medium">{{ item.name || '-' }}</p>
              </div>
              <el-tag
                v-if="item.attribute"
                :style="getAttributeTagStyle(item.attribute)"
                effect="plain"
                size="small"
              >
                {{ item.attributeLabel ?? cardAttributeLabel(item.attribute) }}
              </el-tag>
              <span v-else class="text-slate-300">-</span>
            </div>
            <p class="mt-1 truncate text-xs text-slate-500">{{ item.seriesName || '-' }}</p>
          </div>
        </article>
      </div>

      <!-- 空状态 -->
      <p v-else-if="!loading" class="py-16 text-center text-sm text-slate-400">没有找到相关卡面</p>

      <!-- 加载中 / 没有更多 -->
      <div v-if="loading && list.length" class="py-6 text-center text-sm text-slate-400">
        加载中...
      </div>
      <div v-else-if="disabled && list.length" class="py-6 text-center text-sm text-slate-400">
        没有更多了
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ArrowDown, FullScreen, Grid, Menu, Plus, Search } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { pageCardAPI } from '@/api/card'
import { listCardCategoryOptionsAPI } from '@/api/cardCategory'
import { listCardSeriesOptionsAPI } from '@/api/cardSeries'
import {
  CARD_ATTRIBUTE_OPTIONS,
  CARD_COSTUME_TYPE_OPTIONS,
  CARD_MAX_RARITY,
  CARD_MAX_RARITY_OPTIONS,
  cardAttributeLabel,
  cardMaxRarityLabel,
  SOURCE_TYPE_OPTIONS,
  STATUS,
  STATUS_LABEL,
  STATUS_TAG_TYPE,
  UserRouteName,
} from '@/constants'
import { usePersonStore, useUserStore } from '@/stores'
import type { CardVO } from '@/types/card'
import type { CardCategoryVO } from '@/types/cardCategory'
import type { CardSeriesVO } from '@/types/cardSeries'
import { getAttributeTagStyle, getCoverImage } from '@/utils'

const route = useRoute()
const router = useRouter()

const personStore = usePersonStore()
const { isPassedUser } = useUserStore()

/* ============================================================
 *  卡片大小（本地偏好，不进 URL）
 * ============================================================ */
type CardSize = 'small' | 'normal' | 'big'

const CARD_SIZE_STORAGE_KEY = 'user-card-size'

const cardSize = ref<CardSize>(
  (localStorage.getItem(CARD_SIZE_STORAGE_KEY) as CardSize) || 'normal',
)

watch(cardSize, (v) => {
  localStorage.setItem(CARD_SIZE_STORAGE_KEY, v)
})

const gridClass = computed(() => {
  switch (cardSize.value) {
    case 'small':
      return 'grid-cols-3 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-8 xl:grid-cols-10'
    case 'big':
      return 'grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4'
    case 'normal':
    default:
      return 'grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5'
  }
})

/* ============================================================
 *  筛选条件 <-> URL query 双向同步
 * ============================================================ */

function readStr(v: unknown): string {
  return typeof v === 'string' ? v : ''
}

function readNum(v: unknown): number | undefined {
  if (v === undefined || v === null || v === '') return undefined
  const n = Number(v)
  // 改成 n >= 0：允许 0（"无"），对 category / maxRarity 等 1 起字段无影响
  return Number.isFinite(n) && n >= 0 ? n : undefined
}

/** 返回 undefined：URL 没传该参数；返回 []：用户主动清空 */
function readIds(v: unknown): number[] | undefined {
  if (v === undefined) return undefined
  if (Array.isArray(v)) {
    return v.map((x) => Number(x)).filter((n) => Number.isFinite(n))
  }
  if (typeof v === 'string') {
    if (v === '') return []
    return v
      .split(',')
      .map((s) => Number(s.trim()))
      .filter((n) => Number.isFinite(n))
  }
  return undefined
}

const initialPersonIds = readIds(route.query.personIds)

const keyword = ref(readStr(route.query.keyword))
const filter = reactive<{
  seriesId?: number
  category?: number
  maxRarity?: number
  attribute?: number
  attachedStoryType?: number
  costumeType?: number
  personIds: number[]
}>({
  seriesId: readNum(route.query.seriesId),
  category: readNum(route.query.category),
  maxRarity: readNum(route.query.maxRarity),
  attribute: readNum(route.query.attribute),
  attachedStoryType: readNum(route.query.attachedStoryType),
  costumeType: readNum(route.query.costumeType),
  personIds: initialPersonIds ?? [],
})

/** 更多筛选面板展开状态（首次进入时，若 URL 带了更多筛选则自动展开） */
const showMore = ref(
  filter.category !== undefined ||
    filter.attachedStoryType !== undefined ||
    filter.costumeType !== undefined,
)

/** 标记 query 是否由本组件主动 replace，避免 route watch 重复同步 */
let selfPushing = false

/**
 * 把当前筛选条件写回 URL。
 * @returns 是否真的产生了变化（true 表示已 replace）
 */
function writeToUrl(): boolean {
  const query: Record<string, string> = {}
  const kw = keyword.value.trim()
  if (kw) query.keyword = kw
  if (filter.seriesId !== undefined) query.seriesId = String(filter.seriesId)
  if (filter.category !== undefined) query.category = String(filter.category)
  if (filter.maxRarity !== undefined) query.maxRarity = String(filter.maxRarity)
  if (filter.attribute !== undefined) query.attribute = String(filter.attribute)
  if (filter.attachedStoryType !== undefined)
    query.attachedStoryType = String(filter.attachedStoryType)
  if (filter.costumeType !== undefined) query.costumeType = String(filter.costumeType)
  if (filter.personIds.length) query.personIds = filter.personIds.join(',')

  const current = route.query as Record<string, unknown>
  const keys = new Set([...Object.keys(query), ...Object.keys(current)])
  for (const k of keys) {
    const a = query[k]
    const b = current[k]
    const aStr = a === undefined ? '' : String(a)
    const bStr = b === undefined ? '' : String(b)
    if (aStr !== bStr) {
      selfPushing = true
      router.replace({ query })
      return true
    }
  }
  return false
}

function readFromQuery(q: Record<string, unknown>) {
  return {
    keyword: readStr(q.keyword),
    seriesId: readNum(q.seriesId),
    category: readNum(q.category),
    maxRarity: readNum(q.maxRarity),
    attribute: readNum(q.attribute),
    attachedStoryType: readNum(q.attachedStoryType),
    costumeType: readNum(q.costumeType),
    personIds: readIds(q.personIds) ?? [],
  }
}

// URL 变化 → 同步 UI（前进/后退时）+ 统一触发加载
watch(
  () => route.query,
  (q) => {
    const fromSelf = selfPushing
    selfPushing = false

    if (!fromSelf) {
      const parsed = readFromQuery(q as Record<string, unknown>)
      keyword.value = parsed.keyword
      filter.seriesId = parsed.seriesId
      filter.category = parsed.category
      filter.maxRarity = parsed.maxRarity
      filter.attribute = parsed.attribute
      filter.attachedStoryType = parsed.attachedStoryType
      filter.costumeType = parsed.costumeType
      filter.personIds = parsed.personIds

      // ★ 前进/后退若带更多筛选，自动展开面板
      if (
        parsed.category !== undefined ||
        parsed.attachedStoryType !== undefined ||
        parsed.costumeType !== undefined
      ) {
        showMore.value = true
      }
    }

    void resetAndLoad()
  },
)

// filter 变化 → 只写 URL，加载由 route watch 统一触发
watch(
  filter,
  () => {
    writeToUrl()
  },
  { deep: true },
)

/* ============================================================
 *  列表 / 无限滚动
 * ============================================================ */
const loading = ref(false)
const disabled = ref(false)
const list = ref<CardVO[]>([])
const pageNum = ref(1)
const pageSize = ref(20)

const seriesOptions = ref<CardSeriesVO[]>([])
const categoryOptions = ref<CardCategoryVO[]>([])

async function loadList() {
  loading.value = true
  try {
    const res = await pageCardAPI({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      seriesId: filter.seriesId,
      category: filter.category,
      maxRarity: filter.maxRarity,
      attribute: filter.attribute,
      attachedStoryType: filter.attachedStoryType,
      costumeType: filter.costumeType,
      personIds: filter.personIds.length ? filter.personIds : undefined,
    })
    const records = res.data.records ?? []
    list.value = records
    disabled.value = records.length < pageSize.value
  } catch {
    list.value = []
    disabled.value = true
  } finally {
    loading.value = false
  }
}

async function load() {
  if (loading.value || disabled.value) return

  loading.value = true
  pageNum.value++

  try {
    const res = await pageCardAPI({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      seriesId: filter.seriesId,
      category: filter.category,
      maxRarity: filter.maxRarity,
      attribute: filter.attribute,
      attachedStoryType: filter.attachedStoryType,
      costumeType: filter.costumeType,
      personIds: filter.personIds.length ? filter.personIds : undefined,
    })
    const records = res.data.records ?? []

    if (records.length === 0) {
      disabled.value = true
      return
    }

    list.value.push(...records)

    if (records.length < pageSize.value) {
      disabled.value = true
    }
  } catch {
    disabled.value = true
  } finally {
    loading.value = false
  }
}

async function resetAndLoad() {
  pageNum.value = 1
  list.value = []
  disabled.value = false
  loading.value = false

  await loadList() // 第 1 页
  if (!disabled.value) {
    await load() // 第 2 页
  }
}

const scrollContainerRef = ref<HTMLElement | null>(null)

/**
 * 触发加载的「提前量」。
 * <p>设为「一屏高度」：用户距底还有一屏未读时，就开始加载下一页，
 * 保证滚到当前页尾部时下一页已经就绪，无空缺。
 * <p>想更提前可改成 `el.clientHeight * 1.5` 或 `* 2`。
 */
const preloadDistance = ref(800)

function updatePreloadDistance() {
  const el = scrollContainerRef.value
  if (!el) return
  preloadDistance.value = el.clientHeight
}

/* ============================================================
 *  事件
 * ============================================================ */

function handleSearch() {
  // keyword 变了 → URL 会被 replace，route watch 会自动加载
  // keyword 没变 → 手动强制刷新
  const changed = writeToUrl()
  if (!changed) void resetAndLoad()
}

function handleCreate() {
  router.push({
    name: UserRouteName.CARD_CREATE,
    query: { redirect: '/card' },
  })
}

function handleDetail(row: CardVO) {
  router.push({ name: UserRouteName.CARD_DETAIL, params: { id: String(row.id) } })
}

onMounted(async () => {
  void resetAndLoad() // 首屏 2 页

  await nextTick()
  updatePreloadDistance() // 测量一屏高度
  window.addEventListener('resize', updatePreloadDistance)

  const [seriesRes, categoryRes] = await Promise.all([
    listCardSeriesOptionsAPI(),
    listCardCategoryOptionsAPI(),
  ])
  seriesOptions.value = seriesRes.data ?? []
  categoryOptions.value = categoryRes.data ?? []
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updatePreloadDistance)
})
</script>
