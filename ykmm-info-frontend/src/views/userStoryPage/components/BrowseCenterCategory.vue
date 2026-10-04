<template>
  <div>
    <div class="mb-4">
      <div class="flex justify-between items-center">
        <h2 class="text-lg font-semibold">{{ currentCategory.name }}</h2>
        <el-button v-if="currentCategory.parentId !== 0" @click="emit('goBack')">← 返回</el-button>
      </div>
      <div class="mt-2 flex items-center gap-2 text-xs text-slate-400">
        <el-tag
          :style="{
            borderColor: `var(--color-${color})`,
            color: `var(--color-${color})`,
          }"
          effect="plain"
          size="small"
        >
          {{ currentCategory.categoryTypeLabel ?? typeLabel }}
        </el-tag>
      </div>
    </div>

    <section v-if="currentCategory.children?.length" class="mb-6">
      <h3 class="mb-2 text-sm font-medium text-slate-500">子分类</h3>
      <div class="grid grid-cols-1 gap-3">
        <div
          v-for="child in currentCategory.children"
          :key="child.id"
          class="cursor-pointer rounded-lg border bg-white p-3 transition hover:border-indigo-300 hover:shadow-sm"
          @click="emit('goCategory', child.id!)"
        >
          <div class="font-medium">{{ child.name }}</div>
          <div v-if="child.children?.length" class="mt-0.5 text-xs text-slate-400">
            {{ child.children.length }} 个子项
          </div>
        </div>
      </div>
    </section>

    <section v-if="stories.length">
      <h3 class="mb-2 text-sm font-medium text-slate-500">剧情列表</h3>
      <div v-loading="loadingStories">
        <div class="space-y-2">
          <div
            v-for="s in stories"
            :key="s.id"
            class="cursor-pointer rounded-lg border bg-white p-3 transition hover:border-indigo-300 hover:shadow-sm"
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
  </div>
</template>

<script setup lang="ts">
import type { StoryVO } from '@/types/story'
import type { StoryCategoryVO } from '@/types/storyCategory'

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
