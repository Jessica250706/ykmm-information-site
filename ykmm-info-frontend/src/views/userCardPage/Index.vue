<template>
  <section class="card-list">
    <!-- 标题 + 搜索 -->
    <div class="mb-5 flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-xl font-semibold">卡面</h1>
      <input
        v-model="keyword"
        class="w-56 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm outline-none transition focus:border-[var(--el-color-primary)] focus:ring-2 focus:ring-[var(--el-color-primary-light-8)]"
        placeholder="搜索卡面 / 系列"
        type="search"
        @keyup.enter="handleSearch"
      />
    </div>

    <!-- 筛选行 -->
    <div class="mb-5 flex flex-wrap items-center gap-3">
      <el-select
        v-model="filter.seriesId"
        class="w-48"
        placeholder="系列"
        clearable
        filterable
        @change="handleSearch"
      >
        <el-option v-for="s in seriesOptions" :key="s.id" :label="s.name" :value="s.id!" />
      </el-select>

      <el-select
        v-model="filter.maxRarity"
        class="w-32"
        placeholder="等级"
        clearable
        @change="handleSearch"
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
        @change="handleSearch"
      >
        <el-option
          v-for="opt in CARD_ATTRIBUTE_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
    </div>

    <!-- 卡片网格 -->
    <div v-loading="loading">
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
            <p class="mt-1 truncate text-xs text-slate-500">{{ item.seriesName || '-' }}</p>
          </div>
        </article>
      </div>

      <p v-else-if="!loading" class="py-16 text-center text-sm text-slate-400">没有找到相关卡面</p>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="mt-6 flex justify-center">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="loadList"
      />
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
  cardMaxRarityLabel,
} from '@/constants/card'
import type { CardVO } from '@/types/card'
import type { CardSeriesVO } from '@/types/cardSeries'
import { getCoverImage } from '@/utils'

const router = useRouter()

const keyword = ref('')
const filter = reactive<{
  seriesId?: number
  maxRarity?: number
  attribute?: number
}>({
  seriesId: undefined,
  maxRarity: undefined,
  attribute: undefined,
})

const loading = ref(false)
const list = ref<CardVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const seriesOptions = ref<CardSeriesVO[]>([])

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
    })
    list.value = res.data.records ?? []
    total.value = res.data.total ?? 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  void loadList()
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
