import { ref, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { importDialogueTxtAPI, parseDialogueTxtAPI } from '@/api/dialogueTxt'
import type { DialogueVersionVO } from '@/types/dialogueVersion'

export function useTxtUpload(currentVersion: Ref<DialogueVersionVO | null>) {
  const txtInputRef = ref<HTMLInputElement | null>(null)

  async function onTxtUploadClick() {
    try {
      await ElMessageBox.confirm(
        '如果上传 txt，会自动覆盖当前故事的所有对话信息。是否确认上传？',
        '确认上传',
        { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' },
      )
      txtInputRef.value?.click()
    } catch {
      // 用户取消
    }
  }

  /** 返回 true 表示导入成功，调用方据此决定是否 refresh */
  async function onTxtFileChange(e: Event): Promise<boolean> {
    const target = e.target as HTMLInputElement
    const file = target.files?.[0]
    if (!file) return false
    const versionId = currentVersion.value?.id
    if (!versionId) return false

    try {
      const parseRes = await parseDialogueTxtAPI(versionId, file)
      const parsed = parseRes.data
      if (!parsed?.lines?.length) {
        ElMessage.warning('未解析到有效内容')
        return false
      }
      if (parsed.unmatchedSpeakers?.length) {
        await ElMessageBox.confirm(
          `以下说话人未匹配到角色：\n${parsed.unmatchedSpeakers.join('、')}\n\n仍要导入吗？`,
          '提示',
          { type: 'warning' },
        )
      }
      await importDialogueTxtAPI(versionId, parsed.lines)
      ElMessage.success('导入成功')
      return true
    } catch {
      return false
    } finally {
      target.value = ''
    }
  }

  return {
    txtInputRef,
    onTxtUploadClick,
    onTxtFileChange,
  }
}
