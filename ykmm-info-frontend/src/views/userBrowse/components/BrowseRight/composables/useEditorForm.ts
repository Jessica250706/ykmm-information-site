import { type ComputedRef, reactive, ref, watch } from 'vue'
import { DIALOGUE_ROLE, MONOLOGUE } from '@/constants'
import type { DialogueLineVO } from '@/types/dialogueLine'

export type EditorMode = 'edit' | 'create'

/** 编辑目标：普通行 / RC 选项对 */
export type EditTarget = 'normal' | 'rcOption'

export interface RcQuestionForm {
  id: number | null
  speakerId: number | null
  content: string
}

export interface RcAnswerForm {
  id: number | null
  speakerId: number | null
  content: string
}

export interface RcPairForm {
  /** 选项编号 */
  optionNumber: number
  /** 问句 */
  question: RcQuestionForm
  /** 回答 */
  answer: RcAnswerForm
}

export interface EditorForm {
  // ============ 普通行 ============
  /** 句子ID，新增时为 null */
  id: number | null
  /** 说话角色ID */
  speakerId: number | null
  /** 内容 */
  content: string
  /** RC聊天：1左 2右 */
  side: number | null
  /** 是否内心独白 */
  monologue: number

  // ============ RC 选项对 ============
  /** RC 选项对表单 */
  rcPair: RcPairForm

  // ============ 编辑目标 ============
  /** 当前编辑目标：普通行 / RC 选项 */
  editTarget: EditTarget
}

/** 创建空的 RC 选项对 */
function createEmptyRcPair(): RcPairForm {
  return {
    optionNumber: 1,
    question: { id: null, speakerId: null, content: '' },
    answer: { id: null, speakerId: null, content: '' },
  }
}

