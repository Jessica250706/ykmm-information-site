import { useRouter } from 'vue-router'

export function useBrowseNavigation() {
  const router = useRouter()

  function goCategory(type: number, id: number) {
    router.push({
      name: 'UserStoryBrowseDetail',
      params: { type: String(type), kind: 'category', id: String(id) },
    })
  }

  function goStory(type: number, id: number) {
    router.push({
      name: 'UserStoryBrowseDetail',
      params: { type: String(type), kind: 'story', id: String(id) },
    })
  }

  function goBack(type: number, categoryId: number) {
    goCategory(type, categoryId)
  }

  function handleSwitchType(typeId: number) {
    router.push({ name: 'UserStoryBrowse', params: { type: String(typeId) } })
  }

  return { goCategory, goStory, goBack, handleSwitchType }
}
