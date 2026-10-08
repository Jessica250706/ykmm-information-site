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
        <el-upload
          v-model:file-list="imageFileList"
          :class="'w-full'"
          :http-request="handleImageUpload"
          :on-remove="handleImageRemove"
          accept="image/*"
          list-type="picture-card"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div class="mt-2 flex justify-end">
          <el-button :loading="imageSaving" type="primary" @click="saveImages">保存图片</el-button>
        </div>
      </template>

      <!-- 文字版本 -->
      <template v-else-if="currentVersion.format === 1">
        <!-- txt 上传 -->
        <div class="mb-4">
          <el-button class="w-full" type="warning" plain @click="onTxtUploadClick">
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
            填写完成后点击"创建"，将作为新对话插入。
          </div>
        </div>
      </template>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { useVModel } from '@vueuse/core'
import { ElMessage, ElMessageBox } from 'element-plus'
import { uploadFileAPI } from '@/api/common'
import { batchSaveDialogueImagesAPI } from '@/api/dialogueImage'
import { batchSaveDialogueLinesAPI, updateDialogueLineAPI } from '@/api/dialogueLine'
import { importDialogueTxtAPI, parseDialogueTxtAPI } from '@/api/dialogueTxt'
import { listGroupedRolesAPI } from '@/api/role'
import { MONOLOGUE } from '@/constants'
import type { DialogueImageDTO } from '@/types/dialogueImage'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO, DialogueVersionVO } from '@/types/dialogueVersion'
import type { RoleGroupVO } from '@/types/role'
import type { StoryDetailVO } from '@/types/story'
import type { UploadFile, UploadRequestOptions, UploadUserFile } from 'element-plus'

/* ============================================================
 * Props / Emits
 * ============================================================ */

const props = defineProps<{
  /** 是否处于编辑模式（v-model） */
  editingMode: boolean
  /** 当前剧情详情 */
  storyDetail: StoryDetailVO | null
  /** 当前选中的版本 */
  currentVersion: DialogueVersionVO | null
  /** 当前编辑的句子 */
  editingLine: DialogueLineVO | null
  /** 当前编辑句子的下标 */
  editingLineIndex: number
  /** 当前版本的全部句子，用于上下切换 */
  allLines: DialogueLineVO[]
  /** 新增句子时要插到哪一句后面，null 表示追加到末尾 */
  pendingInsertAfterId: number | null
  /** 当前选中的版本 option；可能 versionId 为 null（尚未创建内容） */
  currentOptionVersion: DialogueVersionOptionVO | null
}>()

const emit = defineEmits<{
  'update:editingMode': [val: boolean]
  'save-line': []
  'add-line': [payload: { afterId: number | null }]
  refresh: []
  /** 请求选中某一句，用于上一条/下一条切换 */
  'select-line': [line: DialogueLineVO]
  /** 请求为当前选中的、尚未创建的版本创建内容 */
  'create-version': [option: DialogueVersionOptionVO]
}>()

const editingModeLocal = useVModel(props, 'editingMode', emit, {
  passive: true,
})

/* ============================================================
 * 空版本分支
 * ============================================================ */

const emptyVersionText = computed(() => {
  const label = props.currentOptionVersion?.label ?? '该版本'
  return `「${label}」尚未创建内容`
})

/** 创建版本内容中，防止重复点击 */
const creatingVersion = ref(false)

function onCreateVersion() {
  const option = props.currentOptionVersion
  if (!option) return
  emit('create-version', option)
}

/* ============================================================
 * 角色下拉
 * ============================================================ */
const roleOptions = ref<RoleGroupVO[]>([])

async function loadRoles() {
  try {
    const res = await listGroupedRolesAPI()
    roleOptions.value = res.data ?? []
  } catch {
    roleOptions.value = []
  }
}

watch(
  () => props.editingMode,
  (val) => {
    if (val && roleOptions.value.length === 0) {
      void loadRoles()
    }
  },
  { immediate: true },
)

