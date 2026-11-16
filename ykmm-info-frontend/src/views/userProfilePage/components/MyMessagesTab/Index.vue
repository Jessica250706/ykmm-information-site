<template>
  <div class="flex h-full min-h-0 flex-col">
    <!-- 筛选 + 操作 -->
    <div class="mb-3 flex shrink-0 flex-col gap-2 md:flex-row md:items-center md:gap-3">
      <!-- 筛选组：移动端两行，桌面单行 -->
      <div class="flex min-w-0 flex-wrap items-center gap-2">
        <el-input
          v-model="query.keyword"
          class="w-full md:w-50!"
          placeholder="标题 / 内容"
          clearable
          @clear="reload"
          @keyup.enter="reload"
        />

        <el-select
          v-model="query.type"
          class="flex-1 md:flex-none"
          placeholder="全部类型"
          style="width: 140px"
          clearable
          @change="reload"
        >
          <el-option
            v-for="opt in MESSAGE_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>

        <el-select
          v-model="query.isRead"
          class="flex-1 md:flex-none"
          placeholder="全部状态"
          style="width: 120px"
          clearable
          @change="reload"
        >
          <el-option
            v-for="opt in MESSAGE_READ_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </div>

      <!-- 未读计数 + 全部标已读 -->
      <div class="flex items-center justify-between gap-2 md:ml-auto">
        <span class="text-sm text-slate-500">
          未读
          <span class="font-medium text-rose-500">{{ unreadCount }}</span>
          条
        </span>
        <el-button :disabled="unreadCount === 0" plain @click="handleMarkAllRead">
          全部标为已读
        </el-button>
      </div>
    </div>

    <!-- 列表 -->
    <div v-loading="loading" class="min-h-0 flex-1 overflow-y-auto">
      <el-empty v-if="!loading && !list.length" description="暂无消息" />

      <ul v-else class="space-y-2">
        <li
          v-for="msg in list"
          :key="msg.id"
          :class="[
            'group cursor-pointer rounded-lg border bg-white p-3 transition hover:shadow-sm',
            msg.isRead === MESSAGE_READ_STATUS.UNREAD
              ? 'border-rose-100 bg-rose-50/30'
              : 'border-slate-200',
          ]"
          @click="handleDetail(msg)"
        >
          <!-- 第一行：未读点 + 标题 + 类型 -->
          <div class="flex items-start gap-2">
            <span
              v-if="msg.isRead === MESSAGE_READ_STATUS.UNREAD"
              class="mt-1.5 inline-block h-2 w-2 shrink-0 rounded-full bg-rose-500"
            />
            <span
              :class="[
                'min-w-0 flex-1 truncate text-sm',
                msg.isRead === MESSAGE_READ_STATUS.UNREAD ? 'font-medium' : 'text-slate-700',
              ]"
            >
              {{ msg.title || '-' }}
            </span>
            <el-tag :type="getTypeTagType(msg.type)" class="shrink-0" effect="plain" size="small">
              {{ msg.typeLabel || messageTypeLabel(msg.type) }}
            </el-tag>
          </div>

          <!-- 内容摘要 -->
          <p v-if="msg.content" class="mt-1 line-clamp-2 text-xs leading-relaxed text-slate-500">
            {{ msg.content }}
          </p>

          <!-- 底部：时间 / 关联 / 操作 -->
          <div class="mt-2 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-slate-400">
            <span>{{ msg.createdAt || '-' }}</span>
            <span v-if="msg.targetType">
              {{ msg.targetTypeLabel || messageTargetTypeLabel(msg.targetType) }}
            </span>
            <div class="ml-auto flex items-center gap-2">
              <el-button
                v-if="msg.isRead === MESSAGE_READ_STATUS.UNREAD"
                size="small"
                type="primary"
                link
                @click.stop="handleMarkRead(msg)"
              >
                标为已读
              </el-button>
              <el-button size="small" type="primary" link @click.stop="handleDetail(msg)">
                查看
              </el-button>
            </div>
          </div>
        </li>
      </ul>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="flex shrink-0 justify-center pt-3 md:justify-end">
      <el-pagination
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="load"
      />
    </div>

    <MessageDetailDialog ref="detailDialogRef" @marked-read="handleMarkedRead" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { markAllReadAPI, markReadAPI, pageMyMessagesAPI } from '@/api/message'
import {
  MESSAGE_READ_OPTIONS,
  MESSAGE_READ_STATUS,
  MESSAGE_TYPE_OPTIONS,
  MESSAGE_TYPE_TAG_TYPE,
  messageTargetTypeLabel,
  messageTypeLabel,
} from '@/constants'
import { useMessageStore } from '@/stores'
import type { SysMessageVO } from '@/types/message'
import MessageDetailDialog from './components/MessageDetailDialog.vue'

const detailDialogRef = ref<InstanceType<typeof MessageDetailDialog> | null>(null)

const messageStore = useMessageStore()
const { unreadCount } = storeToRefs(messageStore)

const loading = ref(false)
const list = ref<SysMessageVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const query = reactive<{
  type?: number
  isRead?: number
  keyword?: string
}>({
  type: undefined,
  isRead: undefined,
  keyword: '',
})

async function load() {
  loading.value = true
  try {
    const res = await pageMyMessagesAPI({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      type: query.type,
      isRead: query.isRead,
      keyword: query.keyword || undefined,
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

function getTypeTagType(type?: number) {
  if (type == null) return 'info'
  return MESSAGE_TYPE_TAG_TYPE[type] ?? 'info'
}

function handleDetail(row: SysMessageVO) {
  detailDialogRef.value?.open(row)
}

async function handleMarkRead(row: SysMessageVO) {
  if (!row.id) return
  await markReadAPI(row.id)
  ElMessage.success('已标记为已读')
  await Promise.all([load(), messageStore.loadUnreadCount()])
}

async function handleMarkAllRead() {
  await markAllReadAPI()
  ElMessage.success('全部已标记为已读')
  await Promise.all([load(), messageStore.loadUnreadCount()])
}

/** 详情弹窗里自动标已读后，刷新列表 + 未读数 */
async function handleMarkedRead() {
  await Promise.all([load(), messageStore.loadUnreadCount()])
}

onMounted(async () => {
  void load()
  await messageStore.loadUnreadCount()
})
</script>
