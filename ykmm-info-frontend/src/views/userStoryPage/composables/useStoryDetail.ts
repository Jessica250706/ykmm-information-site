import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listDialogueVersionOptionsAPI } from '@/api/dialogueVersion'
import { getUserStoryDetailAPI, pageUserStoryAPI } from '@/api/story'
import { SOURCE_TYPE } from '@/constants'
import { STORY_STATUS } from '@/constants/story'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO, StoryVO } from '@/types/story'

export function useStoryDetail() {
  const storyDetail = ref<StoryDetailVO | null>(null)
  const stories = ref<StoryVO[]>([])
  const versionOptions = ref<DialogueVersionOptionVO[] | null>(null)

  const loadingContent = ref(false)
  const loadingStories = ref(false)

  function resetStory() {
    storyDetail.value = null
    stories.value = []
  }

  async function fetchStoryDetail(id: number): Promise<StoryDetailVO | null> {
    loadingContent.value = true
    try {
      const res = await getUserStoryDetailAPI(id)
      storyDetail.value = res.data ?? null
      return storyDetail.value
    } catch {
      ElMessage.error('剧情加载失败')
      return null
    } finally {
      loadingContent.value = false
    }
  }

  async function fetchStories(categoryId: number) {
    loadingStories.value = true
    try {
      const res = await pageUserStoryAPI({
        categoryId,
        status: STORY_STATUS.PUBLISHED,
        pageNum: 1,
        pageSize: 50,
      })
      stories.value = res.data.records ?? []
    } catch {
      ElMessage.error('剧情加载失败')
    } finally {
      loadingStories.value = false
    }
  }

  async function fetchVersionOptions() {
    if (!storyDetail.value?.id) {
      ElMessage.error('当前故事不存在')
      return
    }
    const res = await listDialogueVersionOptionsAPI(SOURCE_TYPE.STORY, storyDetail.value.id)
    versionOptions.value = res.data
  }

  return {
    storyDetail,
    stories,
    versionOptions,
    loadingContent,
    loadingStories,
    resetStory,
    fetchStoryDetail,
    fetchStories,
    fetchVersionOptions,
  }
}
