<template>
  <el-dialog v-model="visible" :title="title" width="480px" @closed="handleClosed">
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

      <el-form-item
        v-if="mode === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC]"
        label="发起人"
        prop="roleId"
      >
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
import { SOURCE_TYPE, SOURCE_TYPE_SMALL_LABEL, type SourceTypeSmallLabelValue } from '@/constants'
import type { RoleGroupVO } from '@/types/role'
import type { FormInstance, FormRules } from 'element-plus'

type Mode = SourceTypeSmallLabelValue

/** 话数表单数据 */
export interface EpisodeFormData {
  episodeNo?: number
  title?: string
  roleId?: number
}

/** 编辑时传入的原始话数数据（含 id） */
export interface EpisodeItemData extends EpisodeFormData {
  id?: number
}

const props = defineProps<{
  saving?: boolean
  /** 已有话数列表，用于计算新增时的默认话数 */
  existingEpisodes: { episodeNo?: number }[]
}>()

const emit = defineEmits<{
  submit: [payload: { mode: Mode; id: number | null; data: EpisodeFormData }]
}>()

const visible = ref(false)
const mode = ref<Mode>(SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC])
/** 编辑时的 id：null 表示新增 */
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive<EpisodeFormData>({
  episodeNo: undefined,
  title: '',
  roleId: undefined,
})

const title = computed(() => {
  const action = editingId.value == null ? '新增' : '编辑'
  const kind = mode.value === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC] ? 'RC' : 'RTV'
  return `${action} ${kind}`
})

const rules = computed<FormRules>(() => ({
  episodeNo: [{ required: true, message: '请输入话数', trigger: 'blur' }],
  ...(mode.value === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC]
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
  if (m === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC] && roleGroups.value.length === 0)
    void loadRoles()
})

/* -------- 对外打开 -------- */
/**
 * 打开弹窗
 *
 * @param m 模式：rc / rtv
 * @param episode 传入则表示编辑，不传表示新增
 */
function open(m: Mode, episode?: EpisodeItemData) {
  mode.value = m
  editingId.value = episode?.id ?? null

  if (episode) {
    // 编辑：回填
    form.episodeNo = episode.episodeNo
    form.title = episode.title ?? ''
    form.roleId = episode.roleId
  } else {
    // 新增：默认话数 = 已有最大话数 + 1
    form.episodeNo = (props.existingEpisodes.at(-1)?.episodeNo ?? 0) + 1
    form.title = ''
    form.roleId = undefined
  }

  visible.value = true
  if (m === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC]) void loadRoles()
}

function handleClosed() {
  formRef.value?.clearValidate()
  editingId.value = null
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  emit('submit', {
    mode: mode.value,
    id: editingId.value,
    data: { ...form },
  })
}

/** 供父组件在保存成功后关闭 */
function close() {
  visible.value = false
}

defineExpose({ open, close })
</script>
