/**
 * 对话句子返回
 *
 * DialogueLineVO
 */
export interface DialogueLineVO {
  content?: string
  id?: number
  /**
   * 对应人物ID
   */
  personId?: number
  /**
   * 对应人物中文名
   */
  personNameCn?: string
  segments?: DialogueSegmentVO[]
  /**
   * 1左 2右
   */
  side?: number
  sort?: number
  speakerId?: number
  speakerName?: string
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
 * 对话句子
 *
 * DialogueLineDTO
 */
export interface DialogueLineDTO {
  /**
   * 文本内容，可含表情包标签
   */
  content?: string
  /**
   * 句子ID，编辑时使用，新增时为空
   */
  id?: number
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
