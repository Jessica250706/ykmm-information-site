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
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  getVisibleNoticeDetailAPI,
  listNoticeCategoriesAPI,
  pageVisibleNoticesAPI,
} from '@/api/notice'
import type { NoticeCategoryVO, NoticeVO } from '@/types/notice'
import NoticeDetail from './components/NoticeDetail.vue'
import NoticeLeft from './components/NoticeLeft.vue'

const route = useRoute()
const router = useRouter()

const categories = ref<NoticeCategoryVO[]>([])
/** 全部可见公告（一次性拉取，个人项目数据量足够小） */
const notices = ref<NoticeVO[]>([])

/** 当前选中的公告 id（string，配合 el-menu 的 :default-active） */
const selectedNoticeId = ref<string>('')
/** 当前公告详情（点击时单独请求，含完整 content + 累加浏览量） */
const currentNotice = ref<NoticeVO | null>(null)
const loadingDetail = ref(false)

/* -------- 分组 -------- */
const groupedCategories = computed(() => {
  const map = new Map<number, NoticeVO[]>()
  for (const n of notices.value) {
    if (n.categoryId == null) continue
    if (!map.has(n.categoryId)) map.set(n.categoryId, [])
    map.get(n.categoryId)!.push(n)
  }

  return categories.value
    .flatMap((cat) => {
      if (cat.id == null) return []
      return [{ ...cat, id: cat.id, notices: map.get(cat.id) ?? [] }]
    })
    .filter((cat) => cat.notices.length > 0)
})

/* -------- URL 同步 -------- */
/** 从 URL 解析公告 id，非法返回 null */
function resolveUrlNoticeId(): number | null {
  const raw = route.query.id
  if (typeof raw !== 'string' || !raw) return null
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? n : null
}

/**
 * 把选中公告 id 写回 URL（?id=123）。
 * 用 router.replace 而非 push：切公告不是"新页面"，
 * 避免用户点 N 个公告后要按 N 次后退才能离开。
 */
function syncNoticeToUrl(id: number | null) {
  const cur = route.query.id
  const target = id == null ? undefined : String(id)
  if ((cur ?? undefined) === target) return

  const query = { ...route.query }
  if (target == null) {
    delete query.id
  } else {
    query.id = target
  }
  void router.replace({ query })
}

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

/* -------- 选中公告 -------- */
/**
 * @param id         公告 id
 * @param syncUrl    是否把 id 写回 URL（URL 变化触发进入时传 false，避免循环）
 */
async function handleSelectNotice(id: number, syncUrl = true) {
  if (String(id) === selectedNoticeId.value && currentNotice.value?.id === id) return

  selectedNoticeId.value = String(id)
  if (syncUrl) syncNoticeToUrl(id)

  loadingDetail.value = true
  try {
    const res = await getVisibleNoticeDetailAPI(id)
    currentNotice.value = res.data ?? null
    // 同步左侧列表里的 viewCount（避免再次点击时数字不一致）
    const idx = notices.value.findIndex((n) => n.id === id)
    const target = notices.value[idx]
    if (target && currentNotice.value?.viewCount != null) {
      target.viewCount = currentNotice.value.viewCount
    }
  } catch {
    ElMessage.error('公告加载失败，可能已被删除或无权查看')
    currentNotice.value = null
    // URL 里的 id 已失效：清掉，避免刷新后继续报错
    selectedNoticeId.value = ''
    syncNoticeToUrl(null)
  } finally {
    loadingDetail.value = false
  }
}

/* -------- 响应 URL 变化（前进 / 后退 / 手动改地址） -------- */
watch(
  () => route.query.id,
  (raw) => {
    const nextId = typeof raw === 'string' && raw ? Number(raw) : null

    // URL 里没有有效 id → 清空选中
    if (nextId == null || !Number.isFinite(nextId) || nextId <= 0) {
      selectedNoticeId.value = ''
      currentNotice.value = null
      return
    }

    // 已选中同一条 → 跳过，避免死循环
    if (String(nextId) === selectedNoticeId.value) return

    void handleSelectNotice(nextId, false)
  },
)

/* -------- 初始化 -------- */
onMounted(async () => {
  await Promise.all([loadCategories(), loadNotices()])

  // URL 带 id：自动加载
  const initialId = resolveUrlNoticeId()
  if (initialId != null) {
    void handleSelectNotice(initialId, false)
  }
})
</script>
