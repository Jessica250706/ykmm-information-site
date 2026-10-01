<template>
  <div class="menu-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑菜单' : '新增菜单' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-2xl" label-width="100px">
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType" @change="handleMenuTypeChange">
            <el-radio v-for="opt in MENU_TYPE_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="父菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentTreeOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            :value-on-clear="0"
            placeholder="不选则为顶级菜单"
            style="width: 100%"
            check-strictly
            clearable
          />
        </el-form-item>

        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>

        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="如：⚙️ 或 Element Plus 图标名" />
        </el-form-item>

        <el-form-item label="路由路径" prop="path">
          <el-input v-model="form.path" placeholder="如：/admin/settings/menu" />
        </el-form-item>

        <el-form-item label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="如：adminSettings/menuManage/Index" />
        </el-form-item>

        <el-form-item label="权限标识" prop="permission">
          <el-input v-model="form.permission" placeholder="如：menu:add" />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :max="9999" :min="0" />
        </el-form-item>

        <el-form-item label="显示状态" prop="visible">
          <el-radio-group v-model="form.visible">
            <el-radio :value="MENU_VISIBLE.VISIBLE">显示</el-radio>
            <el-radio :value="MENU_VISIBLE.HIDDEN">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button :loading="loading" type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { createMenuAPI, getMenuTreeAPI, updateMenuAPI } from '@/api/menu'
import { MENU_TYPE, MENU_TYPE_OPTIONS, MENU_VISIBLE, type MenuTypeValue } from '@/constants/menu'
import { useMenuStore } from '@/stores/menu'
import type { MenuDTO, MenuVO } from '@/types/menu'

const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()

/* -------- 编辑/新增模式 -------- */
const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

/* -------- 表单 -------- */
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<MenuDTO>({
  parentId: 0,
  name: '',
  path: '',
  component: '',
  icon: '',
  menuType: MENU_TYPE.ADMIN,
  sort: 0,
  visible: MENU_VISIBLE.VISIBLE,
  permission: '',
})

const rules: FormRules<MenuDTO> = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
}

/* -------- 父菜单选项 -------- */
const parentTreeOptions = ref<MenuVO[]>([])

/** 加载父菜单候选（同类型；编辑时排除自己及后代） */
async function loadParentOptions() {
  const res = await getMenuTreeAPI(form.menuType)
  let tree = res.data ?? []

  if (isEdit.value && editId.value != null) {
    tree = filterSelfAndDescendants(tree, editId.value)
  }

  parentTreeOptions.value = tree
}

function filterSelfAndDescendants(tree: MenuVO[], excludeId: number): MenuVO[] {
  return tree
    .filter((n) => n.id !== excludeId)
    .map((n) => ({
      ...n,
      children: n.children ? filterSelfAndDescendants(n.children, excludeId) : null,
    }))
}

/* -------- 切换类型：重置父菜单并重新加载 -------- */
function handleMenuTypeChange() {
  form.parentId = 0
  void loadParentOptions()
}

/* -------- 加载详情（编辑模式） -------- */
async function loadDetail() {
  if (!editId.value) return

  const res = await getMenuTreeAPI()
  const found = findById(res.data ?? [], editId.value)

  if (!found) {
    ElMessage.error('菜单不存在')
    handleBack()
    return
  }

  form.parentId = found.parentId ?? 0
  form.name = found.name
  form.path = found.path ?? ''
  form.component = found.component ?? ''
  form.icon = found.icon ?? ''
  form.menuType = found.menuType
  form.sort = found.sort
  form.visible = found.visible
  form.permission = found.permission ?? ''
}

function findById(tree: MenuVO[], id: number): MenuVO | null {
  for (const node of tree) {
    if (node.id === id) return node
    if (node.children) {
      const found = findById(node.children, id)
      if (found) return found
    }
  }
  return null
}

/* -------- 提交 -------- */
async function handleSubmit() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateMenuAPI(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createMenuAPI({ ...form })
      ElMessage.success('新增成功')
    }
    // 同步侧边栏
    await menuStore.loadAll()
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push({ name: 'AdminMenuManage' })
}

/* -------- 初始化 -------- */
onMounted(async () => {
  if (isEdit.value) {
    await loadDetail()
  } else {
    // 新增：从 query 读默认父菜单和类型
    const { parentId, menuType } = route.query
    if (parentId) form.parentId = Number(parentId)
    if (menuType) form.menuType = Number(menuType) as MenuTypeValue
  }
  await loadParentOptions()
})
</script>
