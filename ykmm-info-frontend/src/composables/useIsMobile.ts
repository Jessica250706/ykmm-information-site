import { onBeforeUnmount, onMounted, ref } from 'vue'

/**
 * 响应式判断当前是否为移动端（视口宽度 < breakpoint）。
 * <p>基于 window.matchMedia，SSR 安全：只在 onMounted 时初始化。
 *
 * @param breakpoint 断点阈值（px），默认 768（Tailwind `md`）
 * @returns 响应式 ref，true = 移动端
 */
export function useIsMobile(breakpoint = 768) {
  const isMobile = ref(false)
  let mql: MediaQueryList | null = null

  const handler = (e: MediaQueryListEvent | MediaQueryList) => {
    isMobile.value = e.matches
  }

  onMounted(() => {
    mql = window.matchMedia(`(max-width: ${breakpoint - 1}px)`)
    isMobile.value = mql.matches
    mql.addEventListener('change', handler)
  })

  onBeforeUnmount(() => {
    if (mql) {
      mql.removeEventListener('change', handler)
      mql = null
    }
  })

  return isMobile
}
