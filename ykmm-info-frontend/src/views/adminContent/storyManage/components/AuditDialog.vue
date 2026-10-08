<template>
  <el-dialog v-model="visible" title="剧情审核" width="480px" append-to-body @closed="handleClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="审核结果" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="STORY_AUDIT_ACTION.APPROVE">
            <span class="text-green-600">通过</span>
          </el-radio>
          <el-radio :value="STORY_AUDIT_ACTION.REJECT">
            <span class="text-red-500">拒绝</span>
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="审核备注" prop="reviewRemark">
        <el-input
          v-model="form.reviewRemark"
          :placeholder="
            form.status === STORY_AUDIT_ACTION.REJECT ? '请说明拒绝原因' : '可填审核备注'
          "
          :rows="3"
          type="textarea"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="loading" type="primary" @click="handleSubmit">提交</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { auditStoryAPI } from '@/api/story'
import { STORY_AUDIT_ACTION } from '@/constants'
import type { StoryAuditDTO, StoryVO } from '@/types/story'

const visible = defineModel<boolean>({ required: true })

const props = defineProps<{
  story: StoryVO | null
}>()

const emit = defineEmits<{
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<StoryAuditDTO>({
  status: STORY_AUDIT_ACTION.APPROVE,
  reviewRemark: '',
})

const rules: FormRules<StoryAuditDTO> = {
  status: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  reviewRemark: [
    {
      validator: (_rule, value, callback) => {
        if (form.status === STORY_AUDIT_ACTION.REJECT && !value?.trim()) {
          callback(new Error('拒绝时必须填写原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function handleSubmit() {
  if (!formRef.value || !props.story?.id) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await auditStoryAPI(props.story.id, { ...form })
    ElMessage.success('审核完成')
    visible.value = false
    emit('success')
  } finally {
    loading.value = false
  }
}

function handleClosed() {
  formRef.value?.resetFields()
  form.status = STORY_AUDIT_ACTION.APPROVE
  form.reviewRemark = ''
}
</script>
