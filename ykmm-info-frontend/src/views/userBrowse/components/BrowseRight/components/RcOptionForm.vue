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
        <div class="w-full">
          <el-input
            ref="questionInputRef"
            v-model="pair.question.content"
            :rows="3"
            placeholder="请输入问句内容"
            type="textarea"
          />
          <div class="mt-1 flex justify-end">
            <StickerPicker @pick="(text) => handlePick('question', text)" />
          </div>
        </div>
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
        <div class="w-full">
          <el-input
            ref="answerInputRef"
            v-model="pair.answer.content"
            :rows="3"
            placeholder="请输入回答内容"
            type="textarea"
          />
          <div class="mt-1 flex justify-end">
            <StickerPicker @pick="(text) => handlePick('answer', text)" />
          </div>
        </div>
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
import { computed, nextTick, ref } from 'vue'
import { ElInput } from 'element-plus'
import type { RoleGroupVO } from '@/types/role'
import StickerPicker from './StickerPicker.vue'
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

/* -------- 表情包插入 -------- */

const questionInputRef = ref<InstanceType<typeof ElInput>>()
const answerInputRef = ref<InstanceType<typeof ElInput>>()

/**
 * 在问句 / 回答输入框光标处插入表情包标签。
 *
 * @param target 目标输入框：question / answer
 * @param text   带方括号的标签文本
 */
function handlePick(target: 'question' | 'answer', text: string) {
  const inputRef = target === 'question' ? questionInputRef : answerInputRef
  const model = target === 'question' ? pair.value.question : pair.value.answer
  const textarea = inputRef.value?.textarea
  const content = model.content ?? ''

  if (!textarea) {
    model.content = content + text
    return
  }

  const start = textarea.selectionStart ?? content.length
  const end = textarea.selectionEnd ?? start
  model.content = content.slice(0, start) + text + content.slice(end)

  void nextTick(() => {
    textarea.focus()
    const pos = start + text.length
    textarea.setSelectionRange(pos, pos)
  })
}
</script>
