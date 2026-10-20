import type { PageRequest } from './common'
import type { UserStatusValue, RoleValue } from '@/constants'

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

export interface SendRebindEmailCodeDTO {
  /**
   * 目标邮箱（NEW 时必填；OLD 时忽略，后端强制用当前用户邮箱）
   */
  email?: string
  /**
   * 目标类型：OLD 发到旧邮箱 / NEW 发到新邮箱
   */
  target: string
  [property: string]: any
}

export interface RebindEmailDTO {
  newCode: string
  newEmail: string
  oldCode: string
  [property: string]: any
}

export interface VerifyRebindOldCodeDTO {
  code: string
  [property: string]: any
}
