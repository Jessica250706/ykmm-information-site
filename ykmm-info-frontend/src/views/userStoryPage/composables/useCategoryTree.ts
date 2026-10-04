import { computed, ref, type Ref } from 'vue'
import { listUserStoryCategoryTreeAPI } from '@/api/story'
import type { StoryCategoryTypeValue } from '@/constants/story'
import type { StoryCategoryVO } from '@/types/storyCategory'

function collectIds(list: StoryCategoryVO[]): number[] {
  const ids: number[] = []
  const walk = (arr: StoryCategoryVO[]) => {
    arr.forEach((n) => {
      if (n.id != null) ids.push(n.id)
      if (n.children?.length) walk(n.children)
    })
  }
  walk(list)
  return ids
}

function findCategory(list: StoryCategoryVO[], id: number): StoryCategoryVO | null {
  for (const n of list) {
    if (n.id === id) return n
    if (n.children?.length) {
      const found = findCategory(n.children, id)
      if (found) return found
    }
  }
  return null
}

export function useCategoryTree(
  type: Ref<StoryCategoryTypeValue>,
  kind: Ref<string | null>,
  nodeId: Ref<number | null>,
) {
  const categoryTree = ref<StoryCategoryVO[]>([])
  const expandedKeys = ref<number[]>([])

  const currentHighlightId = computed(() => {
    if (kind.value === 'category') return nodeId.value
    return null
  })

  const currentCategory = computed(() => {
    if (kind.value !== 'category' || nodeId.value == null) return null
    return findCategory(categoryTree.value, nodeId.value)
  })

  async function loadTree() {
    const res = await listUserStoryCategoryTreeAPI({ categoryType: type.value })
    categoryTree.value = res.data ?? []
    expandedKeys.value = collectIds(categoryTree.value).slice(0, 5)
  }

  return {
    categoryTree,
    expandedKeys,
    currentHighlightId,
    currentCategory,
    loadTree,
    findCategory,
  }
}
