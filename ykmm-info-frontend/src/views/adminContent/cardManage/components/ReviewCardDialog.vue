<template>
  <el-dialog
    v-model="visible"
    :close-on-click-modal="false"
    title="审核卡面"
    width="440px"
    @closed="handleClosed"
  >
    <div v-if="card" class="space-y-4">
      <div class="text-sm text-slate-500">
        卡面：
        <span class="font-medium text-slate-800">
          {{ card.name || `#${card.id}` }}[{{ card.seriesName }}]
        </span>
      </div>

      <el-form label-width="84px">
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="form.status">
            <el-radio :value="STATUS.PUBLISHED">通过</el-radio>
            <el-radio :value="STATUS.REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item :required="form.status === STATUS.REJECTED" label="审核备注">
          <el-input
            v-model="form.reviewRemark"
            :placeholder="form.status === STATUS.REJECTED ? '拒绝时必填，说明原因' : '可选'"
            :rows="3"
            maxlength="255"
            type="textarea"
            show-word-limit
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitting" type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { reviewCardAPI } from '@/api/card'
import { STATUS } from '@/constants'
import type { CardVO } from '@/types/card'

const visible = ref(false)
const submitting = ref(false)
const card = ref<CardVO | null>(null)

const form = reactive<{
  status: typeof STATUS.PUBLISHED | typeof STATUS.REJECTED
  reviewRemark: string
}>({
  status: STATUS.PUBLISHED,
  reviewRemark: '',
})

const emit = defineEmits<{
  success: []
}>()

/** 打开对话框，传入待审核卡面 */
function open(target: CardVO) {
  card.value = target
  form.status = STATUS.PUBLISHED
  form.reviewRemark = ''
  visible.value = true
}

async function handleSubmit() {
  if (!card.value?.id) return
  if (form.status === STATUS.REJECTED && !form.reviewRemark.trim()) {
    ElMessage.warning('拒绝时请填写审核备注')
    return
  }

  submitting.value = true
  try {
    await reviewCardAPI(card.value.id, {
      status: form.status,
      reviewRemark: form.reviewRemark.trim() || undefined,
    })
    ElMessage.success('审核完成')
    visible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

function handleClosed() {
  card.value = null
}

defineExpose({ open })
</script>
