import { ref, watch } from 'vue'
import type { AnyRow, PageParams, PageResult, UseTableReturn } from './types'

export interface UseTableOptions<T extends AnyRow, P extends AnyRow = AnyRow> {
  /** 远程请求 */
  request?: (params: P & PageParams) => Promise<PageResult<T>>
  /** 静态数据：可传数组或 getter（getter 保留响应式） */
  data?: T[] | (() => T[] | undefined)
  /** 默认附加参数 */
  defaultParams?: P
  /** 默认每页条数 */
  defaultPageSize?: number
  /** 默认页码 */
  defaultPageNum?: number
  /** 请求成功回调 */
  onSuccess?: (result: PageResult<T>) => void
  /** 请求失败回调 */
  onError?: (error: unknown) => void
}

/**
 * 表格数据管理：分页、loading、请求、刷新
 */
export function useTable<T extends AnyRow, P extends AnyRow = AnyRow>(
  options: UseTableOptions<T, P>,
): UseTableReturn<T> {
  const {
    request,
    data: dataSource,
    defaultParams = {} as P,
    defaultPageSize = 10,
    defaultPageNum = 1,
    onSuccess,
    onError,
  } = options

  const loading = ref(false)
  const tableData = ref<T[]>([]) as UseTableReturn<T>['tableData']
  const total = ref(0)
  const currentPage = ref(defaultPageNum)
  const pageSize = ref(defaultPageSize)
  const extraParams = ref<AnyRow>({ ...defaultParams })

  /** 是否远程模式 */
  const isRemote = !!request

  /** 统一取数据：兼容「数组」和「getter」两种写法 */
  function getData(): T[] {
    if (typeof dataSource === 'function') {
      return (dataSource() ?? []) as T[]
    }
    return (dataSource ?? []) as T[]
  }

  /** 拉取数据 */
  async function fetchData() {
    // 静态数据模式：只做本地分页
    if (!isRemote) {
      const all = getData()
      total.value = all.length
      const start = (currentPage.value - 1) * pageSize.value
      tableData.value = all.slice(start, start + pageSize.value)
      return
    }

    // 远程模式
    loading.value = true
    try {
      const params = {
        ...extraParams.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value,
      } as P & PageParams

      const result = await request!(params)
      tableData.value = result.records ?? []
      total.value = result.total ?? 0
      onSuccess?.(result)
    } catch (err) {
      onError?.(err)
      // 保持表格为空，避免显示旧数据
      tableData.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  /** 刷新（保持当前页） */
  async function refresh() {
    await fetchData()
  }

  /** 搜索（重置到第一页，合并新参数） */
  async function search(params?: AnyRow) {
    if (params) {
      extraParams.value = { ...extraParams.value, ...params }
    }
    currentPage.value = 1
    await fetchData()
  }

  /** 重置到默认状态 */
  async function reset() {
    extraParams.value = { ...defaultParams }
    currentPage.value = defaultPageNum
    pageSize.value = defaultPageSize
    await fetchData()
  }

  /** 手动设置数据（用于某些特殊场景） */
  function setData(list: T[], t?: number) {
    tableData.value = list
    total.value = t ?? list.length
  }

  /** 静态数据变化时自动刷新（getter 形式能感知外部响应式变化） */
  if (!isRemote) {
    watch(
      () => getData(),
      () => {
        void fetchData()
      },
      { immediate: true, deep: true },
    )
  }

  return {
    loading,
    tableData,
    total,
    currentPage,
    pageSize,
    refresh,
    search,
    reset,
    setData,
  }
}
