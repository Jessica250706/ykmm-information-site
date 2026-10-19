<template>
  <div
    v-if="option && option.exists"
    class="flex flex-wrap justify-between items-center gap-x-4 gap-y-1 text-xs text-slate-500"
  >
    <!-- 贡献者 -->
    <div v-if="contributors.length" class="flex min-w-0 items-center gap-1.5">
      <span class="shrink-0">贡献者</span>
      <div class="flex min-w-0 flex-wrap items-center gap-x-1 gap-y-1">
        <template v-for="(c, i) in contributors" :key="c.id ?? `idx-${i}`">
          <span class="inline-flex items-center gap-1">
            <el-avatar v-if="c.avatar" :size="16" :src="c.avatar" class="shrink-0" />
            <span class="text-slate-600">{{ c.displayName || '匿名' }}</span>
            <!-- 代传管理员：加个小标记，与普通内容作者区分 -->
            <el-tag
              v-if="c.contributorRole === CONTRIBUTOR_ROLE.CO_ADMIN"
              effect="plain"
              size="small"
              type="info"
            >
              代传
            </el-tag>
          </span>
          <span v-if="i < contributors.length - 1" class="text-slate-300">、</span>
        </template>
      </div>
    </div>

    <!-- 状态 -->
    <div
      v-if="status != null && (status === STATUS.PENDING || status === STATUS.REJECTED)"
      class="flex items-center gap-1.5"
    >
      <!-- <span>状态</span> -->
      <el-tag :type="statusTagType" effect="plain" size="small">
        {{ statusLabelText }}
      </el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { STATUS, STATUS_LABEL, STATUS_TAG_TYPE, type StatusValue } from '@/constants'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import type { DialogueVersionContributorVO } from '@/types/dialogueVersionContributor'

const props = defineProps<{
  /** 当前选中的版本选项（含 status / contributors）；未选中时为 null */
  option: DialogueVersionOptionVO | null
}>()

/** 贡献者角色：1内容作者 2代传管理员 3协作者 */
const CONTRIBUTOR_ROLE = {
  AUTHOR: 1,
  CO_ADMIN: 2,
  COLLABORATOR: 3,
} as const

/** 状态值，无版本或后端未返回时为 null */
const status = computed<StatusValue | null>(() => {
  const s = props.option?.status
  return s != null ? (s as StatusValue) : null
})

/** 状态标签样式 */
const statusTagType = computed(() => {
  if (status.value == null) return 'info' as const
  return STATUS_TAG_TYPE[status.value]
})

/** 状态文案 */
const statusLabelText = computed(() => {
  if (status.value == null) return ''
  return STATUS_LABEL[status.value] ?? '未知'
})

/** 贡献者列表，兜底为空数组 */
const contributors = computed<DialogueVersionContributorVO[]>(
  () => props.option?.contributors ?? [],
)
</script>
