/**
 * 图片列表
 *
 * 对话图片
 */
export interface DialogueImageDTO {
  /**
   * 图片ID，编辑时使用
   */
  id?: number
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
 * 调整对话图片
 */
export interface DialogueLineSortDTO {
  /**
   * 图片ID顺序
   */
  imageIds?: number[]
  [property: string]: any
}

/**
 * 对话图片
 */
export interface DialogueImageVO {
  id?: number
  sort?: number
  url?: string
  [property: string]: any
}
