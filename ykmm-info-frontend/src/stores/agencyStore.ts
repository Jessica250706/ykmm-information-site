import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { listAgencyAPI } from '@/api/agency'
import type { AgencyVO } from '@/types/agency'

export const useAgencyStore = defineStore(
  'agency',
  () => {
    const agency = ref<AgencyVO[]>([])
    const loaded = ref(false)

    /** id -> AgencyVO 映射，方便按 id 取名字 */
    const agencyMap = computed(() => {
      const map = new Map<number, AgencyVO>()
      agency.value.forEach((item) => {
        if (item.id != null) map.set(item.id, item)
      })
      return map
    })

    /** 加载全部经纪公司 */
    const loadAll = async (force = false) => {
      if (loaded.value && !force) return agency.value
      const res = await listAgencyAPI()
      agency.value = res.data ?? []
      loaded.value = true
      return agency.value
    }

    /** 清空（登出时可用） */
    const clear = () => {
      agency.value = []
      loaded.value = false
    }

    /** 根据 id 拿公司名，业务里很好用 */
    const getAgencyName = (id?: number) => {
      if (id == null) return ''
      return agencyMap.value.get(id)?.name ?? ''
    }

    return {
      agency,
      loaded,
      agencyMap,
      loadAll,
      clear,
      getAgencyName,
    }
  },
  { persist: true },
)
