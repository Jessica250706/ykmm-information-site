<template>
  <div class="card-category-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑卡面类别' : '新增卡面类别' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-xl" label-width="100px">
        <el-form-item label="类别名称" prop="name">
          <el-input
            v-model="form.name"
            maxlength="64"
            placeholder="如：生日、纪念日"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :max="9999" :min="0" :step="1" />
          <span class="ml-2 text-xs text-slate-400">数值越小越靠前，留空视为 0</span>
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
  createCardCategoryAPI,
  getCardCategoryDetailAPI,
  updateCardCategoryAPI,
} from '@/api/cardCategory'
import { AdminRouteName } from '@/constants'
import type { CardCategoryDTO } from '@/types/cardCategory'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<CardCategoryDTO>({
  name: '',
  sort: 0,
})

const rules: FormRules<CardCategoryDTO> = {
  name: [{ required: true, message: '请输入类别名称', trigger: 'blur' }],
}

/* -------- 加载详情 -------- */
async function loadDetail() {
  if (!editId.value) return
  const res = await getCardCategoryDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('卡面类别不存在')
    handleBack()
    return
  }
  form.name = data.name ?? ''
  form.sort = data.sort ?? 0
}

/* -------- 提交 -------- */
async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload: CardCategoryDTO = {
    name: form.name?.trim(),
    sort: form.sort ?? 0,
  }

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateCardCategoryAPI(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createCardCategoryAPI(payload)
      ElMessage.success('新增成功')
    }
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect) {
    router.push(redirect)
    return
  }
  router.push({ name: AdminRouteName.CARD_CATEGORY_MANAGE })
}

/* -------- 初始化 -------- */
onMounted(() => {
  if (isEdit.value) {
    void loadDetail()
  }
})
</script>
