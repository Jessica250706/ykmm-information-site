<template>
  <div class="dialogue-view">
    <div class="mb-4 rounded-lg bg-white p-4 shadow-sm">
      <h2 class="text-lg font-semibold">{{ detail.title || `剧情 #${detail.id}` }}</h2>
      <div class="mt-1 flex flex-wrap items-center gap-2 text-xs text-slate-400">
        <el-tag effect="plain" size="small">{{ detail.categoryTypeLabel }}</el-tag>
        <span>{{ detail.categoryName }}</span>
      </div>
      <p v-if="detail.description" class="mt-3 whitespace-pre-line text-sm text-slate-600">
        {{ detail.description }}
      </p>
    </div>

    <el-empty v-if="!detail.versions?.length" description="暂无对话内容" />

    <el-tabs v-else v-model="activeVersionId" type="border-card">
      <el-tab-pane
        v-for="v in detail.versions"
        :key="v.id"
        :label="versionLabel(v)"
        :name="String(v.id)"
      >
        <!-- 文字对话 -->
        <template v-if="v.format === 1">
          <div v-if="!v.lines?.length" class="py-6 text-center text-slate-400">暂无内容</div>
          <div v-else class="space-y-3">
            <div
              v-for="line in v.lines"
              :key="line.id"
              class="flex items-start gap-3 rounded-lg bg-slate-50 p-3"
            >
              <el-avatar :size="36" :src="line.speakerAvatar">
                {{ line.speakerName?.charAt(0) || '?' }}
              </el-avatar>
              <div class="min-w-0 flex-1">
                <div class="text-xs font-medium text-slate-500">{{ line.speakerName }}</div>
                <div class="mt-1 whitespace-pre-line text-sm text-slate-800">
                  <template v-for="(seg, i) in line.segments ?? []" :key="seg.id ?? i">
                    <template v-if="seg.segmentType === 1">{{ seg.content }}</template>
                    <span v-else-if="seg.segmentType === 2" class="mx-1 align-middle">
                      <img
                        v-if="seg.stickerUrl"
                        :src="seg.stickerUrl"
                        alt="sticker"
                        class="inline-block h-6 w-6 align-middle"
                      />
                      <span v-else>{{ seg.stickerEmoji }}</span>
                    </span>
                  </template>
                  <template v-if="!line.segments?.length">
                    {{ line.content }}
                  </template>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- 图片对话 -->
        <template v-else>
          <div v-if="!v.images?.length" class="py-6 text-center text-slate-400">暂无图片</div>
          <div v-else class="grid grid-cols-1 gap-3 md:grid-cols-2">
            <img
              v-for="img in v.images"
              :key="img.id"
              :src="img.url"
              alt="dialogue"
              class="w-full rounded-lg shadow-sm"
            />
          </div>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { DialogueVersionVO, StoryDetailVO } from '@/types/story'

const props = defineProps<{
  detail: StoryDetailVO
}>()

const activeVersionId = ref<string>('')

watch(
  () => props.detail?.versions,
  (versions) => {
    if (versions?.length) {
      activeVersionId.value = String(versions[0]?.id)
    } else {
      activeVersionId.value = ''
    }
  },
  { immediate: true },
)

function versionLabel(v: DialogueVersionVO): string {
  return (
    [v.languageLabel, v.formatLabel, v.scopeLabel].filter(Boolean).join(' · ') || `版本 ${v.id}`
  )
}
</script>
