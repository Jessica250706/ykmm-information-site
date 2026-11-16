<template>
  <NoticePanel :loading="loading" :no-card="noCard" variant="detail">
    <!-- 未选公告：空白占位 + 移动端打开抽屉入口 -->
    <div v-if="!notice" class="flex h-full items-center justify-center">
      <el-empty description="请从左侧选择公告">
        <el-button v-if="isMobile" type="primary" @click="emit('open-drawer')">选择公告</el-button>
      </el-empty>
    </div>

    <!-- 详情 -->
    <el-scrollbar v-else class="min-h-0 flex-1">
      <article class="mx-auto max-w-3xl px-4 py-4 sm:px-8 sm:py-6">
        <header class="border-b pb-4">
          <!-- 标题行：标题 + 移动端打开抽屉按钮 -->
          <div class="flex items-start justify-between gap-3">
            <h1
              class="min-w-0 flex-1 wrap-break-word text-xl font-semibold text-slate-800 sm:text-2xl"
            >
              {{ notice.title }}
            </h1>

            <el-button
              v-if="isMobile"
              class="shrink-0"
              size="small"
              type="info"
              plain
              @click="emit('open-drawer')"
            >
              <el-icon class="mr-1"><Menu /></el-icon>
              目录
            </el-button>
          </div>

          <div class="mt-3 flex flex-wrap items-center gap-2 text-sm text-slate-500 sm:gap-3">
            <el-tag v-if="notice.isTop === 1" effect="plain" size="small" type="danger">
              置顶
            </el-tag>
            <el-tag
              v-if="notice.categoryName"
              :color="notice.categoryColor"
              effect="plain"
              size="small"
            >
              {{ notice.categoryName }}
            </el-tag>
            <div>
              <span>发布于 {{ notice.publishAt || '暂未发布' }}</span>
              <span>浏览 {{ notice.viewCount ?? 0 }}</span>
            </div>
          </div>
        </header>

        <!-- 富文本内容 -->
        <RichContent :html="notice.content" class="mt-6" />
      </article>
    </el-scrollbar>
  </NoticePanel>
</template>

<script setup lang="ts">
import { Menu } from '@element-plus/icons-vue'
import type { NoticeVO } from '@/types/notice'
import NoticePanel from './NoticePanel.vue'

withDefaults(
  defineProps<{
    notice: NoticeVO | null
    loading: boolean
    /** 是否跳过 el-card 包裹（移动端） */
    noCard?: boolean
    /** 是否是手机端 */
    isMobile?: boolean
  }>(),
  { noCard: false, isMobile: false },
)

const emit = defineEmits<{
  /** 移动端点击「目录」按钮：请求父组件打开抽屉 */
  'open-drawer': []
}>()
</script>
