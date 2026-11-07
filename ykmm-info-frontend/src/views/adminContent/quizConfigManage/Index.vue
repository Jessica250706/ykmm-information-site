<template>
  <div class="quiz-config-manage flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">答题配置</span>
          <el-button @click="router.push({ name: AdminRouteName.QUESTION_MANAGE })">
            返回题目管理
          </el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        v-loading="loading"
        :model="form"
        :rules="rules"
        class="max-w-2xl"
        label-width="160px"
      >
        <el-form-item label="是否开启答题" prop="enabled">
          <el-switch
            v-model="form.enabled"
            :active-value="1"
            :inactive-value="0"
            active-text="开启"
            inactive-text="关闭"
          />
          <div class="mt-1 w-full text-xs text-slate-400">
            关闭后所有用户无需答题即可使用全部功能
          </div>
        </el-form-item>

        <el-form-item label="每次答题总题数" prop="totalCount">
          <el-input-number v-model="form.totalCount" :max="100" :min="1" />
          <span class="ml-2 text-xs text-slate-400">含固定题</span>
        </el-form-item>

        <el-form-item label="通过所需题数" prop="passCount">
          <el-input-number v-model="form.passCount" :max="form.totalCount" :min="1" />
          <span class="ml-2 text-xs text-slate-400">不能大于总题数</span>
        </el-form-item>

        <el-form-item label="最大答题次数" prop="maxAttempts">
          <el-input-number v-model="form.maxAttempts" :max="100" :min="0" />
          <span class="ml-2 text-xs text-slate-400">0 表示不限次数</span>
        </el-form-item>

        <el-form-item label="题库统计">
          <el-descriptions :column="2" class="w-full" border>
            <el-descriptions-item label="固定题数量">
              <el-tag effect="plain" type="danger">{{ stats.fixedCount }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="随机题数量">
              <el-tag effect="plain" type="info">{{ stats.randomCount }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :span="2" label="抽题可行性">
              <span v-if="!canExtract" class="text-rose-500">
                ⚠ 当前题库无法满足配置：总题数（{{ form.totalCount }}） 需要固定题
                {{ stats.fixedCount }} + 随机题 {{ form.totalCount - stats.fixedCount }}，
                但随机题只有 {{ stats.randomCount }} 道
              </span>
              <span v-else class="text-green-600">
                ✓ 可以正常抽题（固定题 {{ stats.fixedCount }} + 随机题
                {{ form.totalCount - stats.fixedCount }}）
              </span>
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <el-form-item label="最后更新时间">
          <span class="text-sm text-slate-500">{{ stats.updatedAt || '-' }}</span>
        </el-form-item>
      </el-form>

      <div class="mt-6 flex justify-center">
        <el-button :loading="submitting" type="primary" @click="handleSubmit">保存配置</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { getQuizConfigAPI, updateQuizConfigAPI } from '@/api/quiz'
import { AdminRouteName } from '@/constants'
import type { QuizConfigDTO } from '@/types/quiz'

const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

const form = reactive<QuizConfigDTO>({
  enabled: 1,
  totalCount: 10,
  passCount: 8,
  maxAttempts: 0,
})

const stats = reactive<{
  fixedCount: number
  randomCount: number
  updatedAt?: string
}>({
  fixedCount: 0,
  randomCount: 0,
  updatedAt: undefined,
})

/** 当前题库是否可以支持当前配置抽题 */
const canExtract = computed(() => {
  if (form.totalCount < stats.fixedCount) return false
  const needRandom = form.totalCount - stats.fixedCount
  return needRandom <= stats.randomCount
})

const rules: FormRules<QuizConfigDTO> = {
  totalCount: [{ required: true, message: '请输入总题数', trigger: 'blur' }],
  passCount: [{ required: true, message: '请输入通过题数', trigger: 'blur' }],
  maxAttempts: [{ required: true, message: '请输入最大答题次数', trigger: 'blur' }],
}

/* -------- 加载 -------- */
async function loadConfig() {
  loading.value = true
  try {
    const res = await getQuizConfigAPI()
    const data = res.data
    if (!data) return
    form.enabled = data.enabled ?? 1
    form.totalCount = data.totalCount ?? 10
    form.passCount = data.passCount ?? 8
    form.maxAttempts = data.maxAttempts ?? 0
    stats.fixedCount = data.fixedCount ?? 0
    stats.randomCount = data.randomCount ?? 0
    stats.updatedAt = data.updatedAt
  } finally {
    loading.value = false
  }
}

/* -------- 提交 -------- */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (form.passCount > form.totalCount) {
    ElMessage.warning('通过题数不能大于总题数')
    return
  }
  if (!canExtract.value) {
    ElMessage.warning('当前题库无法满足配置，请先补充题目')
    return
  }

  submitting.value = true
  try {
    await updateQuizConfigAPI({ ...form })
    ElMessage.success('保存成功')
    await loadConfig()
  } finally {
    submitting.value = false
  }
}

/* -------- 初始化 -------- */
onMounted(loadConfig)
</script>
