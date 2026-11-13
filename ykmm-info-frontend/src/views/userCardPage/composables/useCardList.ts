import { ref, type Ref } from 'vue'
import { pageCardAPI } from '@/api/card'
import type { CardPageQueryDTO, CardVO } from '@/types/card'
import type { CardFilter } from './useCardFilterSync'

export function useCardList(options: { keyword: Ref<string>; filter: CardFilter }) {
  const list = ref<CardVO[]>([])
  const loading = ref(false)
  const disabled = ref(false)
  const pageNum = ref(1)
  const pageSize = ref(20)

  /** 统一构造请求参数 */
  function buildParams(page: number): CardPageQueryDTO {
    return {
      pageNum: page,
      pageSize: pageSize.value,
      keyword: options.keyword.value.trim() || undefined,
      seriesId: options.filter.seriesId,
      category: options.filter.category,
      maxRarity: options.filter.maxRarity,
      attribute: options.filter.attribute,
      attachedStoryType: options.filter.attachedStoryType,
      costumeType: options.filter.costumeType,
      personIds: options.filter.personIds.length ? options.filter.personIds : undefined,
    }
  }

  /** 第一页（覆盖 list） */
  async function loadList() {
    loading.value = true
    try {
      const res = await pageCardAPI(buildParams(1))
      const records = res.data.records ?? []
      list.value = records
      disabled.value = records.length < pageSize.value
    } catch {
      list.value = []
      disabled.value = true
    } finally {
      loading.value = false
    }
  }

  /** 下一页（追加 list） */
  async function load() {
    if (loading.value || disabled.value) return

    loading.value = true
    pageNum.value++

    try {
      const res = await pageCardAPI(buildParams(pageNum.value))
      const records = res.data.records ?? []

      if (records.length === 0) {
        disabled.value = true
        return
      }

      list.value.push(...records)
      if (records.length < pageSize.value) disabled.value = true
    } catch {
      disabled.value = true
    } finally {
      loading.value = false
    }
  }

  /** 重置 + 首屏连发两页（解决"一屏装不满"） */
  async function resetAndLoad() {
    pageNum.value = 1
    list.value = []
    disabled.value = false
    loading.value = false

    await loadList()
    if (!disabled.value) await load()
  }

  return { list, loading, disabled, load, resetAndLoad }
}
