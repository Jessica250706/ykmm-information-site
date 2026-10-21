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
            {{ message.userId ?? '-' }}
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
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { getMessageDetailAPI } from '@/api/message'
import { MESSAGE_TYPE_TAG_TYPE, messageTargetTypeLabel, messageTypeLabel } from '@/constants'
import type { SysMessageVO } from '@/types/message'

const visible = ref(false)
const loading = ref(false)
const message = ref<SysMessageVO | null>(null)

const tagType = computed(() => {
  if (message.value?.type == null) return 'info'
  return MESSAGE_TYPE_TAG_TYPE[message.value.type] ?? 'info'
})

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
    // 兜底用列表字段
  } finally {
    loading.value = false
  }
}

function handleClosed() {
  message.value = null
}

defineExpose({ open })
</script>
