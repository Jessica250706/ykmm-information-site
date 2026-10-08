<template>
  <div class="story-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑剧情' : '新增剧情' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-2xl" label-width="100px">
        <el-form-item label="分类类型" prop="categoryType">
          <el-select
            v-model="categoryTypeFilter"
            placeholder="先选类型"
            style="width: 200px"
            @change="handleCategoryTypeChange"
          >
            <el-option
              v-for="opt in storyCategoryTypeStore.types"
              :key="opt.id"
              :label="opt.name"
              :value="opt.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="所属分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择所属分类"
            style="width: 100%"
            check-strictly
            clearable
            filterable
          />
        </el-form-item>

        <el-form-item label="话标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入话标题" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            :rows="4"
            placeholder="请输入剧情描述"
            type="textarea"
          />
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
import { createStoryAPI, getStoryDetailAPI, updateStoryAPI } from '@/api/story'
import { listStoryCategoryTreeAPI } from '@/api/storyCategory'
import { AdminRouteName, STORY_CATEGORY_TYPE, type StoryCategoryTypeValue } from '@/constants'
import { useStoryCategoryTypeStore } from '@/stores'
import type { StoryDTO } from '@/types/story'
import type { StoryCategoryVO } from '@/types/storyCategory'

const route = useRoute()
const router = useRouter()
const storyCategoryTypeStore = useStoryCategoryTypeStore()

const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<StoryDTO>({
  categoryId: undefined,
  title: '',
  description: '',
  sort: 0,
})

/** 用于筛选分类树，独立于 form */
const categoryTypeFilter = ref<StoryCategoryTypeValue>(STORY_CATEGORY_TYPE.MAIN)

const rules: FormRules<StoryDTO> = {
  categoryId: [{ required: true, message: '请选择所属分类', trigger: 'change' }],
  title: [{ required: true, message: '请输入话标题', trigger: 'blur' }],
}

const categoryTree = ref<StoryCategoryVO[]>([])

async function loadCategories() {
  const res = await listStoryCategoryTreeAPI({ categoryType: categoryTypeFilter.value })
  categoryTree.value = res.data ?? []
}

function handleCategoryTypeChange() {
  form.categoryId = undefined
  void loadCategories()
}

async function loadDetail() {
  if (!editId.value) return
  const res = await getStoryDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('剧情不存在')
    handleBack()
    return
  }

  form.categoryId = data.categoryId
  form.title = data.title ?? ''
  form.description = data.description ?? ''
  form.sort = data.sort ?? 0

  // 用后端返回的分类类型初始化筛选
  if (data.categoryType != null) {
    categoryTypeFilter.value = data.categoryType as StoryCategoryTypeValue
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateStoryAPI(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createStoryAPI({ ...form })
      ElMessage.success('新增成功')
    }
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  const { from, detailId } = route.query

  if (from === 'storyCategoryDetail' && detailId) {
    router.push({
      name: AdminRouteName.STORY_CATEGORY_DETAIL,
      params: { id: String(detailId) },
    })
    return
  }

  if (from === 'storyCategory') {
    router.push({ name: AdminRouteName.STORY_CATEGORY_MANAGE })
    return
  }

  // 默认回剧情管理列表
  router.push({ name: AdminRouteName.STORY_MANAGE })
}

onMounted(async () => {
  if (isEdit.value) {
    await loadDetail()
  } else {
    // 新增模式：从 query 读初始分类类型和分类 ID
    const { categoryId, categoryType } = route.query
    if (categoryType) {
      categoryTypeFilter.value = Number(categoryType) as StoryCategoryTypeValue
    }
    if (categoryId) {
      form.categoryId = Number(categoryId)
    }
  }
  await loadCategories()
})
</script>
