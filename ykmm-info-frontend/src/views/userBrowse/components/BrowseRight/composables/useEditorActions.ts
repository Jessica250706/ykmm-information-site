import { computed, type ComputedRef, ref, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  batchSaveDialogueLinesAPI,
  deleteDialogueLineAPI,
  deleteDialogueLinesBatchAPI,
  updateDialogueLineAPI,
} from '@/api/dialogueLine'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import type { EditorForm, EditorMode } from './useEditorForm'

interface Options {
  editorForm: EditorForm
  editorMode: Ref<EditorMode>
  creatingAfterId: Ref<number | null>
  creatingAtStart: Ref<boolean>
  dirty: Ref<boolean>
  currentVersion: ComputedRef<DialogueVersionVO | null>
  editingLine: ComputedRef<DialogueLineVO | null>
  editingLineIndex: ComputedRef<number>
  allLines: ComputedRef<DialogueLineVO[]>
  onSelectLine: (line: DialogueLineVO) => void
  onSaveDone: () => void
  onAddDone: (payload: { afterId: number | null; atStart: boolean }) => void
  enterCreateMode: (afterId: number | null, atStart?: boolean) => void
  exitCreateMode: (options?: { keepForm?: boolean }) => void
  /** 删除成功后：父级负责刷新 + 定位到相邻行 */
  onDeleteDone: (payload: {
    deletedId: number
    prevId: number | null
    nextId: number | null
  }) => void
  /** RC 选项删除成功后：父级负责清空选中 + 刷新 */
  onRcOptionDeleteDone?: () => void
}

export function useEditorActions(opts: Options) {
  const saving = ref(false)
  const deleting = ref(false)

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
    opts.enterCreateMode(opts.editingLine.value?.id ?? null, false)
  }

  /** 进入创建模式，目标为开头；已在创建模式时只切换目标 */
  function onAddAtStart() {
    if (opts.editorMode.value === 'create') {
      opts.creatingAfterId.value = null
      opts.creatingAtStart.value = true
    } else {
      opts.enterCreateMode(null, true)
    }
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
    const atStart = opts.creatingAtStart.value

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
      opts.exitCreateMode({ keepForm: true })
      opts.onAddDone({ afterId, atStart })
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

  async function onDelete(): Promise<boolean> {
    // 只在 edit 模式、且有选中行时可删
    if (opts.editorMode.value !== 'edit') return false
    const id = opts.editorForm.id
    if (id == null) {
      ElMessage.warning('没有可删除的对话')
      return false
    }

    // 二次确认
    try {
      await ElMessageBox.confirm('确定要删除这条对话吗？删除后不可恢复。', '删除确认', {
        type: 'warning',
        confirmButtonText: '删除',
        confirmButtonClass: 'el-button--danger',
        cancelButtonText: '取消',
      })
    } catch {
      return false // 用户取消
    }

    // 计算相邻行 id，供父级删除后定位
    const i = opts.editingLineIndex.value
    const lines = opts.allLines.value
    const prevId = i > 0 ? (lines[i - 1]?.id ?? null) : null
    const nextId = i >= 0 && i < lines.length - 1 ? (lines[i + 1]?.id ?? null) : null

    deleting.value = true
    try {
      await deleteDialogueLineAPI(id)
      ElMessage.success('删除成功')
      opts.dirty.value = false
      opts.onDeleteDone({ deletedId: id, prevId, nextId })
      return true
    } catch (err) {
      console.error('删除失败', err)
      return false
    } finally {
      deleting.value = false
    }
  }

  /**
   * 删除 RC 选项对（问句 + 回答）
   *
   * 规则：
   * - 至少有一行带 id 才能删
   * - 收集双方 id 一次批量删除
   * - 删除后 emit clear-line + save-line
   */
  async function onDeleteRcOptionPair(): Promise<boolean> {
    const pair = opts.editorForm.rcPair
    const questionId = pair.question.id
    const answerId = pair.answer.id

    const lineIds = [questionId, answerId].filter((id): id is number => id != null)
    if (lineIds.length === 0) {
      ElMessage.warning('该选项尚未保存，无法删除')
      return false
    }

    try {
      await ElMessageBox.confirm(
        '确定要删除这个选项吗？问句和回答都会被删除，删除后不可恢复。',
        '删除确认',
        {
          type: 'warning',
          confirmButtonText: '删除',
          confirmButtonClass: 'el-button--danger',
          cancelButtonText: '取消',
        },
      )
    } catch {
      return false
    }

    deleting.value = true
    try {
      await deleteDialogueLinesBatchAPI(lineIds)
      ElMessage.success('删除选项成功')
      opts.dirty.value = false
      opts.onRcOptionDeleteDone?.()
      return true
    } catch (err) {
      console.error('删除选项失败', err)
      return false
    } finally {
      deleting.value = false
    }
  }

  return {
    saving,
    deleting,
    hasPrev,
    hasNext,
    onAdd,
    onAddAtStart,
    onCancelCreate,
    onSave,
    onDelete,
    onDeleteRcOptionPair,
    goPrev,
    goNext,
  }
}
