import { type MaybeRefOrGetter, type Ref, toValue } from 'vue'
import type { CardEpisodeVO } from '@/types/card'

interface UseEpisodeLoaderParams {
  /** 来源类型：1-RC 2-RTV 3-Rabitter 4-story */
  sourceType: MaybeRefOrGetter<number>
  /** 当前话数 ID（由 useEpisodeRoute 提供） */
  currentEpisodeId: Ref<number | null>
  /** 当前话数详情（来自 useCardEpisodeDetail，用于加载后判断是否成功） */
  episodeDetail: Ref<CardEpisodeVO | null>
  /** 加载门控：true 时隐藏编辑区（首次进入 / 切换话数） */
  dataLoading: Ref<boolean>

  /** 清空当前话数详情（切换话数时调用） */
  resetEpisodeDetail: () => void
  /** 加载话数详情 */
  fetchEpisodeDetail: (sourceType: number, episodeId: number) => Promise<void>
  /** 加载版本选项 */
  fetchVersionOptions: (sourceType: number, episodeId: number) => Promise<void>

  /** 编辑态同步 */
  syncVersionSelection: () => void
  syncEditingLine: () => void
  /** 应用"新增行后定位"的挂起操作 */
  applyPendingInsert: () => Promise<void>
}

/**
 * 话数对话加载器。
 * <p>
 * 统一封装"切换话数"和"保存后刷新"两种加载路径：
 * <ul>
 *   <li>showLoading = true：切换上下文（首次进入 / 切换话数），期间隐藏编辑区避免闪烁</li>
 *   <li>showLoading = false（默认）：原地刷新（保存 / 新增 / 删除后），编辑区保持挂载</li>
 * </ul>
 *
 * @returns
 *  - `loadCurrentEpisode(showLoading?)`：显式带参数的加载
 *  - `reloadCurrentEpisode()`：事件绑定专用（不传参），走原地刷新
 */
export function useEpisodeLoader(params: UseEpisodeLoaderParams) {
  const {
    sourceType,
    currentEpisodeId,
    episodeDetail,
    dataLoading,
    resetEpisodeDetail,
    fetchEpisodeDetail,
    fetchVersionOptions,
    syncVersionSelection,
    syncEditingLine,
    applyPendingInsert,
  } = params

  /**
   * 加载当前话数的对话。
   *
   * @param showLoading 是否展示加载状态（隐藏编辑区）
   */
  async function loadCurrentEpisode(showLoading = false) {
    if (currentEpisodeId.value == null) {
      resetEpisodeDetail()
      return
    }

    if (showLoading) dataLoading.value = true
    try {
      const st = toValue(sourceType)
      await fetchEpisodeDetail(st, currentEpisodeId.value)
      if (!episodeDetail.value) return

      syncVersionSelection()
      syncEditingLine()
      await fetchVersionOptions(st, currentEpisodeId.value)
      await applyPendingInsert()
    } finally {
      if (showLoading) dataLoading.value = false
    }
  }

  /** 事件绑定专用：不传参，走"原地刷新"路径 */
  function reloadCurrentEpisode() {
    void loadCurrentEpisode()
  }

  return { loadCurrentEpisode, reloadCurrentEpisode }
}
