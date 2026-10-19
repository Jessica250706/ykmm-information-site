import type { PageRequest } from './common'
import type { DialogueImageVO } from './dialogueImage'
import type { DialogueLineVO } from './dialogueLine'
import type { RcOptionVO } from './card'
import type { DialogueVersionContributorVO } from './dialogueVersionContributor'

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
   * 来源展示信息（我的版本等场景使用）
   */
  source?: DialogueVersionSourceVO
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
 * 来源展示信息（我的版本等场景使用）
 */
export interface DialogueVersionSourceVO {
  /**
   * 所属卡面ID（仅 card 类，便于前端跳转）
   */
  cardId?: number
  /**
   * 剧情分类类型（仅 story 类，便于前端拼路由 params.type）
   */
  categoryType?: number
  /**
   * 话数（仅 card 类）
   */
  episodeNo?: number
  /**
   * 话标题（仅 card 类）
   */
  episodeTitle?: string
  /**
   * 来源ID：card_rc.id / card_rtv.id / card_rabitter.id / story.id
   */
  sourceId?: number
  /**
   * 来源类型：1-RC 2-RTV 3-Rabitter 4-剧情
   */
  sourceType?: number
  /**
   * 副标题：
   * - card 类：卡面系列名
   * - story 类：剧情分类名
   */
  subtitle?: string
  /**
   * 主标题：
   * - card 类：卡面名称
   * - story 类：剧情话标题
   */
  title?: string
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
   * 无账号贡献者姓名（user_id 为空时使用）
   */
  contributorName?: string
  /**
   * 贡献者用户ID，可为空（无账号时只保留名字）
   */
  contributorUserId?: number
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
   * 当前用户能否修改
   */
  canOperate?: boolean
  /**
   * 贡献者列表；版本不存在时为空列表。
   * <p>
   * 每项含 displayName（有账号优先取昵称，无账号取 contributorName）、
   * contributorRole（1内容作者 2代传管理员 3协作者）等。
   */
  contributors?: DialogueVersionContributorVO[]
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
   * 版本状态：1已发布 2待审核 3已拒绝；版本不存在时为 null
   */
  status?: number
  /**
   * 版本状态标签；版本不存在时为 null
   */
  statusLabel?: string
  /**
   * 版本ID，当前来源下不存在时为 null
   */
  versionId?: number
  [property: string]: any
}
