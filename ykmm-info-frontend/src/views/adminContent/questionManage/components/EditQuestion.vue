<template>
  <div class="question-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑题目' : '新增题目' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-3xl" label-width="100px">
        <el-form-item label="题干" prop="content">
          <el-input
            v-model="form.content"
            :rows="3"
            maxlength="1000"
            placeholder="请输入题目内容"
            type="textarea"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="题型" prop="questionType">
          <el-radio-group v-model="form.questionType" @change="handleTypeChange">
            <el-radio v-for="opt in QUESTION_TYPE_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="题目类型" prop="isFixed">
          <div class="flex items-center">
            <el-radio-group v-model="form.isFixed">
              <el-radio v-for="opt in QUESTION_FIXED_OPTIONS" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </el-radio>
            </el-radio-group>
            <div class="ml-4 text-xs text-slate-400">
              固定题：所有用户必答；随机题：按配置从题库中随机抽取
            </div>
          </div>
        </el-form-item>

        <el-form-item label="选项" prop="options">
          <div class="w-full space-y-2">
            <div v-for="(opt, idx) in form.options" :key="idx" class="flex items-center gap-2">
              <span class="w-8 shrink-0 text-sm text-slate-500">{{ idx + 1 }}.</span>
              <el-input v-model="opt.content" maxlength="500" placeholder="选项内容" />
              <el-checkbox
                v-if="form.questionType === QUESTION_TYPE.MULTI"
                :model-value="opt.isCorrect === 1"
                class="shrink-0"
                @change="(val: boolean) => handleMultiCorrect(idx, val)"
              >
                正确
              </el-checkbox>
              <el-radio
                v-else
                :model-value="opt.isCorrect === 1"
                :value="true"
                class="shrink-0"
                @click.prevent="handleSingleCorrect(idx)"
              >
                正确
              </el-radio>
              <el-button
                :disabled="form.options.length <= 2"
                size="small"
                type="danger"
                link
                @click="handleRemoveOption(idx)"
              >
                删除
              </el-button>
            </div>
            <el-button
              :disabled="form.options.length >= 10"
              class="mt-2"
              size="small"
              type="primary"
              plain
              @click="handleAddOption"
            >
              <el-icon class="mr-1"><Plus /></el-icon>
              添加选项
            </el-button>
            <div class="text-xs text-slate-400">
              {{ form.questionType === QUESTION_TYPE.SINGLE ? '单选题' : '多选题' }}
              至少 2 个选项，
              {{
                form.questionType === QUESTION_TYPE.SINGLE
                  ? '只能有 1 个正确答案'
                  : '可以有多个正确答案'
              }}
            </div>
          </div>
        </el-form-item>

        <el-form-item label="答案解析">
          <el-input
            v-model="form.explanation"
            :rows="2"
            placeholder="可选，答完展示给用户"
            type="textarea"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="opt in QUESTION_STATUS_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :max="9999" :min="0" />
          <span class="ml-2 text-xs text-slate-400">值越小越靠前</span>
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
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { createQuestionAPI, getQuestionDetailAPI, updateQuestionAPI } from '@/api/quiz'
import {
  AdminRouteName,
  QUESTION_FIXED,
  QUESTION_FIXED_OPTIONS,
  QUESTION_STATUS,
  QUESTION_STATUS_OPTIONS,
  QUESTION_TYPE,
  QUESTION_TYPE_OPTIONS,
} from '@/constants'
import type { QuestionDTO, QuestionOptionDTO } from '@/types/quiz'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

const formRef = ref<FormInstance>()
const loading = ref(false)

/* -------- 表单 -------- */
function newEmptyOption(): QuestionOptionDTO {
  return { content: '', isCorrect: 0 }
}

const form = reactive<QuestionDTO>({
  content: '',
  questionType: QUESTION_TYPE.SINGLE,
  isFixed: QUESTION_FIXED.NO,
  explanation: '',
  status: QUESTION_STATUS.ENABLED,
  sort: 0,
  options: [newEmptyOption(), newEmptyOption()],
})

const rules: FormRules<QuestionDTO> = {
  content: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  isFixed: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
}

/* -------- 选项操作 -------- */
function handleAddOption() {
  if (form.options.length >= 10) return
  form.options.push(newEmptyOption())
}

function handleRemoveOption(idx: number) {
  if (form.options.length <= 2) {
    ElMessage.warning('至少保留 2 个选项')
    return
  }
  form.options.splice(idx, 1)
}

/** 单选题：选当前为正确，其他全部清零 */
function handleSingleCorrect(idx: number) {
  form.options.forEach((o, i) => {
    o.isCorrect = i === idx ? 1 : 0
  })
}

/** 多选题：toggle 当前 */
function handleMultiCorrect(idx: number, checked: boolean) {
  const opt = form.options[idx]
  if (opt) opt.isCorrect = checked ? 1 : 0
}

/** 从单选切到多选时，保留已有的正确项；从多选切到单选时，只保留第一个正确项 */
function handleTypeChange() {
  if (form.questionType === QUESTION_TYPE.SINGLE) {
    const firstCorrect = form.options.findIndex((o) => o.isCorrect === 1)
    form.options.forEach((o, i) => {
      o.isCorrect = i === firstCorrect ? 1 : 0
    })
  }
}

/* -------- 提交前校验 -------- */
function validateOptions(): string | null {
  if (form.options.length < 2) return '至少需要 2 个选项'
  if (form.options.some((o) => !o.content.trim())) return '选项内容不能为空'

  const correctCount = form.options.filter((o) => o.isCorrect === 1).length
  if (correctCount === 0) return '必须至少设置一个正确答案'
  if (form.questionType === QUESTION_TYPE.SINGLE && correctCount > 1) {
    return '单选题只能有一个正确答案'
  }
  return null
}

/* -------- 加载详情 -------- */
async function loadDetail() {
  if (!editId.value) return
  const res = await getQuestionDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('题目不存在')
    handleBack()
    return
  }

  form.content = data.content ?? ''
  form.questionType = data.questionType ?? QUESTION_TYPE.SINGLE
  form.isFixed = data.isFixed ?? QUESTION_FIXED.NO
  form.explanation = data.explanation ?? ''
  form.status = data.status ?? QUESTION_STATUS.ENABLED
  form.sort = data.sort ?? 0
  form.options = (data.options ?? []).map((o) => ({
    id: o.id,
    content: o.content ?? '',
    isCorrect: o.isCorrect ?? 0,
    sort: o.sort,
  }))
  if (form.options.length < 2) {
    while (form.options.length < 2) form.options.push(newEmptyOption())
  }
}

/* -------- 提交 -------- */
async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const optionErr = validateOptions()
  if (optionErr) {
    ElMessage.warning(optionErr)
    return
  }

  // 清理选项空值
  const payload: QuestionDTO = {
    ...form,
    options: form.options.map((o, i) => ({
      id: o.id,
      content: o.content.trim(),
      isCorrect: o.isCorrect,
      sort: o.sort ?? i + 1,
    })),
  }

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateQuestionAPI(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createQuestionAPI(payload)
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
  router.push({ name: AdminRouteName.QUESTION_MANAGE })
}

/* -------- 初始化 -------- */
onMounted(async () => {
  if (isEdit.value) await loadDetail()
})
</script>
