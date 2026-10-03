<template>
  <div class="story-category-type-manage flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <ProTable
        ref="tableRef"
        v-loading="loading"
        :columns="columns"
        :data="list"
        :pagination="false"
        row-key="id"
      >
        <template #name="{ row }">
          <el-tag
            :style="{
              borderColor: `var(--color-${row.color})`,
              color: `var(--color-${row.color})`,
            }"
            effect="plain"
          >
            {{ row.name }}
          </el-tag>
        </template>

        <template #description="{ row }">
          <span class="text-slate-600">{{ row.description || '-' }}</span>
        </template>

        <template #createdAt="{ row }">
          {{ row.createdAt || '-' }}
        </template>

        <template #action="{ row }">
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
        </template>
      </ProTable>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { listStoryCategoryTypeAPI } from '@/api/storyCategoryType'
import { ProTable, type ProTableColumn } from '@/components/ProTable'
import type { StoryCategoryTypeVO } from '@/types/storyCategoryType'

const router = useRouter()

const loading = ref(false)
const list = ref<StoryCategoryTypeVO[]>([])

const columns: ProTableColumn<StoryCategoryTypeVO>[] = [
  { prop: 'id', label: 'ID', width: 80, align: 'center' },
  { prop: 'name', label: '名称', minWidth: 140, align: 'center', slot: 'name' },
  { prop: 'description', label: '描述', minWidth: 260, slot: 'description' },
  { prop: 'sort', label: '排序', width: 100, align: 'center' },
  { prop: 'createdAt', label: '创建时间', minWidth: 180, align: 'center', slot: 'createdAt' },
  { label: '操作', width: 120, align: 'center', fixed: 'right', slot: 'action' },
]

async function loadList() {
  loading.value = true
  try {
    const res = await listStoryCategoryTypeAPI()
    list.value = res.data ?? []
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function handleEdit(row: StoryCategoryTypeVO) {
  router.push({
    name: 'AdminStoryCategoryTypeEdit',
    params: { id: String(row.id) },
  })
}

onMounted(loadList)
</script>
