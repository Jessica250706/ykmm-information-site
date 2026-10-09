<template>
  <div class="flex h-full flex-col overflow-hidden">
    <!-- 标题区：固定 -->
    <BrowseHeader
      :color="color"
      :show-back="currentCategory.parentId !== 0"
      :tag="currentCategory.categoryTypeLabel ?? typeLabel"
      :title="currentCategory.name"
      @go-back="emit('goBack')"
    />

    <!-- 子分类区 -->
    <section
      v-if="currentCategory.children?.length"
      class="mb-6 flex-1 min-h-0 overflow-y-auto app-scrollbar pr-1"
    >
      <h3 class="mb-2 text-sm font-medium text-slate-500">子分类</h3>
      <div class="grid grid-cols-1 gap-3">
        <div
          v-for="child in currentCategory.children"
          :key="child.id"
          class="cursor-pointer rounded-lg border bg-white p-3 transition hover:border-(--el-color-primary) hover:shadow-sm"
          @click="emit('goCategory', child.id!)"
        >
          <div class="font-medium">{{ child.name }}</div>
          <div v-if="child.children?.length" class="mt-0.5 text-xs text-slate-400">
            {{ child.children.length }} 个子项
          </div>
        </div>
      </div>
    </section>

    <!-- 简介 + 剧情列表 -->
    <section v-else-if="stories.length" class="shrink-0 overflow-y-auto flex-1 min-h-0">
      <div v-if="currentCategory.description">
        <h3 class="mb-2 text-sm font-medium text-slate-500">简介</h3>
        <div class="mb-2 text-sm whitespace-pre-line">{{ currentCategory.description }}</div>
      </div>
      <h3 class="mb-2 text-sm font-medium text-slate-500">剧情列表</h3>
      <div v-loading="loadingStories">
        <div class="space-y-2">
          <div
            v-for="s in stories"
            :key="s.id"
            class="cursor-pointer rounded-lg border bg-white p-3 transition hover:border-(--el-color-primary) hover:shadow-sm"
            @click="emit('goStory', s.id!)"
          >
            <div class="font-medium">{{ s.title || `剧情 #${s.id}` }}</div>
            <div v-if="s.description" class="mt-0.5 line-clamp-1 text-sm text-slate-500">
              {{ s.description }}
            </div>
          </div>
        </div>
      </div>
    </section>

    <section v-else>
      <el-empty description="暂无故事详情" />
    </section>
  </div>
</template>

<script setup lang="ts">
import type { StoryVO } from '@/types/story'
import type { StoryCategoryVO } from '@/types/storyCategory'
import BrowseHeader from './BrowseHeader.vue'

defineProps<{
  currentCategory: StoryCategoryVO
  typeLabel: string
  color: string
  loadingStories: boolean
  stories: StoryVO[]
}>()

const emit = defineEmits<{
  goCategory: [id: number]
  goStory: [id: number]
  goBack: []
}>()
</script>
