import { computed, ref, type Ref } from 'vue'
import { VERSION_FORMAT } from '@/constants/index'
import type { DialogueVersionOptionVO, DialogueVersionVO } from '@/types/dialogueVersion'
import type { StoryDetailVO } from '@/types/story'

export interface VersionSelectItem {
  /** el-option 的 value */
  key: string
  /** el-option 的展示文案 */
  label: string
  /** 版本 id，无内容时为 null */
  versionId: number | null
  /** 原始 option 数据，透传给 DialogueView / BrowseRight */
  option: DialogueVersionOptionVO
  /** 内容数量文案，如 "12 句" / "3 张" */
  count: string
}

/**
 * 版本选中态 + 下拉项
 *
 * - selectedVersionKey：UI 层的 source of truth，能表达"选中了没有内容的版本"
 * - currentOptionVersion：当前选中的 option（可能 versionId 为 null）
 */
export function useVersionSelection(
  storyDetail: Ref<StoryDetailVO | null>,
  versionOptions: Ref<DialogueVersionOptionVO[] | null>,
) {
  /** 当前选中的 option key */
  const selectedVersionKey = ref<string>('')

  /** id -> version 索引 */
  const versionMapById = computed(() => {
    const map = new Map<number, DialogueVersionVO>()
    for (const v of storyDetail.value?.versions ?? []) {
      if (v.id != null) map.set(v.id, v)
    }
    return map
  })

  /** 下拉项，预计算 label / key / count，模板里只读字段 */
  const versionSelectItems = computed<VersionSelectItem[]>(() => {
    const options = versionOptions.value ?? []
    return options.map((opt, index) => {
      const versionId = opt.versionId ?? null
      const key = versionId != null ? `id:${versionId}` : `label:${opt.label ?? index}`

      let label = opt.label
      if (!label) {
        const v = versionId != null ? versionMapById.value.get(versionId) : undefined
        label = v
          ? [v.languageLabel, v.formatLabel, v.scopeLabel].filter(Boolean).join(' · ') ||
            `版本 ${v.id}`
          : `版本 ${versionId ?? index}`
      }

      const count =
        opt.format === VERSION_FORMAT.IMAGE
          ? `${opt.images?.length ?? 0} 张`
          : `${opt.lines?.length ?? 0} 句`

      return { key, label, versionId, option: opt, count }
    })
  })

  /** key -> item，O(1) 查找 */
  const versionItemMap = computed(() => {
    const map = new Map<string, VersionSelectItem>()
    for (const item of versionSelectItems.value) map.set(item.key, item)
    return map
  })

  /** 当前选中的 option：能表达"选中了没有内容的版本" */
  const currentOptionVersion = computed<DialogueVersionOptionVO | null>(
    () => versionItemMap.value.get(selectedVersionKey.value)?.option ?? null,
  )

  return {
    selectedVersionKey,
    versionSelectItems,
    versionItemMap,
    currentOptionVersion,
  }
}
