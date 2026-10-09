<template>
  <div class="flex flex-col h-full min-h-0">
    <!-- 标题区域 -->
    <BrowseHeader
      v-if="sourceType === SOURCE_TYPE.STORY"
      :color="color"
      :description="storyDetail?.description"
      :subtitle="storyDetail?.categoryName"
      :tag="storyDetail?.categoryTypeLabel"
      :title="storyDetail?.title || `剧情 #${storyDetail?.id}`"
      @go-back="emit('goBack')"
    />
    <BrowseHeader
      v-else-if="sourceType === SOURCE_TYPE.RC"
      :color="color"
      :subtitle="
        cardDetail?.initiatorRoleName
          ? `${cardName} · 发起人：${cardDetail.initiatorRoleName}`
          : cardDetail?.name
      "
      :tag="SOURCE_TYPE_LABEL[sourceType]"
      :title="
        `第${cardDetail?.episodeNo}话 ${cardDetail?.title}` ||
        `${SOURCE_TYPE_LABEL[sourceType]} #${cardDetail?.id}`
      "
      @go-back="emit('goBack')"
    />
    <BrowseHeader
      v-else-if="sourceType === SOURCE_TYPE.RTV || sourceType === SOURCE_TYPE.RABITTER"
      :color="color"
      :subtitle="cardName"
      :tag="SOURCE_TYPE_LABEL[sourceType]"
      :title="
        `第${cardDetail?.episodeNo}话 ${cardDetail?.title}` ||
        `${SOURCE_TYPE_LABEL[sourceType]} #${cardDetail?.id}`
      "
      @go-back="emit('goBack')"
    />

    <!-- 版本选择 -->
    <div class="mb-4 flex items-center gap-2 shrink-0">
      <span class="text-xs text-slate-500 shrink-0">版本：</span>
      <el-select
        :model-value="selectedVersionKey"
        class="flex-1"
        placeholder="请选择对话版本"
        @update:model-value="(key: string) => emit('update:selectedVersionKey', key)"
      >
        <el-option
          v-for="item in versionSelectItems"
          :key="item.key"
          :label="item.label"
          :value="item.key"
        >
          <span>{{ item.label }}</span>
          <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
            {{ item.count }}
          </span>
        </el-option>
      </el-select>
    </div>

    <!-- 对话内容 -->
    <div class="relative flex-1 min-h-0">
      <!-- 滚动区 -->
      <div
        ref="scrollerRef"
        :class="{
          'overflow-y-auto': currentVersion?.format === VERSION_FORMAT.TEXT,
          'overflow-hidden': currentVersion?.format === VERSION_FORMAT.IMAGE,
        }"
        class="h-full app-scrollbar"
      >
        <DialogueView
          :current-option-version="currentOptionVersion"
          :current-version="currentVersion"
          :current-version-id="resolvedVersionId"
          :detail="sourceType === SOURCE_TYPE.STORY ? storyDetail : cardDetail"
          :editing-line-id="editingLineId ?? null"
          :editing-mode="editingMode"
          @select-line="(line) => emit('select-line', line)"
        />
      </div>

      <!-- 顶部内阴影（滚动离开顶部时显示） -->
      <div
        :class="showTopShadow ? 'opacity-100' : 'opacity-0'"
        class="scroll-shadow-top pointer-events-none absolute inset-x-0 top-0 h-6 transition-opacity duration-200"
      />

      <!-- 底部内阴影（未滚到底时显示） -->
      <div
        :class="showBottomShadow ? 'opacity-100' : 'opacity-0'"
        class="scroll-shadow-bottom pointer-events-none absolute inset-x-0 bottom-0 h-6 transition-opacity duration-200"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useScrollShadow } from '@/composables/useScrollShadow'
import { SOURCE_TYPE, SOURCE_TYPE_LABEL, VERSION_FORMAT } from '@/constants'
import type { CardEpisodeVO } from '@/types/card'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO, DialogueVersionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'
import type { VersionSelectItem } from '@/views/userBrowse/composables/useVersionSelection'
import BrowseHeader from './BrowseHeader.vue'
import DialogueView from './DialogueView.vue'

/* -------- Props / Emits -------- */

const props = defineProps<{
  /** 当前剧情详情 */
  storyDetail?: StoryDetailVO | null
  /** 当前剧情详情 */
  cardDetail?: CardEpisodeVO | null
  color: string
  currentVersionId: number | null
  editingLineId: number | null
  editingMode: boolean
  versionOptions: DialogueVersionOptionVO[] | null
  /** 当前选中的下拉 key（由父级统一管理） */
  selectedVersionKey: string
  /** 预计算的下拉项（由父级统一管理） */
  versionSelectItems: VersionSelectItem[]
  /** 来源类型：1-RC 2-RTV 3-Rabitter 4-story */
  sourceType: number
  /** 卡片名称 */
  cardName?: string
}>()

const emit = defineEmits<{
  goBack: []
  'update:currentVersionId': [id: number | null]
  'update:selectedVersionKey': [key: string]
  'select-line': [line: DialogueLineVO]
}>()

/* -------- 从 props 派生 -------- */

/** 当前选中的 option（供 DialogueView 使用），无选中时为 null */
const currentOptionVersion = computed<DialogueVersionOptionVO | null>(() => {
  if (!props.selectedVersionKey) return null
  return props.versionSelectItems.find((i) => i.key === props.selectedVersionKey)?.option ?? null
})

/** 当前选中项对应的真实版本；无 versionId 或 storyDetail 里找不到时为 null */
const currentVersion = computed<DialogueVersionVO | null>(() => {
  if (props.currentVersionId == null) return null
  if (props.sourceType === SOURCE_TYPE.STORY)
    return props.storyDetail?.versions?.find((v) => v.id === props.currentVersionId) ?? null
  else return props.cardDetail?.versions?.find((v) => v.id === props.currentVersionId) ?? null
})

/** 传给 DialogueView 的 versionId，保证与 currentVersion 一致 */
const resolvedVersionId = computed<number | null>(() => currentVersion.value?.id ?? null)

/* -------- 阴影 -------- */
const scrollerRef = ref<HTMLElement | null>(null)
const { atTop, atBottom, canScroll } = useScrollShadow(scrollerRef)

// 有内容可滚 且 已经离开顶部 → 显示顶部阴影
const showTopShadow = computed(() => canScroll.value && !atTop.value)
// 有内容可滚 且 未到底 → 显示底部阴影
const showBottomShadow = computed(() => canScroll.value && !atBottom.value)
</script>

<style lang="scss" scoped>
.scroll-shadow-top {
  background: linear-gradient(to bottom, var(--el-fill-color-darker) 0%, transparent 100%);
}
.scroll-shadow-bottom {
  background: linear-gradient(to top, var(--el-fill-color-darker) 0%, transparent 100%);
}
</style>
