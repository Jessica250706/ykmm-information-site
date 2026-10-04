import type { PageRequest } from './common'
import type { DialogueImageVO } from './dialogueImage'
import type { DialogueLineVO } from './dialogueLine'

export interface DialogueVersionPageQueryDTO extends PageRequest {
  /**
   * 形式：1文字 2图片
   */
  format?: number
  /**
   * 语言：1中文 2日文
   */
  language?: number
  /**
   * 范围：1全部 2节选
   */
  scope?: number
  /**
   * 来源ID
   */
  sourceId?: number
  /**
   * 来源类型：1剧情 2卡面RTV 3卡面RC
   */
  sourceType?: number
  [property: string]: any
}

/**
 * 对话版本返回
 */
export interface DialogueVersionVO {
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
  [property: string]: any
}

export interface DialogueVersionSourceDTO {
  /**
   * 来源ID
   */
  sourceId: number
  /**
   * 来源类型
   */
  sourceType: number
  [property: string]: any
}

/**
 * 创建参数
 */
export interface DialogueVersionDTO {
  /**
   * 1文字 2图片
   */
  format?: number
  /**
   * 1中文 2日文
   */
  language?: number
  /**
   * 1全部 2节选
   */
  scope?: number
  /**
   * 对应 story.id / card_rtv.id / card_rc.id
   */
  sourceId?: number
  /**
   * 1剧情 2卡面RTV 3卡面RC
   */
  sourceType?: number
  [property: string]: any
}

/**
 * 对话版本选项（供下拉框使用）
 */
export interface DialogueVersionOptionVO {
  /**
   * 是否已存在
   */
  exists?: boolean
  /**
   * 格式：1文字 2图片
   */
  format?: number
  /**
   * 格式标签
   */
  formatLabel?: string
  /**
   * 完整展示文案，如「中文 · 文字 · 全部」
   */
  label?: string
  /**
   * 语言：1中文 2日文
   */
  language?: number
  /**
   * 语言标签
   */
  languageLabel?: string
  /**
   * 范围：1全部 2节选
   */
  scope?: number
  /**
   * 范围标签
   */
  scopeLabel?: string
  /**
   * 版本ID，当前来源下不存在时为 null
   */
  versionId?: number
  [property: string]: any
}
