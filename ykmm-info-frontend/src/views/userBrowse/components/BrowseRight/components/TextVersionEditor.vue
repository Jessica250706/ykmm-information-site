<template>
  <div>
    <!-- txt 上传 -->
    <div class="mb-4">
      <el-button class="w-full" type="info" plain @click="onTxtUploadClick">
        📄 上传 txt 文件
      </el-button>
      <input
        ref="txtInputRef"
        accept=".txt"
        style="display: none"
        type="file"
        @change="onTxtFileChangeLocal"
      />
      <div class="mt-1 text-xs text-slate-400">上传 txt 会自动覆盖当前故事的所有对话信息</div>
    </div>

    <!-- 在线编辑 -->
    <div class="border-t pt-3">
      <div class="flex items-center justify-between mb-2">
        <div class="text-sm font-medium">在线编辑</div>
        <div class="flex items-center gap-1">
          <el-button :disabled="!hasPrev" size="small" @click="goPrev">← 上一条</el-button>
          <el-button :disabled="!hasNext" size="small" @click="goNext">下一条 →</el-button>
        </div>
      </div>

      <el-alert
        v-if="editorMode === EDITOR_MODE.CREATE"
        :closable="false"
        class="mb-4!"
        type="info"
        show-icon
      >
        {{
          editorForm.editTarget === EDIT_TARGET.RC_OPTION
            ? '正在新增 RC 选项'
            : creatingAtStart
              ? '将追加到开头'
              : creatingAfterId != null
                ? '将在当前句之后插入新对话'
                : '将追加到末尾'
        }}
      </el-alert>

      <!-- RC 选项编辑 -->
      <RcOptionForm
        v-if="editorForm.editTarget === EDIT_TARGET.RC_OPTION"
        v-model:pair="editorForm.rcPair"
        :deleting="rcDeleting"
        :role-options="roleOptions"
        :saving="rcSaving"
        :show-delete="hasRcOptionSaved"
        @cancel="onCancelRcOption"
        @delete="onDeleteRcOptionPair"
        @save="onSaveRcOptionPair"
      />

      <!-- 普通行编辑 -->
      <NormalLineForm
        v-else
        v-model:form="editorForm"
        :deleting="deleting"
        :editor-mode="editorMode"
        :has-editing-line="!!editingLine"
        :role-options="roleOptions"
        :saving="saving"
        :show-add-rc-option="sourceType === SOURCE_TYPE.RC"
        :source-type="sourceType"
        @add="onAdd"
        @add-at-start="onAddAtStart"
        @add-rc-option="onAddRcOption"
        @cancel-create="onCancelCreate"
        @delete="onDelete"
        @save="onSave"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { EDIT_TARGET, EDITOR_MODE, SOURCE_TYPE, type SourceTypeValue } from '@/constants'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import { useEditorActions } from '../composables/useEditorActions'
import { useEditorForm } from '../composables/useEditorForm'
import { useRcOptionPair } from '../composables/useRcOptionPair'
import { useRoleOptions } from '../composables/useRoleOptions'
import { useTxtUpload } from '../composables/useTxtUpload'
import NormalLineForm from './NormalLineForm.vue'
import RcOptionForm from './RcOptionForm.vue'

const props = defineProps<{
  currentVersion: DialogueVersionVO | null
  editingLine: DialogueLineVO | null
  editingLineIndex: number
  allLines: DialogueLineVO[]
  sourceType: SourceTypeValue
  editingMode: boolean
}>()

const emit = defineEmits<{
  'save-line': []
  'add-line': [payload: { afterId: number | null; atStart: boolean }]
  'select-line': [line: DialogueLineVO]
  'clear-line': []
  'delete-line': [payload: { deletedId: number; prevId: number | null; nextId: number | null }]
  refresh: []
}>()

/* ============================================================
 * 给 composable 用的 Ref
 * ============================================================ */
const editingLineRef = computed(() => props.editingLine)
const editingLineIndexRef = computed(() => props.editingLineIndex)
const allLinesRef = computed(() => props.allLines)
const currentVersionRef = computed(() => props.currentVersion)
const editingModeRef = computed(() => props.editingMode)

/* ============================================================
 * 角色下拉
 * ============================================================ */
const { roleOptions } = useRoleOptions(editingModeRef)

/* ============================================================
 * 编辑器表单
 * ============================================================ */
const {
  editorMode,
  creatingAfterId,
  creatingAtStart,
  dirty,
  editorForm,
  enterCreateMode,
  enterRcOptionCreate,
  exitCreateMode,
} = useEditorForm(editingLineRef, allLinesRef)

/* ============================================================
 * 普通行动作
 * ============================================================ */
const {
  saving,
  deleting,
  hasPrev,
  hasNext,
  onAdd,
  onAddAtStart,
  onCancelCreate,
  onSave,
  onDelete,
  goPrev,
  goNext,
} = useEditorActions({
  editorForm,
  editorMode,
  creatingAfterId,
  creatingAtStart,
  dirty,
  currentVersion: currentVersionRef,
  editingLine: editingLineRef,
  editingLineIndex: editingLineIndexRef,
  allLines: allLinesRef,
  onSelectLine: (line) => emit('select-line', line),
  onSaveDone: () => emit('save-line'),
  onAddDone: (payload) => emit('add-line', payload),
  onDeleteDone: (payload) => emit('delete-line', payload),
  enterCreateMode,
  exitCreateMode,
})

/* ============================================================
 * RC 选项动作
 * ============================================================ */
const {
  saving: rcSaving,
  deleting: rcDeleting,
  hasSaved: hasRcOptionSaved,
  save: rcSave,
  remove: rcRemove,
  cancel: rcCancel,
} = useRcOptionPair({
  editorForm,
  editorMode,
  currentVersion: currentVersionRef,
  dirty,
  exitCreateMode,
  /** 保存成功：清空选中 + 刷新 */
  onSaved: () => {
    emit('clear-line')
    emit('save-line')
  },
  /** 取消：只清空选中 */
  onCleared: () => {
    emit('clear-line')
  },
  /** ★ 删除成功：清空选中 + 刷新 */
  onDeleted: () => {
    emit('clear-line')
    emit('save-line')
  },
})

function onSaveRcOptionPair() {
  void rcSave()
}

function onDeleteRcOptionPair() {
  void rcRemove()
}

function onCancelRcOption() {
  rcCancel()
}

function onAddRcOption() {
  enterRcOptionCreate()
}

/* ============================================================
 * txt 上传
 * ============================================================ */
const { txtInputRef, onTxtUploadClick, onTxtFileChange } = useTxtUpload(currentVersionRef)

async function onTxtFileChangeLocal(e: Event) {
  const ok = await onTxtFileChange(e)
  if (ok) emit('refresh')
}
</script>
