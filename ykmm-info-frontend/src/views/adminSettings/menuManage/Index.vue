<template>
  <div class="menu-manage flex h-full flex-col">
    <!-- 顶部操作栏 -->
    <TableToolbar :model="query" @reset="handleReset" @search="handleSearch">
      <el-form-item label="菜单类型">
        <el-select v-model="query.menuType" placeholder="全部" style="width: 140px" clearable>
          <el-option
            v-for="opt in MENU_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleCreate()">
          <el-icon><Plus /></el-icon>
          新增顶级菜单
        </el-button>
      </template>
    </TableToolbar>

    <!-- 树形表格 -->
    <el-card class="flex-1" shadow="never">
      <ProTable
        ref="tableRef"
        :columns="columns"
        :data="menuTree"
        :tree-props="{ children: 'children' }"
        row-key="id"
        default-expand-all
      >
        <!-- 名称列 -->
        <template #name="{ row }">
          <span class="inline-flex items-center gap-2">
            <span v-if="row.icon" class="text-base leading-none">{{ row.icon }}</span>
            <span>{{ row.name }}</span>
          </span>
        </template>

        <!-- 菜单类型 -->
        <template #menuType="{ row }">
          <el-tag :type="row.menuType === MENU_TYPE.ADMIN ? 'danger' : 'primary'" effect="plain">
            {{ MENU_TYPE_LABEL[row.menuType as MenuTypeValue] ?? '未知' }}
          </el-tag>
        </template>

        <!-- 显示状态 -->
        <template #visible="{ row }">
          <el-tag :type="row.visible === MENU_VISIBLE.VISIBLE ? 'success' : 'info'">
            {{ MENU_VISIBLE_LABEL[row.visible as MenuVisibleValue] ?? '未知' }}
          </el-tag>
        </template>

        <!-- 操作 -->
        <template #action="{ row }">
          <el-button size="small" type="primary" link @click="handleCreate(row)">
            新增子菜单
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
import { deleteMenuAPI, getMenuTreeAPI } from '@/api/menu'
import { ProTable, type ProTableColumn } from '@/components/ProTable'
import {
  AdminRouteName,
  MENU_TYPE,
  MENU_TYPE_LABEL,
  MENU_TYPE_OPTIONS,
  MENU_VISIBLE,
  MENU_VISIBLE_LABEL,
  type MenuTypeValue,
  type MenuVisibleValue,
} from '@/constants'
import { useMenuStore } from '@/stores'
import type { MenuVO } from '@/types/menu'

const router = useRouter()
const menuStore = useMenuStore()

const query = reactive<{
  menuType?: MenuTypeValue
}>({
  menuType: undefined,
})

const menuTree = ref<MenuVO[]>([])

const columns: ProTableColumn<MenuVO>[] = [
  { prop: 'name', label: '菜单名称', minWidth: 220, slot: 'name' },
  { prop: 'path', label: '路由路径', minWidth: 220, showOverflowTooltip: true },
  { prop: 'component', label: '组件', minWidth: 240, showOverflowTooltip: true },
  { prop: 'menuType', label: '类型', width: 100, align: 'center', slot: 'menuType' },
  { prop: 'sort', label: '排序', width: 80, align: 'center' },
  { prop: 'visible', label: '显示', width: 90, align: 'center', slot: 'visible' },
  { label: '操作', width: 220, align: 'center', fixed: 'right', slot: 'action' },
]

/* -------- 搜索 / 重置 -------- */
function handleSearch() {
  void loadData()
}

function handleReset() {
  query.menuType = undefined
  void loadData()
}

async function loadData() {
  const res = await getMenuTreeAPI(query.menuType)
  menuTree.value = res.data ?? []
}

function handleCreate(parent?: MenuVO) {
  router.push({
    name: AdminRouteName.MENU_CREATE,
    query: parent ? { parentId: String(parent.id), menuType: String(parent.menuType) } : {},
  })
}

function handleEdit(row: MenuVO) {
  router.push({
    name: AdminRouteName.MENU_EDIT,
    params: { id: String(row.id) },
  })
}

async function handleDelete(row: MenuVO) {
  try {
    await ElMessageBox.confirm(`确定要删除菜单「${row.name}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger',
    })
  } catch {
    return
  }

  if (!row.id) {
    ElMessage.error('所选菜单不存在')
    return
  }
  await deleteMenuAPI(row.id)
  ElMessage.success('删除成功')
  // 同步侧边栏
  await menuStore.loadAll()
  await loadData()
}

onMounted(loadData)
</script>

<style lang="scss" scoped></style>
