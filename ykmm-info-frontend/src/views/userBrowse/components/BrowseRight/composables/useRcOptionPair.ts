import { computed, type ComputedRef, ref, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  batchSaveDialogueLinesAPI,
  deleteDialogueLinesBatchAPI,
  updateDialogueLinesBatchAPI,
} from '@/api/dialogueLine'
import { DIALOGUE_ROLE } from '@/constants'
import type { DialogueLineDTO } from '@/types/dialogueLine'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import type { EditorForm, EditorMode } from './useEditorForm'

interface Options {
  editorForm: EditorForm
  editorMode: Ref<EditorMode>
  currentVersion: ComputedRef<DialogueVersionVO | null>
  dirty: Ref<boolean>
  /** 退出 create 模式 */
  exitCreateMode: (options?: { keepForm?: boolean }) => void
  /** 保存成功后回调（父级清空选中 + 刷新） */
  onSaved: () => void
  /** 取消 / 删除后回调（父级清空选中） */
  onCleared: () => void
  /** 删除成功后回调（需要刷新数据） */
  onDeleted: () => void
}

/**
 * RC 选项对编辑（问句 + 回答一次性提交）
 */
export function useRcOptionPair(opts: Options) {
  const saving = ref(false)
  const deleting = ref(false)

  /** 是否已保存过（至少一侧有 id） */
  const hasSaved = computed(
    () => opts.editorForm.rcPair.question.id != null || opts.editorForm.rcPair.answer.id != null,
  )

  /** 保存 RC 选项 */
  async function save(): Promise<boolean> {
    const versionId = opts.currentVersion.value?.id
    if (!versionId) {
      ElMessage.error('版本不存在')
      return false
    }

    const pair = opts.editorForm.rcPair
    if (pair.question.speakerId == null || !pair.question.content?.trim()) {
      ElMessage.warning('请填写问句的说话人和内容')
      return false
    }
    if (pair.answer.speakerId == null || !pair.answer.content?.trim()) {
      ElMessage.warning('请填写回答的说话人和内容')
      return false
    }

    const lines: DialogueLineDTO[] = [
      {
        id: pair.question.id ?? undefined,
        speakerId: pair.question.speakerId,
        content: pair.question.content,
        dialogueRole: DIALOGUE_ROLE.QUESTION,
        optionNumber: pair.optionNumber,
      },
      {
        id: pair.answer.id ?? undefined,
        speakerId: pair.answer.speakerId,
        content: pair.answer.content,
        dialogueRole: DIALOGUE_ROLE.ANSWER,
        optionNumber: pair.optionNumber,
      },
    ]

    saving.value = true
    try {
      if (hasSaved.value) {
        await updateDialogueLinesBatchAPI(versionId, lines)
        ElMessage.success('保存选项成功')
      } else {
        await batchSaveDialogueLinesAPI(versionId, lines)
        ElMessage.success('新增选项成功')
      }
      opts.dirty.value = false
      opts.exitCreateMode({ keepForm: false })
      opts.onSaved()
      return true
    } catch (err) {
      console.error('保存选项失败', err)
      return false
    } finally {
      saving.value = false
    }
  }

  /** 删除 RC 选项 */
  async function remove(): Promise<boolean> {
    const pair = opts.editorForm.rcPair
    const lineIds = [pair.question.id, pair.answer.id].filter((id): id is number => id != null)
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
      opts.onDeleted()
      return true
    } catch (err) {
      console.error('删除选项失败', err)
      return false
    } finally {
      deleting.value = false
    }
  }

  /** 取消：
   * - create 模式：退出新增
   * - edit 模式：清空选中
   */
  function cancel() {
    if (opts.editorMode.value === 'create') {
      opts.exitCreateMode()
    } else {
      opts.onCleared()
    }
  }

  return { saving, deleting, hasSaved, save, remove, cancel }
}
