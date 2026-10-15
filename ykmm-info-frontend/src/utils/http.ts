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
const MSG_DEDUP_WINDOW = 2000 // 相同内容 2 秒内只弹一次

function showErrorOnce(msg: string) {
  if (!msg) return
  const now = Date.now()
  const last = recentMsgTime.get(msg) ?? 0
  if (now - last < MSG_DEDUP_WINDOW) return
  recentMsgTime.set(msg, now)
  ElMessage.error(msg)
}

/* ================== 未登录处理去重 ================== */
let isHandlingUnauthorized = false

function handleUnauthorized(msg = '未登录或登录已过期') {
  if (isHandlingUnauthorized) return
  isHandlingUnauthorized = true

  ElMessage.error(msg)

  const userStore = useUserStore()
  userStore.clearUserInfo()

  router.replace({ name: CommonRouteName.LOGIN }).finally(() => {
    // 给一个短暂冷却，防止跳转瞬间还没落地的请求又触发
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
      // 业务层面返回 401
      if (body.code === 401) {
        handleUnauthorized(body.msg)
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

    if (status === 401) {
      handleUnauthorized(msg)
    } else {
      showErrorOnce(msg)
    }

    return Promise.reject(e)
  },
)

export default httpInstance as HttpInstance