/* ============================================================
 * 编辑器：模式 + 表单
 * ============================================================ */

type EditorMode = 'edit' | 'create'

/** 当前编辑器模式：编辑已有行 / 新建 */
const editorMode = ref<EditorMode>('edit')

/** 新建模式下插入到哪个句子的 id 之后；null 表示追加到末尾 */
const creatingAfterId = ref<number | null>(null)

/** 是否有未保存的修改 */
const dirty = ref(false)

const saving = ref(false)

const editorForm = reactive<{
  id: number | null
  speakerId: number | null
  content: string
  side: number | null
  /** 是否内心独白：0否 1是 */
  monologue: number
}>({
  id: null,
  speakerId: null,
  content: '',
  side: null,
  monologue: MONOLOGUE.SPOKEN,
})

/** 用一行数据填充表单；传 null 则清空 */
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

function clearForm() {
  fillForm(null)
}

/**
 * 外部选中行变化（点击左侧某句 / 上下切换 / 父级自动选中）时同步表单。
 * 若处于新建模式且外部切换了选中行，则退出新建模式。
 */
watch(
  () => props.editingLine?.id,
  (newId, oldId) => {
    if (newId === oldId) return

    if (editorMode.value === 'create') {
      editorMode.value = 'edit'
      creatingAfterId.value = null
    }

    fillForm(props.editingLine)
    dirty.value = false
  },
  { immediate: true },
)

