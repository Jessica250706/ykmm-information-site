<template>
  <div class="notice-category-manage flex h-full flex-col">
    <div class="mb-3 flex shrink-0 justify-between">
      <el-button type="primary" @click="handleCreate">新增分类</el-button>
      <el-button @click="router.push({ name: AdminRouteName.NOTICE_MANAGE })">
        返回公告管理
      </el-button>
    </div>

    <el-card class="flex-1" shadow="never">
      <el-table v-loading="loading" :data="categories" stripe>
        <el-table-column align="center" label="ID" prop="id" width="80" />
        <el-table-column label="名称" min-width="120" prop="name" />
        <el-table-column label="描述" min-width="200" prop="description" show-overflow-tooltip />
        <el-table-column align="center" label="颜色" prop="color" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.color" :type="row.color" effect="plain" size="small">
              {{ row.color }}
            </el-tag>
            <span v-else class="text-slate-300">-</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="排序" prop="sort" width="80" />
        <el-table-column align="center" label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分类编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingCategory ? '编辑分类' : '新增分类'"
      width="480px"
    >
      <el-form ref="catFormRef" :model="catForm" :rules="catRules" label-width="80px">
        <el-form-item label="ID" prop="id">
          <el-input-number v-model="catForm.id" :disabled="!!editingCategory" :max="255" :min="1" />
          <div class="mt-1 text-xs text-slate-400">
            字典表主键，需手动指定（1~255），编辑时不可改
          </div>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="catForm.name" maxlength="32" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="catForm.description" :rows="2" maxlength="255" type="textarea" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-select v-model="catForm.color" placeholder="选择标签颜色" clearable>
            <el-option label="primary" value="primary" />
            <el-option label="success" value="success" />
            <el-option label="warning" value="warning" />
            <el-option label="danger" value="danger" />
            <el-option label="info" value="info" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="catForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="saving" type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  adminListNoticeCategoriesAPI,
  createNoticeCategoryAPI,
  deleteNoticeCategoryAPI,
  updateNoticeCategoryAPI,
} from '@/api/notice'
import { AdminRouteName } from '@/constants'
import type { NoticeCategoryVO } from '@/types/notice'

const router = useRouter()
const loading = ref(false)
const categories = ref<NoticeCategoryVO[]>([])

const dialogVisible = ref(false)
const saving = ref(false)
const editingCategory = ref<NoticeCategoryVO | null>(null)
const catFormRef = ref<FormInstance>()

const catForm = reactive<NoticeCategoryVO>({
  id: undefined as unknown as number,
  name: '',
  description: '',
  color: '',
  sort: 0,
})

const catRules: FormRules = {
  id: [{ required: true, message: '请输入分类ID', trigger: 'blur' }],
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

async function load() {
  loading.value = true
  try {
    const res = await adminListNoticeCategoriesAPI()
    categories.value = res.data ?? []
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  editingCategory.value = null
  Object.assign(catForm, { id: undefined, name: '', description: '', color: '', sort: 0 })
  dialogVisible.value = true
}

function handleEdit(row: NoticeCategoryVO) {
  editingCategory.value = row
  Object.assign(catForm, row)
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await catFormRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingCategory.value && editingCategory.value.id) {
      await updateNoticeCategoryAPI(editingCategory.value.id, catForm)
      ElMessage.success('更新成功')
    } else {
      await createNoticeCategoryAPI(catForm)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: NoticeCategoryVO) {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？分类下有公告时无法删除。`, '提示', {
      type: 'warning',
    })
  } catch {
    return
  }
  if (!row.id) {
    ElMessage.error('选中id不存在')
    return
  }
  await deleteNoticeCategoryAPI(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>
