<template>
  <div class="story-category-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑剧情分类' : '新增剧情分类' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-2xl" label-width="100px">
        <el-form-item label="分类类型" prop="categoryType">
          <el-radio-group v-model="form.categoryType" @change="handleCategoryTypeChange">
            <el-radio
              v-for="opt in STORY_CATEGORY_TYPE_OPTIONS"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="父分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="parentTreeOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            :value-on-clear="0"
            placeholder="不选则为顶级分类"
            style="width: 100%"
            check-strictly
            clearable
          />
        </el-form-item>

        <el-form-item label="分类名" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名" />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :max="9999" :min="0" :precision="0" />
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
import {
  createStoryCategoryAPI,
  getStoryCategoryDetailAPI,
  listStoryCategoryTreeAPI,
  updateStoryCategoryAPI,
} from '@/api/storyCategory'
import {
  STORY_CATEGORY_TYPE,
  STORY_CATEGORY_TYPE_OPTIONS,
  type StoryCategoryTypeValue,
} from '@/constants/story'
import type { StoryCategoryDTO, StoryCategoryVO } from '@/types/storyCategory'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<StoryCategoryDTO>({
  parentId: 0,
  name: '',
  categoryType: STORY_CATEGORY_TYPE.MAIN,
  sort: 0,
})

const rules: FormRules<StoryCategoryDTO> = {
  name: [{ required: true, message: '请输入分类名', trigger: 'blur' }],
  categoryType: [{ required: true, message: '请选择分类类型', trigger: 'change' }],
}

const parentTreeOptions = ref<StoryCategoryVO[]>([])

async function loadParentOptions() {
  const res = await listStoryCategoryTreeAPI({ categoryType: form.categoryType })
  let tree = res.data ?? []

  if (isEdit.value && editId.value != null) {
    tree = filterSelfAndDescendants(tree, editId.value)
  }

  parentTreeOptions.value = tree
}

function filterSelfAndDescendants(tree: StoryCategoryVO[], excludeId: number): StoryCategoryVO[] {
  return tree
    .filter((n) => n.id !== excludeId)
    .map((n) => ({
      ...n,
      children: n.children ? filterSelfAndDescendants(n.children, excludeId) : undefined,
    }))
}

function handleCategoryTypeChange() {
  form.parentId = 0
  void loadParentOptions()
}

async function loadDetail() {
  if (!editId.value) return
  const res = await getStoryCategoryDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('分类不存在')
    handleBack()
    return
  }

  form.parentId = data.parentId ?? 0
  form.name = data.name ?? ''
  form.categoryType = (data.categoryType as StoryCategoryTypeValue) ?? STORY_CATEGORY_TYPE.MAIN
  form.sort = data.sort ?? 0
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateStoryCategoryAPI(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createStoryCategoryAPI({ ...form })
      ElMessage.success('新增成功')
    }
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  const { from, detailId } = route.query
  if (from === 'detail' && detailId) {
    router.push({
      name: 'AdminStoryCategoryDetail',
      params: { id: String(detailId) },
    })
    return
  }
  router.push({ name: 'AdminStoryCategoryManage' })
}

onMounted(async () => {
  if (isEdit.value) {
    await loadDetail()
  } else {
    const { parentId, categoryType } = route.query
    if (parentId) form.parentId = Number(parentId)
    if (categoryType) form.categoryType = Number(categoryType) as StoryCategoryTypeValue
  }
  await loadParentOptions()
})
</script>
