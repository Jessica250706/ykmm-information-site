<template>
  <div class="rich-editor">
    <Toolbar
      :default-config="toolbarConfig"
      :editor="editorRef"
      class="rich-editor__toolbar"
      mode="default"
    />
    <Editor
      v-model="valueHtml"
      :default-config="editorConfig"
      :style="{ height: computedHeight }"
      class="rich-editor__editor"
      mode="default"
      @on-created="handleCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor-next/editor-for-vue'
import { ElMessage } from 'element-plus'
import { uploadFileAPI } from '@/api/common'
import '@wangeditor-next/editor/dist/css/style.css'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor-next/editor'

/* -------- Props -------- */
interface Props {
  /** v-model：HTML 字符串 */
  modelValue?: string
  /** 占位文案，默认「请输入内容...」 */
  placeholder?: string
  /** 编辑器高度，默认 400px；支持 '60vh' 之类字符串 */
  height?: string | number
  /** 只读模式（支持动态切换） */
  readonly?: boolean
  /** 最大字符数（不传则不限制） */
  maxLength?: number
  /**
   * 自定义上传函数。
   * 默认使用全局 uploadFileAPI（POST /common/upload）。
   * 传入此函数可覆盖，适用于「上传到不同目录」「上传到不同 OSS 桶」等场景。
   * 函数应返回图片的可访问 URL。
   */
  uploadFn?: (file: File) => Promise<string>
  /** 单张图片最大大小（MB），默认 5 */
  maxImageSizeMB?: number
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '请输入内容...',
  height: '400px',
  readonly: false,
  maxImageSizeMB: 5,
})

const emit = defineEmits<{
  'update:modelValue': [val: string]
  /** 编辑器实例创建完成，父组件可拿到 editor 实例做进一步定制 */
  created: [editor: IDomEditor]
  /** 内容变化（与 update:modelValue 同步触发，方便只关心 onChange 的场景） */
  change: [val: string]
}>()

/* -------- 高度（number → px） -------- */
const computedHeight = computed(() =>
  typeof props.height === 'number' ? `${props.height}px` : props.height,
)

/* -------- 双向绑定（手写，避免 useVModel 的循环触发） -------- */
const valueHtml = ref(props.modelValue ?? '')

watch(
  () => props.modelValue,
  (val) => {
    if (val !== valueHtml.value) {
      valueHtml.value = val ?? ''
    }
  },
)

watch(valueHtml, (val) => {
  if (val !== props.modelValue) {
    emit('update:modelValue', val)
    emit('change', val)
  }
})

/* -------- 编辑器实例 -------- */
const editorRef = shallowRef<IDomEditor>()

function handleCreated(editor: IDomEditor) {
  editorRef.value = editor
  // 初始只读状态需要主动同步一次（editorConfig.readOnly 只在创建时读取）
  if (props.readonly) editor.disable()
  emit('created', editor)
}

onBeforeUnmount(() => {
  editorRef.value?.destroy()
})

/* -------- 只读状态动态切换 -------- */
watch(
  () => props.readonly,
  (val) => {
    const editor = editorRef.value
    if (!editor) return
    if (val) editor.disable()
    else editor.enable()
  },
)

/* -------- 上传函数解析 -------- */
/**
 * 每次上传都动态读取，保证 props.uploadFn 变化后立即生效。
 */
async function doUpload(file: File): Promise<string> {
  if (props.uploadFn) return props.uploadFn(file)
  const res = await uploadFileAPI(file)
  return res.data ?? ''
}

/* -------- base64 → File -------- */
/**
 * base64 dataURL → File 对象。
 * 用于 customPaste 中把 HTML 里的 base64 图片转成可上传的文件。
 */
function base64ToFile(base64: string, filename: string): File {
  const parts = base64.split(',')
  const meta = parts[0]
  const data = parts[1]
  if (!meta || !data) {
    throw new Error('无效的 base64 dataURL 格式')
  }
  const mime = /data:([^;]+);/.exec(meta)?.[1] ?? 'image/png'
  const binary = atob(data)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i)
  }
  return new File([bytes], filename, { type: mime })
}

/* -------- 工具栏 / 编辑器配置 -------- */
const toolbarConfig: Partial<IToolbarConfig> = {}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: props.placeholder,
  readOnly: props.readonly,
  maxLength: props.maxLength,

  /**
   * 自定义粘贴（同步决策 + 异步处理）：
   * 1. 有 file（Ctrl+V 截图）→ 返回 true，交给 uploadImage.customUpload
   * 2. 无 HTML 或 HTML 无 base64 图片（纯文本 / 普通富文本）→ 返回 true，走默认逻辑
   * 3. 含 base64 图片（网页/Word 复制）→ 返回 false 阻止默认，异步上传后手动插入
   */
  customPaste: (editor, event) => {
    const clipboard = event.clipboardData
    if (!clipboard) return true

    if (clipboard.files && clipboard.files.length > 0) {
      return true
    }

    const html = clipboard.getData('text/html')
    if (!html || !html.includes('data:image/')) {
      return true
    }

    void (async () => {
      const regex = /src="(data:image\/[^;]+;base64,[^"]+)"/g
      const matches = [...html.matchAll(regex)]
      if (matches.length === 0) {
        editor.dangerouslyInsertHtml(html)
        return
      }

      let newHtml = html
      for (const match of matches) {
        const base64 = match[1]
        if (!base64) continue
        try {
          const file = base64ToFile(
            base64,
            `paste-${Date.now()}-${Math.random().toString(36).slice(2, 8)}.png`,
          )
          const url = await doUpload(file)
          if (url) {
            newHtml = newHtml.split(base64).join(url)
          }
        } catch (err) {
          console.error('[RichEditor] base64 图片上传失败', err)
        }
      }
      editor.dangerouslyInsertHtml(newHtml)
    })()

    return false
  },

  MENU_CONF: {
    uploadImage: {
      allowedFileTypes: ['image/*'],
      maxFileSize: props.maxImageSizeMB * 1024 * 1024,
      async customUpload(file, insertFn) {
        try {
          const url = await doUpload(file)
          if (!url) {
            ElMessage.error('图片上传失败：服务端未返回 URL')
            return
          }
          insertFn(url, undefined, file.name, '')
        } catch (err) {
          console.error('[RichEditor] 图片上传失败', err)
          ElMessage.error('图片上传失败，请稍后重试')
        }
      },
    },
  },
}

/* -------- 暴露方法 -------- */
defineExpose({
  /** 获取纯文本内容 */
  getText: () => editorRef.value?.getText() ?? '',
  /** 获取 HTML（与 v-model 一致，但可在需要"即时读"时使用） */
  getHtml: () => editorRef.value?.getHtml() ?? '',
  /** 清空内容 */
  clear: () => editorRef.value?.clear(),
  /** 聚焦 */
  focus: () => editorRef.value?.focus(),
  /** 失焦 */
  blur: () => editorRef.value?.blur(),
  /** 手动设置 HTML（v-model 未更新的场景） */
  setHtml: (html: string) => editorRef.value?.setHtml(html),
  /** 底层编辑器实例，用于自定义扩展 */
  getEditor: () => editorRef.value,
})
</script>

<style lang="scss" scoped>
.rich-editor {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
  width: 100%;

  &__toolbar {
    border-bottom: 1px solid var(--el-border-color);
  }

  &__editor {
    overflow-y: hidden;
  }
}
</style>
