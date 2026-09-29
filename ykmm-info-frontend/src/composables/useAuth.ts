import { computed, ref } from 'vue'

const STORAGE_KEY = 'app-auth'

interface AuthState {
  token: string | null
  roles: string[]
}

function read(): AuthState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as AuthState) : { token: null, roles: [] }
  } catch {
    return { token: null, roles: [] }
  }
}

// 模块级单例，组件外（路由守卫）也能访问
const token = ref<string | null>(read().token)
const roles = ref<string[]>(read().roles)

function persist() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ token: token.value, roles: roles.value }))
}

export function useAuth() {
  const isLogin = computed(() => !!token.value)
  const isAdmin = computed(() => roles.value.includes('admin'))

  function login(mockToken: string, nextRoles: string[]) {
    token.value = mockToken
    roles.value = nextRoles
    persist()
  }

  function logout() {
    token.value = null
    roles.value = []
    persist()
  }

  return { token, roles, isLogin, isAdmin, login, logout }
}
