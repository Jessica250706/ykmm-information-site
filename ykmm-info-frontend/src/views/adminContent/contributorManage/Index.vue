<template>
  <div class="contributor-manage flex h-full flex-col">
    <!-- 搜索 + 操作栏 -->
    <TableToolbar :model="query" align="start" @reset="handleReset" @search="handleSearch">
      <el-form-item label="版本ID">
        <el-input-number
          v-model="query.versionId"
          :controls="false"
          :min="1"
          placeholder="版本ID"
          style="width: 140px"
        />
      </el-form-item>

      <el-form-item label="用户ID">
        <el-input-number
          v-model="query.userId"
          :controls="false"
          :min="1"
          placeholder="用户ID"
          style="width: 140px"
        />
      </el-form-item>

      <el-form-item label="角色">
        <el-select
          v-model="query.contributorRole"
          placeholder="全部"
          style="width: 160px"
          clearable
        >
          <el-option
            v-for="opt in CONTRIBUTOR_ROLE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增贡献者
        </el-button>
      </template>
    </TableToolbar>

    <!-- 表格区 -->
    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id" stripe>
        <!-- 贡献者展示 -->
        <template #contributor="{ row }">
          <div class="flex items-center gap-2">
            <el-avatar :size="32" :src="row.avatar">
              {{ (row.displayName || '?').charAt(0) }}
            </el-avatar>
            <div class="leading-tight text-left">
              <div class="font-medium">{{ row.displayName || '匿名' }}</div>
              <div v-if="row.uid" class="text-xs text-slate-400">{{ row.uid }}</div>
            </div>
          </div>
        </template>

        <!-- 角色 -->
        <template #contributorRole="{ row }">
          <el-tag :type="roleTagType(row.contributorRole)" effect="plain">
            {{ row.contributorRoleLabel ?? contributorRoleLabel(row.contributorRole) }}
          </el-tag>
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
import { reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  deleteDialogueVersionContributorAPI,
  pageDialogueVersionContributorsAPI,
} from '@/api/dialogueVersionContributor'
import { type PageResult, type ProTableColumn, type ProTableExpose } from '@/components/ProTable'
import {
  AdminRouteName,
  CONTRIBUTOR_ROLE,
  CONTRIBUTOR_ROLE_OPTIONS,
  contributorRoleLabel,
  type ContributorRoleValue,
} from '@/constants'
import type {
  DialogueVersionContributorPageQueryDTO,
  DialogueVersionContributorVO,
} from '@/types/dialogueVersionContributor'

const router = useRouter()

/* -------- 表格 ref -------- */
type TableInstance = ProTableExpose<DialogueVersionContributorVO>
const tableRef = ref<TableInstance>()

/* -------- 搜索参数 -------- */
const query = reactive<{
  versionId?: number
  userId?: number
  contributorRole?: ContributorRoleValue
}>({
  versionId: undefined,
  userId: undefined,
  contributorRole: undefined,
})

/* -------- 列配置 -------- */
const columns: ProTableColumn<DialogueVersionContributorVO>[] = [
  { prop: 'id', label: 'ID', width: 80, align: 'center', fixed: 'left' },
  { label: '贡献者', minWidth: 220, align: 'left', slot: 'contributor', fixed: 'left' },
  { prop: 'versionId', label: '版本ID', minWidth: 100, align: 'center' },
  { prop: 'userId', label: '用户ID', minWidth: 100, align: 'center' },
  { prop: 'contributorName', label: '贡献者姓名', minWidth: 140, align: 'center' },
  {
    prop: 'contributorRole',
    label: '角色',
    minWidth: 120,
    align: 'center',
    slot: 'contributorRole',
  },
  {
    prop: 'createdAt',
    label: '创建时间',
    minWidth: 180,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', minWidth: 140, align: 'center', fixed: 'right', slot: 'action' },
]

/* -------- tag 类型 -------- */
function roleTagType(role?: number): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  switch (role) {
    case CONTRIBUTOR_ROLE.AUTHOR:
      return 'primary'
    case CONTRIBUTOR_ROLE.DELEGATE:
      return 'warning'
    case CONTRIBUTOR_ROLE.COLLABORATOR:
      return 'info'
    default:
      return 'info'
  }
}

/* -------- 请求适配 -------- */
async function fetchList(
  params: DialogueVersionContributorPageQueryDTO & { pageNum: number; pageSize: number },
): Promise<PageResult<DialogueVersionContributorVO>> {
  const res = await pageDialogueVersionContributorsAPI(params)
  return {
    records: res.data.records ?? [],
    total: res.data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    versionId: query.versionId ?? undefined,
    userId: query.userId ?? undefined,
    contributorRole: query.contributorRole ?? undefined,
  })
}

function handleReset() {
  query.versionId = undefined
  query.userId = undefined
  query.contributorRole = undefined
  tableRef.value?.reset()
}

/* -------- 新增 / 编辑 -------- */
function handleCreate() {
  router.push({ name: AdminRouteName.CONTRIBUTOR_CREATE })
}

function handleEdit(row: DialogueVersionContributorVO) {
  router.push({
    name: AdminRouteName.CONTRIBUTOR_EDIT,
    params: { id: String(row.id) },
  })
}

/* -------- 删除 -------- */
async function handleDelete(row: DialogueVersionContributorVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除贡献者「${row.displayName ?? row.id}」吗？此操作不可恢复。`,
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

  await deleteDialogueVersionContributorAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}
</script>
