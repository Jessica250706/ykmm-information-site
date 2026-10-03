import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { listStoryCategoryTypeAPI } from '@/api/storyCategoryType'
import type { StoryCategoryTypeVO } from '@/types/storyCategoryType'

export const useStoryCategoryTypeStore = defineStore(
  'storyCategoryType',
  () => {
    /** 全部分类类型 */
    const types = ref<StoryCategoryTypeVO[]>([])
    /** 是否已加载 */
    const loaded = ref(false)

    /** id -> StoryCategoryTypeVO 映射 */
    const typeMap = computed(() => {
      const map = new Map<number, StoryCategoryTypeVO>()
      types.value.forEach((t) => {
        if (t.id != null) map.set(t.id, t)
      })
      return map
    })

    /** id -> name 映射，方便按 id 取名称 */
    const typeNameMap = computed(() => {
      const map = new Map<number, string>()
      types.value.forEach((t) => {
        if (t.id != null) map.set(t.id, t.name ?? '')
      })
      return map
    })

    /** 按 sort 排序后的列表 */
    const sortedTypes = computed(() =>
      [...types.value].sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0) || (a.id ?? 0) - (b.id ?? 0)),
    )

    /** 加载全部（带缓存，可强制刷新） */
    const loadAll = async (force = false) => {
      if (loaded.value && !force) return types.value
      const res = await listStoryCategoryTypeAPI()
      types.value = res.data ?? []
      loaded.value = true
      return types.value
    }

    /** 清空 */
    const clear = () => {
      types.value = []
      loaded.value = false
    }

    /** 根据 id 拿名称 */
    const getTypeName = (id?: number) => {
      if (id == null) return ''
      return typeNameMap.value.get(id) ?? ''
    }

    /** 根据 id 拿颜色 */
    const getTypeColor = (id?: number) => {
      if (id == null) return 'info'
      return typeMap.value.get(id)?.color ?? 'info'
    }

    /** 根据 id 拿完整对象 */
    const getType = (id?: number) => {
      if (id == null) return undefined
      return typeMap.value.get(id)
    }

    return {
      types,
      loaded,
      typeMap,
      typeNameMap,
      sortedTypes,
      loadAll,
      clear,
      getTypeName,
      getTypeColor,
      getType,
    }
  },
  { persist: true },
)
