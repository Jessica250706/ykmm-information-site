<template>
  <el-dialog
    :model-value="modelValue"
    title="编辑用户"
    width="480px"
    @update:model-value="handleVisibleChange"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="72px">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="头像" prop="avatar">
        <el-input v-model="form.avatar" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button :loading="loading" type="primary" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { editUserAPI } from '@/api/user'
import type { UserEditDTO, UserInfo } from '@/types/user'
import type { FormInstance, FormRules } from 'element-plus'

const props = defineProps<{
  modelValue: boolean
  user: UserInfo | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<UserEditDTO>({ nickname: '', email: '', avatar: '' })

const rules: FormRules<UserEditDTO> = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
}

watch(
  () => props.user,
  (u) => {
    if (u) {
      form.nickname = u.nickname ?? ''
      form.email = u.email ?? ''
      form.avatar = u.avatar ?? ''
    }
  },
  { immediate: true },
)

function handleVisibleChange(v: boolean) {
  emit('update:modelValue', v)
}

async function handleSubmit() {
  if (!formRef.value || !props.user?.id) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await editUserAPI(props.user.id, { ...form })
    ElMessage.success('保存成功')
    emit('update:modelValue', false)
    emit('success')
  } finally {
    loading.value = false
  }
}
</script>
