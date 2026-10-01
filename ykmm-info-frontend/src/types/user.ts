import type { PageRequest } from './common'
import type { UserStatusValue, RoleValue } from '@/constants/index.ts'

/** 用户查询请求 */
export interface UserRequest extends PageRequest {
  /** 关键字：邮箱 / 昵称 / uid */
  keyword?: string
  /** 状态：1正常 0禁用，null不限 */
  status?: UserStatusValue
  /** 允许扩展其他字段 */
  [property: string]: any
}

/**
 * 用户信息（响应视图对象）
 *
 * UserInfo
 */
export interface UserInfo {
  avatar?: string
  createdAt?: string
  email?: string
  id?: number
  /**
   * 最后上线时间
   */
  lastLoginTime?: string
  nickname?: string
  /**
   * 角色：1-管理员 2-普通用户
   */
  role?: RoleValue
  status?: UserStatusValue
  uid?: string
  [property: string]: any
}

/**
 * UserEditDTO
 */
export interface UserEditDTO {
  avatar?: string
  email?: string
  nickname?: string
  /**
   * 1正常 0禁用
   */
  status?: UserStatusValue
  [property: string]: any
}

/**
 * UserStatusDTO
 */
export interface UserStatusDTO {
  /**
   * 1正常 0禁用
   */
  status?: UserStatusValue
  [property: string]: any
}