export function useEditorForm(
  editingLine: ComputedRef<DialogueLineVO | null>,
  /** 当前版本全部行，用于在编辑 RC 选项时找到对侧行 */
  allLines?: ComputedRef<DialogueLineVO[]>,
) {
  const editorMode = ref<EditorMode>('edit')
  const creatingAfterId = ref<number | null>(null)
  /** 新增目标是否为"开头"（优先级高于 creatingAfterId） */
  const creatingAtStart = ref(false)
  const dirty = ref(false)

  const editorForm = reactive<EditorForm>({
    id: null,
    speakerId: null,
    content: '',
    side: null,
    monologue: MONOLOGUE.SPOKEN,
    rcPair: createEmptyRcPair(),
    editTarget: 'normal',
  })

  /** 重置整个表单 */
  function resetForm() {
    editorForm.id = null
    editorForm.speakerId = null
    editorForm.content = ''
    editorForm.side = null
    editorForm.monologue = MONOLOGUE.SPOKEN
    editorForm.rcPair = createEmptyRcPair()
    editorForm.editTarget = 'normal'
  }

  /** 根据选中的行填充表单 */
  function fillForm(line: DialogueLineVO | null) {
    if (!line) {
      resetForm()
      return
    }

    const isQuestion = line.dialogueRole === DIALOGUE_ROLE.QUESTION
    const isAnswer = line.dialogueRole === DIALOGUE_ROLE.ANSWER

    /** RC 选项行：填 rcPair，并从 allLines 里找对侧 */
    if ((isQuestion || isAnswer) && line.optionNumber != null) {
      editorForm.editTarget = 'rcOption'
      editorForm.rcPair.optionNumber = line.optionNumber

      const lines = allLines?.value ?? []
      const questionLine = lines.find(
        (l) => l.optionNumber === line.optionNumber && l.dialogueRole === DIALOGUE_ROLE.QUESTION,
      )
      const answerLine = lines.find(
        (l) => l.optionNumber === line.optionNumber && l.dialogueRole === DIALOGUE_ROLE.ANSWER,
      )

      editorForm.rcPair.question = {
        id: questionLine?.id ?? null,
        speakerId: questionLine?.speakerId ?? null,
        content: questionLine?.content ?? '',
      }
      editorForm.rcPair.answer = {
        id: answerLine?.id ?? null,
        speakerId: answerLine?.speakerId ?? null,
        content: answerLine?.content ?? '',
      }
      return
    }

    /** 普通行 */
    editorForm.editTarget = 'normal'
    editorForm.id = line.id ?? null
    editorForm.speakerId = line.speakerId ?? null
    editorForm.content = line.content ?? ''
    editorForm.side = line.side ?? null
    editorForm.monologue = line.monologue ?? MONOLOGUE.SPOKEN
  }

  /** 外部选中行变化时同步表单；create 模式下外部切换则退出 create */
  watch(
    () => editingLine.value?.id,
    (newId, oldId) => {
      if (newId === oldId) return
      if (editorMode.value === 'create') {
        editorMode.value = 'edit'
        creatingAfterId.value = null
        creatingAtStart.value = false
      }
      fillForm(editingLine.value)
      dirty.value = false
    },
    { immediate: true },
  )

  /** 追踪 dirty：同时考虑普通表单和 RC 选项表单 */
  watch(
    () => [
      editorForm.speakerId,
      editorForm.content,
      editorForm.monologue,
      editorForm.editTarget,
      editorForm.rcPair.optionNumber,
      editorForm.rcPair.question.speakerId,
      editorForm.rcPair.question.content,
      editorForm.rcPair.answer.speakerId,
      editorForm.rcPair.answer.content,
    ],
    () => {
      /** create 模式：只要有输入就算 dirty */
      if (editorMode.value === 'create') {
        dirty.value =
          !!editorForm.content.trim() ||
          editorForm.speakerId != null ||
          editorForm.rcPair.question.speakerId != null ||
          !!editorForm.rcPair.question.content.trim() ||
          editorForm.rcPair.answer.speakerId != null ||
          !!editorForm.rcPair.answer.content.trim()
        return
      }

      const orig = editingLine.value
      if (!orig) {
        dirty.value = false
        return
      }

      /** edit 模式：RC 选项对比较 */
      if (editorForm.editTarget === 'rcOption') {
        const lines = allLines?.value ?? []
        const q = lines.find(
          (l) =>
            l.optionNumber === editorForm.rcPair.optionNumber &&
            l.dialogueRole === DIALOGUE_ROLE.QUESTION,
        )
        const a = lines.find(
          (l) =>
            l.optionNumber === editorForm.rcPair.optionNumber &&
            l.dialogueRole === DIALOGUE_ROLE.ANSWER,
        )
        dirty.value =
          editorForm.rcPair.question.speakerId !== (q?.speakerId ?? null) ||
          editorForm.rcPair.question.content !== (q?.content ?? '') ||
          editorForm.rcPair.answer.speakerId !== (a?.speakerId ?? null) ||
          editorForm.rcPair.answer.content !== (a?.content ?? '')
        return
      }

      /** edit 模式：普通行比较 */
      dirty.value =
        editorForm.speakerId !== (orig.speakerId ?? null) ||
        editorForm.content !== (orig.content ?? '') ||
        editorForm.monologue !== (orig.monologue ?? MONOLOGUE.SPOKEN)
    },
  )

  function enterCreateMode(afterId: number | null, atStart = false) {
    editorMode.value = 'create'
    creatingAfterId.value = afterId
    creatingAtStart.value = atStart
    fillForm(null)
    dirty.value = false
  }

  /** 进入"新增 RC 选项"模式 */
  function enterRcOptionCreate() {
    editorMode.value = 'create'
    creatingAfterId.value = null
    creatingAtStart.value = false
    editorForm.editTarget = 'rcOption'
    editorForm.rcPair = createEmptyRcPair()
    dirty.value = false
  }

  /**
   * 退出 create 模式。
   * keepForm=true 时不重置表单（用于保存成功后，等父级刷新再 fill）。
   */
  function exitCreateMode(options?: { keepForm?: boolean }) {
    editorMode.value = 'edit'
    creatingAfterId.value = null
    creatingAtStart.value = false
    if (!options?.keepForm) {
      fillForm(editingLine.value)
    }
    dirty.value = false
  }

  return {
    editorMode,
    creatingAfterId,
    creatingAtStart,
    dirty,
    editorForm,
    fillForm,
    resetForm,
    enterCreateMode,
    enterRcOptionCreate,
    exitCreateMode,
  }
}
