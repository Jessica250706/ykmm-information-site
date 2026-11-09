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

  if (isSilent401Url(url) || onPublicPage) {
    userStore.clearUserInfo()
    console.warn('[http] 静默 401：', url, msg)
    return
  }

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

/* ================== Blob 错误文案提取 ================== */
/**
 * 某些后端会用 200 + text/json 返回业务错误（比如 "PRO FEATURE ONLY"）。
 * 前端需要把 Blob 内容读出来，尽量取出可读文案。
 */
async function readBlobMessage(blob: Blob): Promise<string> {
  try {
    const text = await blob.text()
    if (!text) return ''
    try {
      const json = JSON.parse(text)
      return json.msg || json.message || ''
    } catch {
      return text
    }
  } catch {
    return ''
  }
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
  // 改成 async，因为 blob 错误需要异步读内容
  async (res) => {
    /* ---------- 1. 文件流（blob）响应：直接放行 ---------- */
    if (res.config?.responseType === 'blob' || res.data instanceof Blob) {
      const blob = res.data as Blob
      const contentType = blob.type || ''

      // 后端如果按"标准错误"返回 JSON，就读出来走统一错误处理
      if (contentType.includes('application/json')) {
        const msg = (await readBlobMessage(blob)) || '请求失败'
        showErrorOnce(msg)
        return Promise.reject(new Error(msg))
      }

      // 正常文件流：原样返回 Blob，交给调用方处理（下载 / 预览）
      // 注意：这里返回的是 res.data 而不是 body，保持和调用方 as unknown as Blob 的约定一致
      return Object.assign(blob, { headers: res.headers }) as unknown as typeof res
    }

    /* ---------- 2. 普通 JSON 响应 ---------- */
    const body = res.data as ApiResponse

    if (body.code !== 200) {
      if (body.code === 401) {
        handleUnauthorized(res.config.url, body.msg || '未登录')
      } else if (body.code === 423) {
        ElMessage.warning(body.msg || '请先完成答题')
        void router.replace({ name: CommonRouteName.QUIZ })
        return Promise.reject(new Error('QUIZ_REQUIRED'))
      } else {
        showErrorOnce(body.msg || '请求失败')
      }
      return Promise.reject(new Error(body.msg || '请求失败'))
    }

    return body as unknown as typeof res
  },

  // error 分支改成 async，以支持读取 blob 错误体
  async (e) => {
    const status = e?.response?.status
    const url = e?.config?.url as string | undefined
    const data = e?.response?.data

    let msg = e?.message ?? '请求失败'

    // 错误响应也可能是 blob（例如后端用非 2xx + blob 返回）
    if (data instanceof Blob) {
      const text = await readBlobMessage(data)
      if (text) msg = text
    } else if (data?.msg) {
      msg = data.msg
    } else if (data?.message) {
      msg = data.message
    }

    if (status === 401) {
      handleUnauthorized(url, msg)
    } else {
      showErrorOnce(msg)
    }

    // 保留原始 AxiosError（含 response / config），只把 message 改成可读文案
    // 避免破坏其他依赖 e.response 的代码
    e.message = msg
    return Promise.reject(e)
  },
)

export default httpInstance as HttpInstance
