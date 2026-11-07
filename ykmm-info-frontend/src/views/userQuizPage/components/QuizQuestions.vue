<template>
  <div class="flex flex-col gap-6 lg:flex-row">
    <!-- 左侧/顶部题号导航 -->
    <aside class="lg:w-64 lg:shrink-0">
      <div class="rounded-xl border border-slate-200 bg-white p-4 lg:sticky lg:top-0">
        <!-- 进度 -->
        <div class="mb-3 flex items-center justify-between text-sm">
          <span class="text-slate-500">答题进度</span>
          <span class="font-medium text-[var(--el-color-primary)]">
            {{ answeredCount }} / {{ totalCount }}
          </span>
        </div>
        <el-progress
          :percentage="progressPercent"
          :show-text="false"
          :stroke-width="6"
          class="mb-4"
          color="var(--el-color-primary)"
        />

        <!-- 题号网格 -->
        <div class="mb-3 grid grid-cols-6 gap-2 lg:grid-cols-5">
          <button
            v-for="(q, idx) in questions"
            :key="q.questionId ?? idx"
            :class="dotClass(idx, q.questionId)"
            type="button"
            @click="scrollToQuestion(idx)"
          >
            {{ idx + 1 }}
          </button>
        </div>

        <!-- 图例 -->
        <div class="mb-4 flex flex-wrap gap-3 text-xs text-slate-500">
          <span class="inline-flex items-center gap-1">
            <span class="h-2.5 w-2.5 rounded-full bg-[var(--el-color-primary)]" />
            已答
          </span>
          <span class="inline-flex items-center gap-1">
            <span class="h-2.5 w-2.5 rounded-full bg-slate-200" />
            未答
          </span>
        </div>

        <el-button :loading="submitting" class="w-full" type="primary" @click="emit('submit')">
          提交答卷
        </el-button>
        <el-button class="mt-2 w-full" size="small" text @click="emit('go-home')">
          暂时跳过，前往用户端 →
        </el-button>
      </div>
    </aside>

    <!-- 题目列表 -->
    <main class="min-w-0 flex-1 space-y-4">
      <div
        v-for="(q, idx) in questions"
        :key="q.questionId ?? idx"
        :ref="(el) => setQuestionRef(idx, el)"
        :class="[
          'rounded-xl border bg-white p-6 transition-all',
          currentScrollIndex === idx
            ? 'border-[var(--el-color-primary)] shadow-sm'
            : 'border-slate-200',
        ]"
        :data-idx="idx"
      >
        <!-- 题号 + 标签 -->
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <span
            class="flex h-7 w-7 items-center justify-center rounded-md bg-[var(--el-color-primary-light-9)] text-sm font-semibold text-[var(--el-color-primary)]"
          >
            {{ idx + 1 }}
          </span>
          <el-tag
            :type="q.questionType === QUESTION_TYPE.MULTI ? 'warning' : 'primary'"
            effect="plain"
            size="small"
          >
            {{ q.questionType === QUESTION_TYPE.MULTI ? '多选' : '单选' }}
          </el-tag>
          <el-tag v-if="q.isFixed === 1" effect="plain" size="small" type="danger">必答</el-tag>
        </div>

        <!-- 题干 -->
        <h3 class="mb-4 whitespace-pre-line text-base font-medium leading-relaxed text-slate-800">
          {{ q.content }}
        </h3>

        <!-- 选项 -->
        <div class="space-y-2">
          <div
            v-for="(opt, optIdx) in q.options ?? []"
            :key="opt.id"
            :class="[
              'flex cursor-pointer items-center gap-3 rounded-lg border p-3 transition',
              isSelected(q.questionId, opt.id)
                ? 'border-[var(--el-color-primary)] bg-[var(--el-color-primary-light-9)]'
                : 'border-slate-200 hover:border-slate-300 hover:bg-slate-50',
            ]"
            @click="handleToggleOption(q, opt.id)"
          >
            <span
              :class="[
                'flex h-7 w-7 shrink-0 items-center justify-center text-xs font-semibold transition',
                q.questionType === QUESTION_TYPE.MULTI ? 'rounded' : 'rounded-full',
                isSelected(q.questionId, opt.id)
                  ? 'bg-[var(--el-color-primary)] text-white'
                  : 'bg-slate-100 text-slate-500',
              ]"
            >
              {{ getOptionLabel(optIdx) }}
            </span>
            <span class="flex-1 text-sm leading-relaxed text-slate-700">
              {{ opt.content }}
            </span>
          </div>
        </div>
      </div>

      <!-- 底部再次给出提交入口（长列表滚到底时方便） -->
      <div class="flex flex-col items-center gap-2 pb-4">
        <el-button :loading="submitting" type="primary" @click="emit('submit')">提交答卷</el-button>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import type { ComponentPublicInstance } from 'vue'
