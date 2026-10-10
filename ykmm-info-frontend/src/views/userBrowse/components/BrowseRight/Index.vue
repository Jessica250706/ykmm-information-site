<template>
  <div class="browse-right-root flex h-full min-h-0 flex-col">
    <el-card
      body-class="flex flex-col h-full overflow-hidden p-4"
      class="w-96 flex-1 min-h-0 shrink-0 flex flex-col border-l bg-white"
    >
      <!-- 顶部：编辑模式开关 -->
      <div class="flex items-center justify-between border-b pb-3 mb-3 shrink-0">
        <div class="font-medium">编辑区</div>
        <el-switch v-model="editingModeLocal" active-text="编辑模式" inline-prompt />
      </div>

      <!-- 1. 未开启编辑模式 -->
      <div v-if="!editingModeLocal" class="flex-1 flex items-center justify-center">
        <el-empty description="开启编辑模式以编辑对话" />
      </div>

      <!-- 2. 无故事 -->
      <div v-else-if="!storyDetail" class="flex-1 flex items-center justify-center">
        <el-empty description="请选择一个剧情" />
      </div>

      <!-- 3. 未选择版本 -->
      <div v-else-if="!currentOptionVersion" class="flex-1 flex items-center justify-center">
        <el-empty description="请选择一个对话版本" />
      </div>

      <!-- 4. 版本无内容 -->
      <div v-else-if="!currentVersion" class="flex-1 flex items-center justify-center">
        <el-empty :description="emptyVersionText">
          <el-button :loading="creatingVersion" type="primary" @click="onCreateVersion">
            创建该版本内容
          </el-button>
        </el-empty>
      </div>

      <!-- 5. 编辑内容 -->
      <div v-else class="flex-1 overflow-auto min-h-0">
        <!-- 图片版本 -->
        <ImageVersionEditor
          v-if="currentVersion.format === VERSION_FORMAT.IMAGE"
          :current-version="currentVersion"
          @refresh="emit('refresh')"
        />

        <!-- 文字版本 -->
        <TextVersionEditor
          v-else-if="currentVersion.format === VERSION_FORMAT.TEXT"
          :all-lines="allLines"
          :current-version="currentVersion"
          :editing-line="editingLine"
          :editing-line-index="editingLineIndex"
          :editing-mode="editingModeLocal"
          :source-type="sourceType"
          @add-line="(payload) => emit('add-line', payload)"
          @clear-line="emit('clear-line')"
          @delete-line="(payload) => emit('delete-line', payload)"
          @refresh="emit('refresh')"
          @save-line="emit('save-line')"
          @select-line="(line) => emit('select-line', line)"
        />
      </div>
    </el-card>

    <!-- 创建版本弹窗 -->
    <CreateVersionDialog
      ref="createVersionDialogRef"
      :confirming="creatingVersion"
      @confirm="handleCreateVersionConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useVModel } from '@vueuse/core'
import { type SourceTypeValue, VERSION_FORMAT } from '@/constants'
import { useUserStore } from '@/stores/userStore'
import type { DialogueLineVO } from '@/types/dialogueLine'
import type { DialogueVersionOptionVO, DialogueVersionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'
import ImageVersionEditor from './components/ImageVersionEditor.vue'
import TextVersionEditor from './components/TextVersionEditor.vue'
import CreateVersionDialog from './CreateVersionDialog.vue'
import type { CreateVersionPayload } from './CreateVersionDialog.vue'

type CreateVersionEmit = {
  option: DialogueVersionOptionVO
  contributorUserId: number | null
  contributorName: string | null
}

const props = defineProps<{
  editingMode: boolean
  storyDetail: StoryDetailVO | null
  currentVersion: DialogueVersionVO | null
  editingLine: DialogueLineVO | null
  editingLineIndex: number
  allLines: DialogueLineVO[]
  pendingInsertAfterId: number | null
  currentOptionVersion: DialogueVersionOptionVO | null
  /** 来源类型：1-RC 2-RTV 3-Rabitter 4-story */
  sourceType: SourceTypeValue
}>()

const emit = defineEmits<{
  'update:editingMode': [val: boolean]
  'save-line': []
  'add-line': [payload: { afterId: number | null; atStart: boolean }]
  refresh: []
  'select-line': [line: DialogueLineVO]
  'create-version': [option: CreateVersionEmit]
  'delete-line': [payload: { deletedId: number; prevId: number | null; nextId: number | null }]
  'clear-line': []
}>()

const editingModeLocal = useVModel(props, 'editingMode', emit, { passive: true })

const userStore = useUserStore()

const emptyVersionText = computed(() => {
  const label = props.currentOptionVersion?.label ?? '该版本'
  return `「${label}」尚未创建内容`
})

const creatingVersion = ref(false)
const createVersionDialogRef = ref<InstanceType<typeof CreateVersionDialog> | null>(null)

function onCreateVersion() {
  const option = props.currentOptionVersion
  if (!option) return
  if (userStore.isAdmin) createVersionDialogRef.value?.open(option)
  else if (userStore.isLogin) {
    emit('create-version', {
      option,
      contributorUserId: null,
      contributorName: null,
    })
  }
}

function handleCreateVersionConfirm(payload: CreateVersionPayload) {
  emit('create-version', {
    option: payload.option,
    contributorUserId: payload.contributorUserId,
    contributorName: payload.contributorName,
  })
}
</script>
