import { computed, ref, type Ref, watch } from 'vue'
import { listUserStoryCategoryTreeAPI } from '@/api/story'
import type { StoryCategoryTypeValue } from '@/constants'
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
  storyCategoryId: Ref<number | null>,
) {
  const categoryTree = ref<StoryCategoryVO[]>([])
  const expandedKeys = ref<number[]>([])

  const currentHighlightId = computed(() => {
    // story 模式：高亮它所属的分类
    if (kind.value === 'story') return storyCategoryId.value
    // category 模式：高亮当前节点
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
  }

  /** 找到某个分类的祖先 id 链（含自身） */
  function findAncestorPath(list: StoryCategoryVO[], id: number): number[] | null {
    for (const n of list) {
      if (n.id === id) return [n.id]
      if (n.children?.length && n.id) {
        const sub = findAncestorPath(n.children, id)
        if (sub) return [n.id, ...sub]
      }
    }
    return null
  }

  /** story 模式下，把它的分类路径展开 */
  watch(
    () => [kind.value, storyCategoryId.value] as const,
    ([k, id]) => {
      if (k !== 'story' || id == null) return
      const path = findAncestorPath(categoryTree.value, id)
      if (!path) return
      // 合并去重
      const set = new Set([...expandedKeys.value, ...path])
      expandedKeys.value = Array.from(set)
    },
  )

  return {
    categoryTree,
    expandedKeys,
    currentHighlightId,
    currentCategory,
    loadTree,
    findCategory,
  }
}
