<template>
  <div class="sticker-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑表情包' : '新增表情包' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-3xl" label-width="100px">
        <!-- -------- 分组 -------- -->
        <el-form-item label="所属分组" prop="groupId">
          <el-select v-model="form.groupId" placeholder="请选择分组" style="width: 100%" filterable>
            <el-option v-for="g in groupOptions" :key="g.id" :label="g.name" :value="g.id!" />
          </el-select>
        </el-form-item>

        <!-- -------- 类型 -------- -->
        <el-form-item label="类型" prop="stickerType">
          <el-radio-group v-model="form.stickerType" @change="handleStickerTypeChange">
            <el-radio v-for="opt in STICKER_TYPE_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- -------- 标签 -------- -->
        <el-form-item label="标签" prop="label">
          <el-input
            v-model="form.label"
            maxlength="64"
            placeholder="如：国王布丁表情包"
            show-word-limit
          />
        </el-form-item>

        <!-- -------- 自定义图片 -------- -->
        <el-form-item v-if="form.stickerType === STICKER_TYPE.IMAGE" label="图片" prop="imageUrl">
          <ImageUpload v-model="form.imageUrl" :max-size="5" tip="建议正方形，大小 ≤ 5MB" />
        </el-form-item>

        <!-- -------- emoji -------- -->
        <el-form-item
          v-else-if="form.stickerType === STICKER_TYPE.EMOJI"
          label="Emoji"
          prop="emoji"
        >
          <el-input v-model="form.emoji" maxlength="32" placeholder="如：🍮" style="width: 160px" />
          <span class="ml-3 text-3xl align-middle">{{ form.emoji || '😀' }}</span>
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
import { createStickerAPI, getStickerDetailAPI, updateStickerAPI } from '@/api/sticker'
import { listStickerGroupOptionsAPI } from '@/api/stickerGroup'
import {
  AdminRouteName,
  STICKER_TYPE,
  STICKER_TYPE_OPTIONS,
  type StickerTypeValue,
} from '@/constants'
import type { StickerDTO, StickerGroupVO } from '@/types/sticker'

const route = useRoute()
const router = useRouter()

/* -------- 模式 -------- */
const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

/* -------- 分组下拉 -------- */
const groupOptions = ref<StickerGroupVO[]>([])

async function loadGroups() {
  const res = await listStickerGroupOptionsAPI()
  groupOptions.value = res.data ?? []
}

/* -------- 表单 -------- */
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<StickerDTO>({
  groupId: undefined,
  label: '',
  imageUrl: '',
  emoji: '',
  stickerType: STICKER_TYPE.IMAGE,
})

const rules: FormRules<StickerDTO> = {
  groupId: [{ required: true, message: '请选择所属分组', trigger: 'change' }],
  stickerType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  label: [{ required: true, message: '请输入标签', trigger: 'blur' }],
  imageUrl: [
    {
      validator: (_rule, value, callback) => {
        if (form.stickerType === STICKER_TYPE.IMAGE && !value) {
          callback(new Error('请上传图片'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
  emoji: [
    {
      validator: (_rule, value, callback) => {
        if (form.stickerType === STICKER_TYPE.EMOJI && !value) {
          callback(new Error('请输入 emoji'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

/* -------- 切换类型：清掉对侧字段 -------- */
function handleStickerTypeChange(val: StickerTypeValue) {
  if (val === STICKER_TYPE.IMAGE) {
    form.emoji = ''
  } else if (val === STICKER_TYPE.EMOJI) {
    form.imageUrl = ''
  }
}

/* -------- 加载详情 -------- */
async function loadDetail() {
  if (!editId.value) return
  const res = await getStickerDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('表情包不存在')
    handleBack()
    return
  }

  form.stickerType = (data.stickerType as StickerTypeValue) ?? STICKER_TYPE.IMAGE
  form.groupId = data.groupId
  form.label = data.label ?? ''
  form.imageUrl = data.imageUrl ?? ''
  form.emoji = data.emoji ?? ''
}

/* -------- 提交 -------- */
async function handleSubmit() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload: StickerDTO = {
    ...form,
    imageUrl: form.stickerType === STICKER_TYPE.IMAGE ? form.imageUrl : '',
    emoji: form.stickerType === STICKER_TYPE.EMOJI ? form.emoji : '',
  }

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateStickerAPI(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createStickerAPI(payload)
      ElMessage.success('新增成功')
    }
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push({ name: AdminRouteName.STICKER_MANAGE })
}

/* -------- 初始化 -------- */
onMounted(async () => {
  await loadGroups()
  if (isEdit.value) {
    await loadDetail()
  }
})
</script>
