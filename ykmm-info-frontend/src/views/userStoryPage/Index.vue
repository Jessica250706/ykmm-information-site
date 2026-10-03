<template>
  <section class="story-index h-full overflow-auto p-6">
    <h1 class="mb-6 text-2xl font-semibold">剧情</h1>

    <div class="grid grid-cols-1 gap-4">
      <div
        v-for="type in storyCategoryTypeStore.types"
        :key="type.id"
        :style="{
          borderColor: `var(--color-${type.color})`,
          backgroundColor: `color-mix(in srgb, var(--color-${type.color}) 7%, var(--el-bg-color))`,
        }"
        class="cursor-pointer rounded-xl bg-white p-6 shadow-sm transition hover:shadow-md border-2"
        @click="goType(type.id as StoryCategoryTypeValue)"
      >
        <div class="mb-2 flex items-center gap-3">
          <h3 class="text-lg font-medium">{{ type.name }}</h3>
        </div>
        <p class="text-sm text-slate-500">{{ type.description }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { type StoryCategoryTypeValue } from '@/constants/story'
import { useStoryCategoryTypeStore } from '@/stores/storyCategoryTypeStore'

const router = useRouter()
const storyCategoryTypeStore = useStoryCategoryTypeStore()

function goType(type: StoryCategoryTypeValue) {
  router.push({ name: 'UserStoryBrowse', params: { type: String(type) } })
}
</script>
