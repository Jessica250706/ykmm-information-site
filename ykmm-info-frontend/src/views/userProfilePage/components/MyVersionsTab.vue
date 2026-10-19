<template>
  <div class="flex h-full min-h-0 flex-col">
    <div class="mb-3 flex shrink-0 items-center gap-3">
      <div class="mb-3 flex shrink-0 flex-wrap items-center gap-3">
        <el-select
          v-model="sourceTypeFilter"
          placeholder="全部来源"
          style="width: 240px"
          clearable
          @change="reload"
        >
          <el-option
            v-for="opt in SOURCE_TYPE_FILTER_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>

        <el-select
          v-model="statusFilter"
          placeholder="全部状态"
          style="width: 240px"
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
    </div>

    <div v-loading="loading" class="min-h-0 flex-1 overflow-y-auto">
      <el-empty v-if="!loading && !list.length" description="还没有上传过版本" />

      <ul v-else class="space-y-2">
        <li
          v-for="v in list"
          :key="v.id"
          class="flex items-center gap-3 rounded-lg border border-slate-200 px-3 py-2.5"
        >
          <el-tag :type="getStatusTagType(v.status)" class="shrink-0" effect="plain">
            {{ statusLabel(v.status) }}
          </el-tag>

          <div class="min-w-0 flex-1">
            <!-- 第一行：来源主标题 + 版本信息 -->
            <div class="flex flex-wrap items-center gap-2 text-sm">
              <!-- 来源主标题：story=剧情标题；card=卡面名 [系列名] -->
              <span class="font-medium">
                <template v-if="v.source?.sourceType === SOURCE_TYPE.STORY">
                  {{ v.source.title || '剧情' }}
                </template>
                <template v-else-if="v.source">
                  {{ v.source.title || '未知卡面' }}
                  <span v-if="v.source.subtitle" class="text-slate-400">
                    [{{ v.source.subtitle }}]
                  </span>
                </template>
                <template v-else>来源已删除</template>
              </span>

              <el-tag effect="plain" size="small" type="info">
                {{ v.sourceTypeLabel || '未知来源' }}
              </el-tag>
            </div>

            <!-- 第二行：话标题（card 类）+ 版本信息 -->
            <div class="mt-0.5 flex flex-wrap items-center gap-2 text-xs text-slate-500">
              <!-- card 类：第X话 · 话标题 -->
              <span v-if="v.source && v.source.sourceType !== SOURCE_TYPE.STORY">
                <template v-if="v.source.episodeNo != null">第{{ v.source.episodeNo }}话</template>
                <template v-if="v.source.episodeTitle">· {{ v.source.episodeTitle }}</template>
              </span>
              <!-- story 类：分类名 -->
              <span v-if="v.source?.sourceType === SOURCE_TYPE.STORY && v.source.subtitle">
                {{ v.source.subtitle }}
              </span>

              <!-- 版本信息 -->
              <span class="text-slate-300">|</span>
              <span>
                {{ v.label || `${v.languageLabel} · ${v.formatLabel} · ${v.scopeLabel}` }}
              </span>
            </div>

            <div class="mt-0.5 truncate text-xs text-slate-400">
              创建于 {{ v.createdAt || '-' }}
            </div>
            <div
              v-if="v.status === STATUS.REJECTED && v.reviewRemark"
              class="mt-1 text-xs text-rose-500"
            >
              审核备注：{{ v.reviewRemark }}
            </div>
          </div>

          <el-button
            :disabled="!v.source?.sourceId"
            size="small"
            type="primary"
            link
            @click="handleOpen(v)"
          >
            查看
          </el-button>
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
import { pageMyVersionsAPI } from '@/api/dialogueVersion'
import {
  SOURCE_TYPE,
  SOURCE_TYPE_FILTER_OPTIONS,
  sourceTypeBrowseRouteName,
  STATUS,
  STATUS_OPTIONS,
  statusLabel,
  UserRouteName,
} from '@/constants'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import { getStatusTagType } from '@/utils'

const router = useRouter()

const loading = ref(false)
const list = ref<DialogueVersionVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const statusFilter = ref<number | undefined>()
const sourceTypeFilter = ref<number | undefined>()

async function load() {
  loading.value = true
  try {
    const res = await pageMyVersionsAPI({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: statusFilter.value,
      sourceType: sourceTypeFilter.value,
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

/** 跳转到对应浏览页：直接使用后端已带出的 source 信息，无需再请求 */
async function handleOpen(v: DialogueVersionVO) {
  const src = v.source
  if (!src || src.sourceId == null || src.sourceType == null) {
    ElMessage.warning('该版本缺少来源信息，无法查看')
    return
  }

  // 剧情
  if (src.sourceType === SOURCE_TYPE.STORY) {
    if (src.categoryType == null) {
      ElMessage.warning('剧情分类信息缺失，无法跳转')
      return
    }
    await router.push({
      name: UserRouteName.STORY_BROWSE_DETAIL,
      params: {
        type: String(src.categoryType),
        kind: 'story',
        id: String(src.sourceId),
      },
    })
    return
  }

  // 卡面话数
  if (src.cardId == null) {
    ElMessage.warning('该话数所属卡面信息缺失')
    return
  }

  const routeName = sourceTypeBrowseRouteName(src.sourceType)
  if (!routeName) {
    ElMessage.warning('未知的来源类型，无法跳转')
    return
  }

  await router.push({
    name: routeName,
    query: { cardId: String(src.cardId), sourceId: String(src.sourceId) },
  })
}

onMounted(load)
</script>
