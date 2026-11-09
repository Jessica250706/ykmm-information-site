import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

/**
 * 从 URL 读取初始话数 ID。
 * <p>
 * query.sourceId 非法或缺失时返回 null。
 */
function resolveInitialEpisodeId(): number | null {
  const route = useRoute()
  const raw = route.query.sourceId
  if (typeof raw !== 'string' || !raw) return null
  const n = Number(raw)
  return Number.isFinite(n) && n > 0 ? n : null
}

/**
 * 当前话数与 URL 的双向同步。
 * <p>
 * 约定：URL query 用 `sourceId` 承载当前话数 ID。
 * 使用 router.replace 而非 push，避免污染浏览器历史。
 */
export function useEpisodeRoute() {
  const route = useRoute()
  const router = useRouter()

  const currentEpisodeId = ref<number | null>(resolveInitialEpisodeId())

  /**
   * 把当前话数写回 URL query。
   * <p>
   * 值未变化时直接返回，避免触发无意义的 replace。
   */
  function syncEpisodeToUrl(id: number | null) {
    const cur = route.query.sourceId
    const target = id == null ? undefined : String(id)
    if ((cur ?? undefined) === target) return

    const query = { ...route.query }
    if (target == null) {
      delete query.sourceId
    } else {
      query.sourceId = target
    }
    void router.replace({ query })
  }

  return { currentEpisodeId, syncEpisodeToUrl }
}
