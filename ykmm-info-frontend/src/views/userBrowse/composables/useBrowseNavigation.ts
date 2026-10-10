import { useRouter } from 'vue-router'
import { BROWSE_KIND, UserRouteName } from '@/constants'

export function useBrowseNavigation() {
  const router = useRouter()

  function goCategory(type: number, id: number) {
    router.push({
      name: UserRouteName.STORY_BROWSE_DETAIL,
      params: { type: String(type), kind: BROWSE_KIND.CATEGORY, id: String(id) },
    })
  }

  function goStory(type: number, id: number) {
    router.push({
      name: UserRouteName.STORY_BROWSE_DETAIL,
      params: { type: String(type), kind: BROWSE_KIND.STORY, id: String(id) },
    })
  }

  function goBack(type: number, categoryId: number) {
    goCategory(type, categoryId)
  }

  function handleSwitchType(typeId: number) {
    router.push({ name: UserRouteName.STORY_BROWSE, params: { type: String(typeId) } })
  }

  return { goCategory, goStory, goBack, handleSwitchType }
}
