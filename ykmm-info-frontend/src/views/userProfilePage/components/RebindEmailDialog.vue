<template>
  <el-dialog
    v-model="visible"
    :close-on-click-modal="false"
    title="换绑邮箱"
    width="520px"
    @closed="handleClosed"
  >
    <!-- 步骤条 -->
    <el-steps :active="activeStep" class="mb-6!" finish-status="success" align-center>
      <el-step title="验证旧邮箱" />
      <el-step title="设置新邮箱" />
    </el-steps>

    <!-- ========== Step 1：验证旧邮箱 ========== -->
    <div v-show="activeStep === 0">
      <el-alert
        :closable="false"
        class="mb-4!"
        title="请先验证当前绑定邮箱"
        type="info"
        show-icon
      />

      <el-form label-width="96px">
        <el-form-item label="当前邮箱">
          <el-input :model-value="currentEmail" disabled />
        </el-form-item>

        <el-form-item label="邮箱验证码" prop="oldCode">
          <div class="flex w-full items-center justify-between gap-2">
            <el-input-otp
              v-model="form.oldCode"
              :length="6"
              class="min-w-0 flex-1"
              size="normal"
              @change="handleOldCodeChange"
            />
            <el-button
              :disabled="oldCountdown > 0"
              :loading="sendingOld"
              class="shrink-0"
              @click="handleSendOld"
            >
              {{ oldCodeButtonText }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- ========== Step 2：设置新邮箱 ========== -->
    <div v-show="activeStep === 1">
      <el-alert
        :closable="false"
        class="mb-4!"
        title="请输入并验证新的邮箱地址"
        type="success"
        show-icon
      />

      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="新邮箱" prop="newEmail">
          <el-input v-model.trim="form.newEmail" placeholder="请输入新邮箱" clearable />
        </el-form-item>

        <el-form-item label="邮箱验证码" prop="newCode">
          <div class="flex w-full items-center justify-between gap-2">
            <el-input-otp
              v-model="form.newCode"
              :length="6"
              class="min-w-0 flex-1"
              size="normal"
              @change="handleNewCodeChange"
            />
            <el-button
              :disabled="newCountdown > 0"
              :loading="sendingNew"
              class="shrink-0"
              @click="handleSendNew"
            >
              {{ newCodeButtonText }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>

      <!-- Step 1：上一步（隐藏） + 下一步 -->
      <template v-if="activeStep === 0">
        <el-button
          :disabled="form.oldCode.length !== 6"
          :loading="verifyingOld"
          type="primary"
          @click="handleNext"
        >
          下一步
        </el-button>
      </template>

      <!-- Step 2：上一步 + 提交 -->
      <template v-else>
        <el-button @click="handlePrev">上一步</el-button>
        <el-button :loading="submitting" type="primary" @click="handleSubmit">确认换绑</el-button>
      </template>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { rebindEmailAPI, sendRebindEmailCodeAPI, verifyOldEmailCodeAPI } from '@/api/user'
import { useUserStore } from '@/stores'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()

const visible = ref(false)
const formRef = ref<FormInstance>()

/** 当前步骤：0=验证旧邮箱，1=设置新邮箱 */
const activeStep = ref(0)

const sendingOld = ref(false)
const sendingNew = ref(false)
const verifyingOld = ref(false)
const submitting = ref(false)

const oldCountdown = ref(0)
const newCountdown = ref(0)

const form = reactive({
  oldCode: '',
  newEmail: '',
  newCode: '',
})

const currentEmail = computed(() => userStore.userInfo?.email ?? '')

const EMAIL_REGEX = /^[\w.-]+@[\w-]+(\.[\w-]+)+$/

const rules: FormRules = {
  newEmail: [
    { required: true, message: '请输入新邮箱', trigger: 'blur' },
    { pattern: EMAIL_REGEX, message: '邮箱格式不正确', trigger: 'blur' },
    {
      validator: (_rule, value: string, cb) => {
        if (value && value.toLowerCase() === currentEmail.value.toLowerCase()) {
          cb(new Error('新邮箱不能与旧邮箱相同'))
        } else {
          cb()
        }
      },
      trigger: 'blur',
    },
  ],
  newCode: [{ required: true, message: '请输入新邮箱验证码', trigger: 'blur' }],
}

/* -------- 按钮文案 -------- */
const oldCodeButtonText = computed(() =>
  oldCountdown.value > 0 ? `${oldCountdown.value}s` : '发送验证码',
)
const newCodeButtonText = computed(() =>
  newCountdown.value > 0 ? `${newCountdown.value}s` : '发送验证码',
)

/* -------- 打开 / 关闭 -------- */
function open() {
  activeStep.value = 0
  form.oldCode = ''
  form.newEmail = ''
  form.newCode = ''
  oldCountdown.value = 0
  newCountdown.value = 0
  visible.value = true
}

function handleClosed() {
  formRef.value?.resetFields()
  activeStep.value = 0
  oldCountdown.value = 0
  newCountdown.value = 0
}

/* -------- Step 1 -------- */

/** 发送旧邮箱验证码 */
async function handleSendOld() {
  if (oldCountdown.value > 0) return
  sendingOld.value = true
  try {
    await sendRebindEmailCodeAPI({ target: 'OLD' })
    ElMessage.success('验证码已发送至当前邮箱')
    startCountdown('old')
  } finally {
    sendingOld.value = false
  }
}

/** 旧验证码输满 6 位时自动触发下一步 */
async function handleOldCodeChange(val: string) {
  if (val.length !== 6 || verifyingOld.value) return
  await handleNext()
}

/** 下一步：调后端校验旧验证码 */
async function handleNext() {
  if (form.oldCode.length !== 6) {
    ElMessage.warning('请输入 6 位验证码')
    return
  }
  if (verifyingOld.value) return

  verifyingOld.value = true
  try {
    await verifyOldEmailCodeAPI({ code: form.oldCode })
    ElMessage.success('旧邮箱验证通过')
    activeStep.value = 1
  } catch {
    // 全局拦截器已经弹了后端错误，这里只清空，方便用户重输
    form.oldCode = ''
  } finally {
    verifyingOld.value = false
  }
}

/* -------- Step 2 -------- */

/** 上一步：回到验证旧邮箱 */
function handlePrev() {
  activeStep.value = 0
}

/** 发送新邮箱验证码 */
async function handleSendNew() {
  if (newCountdown.value > 0) return

  if (!form.newEmail) {
    ElMessage.warning('请先输入新邮箱')
    return
  }
  if (!EMAIL_REGEX.test(form.newEmail)) {
    ElMessage.warning('新邮箱格式不正确')
    return
  }
  if (form.newEmail.toLowerCase() === currentEmail.value.toLowerCase()) {
    ElMessage.warning('新邮箱不能与旧邮箱相同')
    return
  }

  sendingNew.value = true
  try {
    await sendRebindEmailCodeAPI({ target: 'NEW', email: form.newEmail })
    ElMessage.success('验证码已发送至新邮箱')
    startCountdown('new')
  } finally {
    sendingNew.value = false
  }
}

/** 新验证码输满 6 位时不自动提交（让用户核对新邮箱后再点"确认换绑"） */
function handleNewCodeChange(_val: string) {
  // 预留：如果希望自动提交，可在这里调用 handleSubmit()
}

/** 提交换绑 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.oldCode.length !== 6) {
    ElMessage.warning('旧邮箱验证码无效，请重新验证')
    activeStep.value = 0
    return
  }
  if (submitting.value) return

  submitting.value = true
  try {
    const res = await rebindEmailAPI({
      newEmail: form.newEmail,
      oldCode: form.oldCode,
      newCode: form.newCode,
    })
    if (res.data) {
      userStore.setUserInfo(res.data)
    }
    ElMessage.success('邮箱换绑成功')
    visible.value = false
  } finally {
    submitting.value = false
  }
}

/* -------- 倒计时 -------- */
function startCountdown(which: 'old' | 'new') {
  const target = which === 'old' ? oldCountdown : newCountdown
  target.value = 60
  const timer = setInterval(() => {
    target.value--
    if (target.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

defineExpose({ open })
</script>
