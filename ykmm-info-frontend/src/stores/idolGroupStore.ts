import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { listIdolGroupsAPI } from '@/api/idolGroup'
import type { IdolGroupVO } from '@/types/idolGroup'

export const useIdolGroupStore = defineStore(
  'idolGroup',
  () => {
    const idolGroups = ref<IdolGroupVO[]>([])
    const loaded = ref(false)

    /** id -> IdolGroupVO 映射 */
    const idolGroupMap = computed(() => {
      const map = new Map<number, IdolGroupVO>()
      idolGroups.value.forEach((item) => {
        if (item.id != null) map.set(item.id, item)
      })
      return map
    })

    /** 按经纪公司分组，方便级联 / 筛选 */
    const groupsByAgency = computed(() => {
      const map = new Map<number, IdolGroupVO[]>()
      idolGroups.value.forEach((item) => {
        if (item.agencyId == null) return
        const list = map.get(item.agencyId) ?? []
        list.push(item)
        map.set(item.agencyId, list)
      })
      return map
    })

    /** 加载全部偶像团体 */
    const loadAll = async (force = false) => {
      if (loaded.value && !force) return idolGroups.value
      const res = await listIdolGroupsAPI()
      idolGroups.value = res.data ?? []
      loaded.value = true
      return idolGroups.value
    }

    /** 清空（登出时可用） */
    const clear = () => {
      idolGroups.value = []
      loaded.value = false
    }

    /** 根据 id 拿团体名 */
    const getIdolGroupName = (id?: number) => {
      if (id == null) return ''
      return idolGroupMap.value.get(id)?.name ?? ''
    }

    return {
      idolGroups,
      loaded,
      idolGroupMap,
      groupsByAgency,
      loadAll,
      clear,
      getIdolGroupName,
    }
  },
  { persist: true },
)
