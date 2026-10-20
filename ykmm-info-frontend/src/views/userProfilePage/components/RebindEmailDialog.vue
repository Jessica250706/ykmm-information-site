<template>
  <el-dialog
    v-model="visible"
    :close-on-click-modal="false"
    title="换绑邮箱"
    width="480px"
    @closed="handleClosed"
  >
    <el-alert
      :closable="false"
      style="margin-bottom: 12px"
      title="换绑需同时验证旧邮箱与新邮箱，验证码有效期 5 分钟"
      type="info"
      show-icon
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <!-- 旧邮箱验证 -->
      <el-form-item label="当前邮箱">
        <el-input :model-value="currentEmail" disabled />
      </el-form-item>

      <el-form-item label="旧邮箱验证码" prop="oldCode">
        <div class="flex w-full items-center gap-2">
          <el-input
            v-model="form.oldCode"
            maxlength="6"
            placeholder="请输入旧邮箱收到的验证码"
            clearable
          />
          <el-button
            :disabled="oldCountdown > 0"
            :loading="sendingOld"
            plain
            @click="handleSendOld"
          >
            {{ oldCountdown > 0 ? `${oldCountdown}s` : '发送验证码' }}
          </el-button>
        </div>
      </el-form-item>

      <!-- 新邮箱验证 -->
      <el-form-item label="新邮箱" prop="newEmail">
        <el-input v-model.trim="form.newEmail" placeholder="请输入新邮箱" clearable />
      </el-form-item>

      <el-form-item label="新邮箱验证码" prop="newCode">
        <div class="flex w-full items-center gap-2">
          <el-input
            v-model="form.newCode"
            maxlength="6"
            placeholder="请输入新邮箱收到的验证码"
            clearable
          />
          <el-button
            :disabled="newCountdown > 0"
            :loading="sendingNew"
            plain
            @click="handleSendNew"
          >
            {{ newCountdown > 0 ? `${newCountdown}s` : '发送验证码' }}
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="submitting" type="primary" @click="handleSubmit">确认换绑</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { rebindEmailAPI, sendRebindEmailCodeAPI } from '@/api/user'
import { useUserStore } from '@/stores'
import type { FormInstance, FormRules } from 'element-plus'

const userStore = useUserStore()

const visible = ref(false)
const formRef = ref<FormInstance>()

const sendingOld = ref(false)
const sendingNew = ref(false)
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
  oldCode: [{ required: true, message: '请输入旧邮箱验证码', trigger: 'blur' }],
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

function open() {
  form.oldCode = ''
  form.newEmail = ''
  form.newCode = ''
  oldCountdown.value = 0
  newCountdown.value = 0
  visible.value = true
}

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

/** 发送新邮箱验证码 */
async function handleSendNew() {
  if (newCountdown.value > 0) return

  // 先做前端基本校验，避免发到非法地址
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

/** 60 秒倒计时 */
function startCountdown(which: 'old' | 'new') {
  const ref = which === 'old' ? oldCountdown : newCountdown
  ref.value = 60
  const timer = setInterval(() => {
    ref.value--
    if (ref.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

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

function handleClosed() {
  formRef.value?.resetFields()
  oldCountdown.value = 0
  newCountdown.value = 0
}

defineExpose({ open })
</script>
