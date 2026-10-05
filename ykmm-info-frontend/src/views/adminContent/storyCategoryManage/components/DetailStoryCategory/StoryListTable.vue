<template>
  <ProTable ref="tableRef" :columns="columns" :request="fetchData" row-key="id">
    <template #storyTitle="{ row }">
      <span class="font-medium">{{ row.title || `剧情 #${row.id}` }}</span>
    </template>

    <template #storyDescription="{ row }">
      <span class="text-slate-500 line-clamp-1">{{ row.description ?? '-' }}</span>
    </template>

    <template #storyStatus="{ row }">
      <el-tag :type="storyStatusTag(row.status)" effect="plain">
        {{ row.statusLabel ?? '未知' }}
      </el-tag>
    </template>

    <template #storyAction="{ row }">
      <el-button size="small" type="primary" link @click="emit('edit', row)">编辑</el-button>
      <el-button size="small" type="primary" link @click="emit('view', row)">查看对话</el-button>
      <el-button size="small" type="danger" link @click="emit('delete', row)">删除</el-button>
    </template>
  </ProTable>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { listStoryByCategoryAPI } from '@/api/story'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import type { StoryVO } from '@/types/story'
import { storyStatusTag } from '@/utils/helpers'

const props = defineProps<{
  /** 分类 ID：请求该分类下的剧情 */
  categoryId: number
}>()

const emit = defineEmits<{
  edit: [row: StoryVO]
  view: [row: StoryVO]
  delete: [row: StoryVO]
}>()

const columns: ProTableColumn<StoryVO>[] = [
  { prop: 'title', label: '标题', minWidth: 240, slot: 'storyTitle' },
  { prop: 'description', label: '描述', minWidth: 240, slot: 'storyDescription' },
  { prop: 'status', label: '审核状态', width: 120, align: 'center', slot: 'storyStatus' },
  { prop: 'sort', label: '排序', width: 100, align: 'center' },
  {
    prop: 'createdAt',
    label: '创建时间',
    minWidth: 180,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', minWidth: 220, align: 'center', fixed: 'right', slot: 'storyAction' },
]

async function fetchData(): Promise<PageResult<StoryVO>> {
  const res = await listStoryByCategoryAPI(props.categoryId)
  const records = res.data ?? []
  return { records, total: records.length }
}

const tableRef = ref<ProTableExpose<StoryVO>>()

defineExpose({
  refresh: () => tableRef.value?.refresh(),
})
</script>
