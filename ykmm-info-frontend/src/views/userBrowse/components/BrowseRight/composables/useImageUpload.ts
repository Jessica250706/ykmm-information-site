import { ref, type Ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { batchSaveDialogueImagesAPI } from '@/api/dialogueImage'
import type { DialogueImageDTO } from '@/types/dialogueImage'
import type { DialogueVersionVO } from '@/types/dialogueVersion'

export function useImageUpload(currentVersion: Ref<DialogueVersionVO | null>) {
  /** 当前版本的图片 URL 列表，双向绑定给 ImageUpload */
  const imageUrls = ref<string[]>([])
  const imageSaving = ref(false)

  /** 版本变化时，从 currentVersion.images 同步 URL 列表 */
  watch(
    () => [currentVersion.value?.id, currentVersion.value?.images],
    () => {
      const imgs = currentVersion.value?.images ?? []
      imageUrls.value = imgs.map((img) => img.url).filter((url): url is string => !!url)
    },
    { immediate: true },
  )

  /** 把当前 URL 列表保存到后端 */
  async function saveImages(): Promise<boolean> {
    const versionId = currentVersion.value?.id
    if (!versionId) return false

    imageSaving.value = true
    try {
      const images: DialogueImageDTO[] = imageUrls.value
        .filter((url) => !!url)
        .map((url, i) => ({
          url,
          sort: i + 1,
        }))
      await batchSaveDialogueImagesAPI(versionId, images)
      ElMessage.success('图片保存成功')
      return true
    } catch {
      return false
    } finally {
      imageSaving.value = false
    }
  }

  return {
    imageUrls,
    imageSaving,
    saveImages,
  }
}
