<template>
  <section class="story-index h-full overflow-auto p-6">
    <h1 class="mb-6 text-2xl font-semibold">剧情</h1>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
      <div
        v-for="type in typeOptions"
        :key="type.value"
        class="cursor-pointer rounded-xl bg-white p-6 shadow-sm transition hover:shadow-md"
        @click="goType(type.value)"
      >
        <div class="mb-2 flex items-center gap-3">
          <span :class="type.bgClass" class="grid h-10 w-10 place-items-center rounded-lg text-xl">
            {{ type.emoji }}
          </span>
          <h3 class="text-lg font-medium">{{ type.label }}</h3>
        </div>
        <p class="text-sm text-slate-500">{{ type.desc }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  STORY_CATEGORY_TYPE,
  STORY_CATEGORY_TYPE_OPTIONS,
  type StoryCategoryTypeValue,
} from '@/constants/story'

const router = useRouter()

const META: Record<StoryCategoryTypeValue, { emoji: string; desc: string; bgClass: string }> = {
  [STORY_CATEGORY_TYPE.MAIN]: {
    emoji: '📖',
    desc: '第一部 ~ 第六部的主线剧情',
    bgClass: 'bg-red-50 text-red-500',
  },
  [STORY_CATEGORY_TYPE.RAINBOW_CITY]: {
    emoji: '🌈',
    desc: '欢迎来到彩虹城市！',
    bgClass: 'bg-amber-50 text-amber-500',
  },
  [STORY_CATEGORY_TYPE.SPECIAL]: {
    emoji: '✨',
    desc: '特别企划、纪念日剧情',
    bgClass: 'bg-emerald-50 text-emerald-500',
  },
  [STORY_CATEGORY_TYPE.ACTIVITY]: {
    emoji: '🎉',
    desc: '活动篇剧情',
    bgClass: 'bg-indigo-50 text-indigo-500',
  },
  [STORY_CATEGORY_TYPE.DRAMA]: {
    emoji: '🎭',
    desc: '戏剧篇 - 星巡、妖万华镜等',
    bgClass: 'bg-slate-100 text-slate-500',
  },
}

const typeOptions = computed(() =>
  STORY_CATEGORY_TYPE_OPTIONS.map((o) => ({
    value: o.value as StoryCategoryTypeValue,
    label: o.label,
    ...META[o.value as StoryCategoryTypeValue],
  })),
)

function goType(type: StoryCategoryTypeValue) {
  router.push({ name: 'UserStoryBrowse', params: { type: String(type) } })
}
</script>
