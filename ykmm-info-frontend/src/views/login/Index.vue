<template>
  <div class="grid min-h-screen place-items-center bg-slate-100 p-4">
    <el-card class="w-full max-w-90 rounded-2xl" shadow="never">
      <template #header>
        <div class="text-center">
          <h1 class="text-xl font-semibold text-slate-800">登录</h1>
          <p class="mt-1 text-sm text-slate-500">欢迎回来，请登录你的账号</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @keyup.enter="handleSubmit"
      >
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model.trim="form.email"
            :prefix-icon="Message"
            autocomplete="email"
            placeholder="请输入邮箱"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            :prefix-icon="Lock"
            autocomplete="current-password"
            placeholder="请输入密码"
            type="password"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button :loading="loading" class="w-full" type="primary" @click="handleSubmit">
            {{ loading ? '登录中…' : '登 录' }}
          </el-button>
        </el-form-item>

        <div class="flex items-center justify-between text-sm">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-link :underline="false" type="primary">忘记密码？</el-link>
        </div>

        <div class="mt-4 flex items-center justify-center text-sm text-slate-500">
          <span>还没有账号？</span>
          <el-link :underline="false" class="ml-1" type="primary" @click="goRegister">
            立即注册
          </el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Lock, Message } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { ROLE } from '@/constants/index'
import { useUserStore } from '@/stores/userStore'
import type { LoginRequest } from '@/types/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()

const form = reactive<LoginRequest>({
  email: '',
  password: '',
})

const rememberMe = ref(false)
const loading = ref(false)

const EMAIL_REGEX = /^[\w.-]+@[\w-]+(\.[\w-]+)+$/

const rules: FormRules<LoginRequest> = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { pattern: EMAIL_REGEX, message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度为 6-64 个字符', trigger: 'blur' },
  ],
}

const REMEMBER_KEY = 'app-remember-email'

async function handleSubmit() {
  if (!formRef.value || loading.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const { user } = await userStore.login({ ...form })

    if (rememberMe.value) {
      localStorage.setItem(REMEMBER_KEY, form.email ?? '')
    } else {
      localStorage.removeItem(REMEMBER_KEY)
    }

    ElMessage.success('登录成功')

    const redirect = route.query.redirect as string | undefined
    if (redirect) {
      await router.replace(redirect)
    } else {
      await router.replace(user?.role === ROLE.ADMIN ? '/admin' : '/cards')
    }
  } catch (err: unknown) {
    console.error('[login] failed:', err)
  } finally {
    loading.value = false
  }
}

function goRegister() {
  router.replace({ name: 'Register' })
}

// 优先用 URL 里带过来的邮箱（从注册页跳回来）
const emailFromQuery = route.query.email as string | undefined

if (emailFromQuery) {
  form.email = emailFromQuery
} else {
  // 其次用 localStorage 里记住的
  const remembered = localStorage.getItem(REMEMBER_KEY)
  if (remembered) {
    form.email = remembered
    rememberMe.value = true
  }
}
</script>

<style lang="scss" scoped>
:deep(.el-card__header) {
  padding-bottom: 0;
  border-bottom: none;
}
</style>
