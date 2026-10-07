<template>
  <div class="card-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑卡面' : '新增卡面' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-3xl" label-width="120px">
        <!-- -------- 基本信息 -------- -->
        <el-form-item label="卡面名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入卡面名称" />
        </el-form-item>

        <el-form-item label="所属系列" prop="seriesId">
          <el-select
            v-model="form.seriesId"
            placeholder="选择或输入新系列名"
            style="width: 100%"
            allow-create
            default-first-option
            filterable
            @change="handleSeriesChange"
          >
            <el-option v-for="s in seriesOptions" :key="s.id" :label="s.name" :value="s.id!" />
          </el-select>
          <div class="mt-1 text-xs text-slate-400">提示：输入不存在的系列名后回车，会自动创建</div>
        </el-form-item>

        <el-form-item label="最高等级" prop="maxRarity">
          <el-radio-group v-model="form.maxRarity">
            <el-radio v-for="opt in CARD_MAX_RARITY_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="属性" prop="attribute">
          <el-radio-group v-model="form.attribute">
            <el-radio v-for="opt in CARD_ATTRIBUTE_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="首次入池时间" prop="firstPoolTime">
          <el-date-picker
            v-model="form.firstPoolTime"
            placeholder="可为空，表示未知"
            style="width: 240px"
            type="date"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="关联人物" prop="personIds">
          <el-select
            v-model="form.personIds"
            placeholder="请选择卡面包含的人物"
            style="width: 100%"
            filterable
            multiple
          >
            <el-option
              v-for="p in personStore.persons"
              :key="p.id"
              :label="p.nameCn || p.nameJp || `#${p.id}`"
              :value="p.id!"
            />
          </el-select>
        </el-form-item>

        <!-- -------- 附属剧情 -------- -->
        <el-form-item label="附属剧情" prop="attachedStoryType">
          <el-radio-group v-model="form.attachedStoryType">
            <el-radio
              v-for="opt in CARD_ATTACHED_STORY_TYPE_OPTIONS"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- -------- 服装 -------- -->
        <el-form-item label="服装类型" prop="costumeType">
          <el-radio-group v-model="form.costumeType">
            <el-radio v-for="opt in CARD_COSTUME_TYPE_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="魅力技能" prop="skillDesc">
          <el-input
            v-model="form.skillDesc"
            :rows="3"
            placeholder="请输入魅力技能描述"
            type="textarea"
          />
        </el-form-item>

        <!-- -------- 卡面图片 -------- -->
        <el-divider content-position="left">卡面图片</el-divider>

        <div v-for="type in CARD_IMAGE_TYPE_OPTIONS" :key="type.value" class="mb-4">
          <div class="mb-2 flex items-center gap-2">
            <el-tag effect="plain" type="info">{{ type.label }}</el-tag>
            <span class="text-xs text-slate-400">{{ imageTypeHint(type.value) }}</span>
          </div>
          <ImageUpload v-model="imageMap[type.value]" :max-size="10" />
        </div>
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
import { createCardAPI, getCardDetailAPI, updateCardAPI } from '@/api/card'
import { findOrCreateCardSeriesAPI, listCardSeriesOptionsAPI } from '@/api/cardSeries'
import {
  CARD_ATTACHED_STORY_TYPE_OPTIONS,
  CARD_ATTRIBUTE_OPTIONS,
  CARD_COSTUME_TYPE_OPTIONS,
  CARD_IMAGE_TYPE,
  CARD_IMAGE_TYPE_OPTIONS,
  CARD_MAX_RARITY,
  CARD_MAX_RARITY_OPTIONS,
} from '@/constants/card'
import { usePersonStore } from '@/stores/personStore'
import type { CardDTO } from '@/types/card'
import type { CardSeriesVO } from '@/types/cardSeries'

const route = useRoute()
const router = useRouter()
const personStore = usePersonStore()

const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<CardDTO>({
  name: '',
  seriesId: undefined,
  maxRarity: CARD_MAX_RARITY.SSR,
  firstPoolTime: undefined,
  attribute: undefined,
  skillDesc: '',
  attachedStoryType: undefined,
  costumeType: undefined,
  personIds: [],
})

