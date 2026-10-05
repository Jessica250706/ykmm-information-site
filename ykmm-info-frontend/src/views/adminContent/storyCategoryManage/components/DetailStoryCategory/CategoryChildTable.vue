<template>
  <ProTable
    ref="tableRef"
    :columns="columns"
    :request="fetchData"
    :tree-props="{ children: 'children' }"
    row-key="id"
    default-expand-all
  >
    <template #name="{ row }">
      <span class="font-medium">{{ row.name }}</span>
    </template>

    <template #categoryType="{ row }">
      <el-tag
        :style="categoryTagStyle(storyCategoryTypeStore.getTypeColor(row.categoryType))"
        effect="plain"
      >
        {{ row.categoryTypeLabel ?? '未知' }}
      </el-tag>
    </template>

    <template #categoryAction="{ row }">
      <el-button size="small" type="primary" link @click="emit('detail', row)">详情</el-button>
      <el-button size="small" type="primary" link @click="emit('createCategory', row)">
        新增子分类
      </el-button>
      <el-button
        v-if="row.children?.length === 0"
        size="small"
        type="primary"
        link
        @click="emit('createStory', row)"
      >
        新增剧情
      </el-button>
      <el-button size="small" type="primary" link @click="emit('edit', row)">编辑</el-button>
      <el-button size="small" type="danger" link @click="emit('delete', row)">删除</el-button>
    </template>
  </ProTable>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { pageStoryCategoryTreeAPI } from '@/api/storyCategory'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import { useStoryCategoryTypeStore } from '@/stores/storyCategoryTypeStore'
import type { StoryCategoryPageQueryDTO, StoryCategoryVO } from '@/types/storyCategory'
import { categoryTagStyle } from '@/utils/helpers'

const props = defineProps<{
  /** 父分类 ID：请求子分类时带上 */
  parentId: number
}>()

const emit = defineEmits<{
  detail: [row: StoryCategoryVO]
  createCategory: [row: StoryCategoryVO]
  createStory: [row: StoryCategoryVO]
  edit: [row: StoryCategoryVO]
  delete: [row: StoryCategoryVO]
}>()

const storyCategoryTypeStore = useStoryCategoryTypeStore()

const columns: ProTableColumn<StoryCategoryVO>[] = [
  { prop: 'name', label: '分类名', minWidth: 240, slot: 'name' },
  { prop: 'categoryType', label: '分类类型', width: 140, align: 'center', slot: 'categoryType' },
  { prop: 'sort', label: '排序', width: 100, align: 'center' },
  {
    prop: 'createdAt',
    label: '创建时间',
    minWidth: 180,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', minWidth: 300, align: 'center', fixed: 'right', slot: 'categoryAction' },
]

async function fetchData(params: {
  pageNum: number
  pageSize: number
  [k: string]: unknown
}): Promise<PageResult<StoryCategoryVO>> {
  const res = await pageStoryCategoryTreeAPI({
    ...(params as StoryCategoryPageQueryDTO),
    parentId: props.parentId,
  })
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

const tableRef = ref<ProTableExpose<StoryCategoryVO>>()

defineExpose({
  refresh: () => tableRef.value?.refresh(),
})
</script>
