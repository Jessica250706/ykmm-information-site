import { reactive, ref, type Ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

/** 筛选条件类型（供外部复用） */
export interface CardFilter {
  seriesId?: number
  category?: number
  maxRarity?: number
  attribute?: number
  attachedStoryType?: number
  costumeType?: number
  personIds: number[]
}

function readStr(v: unknown): string {
  return typeof v === 'string' ? v : ''
}

function readNum(v: unknown): number | undefined {
  if (v === undefined || v === null || v === '') return undefined
  const n = Number(v)
  return Number.isFinite(n) && n >= 0 ? n : undefined
}

function readIds(v: unknown): number[] | undefined {
  if (v === undefined) return undefined
  if (Array.isArray(v)) {
    return v.map((x) => Number(x)).filter((n) => Number.isFinite(n))
  }
  if (typeof v === 'string') {
    if (v === '') return []
    return v
      .split(',')
      .map((s) => Number(s.trim()))
      .filter((n) => Number.isFinite(n))
  }
  return undefined
}

/**
 * 筛选条件 <-> URL query 双向同步
 *
 * @param options.onChange URL 变化（前进/后退 / 筛选变更）时的回调，
 *                         由调用方延迟绑定（避免声明顺序问题）
 */
export function useCardFilterSync(options?: { onChange?: () => void }) {
  const route = useRoute()
  const router = useRouter()

  const initialPersonIds = readIds(route.query.personIds)

  const keyword = ref(readStr(route.query.keyword))
  const filter = reactive<CardFilter>({
    seriesId: readNum(route.query.seriesId),
    category: readNum(route.query.category),
    maxRarity: readNum(route.query.maxRarity),
    attribute: readNum(route.query.attribute),
    attachedStoryType: readNum(route.query.attachedStoryType),
    costumeType: readNum(route.query.costumeType),
    personIds: initialPersonIds ?? [],
  })

  const showMore = ref(
    filter.category !== undefined ||
      filter.attachedStoryType !== undefined ||
      filter.costumeType !== undefined,
  )

  let selfPushing = false

  /** 把当前筛选写回 URL；返回是否真的变化了 */
  function writeToUrl(): boolean {
    const query: Record<string, string> = {}
    const kw = keyword.value.trim()
    if (kw) query.keyword = kw
    if (filter.seriesId !== undefined) query.seriesId = String(filter.seriesId)
    if (filter.category !== undefined) query.category = String(filter.category)
    if (filter.maxRarity !== undefined) query.maxRarity = String(filter.maxRarity)
    if (filter.attribute !== undefined) query.attribute = String(filter.attribute)
    if (filter.attachedStoryType !== undefined)
      query.attachedStoryType = String(filter.attachedStoryType)
    if (filter.costumeType !== undefined) query.costumeType = String(filter.costumeType)
    if (filter.personIds.length) query.personIds = filter.personIds.join(',')

    const current = route.query as Record<string, unknown>
    const keys = new Set([...Object.keys(query), ...Object.keys(current)])
    for (const k of keys) {
      const a = query[k] === undefined ? '' : String(query[k])
      const b = current[k] === undefined ? '' : String(current[k])
      if (a !== b) {
        selfPushing = true
        router.replace({ query })
        return true
      }
    }
    return false
  }

  function readFromQuery(q: Record<string, unknown>) {
    return {
      keyword: readStr(q.keyword),
      seriesId: readNum(q.seriesId),
      category: readNum(q.category),
      maxRarity: readNum(q.maxRarity),
      attribute: readNum(q.attribute),
      attachedStoryType: readNum(q.attachedStoryType),
      costumeType: readNum(q.costumeType),
      personIds: readIds(q.personIds) ?? [],
    }
  }

  // URL 变化 → 回填 UI + 通知调用方重新加载
  watch(
    () => route.query,
    (q) => {
      const fromSelf = selfPushing
      selfPushing = false

      if (!fromSelf) {
        const parsed = readFromQuery(q as Record<string, unknown>)
        keyword.value = parsed.keyword
        filter.seriesId = parsed.seriesId
        filter.category = parsed.category
        filter.maxRarity = parsed.maxRarity
        filter.attribute = parsed.attribute
        filter.attachedStoryType = parsed.attachedStoryType
        filter.costumeType = parsed.costumeType
        filter.personIds = parsed.personIds

        if (
          parsed.category !== undefined ||
          parsed.attachedStoryType !== undefined ||
          parsed.costumeType !== undefined
        ) {
          showMore.value = true
        }
      }

      options?.onChange?.()
    },
  )

  // filter 变化 → 写 URL（加载由 URL watch 统一触发）
  watch(filter, () => writeToUrl(), { deep: true })

  return {
    keyword: keyword as Ref<string>,
    filter,
    showMore,
    writeToUrl,
  }
}
