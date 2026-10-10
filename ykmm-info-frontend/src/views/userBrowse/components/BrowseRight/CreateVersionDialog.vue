<template>
  <el-dialog
    v-model="visible"
    :close-on-click-modal="false"
    :title="dialogTitle"
    width="520px"
    append-to-body
  >
    <el-form label-width="110px">
      <!-- 创建方式 -->
      <el-form-item label="创建方式">
        <el-radio-group v-model="mode">
          <el-radio value="self">为自己创建</el-radio>
          <el-radio value="delegate">代他人创建</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 代传：贡献者类型 -->
      <template v-if="mode === 'delegate'">
        <el-form-item label="贡献者类型">
          <el-radio-group v-model="contributorKind">
            <el-radio value="user">已注册用户</el-radio>
            <el-radio value="anonymous">无账号（仅姓名）</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="contributorKind === 'user'" label="用户ID" required>
          <el-input-number
            v-model="contributorUserId"
            :controls="false"
            :min="1"
            placeholder="请输入用户ID"
            style="width: 100%"
          />
          <div class="mt-1 text-xs text-slate-400">
            内容将归属到该用户，同时在贡献者中记录代传管理员
          </div>
        </el-form-item>

        <el-form-item v-else label="贡献者姓名" required>
          <el-input
            v-model="contributorName"
            maxlength="64"
            placeholder="请输入姓名"
            style="width: 100%"
          />
          <div class="mt-1 text-xs text-slate-400">用于展示，不关联账号</div>
        </el-form-item>
      </template>

      <!-- 本人创建说明 -->
      <el-alert v-else :closable="false" type="info" show-icon>
        <template #default>
          <div class="text-xs">该版本将作为您本人的贡献记录：您既是实际操作人，也是内容作者。</div>
        </template>
      </el-alert>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="confirming" type="primary" @click="onConfirm">确认创建</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { CONTRIBUTOR_KIND, type ContributorKind, CREATE_MODE, type CreateMode } from '@/constants'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'

/** 创建版本时携带的贡献者信息 */
export interface CreateVersionPayload {
  /** 选中的版本 option */
  option: DialogueVersionOptionVO
  /** 贡献者用户ID，本人创建时为 null */
  contributorUserId: number | null
  /** 无账号贡献者姓名，本人创建时为 null */
  contributorName: string | null
}

defineProps<{
  /** 是否正在创建（loading） */
  confirming?: boolean
}>()

const emit = defineEmits<{
  confirm: [payload: CreateVersionPayload]
}>()

const visible = ref(false)
const currentOption = ref<DialogueVersionOptionVO | null>(null)

const mode = ref<CreateMode>('self')
const contributorKind = ref<ContributorKind>('user')
const contributorUserId = ref<number | undefined>(undefined)
const contributorName = ref('')

const dialogTitle = computed(() =>
  currentOption.value?.label ? `创建「${currentOption.value.label}」内容` : '创建版本内容',
)

/** 打开对话框，重置状态 */
function open(option: DialogueVersionOptionVO) {
  currentOption.value = option
  mode.value = CREATE_MODE.SELF
  contributorKind.value = CONTRIBUTOR_KIND.USER
  contributorUserId.value = undefined
  contributorName.value = ''
  visible.value = true
}

/** 切换创建方式时清空对侧输入 */
watch(mode, () => {
  contributorUserId.value = undefined
  contributorName.value = ''
})

watch(contributorKind, () => {
  contributorUserId.value = undefined
  contributorName.value = ''
})

function onConfirm() {
  const option = currentOption.value
  if (!option) return

  // 为自己创建：不需要传贡献者信息，后端用当前登录用户
  if (mode.value === CREATE_MODE.SELF) {
    emit('confirm', {
      option,
      contributorUserId: null,
      contributorName: null,
    })
    visible.value = false
    return
  }

  // 代传已注册用户
  if (contributorKind.value === CONTRIBUTOR_KIND.USER) {
    if (contributorUserId.value == null) {
      ElMessage.warning('请输入用户ID')
      return
    }
    emit('confirm', {
      option,
      contributorUserId: contributorUserId.value,
      contributorName: null,
    })
    visible.value = false
    return
  }

  // 代传无账号用户
  const name = contributorName.value.trim()
  if (!name) {
    ElMessage.warning('请输入姓名')
    return
  }
  emit('confirm', {
    option,
    contributorUserId: null,
    contributorName: name,
  })
  visible.value = false
}

defineExpose({ open })
</script>
