<template>
  <div :class="{ 'image-upload--single': !multiple }" class="image-upload">
    <el-upload
      v-model:file-list="fileList"
      :before-upload="beforeUpload"
      :class="{ 'hide-trigger': fileList.length >= limit }"
      :http-request="handleUpload"
      :limit="limit"
      :on-error="handleError"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      accept="image/*"
      list-type="picture-card"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>

    <div v-if="tip" class="mt-1 text-xs text-slate-400">{{ tip }}</div>

    <!-- 预览弹窗 -->
    <el-dialog v-model="previewVisible" width="600px" append-to-body>
      <img :src="previewUrl" alt="preview" class="w-full" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import {
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
const previewVisible = ref(false)
const previewUrl = ref('')

const limit = computed(() => (props.multiple ? props.maxCount : 1))

/** 把 modelValue 统一成 string[] */
function toArray(val: string | string[] | undefined): string[] {
  if (props.multiple) return Array.isArray(val) ? val : []
  return typeof val === 'string' && val ? [val] : []
}

/** 外部 modelValue → fileList */
watch(
  () => props.modelValue,
  (val) => {
    const urls = toArray(val)
    const currentUrls = fileList.value.filter((f) => f.status === 'success').map((f) => f.url ?? '')

    // 内容相同，跳过（避免循环更新）
    if (urls.join('|') === currentUrls.join('|')) return

    // 保留正在上传中的文件，重建已完成的
    const uploading = fileList.value.filter((f) => f.status !== 'success')
    fileList.value = [
      ...urls.map((url, i) => ({
        name: `image-${i + 1}.png`,
        url,
        uid: -(Date.now() + i),
        status: 'success' as const,
      })),
      ...uploading,
    ]
  },
  { immediate: true },
)

/** fileList → 外部 modelValue */
function syncToModel() {
  const urls = fileList.value
    .filter((f) => f.url && f.status !== 'fail')
    .map((f) => f.url as string)

  if (props.multiple) {
    emit('update:modelValue', urls)
  } else {
    emit('update:modelValue', urls[0] ?? '')
  }
}

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

/** 自定义上传：调用后端接口 */
async function handleUpload(options: UploadRequestOptions) {
  try {
    const res = await uploadFileAPI(options.file as File)
    const url = res.data

    // 1. 找到 fileList 里对应的文件，写回 url 和 status
    const file = fileList.value.find((f) => f.uid === options.file.uid)
    if (file) {
      file.url = url
      file.status = 'success'
    }

    // 2. 通知 element-plus：上传成功（触发内部状态同步）
    options.onSuccess?.(url)

    // 3. 等一个 tick，确保 fileList 已经同步，再 emit
    await nextTick()
    syncToModel()
  } catch (err) {
    options.onError?.(err as Error)
    ElMessage.error('上传失败')
  }
}

/** 上传成功：回写 URL 并同步 v-model */
function handleSuccess(response: unknown, uploadFile: UploadFile) {
  uploadFile.url = response as string
  syncToModel()
}

function handleError() {
  // 错误已在 handleUpload 内提示
}

/** 删除文件 */
function handleRemove() {
  syncToModel()
}

/** 点击预览 */
function handlePreview(file: UploadFile) {
  if (!file.url) return
  previewUrl.value = file.url
  previewVisible.value = true
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
