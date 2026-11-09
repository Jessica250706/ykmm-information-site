import { computed, ref, type Ref } from 'vue'

/**
 * 浏览页左右侧栏与数据加载的布局状态。
 * <p>
 * `dataLoading` 只在"切换上下文"（首次进入 / 切换分类 / 切换话数）时置 true，
 * 保存 / 新增等"原地刷新"不设，避免编辑区被卸载重建造成闪烁。
 *
 * @param canEditCurrent 当前版本是否可编辑（来自 useBrowseEditPermission）
 */
export function useBrowseLayout(canEditCurrent: Ref<boolean>) {
  /** 左侧目录是否展开 */
  const showLeft = ref(true)
  /** 右侧编辑区是否展开（用户手动控制） */
  const showRight = ref(true)
  /** 「切换上下文」期间为 true，用于阻止编辑区误闪 */
  const dataLoading = ref(false)

  /** 右侧编辑区是否渲染：数据就绪 + 用户手动展开 + 当前版本可编辑 */
  const showEditPanel = computed(
    () => !dataLoading.value && showRight.value && canEditCurrent.value,
  )

  return { showLeft, showRight, dataLoading, showEditPanel }
}
