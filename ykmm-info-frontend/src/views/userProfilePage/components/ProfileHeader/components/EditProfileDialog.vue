<template>
  <el-dialog
    v-model="visible"
    :close-on-click-modal="false"
    title="修改个人信息"
    width="480px"
    @closed="handleClosed"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="72px">
      <el-form-item label="头像">
        <ImageUpload v-model="form.avatar" :max-size="2" tip="建议 200x200 尺寸，2MB 以内" />
      </el-form-item>

      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="form.nickname"
          maxlength="32"
          placeholder="请输入昵称"
          clearable
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitting" type="primary" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { editUserAPI } from '@/api/user'
import { useUserStore } from '@/stores'
import type { UserInfo } from '@/types/user'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()

const visible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<{
  avatar: string
  nickname: string
}>({
  avatar: '',
  nickname: '',
})

const rules: FormRules = {
  nickname: [{ max: 32, message: '昵称最多 32 个字符', trigger: 'blur' }],
}

/** 打开弹窗，可选传入初始数据 */
function open(target?: UserInfo | null) {
  const src = target ?? userStore.userInfo
  form.avatar = src?.avatar ?? ''
  form.nickname = src?.nickname ?? ''
  visible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const uid = userStore.userInfo?.id
  if (!uid) {
    ElMessage.error('用户信息缺失，无法保存')
    return
  }

  submitting.value = true
  try {
    const res = await editUserAPI(uid, {
      avatar: form.avatar || undefined,
      nickname: form.nickname || undefined,
    })
    if (res.data) userStore.setUserInfo(res.data)
    ElMessage.success('保存成功')
    visible.value = false
  } finally {
    submitting.value = false
  }
}

function handleClosed() {
  formRef.value?.resetFields()
}

defineExpose({ open })
</script>
