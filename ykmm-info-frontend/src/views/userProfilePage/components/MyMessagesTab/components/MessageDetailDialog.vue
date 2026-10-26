<template>
  <el-dialog
    v-model="visible"
    :close-on-click-modal="false"
    title="消息详情"
    width="560px"
    @closed="handleClosed"
  >
    <div v-loading="loading" class="space-y-3">
      <template v-if="message">
        <div class="flex items-start justify-between gap-3">
          <h3 class="text-base font-semibold">{{ message.title }}</h3>
          <el-tag :type="tagType" effect="plain" size="small">
            {{ message.typeLabel || messageTypeLabel(message.type) }}
          </el-tag>
        </div>

        <div class="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-400">
          <span>时间：{{ message.createdAt || '-' }}</span>
          <span v-if="message.targetType">
            关联：{{ message.targetTypeLabel || messageTargetTypeLabel(message.targetType) }}
          </span>
          <span v-if="message.isRead === 0" class="text-rose-500">未读</span>
          <span v-else-if="message.readAt">已读于 {{ message.readAt }}</span>
        </div>

        <div
          class="rounded-lg border border-slate-200 bg-slate-50 p-4 text-sm leading-relaxed whitespace-pre-wrap text-slate-700"
        >
          {{ message.content || '（无正文）' }}
        </div>
      </template>
      <el-empty v-else-if="!loading" description="消息不存在" />
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button
        v-if="message && message.targetType && message.targetId"
        :loading="navigating"
        type="primary"
        @click="handleNavigate"
      >
        查看详情
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getCardEpisodeDialogueAPI } from '@/api/card'
import { getDialogueVersionDetailAPI } from '@/api/dialogueVersion'
import { getMyMessageDetailAPI, markReadAPI } from '@/api/message'
import { getStoryDetailAPI } from '@/api/story'
import {
  MESSAGE_TARGET_TYPE,
  MESSAGE_TYPE_TAG_TYPE,
  messageTargetTypeLabel,
  messageTypeLabel,
  SOURCE_TYPE,
  UserRouteName,
} from '@/constants'
import type { SysMessageVO } from '@/types/message'

const router = useRouter()

const visible = ref(false)
const loading = ref(false)
const navigating = ref(false)
const message = ref<SysMessageVO | null>(null)

const tagType = computed(() => {
  if (message.value?.type == null) return 'info'
  return MESSAGE_TYPE_TAG_TYPE[message.value.type] ?? 'info'
})

const emit = defineEmits<{
  /** 已读状态发生变化 */
  markedRead: []
}>()

/**
 * 打开详情。若消息未读，打开的同时自动标记已读。
 */
async function open(row: SysMessageVO) {
  visible.value = true
  message.value = row
  loading.value = true

  // 加载完整详情（列表返回的是基础字段，详情可能有额外内容）
  try {
    const res = await getMyMessageDetailAPI(row.id!)
    if (res.data) {
      message.value = res.data
    }
  } catch {
    // 拉取详情失败，用列表里的字段兜底展示
  } finally {
    loading.value = false
  }

  // 未读则自动标记已读
  if (message.value?.isRead === 0 && message.value.id) {
    try {
      await markReadAPI(message.value.id)
      message.value.isRead = 1
      emit('markedRead')
    } catch {
      // 标记失败不阻塞查看
    }
  }
}

/**
 * 跳转到关联业务（新标签页打开）。
 * - CARD：直接跳卡面详情
 * - STORY：先查详情拿 categoryType，再跳剧情浏览页
 * - DIALOGUE_VERSION：查版本详情拿 sourceType / sourceId，再按来源跳
 *
 * 实现说明：
 * 函数内有 await，若在 await 之后再 window.open 会被浏览器拦截。
 * 所以入口处先同步打开空白占位窗口（占住"用户手势"），
 * 异步拿到目标 URL 后再填 href；失败则关闭占位窗口。
 */
