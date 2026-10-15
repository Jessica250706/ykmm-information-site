import axios from 'axios'
import { ElMessage } from 'element-plus'
import qs from 'qs'
import { CommonRouteName } from '@/constants'
import router from '@/router'
import { useUserStore } from '@/stores'
import type { AxiosRequestConfig } from 'axios'
import 'element-plus/theme-chalk/el-message.css'

export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

interface HttpInstance {
  <T = unknown>(config: AxiosRequestConfig): Promise<ApiResponse<T>>
  <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  request<T = unknown>(config: AxiosRequestConfig): Promise<ApiResponse<T>>
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  head<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  options<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>>
  post<T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig,
  ): Promise<ApiResponse<T>>
  put<T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig,
  ): Promise<ApiResponse<T>>
  patch<T = unknown>(
    url: string,
    data?: unknown,
    config?: AxiosRequestConfig,
  ): Promise<ApiResponse<T>>
}

/* ================== 错误提示去重 ================== */
const recentMsgTime = new Map<string, number>()
const MSG_DEDUP_WINDOW = 2000

function showErrorOnce(msg: string) {
  if (!msg) return
  const now = Date.now()
  const last = recentMsgTime.get(msg) ?? 0
  if (now - last < MSG_DEDUP_WINDOW) return
  recentMsgTime.set(msg, now)
  ElMessage.error(msg)
}

/* ================== 401 处理 ================== */

/**
 * 这些前缀下的接口在 401 时不强制登录
 * - /auth：登录、注册、刷新、me 等
 * - /pub ：游客接口，本就不该 401
 */
const SILENT_401_PREFIXES = ['/auth', '/pub']

function isSilent401Url(url?: string): boolean {
  if (!url) return false
  return SILENT_401_PREFIXES.some((p) => url.startsWith(p))
}

let isHandlingUnauthorized = false

function handleUnauthorized(url: string | undefined, msg: string) {
  const userStore = useUserStore()
  const currentRoute = router.currentRoute.value
  const onPublicPage = !currentRoute.meta.requiresAuth

  // 静默情况：/auth、/pub 接口，或当前在公开页面
  if (isSilent401Url(url) || onPublicPage) {
    userStore.clearUserInfo()
    console.warn('[http] 静默 401：', url, msg)
    return
  }

  // 以下为「已登录 + 访问需要认证的页面」时 token 失效
  if (isHandlingUnauthorized) return
  isHandlingUnauthorized = true

  ElMessage.error(msg)
  userStore.clearUserInfo()

  router.replace({ name: CommonRouteName.LOGIN }).finally(() => {
    setTimeout(() => {
      isHandlingUnauthorized = false
    }, 1500)
  })
}

/* ================== 实例 ================== */
const httpInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  paramsSerializer: {
    serialize: (params) =>
      qs.stringify(params, {
        arrayFormat: 'repeat',
        skipNulls: true,
      }),
  },
})

httpInstance.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const token = userStore.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (e) => Promise.reject(e),
)

httpInstance.interceptors.response.use(
  (res) => {
    const body = res.data as ApiResponse

    if (body.code !== 200) {
      if (body.code === 401) {
        handleUnauthorized(res.config.url, body.msg || '未登录')
      } else {
        showErrorOnce(body.msg || '请求失败')
      }
      return Promise.reject(new Error(body.msg))
    }

    return body as unknown as typeof res
  },
  (e) => {
    const status = e?.response?.status
    const msg = e?.response?.data?.msg ?? e?.message ?? '请求失败'
    const url = e?.config?.url as string | undefined

    if (status === 401) {
      handleUnauthorized(url, msg)
    } else {
      showErrorOnce(msg)
    }

    return Promise.reject(e)
  },
)

export default httpInstance as HttpInstance
