<template>
  <div class="user-manage">
    <!-- 搜索区 -->
    <el-card class="mb-4" shadow="never">
      <el-form :model="query" inline @submit.prevent>
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
            <el-option :value="1" label="正常" />
            <el-option :value="0" label="禁用" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card shadow="never">
      <ProTable ref="tableRef" :columns="columns" :request="fetchList" row-key="id" border stripe>
        <!-- 头像 -->
        <template #avatar="{ row }">
          <el-avatar :size="32" :src="row.avatar">
            {{ row.nickname?.charAt(0) || 'U' }}
          </el-avatar>
        </template>

        <!-- 角色 -->
        <template #role="{ row }">
          <el-tag :type="row.role === ROLE.ADMIN ? 'danger' : 'info'" effect="plain">
            {{ row.role === ROLE.ADMIN ? '管理员' : '普通用户' }}
          </el-tag>
        </template>

        <!-- 状态 -->
        <template #status="{ row }">
          <el-tag
            :type="row.status === 1 ? 'success' : 'danger'"
            class="cursor-pointer"
            @click="handleStatusToggle(row)"
          >
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>

        <!-- 操作 -->
        <template #action="{ row }">
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
import { ROLE } from '@/constants/index.ts'
import type { UserInfo, UserRequest } from '@/types/user'
import UserEditDialog from './components/UserEditDialog.vue'

/* -------- 表格 ref -------- */
type TableInstance = ProTableExpose<UserInfo>
const tableRef = ref<TableInstance>()

/* -------- 搜索参数 -------- */
const query = reactive<{
  keyword?: string
  status?: number
}>({
  keyword: '',
  status: undefined,
})

/* -------- 列配置 -------- */
const columns: ProTableColumn<UserInfo>[] = [
  { label: '头像', width: 80, align: 'center', slot: 'avatar' },
  { prop: 'nickname', label: '昵称', minWidth: 120 },
  { prop: 'email', label: '邮箱', minWidth: 200, showOverflowTooltip: true },
  { prop: 'uid', label: 'UID', minWidth: 180, showOverflowTooltip: true },
  { prop: 'role', label: '角色', width: 100, align: 'center', slot: 'role' },
  { prop: 'status', label: '状态', width: 90, align: 'center', slot: 'status' },
  { prop: 'lastLoginTime', label: '最后登录', width: 170, showOverflowTooltip: true },
  { label: '操作', width: 130, align: 'center', fixed: 'right', slot: 'action' },
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

/* -------- 状态切换 -------- */
async function handleStatusToggle(row: UserInfo) {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 1 ? '启用' : '禁用'

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
