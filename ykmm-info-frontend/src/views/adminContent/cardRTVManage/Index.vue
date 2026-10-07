<template>
  <div class="card-rtv-manage flex h-full flex-col">
    <!-- 顶部：卡面信息 + 操作栏 -->
    <el-card class="mb-4 shrink-0" shadow="never">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <el-button @click="handleBack">← 返回</el-button>
          <div>
            <div class="text-xs text-slate-400">RTV 管理</div>
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

      <el-empty v-if="!loading && !list.length" description="暂无 RTV 话数" />
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑 RTV' : '新增 RTV'"
      width="480px"
      @closed="handleDialogClosed"
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
  createCardRtvAPI,
  deleteCardRtvAPI,
  listCardRtvByCardAPI,
  updateCardRtvAPI,
} from '@/api/cardRtv'
import { SOURCE_TYPE } from '@/constants'
import type { CardVO } from '@/types/card'
import type { CardRtvDTO, CardRtvVO } from '@/types/cardRtv'

const route = useRoute()
const router = useRouter()

/** 卡面ID，从 query 里取 */
const cardId = computed(() => Number(route.query.cardId ?? 0))

const loading = ref(false)
const list = ref<CardRtvVO[]>([])
const cardInfo = ref<CardVO>({})

/* -------- 数据加载 -------- */
async function loadList() {
  if (!cardId.value) return
  loading.value = true
  try {
    const res = await listCardRtvByCardAPI(cardId.value)
    list.value = res.data ?? []
  } catch {
    ElMessage.error('加载 RTV 列表失败')
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
    // 忽略：卡面信息只用于展示
  }
}

/* -------- 弹窗 & 表单 -------- */
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<CardRtvDTO & { id?: number }>({
  id: undefined,
  cardId: undefined,
  episodeNo: undefined,
  title: '',
})

const rules: FormRules = {
  episodeNo: [{ required: true, message: '请输入话数', trigger: 'blur' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}

function handleCreate() {
  form.id = undefined
  form.cardId = cardId.value
  form.episodeNo = (list.value.at(-1)?.episodeNo ?? 0) + 1
  form.title = ''
  dialogVisible.value = true
}

function handleEdit(row: CardRtvVO) {
  form.id = row.id
  form.cardId = row.cardId
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
      await updateCardRtvAPI(form.id, {
        cardId: form.cardId,
        episodeNo: form.episodeNo,
        title: form.title,
      })
      ElMessage.success('更新成功')
    } else {
      await createCardRtvAPI({
        cardId: cardId.value,
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

async function handleDelete(row: CardRtvVO) {
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
  await deleteCardRtvAPI(row.id!)
  ElMessage.success('删除成功')
  await loadList()
}

/**
 * 跳转到对话编辑页
 */
function handleManageDialogue(row: CardRtvVO) {
  ElMessage.info('对话编辑入口待接入，请使用对应的对话编辑页面')
  router.push({
    name: 'UserCardRtvEdit',
    query: { sourceType: SOURCE_TYPE.RTV, sourceId: String(row.id) },
  })
}

function handleBack() {
  router.push({ name: 'AdminCardManage' })
}

/* -------- 初始化 -------- */
onMounted(() => {
  if (!cardId.value) {
    ElMessage.error('缺少卡面ID')
    handleBack()
    return
  }
  void loadCardInfo()
  void loadList()
})
</script>
