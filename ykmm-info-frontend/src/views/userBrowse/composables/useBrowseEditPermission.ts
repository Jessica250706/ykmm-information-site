import { computed, type Ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores'
import type { DialogueVersionOptionVO } from '@/types/dialogueVersion'

/**
 * 浏览页「当前选中版本是否可编辑」权限判断。
 * <p>
 * 规则：
 * <ul>
 *   <li>管理员：不受答题门槛限制</li>
 *   <li>普通用户：必须先通过答题</li>
 *   <li>版本已存在：以后端返回的 canOperate 为准</li>
 *   <li>版本不存在：登录用户视为"可创建"，游客不可</li>
 * </ul>
 *
 * @param currentOptionVersion 当前选中的版本 option（来自 useVersionSelection）
 */
export function useBrowseEditPermission(currentOptionVersion: Ref<DialogueVersionOptionVO | null>) {
  const userStore = useUserStore()
  const { isLogin, isAdmin, isPassedUser } = storeToRefs(userStore)

  const canEditCurrent = computed(() => {
    const opt = currentOptionVersion.value
    if (!opt) return false

    // 普通用户必须先通过答题；管理员不受答题门槛限制
    if (!isAdmin.value && !isPassedUser.value) return false

    return opt.exists ? opt.canOperate === true : isLogin.value
  })

  return { canEditCurrent }
}
