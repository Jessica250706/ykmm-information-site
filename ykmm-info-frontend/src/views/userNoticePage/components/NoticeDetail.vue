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
            <span>发布于 {{ notice.publishAt || notice.createdAt || '-' }}</span>
            <span>浏览 {{ notice.viewCount ?? 0 }}</span>
            <el-tag v-if="notice.isTop === 1" effect="plain" size="small" type="danger">
              置顶
            </el-tag>
          </div>
        </header>

        <!-- 富文本内容：DOMPurify 清洗后渲染 -->
        <div class="rich-content mt-6" v-html="safeContent" />
      </article>
    </el-scrollbar>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DOMPurify from 'dompurify'
import type { NoticeVO } from '@/types/notice'

const props = defineProps<{
  notice: NoticeVO | null
  loading: boolean
}>()

/**
 * 富文本内容安全清洗：
 * content 是管理员可控 HTML，即使管理员账号被盗或误粘贴，
 * DOMPurify 也能拦住 <script>、onerror 等 XSS 攻击向量。
 */
const safeContent = computed(() =>
  DOMPurify.sanitize(props.notice?.content ?? '', {
    // 允许的标签：默认已足够宽；如需禁 img 等再收紧
    FORBID_TAGS: ['style'],
  }),
)
</script>

<style lang="scss" scoped>
/**
 * 富文本内容样式：使用 :deep 覆盖 v-html 生成的内容
 * 避免管理员随意写 h1/h2 打乱页面层级
 */
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