async function handleNavigate() {
  const msg = message.value
  if (!msg || msg.targetType == null || msg.targetId == null) return

  // 同步阶段先占位打开新标签页，避免后续 await 后被浏览器拦截
  const newWindow = window.open('', '_blank')
  if (!newWindow) {
    ElMessage.warning('浏览器拦截了新窗口，请允许弹出窗口后重试')
    return
  }
  // 立即断开 opener 引用，防止新页面通过 window.opener 操作当前页
  newWindow.opener = null

  /** 打开成功：填充 URL 并关闭弹窗 */
  const openUrl = (href: string) => {
    newWindow.location.href = href
    visible.value = false
  }

  /** 中途放弃：关闭占位窗口 */
  const abort = () => {
    newWindow.close()
  }

  navigating.value = true
  try {
    // ---------- 卡面：直接跳详情 ----------
    if (msg.targetType === MESSAGE_TARGET_TYPE.CARD) {
      const { href } = router.resolve({
        name: UserRouteName.CARD_DETAIL,
        params: { id: String(msg.targetId) },
      })
      openUrl(href)
      return
    }

    // ---------- 剧情：先查详情拿 categoryType ----------
    if (msg.targetType === MESSAGE_TARGET_TYPE.STORY) {
      const detail = await getStoryDetailAPI(msg.targetId)
      const categoryType = detail.data?.categoryType
      if (categoryType == null) {
        abort()
        ElMessage.warning('剧情分类信息缺失，无法跳转')
        return
      }
      const { href } = router.resolve({
        name: UserRouteName.STORY_BROWSE_DETAIL,
        params: {
          type: String(categoryType),
          kind: 'story',
          id: String(msg.targetId),
        },
      })
      openUrl(href)
      return
    }

    // ---------- 对话版本：先查版本详情，再按来源跳 ----------
    if (msg.targetType === MESSAGE_TARGET_TYPE.DIALOGUE_VERSION) {
      const v = await getDialogueVersionDetailAPI(msg.targetId)
      const sourceType = v.data?.sourceType
      const sourceId = v.data?.sourceId
      if (sourceType == null || sourceId == null) {
        abort()
        ElMessage.warning('版本来源信息缺失，无法跳转')
        return
      }

      // 版本来自剧情
      if (sourceType === SOURCE_TYPE.STORY) {
        const detail = await getStoryDetailAPI(sourceId)
        const categoryType = detail.data?.categoryType
        if (categoryType == null) {
          abort()
          ElMessage.warning('剧情分类信息缺失，无法跳转')
          return
        }
        const { href } = router.resolve({
          name: UserRouteName.STORY_BROWSE_DETAIL,
          params: {
            type: String(categoryType),
            kind: 'story',
            id: String(sourceId),
          },
        })
        openUrl(href)
        return
      }

      // 版本来自卡面话数（RC / RTV / Rabitter）
      const ep = await getCardEpisodeDialogueAPI({ sourceType, sourceId })
      const cardId = ep.data?.cardId
      if (cardId == null) {
        abort()
        ElMessage.warning('该话数所属卡面信息缺失')
        return
      }
      const routeName =
        sourceType === SOURCE_TYPE.RC
          ? UserRouteName.CARD_RC_BROWSE
          : sourceType === SOURCE_TYPE.RTV
            ? UserRouteName.CARD_RTV_BROWSE
            : UserRouteName.CARD_RABITTER_BROWSE
      const { href } = router.resolve({
        name: routeName,
        query: { cardId: String(cardId), sourceId: String(sourceId) },
      })
      openUrl(href)
      return
    }

    // 未支持的类型
    abort()
    ElMessage.info('暂不支持跳转到该类型')
  } catch {
    abort()
    ElMessage.error('跳转失败，该内容可能已被删除')
  } finally {
    navigating.value = false
  }
}

function handleClosed() {
  message.value = null
}

defineExpose({ open })
</script>
