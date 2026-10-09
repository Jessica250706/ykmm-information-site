<template>
  <div>
    <el-form label-width="70px" size="default">
      <el-form-item label="说话人">
        <el-cascader
          v-model="form.speakerId"
          :options="roleCascaderOptions"
          :props="cascaderProps"
          :show-all-levels="false"
          class="w-full"
          placeholder="请选择角色"
          clearable
          filterable
        />
      </el-form-item>
      <el-form-item v-if="showMonologue" label="内心独白">
        <el-switch
          v-model="form.monologue"
          :active-value="MONOLOGUE.INNER"
          :inactive-value="MONOLOGUE.SPOKEN"
          active-text="内心独白"
          inactive-text="说出来的话"
          inline-prompt
        />
      </el-form-item>
      <el-form-item label="内容">
        <el-input
          v-model="form.content"
          :rows="4"
          placeholder="请输入对话内容，可含表情包标签，如 [国王布丁表情包]"
          type="textarea"
        />
      </el-form-item>
    </el-form>

    <div class="flex justify-between">
      <div class="flex items-center gap-1">
        <template v-if="editorMode === EDITOR_MODE.EDIT">
          <el-button @click="emit('add')">+ 新增</el-button>
          <el-button v-if="showAddRcOption" @click="emit('addRcOption')">+ 新增选项</el-button>
          <el-button
            v-if="hasEditingLine"
            :loading="deleting"
            type="danger"
            plain
            @click="emit('delete')"
          >
            删除
          </el-button>
        </template>
        <template v-else>
          <el-button @click="emit('cancelCreate')">取消新增</el-button>
          <el-button plain @click="emit('addAtStart')">↑ 追加到开头</el-button>
        </template>
      </div>
      <el-button
        :disabled="!form.id && editorMode === EDITOR_MODE.EDIT"
        :loading="saving"
        type="primary"
        @click="emit('save')"
      >
        {{ editorMode === EDITOR_MODE.CREATE ? '创建' : '保存' }}
      </el-button>
    </div>

    <div
      v-if="editorMode === EDITOR_MODE.EDIT && !hasEditingLine"
      class="mt-2 text-xs text-slate-400"
    >
      点击左侧某一句对话可加载到编辑器；未选中时新增将追加到末尾。
    </div>
    <div v-else-if="editorMode === EDITOR_MODE.CREATE" class="mt-2 text-xs text-slate-400">
      填写完成后点击"创建"，将作为新对话插入。
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  EDITOR_MODE,
  type EditorMode,
  MONOLOGUE,
  SOURCE_TYPE,
  type SourceTypeValue,
} from '@/constants'
import type { RoleGroupVO } from '@/types/role'
import type { EditorForm } from '../composables/useEditorForm'

/**
 * 双向绑定的普通行表单。
 * 父组件：<NormalLineForm v-model:form="editorForm" ... />
 */
const form = defineModel<EditorForm>('form', { required: true })

const props = defineProps<{
  roleOptions: RoleGroupVO[]
  sourceType: SourceTypeValue
  editorMode: EditorMode
  hasEditingLine?: boolean
  showAddRcOption?: boolean
  saving?: boolean
  deleting?: boolean
}>()

const emit = defineEmits<{
  save: []
  add: []
  addAtStart: []
  addRcOption: []
  cancelCreate: []
  delete: []
}>()

/** 是否显示"内心独白"：剧情和 RTV */
const showMonologue = computed(
  () => props.sourceType === SOURCE_TYPE.STORY || props.sourceType === SOURCE_TYPE.RTV,
)

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
