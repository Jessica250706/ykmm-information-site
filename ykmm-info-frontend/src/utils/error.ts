/**
 * 从任意异常对象中提取可读的错误信息
 *
 * @param e        捕获到的异常
 * @param fallback 无法提取时的兜底文案
 * @returns 可读的错误文案
 */
export function getErrorMessage(e: unknown, fallback = '请求失败'): string {
  if (e instanceof Error) return e.message || fallback
  if (typeof e === 'string') return e
  return fallback
}
