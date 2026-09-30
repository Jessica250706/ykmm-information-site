import { computed, ref } from 'vue'
import type { UserInfo } from '@/types/user'
import { ROLE } from '@/constants/index'

const STORAGE_KEY = 'app-auth'

interface AuthState {
  token: string | null
  userInfo: UserInfo | null
}

function read(): AuthState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as AuthState) : { token: null, userInfo: null }
  } catch {
    return { token: null, userInfo: null }
  }
}

// 模块级单例，路由守卫里也能用
const initial = read()
const token = ref<string | null>(initial.token)
const userInfo = ref<UserInfo | null>(initial.userInfo)

function persist() {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify({ token: token.value, userInfo: userInfo.value }),
  )
}

export function useAuth() {
  const isLogin = computed(() => !!token.value)

  /** 角色：1-管理员 2-普通用户 */
  const role = computed(() => userInfo.value?.role)
  const isAdmin = computed(() => role.value === ROLE.ADMIN)

  function setAuth(nextToken: string, nextUser: UserInfo) {
    token.value = nextToken
    userInfo.value = nextUser
    persist()
  }

  function logout() {
    token.value = null
    userInfo.value = null
    persist()
  }

  return { token, userInfo, isLogin, role, isAdmin, setAuth, logout }
}
