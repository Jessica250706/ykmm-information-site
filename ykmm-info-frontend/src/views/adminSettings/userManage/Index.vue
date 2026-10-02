<template>
  <div class="user-manage flex h-full flex-col">
    <!-- 搜索区 -->
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="邮箱 / 昵称 / UID"
          style="width: 220px"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" style="width: 140px" clearable>
          <el-option :label="USER_STATUS_LABEL[USER_STATUS.ENABLED]" :value="USER_STATUS.ENABLED" />
          <el-option
            :label="USER_STATUS_LABEL[USER_STATUS.DISABLED]"
            :value="USER_STATUS.DISABLED"
          />
        </el-select>
      </el-form-item>
    </TableToolbar>

    <!-- 表格区 -->
    <el-card class="flex-1" shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id" stripe>
        <!-- 头像 -->
        <template #avatar="{ row }">
          <el-avatar :size="32" :src="row.avatar" class="leading-8">
            {{ row.nickname?.charAt(0) || 'U' }}
          </el-avatar>
        </template>

        <!-- 角色 -->
        <template #role="{ row }">
          <el-tag :type="row.role === ROLE.ADMIN ? 'danger' : 'info'" effect="plain">
            {{ ROLE_LABEL[row.role as RoleValue] }}
          </el-tag>
        </template>

        <!-- 状态 -->
        <template #status="{ row }">
          <el-tag
            :type="row.status === USER_STATUS.ENABLED ? 'success' : 'danger'"
            class="cursor-pointer"
          >
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>

        <!-- 操作列 -->
        <template #action="{ row }">
          <el-button
            :type="row.status === USER_STATUS.ENABLED ? 'danger' : 'primary'"
            size="small"
            link
            @click="handleStatusToggle(row)"
          >
            {{ USER_STATUS_ACTION_LABEL[row.status as UserStatusValue] }}
          </el-button>
          <el-button size="small" type="primary" link @click="handleDetail(row)">详情</el-button>
          <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </ProTable>
    </el-card>

    <!-- 编辑弹窗 -->
    <UserEditDialog v-model="dialogVisible" :user="editingUser" @success="handleEditSuccess" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteUserAPI, listUserAPI, updateUserStatusAPI } from '@/api/user'
import {
  type PageResult,
  ProTable,
  type ProTableColumn,
  type ProTableExpose,
} from '@/components/ProTable'
import {
  ROLE,
  ROLE_LABEL,
  type RoleValue,
  USER_STATUS,
  USER_STATUS_ACTION_LABEL,
  USER_STATUS_LABEL,
  type UserStatusValue,
} from '@/constants/index.ts'
import type { UserInfo, UserRequest } from '@/types/user'
import UserEditDialog from './components/UserEditDialog.vue'

/* -------- 表格 ref -------- */
type TableInstance = ProTableExpose<UserInfo>
const tableRef = ref<TableInstance>()

/* -------- 搜索参数 -------- */
const query = reactive<{
  keyword?: string
  status?: UserStatusValue
}>({
  keyword: '',
  status: undefined,
})

/* -------- 列配置 -------- */
const columns: ProTableColumn<UserInfo>[] = [
  { label: '头像', minWidth: 80, align: 'center', slot: 'avatar' },
  { prop: 'nickname', label: '昵称', minWidth: 120, align: 'center' },
  { prop: 'email', label: '邮箱', minWidth: 200, align: 'center', showOverflowTooltip: true },
  { prop: 'uid', label: 'UID', minWidth: 300, align: 'center', showOverflowTooltip: true },
  { prop: 'role', label: '角色', minWidth: 120, align: 'center', slot: 'role' },
  { prop: 'status', label: '状态', minWidth: 90, align: 'center', slot: 'status' },
  {
    prop: 'lastLoginTime',
    label: '最后登录时间',
    minWidth: 170,
    align: 'center',
    showOverflowTooltip: true,
  },
  { label: '操作', minWidth: 200, align: 'center', fixed: 'right', slot: 'action' },
]

/* -------- 请求适配 -------- */
async function fetchList(
  params: UserRequest & { pageNum: number; pageSize: number },
): Promise<PageResult<UserInfo>> {
  const { data } = await listUserAPI(params)

  return {
    records: data.records ?? [],
    total: data.total ?? 0,
  }
}

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  tableRef.value?.search({
    keyword: query.keyword || undefined,
    status: query.status ?? undefined,
  })
}

function handleReset() {
  query.keyword = ''
  query.status = undefined
  tableRef.value?.reset()
}

/* -------- 状态文案 helper（收窄 row.status 的 any） -------- */
function statusLabel(status?: number): string {
  if (status == null) return '未知'
  return USER_STATUS_LABEL[status as UserStatusValue] ?? '未知'
}

/* -------- 状态切换 -------- */
async function handleStatusToggle(row: UserInfo) {
  const nextStatus = row.status === USER_STATUS.ENABLED ? USER_STATUS.DISABLED : USER_STATUS.ENABLED
  const actionText = USER_STATUS_LABEL[nextStatus]

  try {
    await ElMessageBox.confirm(`确定要${actionText}该用户吗？`, '提示', {
      type: 'warning',
    })
  } catch {
    return
  }

  await updateUserStatusAPI(row.id!, { status: nextStatus })
  ElMessage.success(`${actionText}成功`)
  await tableRef.value?.refresh()
}

/* -------- 编辑：只留开关和当前行 -------- */

function handleDetail(row: UserInfo) {
  // TODO: 跳转到个人中心详情页
}

/* -------- 编辑：只留开关和当前行 -------- */
const dialogVisible = ref(false)
const editingUser = ref<UserInfo | null>(null)

function handleEdit(row: UserInfo) {
  editingUser.value = row
  dialogVisible.value = true
}

function handleEditSuccess() {
  void tableRef.value?.refresh()
}

/* -------- 删除 -------- */
async function handleDelete(row: UserInfo) {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户「${row.nickname ?? row.email}」吗？此操作不可恢复。`,
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

  await deleteUserAPI(row.id!)
  ElMessage.success('删除成功')
  await tableRef.value?.refresh()
}
</script>
