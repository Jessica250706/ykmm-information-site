import { computed, ref, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import { sortDialogueLinesAPI } from '@/api/dialogueLine'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'

export function useDialogueEdit(storyDetail: Ref<StoryDetailVO | null>) {
  /** 是否处于编辑模式 */
  const editingMode = ref(false)
  /** 当前选中的版本 id */
  const currentVersionId = ref<number | null>(null)
  /** 当前选中的行 id */
  const editingLineId = ref<number | null>(null)
  /** 新增句子时要插到哪一句后面，null 表示追加到末尾 */
  const pendingInsertAfterId = ref<number | null>(null)
  /** 新增句子时是否要插到开头（优先级高于 pendingInsertAfterId） */
  const pendingInsertAtStart = ref(false)

  /** 当前选中的版本 */
  const currentVersion = computed<DialogueVersionVO | null>(() => {
    if (!storyDetail.value?.versions?.length || currentVersionId.value == null) return null
    return storyDetail.value.versions.find((v) => v.id === currentVersionId.value) ?? null
  })

  /** 当前编辑的句子 */
  const editingLine = computed<DialogueLineVO | null>(() => {
    if (!currentVersion.value?.lines?.length || editingLineId.value == null) return null
    return currentVersion.value.lines.find((l) => l.id === editingLineId.value) ?? null
  })

  /** 当前编辑的句子在版本中的下标 */
  const editingLineIndex = computed(() => {
    if (!currentVersion.value?.lines?.length || editingLineId.value == null) return -1
    return currentVersion.value.lines.findIndex((l) => l.id === editingLineId.value)
  })

  /** storyDetail 更新后：保留已选版本；若原版本不存在则回退到第一个 */
  function syncVersionSelection() {
    const versions = storyDetail.value?.versions ?? []
    if (versions.length) {
      const stillExists = versions.some((v) => v.id === currentVersionId.value)
      if (!stillExists) {
        currentVersionId.value = versions[0]!.id ?? null
      }
    } else {
      currentVersionId.value = null
    }
  }

  /** storyDetail 更新后：若当前行已不存在，清空高亮 */
  function syncEditingLine() {
    const lines = currentVersion.value?.lines ?? []
    if (editingLineId.value != null && !lines.some((l) => l.id === editingLineId.value)) {
      editingLineId.value = null
    }
  }

  /** 非 story 视图时清理编辑状态 */
  function resetEditState() {
    currentVersionId.value = null
    editingLineId.value = null
    pendingInsertAfterId.value = null
    pendingInsertAtStart.value = false
  }

  function selectLine(line: DialogueLineVO) {
    editingLineId.value = line.id ?? null
  }

  /**
   * 若存在待插入目标，把刚追加到末尾的句子移动到目标句之后
   */
  async function applyPendingInsert() {
    const atStart = pendingInsertAtStart.value
    const targetId = pendingInsertAfterId.value

    // 先重置，避免下次误用
    pendingInsertAfterId.value = null
    pendingInsertAtStart.value = false

    // 两者都没设置 → 无需重排
    if (!atStart && targetId == null) {
      return
    }

    const version = currentVersion.value
    if (!version?.id || !version.lines?.length) {
      return
    }

    const lines = version.lines
    const newLine = lines[lines.length - 1]
    if (!newLine?.id) {
      return
    }

    let targetIndex: number
    if (atStart) {
      // 插到位置 0 之前 → 用 -1 让 splice(targetIndex + 1, ...) = 0
      targetIndex = -1
    } else {
      targetIndex = lines.findIndex((l) => l.id === targetId)
      // 目标句不存在，或新行本来就在末尾，无需重排
      if (targetIndex < 0 || targetIndex >= lines.length - 1) {
        return
      }
    }

    const ordered = [...lines]
    const [moved] = ordered.splice(ordered.length - 1, 1)
    if (!moved) return
    ordered.splice(targetIndex + 1, 0, moved)

    try {
      await sortDialogueLinesAPI(version.id, {
        lineIds: ordered.map((l) => l.id!),
      })
      if (currentVersion.value) {
        currentVersion.value.lines = ordered
      }
    } catch (err) {
      console.error('[applyPendingInsert] sort failed', err)
      ElMessage.error('对话顺序调整失败')
    }
  }

  return {
    editingMode,
    currentVersionId,
    editingLineId,
    pendingInsertAfterId,
    pendingInsertAtStart,
    currentVersion,
    editingLine,
    editingLineIndex,
    syncVersionSelection,
    syncEditingLine,
    resetEditState,
    selectLine,
    applyPendingInsert,
  }
}
