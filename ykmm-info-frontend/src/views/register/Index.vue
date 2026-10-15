<template>
  <div class="grid min-h-screen place-items-center bg-slate-100 p-4">
    <el-card class="w-full max-w-90 rounded-2xl" shadow="never">
      <template #header>
        <div class="text-center">
          <h1 class="text-xl font-semibold text-slate-800">注册</h1>
          <p class="mt-1 text-sm text-slate-500">创建一个账号，开启你的旅程</p>
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
            @change="handleEmailChange"
          />
        </el-form-item>

        <el-form-item label="邮箱验证码" prop="code">
          <div class="flex w-full items-center justify-between gap-2">
            <el-input-otp
              v-model="form.code"
              :length="6"
              class="min-w-0 flex-1"
              size="normal"
              @change="handleCodeChange"
            />
            <el-button
              :disabled="!canSendCode"
              :loading="sendingCode"
              class="shrink-0"
              @click="handleSendCode"
            >
              {{ codeButtonText }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model.trim="form.nickname"
            :prefix-icon="User"
            autocomplete="nickname"
            placeholder="请输入昵称"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            :prefix-icon="Lock"
            autocomplete="new-password"
            placeholder="请输入密码"
            type="password"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            :prefix-icon="Lock"
            autocomplete="new-password"
            placeholder="请再次输入密码"
            type="password"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button :loading="loading" class="w-full" type="primary" @click="handleSubmit">
            {{ loading ? '注册中…' : '注 册' }}
          </el-button>
        </el-form-item>

        <div class="flex items-center justify-center text-sm text-slate-500">
          <span>已有账号？</span>
          <el-link :underline="false" class="ml-1" type="primary" @click="goLogin">
            返回登录
          </el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { Lock, Message, User } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { registerAPI, sendRegisterCodeAPI } from '@/api/auth'
import { CommonRouteName, ROLE } from '@/constants'
import { useUserStore } from '@/stores/userStore'
import type { RegisterRequest } from '@/types/auth'

/** 表单内部类型：比 RegisterRequest 多一个确认密码字段 */
interface RegisterForm extends RegisterRequest {
  confirmPassword: string
}

const router = useRouter()
const { setAuth } = useUserStore()

const formRef = ref<FormInstance>()

const form = reactive<RegisterForm>({
  email: '',
  nickname: '',
  password: '',
  code: '',
  confirmPassword: '',
})

const loading = ref(false)

const EMAIL_REGEX = /^[\w.-]+@[\w-]+(\.[\w-]+)+$/

/* ========== 发送验证码相关 ========== */
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const isEmailValid = computed(() => EMAIL_REGEX.test(form.email ?? ''))
const canSendCode = computed(
  () => isEmailValid.value && countdown.value === 0 && !sendingCode.value,
)
const codeButtonText = computed(() => {
  if (sendingCode.value) return '发送中…'
  if (countdown.value > 0) return `${countdown.value}s 后重发`
  return '获取验证码'
})

function startCountdown(seconds = 60) {
  countdown.value = seconds
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      countdown.value = 0
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }
  }, 1000)
}

onBeforeUnmount(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

async function handleSendCode() {
  if (!canSendCode.value) return

  try {
    await formRef.value?.validateField('email')
  } catch {
    return
  }

  sendingCode.value = true
  try {
    await sendRegisterCodeAPI({ email: form.email ?? '' })
    ElMessage.success('验证码已发送，请查收邮箱')
    startCountdown(60)
  } catch (err) {
    console.error('[register] send code failed:', err)
  } finally {
    sendingCode.value = false
  }
}

/** 邮箱被改动时，清空倒计时并重置验证码，避免用错邮箱 */
function handleEmailChange() {
  form.code = ''
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  countdown.value = 0
}

/** OTP 组件 v-model 更新后，手动触发 code 字段校验 */
function handleCodeChange() {
  // 等 v-model 写入后下一帧再校验，避免读到旧值
  requestAnimationFrame(() => {
    formRef.value?.validateField('code').catch(() => {})
  })
}

/* ========== 表单校验 ========== */
function validateConfirm(_rule: unknown, value: string, callback: (e?: Error) => void) {
  if (!value) {
    callback(new Error('请再次输入密码'))
    return
  }
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
    return
  }
  callback()
}

const rules: FormRules<RegisterForm> = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { pattern: EMAIL_REGEX, message: '邮箱格式不正确', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入邮箱验证码', trigger: 'change' },
    { pattern: /^\d{6}$/, message: '验证码为 6 位数字', trigger: 'change' },
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度为 2-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度为 6-64 个字符', trigger: 'blur' },
  ],
  confirmPassword: [{ validator: validateConfirm, trigger: 'blur' }],
}

function goLogin() {
  router.replace({ name: CommonRouteName.LOGIN })
}

/* ========== 提交注册 ========== */
async function handleSubmit() {
  if (!formRef.value || loading.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const { email, nickname, password, code } = form
    const payload: RegisterRequest = { email, nickname, password, code }

    const { data } = await registerAPI(payload)

    if (data?.token && data?.user) {
      setAuth(data.token, data.user)
      ElMessage.success('注册成功')
      await router.replace(data.user.role === ROLE.ADMIN ? '/admin' : '/card')
      return
    }

    ElMessage.success('注册成功，请登录')
    await router.replace({
      name: CommonRouteName.LOGIN,
      query: { email: form.email },
    })
  } catch (err: unknown) {
    console.error('[register] failed:', err)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
:deep(.el-card__header) {
  padding-bottom: 0;
  border-bottom: none;
}
</style>
