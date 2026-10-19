<template>
  <div class="flex h-full min-h-0 flex-col">
    <!-- 状态筛选 + 刷新 -->
    <div class="mb-3 flex shrink-0 items-center gap-3">
      <el-select
        v-model="statusFilter"
        placeholder="全部状态"
        style="width: 240px"
        clearable
        @change="reload"
      >
        <el-option
          v-for="opt in STATUS_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <span class="text-sm text-slate-500">共 {{ total }} 张</span>
    </div>

    <div v-loading="loading" class="min-h-0 flex-1 overflow-y-auto">
      <el-empty v-if="!loading && !list.length" description="还没有上传过卡面" />

      <div v-else class="grid grid-cols-2 gap-4 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
        <article
          v-for="card in list"
          :key="card.id"
          class="group cursor-pointer overflow-hidden rounded-xl bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md"
          @click="router.push({ name: UserRouteName.CARD_DETAIL, params: { id: String(card.id) } })"
        >
          <div class="relative aspect-3/4 overflow-hidden bg-slate-100">
            <img
              v-if="getCoverImage(card)"
              :alt="card.name"
              :src="getCoverImage(card)"
              class="h-full w-full object-cover transition group-hover:scale-105"
            />
            <div v-else class="h-full w-full bg-linear-to-br from-slate-200 to-slate-300" />
            <el-tag
              v-if="card.status"
              :type="getStatusTagType(card.status)"
              class="absolute left-2 top-2"
              effect="dark"
              size="small"
            >
              {{ statusLabel(card.status) }}
            </el-tag>
          </div>
          <div class="p-2.5">
            <p class="truncate text-sm font-medium">{{ card.name || '-' }}</p>
            <p class="mt-0.5 truncate text-xs text-slate-400">{{ card.seriesName || '-' }}</p>
          </div>
        </article>
      </div>
    </div>

    <div v-if="total > pageSize" class="shrink-0 pt-3">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { pageMyCardsAPI } from '@/api/card'
import { STATUS_OPTIONS, statusLabel, UserRouteName } from '@/constants'
import type { CardVO } from '@/types/card'
import { getCoverImage, getStatusTagType } from '@/utils'

const router = useRouter()

const loading = ref(false)
const list = ref<CardVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const statusFilter = ref<number | undefined>()

async function load() {
  loading.value = true
  try {
    const res = await pageMyCardsAPI({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: statusFilter.value,
    })
    list.value = res.data.records ?? []
    total.value = res.data.total ?? 0
  } finally {
    loading.value = false
  }
}

function reload() {
  pageNum.value = 1
  void load()
}

onMounted(load)
</script>
