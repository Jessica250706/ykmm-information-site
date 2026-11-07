<template>
  <div class="quiz-page">
    <header class="quiz-header">
      <div class="quiz-logo">
        <el-avatar :size="28" :style="{ backgroundColor: 'var(--el-color-primary)' }">U</el-avatar>
        <span>用户端 · 答题</span>
      </div>
      <el-button text @click="handleLogout">退出登录</el-button>
    </header>

    <main class="quiz-main">
      <div
        v-if="loading"
        class="flex min-h-full flex-col items-center justify-center gap-3 text-slate-500"
      >
        <el-icon :size="40" class="is-loading"><Loading /></el-icon>
        <p>正在准备答题…</p>
      </div>

      <div
        v-else-if="autoRedirecting"
        class="flex min-h-full items-center justify-center text-slate-500"
      >
        正在跳转…
      </div>

      <div v-else-if="!attempt && !result" class="flex min-h-full items-center justify-center">
        <QuizStart :status="status" @go-home="handleGoHome" @start="handleStart" />
      </div>

      <QuizQuestions
        v-else-if="attempt && !result"
        :answers="answers"
        :attempt="attempt"
        :submitting="submitting"
        @go-home="handleGoHome"
        @submit="handleSubmit"
        @update:answer="handleAnswer"
      />

      <div v-else-if="result" class="flex min-h-full items-center justify-center">
        <QuizResult
          :attempt="attempt"
          :result="result"
          @go-home="handleGoHome"
          @retry="handleRetry"
        />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getQuizStatusAPI, startQuizAPI, submitQuizAPI } from '@/api/quiz'
import { CommonRouteName, UserRouteName } from '@/constants'
import { useUserStore } from '@/stores'
import type { QuizAnswerDTO, QuizAttemptVO, QuizStatusVO, QuizSubmitResultVO } from '@/types/quiz'
import QuizQuestions from './components/QuizQuestions.vue'
import QuizResult from './components/QuizResult.vue'
import QuizStart from './components/QuizStart.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const autoRedirecting = ref(false)
const submitting = ref(false)

const status = ref<QuizStatusVO | null>(null)
const attempt = ref<QuizAttemptVO | null>(null)
const result = ref<QuizSubmitResultVO | null>(null)

/** 用户答案：questionId -> 选项ID列表 */
const answers = ref<Record<number, number[]>>({})

onMounted(async () => {
  try {
    const res = await getQuizStatusAPI()
    const s = res.data
    if (!s) {
      ElMessage.error('获取答题状态失败')
      return
    }
    status.value = s

    if (!s.enabled || s.quizPassed) {
      await redirectToHome()
      return
    }
    if (s.hasInProgress) {
      await handleStart(true)
      return
    }
  } catch (e) {
    console.error('[quiz] 加载状态失败', e)
    ElMessage.error('加载答题状态失败')
  } finally {
    loading.value = false
  }
})

async function handleStart(silent = false) {
  try {
    const res = await startQuizAPI()
    const a = res.data
    if (!a || !a.questions || a.questions.length === 0) {
      ElMessage.error('获取题目失败')
      return
    }
    attempt.value = a
    answers.value = {}
    result.value = null
    if (!silent) ElMessage.success('开始答题')
  } catch (e) {
    console.error('[quiz] 开始答题失败', e)
  }
}

function handleAnswer(questionId: number, optionIds: number[]) {
  answers.value = { ...answers.value, [questionId]: optionIds }
}

async function handleSubmit() {
  if (!attempt.value?.attemptId) return

  const questions = attempt.value.questions ?? []
  const answeredCount = Object.values(answers.value).filter((ids) => ids.length > 0).length
  const unanswered = questions.length - answeredCount

  if (unanswered > 0) {
    try {
      await ElMessageBox.confirm(
        `还有 ${unanswered} 道题未作答，未答的题会判定为错误，确定提交吗？`,
        '提交确认',
        { type: 'warning', confirmButtonText: '继续提交', cancelButtonText: '返回检查' },
      )
    } catch {
      return
    }
  }

  submitting.value = true
  try {
    const answersPayload: QuizAnswerDTO[] = questions
      .filter((q): q is { questionId: number } => q.questionId != null)
      .map((q) => ({
        questionId: q.questionId,
        optionIds: answers.value[q.questionId] ?? [],
      }))

    const res = await submitQuizAPI({
      attemptId: attempt.value.attemptId,
      answers: answersPayload,
    })
    result.value = res.data ?? null

    if (res.data?.passed && userStore.userInfo) {
      userStore.setUserInfo({ ...userStore.userInfo, quizPassed: 1 })
    }
  } catch (e) {
    console.error('[quiz] 提交失败', e)
  } finally {
    submitting.value = false
  }
}

async function handleRetry() {
  result.value = null
  attempt.value = null
  answers.value = {}

  try {
    const s = await getQuizStatusAPI()
    status.value = s.data ?? null
    if (status.value && !status.value.canAttempt) {
      ElMessage.warning('答题次数已用完')
      return
    }
  } catch (e) {
    console.error('[quiz] 刷新状态失败', e)
  }

  await handleStart()
}

async function redirectToHome() {
  autoRedirecting.value = true
  await router.replace({ name: UserRouteName.CARD_LIST })
}

async function handleGoHome() {
  await router.replace({ name: UserRouteName.CARD_LIST })
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  userStore.logout()
  await router.replace({ name: CommonRouteName.LOGIN })
}
</script>

<style lang="scss" scoped>
/* ★ 核心改动：高度固定 + overflow hidden，让 page 本身不滚动 */
.quiz-page {
  height: 100dvh; /* 原来是 min-height，改成 height */
  display: flex;
  flex-direction: column;
  background: #f8fafc;
  overflow: hidden; /* 防止 page 被撑出滚动条 */
}

/* ★ header 不参与压缩，永远占据顶部固定高度 */
.quiz-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
}

.quiz-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1e293b;
}

/* ★ main 是唯一的滚动容器 */
.quiz-main {
  flex: 1;
  min-height: 0; /* 关键：允许 flex 项收缩，否则 overflow 不生效 */
  overflow-y: auto;
  overflow-x: hidden;
  padding: 24px;
}

.quiz-main > :deep(*) {
  max-width: 1200px;
  width: 100%;
  margin-left: auto;
  margin-right: auto;
}
</style>
