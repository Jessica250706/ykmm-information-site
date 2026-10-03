import type { DialogueLineVO } from './dialogueLine'

/**
 * 数据
 */
export interface DialogueTxtParseVO {
  /**
   * 错误信息
   */
  errors?: string[]
  /**
   * 解析出的句子
   */
  lines?: DialogueLineVO[]
  /**
   * 全部识别到的说话人名称
   */
  speakers?: string[]
  /**
   * 未匹配到角色的说话人
   */
  unmatchedSpeakers?: string[]
  /**
   * 未匹配到表情包的标签
   */
  unmatchedStickers?: string[]
  [property: string]: any
}
