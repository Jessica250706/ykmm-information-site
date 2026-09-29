import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { LoginRequest, UserInfo } from '@/types/auth'
import { loginAPI } from '@/api/auth'

export const useUserStore = defineStore(
  'user',
  () => {
    // state
    const userInfo = ref<UserInfo>()

    // action
    const getUserInfo = async (data: LoginRequest) => {
      const res = await loginAPI(data)
      userInfo.value = res.result
    }

    const clearUserInfo = () => {
      userInfo.value = undefined
    }

    return {
      userInfo,
      getUserInfo,
      clearUserInfo,
    }
  },
  { persist: true },
)
