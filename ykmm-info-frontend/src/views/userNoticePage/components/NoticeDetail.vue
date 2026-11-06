<template>
  <el-card
    v-loading="loading"
    body-class="flex flex-col h-full overflow-hidden"
    class="h-full"
    shadow="never"
  >
    <!-- 未选公告：空白占位 -->
    <div v-if="!notice" class="flex h-full items-center justify-center">
      <el-empty description="请从左侧选择公告" />
    </div>

    <!-- 详情 -->
    <el-scrollbar v-else class="min-h-0 flex-1">
      <article class="mx-auto max-w-3xl px-8 py-6">
        <header class="border-b pb-4">
          <h1 class="text-2xl font-semibold text-slate-800">
            {{ notice.title }}
          </h1>

          <div class="mt-3 flex flex-wrap items-center gap-3 text-sm text-slate-500">
            <el-tag
              v-if="notice.categoryName"
              :color="notice.categoryColor"
              effect="plain"
              size="small"
            >
              {{ notice.categoryName }}
            </el-tag>
            <span>发布于 {{ notice.publishAt || '暂未发布' }}</span>
            <span>浏览 {{ notice.viewCount ?? 0 }}</span>
            <el-tag v-if="notice.isTop === 1" effect="plain" size="small" type="danger">
              置顶
            </el-tag>
          </div>
        </header>

        <!-- 富文本内容：DOMPurify 清洗 + 点击图片打开预览 -->
        <div
          ref="contentRef"
          class="rich-content mt-6"
          @click="handleContentClick"
          v-html="safeContent"
        />
      </article>
    </el-scrollbar>

    <!-- 图片预览 -->
    <el-image-viewer
      v-if="viewerVisible"
      :initial-index="previewIndex"
      :url-list="previewList"
      hide-on-click-modal
      lazy
      teleported
      @close="viewerVisible = false"
    />
  </el-card>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import DOMPurify from 'dompurify'
import type { NoticeVO } from '@/types/notice'

const props = defineProps<{
  notice: NoticeVO | null
  loading: boolean
}>()

const contentRef = ref<HTMLElement | null>(null)

/* -------- 图片预览状态 -------- */
const viewerVisible = ref(false)
const previewList = ref<string[]>([])
const previewIndex = ref(0)

/**
 * 富文本内容安全清洗 + 拆掉「包图片的 a 标签」。
 *
 * 为什么拆 a 标签：
 * - wangEditor 插入图片时可能生成 <a href="原图URL"><img src="..."/></a>
 * - 点击图片 → 浏览器跳转到 href → 新标签页请求原图，
 *   但 OSS 私有读需要 token → 新标签页无 token → 403/404
 * - 拆掉 a 后 img 独立存在，点击不会跳转
 *
 * 为什么只拆「包图片的 a」：
 * - 富文本里可能有正常的超链接（外链、站内跳转），这些要保留
 * - 只处理 a 下有 img 的情况，正常的 <a>文本</a> 不动
 */
const safeContent = computed(() => {
  const sanitized = DOMPurify.sanitize(props.notice?.content ?? '', {
    FORBID_TAGS: ['style'],
  })

  // SSR 环境下没有 document，跳过 DOM 处理（本项目是纯 CSR，不会走到这里）
  if (typeof document === 'undefined') return sanitized

  const temp = document.createElement('div')
  temp.innerHTML = sanitized

  // 把 <a> 里的 <img> 提出来放到 a 前面，然后删掉 a
  temp.querySelectorAll('a').forEach((a) => {
    if (!a.querySelector('img')) return
    const parent = a.parentNode
    if (!parent) return
    // 把 a 的所有子节点（含 img）挪到 a 前面
    while (a.firstChild) {
      parent.insertBefore(a.firstChild, a)
    }
    parent.removeChild(a)
  })

  return temp.innerHTML
})

/**
 * 点击富文本内容：拦截图片点击，打开 el-image-viewer 预览。
 *
 * 为什么用事件委托：
 * - v-html 渲染的内容无法绑定 Vue 事件处理器
 * - 在容器上监听 click，判断 e.target 是不是 img
 *
 * 为什么收集所有 img 的 src 而不是只传当前图片：
 * - 让用户能在预览里左右切换，看同一篇文章里的其他图片
 * - 这是 Element Plus 的 el-image-viewer 的标准用法
 */
function handleContentClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (target.tagName !== 'IMG') return

  e.preventDefault()
  e.stopPropagation()

  // 收集当前富文本里所有 img 的 src
  const imgs = contentRef.value?.querySelectorAll('img') ?? []
  const urls: string[] = []
  let clickedIndex = 0

  imgs.forEach((img) => {
    const src = img.getAttribute('src')
    if (!src) return
    // 在 push 之前记录索引，正好是当前图片即将被 push 到的位置
    if (img === target) clickedIndex = urls.length
    urls.push(src)
  })

  if (urls.length === 0) return

  previewList.value = urls
  previewIndex.value = clickedIndex
  viewerVisible.value = true
}
</script>

<style lang="scss" scoped>
.rich-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--el-text-color-primary);
  word-break: break-word;

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 6px;
    margin: 8px 0;
    /* ★ 提示可点击放大 */
    cursor: zoom-in;
  }

  :deep(h1),
  :deep(h2),
  :deep(h3) {
    margin: 20px 0 10px;
    font-weight: 600;
    line-height: 1.4;
  }

  :deep(h1) {
    font-size: 22px;
  }
  :deep(h2) {
    font-size: 20px;
  }
  :deep(h3) {
    font-size: 17px;
  }

  :deep(p) {
    margin: 10px 0;
  }

  :deep(a) {
    color: var(--el-color-primary);
    text-decoration: underline;
  }

  :deep(blockquote) {
    margin: 12px 0;
    padding: 8px 16px;
    border-left: 4px solid var(--el-color-primary-light-5);
    background: var(--el-fill-color-lighter);
    color: var(--el-text-color-regular);
  }

  :deep(ul),
  :deep(ol) {
    margin: 10px 0;
    padding-left: 24px;
  }

  :deep(code) {
    padding: 2px 6px;
    background: var(--el-fill-color);
    border-radius: 4px;
    font-family: ui-monospace, monospace;
    font-size: 13px;
  }

  :deep(pre) {
    padding: 12px;
    background: var(--el-fill-color);
    border-radius: 6px;
    overflow-x: auto;
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;

    th,
    td {
      padding: 8px 12px;
      border: 1px solid var(--el-border-color);
    }

    th {
      background: var(--el-fill-color-lighter);
    }
  }
}
</style>
