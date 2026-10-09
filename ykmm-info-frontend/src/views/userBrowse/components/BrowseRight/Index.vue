<template>
  <el-card
    body-class="flex flex-col h-full overflow-hidden p-4"
    class="w-96 shrink-0 flex flex-col border-l bg-white"
  >
    <!-- 顶部：编辑模式开关 -->
    <div class="flex items-center justify-between border-b pb-3 mb-3 shrink-0">
      <div class="font-medium">编辑区</div>
      <el-switch v-model="editingModeLocal" active-text="编辑模式" inline-prompt />
    </div>

    <!-- 1. 未开启编辑模式 -->
    <div v-if="!editingModeLocal" class="flex-1 flex items-center justify-center">
      <el-empty description="开启编辑模式以编辑对话" />
    </div>

    <!-- 2. 无故事 -->
    <div v-else-if="!storyDetail" class="flex-1 flex items-center justify-center">
      <el-empty description="请选择一个剧情" />
    </div>

    <!-- 3. 未选择版本 -->
    <div v-else-if="!currentOptionVersion" class="flex-1 flex items-center justify-center">
      <el-empty description="请选择一个对话版本" />
    </div>

    <!-- 4. 选中了版本，但该版本还没有内容 -->
    <div v-else-if="!currentVersion" class="flex-1 flex items-center justify-center">
      <el-empty :description="emptyVersionText">
        <el-button :loading="creatingVersion" type="primary" @click="onCreateVersion">
          创建该版本内容
        </el-button>
      </el-empty>
    </div>

    <!-- 5. 编辑内容 -->
    <div v-else class="flex-1 overflow-auto min-h-0">
      <!-- 图片版本 -->
      <template v-if="currentVersion.format === 2">
        <div class="text-sm font-medium mb-2">图片列表</div>

        <ImageUpload
          v-model="imageUrls"
          :max-count="50"
          :max-size="10"
          tip="支持多图上传，顺序即为展示顺序"
          multiple
        />

        <div class="mt-2 flex justify-center">
          <el-button :loading="imageSaving" type="primary" @click="saveImages">保存图片</el-button>
        </div>
      </template>

      <!-- 文字版本 -->
      <template v-else-if="currentVersion.format === 1">
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
            @change="onTxtFileChange"
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
            v-if="editorMode === 'create'"
            :closable="false"
            class="mb-4!"
            type="info"
            show-icon
          >
            {{
              editorForm.editTarget === 'rcOption'
                ? '正在新增 RC 选项'
                : creatingAtStart
                  ? '将追加到开头'
                  : creatingAfterId != null
                    ? '将在当前句之后插入新对话'
                    : '将追加到末尾'
            }}
          </el-alert>

          <!-- =============================================
               模式 A：RC 选项编辑
               ============================================= -->
          <template v-if="editorForm.editTarget === 'rcOption'">
            <el-form label-width="70px" size="default">
              <el-form-item label="选项编号">
                <el-input-number
                  v-model="editorForm.rcPair.optionNumber"
                  :controls="false"
                  :min="1"
                  placeholder="如 1、2、3"
                  style="width: 100%"
                />
              </el-form-item>

              <div class="mb-2 text-xs font-medium text-slate-500">问句</div>
              <el-form-item label="说话人">
                <el-cascader
                  v-model="editorForm.rcPair.question.speakerId"
                  :options="roleCascaderOptions"
                  :props="cascaderProps"
                  :show-all-levels="false"
                  class="w-full"
                  placeholder="请选择角色"
                  clearable
                  filterable
                />
              </el-form-item>
              <el-form-item label="内容">
                <el-input
                  v-model="editorForm.rcPair.question.content"
                  :rows="3"
                  placeholder="请输入问句内容"
                  type="textarea"
                />
              </el-form-item>

              <div class="mb-2 text-xs font-medium text-slate-500">回答</div>
              <el-form-item label="说话人">
                <el-cascader
                  v-model="editorForm.rcPair.answer.speakerId"
                  :options="roleCascaderOptions"
                  :props="cascaderProps"
                  :show-all-levels="false"
                  class="w-full"
                  placeholder="请选择角色"
                  clearable
                  filterable
                />
              </el-form-item>
              <el-form-item label="内容">
                <el-input
                  v-model="editorForm.rcPair.answer.content"
                  :rows="3"
                  placeholder="请输入回答内容"
                  type="textarea"
                />
              </el-form-item>
            </el-form>

            <div class="flex justify-between">
              <div class="flex items-center gap-1">
                <el-button @click="onCancelRcOption">取消</el-button>
                <el-button
                  v-if="hasRcOptionSaved"
                  :loading="deleting"
                  type="danger"
                  plain
                  @click="onDeleteRcOptionPair"
                >
                  删除选项
                </el-button>
              </div>
              <el-button :loading="saving" type="primary" @click="onSaveRcOptionPair">
                保存选项
              </el-button>
            </div>
          </template>

          <!-- 模式 B：普通行编辑 -->
          <template v-else>
            <el-form label-width="70px" size="default">
              <el-form-item label="说话人">
                <el-cascader
                  v-model="editorForm.speakerId"
                  :options="roleCascaderOptions"
                  :props="cascaderProps"
                  :show-all-levels="false"
                  class="w-full"
                  placeholder="请选择角色"
                  clearable
                  filterable
                />
              </el-form-item>
              <el-form-item
                v-if="sourceType === SOURCE_TYPE.STORY || sourceType === SOURCE_TYPE.RTV"
                label="内心独白"
              >
                <el-switch
                  v-model="editorForm.monologue"
                  :active-value="MONOLOGUE.INNER"
                  :inactive-value="MONOLOGUE.SPOKEN"
                  active-text="内心独白"
                  inactive-text="说出来的话"
                  inline-prompt
                />
              </el-form-item>
              <el-form-item label="内容">
                <el-input
                  v-model="editorForm.content"
                  :rows="4"
                  placeholder="请输入对话内容，可含表情包标签，如 [国王布丁表情包]"
                  type="textarea"
                />
              </el-form-item>
            </el-form>

            <div class="flex justify-between">
              <div class="flex items-center gap-1">
                <template v-if="editorMode === 'edit'">
                  <el-button @click="onAdd">+ 新增</el-button>
                  <el-button v-if="sourceType === SOURCE_TYPE.RC" @click="onAddRcOption">
                    + 新增选项
                  </el-button>
                  <el-button
                    v-if="editingLine"
                    :loading="deleting"
                    type="danger"
                    plain
                    @click="onDelete"
                  >
                    删除
                  </el-button>
                </template>
                <template v-else>
                  <el-button @click="onCancelCreate">取消新增</el-button>
                  <el-button plain @click="onAddAtStart">↑ 追加到开头</el-button>
                </template>
              </div>
              <el-button
                :disabled="!editorForm.id && editorMode === 'edit'"
                :loading="saving"
                type="primary"
                @click="onSave"
              >
                {{ editorMode === 'create' ? '创建' : '保存' }}
              </el-button>
            </div>

            <div v-if="editorMode === 'edit' && !editingLine" class="mt-2 text-xs text-slate-400">
              点击左侧某一句对话可加载到编辑器；未选中时新增将追加到末尾。
            </div>
            <div v-else-if="editorMode === 'create'" class="mt-2 text-xs text-slate-400">
              填写完成后点击"创建"，将作为新对话插入。
            </div>
          </template>
        </div>
      </template>
    </div>
  </el-card>

  <!-- 创建版本弹窗 -->
  <CreateVersionDialog
    ref="createVersionDialogRef"
    :confirming="creatingVersion"
    @confirm="handleCreateVersionConfirm"
  />
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useVModel } from '@vueuse/core'
import { ElMessage } from 'element-plus'
import { batchSaveDialogueLinesAPI, updateDialogueLinesBatchAPI } from '@/api/dialogueLine'
import { DIALOGUE_ROLE, MONOLOGUE, SOURCE_TYPE, type SourceTypeValue } from '@/constants'
import { useUserStore } from '@/stores/userStore'
import type { DialogueLineDTO, DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO, DialogueVersionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'
import { useEditorActions } from './composables/useEditorActions'
import { useEditorForm } from './composables/useEditorForm'
import { useImageUpload } from './composables/useImageUpload'
import { useRoleOptions } from './composables/useRoleOptions'
import { useTxtUpload } from './composables/useTxtUpload'
import CreateVersionDialog from './CreateVersionDialog.vue'
import type { CreateVersionPayload } from './CreateVersionDialog.vue'

/* ============================================================
 * Props / Emits
 * ============================================================ */

type CreateVersionEmit = {
  option: DialogueVersionOptionVO
  contributorUserId: number | null
  contributorName: string | null
}

const props = defineProps<{
  editingMode: boolean
  storyDetail: StoryDetailVO | null
  currentVersion: DialogueVersionVO | null
  editingLine: DialogueLineVO | null
  editingLineIndex: number
  allLines: DialogueLineVO[]
  pendingInsertAfterId: number | null
  currentOptionVersion: DialogueVersionOptionVO | null
  /** 来源类型：1-RC 2-RTV 3-Rabitter 4-story */
  sourceType: SourceTypeValue
}>()

const emit = defineEmits<{
  'update:editingMode': [val: boolean]
  'save-line': []
  'add-line': [payload: { afterId: number | null; atStart: boolean }]
  refresh: []
  'select-line': [line: DialogueLineVO]
  'create-version': [option: CreateVersionEmit]
  'delete-line': [payload: { deletedId: number; prevId: number | null; nextId: number | null }]
  /** 清空当前选中行 */
  'clear-line': []
}>()

const editingModeLocal = useVModel(props, 'editingMode', emit, {
  passive: true,
})

/* ============================================================
 * 给 composable 用的 Ref
 * ============================================================ */
const editingLineRef = computed(() => props.editingLine)
const editingLineIndexRef = computed(() => props.editingLineIndex)
const allLinesRef = computed(() => props.allLines)
const currentVersionRef = computed(() => props.currentVersion)

const userStore = useUserStore()

/** 选项是否已经保存过（至少一侧有 id），用于控制"删除选项"按钮是否可用 */
const hasRcOptionSaved = computed(
  () => editorForm.rcPair.question.id != null || editorForm.rcPair.answer.id != null,
)

/* ============================================================
 * 空版本分支
 * ============================================================ */
const emptyVersionText = computed(() => {
  const label = props.currentOptionVersion?.label ?? '该版本'
  return `「${label}」尚未创建内容`
})

const creatingVersion = ref(false)
const createVersionDialogRef = ref<InstanceType<typeof CreateVersionDialog> | null>(null)

function onCreateVersion() {
  const option = props.currentOptionVersion
  if (!option) return
  if (userStore.isAdmin) createVersionDialogRef.value?.open(option)
  else if (userStore.isLogin) {
    emit('create-version', {
      option: option,
      contributorUserId: null,
      contributorName: null,
    })
  }
}

function handleCreateVersionConfirm(payload: CreateVersionPayload) {
  emit('create-version', {
    option: payload.option,
    contributorUserId: payload.contributorUserId,
    contributorName: payload.contributorName,
  })
}

/* ============================================================
 * 角色下拉
 * ============================================================ */
const { roleOptions } = useRoleOptions(editingModeLocal)

const cascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: false,
  checkStrictly: false,
} as const

