import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { loginAPI } from '@/api/auth'
import { ROLE } from '@/constants/index'
import type { LoginRequest } from '@/types/auth'
import type { UserInfo } from '@/types/user'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref<string | null>(null)
    const userInfo = ref<UserInfo | null>(null)

    const isLogin = computed(() => !!token.value)
    const role = computed(() => userInfo.value?.role)
    const isAdmin = computed(() => role.value === ROLE.ADMIN)

    const login = async (data: LoginRequest) => {
      const res = await loginAPI(data)
      token.value = res.data.token ?? null
      userInfo.value = res.data.user ?? null
      return res.data
    }

    function setAuth(nextToken: string, nextUser: UserInfo) {
      token.value = nextToken
      userInfo.value = nextUser
    }

    function logout() {
      token.value = null
      userInfo.value = null
    }

    // 兼容旧调用
    const clearUserInfo = logout

    return {
      token,
      userInfo,
      isLogin,
      role,
      isAdmin,
      login,
      setAuth,
      logout,
      clearUserInfo,
    }
  },
  { persist: true },
)
