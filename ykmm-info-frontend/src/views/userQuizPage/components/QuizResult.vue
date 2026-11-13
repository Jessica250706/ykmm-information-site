<template>
  <div class="w-full max-w-2xl">
    <div :class="['rounded-2xl bg-white p-8 shadow-sm']">
      <div class="text-center">
        <div class="mb-3 text-6xl">{{ result.passed ? '🎉' : '💔' }}</div>
        <h1 class="mb-2 text-2xl font-semibold text-slate-800">
          {{ result.passed ? '~恭喜，答题通过！~' : '很遗憾，未通过答题' }}
        </h1>
        <p class="mb-6 text-sm text-slate-500">
          {{ result.passed ? '你已获得完整的用户端权限' : '' }}
        </p>
      </div>

      <!-- 分数 -->
      <div class="mb-6 flex items-center justify-center gap-4 rounded-xl bg-slate-50 p-5 sm:gap-6">
        <div class="text-center">
          <div
            :class="[
              'text-3xl font-semibold',
              result.passed ? 'text-(--el-color-primary)' : 'text-rose-500',
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
          <div class="text-3xl font-semibold text-(--el-color-primary)">
            {{ result.passCount }}
          </div>
          <div class="mt-1 text-xs text-slate-500">通过标准</div>
        </div>
      </div>

      <!-- 按钮 -->
      <div class="flex flex-col items-center gap-3">
        <el-button
          v-if="result.passed"
          class="h-11! w-full"
          size="large"
          type="primary"
          @click="emit('go-home')"
        >
          进入用户端 →
        </el-button>
        <template v-else>
          <el-button class="h-11! w-full" size="large" type="primary" @click="emit('retry')">
            再试一次
          </el-button>
          <el-button size="small" text @click="emit('go-home')">暂时跳过，前往用户端 →</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { QuizAttemptVO, QuizSubmitResultVO } from '@/types/quiz'

defineProps<{
  result: QuizSubmitResultVO
  attempt: QuizAttemptVO | null
}>()

const emit = defineEmits<{
  retry: []
  'go-home': []
}>()
</script>