const roleCascaderOptions = computed(() =>
  roleOptions.value.map((g) => ({
    value: `person:${g.personId ?? 'other'}`,
    label: g.personName ?? '未分组',
    disabled: false,
    children: (g.roles ?? []).map((r) => ({
      value: r.id!,
      label: r.name ?? '',
    })),
  })),
)

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
 * 编辑器动作
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
  onDeleteRcOptionPair,
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
  /** RC 选项删除完成后：清空选中 + 刷新 */
  onRcOptionDeleteDone: () => {
    emit('clear-line')
    emit('save-line')
  },
  enterCreateMode,
  exitCreateMode,
})

/* ============================================================
 * RC 选项：新增 / 保存
 * ============================================================ */

/** 进入"新增 RC 选项"模式 */
function onAddRcOption() {
  enterRcOptionCreate()
}

/** 保存 RC 选项对（问句 + 回答 一起提交） */
async function onSaveRcOptionPair() {
  const versionId = currentVersionRef.value?.id
  if (!versionId) {
    ElMessage.error('版本不存在')
    return
  }

  const pair = editorForm.rcPair
  if (pair.question.speakerId == null || !pair.question.content?.trim()) {
    ElMessage.warning('请填写问句的说话人和内容')
    return
  }
  if (pair.answer.speakerId == null || !pair.answer.content?.trim()) {
    ElMessage.warning('请填写回答的说话人和内容')
    return
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

  const hasId = pair.question.id != null || pair.answer.id != null

  saving.value = true
  try {
    if (hasId) {
      await updateDialogueLinesBatchAPI(versionId, lines)
      ElMessage.success('保存选项成功')
    } else {
      await batchSaveDialogueLinesAPI(versionId, lines)
      ElMessage.success('新增选项成功')
    }
    dirty.value = false
    /** 退出 create 模式；keepForm 防止覆盖刚才的清空动作 */
    exitCreateMode({ keepForm: false })
    emit('clear-line')
    emit('save-line')
  } catch (err) {
    console.error('保存选项失败', err)
  } finally {
    saving.value = false
  }
}

/**
 * RC 选项编辑区的"取消"：
 * - create 模式：取消新增
 * - edit 模式：取消选中（清空 editingLineId）
 */
function onCancelRcOption() {
  if (editorMode.value === 'create') {
    onCancelCreate()
  } else {
    emit('clear-line')
  }
}

/* ============================================================
 * 图片上传
 * ============================================================ */
const { imageUrls, imageSaving, saveImages: saveImagesRaw } = useImageUpload(currentVersionRef)

async function saveImages() {
  const ok = await saveImagesRaw()
  if (ok) emit('refresh')
}

/* ============================================================
 * txt 上传
 * ============================================================ */
const {
  txtInputRef,
  onTxtUploadClick,
  onTxtFileChange: onTxtFileChangeRaw,
} = useTxtUpload(currentVersionRef)

async function onTxtFileChange(e: Event) {
  const ok = await onTxtFileChangeRaw(e)
  if (ok) emit('refresh')
}
</script>
