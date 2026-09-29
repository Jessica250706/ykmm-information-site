import type {
  RegisterRequest,
  LoginResponse,
  LoginRequest,
  MapString,
  UserInfo,
  PasswordRequest,
} from '@/types/auth'
import request from '@/utils/http'

/**
 * @description: 用户注册
 */
export const registerAPI = (data: RegisterRequest) => {
  return request.post<LoginResponse>('/auth/register', data)
}

/**
 * @description: 用户登录
 */
export const loginAPI = (data: LoginRequest) => {
  return request.post<LoginResponse>('/auth/login', data)
}

/**
 * @description: 用户登出
 */
export const logoutAPI = () => {
  return request.post('/auth/logout')
}

/**
 * @description: 刷新 token
 */
export const refreshTokenAPI = () => {
  return request.post<MapString>('/auth/refresh')
}

/**
 * @description: 获取当前登录用户信息
 */
export const getCurrentUserInfoAPI = () => {
  return request.get<UserInfo>('/auth/me')
}

/**
 * @description: 修改密码
 */
export const changePasswordAPI = (data: PasswordRequest) => {
  return request.put('/auth/password', data)
}
