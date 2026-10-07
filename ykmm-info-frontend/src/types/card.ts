import type { PageRequest } from './common'

export interface CardPageQueryDTO extends PageRequest {
  /**
   * 属性
   */
  attribute?: number
  /**
   * 关键字：卡面名称模糊匹配
   */
  keyword?: string
  /**
   * 最高等级：1-SSR 2-UR
   */
  maxRarity?: number
  /**
   * 关联人物ID列表（多选，任选其一即匹配）
   */
  personIds?: number[]
  /**
   * 所属系列ID
   */
  seriesId?: number
  /**
   * 状态：1已发布 2待审核 3已拒绝
   */
  status?: number
  [property: string]: any
}

/**
 * 卡面返回
 *
 * CardVO
 */
export interface CardVO {
  /**
   * 附属剧情类型
   */
  attachedStoryType?: number
  /**
   * 附属剧情类型标签
   */
  attachedStoryTypeLabel?: string
  /**
   * 属性
   */
  attribute?: number
  /**
   * 属性标签
   */
  attributeLabel?: string
  /**
   * 关联 card_category.id
   */
  category?: number
  /**
   * 偶像小人ID
   */
  chibiId?: number
  /**
   * 造型ID
   */
  costumeId?: number
  /**
   * 服装类型
   */
  costumeType?: number
  /**
   * 服装类型标签
   */
  costumeTypeLabel?: string
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 首次入池时间
   */
  firstPoolTime?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 卡面图片列表
   */
  images?: CardImageVO[]
  /**
   * 最高等级
   */
  maxRarity?: number
  /**
   * 最高等级标签
   */
  maxRarityLabel?: string
  /**
   * 卡面名称
   */
  name?: string
  /**
   * 关联人物列表
   */
  persons?: CardPersonVO[]
  /**
   * 系列ID
   */
  seriesId?: number
  /**
   * 系列名
   */
  seriesName?: string
  /**
   * 魅力技能描述
   */
  skillDesc?: string
  /**
   * 状态
   */
  status?: number
  /**
   * 状态标签
   */
  statusLabel?: string
  [property: string]: any
}

/**
 * 卡面图片返回
 *
 * CardImageVO
 */
export interface CardImageVO {
  /**
   * 主键
   */
  id?: number
  /**
   * 图片类型
   */
  imageType?: number
  /**
   * 图片类型标签
   */
  imageTypeLabel?: string
  /**
   * 排序
   */
  sort?: number
  /**
   * 图片地址
   */
  url?: string
  [property: string]: any
}

/**
 * 卡面关联人物
 *
 * CardPersonVO
 */
export interface CardPersonVO {
  /**
   * 头像
   */
  avatar?: string
  /**
   * 中文名
   */
  nameCn?: string
  /**
   * 人物ID
   */
  personId?: number
  [property: string]: any
}

/**
 * CardDTO
 */
export interface CardDTO {
  /**
   * 附属剧情类型：0无 1RC 2RTV 3Rabbiter
   */
  attachedStoryType?: number
  /**
   * 属性：1-Shout 2-Beat 3-Melody
   */
  attribute?: number
  /**
   * 关联 card_category.id
   */
  category?: number
  /**
   * 偶像小人ID
   */
  chibiId?: number
  /**
   * 造型ID
   */
  costumeId?: number
  /**
   * 服装类型：1偶像小人 2 3D造型
   */
  costumeType?: number
  /**
   * 首次入池时间
   */
  firstPoolTime?: string
  /**
   * 卡面图片列表
   */
  images?: CardImageDTO[]
  /**
   * 最高等级：1-SSR 2-UR
   */
  maxRarity?: number
  /**
   * 卡面名称
   */
  name?: string
  /**
   * 关联人物ID列表
   */
  personIds?: number[]
  /**
   * 所属系列ID
   */
  seriesId?: number
  /**
   * 魅力技能描述
   */
  skillDesc?: string
  [property: string]: any
}

/**
 * 卡面图片参数
 *
 * CardImageDTO
 */
export interface CardImageDTO {
  /**
   * 图片类型：1-R 2-SR 3-SSR 4-UR竖卡 5-UR横卡
   */
  imageType?: number
  /**
   * 排序
   */
  sort?: number
  /**
   * 图片地址
   */
  url?: string
  [property: string]: any
}
