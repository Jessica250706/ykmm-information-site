import { computed, ref, type Ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'

/**
 * 浏览页「当前选中版本是否可编辑」权限判断。
 * <p>
 * 除实时权限外，额外提供记忆化的 {@link effectiveCanEdit}：
 * 切换话数 / 版本时，中间态可能短暂出现"上一个话数的 option"，
 * 用实时值会导致编辑区闪烁。
 * `effectiveCanEdit` 在数据未就绪 / 加载期间保持上一次真实权限，避免这个问题。
 *
 * @param currentOptionVersion 当前选中的版本 option（来自 useVersionSelection）
 * @param dataLoading          加载门控（来自组件，true 时进入"信任记忆值"模式）
 */
export function useBrowseEditPermission(
  currentOptionVersion: Ref<DialogueVersionOptionVO | null>,
  dataLoading: Ref<boolean>,
) {
  const userStore = useUserStore()
  const { isLogin, isAdmin, isPassedUser } = storeToRefs(userStore)

  /** 判断一个 option 是否为「权威」——即来自 versionOptions、带有明确的 exists 值 */
  function isAuthoritative(opt: DialogueVersionOptionVO | null): boolean {
    return opt != null && opt.exists !== undefined
  }

  /**
   * 实时权限。
   * <p>
   * - 无 option 或临时 option（exists === undefined）：返回 false（中性值）
   * - 权威 option：按 exists / canOperate 判断
   */
  const canEditCurrent = computed(() => {
    const opt = currentOptionVersion.value
    if (!opt) return false
    if (opt.exists === undefined) return false
    if (!isAdmin.value && !isPassedUser.value) return false
    return opt.exists ? opt.canOperate === true : isLogin.value
  })

  /** 上一次「加载完成时」的真实权限，用于过渡期间保持 */
  const lastKnownCanEdit = ref(false)

  /**
   * 记忆化写入：
   * <p>
   * 只在「加载完成 + 权威 option」时写入。加载期间（dataLoading=true）不写，
   * 因为此时 canEditCurrent 可能被"上一个话数残留的 option"污染成错误值。
   */
  watch(
    [canEditCurrent, dataLoading],
    ([val, loading]) => {
      if (loading) return
      if (isAuthoritative(currentOptionVersion.value)) {
        lastKnownCanEdit.value = val
      }
    },
    { immediate: true },
  )

  /**
   * 实际使用的权限：
   * - 加载期间：用记忆值（防止中间态抖动）
   * - 非权威 option：用记忆值
   * - 加载完成 + 权威 option：用实时值
   */
  const effectiveCanEdit = computed(() => {
    if (dataLoading.value) return lastKnownCanEdit.value
    if (!isAuthoritative(currentOptionVersion.value)) return lastKnownCanEdit.value
    return canEditCurrent.value
  })

  return { canEditCurrent, effectiveCanEdit }
}
