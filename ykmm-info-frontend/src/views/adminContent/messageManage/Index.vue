<template>
  <div class="message-manage flex h-full flex-col">
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="用户ID">
        <el-input
          v-model="userIdInput"
          placeholder="精确查询"
          style="width: 140px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

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

      <el-form-item label="关联业务">
        <el-select v-model="query.targetType" placeholder="全部" style="width: 140px" clearable>
          <el-option
            v-for="opt in MESSAGE_TARGET_TYPE_OPTIONS"
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
    </TableToolbar>

    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id">
        <template #title="{ row }">
          <span :class="row.isRead === MESSAGE_READ_STATUS.UNREAD ? 'font-medium' : ''">
            {{ row.title || '-' }}
          </span>
        </template>

        <template #type="{ row }">
          <el-tag :type="getTypeTagType(row.type)" effect="plain" size="small">
            {{ row.typeLabel || messageTypeLabel(row.type) }}
          </el-tag>
        </template>

        <template #targetType="{ row }">
          <span v-if="row.targetType">
            {{ row.targetTypeLabel || messageTargetTypeLabel(row.targetType) }}
            <span v-if="row.targetId" class="ml-1 text-slate-400">#{{ row.targetId }}</span>
          </span>
          <span v-else class="text-slate-300">-</span>
        </template>

        <template #isRead="{ row }">
          <el-tag
            :type="row.isRead === MESSAGE_READ_STATUS.READ ? 'success' : 'warning'"
            effect="plain"
            size="small"
          >
            {{ row.isRead === MESSAGE_READ_STATUS.READ ? '已读' : '未读' }}
          </el-tag>
        </template>

        <template #action="{ row }">
          <el-button size="small" type="primary" link @click="handleDetail(row)">查看</el-button>
        </template>
      </ProTable>
    </el-card>

    <MessageDetailDialog ref="detailDialogRef" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { pageMessagesAPI } from '@/api/message'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import {
  MESSAGE_READ_OPTIONS,
  MESSAGE_READ_STATUS,
  MESSAGE_TARGET_TYPE_OPTIONS,
  MESSAGE_TYPE_OPTIONS,
  MESSAGE_TYPE_TAG_TYPE,
  messageTargetTypeLabel,
  messageTypeLabel,
} from '@/constants'
import type { SysMessagePageQueryDTO, SysMessageVO } from '@/types/message'
import MessageDetailDialog from './components/MessageDetailDialog.vue'

const tableRef = ref<ProTableExpose<SysMessageVO>>()
const detailDialogRef = ref<InstanceType<typeof MessageDetailDialog> | null>(null)

/** 用户ID用字符串存，避免输入过程中出现 NaN */
const userIdInput = ref('')

const query = reactive<{
  type?: number
  targetType?: number
  isRead?: number
  keyword?: string
}>({
  type: undefined,
  targetType: undefined,
  isRead: undefined,
  keyword: '',
})

const columns: ProTableColumn<SysMessageVO>[] = [
  { label: 'ID', prop: 'id', width: 90, align: 'center' },
  { label: '用户ID', prop: 'userId', width: 110, align: 'center' },
  { label: '标题', minWidth: 220, slot: 'title', showOverflowTooltip: true },
  { label: '类型', width: 110, align: 'center', slot: 'type' },
  { label: '关联业务', width: 150, align: 'center', slot: 'targetType' },
  { label: '状态', width: 90, align: 'center', slot: 'isRead' },
  {
    label: '创建时间',
    prop: 'createdAt',
    width: 170,
    align: 'center',
    showOverflowTooltip: true,
  },
  {
    label: '操作',
    width: 100,
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

async function fetchList(params: SysMessagePageQueryDTO): Promise<PageResult<SysMessageVO>> {
  const res = await pageMessagesAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

function parseUserId(): number | undefined {
  const raw = userIdInput.value.trim()
  if (!raw) return undefined
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? n : undefined
}

function handleSearch() {
  tableRef.value?.search({
    userId: parseUserId(),
    type: query.type ?? undefined,
    targetType: query.targetType ?? undefined,
    isRead: query.isRead ?? undefined,
    keyword: query.keyword || undefined,
  })
}

function handleReset() {
  userIdInput.value = ''
  query.type = undefined
  query.targetType = undefined
  query.isRead = undefined
  query.keyword = ''
  tableRef.value?.reset()
}

function handleDetail(row: SysMessageVO) {
  detailDialogRef.value?.open(row)
}
</script>
