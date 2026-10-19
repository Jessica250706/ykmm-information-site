<template>
  <div class="flex h-full min-h-0 flex-col">
    <div class="mb-3 flex shrink-0 items-center gap-3">
      <el-select
        v-model="statusFilter"
        class="w-32"
        placeholder="全部状态"
        clearable
        @change="reload"
      >
        <el-option
          v-for="opt in STATUS_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <span class="text-sm text-slate-500">共 {{ total }} 个</span>
    </div>

    <div v-loading="loading" class="min-h-0 flex-1 overflow-y-auto">
      <el-empty v-if="!loading && !list.length" description="还没有上传过版本" />

      <ul v-else class="space-y-2">
        <li
          v-for="v in list"
          :key="v.id"
          class="flex items-center gap-3 rounded-lg border border-slate-200 px-3 py-2.5"
        >
          <el-tag :type="getStatusTagType(v.status)" effect="plain">
            {{ statusLabel(v.status) }}
          </el-tag>

          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-center gap-2 text-sm">
              <span class="font-medium">
                {{ v.label || `${v.languageLabel} · ${v.formatLabel} · ${v.scopeLabel}` }}
              </span>
              <el-tag effect="plain" size="small" type="info">
                {{ v.sourceTypeLabel || '未知来源' }}
              </el-tag>
            </div>
            <div class="mt-0.5 truncate text-xs text-slate-400">
              #{{ v.id }} · 创建于 {{ v.createdAt || '-' }}
            </div>
            <div
              v-if="v.status === STATUS.REJECTED && v.reviewRemark"
              class="mt-1 text-xs text-rose-500"
            >
              审核备注：{{ v.reviewRemark }}
            </div>
          </div>

          <el-button size="small" type="primary" link @click="handleOpen(v)">查看</el-button>
        </li>
      </ul>
    </div>

    <div v-if="total > pageSize" class="shrink-0 pt-3">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCardEpisodeDialogueAPI } from '@/api/card'
import { pageMyVersionsAPI } from '@/api/dialogueVersion'
import { getStoryDetailAPI } from '@/api/story'
import { SOURCE_TYPE, STATUS, STATUS_OPTIONS, statusLabel, UserRouteName } from '@/constants'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import { getStatusTagType } from '@/utils'

const router = useRouter()

const loading = ref(false)
const list = ref<DialogueVersionVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const statusFilter = ref<number | undefined>()

async function load() {
  loading.value = true
  try {
    const res = await pageMyVersionsAPI({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: statusFilter.value,
    })
    list.value = res.data.records ?? []
    total.value = res.data.total ?? 0
  } finally {
    loading.value = false
  }
}

function reload() {
  pageNum.value = 1
  void load()
}

/** 跳转到对应浏览页 */
async function handleOpen(v: DialogueVersionVO) {
  if (v.sourceType == null || v.sourceId == null) {
    ElMessage.warning('该版本缺少来源信息，无法查看')
    return
  }

  try {
    if (v.sourceType === SOURCE_TYPE.STORY) {
      const res = await getStoryDetailAPI(v.sourceId)
      const categoryType = res.data.categoryType ?? 1
      await router.push({
        name: UserRouteName.STORY_BROWSE_DETAIL,
        params: {
          type: String(categoryType),
          kind: 'story',
          id: String(v.sourceId),
        },
      })
      return
    }

    // 判空后这里 sourceType / sourceId 都已是 number
    const res = await getCardEpisodeDialogueAPI({
      sourceType: v.sourceType,
      sourceId: v.sourceId,
    })
    const cardId = res.data.cardId
    if (cardId == null) {
      ElMessage.error('该话数所属卡面信息缺失')
      return
    }

    const routeName =
      v.sourceType === SOURCE_TYPE.RC
        ? UserRouteName.CARD_RC_BROWSE
        : v.sourceType === SOURCE_TYPE.RTV
          ? UserRouteName.CARD_RTV_BROWSE
          : UserRouteName.CARD_RABITTER_BROWSE

    await router.push({
      name: routeName,
      query: { cardId: String(cardId), sourceId: String(v.sourceId) },
    })
  } catch {
    ElMessage.error('该内容可能已被删除或您无权查看')
  }
}

onMounted(load)
</script>
