<template>
  <div class="person-edit flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <div class="flex items-center justify-between">
          <span class="font-medium">{{ isEdit ? '编辑人物' : '新增人物' }}</span>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" class="max-w-3xl" label-width="100px">
        <!-- -------- 基本类型 -------- -->
        <el-form-item label="类型" prop="personType">
          <el-radio-group v-model="form.personType" @change="handlePersonTypeChange">
            <el-radio v-for="opt in PERSON_TYPE_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- -------- 名称 -------- -->
        <el-form-item label="中文名" prop="nameCn">
          <el-input v-model="form.nameCn" placeholder="请输入中文名" />
        </el-form-item>

        <el-form-item label="日文名" prop="nameJp">
          <el-input v-model="form.nameJp" placeholder="请输入日文名" />
        </el-form-item>

        <el-form-item label="罗马音" prop="nameRomaji">
          <el-input v-model="form.nameRomaji" placeholder="请输入罗马音" />
        </el-form-item>

        <el-form-item label="声优" prop="cv">
          <el-input v-model="form.cv" placeholder="请输入声优" />
        </el-form-item>

        <el-form-item label="代表符号" prop="symbol">
          <el-input v-model="form.symbol" placeholder="如：★" style="width: 120px" />
        </el-form-item>

        <!-- -------- 外观 -------- -->
        <el-form-item label="头像" prop="avatar">
          <ImageUpload v-model="form.avatar" :max-size="5" tip="建议正方形，大小 ≤ 5MB" />
        </el-form-item>

        <el-form-item label="应援色" prop="themeColor">
          <el-color-picker v-model="form.themeColor" show-alpha />
          <span class="ml-2 text-sm text-slate-500">{{ form.themeColor || '未选择' }}</span>
        </el-form-item>

        <el-form-item label="展示图片" prop="images">
          <ImageUpload
            v-model="form.images"
            :max-count="10"
            :max-size="5"
            tip="最多 10 张，单张 ≤ 5MB"
            multiple
          />
        </el-form-item>

        <!-- -------- 基本资料 -------- -->
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="form.birthday"
            format="MM-DD"
            placeholder="请选择生日"
            style="width: 200px"
            type="date"
            value-format="MM-DD"
          />
        </el-form-item>

        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :max="200" :min="0" :precision="0" />
        </el-form-item>

        <el-form-item label="血型" prop="bloodType">
          <el-select v-model="form.bloodType" placeholder="请选择" style="width: 200px" clearable>
            <el-option
              v-for="opt in BLOOD_TYPE_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="身高(cm)" prop="height">
          <el-input-number v-model="form.height" :max="300" :min="0" :precision="0" />
        </el-form-item>

        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="form.weight" :max="500" :min="0" :precision="1" />
        </el-form-item>

        <el-form-item label="鞋码" prop="shoeSize">
          <el-input-number v-model="form.shoeSize" :max="100" :min="0" :precision="1" />
        </el-form-item>

        <!-- -------- 所属（根据类型切换） -------- -->
        <el-form-item v-if="form.personType === PERSON_TYPE.IDOL" label="所属团体" prop="groupIds">
          <el-select
            v-model="form.groupIds"
            :multiple-limit="2"
            placeholder="请选择所属团体"
            style="width: 100%"
            filterable
            multiple
          >
            <el-option
              v-for="g in idolGroupStore.idolGroups"
              :key="g.id"
              :label="g.name"
              :value="g.id!"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          v-else-if="form.personType === PERSON_TYPE.AGENT"
          label="所属公司"
          prop="agencyIds"
        >
          <el-select
            v-model="form.agencyIds"
            :multiple-limit="1"
            placeholder="请选择所属公司"
            style="width: 100%"
            filterable
            multiple
          >
            <el-option
              v-for="a in agencyStore.agencies"
              :key="a.id"
              :label="a.name"
              :value="a.id!"
            />
          </el-select>
        </el-form-item>

        <!-- -------- 文字介绍 -------- -->
        <el-form-item label="喜欢的事物" prop="likes">
          <el-input v-model="form.likes" :rows="2" placeholder="喜欢的事物" type="textarea" />
        </el-form-item>

        <el-form-item label="不擅长的事" prop="dislikes">
          <el-input v-model="form.dislikes" :rows="2" placeholder="不擅长的事物" type="textarea" />
        </el-form-item>

        <el-form-item label="角色简介" prop="intro">
          <el-input v-model="form.intro" :rows="4" placeholder="角色简介" type="textarea" />
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
import { createPersonAPI, getPersonDetailAPI, updatePersonAPI } from '@/api/person'
import {
  BLOOD_TYPE_OPTIONS,
  PERSON_TYPE,
  PERSON_TYPE_OPTIONS,
  type PersonTypeValue,
} from '@/constants/person'
import { useAgencyStore } from '@/stores/agencyStore'
import { useIdolGroupStore } from '@/stores/idolGroupStore'
import type { PersonDTO } from '@/types/person'

