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
            {{ creatingAfterId != null ? '将在当前句之后插入新对话' : '将追加到末尾' }}
          </el-alert>

          <el-form label-width="70px" size="default">
            <el-form-item label="说话人">
              <el-select
                v-model="editorForm.speakerId"
                class="w-full"
                placeholder="请选择角色"
                filterable
              >
                <el-option-group
                  v-for="g in roleOptions"
                  :key="g.personId ?? 'other'"
                  :label="g.personName"
                >
                  <el-option v-for="r in g.roles" :key="r.id" :label="r.name" :value="r.id!" />
                </el-option-group>
              </el-select>
            </el-form-item>
            <el-form-item label="内心独白">
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
            <div>
              <el-button v-if="editorMode === 'edit'" @click="onAdd">+ 新增</el-button>
              <el-button v-else @click="onCancelCreate">取消新增</el-button>
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
            填写完成后点击“创建”，将作为新对话插入。
          </div>
        </div>
      </template>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useVModel } from '@vueuse/core'
import { MONOLOGUE } from '@/constants/index'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO, DialogueVersionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'
import { useEditorActions } from './composables/useEditorActions'
import { useEditorForm } from './composables/useEditorForm'
import { useImageUpload } from './composables/useImageUpload'
import { useRoleOptions } from './composables/useRoleOptions'
import { useTxtUpload } from './composables/useTxtUpload'

/* ============================================================
 * Props / Emits
 * ============================================================ */

const props = defineProps<{
  editingMode: boolean
  storyDetail: StoryDetailVO | null
  currentVersion: DialogueVersionVO | null
  editingLine: DialogueLineVO | null
  editingLineIndex: number
  allLines: DialogueLineVO[]
  pendingInsertAfterId: number | null
  currentOptionVersion: DialogueVersionOptionVO | null
}>()

const emit = defineEmits<{
  'update:editingMode': [val: boolean]
  'save-line': []
  'add-line': [payload: { afterId: number | null }]
  refresh: []
  'select-line': [line: DialogueLineVO]
  'create-version': [option: DialogueVersionOptionVO]
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

/* ============================================================
 * 空版本分支
 * ============================================================ */
const emptyVersionText = computed(() => {
  const label = props.currentOptionVersion?.label ?? '该版本'
  return `「${label}」尚未创建内容`
})

const creatingVersion = ref(false)

function onCreateVersion() {
  const option = props.currentOptionVersion
  if (!option) return
  emit('create-version', option)
}

/* ============================================================
 * 角色下拉
 * ============================================================ */
const { roleOptions } = useRoleOptions(editingModeLocal)

/* ============================================================
 * 编辑器表单
 * ============================================================ */
const { editorMode, creatingAfterId, dirty, editorForm, enterCreateMode, exitCreateMode } =
  useEditorForm(editingLineRef)

/* ============================================================
 * 编辑器动作
 * ============================================================ */
const { saving, hasPrev, hasNext, onAdd, onCancelCreate, onSave, goPrev, goNext } =
  useEditorActions({
    editorForm,
    editorMode,
    creatingAfterId,
    dirty,
    currentVersion: currentVersionRef,
    editingLine: editingLineRef,
    editingLineIndex: editingLineIndexRef,
    allLines: allLinesRef,
    onSelectLine: (line) => emit('select-line', line),
    onSaveDone: () => emit('save-line'),
    onAddDone: (afterId) => emit('add-line', { afterId }),
    enterCreateMode,
    exitCreateMode,
  })

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
