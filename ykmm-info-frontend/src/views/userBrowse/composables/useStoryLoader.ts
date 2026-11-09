import { type MaybeRefOrGetter, type Ref, toValue, watch } from 'vue'
import { BROWSE_KIND } from '@/constants'
import type { StoryDetailVO, StoryVO } from '@/types/story'

interface UseStoryLoaderParams {
  /** 剧情类型（来自 URL 的 :type 参数，如 main / rainbow / special） */
  type: MaybeRefOrGetter<string | number>
  /** 视图类型：STORY（详情） / CATEGORY（分类列表） */
  kind: MaybeRefOrGetter<string>
  /** 节点 ID：STORY 时为 storyId，CATEGORY 时为 categoryId */
  nodeId: MaybeRefOrGetter<number | null>
  /** 加载门控：true 时隐藏编辑区（首次进入 / 切换上下文） */
  dataLoading: Ref<boolean>

  // ---- useStoryDetail ----
  storyDetail: Ref<StoryDetailVO | null>
  resetStory: () => void
  fetchStoryDetail: (id: number) => Promise<unknown>
  fetchStories: (categoryId: number) => Promise<unknown>
  fetchVersionOptions: () => Promise<unknown>

  // ---- useCategoryTree ----
  loadTree: () => Promise<unknown>

  // ---- useDialogueEdit ----
  resetEditState: () => void
  syncVersionSelection: () => void
  syncEditingLine: () => void
  applyPendingInsert: () => Promise<unknown>
}

/**
 * 剧情浏览页「当前视图加载器」。
 * <p>
 * 统一封装三种加载路径：
 * <ul>
 *   <li>切换剧情类型 → 先重载分类树，再重载内容</li>
 *   <li>切换节点（kind / nodeId 变化） → 重载内容</li>
 *   <li>保存 / 新增 / 删除后 → 原地刷新，不隐藏编辑区</li>
 * </ul>
 *
 * `showLoading = true` 时置 `dataLoading = true`，期间 `showEditPanel` 会临时变 false，
 * 用于阻止「切换上下文」时的编辑区误闪。
 *
 * 使用方只需：
 * 1. 将 `loadCurrent` 绑定到业务回调
 * 2. 将 `reloadCurrent` 绑定到模板事件（如 `@refresh`）
 * 3. 不再自己写 watch
 *
 * @returns
 *  - `loadCurrent(showLoading?)`：带参数加载（内部调用 / 业务流程用）
 *  - `reloadCurrent()`：无参加载（模板事件绑定用）
 */
export function useStoryLoader(params: UseStoryLoaderParams) {
  const {
    type,
    kind,
    nodeId,
    dataLoading,
    storyDetail,
    resetStory,
    fetchStoryDetail,
    fetchStories,
    fetchVersionOptions,
    loadTree,
    resetEditState,
    syncVersionSelection,
    syncEditingLine,
    applyPendingInsert,
  } = params

  /**
   * 加载当前视图。
   *
   * @param showLoading 是否展示加载状态（隐藏编辑区）。
   *   - 首次进入 / 切换路由 / 切换分类 → true
   *   - 保存 / 新增 / 删除后刷新 → false（编辑区保持挂载，避免闪烁）
   */
  async function loadCurrent(showLoading = false) {
    if (showLoading) dataLoading.value = true
    try {
      const k = toValue(kind)
      const id = toValue(nodeId)

      if (k === BROWSE_KIND.STORY && id != null) {
        await fetchStoryDetail(id)
        if (!storyDetail.value) return

        syncVersionSelection()
        syncEditingLine()
        await fetchVersionOptions()
        await applyPendingInsert()
        return
      }

      /* 非 STORY 分支才清空 */
      resetStory()
      resetEditState()

      if (k === BROWSE_KIND.CATEGORY && id != null) {
        await fetchStories(id)
      }
    } finally {
      if (showLoading) dataLoading.value = false
    }
  }

  /** 事件绑定专用：不传参，走"原地刷新"路径 */
  function reloadCurrent() {
    void loadCurrent()
  }

  /**
   * 首次进入 + 切换剧情类型：
   * 先重载分类树（type 变了 → 树的根节点也跟着变），再重载内容。
   */
  watch(
    () => toValue(type),
    async () => {
      await loadTree()
      await loadCurrent(true)
    },
    { immediate: true },
  )

  /**
   * 切换节点（kind / nodeId 变化）：
   * 树不用重载，只重载内容。
   */
  watch(
    () => [toValue(kind), toValue(nodeId)],
    () => {
      void loadCurrent(true)
    },
  )

  return { loadCurrent, reloadCurrent }
}
