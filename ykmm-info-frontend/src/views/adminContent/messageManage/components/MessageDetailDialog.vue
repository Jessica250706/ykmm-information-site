<template>
  <el-dialog
    v-model="visible"
    :close-on-click-modal="false"
    title="消息详情"
    width="640px"
    @closed="handleClosed"
  >
    <div v-loading="loading">
      <template v-if="message">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ message.id ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="接收用户ID">
            {{ message.userId ?? '（广播）' }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="tagType" effect="plain" size="small">
              {{ message.typeLabel || messageTypeLabel(message.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="关联业务">
            {{ message.targetTypeLabel || messageTargetTypeLabel(message.targetType) }}
            <span v-if="message.targetId" class="ml-1 text-slate-400">#{{ message.targetId }}</span>
          </el-descriptions-item>
          <el-descriptions-item :span="2" label="标题">
            {{ message.title || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :span="2" label="内容">
            <span class="whitespace-pre-wrap">{{ message.content || '（无正文）' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="是否已读">
            <el-tag
              :type="message.isRead === 1 ? 'success' : 'warning'"
              effect="plain"
              size="small"
            >
              {{ message.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="阅读时间">
            {{ message.readAt || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :span="2" label="创建时间">
            {{ message.createdAt || '-' }}
          </el-descriptions-item>
        </el-descriptions>
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
import { getMessageDetailAPI, markReadAPI } from '@/api/message'
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

/** 标记已读后通知父组件刷新列表 / 未读数 */
const emit = defineEmits<{
  markedRead: []
}>()

const tagType = computed(() => {
  if (message.value?.type == null) return 'info'
  return MESSAGE_TYPE_TAG_TYPE[message.value.type] ?? 'info'
})

/**
 * 打开详情：
 * 1. 先用列表字段兜底渲染，再拉详情覆盖
 * 2. 未读自动标已读（管理端 API）
 *   - 广播消息：任一管理员操作即全群体已读
 *   - 自己的个人消息：正常标记
 *   - 别人的个人消息：后端抛 403，前端 catch 兜底，不影响查看
 */
async function open(row: SysMessageVO) {
  visible.value = true
  message.value = row
  loading.value = true

  try {
    const res = await getMessageDetailAPI(row.id!)
    if (res.data) {
      message.value = res.data
    }
  } catch {
    // 拉取详情失败，用列表字段兜底
  } finally {
    loading.value = false
  }

  // 未读则自动标已读
  if (message.value?.isRead === 0 && message.value.id) {
    try {
      await markReadAPI(message.value.id)
      message.value.isRead = 1
      emit('markedRead')
    } catch {
      // 标记失败不阻塞查看（例如别人的个人消息无权标记）
    }
  }
}

/**
 * 查看详情：新标签页打开用户端浏览页。
 *
 * 因为函数内有 await，若在 await 之后再 window.open 会被浏览器拦截，
 * 所以入口处先同步打开空白占位窗口，异步拿到目标 URL 后再填 href；
 * 失败则关闭占位窗口。
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
  newWindow.opener = null

  const openUrl = (href: string) => {
    newWindow.location.href = href
    visible.value = false
  }
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

    // ---------- 对话版本：查版本详情，再按来源跳 ----------
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
