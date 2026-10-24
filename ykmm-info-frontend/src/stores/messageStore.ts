import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getUnreadCountAPI } from '@/api/message'
import { useUserStore } from './userStore'

export const useMessageStore = defineStore('message', () => {
  /** 当前用户未读消息数 */
  const unreadCount = ref(0)
  /** 拉取中 */
  const loading = ref(false)

  /** 是否有未读 */
  const hasUnread = computed(() => unreadCount.value > 0)

  /**
   * 从后端拉取最新未读数
   * - 未登录时直接清零，不发请求
   * - 请求失败不抛错，只清零，避免影响页面其他交互
   */
  async function loadUnreadCount() {
    const userStore = useUserStore()
    if (!userStore.isLogin) {
      unreadCount.value = 0
      return
    }
    loading.value = true
    try {
      const res = await getUnreadCountAPI()
      unreadCount.value = res.data ?? 0
    } catch {
      unreadCount.value = 0
    } finally {
      loading.value = false
    }
  }

  /** 本地递减（乐观更新用，非必须） */
  function decrement(count = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - count)
  }

  /** 清零（退出登录时调用） */
  function reset() {
    unreadCount.value = 0
  }

  return {
    unreadCount,
    loading,
    hasUnread,
    loadUnreadCount,
    decrement,
    reset,
  }
})
