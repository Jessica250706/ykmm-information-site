<template>
  <div class="w-full max-w-lg">
    <div class="rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
      <div class="text-center">
        <div class="mb-3 text-5xl">🎯</div>
        <h1 class="mb-2 text-2xl font-semibold text-slate-800">欢迎，来答个题吧</h1>
        <p class="mb-6 text-sm leading-relaxed text-slate-500">
          为了保护社区环境，新用户需要完成答题才能使用完整功能。
        </p>
      </div>

      <div class="mb-6 space-y-2 rounded-xl bg-slate-50 p-4 text-sm text-slate-600">
        <div class="flex items-start gap-2">
          <span>📝</span>
          <span>根据后台配置随机抽取题目</span>
        </div>
        <div class="flex items-start gap-2">
          <span>🎲</span>
          <span>题目选项顺序随机，每次都不一样</span>
        </div>
        <div class="flex items-start gap-2">
          <span>✅</span>
          <span>答对足够数量即可通过，获得完整权限</span>
        </div>
        <div v-if="status" class="flex items-start gap-2">
          <span>🔄</span>
          <span>
            剩余答题机会：
            <strong v-if="status.maxAttempts === 0" class="text-[var(--el-color-primary)]">
              不限次数
            </strong>
            <strong v-else class="text-[var(--el-color-primary)]">
              {{ Math.max(0, status.maxAttempts - status.attemptsUsed) }} /
              {{ status.maxAttempts }}
            </strong>
          </span>
        </div>
      </div>

      <div class="flex flex-col items-center gap-3">
        <el-button
          :disabled="status ? !status.canAttempt : false"
          class="!h-11 w-full"
          size="large"
          type="primary"
          @click="emit('start')"
        >
          开始答题
        </el-button>
        <el-button size="small" text @click="emit('go-home')">暂时跳过，前往用户端 →</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { QuizStatusVO } from '@/types/quiz'

defineProps<{
  status: QuizStatusVO | null
}>()

const emit = defineEmits<{
  start: []
  'go-home': []
}>()
</script>
