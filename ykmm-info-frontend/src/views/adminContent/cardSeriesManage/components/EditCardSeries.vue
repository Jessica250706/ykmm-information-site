<template>
  <div class="card-series-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑卡面系列' : '新增卡面系列' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-2xl" label-width="100px">
        <el-form-item label="系列名" prop="name">
          <el-input v-model="form.name" placeholder="请输入系列名" />
        </el-form-item>

        <!-- <el-form-item label="封面图" prop="coverImage">
          <ImageUpload
            v-model="form.coverImage"
            :max-size="5"
            tip="建议方形，尺寸 400×400，大小 ≤ 5MB"
          />
        </el-form-item> -->

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            :rows="5"
            placeholder="请输入系列描述"
            type="textarea"
          />
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
import { createCardSeriesAPI, getCardSeriesDetailAPI, updateCardSeriesAPI } from '@/api/cardSeries'
import { AdminRouteName } from '@/constants'
import type { CardSeriesDTO } from '@/types/cardSeries'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<CardSeriesDTO>({
  name: '',
  description: '',
  coverImage: '',
})

const rules: FormRules<CardSeriesDTO> = {
  name: [{ required: true, message: '请输入系列名', trigger: 'blur' }],
}

async function loadDetail() {
  if (!editId.value) return
  const res = await getCardSeriesDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('系列不存在')
    handleBack()
    return
  }
  form.name = data.name ?? ''
  form.description = data.description ?? ''
  form.coverImage = data.coverImage ?? ''
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateCardSeriesAPI(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createCardSeriesAPI({ ...form })
      ElMessage.success('新增成功')
    }
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push({ name: AdminRouteName.CARD_SERIES_MANAGE })
}

onMounted(async () => {
  if (isEdit.value) {
    await loadDetail()
  }
})
</script>
