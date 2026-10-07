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

/* -------- 内部工具 -------- */

function formatVersionLabel(v: DialogueVersionVO): string {
  const base =
    [v.languageLabel, v.formatLabel, v.scopeLabel].filter(Boolean).join(' · ') || `版本 ${v.id}`
  return base
}

function buildCount(source: { format?: number; images?: unknown[]; lines?: unknown[] }): string {
  if (source.format === VERSION_FORMAT.IMAGE) {
    return `${source.images?.length ?? 0} 张`
  }
  return `${source.lines?.length ?? 0} 句`
}

/** 语言-形式-范围的组合 key，用于把 option 和真实版本配对 */
function comboKey(v: { language?: number; format?: number; scope?: number }): string {
  return `${v.language ?? ''}|${v.format ?? ''}|${v.scope ?? ''}`
}

/**
 * 版本选中态 + 下拉项
 *
 * 关键点：versionSelectItems 会合并 versionOptions 和 storyDetail.versions，
 * 保证「新创建但 options 尚未刷新」的版本也能出现在下拉里。
 */
export function useVersionSelection(
  storyDetail: Ref<StoryDetailVO | null>,
  versionOptions: Ref<DialogueVersionOptionVO[] | null>,
) {
  /** 当前选中的 option key（UI 层 source of truth） */
  const selectedVersionKey = ref<string>('')

  /** id -> version 索引 */
  const versionMapById = computed(() => {
    const map = new Map<number, DialogueVersionVO>()
    for (const v of storyDetail.value?.versions ?? []) {
      if (v.id != null) map.set(v.id, v)
    }
    return map
  })

  /** 下拉项，合并 options + versions */
  const versionSelectItems = computed<VersionSelectItem[]>(() => {
    const options = versionOptions.value ?? []
    const versions = storyDetail.value?.versions ?? []

    // 组合 key -> version
    const versionByCombo = new Map<string, DialogueVersionVO>()
    for (const v of versions) {
      versionByCombo.set(comboKey(v), v)
    }

    // 记录被 options 消费掉的 versionId，用于第二遍去重
    const consumedIds = new Set<number>()
    const items: VersionSelectItem[] = []

    /* -------- 第一遍：按 options 顺序生成 -------- */
    options.forEach((opt, index) => {
      let versionId = opt.versionId ?? null
      let matchedVersion: DialogueVersionVO | undefined

      if (versionId != null) {
        // option 自带 id，直接查
        matchedVersion = versionMapById.value.get(versionId)
        consumedIds.add(versionId)
      } else {
        // option 无 id，用组合匹配
        matchedVersion = versionByCombo.get(comboKey(opt))
        if (matchedVersion?.id != null) {
          versionId = matchedVersion.id
          consumedIds.add(versionId)
        }
      }

      const key = versionId != null ? `id:${versionId}` : `label:${opt.label ?? index}`
      const label =
        opt.label ??
        (matchedVersion ? formatVersionLabel(matchedVersion) : `版本 ${versionId ?? index}`)
      const countSource = matchedVersion ?? opt

      items.push({
        key,
        label,
        versionId,
        option: opt,
        count: buildCount(countSource),
      })
    })

    /* -------- 第二遍：补上 options 没有的版本 -------- */
    for (const v of versions) {
      if (v.id == null || consumedIds.has(v.id)) continue

      const label = formatVersionLabel(v)
      items.push({
        key: `id:${v.id}`,
        label,
        versionId: v.id,
        option: {
          versionId: v.id,
          label,
          language: v.language,
          languageLabel: v.languageLabel,
          format: v.format,
          formatLabel: v.formatLabel,
          scope: v.scope,
          scopeLabel: v.scopeLabel,
        },
        count: buildCount(v),
      })
    }

    return items
  })

  /** key -> item，O(1) 查找 */
  const versionItemMap = computed(() => {
    const map = new Map<string, VersionSelectItem>()
    for (const item of versionSelectItems.value) map.set(item.key, item)
    return map
  })

  /** 当前选中的 option */
  const currentOptionVersion = computed<DialogueVersionOptionVO | null>(
    () => versionItemMap.value.get(selectedVersionKey.value)?.option ?? null,
  )

  /* -------- setter -------- */

  function setSelectedVersionKey(key: string) {
    if (selectedVersionKey.value === key) return
    selectedVersionKey.value = key
  }

  /**
   * 按版本 id 选中对应 option。
   * 找不到对应 option 时（例如数据源尚未刷新），key 置空。
   */
  function selectByVersionId(id: number | null) {
    if (id == null) {
      setSelectedVersionKey('')
      return
    }
    const key = `id:${id}`
    if (versionItemMap.value.has(key)) {
      setSelectedVersionKey(key)
    } else {
      setSelectedVersionKey('')
    }
  }

  return {
    selectedVersionKey,
    versionSelectItems,
    versionItemMap,
    currentOptionVersion,
    setSelectedVersionKey,
    selectByVersionId,
  }
}