import { QUESTION_TYPE } from '@/constants'
import type { QuizAttemptVO, QuizQuestionVO } from '@/types/quiz'

const props = defineProps<{
  attempt: QuizAttemptVO
  answers: Record<number, number[]>
  submitting: boolean
}>()

const emit = defineEmits<{
  'update:answer': [questionId: number, optionIds: number[]]
  submit: []
  'go-home': []
}>()

const questions = computed<QuizQuestionVO[]>(() => props.attempt.questions ?? [])
const totalCount = computed(() => questions.value.length)
const answeredCount = computed(
  () => Object.values(props.answers).filter((ids) => ids.length > 0).length,
)
const progressPercent = computed(() => {
  if (totalCount.value === 0) return 0
  return Math.round((answeredCount.value / totalCount.value) * 100)
})

/* -------- 选项相关 -------- */
function getOptionLabel(idx: number): string {
  return String.fromCharCode(65 + idx)
}

function isSelected(questionId: number | undefined, optionId: number): boolean {
  if (questionId == null) return false
  return (props.answers[questionId] ?? []).includes(optionId)
}

function handleToggleOption(q: QuizQuestionVO, optionId: number) {
  if (q.questionId == null) return

  const questionId = q.questionId
  const current = props.answers[questionId] ?? []
  const isMulti = q.questionType === QUESTION_TYPE.MULTI

  let next: number[]
  if (isMulti) {
    next = current.includes(optionId)
      ? current.filter((id) => id !== optionId)
      : [...current, optionId]
  } else {
    next = current.length === 1 && current[0] === optionId ? [] : [optionId]
  }

  emit('update:answer', questionId, next)
}

/* -------- 题号样式 -------- */
function dotClass(idx: number, questionId?: number) {
  const answered = questionId != null && (props.answers[questionId]?.length ?? 0) > 0
  const isCurrent = idx === currentScrollIndex.value
  return [
    'flex h-9 items-center justify-center rounded-md text-sm font-medium transition cursor-pointer',
    answered
      ? 'bg-[var(--el-color-primary)] text-white'
      : 'bg-slate-100 text-slate-600 hover:bg-slate-200',
    isCurrent ? 'shadow-[0_0_0_2px_var(--el-color-primary)]' : '',
  ]
}

/* -------- 滚动定位 -------- */
const currentScrollIndex = ref(0)

const questionEls = new Map<number, HTMLElement>()
let observer: IntersectionObserver | null = null

function setQuestionRef(idx: number, el: Element | ComponentPublicInstance | null) {
  const old = questionEls.get(idx)
  if (old) observer?.unobserve(old)

  if (el instanceof HTMLElement) {
    questionEls.set(idx, el)
    observer?.observe(el)
  } else {
    questionEls.delete(idx)
  }
}

function scrollToQuestion(idx: number) {
  const el = questionEls.get(idx)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

onMounted(async () => {
  observer = new IntersectionObserver(
    (entries) => {
      // 找出可见条目里最靠上的那个
      const visible = entries
        .filter((e) => e.isIntersecting)
        .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)

      if (visible.length > 0) {
        const target = visible[0]!.target as HTMLElement
        const idx = Number(target.dataset.idx)
        if (!Number.isNaN(idx)) {
          currentScrollIndex.value = idx
        }
      }
    },
    { rootMargin: '-80px 0px -55% 0px', threshold: 0 },
  )

  // 首次渲染后再观察一次（此时 ref 已就绪）
  await nextTick()
  questionEls.forEach((el) => observer!.observe(el))
})

onBeforeUnmount(() => {
  observer?.disconnect()
  observer = null
})
</script>
