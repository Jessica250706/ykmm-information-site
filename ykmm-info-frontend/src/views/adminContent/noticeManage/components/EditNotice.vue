<template>
  <div class="notice-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑公告' : '新增公告' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-4xl" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="form.title"
            maxlength="255"
            placeholder="请输入公告标题"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 240px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="可见性" prop="minVisibility">
          <el-radio-group v-model="form.minVisibility">
            <el-radio v-for="opt in NOTICE_VISIBILITY_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="置顶">
          <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>

        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            :rows="2"
            maxlength="500"
            placeholder="列表展示用，可为空"
            type="textarea"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="封面图">
          <ImageUpload v-model="form.coverImage" :max-size="5" tip="建议 16:9 尺寸" />
        </el-form-item>

        <el-form-item label="发布时间">
          <el-date-picker
            v-model="form.publishAt"
            placeholder="为空则发布时取当前时间"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="过期时间">
          <el-date-picker
            v-model="form.expireAt"
            placeholder="为空则永不过期"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <div style="border: 1px solid var(--el-border-color); width: 100%">
            <Toolbar
              :default-config="toolbarConfig"
              :editor="editorRef"
              mode="default"
              style="border-bottom: 1px solid var(--el-border-color)"
            />
            <Editor
              v-model="form.content"
              :default-config="editorConfig"
              mode="default"
              style="height: 400px; overflow-y: hidden"
              @on-created="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>

      <div class="mt-4 flex justify-center">
        <el-button :loading="loading" type="primary" @click="handleSubmit">保存</el-button>
        <el-button @click="handleBack">取消</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, shallowRef } from 'vue'
import { Editor, Toolbar } from '@wangeditor-next/editor-for-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  createNoticeAPI,
  getAdminNoticeDetailAPI,
  listNoticeCategoriesAPI,
  updateNoticeAPI,
} from '@/api/notice'
import { AdminRouteName, NOTICE_VISIBILITY, NOTICE_VISIBILITY_OPTIONS } from '@/constants'
import type { NoticeCategoryVO, NoticeDTO } from '@/types/notice'
import '@wangeditor-next/editor/dist/css/style.css'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor-next/editor'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

const formRef = ref<FormInstance>()
const loading = ref(false)
const categories = ref<NoticeCategoryVO[]>([])

const form = reactive<NoticeDTO>({
  title: '',
  categoryId: undefined as unknown as number,
  summary: '',
  content: '',
  coverImage: '',
  minVisibility: NOTICE_VISIBILITY.GUEST,
  isTop: 0,
  publishAt: undefined,
  expireAt: undefined,
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  minVisibility: [{ required: true, message: '请选择可见性', trigger: 'change' }],
}

/* -------- 富文本 -------- */
const editorRef = shallowRef<IDomEditor>()
const toolbarConfig: Partial<IToolbarConfig> = {}
const editorConfig: Partial<IEditorConfig> = { placeholder: '请输入公告内容...' }

function handleCreated(editor: IDomEditor) {
  editorRef.value = editor
}

onBeforeUnmount(() => {
  editorRef.value?.destroy()
})

/* -------- 加载 -------- */
async function loadCategories() {
  const res = await listNoticeCategoriesAPI()
  categories.value = res.data ?? []
}

async function loadDetail() {
  if (!editId.value) return
  const res = await getAdminNoticeDetailAPI(editId.value)
  const d = res.data
  if (!d) {
    ElMessage.error('公告不存在')
    handleBack()
    return
  }
  form.title = d.title ?? ''
  form.categoryId = d.categoryId!
  form.summary = d.summary ?? ''
  form.content = d.content ?? ''
  form.coverImage = d.coverImage ?? ''
  form.minVisibility = d.minVisibility ?? NOTICE_VISIBILITY.GUEST
  form.isTop = d.isTop ?? 0
  form.publishAt = d.publishAt
  form.expireAt = d.expireAt
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const payload = { ...form }
    if (isEdit.value && editId.value) {
      await updateNoticeAPI(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createNoticeAPI(payload)
      ElMessage.success('新增成功，公告为草稿状态，需手动发布')
    }
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect) {
    router.push(redirect)
    return
  }
  router.push({ name: AdminRouteName.NOTICE_MANAGE })
}

onMounted(async () => {
  await loadCategories()
  if (isEdit.value) await loadDetail()
})
</script>
