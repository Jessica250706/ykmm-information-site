<template>
  <div>
    <div class="text-sm font-medium mb-2">图片列表</div>

    <ImageUpload
      v-model="imageUrls"
      :max-count="50"
      :max-size="10"
      tip="支持多图上传，顺序即为展示顺序"
      multiple
    />

    <div class="mt-2 flex justify-center">
      <el-button :loading="imageSaving" type="primary" @click="saveImages">保存图片</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import { useImageUpload } from '../composables/useImageUpload'

const props = defineProps<{
  currentVersion: DialogueVersionVO | null
}>()

const emit = defineEmits<{
  refresh: []
}>()

const currentVersionRef = computed(() => props.currentVersion)

const { imageUrls, imageSaving, saveImages: saveImagesRaw } = useImageUpload(currentVersionRef)

async function saveImages() {
  const ok = await saveImagesRaw()
  if (ok) emit('refresh')
}
</script>
