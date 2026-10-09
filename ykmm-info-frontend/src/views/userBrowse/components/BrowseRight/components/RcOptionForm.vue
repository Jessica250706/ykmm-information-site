<template>
  <div>
    <el-form label-width="70px" size="default">
      <el-form-item label="选项编号">
        <el-input-number
          v-model="pair.optionNumber"
          :controls="false"
          :min="1"
          placeholder="如 1、2、3"
          style="width: 100%"
        />
      </el-form-item>

      <div class="mb-2 text-xs font-medium text-slate-500">问句</div>
      <el-form-item label="说话人">
        <el-cascader
          v-model="pair.question.speakerId"
          :options="roleCascaderOptions"
          :props="cascaderProps"
          :show-all-levels="false"
          class="w-full"
          placeholder="请选择角色"
          clearable
          filterable
        />
      </el-form-item>
      <el-form-item label="内容">
        <el-input
          v-model="pair.question.content"
          :rows="3"
          placeholder="请输入问句内容"
          type="textarea"
        />
      </el-form-item>

      <div class="mb-2 text-xs font-medium text-slate-500">回答</div>
      <el-form-item label="说话人">
        <el-cascader
          v-model="pair.answer.speakerId"
          :options="roleCascaderOptions"
          :props="cascaderProps"
          :show-all-levels="false"
          class="w-full"
          placeholder="请选择角色"
          clearable
          filterable
        />
      </el-form-item>
      <el-form-item label="内容">
        <el-input
          v-model="pair.answer.content"
          :rows="3"
          placeholder="请输入回答内容"
          type="textarea"
        />
      </el-form-item>
    </el-form>

    <div class="flex justify-between">
      <div class="flex items-center gap-1">
        <el-button @click="emit('cancel')">取消</el-button>
        <el-button
          v-if="showDelete"
          :loading="deleting"
          type="danger"
          plain
          @click="emit('delete')"
        >
          删除选项
        </el-button>
      </div>
      <el-button :loading="saving" type="primary" @click="emit('save')">保存选项</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RoleGroupVO } from '@/types/role'
import type { RcPairForm } from '../composables/useEditorForm'

/** 双向绑定的 RC 选项对 */
const pair = defineModel<RcPairForm>('pair', { required: true })

const props = defineProps<{
  roleOptions: RoleGroupVO[]
  saving?: boolean
  deleting?: boolean
  showDelete?: boolean
}>()

const emit = defineEmits<{
  save: []
  cancel: []
  delete: []
}>()

const cascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  emitPath: false,
  checkStrictly: false,
} as const

const roleCascaderOptions = computed(() =>
  props.roleOptions.map((g) => ({
    value: `person:${g.personId ?? 'other'}`,
    label: g.personName ?? '未分组',
    disabled: false,
    children: (g.roles ?? []).map((r) => ({
      value: r.id!,
      label: r.name ?? '',
    })),
  })),
)
</script>
