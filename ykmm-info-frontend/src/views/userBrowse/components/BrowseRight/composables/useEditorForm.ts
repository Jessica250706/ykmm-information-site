import { type ComputedRef, reactive, ref, watch } from 'vue'
import { MONOLOGUE } from '@/constants'
import type { DialogueLineVO } from '@/types/dialogueLine'

export type EditorMode = 'edit' | 'create'

export interface EditorForm {
  id: number | null
  speakerId: number | null
  content: string
  side: number | null
  monologue: number
}

export function useEditorForm(editingLine: ComputedRef<DialogueLineVO | null>) {
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
  })

  function fillForm(line: DialogueLineVO | null) {
    if (line) {
      editorForm.id = line.id ?? null
      editorForm.speakerId = line.speakerId ?? null
      editorForm.content = line.content ?? ''
      editorForm.side = line.side ?? null
      editorForm.monologue = line.monologue ?? MONOLOGUE.SPOKEN
    } else {
      editorForm.id = null
      editorForm.speakerId = null
      editorForm.content = ''
      editorForm.side = null
      editorForm.monologue = MONOLOGUE.SPOKEN
    }
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

  /** 追踪 dirty */
  watch(
    () => [editorForm.speakerId, editorForm.content, editorForm.monologue],
    () => {
      if (editorMode.value === 'create') {
        dirty.value = !!editorForm.content.trim() || editorForm.speakerId != null
        return
      }
      const orig = editingLine.value
      if (!orig) {
        dirty.value = false
        return
      }
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
    enterCreateMode,
    exitCreateMode,
  }
}
