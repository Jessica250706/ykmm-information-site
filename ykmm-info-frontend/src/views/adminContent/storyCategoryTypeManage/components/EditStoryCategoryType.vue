<template>
  <div class="story-category-type-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">编辑剧情分类类型</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-2xl" label-width="100px">
        <el-form-item label="ID">
          <el-input :model-value="editId ?? ''" style="width: 200px" disabled />
        </el-form-item>

        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" style="width: 260px" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" :rows="3" placeholder="请输入描述" type="textarea" />
        </el-form-item>

        <el-form-item label="标签颜色" prop="color">
          <el-select v-model="form.color" placeholder="请选择" style="width: 200px" clearable>
            <template #label="{ label, value }">
              <span
                :style="{
                  color: `var(--color-${value})`,
                }"
              >
                {{ label }}
              </span>
            </template>
            <el-option
              v-for="opt in PALETTE_COLOR_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            >
              <el-tag
                :style="{
                  borderColor: `var(--color-${opt.value})`,
                  color: `var(--color-${opt.value})`,
                }"
                :type="opt.value as any"
                effect="plain"
              >
                {{ opt.label }}
              </el-tag>
            </el-option>
          </el-select>
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
import { getStoryCategoryTypeDetailAPI, updateStoryCategoryTypeAPI } from '@/api/storyCategoryType'
import { PALETTE_COLOR_OPTIONS } from '@/constants/index'
import type { StoryCategoryTypeDTO } from '@/types/storyCategoryType'

const route = useRoute()
const router = useRouter()

const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<StoryCategoryTypeDTO>({
  name: '',
  description: '',
  color: '',
  sort: 0,
})

const rules: FormRules<StoryCategoryTypeDTO> = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
}

async function loadDetail() {
  if (!editId.value) return
  const res = await getStoryCategoryTypeDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('分类类型不存在')
    handleBack()
    return
  }
  form.name = data.name ?? ''
  form.description = data.description ?? ''
  form.color = data.color ?? ''
  form.sort = data.sort ?? 0
}

async function handleSubmit() {
  if (!formRef.value || !editId.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await updateStoryCategoryTypeAPI(editId.value, { ...form })
    ElMessage.success('更新成功')
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push({ name: 'AdminStoryCategoryTypeManage' })
}

onMounted(loadDetail)
</script>
