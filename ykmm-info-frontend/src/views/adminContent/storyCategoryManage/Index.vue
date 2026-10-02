<template>
  <div class="story-category-manage flex h-full flex-col">
    <!-- 顶部操作栏 -->
    <el-card class="search mb-4" shadow="never">
      <div class="flex items-center justify-between">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="分类类型">
            <el-select
              v-model="query.categoryType"
              placeholder="全部"
              style="width: 160px"
              clearable
              @change="loadData"
            >
              <el-option
                v-for="opt in STORY_CATEGORY_TYPE_OPTIONS"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <el-button type="primary" @click="handleCreate()">
          <el-icon><Plus /></el-icon>
          新增顶级分类
        </el-button>
      </div>
    </el-card>

    <!-- 树形表格 -->
    <el-card class="flex-1" shadow="never">
      <ProTable
        ref="tableRef"
        :columns="columns"
        :data="categoryTree"
        :pagination="false"
        :tree-props="{ children: 'children' }"
        row-key="id"
        default-expand-all
      >
        <!-- 分类名 -->
        <template #name="{ row }">
          <span class="font-medium">{{ row.name }}</span>
        </template>

        <!-- 类型 -->
        <template #categoryType="{ row }">
          <el-tag :type="categoryTypeTag(row.categoryType)" effect="plain">
            {{ STORY_CATEGORY_TYPE_LABEL[row.categoryType as StoryCategoryTypeValue] ?? '未知' }}
          </el-tag>
        </template>

        <!-- 操作 -->
        <template #action="{ row }">
          <el-button size="small" type="primary" link @click="handleCreate(row)">
            新增子分类
          </el-button>
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </ProTable>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteStoryCategoryAPI, listStoryCategoryAPI } from '@/api/storyCategory'
import { ProTable, type ProTableColumn } from '@/components/ProTable'
import {
  STORY_CATEGORY_TYPE,
  STORY_CATEGORY_TYPE_LABEL,
  STORY_CATEGORY_TYPE_OPTIONS,
  type StoryCategoryTypeValue,
} from '@/constants/story'
import type { StoryCategoryVO } from '@/types/storyCategory'

const router = useRouter()

const query = reactive<{
  categoryType?: StoryCategoryTypeValue
}>({
  categoryType: undefined,
})

const categoryTree = ref<StoryCategoryVO[]>([])
const tableRef = ref()

const columns: ProTableColumn<StoryCategoryVO>[] = [
  { prop: 'name', label: '分类名', minWidth: 240, slot: 'name' },
  { prop: 'categoryType', label: '分类类型', minWidth: 140, align: 'center', slot: 'categoryType' },
  { prop: 'sort', label: '排序', width: 100, align: 'center' },
  {
    prop: 'createdAt',
    label: '创建时间',
    minWidth: 180,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', width: 220, align: 'center', fixed: 'right', slot: 'action' },
]

/** 不同分类类型给不同 tag 颜色 */
function categoryTypeTag(type?: number): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  switch (type) {
    case STORY_CATEGORY_TYPE.MAIN:
      return 'danger'
    case STORY_CATEGORY_TYPE.RAINBOW_CITY:
      return 'warning'
    case STORY_CATEGORY_TYPE.SPECIAL:
      return 'success'
    case STORY_CATEGORY_TYPE.ACTIVITY:
      return 'primary'
    case STORY_CATEGORY_TYPE.DRAMA:
      return 'info'
    default:
      return 'info'
  }
}

async function loadData() {
  const res = await listStoryCategoryAPI({ categoryType: query.categoryType })
  categoryTree.value = res.data ?? []
}

function handleCreate(parent?: StoryCategoryVO) {
  router.push({
    name: 'AdminStoryCategoryCreate',
    query: parent ? { parentId: String(parent.id), categoryType: String(parent.categoryType) } : {},
  })
}

function handleEdit(row: StoryCategoryVO) {
  router.push({
    name: 'AdminStoryCategoryEdit',
    params: { id: String(row.id) },
  })
}

async function handleDelete(row: StoryCategoryVO) {
  try {
    await ElMessageBox.confirm(`确定要删除分类「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger',
    })
  } catch {
    return
  }

  await deleteStoryCategoryAPI(row.id!)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>
