<template>
  <section class="card-list">
    <!-- 标题 + 搜索 -->
    <div class="mb-5 flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-xl font-semibold">卡面</h1>
      <input
        v-model="keyword"
        class="w-56 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm outline-none transition focus:border-(--el-color-primary) focus:ring-2 focus:ring-(--el-color-primary-light-8)"
        placeholder="搜索卡面 / 系列"
        type="search"
        @keyup.enter="handleSearch"
      />
    </div>

    <!-- 筛选行 -->
    <div class="mb-5 flex items-center gap-3 col-4">
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
        @change="handleFilterChange"
      >
        <el-option
          v-for="p in personStore.persons"
          :key="p.id"
          :label="p.nameCn || p.nameJp || `#${p.id}`"
          :value="p.id!"
        />
      </el-select>

      <el-select
        v-model="filter.seriesId"
        class="w-48"
        placeholder="系列"
        clearable
        filterable
        @change="handleFilterChange"
      >
        <el-option v-for="s in seriesOptions" :key="s.id" :label="s.name" :value="s.id!" />
      </el-select>

      <el-select
        v-model="filter.maxRarity"
        class="w-32"
        placeholder="等级"
        clearable
        @change="handleFilterChange"
      >
        <el-option
          v-for="opt in CARD_MAX_RARITY_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>

      <el-select
        v-model="filter.attribute"
        class="w-32"
        placeholder="属性"
        clearable
        @change="handleFilterChange"
      >
        <el-option
          v-for="opt in CARD_ATTRIBUTE_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
    </div>

    <!-- 卡片网格：无限滚动 -->
    <div
      v-infinite-scroll="load"
      v-loading="loading && !list.length"
      :infinite-scroll-disabled="disabled || loading"
      :infinite-scroll-distance="80"
      class="min-h-40"
    >
      <div
        v-if="list.length"
        class="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5"
      >
        <article
          v-for="item in list"
          :key="item.id"
          class="group cursor-pointer overflow-hidden rounded-xl bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
          @click="handleDetail(item)"
        >
          <div class="aspect-3/4 overflow-hidden bg-slate-100">
            <img
              v-if="getCoverImage(item)"
              :alt="item.name"
              :src="getCoverImage(item)"
              class="h-full w-full object-cover transition duration-300 group-hover:scale-105"
            />
            <div v-else class="h-full w-full bg-linear-to-br from-slate-200 to-slate-300" />
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
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { pageUserCardAPI } from '@/api/card'
import { listCardSeriesOptionsAPI } from '@/api/cardSeries'
import {
  CARD_ATTRIBUTE_OPTIONS,
  CARD_MAX_RARITY,
  CARD_MAX_RARITY_OPTIONS,
  cardAttributeLabel,
  cardMaxRarityLabel,
} from '@/constants'
import { usePersonStore } from '@/stores/personStore'
import type { CardVO } from '@/types/card'
import type { CardSeriesVO } from '@/types/cardSeries'
import { getAttributeTagStyle, getCoverImage } from '@/utils'

const router = useRouter()

const keyword = ref('')
const filter = reactive<{
  seriesId?: number
  maxRarity?: number
  attribute?: number
  personIds: number[]
}>({
  seriesId: undefined,
  maxRarity: undefined,
  attribute: undefined,
  personIds: [11, 12], // 默认百和千
})

const personStore = usePersonStore()

/* -------- 无限滚动状态 -------- */
const loading = ref(false) // 加载锁
const disabled = ref(false) // 没有更多了
const list = ref<CardVO[]>([])
const pageNum = ref(1)
const pageSize = ref(20)

const seriesOptions = ref<CardSeriesVO[]>([])

/* -------- 首次加载 / 重置后加载 -------- */
async function loadList() {
  loading.value = true
  try {
    const res = await pageUserCardAPI({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      seriesId: filter.seriesId,
      maxRarity: filter.maxRarity,
      attribute: filter.attribute,
      personIds: filter.personIds.length ? filter.personIds : undefined,
    })
    const records = res.data.records ?? []
    list.value = records
    // 首页数据不足 pageSize → 没有更多
    disabled.value = records.length < pageSize.value
  } catch {
    list.value = []
    disabled.value = true
  } finally {
    loading.value = false
  }
}

/* -------- 无限滚动触发 -------- */
async function load() {
  if (loading.value || disabled.value) return

  loading.value = true
  pageNum.value++

  try {
    const res = await pageUserCardAPI({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      seriesId: filter.seriesId,
      maxRarity: filter.maxRarity,
      attribute: filter.attribute,
    })
    const records = res.data.records ?? []

    if (records.length === 0) {
      disabled.value = true
      return
    }

    list.value.push(...records)

    // 本次返回不足 pageSize → 没有更多
    if (records.length < pageSize.value) {
      disabled.value = true
    }
  } catch {
    disabled.value = true
  } finally {
    loading.value = false
  }
}

/* -------- 条件变化：重置并重新加载 -------- */
function resetAndLoad() {
  pageNum.value = 1
  list.value = []
  disabled.value = false
  loading.value = false
  void loadList()
}

function handleSearch() {
  resetAndLoad()
}

function handleFilterChange() {
  resetAndLoad()
}

function handleDetail(row: CardVO) {
  router.push({ name: 'UserCardDetail', params: { id: String(row.id) } })
}

onMounted(async () => {
  void loadList()
  const res = await listCardSeriesOptionsAPI()
  seriesOptions.value = res.data ?? []
})
</script>
