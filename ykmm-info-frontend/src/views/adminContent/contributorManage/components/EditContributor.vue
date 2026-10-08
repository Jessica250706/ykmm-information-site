<template>
  <div class="contributor-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑贡献者' : '新增贡献者' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-2xl" label-width="120px">
        <!-- 对话版本 ID -->
        <el-form-item label="对话版本ID" prop="versionId">
          <el-input-number
            v-model="form.versionId"
            :controls="false"
            :disabled="isEdit"
            :min="1"
            placeholder="请输入对话版本ID"
            style="width: 220px"
          />
          <span class="ml-2 text-xs text-slate-400">
            {{ isEdit ? '编辑时不可修改' : '该贡献者所属的对话版本ID' }}
          </span>
        </el-form-item>

        <!-- 类型选择：账号 / 匿名 -->
        <el-form-item label="贡献者类型">
          <el-radio-group v-model="contributorKind" @change="handleKindChange">
            <el-radio value="user">注册用户</el-radio>
            <el-radio value="anonymous">无账号（仅姓名）</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 有账号：用户 ID -->
        <el-form-item v-if="contributorKind === 'user'" label="用户ID" prop="userId">
          <el-input-number
            v-model="form.userId"
            :controls="false"
            :min="1"
            placeholder="请输入用户ID"
            style="width: 220px"
          />
          <span class="ml-2 text-xs text-slate-400">关联 sys_user.id，头像和昵称将自动带出</span>
        </el-form-item>

        <!-- 无账号：姓名 -->
        <el-form-item v-else label="贡献者姓名" prop="contributorName">
          <el-input v-model="form.contributorName" placeholder="请输入姓名" style="width: 220px" />
          <span class="ml-2 text-xs text-slate-400">如"张三"，用于展示，不关联账号</span>
        </el-form-item>

        <!-- 贡献者角色 -->
        <el-form-item label="角色" prop="contributorRole">
          <el-radio-group v-model="form.contributorRole">
            <el-radio v-for="opt in CONTRIBUTOR_ROLE_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 说明 -->
        <el-form-item label="说明">
          <div class="text-xs leading-relaxed text-slate-500">
            <p>
              ·
              <b>内容作者</b>
              ：版本内容的实际归属人，通常是被代传的用户，或上传内容的用户本人。
            </p>
            <p>
              ·
              <b>代传管理员</b>
              ：管理员代替用户上传内容时，记录管理员自己。
            </p>
            <p>
              ·
              <b>协作者</b>
              ：参与协作编辑的其他用户。
            </p>
            <p class="mt-1 text-slate-400">同一个版本下，同一用户 / 同一姓名只允许存在一条记录。</p>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import {
  createDialogueVersionContributorAPI,
  getDialogueVersionContributorDetailAPI,
  updateDialogueVersionContributorAPI,
} from '@/api/dialogueVersionContributor'
import {
  AdminRouteName,
  CONTRIBUTOR_ROLE,
  CONTRIBUTOR_ROLE_OPTIONS,
  type ContributorRoleValue,
} from '@/constants'
import type { DialogueVersionContributorDTO } from '@/types/dialogueVersionContributor'

const route = useRoute()
const router = useRouter()

/* -------- 模式 -------- */
const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

/* -------- 表单 -------- */
const formRef = ref<FormInstance>()
const loading = ref(false)

/** 贡献者类型：有账号 / 无账号（前端辅助状态，不入库） */
const contributorKind = ref<'user' | 'anonymous'>('user')

const form = reactive<DialogueVersionContributorDTO>({
  versionId: undefined,
  userId: undefined,
  contributorName: '',
  contributorRole: CONTRIBUTOR_ROLE.AUTHOR,
})

const rules: FormRules<DialogueVersionContributorDTO> = {
  versionId: [{ required: true, message: '请输入对话版本ID', trigger: 'blur' }],
  contributorRole: [{ required: true, message: '请选择贡献者角色', trigger: 'change' }],
}

/* -------- 切换贡献者类型：清掉对侧字段 -------- */
function handleKindChange(val: 'user' | 'anonymous') {
  if (val === 'user') {
    form.contributorName = ''
  } else {
    form.userId = undefined
  }
}

/* -------- 加载详情 -------- */
async function loadDetail() {
  if (!editId.value) return
  const res = await getDialogueVersionContributorDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('贡献者不存在')
    handleBack()
    return
  }

  form.versionId = data.versionId
  form.userId = data.userId
  form.contributorName = data.contributorName ?? ''
  form.contributorRole = (data.contributorRole as ContributorRoleValue) ?? CONTRIBUTOR_ROLE.AUTHOR

  // 回填类型选择
  contributorKind.value = data.userId != null ? 'user' : 'anonymous'
}

/* -------- 提交 -------- */
async function handleSubmit() {
  if (!formRef.value) return

  // 自定义校验：用户ID 和 姓名至少有一个
  const hasUser = form.userId != null
  const hasName = !!form.contributorName?.trim()
  if (!hasUser && !hasName) {
    ElMessage.warning('注册用户ID 和 姓名不能同时为空')
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 根据类型清理字段
  const payload: DialogueVersionContributorDTO = {
    versionId: form.versionId,
    contributorRole: form.contributorRole,
    userId: contributorKind.value === 'user' ? form.userId : undefined,
    contributorName:
      contributorKind.value === 'anonymous' ? form.contributorName?.trim() : undefined,
  }

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateDialogueVersionContributorAPI(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createDialogueVersionContributorAPI(payload)
      ElMessage.success('新增成功')
    }
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push({ name: AdminRouteName.CONTRIBUTOR_MANAGE })
}

/* -------- 初始化 -------- */
onMounted(async () => {
  if (isEdit.value) {
    await loadDetail()
  }
})
</script>
