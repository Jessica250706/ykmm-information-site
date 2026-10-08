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
      ElMessage.error(body.msg || '请求失败')
      return Promise.reject(new Error(body.msg))
    }

    return body as unknown as typeof res
  },
  (e) => {
    // 统一错误提示
    ElMessage.error(e.response?.data?.msg ?? e.message ?? '请求失败')
    // 401 token 失效处理
    if (e?.response?.status === 401) {
      // 1. 清除本地用户数据
      const userStore = useUserStore()
      userStore.clearUserInfo()
      // 2. 跳转到登录页
      router.replace({ name: CommonRouteName.LOGIN })
    }
    return Promise.reject(e)
  },
)

export default httpInstance as HttpInstance
