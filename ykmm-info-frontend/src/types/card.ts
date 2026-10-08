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
 * 数据
 *
 * CardVO
 */
export interface CardVO {
  /**
   * 附属剧情（RC / RTV / Rabitter）
   */
  attachedStory?: CardAttachedStoryVO
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
 * 附属剧情（RC / RTV / Rabitter）
 *
 * CardAttachedStoryVO
 */
export interface CardAttachedStoryVO {
  /**
   * 话列表
   */
  episodes?: CardEpisodeVO[]
  /**
   * 附属剧情类型：1-RC 2-RTV 3-Rabitter
   */
  storyType?: number
  /**
   * 类型标签
   */
  storyTypeLabel?: string
  [property: string]: any
}

/**
 * 卡面图片返回
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
 */
export interface CardPersonVO {
  /**
   * 头像
   */
  avatar?: string
  /**
   * 卡面ID（仅用于批量查询分组，不返回给前端）
   */
  cardId?: number
  /**
   * 中文名
   */
  nameCn?: string
  /**
   * 人物ID
   */
  personId?: number
  /**
   * 代表色
   */
  themeColor?: string
  [property: string]: any
}

/**
 * CardDTO
 */
export interface CardDTO {
  /**
   * 附属剧情类型：0无 1RC 2RTV 3Rabitter
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

export interface CardEpisodeDialogueDTO {
  /**
   * 来源ID
   */
  sourceId: number
  /**
   * 来源类型：1-RC 2-RTV 3-Rabitter
   */
  sourceType: number
  [property: string]: any
}

/**
 * 数据
 */
export interface CardEpisodeVO {
  /**
   * 卡面ID
   */
  cardId?: number
  /**
   * 第几话
   */
  episodeNo?: number
  /**
   * 话ID（card_rc.id 或 card_rtv.id）
   */
  id?: number
  /**
   * 发起人角色ID（仅 RC）
   */
  initiatorRoleId?: number
  /**
   * 发起人角色名（仅 RC）
   */
  initiatorRoleName?: string
  /**
   * 话标题
   */
  title?: string
  /**
   * 该话下的所有对话版本
   */
  versions?: DialogueVersionVO[]
  [property: string]: any
}

/**
 * 对话版本返回
 */
export interface DialogueVersionVO {
  /**
   * 贡献者列表
   */
  contributors?: DialogueVersionContributorVO[]
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 1文字 2图片
   */
  format?: number
  formatLabel?: string
  id?: number
  images?: DialogueImageVO[]
  /**
   * 1中文 2日文
   */
  language?: number
  languageLabel?: string
  lines?: DialogueLineVO[]
  /**
   * RC 选项列表
   */
  options?: RcOptionVO[]
  /**
   * 1全部 2节选
   */
  scope?: number
  scopeLabel?: string
  /**
   * 来源ID
   */
  sourceId?: number
  /**
   * 来源类型
   */
  sourceType?: number
  /**
   * 来源类型标签
   */
  sourceTypeLabel?: string
  /**
   * 状态
   */
  status?: number
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
 * 对话图片
 */
export interface DialogueImageVO {
  id?: number
  sort?: number
  url?: string
  /**
   * 版本ID
   */
  versionId?: number
  [property: string]: any
}

/**
 * 对话句子
 */
export interface DialogueLineVO {
  content?: string
  id?: number
  /**
   * 是否内心独白：0否 1是
   */
  monologue?: number
  /**
   * 对应人物头像
   */
  personAvatar?: string
  /**
   * 对应人物ID
   */
  personId?: number
  /**
   * 对应人物中文名
   */
  personNameCn?: string
  /**
   * 应援色
   */
  personThemeColor?: string
  segments?: DialogueSegmentVO[]
  /**
   * 1左 2右
   */
  side?: number
  sort?: number
  speakerId?: number
  speakerName?: string
  /**
   * 版本ID
   */
  versionId?: number
  [property: string]: any
}

/**
 * 对话片段
 */
export interface DialogueSegmentVO {
  content?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 行ID
   */
  lineId?: number
  /**
   * 1文本 2表情包
   */
  segmentType?: number
  sort?: number
  stickerEmoji?: string
  stickerId?: number
  /**
   * 表情包图片
   */
  stickerImageUrl?: string
  /**
   * 表情包标签
   */
  stickerLabel?: string
  [property: string]: any
}

/**
 * RC 选项返回
 */
export interface RcOptionVO {
  /**
   * 答句ID
   */
  answerLineId?: number
  /**
   * 主键
   */
  id?: number
  /**
   * 问句ID
   */
  questionLineId?: number
  /**
   * RC ID
   */
  rcId?: number
  /**
   * 排序
   */
  sort?: number
  /**
   * 版本ID
   */
  versionId?: number
  [property: string]: any
}
