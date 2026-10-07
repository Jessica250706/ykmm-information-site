<template>
  <div class="card-rc-manage flex h-full flex-col">
    <!-- 顶部：卡面信息 + 操作栏 -->
    <el-card class="mb-4 shrink-0" shadow="never">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <el-button @click="handleBack">← 返回</el-button>
          <div>
            <div class="text-xs text-slate-400">RC 管理</div>
            <div class="font-medium">
              {{ cardInfo.name || `卡面 #${cardId}` }}
              <el-tag v-if="cardInfo.seriesName" class="ml-2" effect="plain" size="small">
                {{ cardInfo.seriesName }}
              </el-tag>
            </div>
          </div>
        </div>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增话数
        </el-button>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card class="flex-1" shadow="never">
      <el-table v-loading="loading" :data="list" row-key="id" stripe>
        <el-table-column align="center" label="话数" prop="episodeNo" width="100">
          <template #default="{ row }">
            <el-tag effect="plain" type="primary">第 {{ row.episodeNo }} 话</el-tag>
          </template>
        </el-table-column>

        <el-table-column align="center" label="发起人" min-width="160">
          <template #default="{ row }">
            <el-tag v-if="row.roleId" effect="plain" type="success">
              {{ row.roleName || `#${row.roleId}` }}
            </el-tag>
            <span v-else class="text-slate-300">-</span>
          </template>
        </el-table-column>

        <el-table-column label="标题" min-width="240" prop="title" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="font-medium">{{ row.title || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column align="center" label="创建时间" prop="createdAt" width="180" />

        <el-table-column align="center" fixed="right" label="操作" width="260">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="success" link @click="handleManageDialogue(row)">
              管理对话
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && !list.length" description="暂无 RC 话数" />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑 RC' : '新增 RC'"
      width="520px"
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
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

        <el-form-item label="发起人" prop="roleId">
          <el-cascader
            v-model="form.roleId"
            :options="roleCascaderOptions"
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="saving" type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getCardDetailAPI } from '@/api/card'
import {
  createCardRcAPI,
  deleteCardRcAPI,
  listCardRcByCardAPI,
  updateCardRcAPI,
} from '@/api/cardRc'
import { listGroupedRolesAPI } from '@/api/role'
import { SOURCE_TYPE } from '@/constants'
import type { CardVO } from '@/types/card'
import type { CardRcDTO, CardRcVO } from '@/types/cardRc'
import type { RoleGroupVO } from '@/types/role'

const route = useRoute()
const router = useRouter()

/** 卡面ID，从 query 里取 */
const cardId = computed(() => Number(route.query.cardId ?? 0))

const loading = ref(false)
const list = ref<CardRcVO[]>([])
const cardInfo = ref<CardVO>({})

/* -------- 数据加载 -------- */
async function loadList() {
  if (!cardId.value) return
  loading.value = true
  try {
    const res = await listCardRcByCardAPI(cardId.value)
    list.value = res.data ?? []
  } catch {
    ElMessage.error('加载 RC 列表失败')
  } finally {
    loading.value = false
  }
}

async function loadCardInfo() {
  if (!cardId.value) return
  try {
    const res = await getCardDetailAPI(cardId.value)
    cardInfo.value = res.data ?? {}
  } catch {
    // 忽略
  }
}

/* -------- 发起人下拉 -------- */
const roleOptions = ref<RoleGroupVO[]>([])

async function loadRoles() {
  try {
    const res = await listGroupedRolesAPI()
    roleOptions.value = res.data ?? []
  } catch {
    roleOptions.value = []
  }
}

const cascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: false,
  checkStrictly: false,
} as const

const roleCascaderOptions = computed(() =>
  roleOptions.value.map((g) => ({
    value: `person:${g.personId ?? 'other'}`,
    label: g.personName ?? '未分组',
    disabled: false,
    children: (g.roles ?? []).map((r) => ({
      value: r.id!,
      label: r.name ?? '',
    })),
  })),
)

/* -------- 弹窗 & 表单 -------- */
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<CardRcDTO & { id?: number }>({
  id: undefined,
  cardId: undefined,
  roleId: undefined,
  episodeNo: undefined,
  title: '',
})

const rules: FormRules = {
  episodeNo: [{ required: true, message: '请输入话数', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择 RC 发起人', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}

function handleCreate() {
  form.id = undefined
  form.cardId = cardId.value
  form.roleId = undefined
  form.episodeNo = (list.value.at(-1)?.episodeNo ?? 0) + 1
  form.title = ''
  dialogVisible.value = true
}

function handleEdit(row: CardRcVO) {
  form.id = row.id
  form.cardId = row.cardId
  form.roleId = row.roleId
  form.episodeNo = row.episodeNo
  form.title = row.title ?? ''
  dialogVisible.value = true
}

function handleDialogClosed() {
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (form.id) {
      await updateCardRcAPI(form.id, {
        cardId: form.cardId,
        roleId: form.roleId,
        episodeNo: form.episodeNo,
        title: form.title,
      })
      ElMessage.success('更新成功')
    } else {
      await createCardRcAPI({
        cardId: cardId.value,
        roleId: form.roleId,
        episodeNo: form.episodeNo,
        title: form.title,
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: CardRcVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除第 ${row.episodeNo} 话「${row.title ?? ''}」吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '删除',
        confirmButtonClass: 'el-button--danger',
      },
    )
  } catch {
    return
  }
  await deleteCardRcAPI(row.id!)
  ElMessage.success('删除成功')
  await loadList()
}

/**
 * 跳转到对话编辑页
 * RC 的对话版本 sourceType=3，sourceId=rcId
 */
function handleManageDialogue(row: CardRcVO) {
  ElMessage.info('对话编辑入口待接入，请使用对应的对话编辑页面')
  router.push({
    name: 'UserCardRcEdit',
    query: { sourceType: SOURCE_TYPE.RC, sourceId: String(row.id) },
  })
}

function handleBack() {
  router.push({ name: 'AdminCardManage' })
}

/* -------- 初始化 -------- */
onMounted(async () => {
  if (!cardId.value) {
    ElMessage.error('缺少卡面ID')
    handleBack()
    return
  }
  await Promise.all([loadCardInfo(), loadList(), loadRoles()])
})
</script>
