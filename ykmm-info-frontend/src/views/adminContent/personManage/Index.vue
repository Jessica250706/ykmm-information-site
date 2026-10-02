<template>
  <div class="person-manage flex h-full flex-col">
    <!-- 搜索 + 操作栏 -->
    <TableToolbar :model="query" align="start" @reset="handleReset" @search="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="中文名 / 日文名 / 罗马音"
          style="width: 220px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="类型">
        <el-select v-model="query.personType" placeholder="全部" style="width: 140px" clearable>
          <el-option
            v-for="opt in PERSON_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="经纪公司">
        <el-select
          v-model="query.agencyId"
          placeholder="全部"
          style="width: 180px"
          clearable
          filterable
        >
          <el-option v-for="a in agencyStore.agencies" :key="a.id" :label="a.name" :value="a.id!" />
        </el-select>
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增人物
        </el-button>
      </template>
    </TableToolbar>

    <!-- 表格区 -->
    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id" stripe>
        <!-- 头像 -->
        <template #avatar="{ row }">
          <el-avatar :size="40" :src="row.avatar">
            {{ row.nameCn?.charAt(0) || 'P' }}
          </el-avatar>
        </template>

        <!-- 名称 -->
        <template #name="{ row }">
          <div class="leading-tight">
            <div class="font-medium">{{ row.nameCn || '-' }}</div>
            <div class="text-xs text-slate-400">
              {{ row.nameJp || row.nameRomaji || '' }}
            </div>
          </div>
        </template>

        <!-- 类型 -->
        <template #personType="{ row }">
          <el-tag :type="row.personType === PERSON_TYPE.IDOL ? 'danger' : 'primary'" effect="plain">
            {{ PERSON_TYPE_LABEL[row.personType as PersonTypeValue] ?? '未知' }}
          </el-tag>
        </template>

        <!-- 应援色 -->
        <template #themeColor="{ row }">
          <span v-if="row.themeColor" class="inline-flex items-center gap-2">
            <span
              :style="{ backgroundColor: row.themeColor }"
              class="h-4 w-4 rounded-full border border-slate-200"
            />
            <span class="text-xs text-slate-500">{{ row.themeColor }}</span>
          </span>
          <span v-else class="text-slate-300">-</span>
        </template>

        <!-- 所属（偶像显示团体，经纪人显示公司） -->
        <template #belong="{ row }">
          <div class="flex flex-wrap gap-1 justify-center">
            <template v-if="row.personType === PERSON_TYPE.IDOL">
              <el-tag
                v-for="g in row.groups ?? []"
                :key="g.id"
                effect="plain"
                size="small"
                type="info"
              >
                {{ g.name }}
              </el-tag>
              <span v-if="!row.groups?.length" class="text-slate-300">-</span>
            </template>
            <template v-else>
              <el-tag
                v-for="a in row.agencies ?? []"
                :key="a.id"
                effect="plain"
                size="small"
                type="info"
              >
                {{ a.name }}
              </el-tag>
              <span v-if="!row.agencies?.length" class="text-slate-300">-</span>
            </template>
          </div>
        </template>

        <!-- 操作 -->
        <template #action="{ row }">
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
import { deletePersonAPI, listPersonAPI } from '@/api/person'
import { type PageResult, type ProTableColumn, type ProTableExpose } from '@/components/ProTable'
import {
  PERSON_TYPE,
  PERSON_TYPE_LABEL,
  PERSON_TYPE_OPTIONS,
  type PersonTypeValue,
} from '@/constants/person'
import { useAgencyStore } from '@/stores/agencyStore'
import type { PersonPageQueryDTO, PersonVO } from '@/types/person'

const router = useRouter()
const agencyStore = useAgencyStore()

/* -------- 表格 ref -------- */
type TableInstance = ProTableExpose<PersonVO>
const tableRef = ref<TableInstance>()

/* -------- 搜索参数 -------- */
const query = reactive<{
  keyword?: string
  personType?: PersonTypeValue
  agencyId?: number
}>({
  keyword: '',
  personType: undefined,
  agencyId: undefined,
})

/* -------- 列配置 -------- */
const columns: ProTableColumn<PersonVO>[] = [
  { label: '头像', minWidth: 80, align: 'center', slot: 'avatar', fixed: 'left' },
  { label: '名称', minWidth: 160, align: 'center', slot: 'name', fixed: 'left' },
  { prop: 'cv', label: '声优', minWidth: 120, align: 'center' },
  { prop: 'age', label: '年龄', minWidth: 80, align: 'center' },
  { prop: 'birthday', label: '生日', minWidth: 80, align: 'center' },
  { prop: 'height', label: '身高(cm)', minWidth: 90, align: 'center' },
  { prop: 'weight', label: '体重(kg)', minWidth: 90, align: 'center' },
  { prop: 'bloodTypeLabel', label: '血型', minWidth: 80, align: 'center' },
  { prop: 'personType', label: '类型', minWidth: 100, align: 'center', slot: 'personType' },
  { label: '应援色', minWidth: 180, align: 'center', slot: 'themeColor' },
  { label: '所属', minWidth: 200, align: 'center', slot: 'belong' },
  { label: '操作', minWidth: 140, align: 'center', fixed: 'right', slot: 'action' },
]

/* -------- 请求适配 -------- */
async function fetchList(
  params: PersonPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<PersonVO>> {
  const res = await listPersonAPI(params)

  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    keyword: query.keyword || undefined,
    personType: query.personType ?? undefined,
    agencyId: query.agencyId ?? undefined,
  })
}

function handleReset() {
  query.keyword = ''
  query.personType = undefined
  query.agencyId = undefined
  tableRef.value?.reset()
}

/* -------- 新增 / 编辑 -------- */
function handleCreate() {
  router.push({ name: 'AdminPersonCreate' })
}

function handleEdit(row: PersonVO) {
  router.push({
    name: 'AdminPersonEdit',
    params: { id: String(row.id) },
  })
}

/* -------- 删除 -------- */
async function handleDelete(row: PersonVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除人物「${row.nameCn ?? row.nameJp ?? row.id}」吗？此操作不可恢复。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '删除',
        confirmButtonClass: 'el-button--danger',
      },
    )
  } catch {
    return
  }

  await deletePersonAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}

/* -------- 初始化 -------- */
onMounted(() => {
  // 保证经纪公司下拉有数据
  void agencyStore.loadAll()
})
</script>
