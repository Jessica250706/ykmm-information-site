import type { AnyRow, PageResult } from './types'

/** 响应适配器：把原始响应转成 { list, total } */
export type ResponseAdapter = (raw: unknown) => PageResult<AnyRow>

let globalAdapter: ResponseAdapter | null = null

/**
 * 全局配置 ProTable。
 * 在 main.ts 里调用一次，所有 ProTable 默认走这套适配。
 */
export function configureProTable(options: { responseAdapter?: ResponseAdapter }) {
  if (options.responseAdapter) {
    globalAdapter = options.responseAdapter
  }
}

export function getGlobalAdapter(): ResponseAdapter | null {
  return globalAdapter
}
