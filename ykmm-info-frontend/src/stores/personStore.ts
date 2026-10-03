import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { listPersonOptionsAPI } from '@/api/person'
import type { PersonOptionVO } from '@/types/person'

export const usePersonStore = defineStore(
  'person',
  () => {
    const persons = ref<PersonOptionVO[]>([])
    const loaded = ref(false)

    const personMap = computed(() => {
      const map = new Map<number, PersonOptionVO>()
      persons.value.forEach((p) => {
        if (p.id != null) map.set(p.id, p)
      })
      return map
    })

    /** 加载全部人物（用于下拉选择） */
    const loadAll = async (force = false) => {
      if (loaded.value && !force) return persons.value

      // 一次性拉取足够多的数据（后端上限内）
      const res = await listPersonOptionsAPI()
      persons.value = res.data ?? []
      loaded.value = true
      return persons.value
    }

    const clear = () => {
      persons.value = []
      loaded.value = false
    }

    /** 根据 id 拿人物名 */
    const getPersonName = (id?: number) => {
      if (id == null) return ''
      const p = personMap.value.get(id)
      return p?.nameCn || p?.nameJp || ''
    }

    return {
      persons,
      loaded,
      personMap,
      loadAll,
      clear,
      getPersonName,
    }
  },
  { persist: true },
)
