<template>
  <div class="notice-browse flex h-full gap-4 overflow-hidden">
    <!-- 左侧目录 -->
    <NoticeLeft
      :categories="categories"
      :grouped-categories="groupedCategories"
      :selected-notice-id="selectedNoticeId"
      class="shrink-0 overflow-hidden"
      @select-notice="handleSelectNotice"
    />

    <!-- 右侧详情 -->
    <NoticeDetail
      :loading="loadingDetail"
      :notice="currentNotice"
      class="min-w-0 flex-1 overflow-hidden"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getVisibleNoticeDetailAPI,
  listNoticeCategoriesAPI,
  pageVisibleNoticesAPI,
} from '@/api/notice'
import type { NoticeCategoryVO, NoticeVO } from '@/types/notice'
import NoticeDetail from './components/NoticeDetail.vue'
import NoticeLeft from './components/NoticeLeft.vue'

/** 分类字典 */
const categories = ref<NoticeCategoryVO[]>([])
/** 全部可见公告（一次性拉取，个人项目数据量足够小） */
const notices = ref<NoticeVO[]>([])

/** 当前选中的公告 id（string，配合 el-menu 的 :default-active） */
const selectedNoticeId = ref<string>('')
/** 当前公告详情（点击时单独请求，含完整 content + 累加浏览量） */
const currentNotice = ref<NoticeVO | null>(null)
const loadingDetail = ref(false)

/**
 * 按分类分组：只返回「有公告」的分类
 * 前端分组的好处：左侧菜单直接复用这一份数据，搜索时也能快速重算
 */
const groupedCategories = computed(() => {
  const map = new Map<number, NoticeVO[]>()
  for (const n of notices.value) {
    if (n.categoryId == null) continue
    if (!map.has(n.categoryId)) map.set(n.categoryId, [])
    map.get(n.categoryId)!.push(n)
  }
  return categories.value
    .map((cat) => ({ ...cat, notices: map.get(cat.id) ?? [] }))
    .filter((cat) => cat.notices.length > 0)
})

/* -------- 加载 -------- */
async function loadCategories() {
  const res = await listNoticeCategoriesAPI()
  categories.value = res.data ?? []
}

/**
 * 一次性拉取全部可见公告。
 * 个人项目公告数量通常 < 200，一次加载后本地搜索即可；
 * 若将来数量激增，可改为「左侧只展示最近 100 条 + 搜索时服务端查询」。
 */
async function loadNotices() {
  const res = await pageVisibleNoticesAPI({ pageNum: 1, pageSize: 200 })
  notices.value = res.data.records ?? []
}

/* -------- 交互 -------- */
async function handleSelectNotice(id: number) {
  if (String(id) === selectedNoticeId.value && currentNotice.value?.id === id) return
  selectedNoticeId.value = String(id)

  loadingDetail.value = true
  try {
    const res = await getVisibleNoticeDetailAPI(id)
    currentNotice.value = res.data ?? null
    // 同步更新左侧列表里的 viewCount（避免再次点击时数字不一致）
    const idx = notices.value.findIndex((n) => n.id === id)
    if (idx >= 0 && currentNotice.value?.viewCount != null) {
      notices.value[idx].viewCount = currentNotice.value.viewCount
    }
  } catch {
    ElMessage.error('公告加载失败，可能已被删除或无权查看')
    currentNotice.value = null
  } finally {
    loadingDetail.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadNotices()])
})
</script>
