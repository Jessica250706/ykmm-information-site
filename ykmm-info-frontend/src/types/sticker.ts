import type { PageRequest } from './common'

/* ========================================================
 * 表情包
 * ======================================================== */

/**
 * 表情包分页查询条件
 */
export interface StickerPageQueryDTO extends PageRequest {
  /**
   * 所属分组ID
   */
  groupId?: number
  /**
   * 标签关键字
   */
  label?: string
  /**
   * 1自定义图片 2 emoji
   */
  stickerType?: number
  [property: string]: any
}

/**
 * 表情包列表/详情返回
 */
export interface StickerVO {
  /**
   * 主键
   */
  id?: number
  /**
   * 所属分组ID
   */
  groupId?: number
  /**
   * 所属分组名称
   */
  groupName?: string
  /**
   * 标签
   */
  label?: string
  /**
   * 图片地址
   */
  imageUrl?: string
  /**
   * 对应emoji
   */
  emoji?: string
  /**
   * 1自定义图片 2 emoji
   */
  stickerType?: number
  /**
   * 文本：1自定义图片 2 emoji
   */
  stickerTypeDesc?: string
  /**
   * 创建者用户ID
   */
  creatorId?: number
  /**
   * 创建时间
   */
  createdAt?: string
  [property: string]: any
}

/**
 * 表情包新增/更新DTO
 */
export interface StickerDTO {
  /**
   * 主键
   */
  id?: number
  /**
   * 所属分组ID
   */
  groupId?: number
  /**
   * 标签
   */
  label?: string
  /**
   * 图片地址
   */
  imageUrl?: string
  /**
   * 对应emoji
   */
  emoji?: string
  /**
   * 1自定义图片 2 emoji
   */
  stickerType?: number
  [property: string]: any
}

/* ========================================================
 * 表情包分组
 * ======================================================== */

/**
 * 表情包分组分页查询条件
 */
export interface StickerGroupPageQueryDTO extends PageRequest {
  /**
   * 分组名称关键字
   */
  name?: string
  [property: string]: any
}

/**
 * 表情包分组列表/详情返回
 */
export interface StickerGroupVO {
  /**
   * 主键
   */
  id?: number
  /**
   * 分组名称
   */
  name?: string
  /**
   * 分组描述
   */
  description?: string
  /**
   * 排序
   */
  sort?: number
  /**
   * 创建者用户ID
   */
  creatorId?: number
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 更新时间
   */
  updatedAt?: string
  /**
   * 分组下表情包数量
   */
  stickerCount?: number
  [property: string]: any
}

/**
 * 表情包分组新增/更新DTO
 */
export interface StickerGroupDTO {
  /**
   * 主键
   */
  id?: number
  /**
   * 分组名称
   */
  name?: string
  /**
   * 分组描述
   */
  description?: string
  /**
   * 排序
   */
  sort?: number
  [property: string]: any
}

/**
 * 表情包分组（含下属表情包），用于表情选择器
 */
export interface StickerGroupWithStickersVO {
  /**
   * 分组ID
   */
  id?: number
  /**
   * 分组名称
   */
  name?: string
  /**
   * 分组描述
   */
  description?: string
  /**
   * 排序
   */
  sort?: number
  /**
   * 该分组下的表情包
   */
  stickers?: StickerVO[]
  [property: string]: any
}