/** 跟踪 dirty：编辑器内容与原始行不一致时置 true */
watch(
  () => [editorForm.speakerId, editorForm.content, editorForm.monologue],
  () => {
    if (editorMode.value === 'create') {
      dirty.value = !!editorForm.content.trim() || editorForm.speakerId != null
      return
    }
    const orig = props.editingLine
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

/* ============================================================
 * 上一句 / 下一句
 * ============================================================ */

const hasPrev = computed(() => {
  if (editorMode.value === 'create') return true
  return props.editingLineIndex > 0
})

const hasNext = computed(() => {
  if (editorMode.value === 'create') return true
  return props.editingLineIndex >= 0 && props.editingLineIndex < props.allLines.length - 1
})

async function goPrev() {
  if (editorMode.value === 'create') {
    if (dirty.value) {
      // 有内容：保存新行，父级刷新后会选中新行；不继续切换
      await onSave()
      return
    }
    // 无内容：取消新建
    cancelCreate()
  }

  if (props.editingLineIndex <= 0) return
  const prev = props.allLines[props.editingLineIndex - 1]
  if (!prev) return

  if (dirty.value) {
    const ok = await onSave()
    if (!ok) return
  }

  emit('select-line', prev)
}

async function goNext() {
  if (editorMode.value === 'create') {
    if (dirty.value) {
      await onSave()
      return
    }
    cancelCreate()
  }

  if (props.editingLineIndex < 0 || props.editingLineIndex >= props.allLines.length - 1) return
  const next = props.allLines[props.editingLineIndex + 1]
  if (!next) return

  if (dirty.value) {
    const ok = await onSave()
    if (!ok) return
  }

  emit('select-line', next)
}

/* ============================================================
 * 新增 / 取消 / 保存
 * ============================================================ */

/** 进入新建模式 */
function onAdd() {
  if (editorMode.value === 'create') return
  editorMode.value = 'create'
  creatingAfterId.value = props.editingLine?.id ?? null
  clearForm()
  dirty.value = false
}

/** 退出新建模式，恢复编辑器为当前选中行 */
function cancelCreate() {
  editorMode.value = 'edit'
  creatingAfterId.value = null
  fillForm(props.editingLine)
  dirty.value = false
}

function onCancelCreate() {
  cancelCreate()
}

async function onSave(): Promise<boolean> {
  if (editorForm.speakerId == null) {
    ElMessage.warning('请选择说话人')
    return false
  }
  if (!editorForm.content?.trim()) {
    ElMessage.warning('内容不能为空')
    return false
  }

  saving.value = true
  try {
    if (editorMode.value === 'create') {
      return await saveCreate()
    }
    return await saveEdit()
  } finally {
    saving.value = false
  }
}

/** 更新已有行 */
async function saveEdit(): Promise<boolean> {
  if (editorForm.id == null) {
    ElMessage.warning('无效的编辑行')
    return false
  }
  try {
    await updateDialogueLineAPI(editorForm.id, {
      id: editorForm.id,
      speakerId: editorForm.speakerId!,
      content: editorForm.content,
      side: editorForm.side ?? undefined,
      monologue: editorForm.monologue,
    })
    ElMessage.success('保存成功')
    dirty.value = false
    emit('save-line')
    return true
  } catch (err) {
    console.error('保存失败', err)
    return false
  }
}

/** 创建新行 */
async function saveCreate(): Promise<boolean> {
  const versionId = props.currentVersion?.id
  if (!versionId) {
    ElMessage.error('版本不存在')
    return false
  }

  const afterId = creatingAfterId.value

  try {
    await batchSaveDialogueLinesAPI(versionId, [
      {
        speakerId: editorForm.speakerId!,
        content: editorForm.content,
        side: editorForm.side ?? undefined,
        monologue: editorForm.monologue,
      },
    ])
    ElMessage.success('新增成功')
    dirty.value = false
    editorMode.value = 'edit'
    creatingAfterId.value = null
    emit('add-line', { afterId })
    return true
  } catch (err) {
    console.error('新增失败', err)
    return false
  }
}

/* ============================================================
 * 图片上传
 * ============================================================ */
const imageFileList = ref<UploadUserFile[]>([])
const imageSaving = ref(false)

watch(
  () => [props.currentVersion?.id, props.currentVersion?.images],
  () => {
    const imgs = props.currentVersion?.images ?? []
    imageFileList.value = imgs.map((img) => ({
      name: `image-${img.id}`,
      url: img.url,
      uid: img.id,
      status: 'success',
    }))
  },
  { immediate: true },
)

async function handleImageUpload(options: UploadRequestOptions) {
  const { file } = options
  const res = await uploadFileAPI(file)
  return { url: res.data }
}

function handleImageRemove(file: UploadFile) {
  imageFileList.value = imageFileList.value.filter((f) => f.uid !== file.uid)
}

async function saveImages() {
  const versionId = props.currentVersion?.id
  if (!versionId) return
  imageSaving.value = true
  try {
    const images: DialogueImageDTO[] = imageFileList.value
      .filter((f) => f.url)
      .map((f, i) => ({
        url: f.url!,
        sort: i + 1,
      }))
    await batchSaveDialogueImagesAPI(versionId, images)
    ElMessage.success('图片保存成功')
    emit('refresh')
  } catch {
    return
  } finally {
    imageSaving.value = false
  }
}

/* ============================================================
 * txt 上传
 * ============================================================ */
const txtInputRef = ref<HTMLInputElement | null>(null)

async function onTxtUploadClick() {
  try {
    await ElMessageBox.confirm(
      '如果上传 txt，会自动覆盖当前故事的所有对话信息。是否确认上传？',
      '确认上传',
      { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' },
    )
    txtInputRef.value?.click()
  } catch {
    // 用户取消
  }
}

async function onTxtFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  const versionId = props.currentVersion?.id
  if (!versionId) return

  try {
    const parseRes = await parseDialogueTxtAPI(versionId, file)
    const parsed = parseRes.data
    if (!parsed?.lines?.length) {
      ElMessage.warning('未解析到有效内容')
      return
    }
    if (parsed.unmatchedSpeakers?.length) {
      await ElMessageBox.confirm(
        `以下说话人未匹配到角色：\n${parsed.unmatchedSpeakers.join('、')}\n\n仍要导入吗？`,
        '提示',
        { type: 'warning' },
      )
    }
    await importDialogueTxtAPI(versionId, parsed.lines)
    ElMessage.success('导入成功')
    emit('refresh')
  } catch {
    return
  } finally {
    target.value = ''
  }
}
</script>
