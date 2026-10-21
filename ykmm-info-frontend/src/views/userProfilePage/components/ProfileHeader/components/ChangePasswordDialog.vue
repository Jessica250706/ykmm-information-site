<template>
  <el-dialog
    v-model="visible"
    :close-on-click-modal="false"
    title="修改密码"
    width="500px"
    @closed="handleClosed"
  >
    <el-tabs v-model="mode">
      <!-- ========== Tab 1：原密码修改 ========== -->
      <el-tab-pane label="原密码修改" name="password">
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          class="mt-2!"
          label-width="100px"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              placeholder="请输入当前密码"
              type="password"
              show-password
            />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              placeholder="6-64 个字符"
              type="password"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              placeholder="请再次输入新密码"
              type="password"
              show-password
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- ========== Tab 2：邮箱验证码重置 ========== -->
      <el-tab-pane label="邮箱验证码重置" name="code">
        <el-alert
          :closable="false"
          class="mb-4!"
          title="忘记原密码？请输入邮箱验证码进行身份验证"
          type="info"
          show-icon
        />

        <el-form ref="codeFormRef" :model="codeForm" :rules="codeRules" label-width="100px">
          <el-form-item label="邮箱验证码" prop="code">
            <div class="flex w-full items-center justify-between gap-2">
              <el-input-otp
                v-model="codeForm.code"
                :length="6"
                class="min-w-0 flex-1"
                size="normal"
              />
              <el-button
                :disabled="countdown > 0"
                :loading="sendingCode"
                class="shrink-0"
                @click="handleSendCode"
              >
                {{ codeButtonText }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="codeForm.newPassword"
              placeholder="6-64 个字符"
              type="password"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="codeForm.confirmPassword"
              placeholder="请再次输入新密码"
              type="password"
              show-password
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitting" type="primary" @click="handleSubmit">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { changePasswordAPI, resetPasswordByCodeAPI, sendResetPasswordCodeAPI } from '@/api/user'
import { CommonRouteName } from '@/constants'
import { useUserStore } from '@/stores'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

type Mode = 'password' | 'code'

const visible = ref(false)
const mode = ref<Mode>('password')
const submitting = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

const passwordFormRef = ref<FormInstance>()
const codeFormRef = ref<FormInstance>()

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const codeForm = reactive({
  code: '',
  newPassword: '',
  confirmPassword: '',
})

/** 通用的新密码 + 确认密码规则 */
const PASSWORD_MIN = 6
const PASSWORD_MAX = 64

function makeConfirmValidator(getNewPassword: () => string) {
  return (_rule: unknown, value: string, cb: (e?: Error) => void) => {
    if (!value) {
      cb(new Error('请再次输入新密码'))
    } else if (value !== getNewPassword()) {
      cb(new Error('两次输入的密码不一致'))
    } else {
      cb()
    }
  }
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      min: PASSWORD_MIN,
      max: PASSWORD_MAX,
      message: `密码长度为 ${PASSWORD_MIN}-${PASSWORD_MAX} 个字符`,
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    {
      validator: makeConfirmValidator(() => passwordForm.newPassword),
      trigger: 'blur',
    },
  ],
}

const codeRules: FormRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为 6 位数字', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      min: PASSWORD_MIN,
      max: PASSWORD_MAX,
      message: `密码长度为 ${PASSWORD_MIN}-${PASSWORD_MAX} 个字符`,
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    {
      validator: makeConfirmValidator(() => codeForm.newPassword),
      trigger: 'blur',
    },
  ],
}

const codeButtonText = computed(() => (countdown.value > 0 ? `${countdown.value}s` : '发送验证码'))

/* -------- 打开 / 关闭 -------- */

function open() {
  mode.value = 'password'
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  codeForm.code = ''
  codeForm.newPassword = ''
  codeForm.confirmPassword = ''
  countdown.value = 0
  visible.value = true
}

function handleClosed() {
  passwordFormRef.value?.resetFields()
  codeFormRef.value?.resetFields()
  countdown.value = 0
}

/* -------- 发送验证码 -------- */

async function handleSendCode() {
  if (countdown.value > 0) return
  sendingCode.value = true
  try {
    await sendResetPasswordCodeAPI()
    ElMessage.success('验证码已发送至当前邮箱')
    startCountdown()
  } finally {
    sendingCode.value = false
  }
}

function startCountdown() {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

/* -------- 提交 -------- */

async function handleSubmit() {
  if (submitting.value) return

  if (mode.value === 'password') {
    const valid = await passwordFormRef.value?.validate().catch(() => false)
    if (!valid) return
    submitting.value = true
    try {
      await changePasswordAPI({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword,
      })
      await handleSuccess()
    } finally {
      submitting.value = false
    }
    return
  }

  // mode === 'code'
  const valid = await codeFormRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await resetPasswordByCodeAPI({
      code: codeForm.code,
      newPassword: codeForm.newPassword,
    })
    await handleSuccess()
  } finally {
    submitting.value = false
  }
}

/**
 * 修改成功后的统一处理：
 * 密码变了，让用户用新密码重新登录，更安全（旧 token 可能仍在有效期）
 */
async function handleSuccess() {
  visible.value = false

  try {
    await ElMessageBox.confirm('密码修改成功，请使用新密码重新登录', '提示', {
      type: 'success',
      confirmButtonText: '立即登录',
      cancelButtonText: '稍后',
      distinguishCancelAndClose: true,
    })
    // 用户选择"立即登录"
    userStore.logout()
    await router.replace({ name: CommonRouteName.LOGIN })
  } catch {
    // 用户选择"稍后"，只清除登录态不跳转
    userStore.logout()
    await router.replace({ name: CommonRouteName.LOGIN })
  }
}

defineExpose({ open })
</script>
