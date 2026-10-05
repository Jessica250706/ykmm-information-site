import { computed, type ComputedRef, ref, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import { batchSaveDialogueLinesAPI, updateDialogueLineAPI } from '@/api/dialogueLine'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import type { EditorForm, EditorMode } from './useEditorForm'

interface Options {
  editorForm: EditorForm
  editorMode: Ref<EditorMode>
  creatingAfterId: Ref<number | null>
  dirty: Ref<boolean>
  currentVersion: ComputedRef<DialogueVersionVO | null>
  editingLine: ComputedRef<DialogueLineVO | null>
  editingLineIndex: ComputedRef<number>
  allLines: ComputedRef<DialogueLineVO[]>
  onSelectLine: (line: DialogueLineVO) => void
  onSaveDone: () => void
  onAddDone: (afterId: number | null) => void
  enterCreateMode: (afterId: number | null) => void
  exitCreateMode: (options?: { keepForm?: boolean }) => void
}

export function useEditorActions(opts: Options) {
  const saving = ref(false)

  const hasPrev = computed(() => {
    if (opts.editorMode.value === 'create') return true
    return opts.editingLineIndex.value > 0
  })

  const hasNext = computed(() => {
    if (opts.editorMode.value === 'create') return true
    const i = opts.editingLineIndex.value
    return i >= 0 && i < opts.allLines.value.length - 1
  })

  function onAdd() {
    if (opts.editorMode.value === 'create') return
    opts.enterCreateMode(opts.editingLine.value?.id ?? null)
  }

  function onCancelCreate() {
    opts.exitCreateMode()
  }

  async function onSave(): Promise<boolean> {
    if (opts.editorForm.speakerId == null) {
      ElMessage.warning('请选择说话人')
      return false
    }
    if (!opts.editorForm.content?.trim()) {
      ElMessage.warning('内容不能为空')
      return false
    }
    saving.value = true
    try {
      if (opts.editorMode.value === 'create') {
        return await saveCreate()
      }
      return await saveEdit()
    } finally {
      saving.value = false
    }
  }

  async function saveEdit(): Promise<boolean> {
    const id = opts.editorForm.id
    if (id == null) {
      ElMessage.warning('无效的编辑行')
      return false
    }
    try {
      await updateDialogueLineAPI(id, {
        id,
        speakerId: opts.editorForm.speakerId!,
        content: opts.editorForm.content,
        side: opts.editorForm.side ?? undefined,
        monologue: opts.editorForm.monologue,
      })
      ElMessage.success('保存成功')
      opts.dirty.value = false
      opts.onSaveDone()
      return true
    } catch (err) {
      console.error('保存失败', err)
      return false
    }
  }

  async function saveCreate(): Promise<boolean> {
    const versionId = opts.currentVersion.value?.id
    if (!versionId) {
      ElMessage.error('版本不存在')
      return false
    }

    const afterId = opts.creatingAfterId.value
    try {
      await batchSaveDialogueLinesAPI(versionId, [
        {
          speakerId: opts.editorForm.speakerId!,
          content: opts.editorForm.content,
          side: opts.editorForm.side ?? undefined,
          monologue: opts.editorForm.monologue,
        },
      ])
      ElMessage.success('新增成功')
      // 立即退出 create，但保留表单，等父级刷新后 watch 会 fill 到新行
      opts.exitCreateMode({ keepForm: true })
      opts.onAddDone(afterId)
      return true
    } catch (err) {
      console.error('新增失败', err)
      return false
    }
  }

  async function goPrev() {
    if (opts.editorMode.value === 'create') {
      if (opts.dirty.value) {
        await onSave()
        return
      }
      opts.exitCreateMode()
    }

    const i = opts.editingLineIndex.value
    if (i <= 0) return
    const prev = opts.allLines.value[i - 1]
    if (!prev) return

    if (opts.dirty.value) {
      const ok = await onSave()
      if (!ok) return
    }
    opts.onSelectLine(prev)
  }

  async function goNext() {
    if (opts.editorMode.value === 'create') {
      if (opts.dirty.value) {
        await onSave()
        return
      }
      opts.exitCreateMode()
    }

    const i = opts.editingLineIndex.value
    const lines = opts.allLines.value
    if (i < 0 || i >= lines.length - 1) return
    const next = lines[i + 1]
    if (!next) return

    if (opts.dirty.value) {
      const ok = await onSave()
      if (!ok) return
    }
    opts.onSelectLine(next)
  }

  return {
    saving,
    hasPrev,
    hasNext,
    onAdd,
    onCancelCreate,
    onSave,
    goPrev,
    goNext,
  }
}
