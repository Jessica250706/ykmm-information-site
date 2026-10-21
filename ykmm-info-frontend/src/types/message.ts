import type { PageRequest } from './common'

export interface SysMessagePageQueryDTO extends PageRequest {
  /**
   * 是否已读：0-未读 1-已读
   */
  isRead?: number
  /**
   * 关键字：标题 / 内容模糊匹配
   */
  keyword?: string
  /**
   * 关联业务类型：1-卡面 2-对话版本 3-剧情
   */
  targetType?: number
  /**
   * 消息类型：1-审核通过 2-审核拒绝 3-系统通知
   */
  type?: number
  /**
   * 接收者用户ID。
   * <p>
   * 仅管理员生效：管理员可按用户筛选；普通用户该字段会被 Service 忽略，
   * 强制查询自己的消息，防止越权。
   */
  userId?: number
  [property: string]: any
}

/**
 * 系统消息 VO
 *
 * SysMessageVO
 */
export interface SysMessageVO {
  /**
   * 消息正文（生成时的快照）
   */
  content?: string
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 0-未读 1-已读
   */
  isRead?: number
  /**
   * 阅读时间
   */
  readAt?: string
  /**
   * 关联业务主键
   */
  targetId?: number
  /**
   * 关联业务类型：1-卡面 2-对话版本 3-剧情
   */
  targetType?: number
  /**
   * 关联业务类型标签，如「卡面」
   */
  targetTypeLabel?: string
  /**
   * 消息标题
   */
  title?: string
  /**
   * 消息类型：1-审核通过 2-审核拒绝 3-系统通知
   */
  type?: number
  /**
   * 消息类型标签，如「审核通过」
   */
  typeLabel?: string
  /**
   * 接收者用户ID，关联 sys_user.id
   */
  userId?: number
  [property: string]: any
}
