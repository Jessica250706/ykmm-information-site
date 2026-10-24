<template>
  <div class="flex h-full min-h-0 flex-col">
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="类型">
        <el-select v-model="query.type" placeholder="全部" style="width: 140px" clearable>
          <el-option
            v-for="opt in MESSAGE_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="query.isRead" placeholder="全部" style="width: 120px" clearable>
          <el-option
            v-for="opt in MESSAGE_READ_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="标题 / 内容"
          style="width: 200px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <template #actions>
        <span class="text-sm text-slate-500">
          未读
          <span class="font-medium text-rose-500">{{ unreadCount }}</span>
          条
        </span>
        <el-button :disabled="unreadCount === 0" plain @click="handleMarkAllRead">
          全部标为已读
        </el-button>
      </template>
    </TableToolbar>

    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id">
        <!-- 标题 -->
        <template #title="{ row }">
          <div class="flex items-center gap-2">
            <span
              v-if="row.isRead === MESSAGE_READ_STATUS.UNREAD"
              class="inline-block h-2 w-2 shrink-0 rounded-full bg-rose-500"
            />
            <span :class="row.isRead === MESSAGE_READ_STATUS.UNREAD ? 'font-medium' : ''">
              {{ row.title || '-' }}
            </span>
          </div>
        </template>

        <!-- 类型 -->
        <template #type="{ row }">
          <el-tag :type="getTypeTagType(row.type)" effect="plain" size="small">
            {{ row.typeLabel || messageTypeLabel(row.type) }}
          </el-tag>
        </template>

        <!-- 关联业务 -->
        <template #targetType="{ row }">
          <span v-if="row.targetType">
            {{ row.targetTypeLabel || messageTargetTypeLabel(row.targetType) }}
          </span>
          <span v-else class="text-slate-300">-</span>
        </template>

        <!-- 操作 -->
        <template #action="{ row }">
          <el-button size="small" type="primary" link @click="handleDetail(row)">查看</el-button>
          <el-button
            v-if="row.isRead === MESSAGE_READ_STATUS.UNREAD"
            size="small"
            type="primary"
            link
            @click="handleMarkRead(row)"
          >
            标为已读
          </el-button>
        </template>
      </ProTable>
    </el-card>

    <MessageDetailDialog ref="detailDialogRef" @marked-read="handleMarkedRead" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { markAllReadAPI, markReadAPI, pageMyMessagesAPI } from '@/api/message'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import {
  MESSAGE_READ_OPTIONS,
  MESSAGE_READ_STATUS,
  MESSAGE_TYPE_OPTIONS,
  MESSAGE_TYPE_TAG_TYPE,
  messageTargetTypeLabel,
  messageTypeLabel,
} from '@/constants'
import { useMessageStore } from '@/stores'
import type { SysMessagePageQueryDTO, SysMessageVO } from '@/types/message'
import MessageDetailDialog from './components/MessageDetailDialog.vue'

const tableRef = ref<ProTableExpose<SysMessageVO>>()
const detailDialogRef = ref<InstanceType<typeof MessageDetailDialog> | null>(null)

const messageStore = useMessageStore()
const { unreadCount } = storeToRefs(messageStore)

const query = reactive<{
  type?: number
  isRead?: number
  keyword?: string
}>({
  type: undefined,
  isRead: undefined,
  keyword: '',
})

const columns: ProTableColumn<SysMessageVO>[] = [
  { label: '标题', minWidth: 240, slot: 'title' },
  { label: '类型', width: 110, align: 'center', slot: 'type' },
  { label: '关联业务', width: 120, align: 'center', slot: 'targetType' },
  {
    label: '时间',
    prop: 'createdAt',
    width: 170,
    align: 'center',
    showOverflowTooltip: true,
  },
  {
    label: '操作',
    width: 160,
    align: 'center',
    fixed: 'right',
    slot: 'action',
    displayFlex: true,
  },
]

function getTypeTagType(type?: number) {
  if (type == null) return 'info'
  return MESSAGE_TYPE_TAG_TYPE[type] ?? 'info'
}

/** 拉列表 */
async function fetchList(params: SysMessagePageQueryDTO): Promise<PageResult<SysMessageVO>> {
  const res = await pageMyMessagesAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

function handleSearch() {
  tableRef.value?.search({
    type: query.type ?? undefined,
    isRead: query.isRead ?? undefined,
    keyword: query.keyword || undefined,
  })
}

function handleReset() {
  query.type = undefined
  query.isRead = undefined
  query.keyword = ''
  tableRef.value?.reset()
  void messageStore.loadUnreadCount()
}

function handleDetail(row: SysMessageVO) {
  detailDialogRef.value?.open(row)
}

async function handleMarkRead(row: SysMessageVO) {
  if (!row.id) return
  await markReadAPI(row.id)
  ElMessage.success('已标记为已读')
  await Promise.all([tableRef.value?.refresh(), messageStore.loadUnreadCount()])
}

async function handleMarkAllRead() {
  await markAllReadAPI()
  ElMessage.success('全部已标记为已读')
  await Promise.all([tableRef.value?.refresh(), messageStore.loadUnreadCount()])
}

/** 详情弹窗里自动标已读后，刷新列表 + 未读数 */
async function handleMarkedRead() {
  await Promise.all([tableRef.value?.refresh(), messageStore.loadUnreadCount()])
}

onMounted(async () => {
  await messageStore.loadUnreadCount()
})
</script>
