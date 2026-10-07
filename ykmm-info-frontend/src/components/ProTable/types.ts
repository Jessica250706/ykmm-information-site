import type { Ref, VNodeChild } from 'vue'
import type { TableColumnCtx, TableInstance } from 'element-plus'

/* eslint-disable @typescript-eslint/no-explicit-any */
/**
 * 表格行数据的通用类型。
 * Element Plus 内部的 DefaultRow 本质也是宽松索引对象，
 * 表格组件作为“容器”无法预知业务字段，这里集中豁免 any。
 */
export type AnyRow = Record<string, any>
/* eslint-enable @typescript-eslint/no-explicit-any */

/** 单元格 render 函数的 scope */
export interface ProTableScope<T extends AnyRow = AnyRow> {
  row: T
  column: TableColumnCtx<T>
  $index: number
}

/**
 * 表格自定义渲染函数类型。
 * 用方法签名（bivarianceHack）打开函数参数的双变性，
 * 让 (scope: ProTableScope<T>) => VNodeChild 和
 *     (scope: ProTableScope<AnyRow>) => VNodeChild
 * 能互相赋值，避免逆变报错。
 */
export type ProTableRenderFn = {
  bivarianceHack(scope: ProTableScope): VNodeChild
}['bivarianceHack']

/** 列配置 */
export interface ProTableColumn<T extends AnyRow = AnyRow> {
  /** 字段名 */
  prop?: string
  /** 表头 */
  label?: string
  /** 固定宽度 */
  width?: string | number
  /** 最小宽度 */
  minWidth?: string | number
  /** 固定列 */
  fixed?: boolean | 'left' | 'right'
  /** 对齐 */
  align?: 'left' | 'center' | 'right'
  /** 排序：true / 'custom' / false */
  sortable?: boolean | 'custom'
  /** 内容超出显示省略号 */
  showOverflowTooltip?: boolean
  /** 特殊列类型：多选 / 序号 / 展开 */
  type?: 'selection' | 'index' | 'expand'
  /** 多选列专用 */
  selectable?: {
    bivarianceHack(row: T, index: number): boolean
  }['bivarianceHack']
  /** 序号列专用 */
  index?: number | ((index: number) => number)
  /** 表头自定义 render */
  headerRender?: ProTableRenderFn
  /** 单元格自定义 render（优先级低于 slot） */
  render?: ProTableRenderFn
  /** 单元格插槽名，对应父组件 <template #xxx="{ row }"> */
  slot?: string
  /** 表头插槽名 */
  headerSlot?: string
  /** 是否隐藏（v-if 风格） */
  hidden?: boolean
  /** 多级表头 */
  children?: ProTableColumn<T>[]
  /** 是否采用 flex 布局 */
  displayFlex?: boolean
  /** 透传给 el-table-column 的其他属性 */
  [key: string]: unknown
}

/** 分页参数 */
export interface PageParams {
  pageNum: number
  pageSize: number
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
}

/** ProTable 的 props */
export interface ProTableProps<T extends AnyRow = AnyRow, P extends AnyRow = AnyRow> {
  /** 列配置 */
  columns?: ProTableColumn<T>[]
  /** 静态数据（与 request 二选一） */
  data?: T[]
  /** 远程请求函数 */
  request?: (params: P & PageParams) => Promise<PageResult<T>>
  /** 请求附加参数（会合并到分页参数） */
  params?: P
  /** 是否显示分页，默认 true */
  pagination?: boolean
  /** 分页配置 */
  pageSizes?: number[]
  /** 默认每页条数 */
  defaultPageSize?: number
  /** 默认页码 */
  defaultPageNum?: number
  /** 分页布局 */
  paginationLayout?: string
  /** 挂载即请求 */
  immediate?: boolean
}

/** useTable 的返回 */
export interface UseTableReturn<T extends AnyRow = AnyRow> {
  loading: Ref<boolean>
  tableData: Ref<T[]>
  total: Ref<number>
  currentPage: Ref<number>
  pageSize: Ref<number>
  refresh: () => Promise<void>
  search: (params?: AnyRow) => Promise<void>
  reset: () => Promise<void>
  setData: (list: T[], total?: number) => void
}

export interface ProTableExpose<T extends AnyRow = AnyRow> {
  /** 底层 el-table 实例 */
  tableRef: Ref<TableInstance | undefined>
  /** 刷新（保持当前页） */
  refresh: () => Promise<void>
  /** 搜索（重置到第一页，合并参数） */
  search: (params?: AnyRow) => Promise<void>
  /** 重置到默认状态 */
  reset: () => Promise<void>
  /** 手动设置数据 */
  setData: (list: T[], total?: number) => void
  /** 当前 loading 状态 */
  loading: Ref<boolean>
  /** 当前表格数据 */
  tableData: Ref<T[]>
  /** 总条数 */
  total: Ref<number>
}
