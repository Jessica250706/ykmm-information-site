<template>
  <el-dialog
    v-model="visible"
    :title="mode === 'rc' ? '新增 RC' : '新增 RTV'"
    width="480px"
    @closed="handleClosed"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="话数" prop="episodeNo">
        <el-input-number
          v-model="form.episodeNo"
          :max="999"
          :min="1"
          :precision="0"
          placeholder="第几话"
          style="width: 180px"
        />
      </el-form-item>

      <el-form-item v-if="mode === 'rc'" label="发起人" prop="roleId">
        <el-cascader
          v-model="form.roleId"
          :options="roleOptions"
          :props="cascaderProps"
          :show-all-levels="false"
          class="w-full"
          placeholder="请选择 RC 发起人"
          clearable
          filterable
        />
        <div class="mt-1 text-xs text-slate-400">
          发起人的对话默认显示在右侧，其他角色显示在左侧
        </div>
      </el-form-item>

      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" maxlength="255" placeholder="请输入话标题" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="saving" type="primary" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { listGroupedRolesAPI } from '@/api/role'
import type { RoleGroupVO } from '@/types/role'
import type { FormInstance, FormRules } from 'element-plus'

type Mode = 'rc' | 'rtv'

export interface EpisodeFormData {
  episodeNo?: number
  title?: string
  roleId?: number
}

const props = defineProps<{
  saving?: boolean
  /** 已有话数列表，用于计算默认话数 */
  existingEpisodes: { episodeNo?: number }[]
}>()

const emit = defineEmits<{
  submit: [payload: { mode: Mode; data: EpisodeFormData }]
}>()

const visible = ref(false)
const mode = ref<Mode>('rc')
const formRef = ref<FormInstance>()

const form = reactive<EpisodeFormData>({
  episodeNo: undefined,
  title: '',
  roleId: undefined,
})

const rules = computed<FormRules>(() => ({
  episodeNo: [{ required: true, message: '请输入话数', trigger: 'blur' }],
  ...(mode.value === 'rc'
    ? { roleId: [{ required: true, message: '请选择发起人', trigger: 'change' }] }
    : {}),
}))

/* -------- 角色下拉 -------- */
const roleGroups = ref<RoleGroupVO[]>([])

const cascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: false,
  checkStrictly: false,
} as const

const roleOptions = computed(() =>
  roleGroups.value.map((g) => ({
    value: `person:${g.personId ?? 'other'}`,
    label: g.personName ?? '未分组',
    disabled: false,
    children: (g.roles ?? []).map((r) => ({
      value: r.id!,
      label: r.name ?? '',
    })),
  })),
)

async function loadRoles() {
  try {
    const res = await listGroupedRolesAPI()
    roleGroups.value = res.data ?? []
  } catch {
    roleGroups.value = []
  }
}

watch(mode, (m) => {
  if (m === 'rc' && roleGroups.value.length === 0) void loadRoles()
})

/* -------- 对外打开 -------- */
function open(m: Mode) {
  mode.value = m
  form.episodeNo = (props.existingEpisodes.at(-1)?.episodeNo ?? 0) + 1
  form.title = ''
  form.roleId = undefined
  visible.value = true
  if (m === 'rc') void loadRoles()
}

function handleClosed() {
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  emit('submit', { mode: mode.value, data: { ...form } })
}

/** 供父组件在保存成功后关闭 */
function close() {
  visible.value = false
}

defineExpose({ open, close })
</script>
