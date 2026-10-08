import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { STORY_CATEGORY_TYPE_LABEL, type StoryCategoryTypeValue } from '@/constants'

export function useBrowseRoute() {
  const route = useRoute()

  const type = computed(() => Number(route.params.type) as StoryCategoryTypeValue)
  const kind = computed(() => (route.params.kind as string) ?? null)
  const nodeId = computed(() => (route.params.id ? Number(route.params.id) : null))

  const typeLabel = computed(
    () => STORY_CATEGORY_TYPE_LABEL[type.value as StoryCategoryTypeValue] ?? '剧情',
  )

  return { type, kind, nodeId, typeLabel }
}