/** 每种图片类型独立的 URL 列表 */
/** 每种图片类型对应的单张 URL */
const imageMap = reactive<Record<number, string>>({
  [CARD_IMAGE_TYPE.R]: '',
  [CARD_IMAGE_TYPE.SR]: '',
  [CARD_IMAGE_TYPE.SSR]: '',
  [CARD_IMAGE_TYPE.SSR_HIDDEN]: '',
  [CARD_IMAGE_TYPE.UR_VERTICAL]: '',
  [CARD_IMAGE_TYPE.UR_HORIZONTAL]: '',
})

const rules: FormRules<CardDTO> = {
  name: [{ required: true, message: '请输入卡面名称', trigger: 'blur' }],
  maxRarity: [{ required: true, message: '请选择最高等级', trigger: 'change' }],
}

/** 图片类型提示文案 */
function imageTypeHint(type: number): string {
  switch (type) {
    case CARD_IMAGE_TYPE.SSR_HIDDEN:
      return 'SSR 的隐藏款，仅特殊情况展示'
    case CARD_IMAGE_TYPE.UR_VERTICAL:
      return 'UR 竖卡，详情页封面优先展示'
    case CARD_IMAGE_TYPE.UR_HORIZONTAL:
      return 'UR 横卡，详情页可横向浏览'
    default:
      return ''
  }
}

/* -------- 系列下拉 -------- */
const seriesOptions = ref<CardSeriesVO[]>([])

async function loadSeriesOptions() {
  const res = await listCardSeriesOptionsAPI()
  seriesOptions.value = res.data ?? []
}

/**
 * allow-create 输入了新系列名 → 自动创建并绑定
 */
async function handleSeriesChange(val: number | string) {
  // 数字 = 已存在系列ID，直接接受
  if (typeof val === 'number') return
  // 字符串 = 用户输入的新系列名
  if (typeof val === 'string' && val.trim()) {
    try {
      const res = await findOrCreateCardSeriesAPI(val.trim())
      const newId = res.data
      if (newId != null) {
        form.seriesId = newId
        await loadSeriesOptions()
      }
    } catch {
      ElMessage.error('创建系列失败')
    }
  }
}

/* -------- 加载详情 -------- */
async function loadDetail() {
  if (!editId.value) return
  const res = await getCardDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('卡面不存在')
    handleBack()
    return
  }

  form.name = data.name ?? ''
  form.seriesId = data.seriesId ?? undefined
  form.maxRarity = data.maxRarity ?? CARD_MAX_RARITY.SSR
  form.firstPoolTime = data.firstPoolTime
  form.attribute = data.attribute
  form.skillDesc = data.skillDesc ?? ''
  form.attachedStoryType = data.attachedStoryType
  form.costumeType = data.costumeType
  form.personIds = (data.persons ?? []).map((p) => p.personId!).filter((id) => id != null)

  // 清空后按类型填一张
  Object.keys(imageMap).forEach((k) => {
    imageMap[Number(k)] = ''
  })
  for (const img of data.images ?? []) {
    if (img.imageType != null && img.url) {
      // 每种类型只取第一张，后续忽略
      if (!imageMap[img.imageType]) {
        imageMap[img.imageType] = img.url
      }
    }
  }
}

/* -------- 提交 -------- */
async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 组装图片列表：每种类型最多一张
  const images: CardDTO['images'] = []
  for (const type of Object.keys(imageMap).map(Number)) {
    const url = imageMap[type]
    if (!url) continue
    images.push({
      imageType: type,
      url,
      sort: 1,
    })
  }

  const payload: CardDTO = {
    ...form,
    personIds: (form.personIds ?? []).filter((id) => id != null),
    images,
  }

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updateCardAPI(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createCardAPI(payload)
      ElMessage.success('新增成功')
    }
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push({ name: 'AdminCardManage' })
}

/* -------- 初始化 -------- */
onMounted(async () => {
  await Promise.all([personStore.loadAll(), loadSeriesOptions()])
  if (isEdit.value) {
    await loadDetail()
  }
})
</script>
