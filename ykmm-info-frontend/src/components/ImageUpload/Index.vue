<template>
  <div :class="{ 'image-upload--single': !multiple }" class="image-upload">
    <el-upload
      v-model:file-list="fileList"
      :before-upload="beforeUpload"
      :class="{ 'hide-trigger': fileList.length >= limit }"
      :http-request="handleUpload"
      :limit="limit"
      :multiple="multiple"
      :on-error="handleError"
      :on-exceed="handleExceed"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      accept="image/*"
      list-type="picture-card"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>

    <div v-if="tip" class="mt-1 text-xs text-slate-400">{{ tip }}</div>

    <!-- 图片预览：全屏查看器，支持左右切换 / 缩放 / 旋转 -->
    <el-image-viewer
      v-if="viewerVisible"
      :initial-index="previewIndex"
      :url-list="previewList"
      hide-on-click-modal
      lazy
      teleported
      @close="handleViewerClose"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import {
  ElImageViewer,
  ElMessage,
  type UploadFile,
  type UploadRawFile,
  type UploadRequestOptions,
  type UploadUserFile,
} from 'element-plus'
import { uploadFileAPI } from '@/api/common'

interface Props {
  /** 单张时 string，多张时 string[] */
  modelValue?: string | string[]
  /** 是否多图 */
  multiple?: boolean
  /** 多图时的最大数量 */
  maxCount?: number
  /** 大小限制（MB） */
  maxSize?: number
  /** 提示文案 */
  tip?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  multiple: false,
  maxCount: 10,
  maxSize: 5,
  tip: '',
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: string | string[]): void
}>()

const fileList = ref<UploadUserFile[]>([])

const limit = computed(() => (props.multiple ? props.maxCount : 1))

/** 把 modelValue 统一成 string[] */
function toArray(val: string | string[] | undefined): string[] {
  if (props.multiple) return Array.isArray(val) ? val : []
  return typeof val === 'string' && val ? [val] : []
}

/** 生成不冲突的 uid（负数，避免和 el-upload 内部 uid 重叠） */
let uidSeed = Date.now()
function genUid() {
  return -++uidSeed
}

/** 是否有文件处于上传中（ready / uploading） */
function hasUploading() {
  return fileList.value.some((f) => f.status === 'ready' || f.status === 'uploading')
}

/* ============================================================
 * 图片预览
 *   - viewerVisible 控制查看器显隐
 *   - previewIndex 当前点击的图片在 previewList 中的索引
 *   - previewList 所有「已成功上传」的图片 URL，保证左右切换顺序与列表一致
 * ============================================================ */
const viewerVisible = ref(false)
const previewIndex = ref(0)

const previewList = computed(() =>
  fileList.value.filter((f) => f.status === 'success' && f.url).map((f) => f.url as string),
)

function handlePreview(file: UploadFile) {
  if (!file.url) return

  // 用 uid 匹配，避免同名 / 同 URL 造成索引错位
  const list = fileList.value.filter((f) => f.status === 'success' && f.url)
  const idx = list.findIndex((f) => f.uid === file.uid)
  if (idx < 0) return

  previewIndex.value = idx
  viewerVisible.value = true
}

function handleViewerClose() {
  viewerVisible.value = false
  // 关闭预览后，主动移除缩略图上的焦点，避免“按Delete键”提示残留
  if (document.activeElement instanceof HTMLElement) {
    document.activeElement.blur()
  }
}

/* ============================================================
 * 方向一：外部 modelValue → fileList
 *   - 仅做「外部替换」：编辑页初次加载、父组件主动覆盖
 *   - 上传过程中直接跳过，避免打断并发上传
 * ============================================================ */
watch(
  () => props.modelValue,
  (val) => {
    // ★ 上传中不同步，避免重建把正在上传的文件丢弃 / 重复
    if (hasUploading()) return

    const urls = toArray(val)
    const currentUrls = fileList.value.filter((f) => f.status === 'success').map((f) => f.url ?? '')

    // 内容一致，跳过（避免循环）
    if (urls.join('|') === currentUrls.join('|')) return

    fileList.value = urls.map((url, i) => ({
      name: `image-${i + 1}.png`,
      url,
      uid: genUid(),
      status: 'success' as const,
    }))
  },
  { immediate: true },
)

/* ============================================================
 * 方向二：fileList → 外部 modelValue
 *   - 唯一 emit 出口
 *   - 过滤条件与上面保持一致：status === 'success' && url
 * ============================================================ */
watch(
  fileList,
  (files) => {
    const urls = files.filter((f) => f.status === 'success' && f.url).map((f) => f.url as string)

    const current = toArray(props.modelValue)
    if (urls.join('|') === current.join('|')) return

    if (props.multiple) {
      emit('update:modelValue', urls)
    } else {
      emit('update:modelValue', urls[0] ?? '')
    }
  },
  { deep: true },
)

/** 上传前校验 */
function beforeUpload(file: UploadRawFile) {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (file.size / 1024 / 1024 >= props.maxSize) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB`)
    return false
  }
  return true
}

/** 自定义上传 */
async function handleUpload(options: UploadRequestOptions) {
  try {
    const res = await uploadFileAPI(options.file as File)
    const url = res.data

    // 直接改 fileList 中对应元素，deep watch 会自动 emit 到 modelValue
    const file = fileList.value.find((f) => f.uid === options.file.uid)
    if (file) {
      file.url = url
      file.status = 'success'
    }

    // 通知 el-upload 内部状态同步
    options.onSuccess?.(url)
  } catch {
    // 错误已由 el-upload 内部标记 status = 'fail'
  }
}

function handleError() {
  // 错误已在 handleUpload 内处理
}

/** 删除：fileList 变化由 watch(fileList) 自动同步到 modelValue */
function handleRemove() {
  // 无需手动 sync
}

/** 超出 limit 时提示 */
function handleExceed() {
  const remaining = limit.value - fileList.value.length
  ElMessage.warning(
    remaining > 0 ? `最多还能再上传 ${remaining} 张图片` : `最多只能上传 ${limit.value} 张图片`,
  )
}
</script>

<style lang="scss" scoped>
.image-upload {
  :deep(.el-upload--picture-card),
  :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 100px;
    height: 100px;
    line-height: 100px;
  }
}

/* 单图模式：大一点 */
.image-upload--single {
  :deep(.el-upload--picture-card),
  :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 120px;
    height: 120px;
    line-height: 120px;
  }
}

/* 达到上限时隐藏「+」触发器 */
.hide-trigger {
  :deep(.el-upload--picture-card) {
    display: none;
  }
}
</style>
