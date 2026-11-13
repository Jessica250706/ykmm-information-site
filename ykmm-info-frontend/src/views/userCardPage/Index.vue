<template>
  <section class="card-list h-full flex flex-col">
    <CardListHeader
      v-model:card-size="cardSize"
      v-model:keyword="keyword"
      :show-create="isPassedUser"
      @create="handleCreate"
      @search="handleSearch"
    />

    <CardListFilter
      v-model:show-more="showMore"
      :category-options="categoryOptions"
      :filter="filter"
      :series-options="seriesOptions"
      @update="handleFilterUpdate"
    />

    <CardGrid
      :disabled="disabled"
      :grid-class="gridClass"
      :list="list"
      :loading="loading"
      @item-click="handleDetail"
      @load="load"
    />
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listCardCategoryOptionsAPI } from '@/api/cardCategory'
import { listCardSeriesOptionsAPI } from '@/api/cardSeries'
import { UserRouteName } from '@/constants'
import { useUserStore } from '@/stores'
import type { CardVO } from '@/types/card'
import type { CardCategoryVO } from '@/types/cardCategory'
import type { CardSeriesVO } from '@/types/cardSeries'
import CardGrid from './components/CardGrid.vue'
import CardListFilter from './components/CardListFilter.vue'
import CardListHeader from './components/CardListHeader.vue'
import { type CardFilter, useCardFilterSync } from './composables/useCardFilterSync'
import { useCardList } from './composables/useCardList'
import { useCardSize } from './composables/useCardSize'

const router = useRouter()
const { isPassedUser } = useUserStore()

/* 1. 卡片尺寸偏好 */
const { cardSize, gridClass } = useCardSize()

/* 2. 筛选同步（onChange 通过中间变量延迟引用 resetAndLoad，避开声明顺序） */
let resetAndLoadFn: (() => void) | null = null
const { keyword, filter, showMore, writeToUrl } = useCardFilterSync({
  onChange: () => resetAndLoadFn?.(),
})

/* 3. 列表加载 */
const { list, loading, disabled, load, resetAndLoad } = useCardList({ keyword, filter })
resetAndLoadFn = () => {
  void resetAndLoad()
}

/* 4. 下拉数据源 */
const seriesOptions = ref<CardSeriesVO[]>([])
const categoryOptions = ref<CardCategoryVO[]>([])

/* ---------- 事件 ---------- */
function handleSearch() {
  const changed = writeToUrl()
  if (!changed) void resetAndLoad()
}

function handleCreate() {
  router.push({ name: UserRouteName.CARD_CREATE, query: { redirect: '/card' } })
}

/** 子组件只 emit，父组件在这里合并到自己拥有的 reactive filter */
function handleFilterUpdate(payload: Partial<CardFilter>) {
  Object.assign(filter, payload)
}

function handleDetail(row: CardVO) {
  router.push({ name: UserRouteName.CARD_DETAIL, params: { id: String(row.id) } })
}

/* ---------- 生命周期 ---------- */
onMounted(async () => {
  void resetAndLoad()

  const [seriesRes, categoryRes] = await Promise.all([
    listCardSeriesOptionsAPI(),
    listCardCategoryOptionsAPI(),
  ])
  seriesOptions.value = seriesRes.data ?? []
  categoryOptions.value = categoryRes.data ?? []
})
</script>
