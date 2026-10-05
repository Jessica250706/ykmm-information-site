import { ref, type Ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFileAPI } from '@/api/common'
import { batchSaveDialogueImagesAPI } from '@/api/dialogueImage'
import type { DialogueImageDTO } from '@/types/dialogueImage'
import type { DialogueVersionVO } from '@/types/dialogueVersion'
import type { UploadFile, UploadRequestOptions, UploadUserFile } from 'element-plus'

export function useImageUpload(currentVersion: Ref<DialogueVersionVO | null>) {
  const imageFileList = ref<UploadUserFile[]>([])
  const imageSaving = ref(false)

  watch(
    () => [currentVersion.value?.id, currentVersion.value?.images],
    () => {
      const imgs = currentVersion.value?.images ?? []
      imageFileList.value = imgs.map((img) => ({
        name: `image-${img.id}`,
        url: img.url,
        uid: img.id,
        status: 'success',
      }))
    },
    { immediate: true },
  )

  async function handleImageUpload(options: UploadRequestOptions) {
    const { file } = options
    const res = await uploadFileAPI(file)
    return { url: res.data }
  }

  function handleImageRemove(file: UploadFile) {
    imageFileList.value = imageFileList.value.filter((f) => f.uid !== file.uid)
  }

  /** 返回 true 表示保存成功 */
  async function saveImages(): Promise<boolean> {
    const versionId = currentVersion.value?.id
    if (!versionId) return false
    imageSaving.value = true
    try {
      const images: DialogueImageDTO[] = imageFileList.value
        .filter((f) => f.url)
        .map((f, i) => ({
          url: f.url!,
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
    imageFileList,
    imageSaving,
    handleImageUpload,
    handleImageRemove,
    saveImages,
  }
}
