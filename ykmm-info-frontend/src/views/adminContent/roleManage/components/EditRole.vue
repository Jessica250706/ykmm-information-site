<template>
  <div class="role-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑角色' : '新增角色' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-2xl" label-width="100px">
        <el-form-item label="角色名" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名" />
        </el-form-item>

        <el-form-item label="对应人物" prop="personId">
          <el-select
            v-model="form.personId"
            placeholder="可为空，选择对应人物"
            style="width: 100%"
            clearable
            filterable
          >
            <el-option
              v-for="p in personStore.persons"
              :key="p.id"
              :label="p.nameCn || p.nameJp || `#${p.id}`"
              :value="p.id!"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="所属剧情分类" prop="storyCategoryIds">
          <el-tree-select
            v-model="form.storyCategoryIds"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            :render-after-expand="false"
            node-key="id"
            placeholder="可选择多个剧情分类"
            style="width: 100%"
            check-strictly
            clearable
            filterable
            multiple
            @clear="handleCategoryClear"
          />
        </el-form-item>

        <el-form-item label="角色简介" prop="intro">
          <el-input v-model="form.intro" :rows="5" placeholder="请输入角色简介" type="textarea" />
        </el-form-item>
      </el-form>

      <div class="mt-4 flex justify-center">
        <el-button :loading="loading" type="primary" @click="handleSubmit">保存</el-button>
        <el-button @click="handleBack">取消</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { createRoleAPI, getRoleDetailAPI, updateRoleAPI } from '@/api/role'
import { listStoryCategoryTreeAPI } from '@/api/storyCategory'
import { usePersonStore } from '@/stores/personStore'
import type { RoleDTO } from '@/types/role'
import type { StoryCategoryVO } from '@/types/storyCategory'

const route = useRoute()
const router = useRouter()
const personStore = usePersonStore()

/* -------- 模式 -------- */
const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

/* -------- 表单 -------- */
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<RoleDTO>({
  name: '',
  intro: '',
  personId: undefined,
  storyCategoryIds: [],
})

const rules: FormRules<RoleDTO> = {
  name: [{ required: true, message: '请输入角色名', trigger: 'blur' }],
}

/* -------- 剧情分类树 -------- */
const categoryTree = ref<StoryCategoryVO[]>([])

async function loadCategories() {
  const res = await listStoryCategoryTreeAPI({})
  categoryTree.value = res.data ?? []
}

/* -------- 加载详情（编辑模式） -------- */
async function loadDetail() {
  if (!editId.value) return
  const res = await getRoleDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('角色不存在')
    handleBack()
    return
  }

  form.name = data.name ?? ''
  form.intro = data.intro ?? ''
  form.personId = data.personId ?? undefined

  // 后端返回的是 StoryCategoryVO[]，提交需要 ID 数组
  form.storyCategoryIds = (data.storyCategories ?? [])
    .map((c) => (c.id != null ? Number(c.id) : null))
    .filter((id): id is number => id != null)
}

/* -------- 提交 -------- */
async function handleSubmit() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 清理空值
  const payload: RoleDTO = {
    ...form,
    personId: form.personId ?? undefined,
    storyCategoryIds: (form.storyCategoryIds ?? []).filter((id) => id != null),
  }

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateRoleAPI(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createRoleAPI(payload)
      ElMessage.success('新增成功')
    }
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push({ name: 'AdminRoleManage' })
}

function handleCategoryClear() {
  form.storyCategoryIds = []
}

/* -------- 初始化 -------- */
onMounted(async () => {
  // 并行加载下拉数据和分类树
  await Promise.all([personStore.loadAll(), loadCategories()])

  if (isEdit.value) {
    await loadDetail()
  }
})
</script>