const route = useRoute()
const router = useRouter()
const agencyStore = useAgencyStore()
const idolGroupStore = useIdolGroupStore()

/* -------- 模式 -------- */
const isEdit = computed(() => !!route.params.id)
const editId = computed(() => (route.params.id ? Number(route.params.id) : null))

/* -------- 表单 -------- */
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<PersonDTO>({
  personType: PERSON_TYPE.IDOL,
  nameCn: '',
  nameJp: '',
  nameRomaji: '',
  cv: '',
  symbol: '',
  avatar: '',
  themeColor: '',
  images: [],
  birthday: '',
  age: undefined,
  bloodType: undefined,
  height: undefined,
  weight: undefined,
  shoeSize: undefined,
  groupIds: [],
  agencyIds: [],
  likes: '',
  dislikes: '',
  intro: '',
})

const rules: FormRules<PersonDTO> = {
  personType: [{ required: true, message: '请选择类型', trigger: 'change' }],
  nameCn: [{ required: true, message: '请输入中文名', trigger: 'blur' }],
}

/* -------- 切换类型：清掉对侧关联 -------- */
function handlePersonTypeChange(val: PersonTypeValue) {
  if (val === PERSON_TYPE.IDOL) {
    form.agencyIds = []
  } else if (val === PERSON_TYPE.AGENT) {
    form.groupIds = []
  }
}

/* -------- 加载详情 -------- */
async function loadDetail() {
  if (!editId.value) return
  const res = await getPersonDetailAPI(editId.value)
  const data = res.data
  if (!data) {
    ElMessage.error('人物不存在')
    handleBack()
    return
  }

  form.personType = (data.personType as PersonTypeValue) ?? PERSON_TYPE.IDOL
  form.nameCn = data.nameCn ?? ''
  form.nameJp = data.nameJp ?? ''
  form.nameRomaji = data.nameRomaji ?? ''
  form.cv = data.cv ?? ''
  form.symbol = data.symbol ?? ''
  form.avatar = data.avatar ?? ''
  form.themeColor = data.themeColor ?? ''
  form.images = data.images ?? []
  form.birthday = data.birthday ?? ''
  form.age = data.age
  form.bloodType = data.bloodType
  form.height = data.height
  form.weight = data.weight
  form.shoeSize = data.shoeSize
  form.likes = data.likes ?? ''
  form.dislikes = data.dislikes ?? ''
  form.intro = data.intro ?? ''

  // 关联 ID
  form.groupIds = (data.groups ?? []).map((g) => g.id!).filter((id) => id != null)
  form.agencyIds = (data.agencies ?? []).map((a) => a.id!).filter((id) => id != null)
}

/* -------- 提交 -------- */
async function handleSubmit() {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 过滤掉空的图片 URL
  const payload: PersonDTO = {
    ...form,
    images: (form.images ?? []).filter((url) => url.trim()),
  }

  loading.value = true
  try {
    if (isEdit.value && editId.value) {
      await updatePersonAPI(editId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createPersonAPI(payload)
      ElMessage.success('新增成功')
    }
    handleBack()
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push({ name: 'AdminPersonManage' })
}

/* -------- 初始化 -------- */
onMounted(async () => {
  // 预加载下拉数据
  await Promise.all([agencyStore.loadAll(), idolGroupStore.loadAll()])

  if (isEdit.value) {
    await loadDetail()
  }
})
</script>
