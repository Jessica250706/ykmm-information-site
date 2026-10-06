import type { PageRequest } from './common'

export interface DialogueVersionContributorPageQueryDTO extends PageRequest {
  /**
   * 贡献者角色：1内容作者 2代传管理员 3协作者
   */
  contributorRole?: number
  /**
   * 贡献者用户ID
   */
  userId?: number
  /**
   * 对话版本ID
   */
  versionId?: number
  [property: string]: any
}

/**
 * 对话版本贡献者返回
 */
export interface DialogueVersionContributorVO {
  /**
   * 头像
   */
  avatar?: string
  /**
   * 无账号贡献者姓名
   */
  contributorName?: string
  /**
   * 贡献者角色
   */
  contributorRole?: number
  /**
   * 贡献者角色标签
   */
  contributorRoleLabel?: string
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 展示名：优先昵称，其次姓名，都没有则用"匿名"
   */
  displayName?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 昵称（有账号时）
   */
  nickname?: string
  /**
   * 用户随机标识
   */
  uid?: string
  /**
   * 贡献者用户ID，可为空
   */
  userId?: number
  /**
   * 对话版本ID
   */
  versionId?: number
  [property: string]: any
}

/**
 * 故事贡献者（按用户 / 姓名聚合）
 */
export interface StoryContributorVO {
  /**
   * 头像
   */
  avatar?: string
  /**
   * 无账号贡献者姓名
   */
  contributorName?: string
  /**
   * 展示名：优先昵称，其次姓名，都没有则"匿名"
   */
  displayName?: string
  /**
   * 昵称（有账号时）
   */
  nickname?: string
  /**
   * 用户随机标识
   */
  uid?: string
  /**
   * 贡献者用户ID，可为空
   */
  userId?: number
  /**
   * 贡献的版本数
   */
  versionCount?: number
  [property: string]: any
}

/**
 * 新增参数
 */
export interface DialogueVersionContributorDTO {
  /**
   * 无账号贡献者姓名，userId 为空时必填
   */
  contributorName?: string
  /**
   * 贡献者角色：1内容作者 2代传管理员 3协作者
   */
  contributorRole?: number
  /**
   * 贡献者用户ID，可为空（无账号时只填 contributorName）
   */
  userId?: number
  /**
   * 对话版本ID
   */
  versionId?: number
  [property: string]: any
}
