import { type Ref, watch } from 'vue'

interface VersionItem {
  versionId: number | null
}

interface Params {
  /** 当前选中版本 id（Ref<number | null>） */
  currentVersionId: Ref<number | null>
  /** 当前选中的下拉 key（Ref<string>） */
  selectedVersionKey: Ref<string>
  /** key → item 的映射（来自 useVersionSelection） */
  versionItemMap: Ref<Map<string, VersionItem>>
  /** 当前编辑高亮行 id，切换版本时要清空 */
  editingLineId: Ref<number | null>
  /** 当前编辑模式，退出时清空高亮 */
  editingMode: Ref<boolean>
}

/**
 * 版本选中与编辑高亮的双向同步。
 * <p>
 * 把三个"每个 Browse 页面都要写一遍"的 watch 收拢到这里：
 * <ul>
 *   <li>currentVersionId 变化 → 同步 selectedVersionKey（跳过已同指向的 option，避免循环）</li>
 *   <li>currentVersionId 变化 → 清空 editingLineId（避免引用旧行）</li>
 *   <li>editingMode 退出 → 清空 editingLineId</li>
 * </ul>
 *
 * 同时提供 {@link handleSelectedVersionKeyChange} 供模板绑定下拉框的 `update:model-value`。
 */
export function useVersionSelectionSync(params: Params) {
  const { currentVersionId, selectedVersionKey, versionItemMap, editingLineId, editingMode } =
    params

  /** 外部 currentVersionId 变化时同步本地 key（跳过"已同指向"避免循环覆盖） */
  watch(
    currentVersionId,
    (id) => {
      const cur = versionItemMap.value.get(selectedVersionKey.value)
      if (cur && cur.versionId === (id ?? null)) return
      selectedVersionKey.value = id == null ? '' : `id:${id}`
    },
    { immediate: true },
  )

  /** 版本切换 → 清空编辑高亮 */
  watch(currentVersionId, () => {
    editingLineId.value = null
  })

  /** 退出编辑模式 → 清空编辑高亮 */
  watch(editingMode, (val) => {
    if (!val) editingLineId.value = null
  })

  /** 下拉切换 → 更新 key 并同步 currentVersionId */
  function handleSelectedVersionKeyChange(key: string) {
    selectedVersionKey.value = key
    currentVersionId.value = versionItemMap.value.get(key)?.versionId ?? null
  }

  return { handleSelectedVersionKeyChange }
}
