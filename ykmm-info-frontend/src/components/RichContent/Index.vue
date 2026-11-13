<template>
  <div ref="contentRef" class="rich-content" @click="handleContentClick" v-html="safeContent" />

  <!-- 图片预览：点击正文里的图片时打开 -->
  <el-image-viewer
    v-if="viewerVisible"
    :initial-index="previewIndex"
    :url-list="previewList"
    hide-on-click-modal
    lazy
    teleported
    @close="viewerVisible = false"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import DOMPurify from 'dompurify'

interface Props {
  /** 富文本 HTML 原文（未经清洗） */
  html?: string
  /** 是否允许点击图片放大预览 */
  preview?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  html: '',
  preview: true,
})

const contentRef = ref<HTMLElement | null>(null)

/* -------- 图片预览状态 -------- */
const viewerVisible = ref(false)
const previewList = ref<string[]>([])
const previewIndex = ref(0)

/**
 * 富文本安全清洗 + 拆掉「包图片的 a 标签」。
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
  const sanitized = DOMPurify.sanitize(props.html ?? '', {
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
  if (!props.preview) return

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
