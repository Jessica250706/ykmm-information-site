<template>
  <div class="sticker-group-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑表情包分组' : '新增表情包分组' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-2xl" label-width="100px">
        <el-form-item label="分组名称" prop="name">
          <el-input
            v-model="form.name"
            maxlength="64"
            placeholder="如：国王布丁、奇娜子"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            :rows="3"
            maxlength="255"
            placeholder="分组描述（可选）"
            type="textarea"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :max="9999" :min="0" :precision="0" />
          <span class="ml-3 text-sm text-slate-500">数值越小越靠前</span>
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
  createStickerGroupAPI,
  getStickerGroupDetailAPI,
  updateStickerGroupAPI,
} from '@/api/stickerGroup'
import { AdminRouteName } from '@/constants'
import type { StickerGroupDTO } from '@/types/sticker'

const route = useRoute()
const router = useRouter()

/* -------- 模式 -------- */
const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

/* -------- 表单 -------- */
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<StickerGroupDTO>({
  name: '',
  description: '',
  sort: 0,
})

const rules: FormRules<StickerGroupDTO> = {
  name: [{ required: true, message: '请输入分组名称', trigger: 'blur' }],
}

/* -------- 加载详情 -------- */
async function loadDetail() {
  if (!editId.value) return
  const res = await getStickerGroupDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('分组不存在')
    handleBack()
    return
  }

  form.name = data.name ?? ''
  form.description = data.description ?? ''
  form.sort = data.sort ?? 0
}

/* -------- 提交 -------- */
async function handleSubmit() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateStickerGroupAPI(editId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await createStickerGroupAPI({ ...form })
      ElMessage.success('新增成功')
    }
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push({ name: AdminRouteName.STICKER_GROUP_MANAGE })
}

/* -------- 初始化 -------- */
onMounted(async () => {
  if (isEdit.value) {
    await loadDetail()
  }
})
</script>
