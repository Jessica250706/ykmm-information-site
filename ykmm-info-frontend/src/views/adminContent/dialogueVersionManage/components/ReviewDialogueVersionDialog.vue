<template>
  <el-dialog
    v-model="visible"
    :close-on-click-modal="false"
    title="审核对话版本"
    width="480px"
    @closed="handleClosed"
  >
    <div v-if="version" class="space-y-3">
      <!-- 来源 -->
      <div class="text-sm text-slate-500">
        来源：
        <span class="font-medium text-slate-800">{{ sourceDesc }}</span>
      </div>

      <!-- 版本规格 -->
      <div class="text-sm text-slate-500">
        规格：
        <span class="font-medium text-slate-800">{{ specDesc }}</span>
      </div>

      <!-- 版本 ID（方便对照） -->
      <div class="text-xs text-slate-400">版本 ID：#{{ version.id }}</div>

      <el-form label-width="84px">
        <el-form-item label="审核结果" required>
          <el-radio-group v-model="form.status">
            <el-radio v-for="opt in REVIEW_STATUS_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio>
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
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { reviewDialogueVersionAPI } from '@/api/dialogueVersion'
import { REVIEW_STATUS, REVIEW_STATUS_OPTIONS, SOURCE_TYPE, STATUS } from '@/constants'
import type { DialogueVersionVO } from '@/types/dialogueVersion'

const visible = ref(false)
const submitting = ref(false)
const version = ref<DialogueVersionVO | null>(null)

const form = reactive<{
  status: typeof REVIEW_STATUS.PUBLISHED | typeof REVIEW_STATUS.REJECTED
  reviewRemark: string
}>({
  status: REVIEW_STATUS.PUBLISHED,
  reviewRemark: '',
})

/** 来源描述：剧情《xxx》 / 卡面「xxx」第 N 话 */
const sourceDesc = computed(() => {
  const v = version.value
  if (!v) return ''
  const src = v.source
  if (!src) {
    return v.sourceTypeLabel ? `${v.sourceTypeLabel} #${v.sourceId}` : `#${v.sourceId}`
  }
  if (v.sourceType === SOURCE_TYPE.STORY) {
    return src.title ? `剧情《${src.title}》` : `剧情 #${src.sourceId}`
  }
  // 卡面类
  const cardPart = src.title || `卡面 #${src.cardId}`
  const seriesPart = src.subtitle ? `[${src.subtitle}]` : ''
  const epPart =
    src.episodeNo != null
      ? `第${src.episodeNo}话${src.episodeTitle ? ` ${src.episodeTitle}` : ''}`
      : ''
  return `${cardPart}${seriesPart}${epPart ? ' · ' + epPart : ''}`
})

/** 规格描述：中文 · 文字 · 全部 */
const specDesc = computed(() => {
  const v = version.value
  if (!v) return ''
  return v.label || [v.languageLabel, v.formatLabel, v.scopeLabel].filter(Boolean).join(' · ')
})

const emit = defineEmits<{
  success: []
}>()

function open(target: DialogueVersionVO) {
  version.value = target
  form.status = REVIEW_STATUS.PUBLISHED
  form.reviewRemark = ''
  visible.value = true
}

async function handleSubmit() {
  if (!version.value?.id) return
  if (form.status === STATUS.REJECTED && !form.reviewRemark.trim()) {
    ElMessage.warning('拒绝时请填写审核备注')
    return
  }

  submitting.value = true
  try {
    await reviewDialogueVersionAPI(version.value.id, {
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
  version.value = null
}

defineExpose({ open })
</script>
