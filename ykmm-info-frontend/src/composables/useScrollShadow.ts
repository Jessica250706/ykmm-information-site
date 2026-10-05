import { onBeforeUnmount, onMounted, ref, type Ref } from 'vue'

export function useScrollShadow(scrollerRef: Ref<HTMLElement | null>) {
  /** 是否已经滚动离开顶部（用于显示顶部阴影） */
  const atTop = ref(true)
  /** 是否滚动到底部（用于隐藏底部阴影） */
  const atBottom = ref(false)
  /** 内容是否足以滚动（不足时两条阴影都不显示） */
  const canScroll = ref(false)

  function update() {
    const el = scrollerRef.value
    if (!el) return

    const { scrollTop, scrollHeight, clientHeight } = el
    const threshold = 1 // 容差，避免亚像素误差

    canScroll.value = scrollHeight - clientHeight > threshold
    atTop.value = scrollTop <= threshold
    atBottom.value = scrollTop + clientHeight >= scrollHeight - threshold
  }

  let observer: ResizeObserver | null = null

  onMounted(() => {
    const el = scrollerRef.value
    if (!el) return

    el.addEventListener('scroll', update, { passive: true })
    update()

    // 内容变化 / 容器尺寸变化时重算
    observer = new ResizeObserver(update)
    observer.observe(el)
    // 观察第一个子元素（内容），也能捕获内容高度变化
    if (el.firstElementChild) {
      observer.observe(el.firstElementChild)
    }
  })

  onBeforeUnmount(() => {
    scrollerRef.value?.removeEventListener('scroll', update)
    observer?.disconnect()
    observer = null
  })

  return { atTop, atBottom, canScroll, update }
}
