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

    <!-- 未开启编辑模式 -->
    <div v-if="!editingModeLocal" class="flex-1 flex items-center justify-center">
      <el-empty description="开启编辑模式以编辑对话" />
    </div>

    <!-- 无故事 -->
    <div v-else-if="!storyDetail" class="flex-1 flex items-center justify-center">
      <el-empty description="请选择一个剧情" />
    </div>

    <!-- 无版本 -->
    <div v-else-if="!currentVersion" class="flex-1 flex items-center justify-center">
      <el-empty description="请选择一个对话版本" />
    </div>

    <!-- 编辑内容 -->
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

          <el-form label-width="60px" size="default">
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
            <el-button @click="onAdd">+ 新增</el-button>
            <el-button :loading="saving" type="primary" @click="onSave">保存</el-button>
          </div>

          <div v-if="!editingLine" class="mt-2 text-xs text-slate-400">
            点击左侧某一句对话可加载到编辑器；未选中时新增将追加到末尾。
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
import type { DialogueImageDTO } from '@/types/dialogueImage'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import type { RoleGroupVO, RoleVO } from '@/types/role'
import type { StoryDetailVO } from '@/types/story'
import type { UploadFile, UploadRequestOptions, UploadUserFile } from 'element-plus'

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
}>()

const emit = defineEmits<{
  'update:editingMode': [val: boolean]
  'save-line': []
  'add-line': [payload: { afterId: number | null }]
  refresh: []
  /** 请求选中某一句，用于上一条/下一条切换 */
  'select-line': [line: DialogueLineVO]
}>()

const editingModeLocal = useVModel(props, 'editingMode', emit, {
  passive: true,
})

/* ============================================================
 * 角色下拉
 * ============================================================ */
const roleOptions = ref<RoleGroupVO[]>([])

async function loadRoles() {
  try {
    const res = await listGroupedRolesAPI()
    roleOptions.value = res.data ?? []
  } catch {
    // 忽略，用户可在管理端维护
    roleOptions.value = []
  }
}

function roleLabel(r: RoleVO): string {
  if (r.personNameCn) return `${r.name}（${r.personNameCn}）`
  return r.name ?? `角色 #${r.id}`
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
 * 编辑器表单
 * ============================================================ */
const saving = ref(false)
const editorForm = reactive<{
  id: number | null
  speakerId: number | null
  content: string
  side: number | null
}>({
  id: null,
  speakerId: null,
  content: '',
  side: null,
})

/** 编辑对象变化时，同步表单 */
watch(
  () => props.editingLine,
  (line) => {
    if (line) {
      editorForm.id = line.id ?? null
      editorForm.speakerId = line.speakerId ?? null
      editorForm.content = line.content ?? ''
      editorForm.side = line.side ?? null
    } else {
      editorForm.id = null
      editorForm.speakerId = null
      editorForm.content = ''
      editorForm.side = null
    }
  },
  { immediate: true },
)

/* ============================================================
 * 上一句 / 下一句
 * ============================================================ */
const hasPrev = computed(() => props.editingLineIndex > 0)
const hasNext = computed(
  () => props.editingLineIndex >= 0 && props.editingLineIndex < props.allLines.length - 1,
)

function goPrev() {
  if (!hasPrev.value) return
  const prev = props.allLines[props.editingLineIndex - 1]
  if (prev) emit('select-line', prev)
}

function goNext() {
  if (!hasNext.value) return
  const next = props.allLines[props.editingLineIndex + 1]
  if (next) emit('select-line', next)
}

/* ============================================================
 * 保存 / 新增
 * ============================================================ */
async function onSave() {
  if (editorForm.speakerId == null) {
    ElMessage.warning('请选择说话人')
    return
  }
  if (!editorForm.content?.trim()) {
    ElMessage.warning('内容不能为空')
    return
  }
  saving.value = true
  try {
    if (editorForm.id != null) {
      await updateDialogueLineAPI(editorForm.id, {
        id: editorForm.id,
        speakerId: editorForm.speakerId,
        content: editorForm.content,
        side: editorForm.side ?? undefined,
      })
    } else {
      // 新增：先追加到末尾，再重排到目标位置
      await appendAndReorder()
    }
    ElMessage.success('保存成功')
    emit('save-line')
    emit('refresh')
  } catch {
    return
  } finally {
    saving.value = false
  }
}

async function onAdd() {
  if (editorForm.speakerId == null) {
    ElMessage.warning('请选择说话人')
    return
  }
  if (!editorForm.content?.trim()) {
    ElMessage.warning('内容不能为空')
    return
  }
  saving.value = true
  try {
    await appendNewLine()
    ElMessage.success('新增成功')
    // 把目标句 ID 传给父组件，让父组件决定是否重排
    emit('add-line', { afterId: props.editingLine?.id ?? null })
  } catch {
    return
  } finally {
    saving.value = false
  }
}

/**
 * 只追加到末尾，不负责重排
 */
async function appendNewLine() {
  if (!props.currentVersion?.id) {
    throw new Error('版本不存在')
  }
  const versionId = props.currentVersion.id
  await batchSaveDialogueLinesAPI(versionId, [
    {
      speakerId: editorForm.speakerId!,
      content: editorForm.content,
      side: editorForm.side ?? undefined,
    },
  ])
}

/**
 * 新增并重排：
 * 1. 追加到末尾
 * 2. 如果需要插入到中间，刷新后由前端再调 sort 接口重排
 */
async function appendAndReorder() {
  if (!props.currentVersion?.id) {
    throw new Error('版本不存在')
  }
  const versionId = props.currentVersion.id
  const newLine = {
    speakerId: editorForm.speakerId!,
    content: editorForm.content,
    side: editorForm.side ?? undefined,
  }

  // 1. 追加到末尾
  await batchSaveDialogueLinesAPI(versionId, [newLine])

  // 2. 如果是在中间插入，需要在 refresh 后再调 sort
  //    这里记录目标位置，父组件 refresh 完成后回到这里处理
}

/* ============================================================
 * 图片上传
 * ============================================================ */
const imageFileList = ref<UploadUserFile[]>([])
const imageSaving = ref(false)

/** 版本变化时，同步已有图片 */
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
  // 从本地列表移除
  imageFileList.value = imageFileList.value.filter((f) => f.uid !== file.uid)
}

async function saveImages() {
  if (!props.currentVersion?.id) return
  imageSaving.value = true
  try {
    const images: DialogueImageDTO[] = imageFileList.value
      .filter((f) => f.url || f.response?.url)
      .map((f, i) => ({
        url: f.url ?? f.response?.url,
        sort: i + 1,
      }))
    await batchSaveDialogueImagesAPI(props.currentVersion.id, images)
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
  if (!props.currentVersion?.id) return

  try {
    const parseRes = await parseDialogueTxtAPI(props.currentVersion.id, file)
    const parsed = parseRes.data
    if (!parsed?.lines?.length) {
      ElMessage.warning('未解析到有效内容')
      return
    }
    // 展示简要信息
    if (parsed.unmatchedSpeakers?.length) {
      await ElMessageBox.confirm(
        `以下说话人未匹配到角色：\n${parsed.unmatchedSpeakers.join('、')}\n\n仍要导入吗？`,
        '提示',
        { type: 'warning' },
      )
    }
    await importDialogueTxtAPI(props.currentVersion.id, parsed.lines)
    ElMessage.success('导入成功')
    emit('refresh')
  } catch {
    return
  } finally {
    target.value = ''
  }
}
</script>
