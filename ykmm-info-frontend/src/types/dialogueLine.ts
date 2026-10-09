/**
 * 对话句子返回
 */
export interface DialogueLineVO {
  content?: string
  /**
   * 对话角色：0-普通 1-问句 2-回答
   */
  dialogueRole?: number
  /**
   * 对话角色标签（"普通" / "问句" / "回答"）
   */
  dialogueRoleLabel?: string
  id?: number
  /**
   * 是否内心独白：0否 1是
   */
  monologue?: number
  /**
   * 选项编号（仅 RC 的问句/回答有效）
   */
  optionNumber?: number
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
 *
 * DialogueSegmentVO
 */
export interface DialogueSegmentVO {
  content?: string
  /**
   * 1文本 2表情包
   */
  segmentType?: number
  sort?: number
  stickerEmoji?: string
  stickerId?: number
  /**
   * 表情包标签
   */
  stickerLabel?: string
  stickerUrl?: string
  [property: string]: any
}

/**
 * 确认后的句子
 */
export interface DialogueTxtImportDTO {
  /**
   * 确认后的句子列表
   */
  lines?: DialogueLineDTO[]
  [property: string]: any
}

/**
 * 对话句子
 */
export interface DialogueLineDTO {
  /**
   * 文本内容，可含表情包标签
   */
  content?: string
  /**
   * 对话角色：0-普通 1-问句 2-回答
   */
  dialogueRole?: number
  /**
   * 句子ID，编辑时使用，新增时为空
   */
  id?: number
  /**
   * 是否内心独白：0否 1是
   */
  monologue?: number
  /**
   * 选项编号（仅 RC 的问句/回答有效）
   */
  optionNumber?: number
  /**
   * RC聊天：1左 2右，非RC可为空
   */
  side?: number
  /**
   * 排序
   */
  sort?: number
  /**
   * 说话角色ID
   */
  speakerId?: number
  [property: string]: any
}

/**
 * 调整对话句子
 */
export interface DialogueLineSortDTO {
  /**
   * 句子ID顺序
   */
  lineIds?: number[]
  [property: string]: any
}
