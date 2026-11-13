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

        <!-- 富文本内容 -->
        <RichContent :html="notice.content" class="mt-6" />
      </article>
    </el-scrollbar>
  </el-card>
</template>

<script setup lang="ts">
import type { NoticeVO } from '@/types/notice'

defineProps<{
  notice: NoticeVO | null
  loading: boolean
}>()
</script>
