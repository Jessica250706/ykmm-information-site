/**
 * 注册请求参数，包含邮箱、密码和昵称
 *
 * RegisterRequest
 */
export interface RegisterRequest {
  email?: string
  nickname?: string
  password?: string
  [property: string]: any
}

/**
 * 数据
 *
 * LoginResponse
 */
export interface LoginResponse {
  token?: string
  user?: UserInfo
  [property: string]: any
}

/**
 * UserInfo
 */
export interface UserInfo {
  avatar?: string
  createdAt?: string
  email?: string
  id?: number
  nickname?: string
  status?: number
  uid?: string
  [property: string]: any
}

/**
 * 登录请求参数，包含邮箱和密码
 *
 * LoginRequest
 */
export interface LoginRequest {
  email?: string
  password?: string
  [property: string]: any
}

/**
 * 数据
 *
 * MapString
 */
export interface MapString {
  key?: string
  [property: string]: any
}

/**
 * 密码请求参数，包含旧密码和新密码
 *
 * PasswordRequest
 */
export interface PasswordRequest {
  newPassword?: string
  oldPassword?: string
  [property: string]: any
}
