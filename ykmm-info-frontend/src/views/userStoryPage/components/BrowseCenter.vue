<template>
  <el-card class="min-w-0 flex-1 overflow-auto bg-slate-50 p-5">
    <el-skeleton v-if="loadingContent" :rows="6" animated />

    <!-- Story 详情 -->
    <template v-else-if="storyDetail">
      <!-- <div>{{ storyDetail }}</div> -->
      <!-- 详情头部 -->
      <div class="mb-4">
        <div class="flex justify-between items-center">
          <h2 class="text-lg font-semibold">
            {{ storyDetail.title || `剧情 #${storyDetail.id}` }}
          </h2>
          <el-button @click="emit('goBack')">← 返回</el-button>
        </div>
        <div class="mt-1 flex flex-wrap items-center gap-2 text-xs text-slate-400">
          <el-tag
            :style="{
              borderColor: `var(--color-${color})`,
              color: `var(--color-${color})`,
            }"
            effect="plain"
            size="small"
          >
            {{ storyDetail.categoryTypeLabel }}
          </el-tag>
          <span>{{ storyDetail.categoryName }}</span>
        </div>
        <p v-if="storyDetail.description" class="mt-3 whitespace-pre-line text-sm text-slate-600">
          {{ storyDetail.description }}
        </p>
      </div>

      <!-- 对话内容 -->
      <DialogueView :detail="storyDetail" />
    </template>

    <!-- 分类：子分类 + 剧情列表 -->
    <template v-else-if="currentCategory">
      <!-- <div>{{ currentCategory }}</div> -->
      <div class="mb-4">
        <div class="flex justify-between items-center">
          <h2 class="text-lg font-semibold">{{ currentCategory.name }}</h2>
          <el-button v-if="currentCategory.parentId !== 0" @click="emit('goBack')">
            ← 返回
          </el-button>
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

      <!-- 子分类 -->
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

      <!-- 剧情列表 -->
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
    </template>

    <!-- 未选节点 -->
    <el-empty v-else description="请从左侧选择一个分类" />
  </el-card>
</template>

<script setup lang="ts">
import type { StoryDetailVO, StoryVO } from '@/types/story'
import type { StoryCategoryVO } from '@/types/storyCategory'
import DialogueView from './DialogueView.vue'

defineProps<{
  /** 详情加载中 */
  loadingContent: boolean
  /** 当前剧情详情 */
  storyDetail: StoryDetailVO | null
  /** 当前分类 */
  currentCategory: StoryCategoryVO | null
  /** 类型标签，用于分类标签回退显示 */
  typeLabel: string
  /** 剧情列表加载中 */
  loadingStories: boolean
  /** 当前分类下的剧情列表 */
  stories: StoryVO[]
  /** 当前分类下的剧情列表 */
  color: string
}>()

const emit = defineEmits<{
  /** 点击子分类 */
  goCategory: [id: number]
  /** 点击剧情 */
  goStory: [id: number]
  /** 返回 */
  goBack: []
}>()
</script>
