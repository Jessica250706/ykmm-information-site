import { computed, ref, type Ref } from 'vue'

/**
 * 浏览页左右侧栏与数据加载的布局状态。
 * <p>
 * `effectiveCanEdit` 已包含权限记忆化，`showEditPanel` 直接用它，
 * 不再依赖 `dataLoading` —— 加载期间保持权限由 `useBrowseEditPermission` 内部消化。
 *
 * @param effectiveCanEdit 记忆化后的可编辑权限（来自 useBrowseEditPermission）
 */
export function useBrowseLayout(effectiveCanEdit: Ref<boolean>) {
  /** 左侧目录是否展开 */
  const showLeft = ref(true)
  /** 右侧编辑区是否展开（用户手动控制） */
  const showRight = ref(true)

  /** 右侧编辑区是否渲染：用户手动展开 + 当前版本可编辑 */
  const showEditPanel = computed(() => showRight.value && effectiveCanEdit.value)

  return { showLeft, showRight, showEditPanel }
}
