<template>
  <div class="w-full max-w-2xl">
    <div
      :class="[
        'rounded-2xl border bg-white p-8 shadow-sm',
        result.passed ? 'border-emerald-200' : 'border-rose-200',
      ]"
    >
      <div class="text-center">
        <div class="mb-3 text-6xl">{{ result.passed ? '🎉' : '💔' }}</div>
        <h1 class="mb-2 text-2xl font-semibold text-slate-800">
          {{ result.passed ? '恭喜，答题通过！' : '很遗憾，未通过答题' }}
        </h1>
        <p class="mb-6 text-sm text-slate-500">
          {{ result.passed ? '你已获得完整的用户端权限' : '再仔细看看错题，可以再试一次' }}
        </p>
      </div>

      <!-- 分数 -->
      <div class="mb-6 flex items-center justify-center gap-4 rounded-xl bg-slate-50 p-5 sm:gap-6">
        <div class="text-center">
          <div
            :class="[
              'text-3xl font-semibold',
              result.passed ? 'text-[var(--el-color-primary)]' : 'text-rose-500',
            ]"
          >
            {{ result.correctCount }}
          </div>
          <div class="mt-1 text-xs text-slate-500">答对题数</div>
        </div>
        <div class="text-2xl text-slate-300">/</div>
        <div class="text-center">
          <div class="text-3xl font-semibold text-slate-700">{{ result.totalCount }}</div>
          <div class="mt-1 text-xs text-slate-500">总题数</div>
        </div>
        <div class="text-2xl text-slate-300">|</div>
        <div class="text-center">
          <div class="text-3xl font-semibold text-[var(--el-color-primary)]">
            {{ result.passCount }}
          </div>
          <div class="mt-1 text-xs text-slate-500">通过标准</div>
        </div>
      </div>

      <!-- 错题 -->
      <div v-if="wrongQuestions.length > 0" class="mb-6">
        <div class="mb-3 border-b border-slate-200 pb-2 text-sm font-medium text-rose-500">
          ❌ 错题回顾（{{ wrongQuestions.length }} 题）
        </div>
        <div class="max-h-80 space-y-3 overflow-y-auto pr-1">
          <div
            v-for="(q, idx) in wrongQuestions"
            :key="q.questionId ?? idx"
            class="rounded-lg border border-rose-100 bg-rose-50/50 p-4"
          >
            <div class="mb-2 text-sm font-medium text-slate-800">{{ q.content }}</div>
            <div class="space-y-1 text-xs">
              <div class="flex gap-2">
                <span class="shrink-0 text-rose-500">你的答案</span>
                <span class="text-slate-600">{{ formatAnswer(q, q.userAnswer) }}</span>
              </div>
              <div class="flex gap-2">
                <span class="shrink-0 text-emerald-600">正确答案</span>
                <span class="text-slate-600">{{ formatAnswer(q, q.correctAnswer) }}</span>
              </div>
              <div v-if="q.explanation" class="flex gap-2">
                <span class="shrink-0 text-slate-400">解析</span>
                <span class="text-slate-600">{{ q.explanation }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 按钮 -->
      <div class="flex flex-col items-center gap-3">
        <el-button
          v-if="result.passed"
          class="!h-11 w-full"
          size="large"
          type="primary"
          @click="emit('go-home')"
        >
          进入用户端 →
        </el-button>
        <template v-else>
          <el-button class="!h-11 w-full" size="large" type="primary" @click="emit('retry')">
            再试一次
          </el-button>
          <el-button size="small" text @click="emit('go-home')">暂时跳过，前往用户端 →</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { QuizAttemptVO, QuizSubmitResultVO } from '@/types/quiz'

const props = defineProps<{
  result: QuizSubmitResultVO
  attempt: QuizAttemptVO | null
}>()

const emit = defineEmits<{
  retry: []
  'go-home': []
}>()

const wrongQuestions = computed(() => (props.result.questions ?? []).filter((q) => !q.isCorrect))

function formatAnswer(
  _q: { userAnswer?: number[]; correctAnswer?: number[] },
  ids?: number[],
): string {
  if (!ids || ids.length === 0) return '未作答'
  return ids.map((id) => `#${id}`).join('、')
}
</script>
