import type { PageRequest } from './common'

/**
 * 数据
 */
export interface QuizConfigVO {
  /**
   * 是否开启答题门槛：1-开启 0-关闭
   */
  enabled?: number
  /**
   * 固定题数量（统计，只读）
   */
  fixedCount?: number
  /**
   * 最大答题次数：0-不限
   */
  maxAttempts?: number
  /**
   * 通过所需答对题数
   */
  passCount?: number
  /**
   * 随机题数量（统计，只读）
   */
  randomCount?: number
  /**
   * 每次答题的总题数
   */
  totalCount?: number
  /**
   * 更新时间
   */
  updatedAt?: string
  [property: string]: any
}

/**
 * QuizConfigDTO
 */
export interface QuizConfigDTO {
  /**
   * 是否开启答题门槛：1-开启 0-关闭
   */
  enabled: number
  /**
   * 最大答题次数：0-不限
   */
  maxAttempts: number
  /**
   * 通过所需答对题数
   */
  passCount: number
  /**
   * 每次答题的总题数（含固定题）
   */
  totalCount: number
  [property: string]: any
}

export interface QuestionPageQueryDTO extends PageRequest {
  /**
   * 是否固定题：0-随机 1-固定
   */
  isFixed?: number
  /**
   * 关键词：题干模糊匹配
   */
  keyword?: string
  /**
   * 页码，从 1 开始
   */
  pageNum?: number
  /**
   * 每页条数，默认 20，最大 100
   */
  pageSize?: number
  /**
   * 题型：1-单选 2-多选
   */
  questionType?: number
  /**
   * 状态：1-启用 0-禁用
   */
  status?: number
  [property: string]: any
}

/**
 * 题目列表项 VO（不含选项详情）
 */
export interface QuestionListVO {
  /**
   * 题干
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
   * 是否固定题：0-随机 1-固定
   */
  isFixed?: number
  /**
   * 选项数
   */
  optionCount?: number
  /**
   * 题型：1-单选 2-多选
   */
  questionType?: number
  /**
   * 题型标签：1-单选 2-多选
   */
  questionTypeLabel?: string
  /**
   * 排序
   */
  sort?: number
  /**
   * 状态：1-启用 0-禁用
   */
  status?: number
  /**
   * 状态标签：1-启用 0-禁用
   */
  statusLabel?: string
  [property: string]: any
}

/**
 * 数据
 */
export interface QuestionVO {
  /**
   * 题干
   */
  content?: string
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 创建者ID
   */
  creatorId?: number
  /**
   * 答案解析（可选）
   */
  explanation?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 是否固定题：0-随机 1-固定
   */
  isFixed?: number
  /**
   * 选项
   */
  options?: QuestionOptionVO[]
  /**
   * 题型：1-单选 2-多选
   */
  questionType?: number
  /**
   * 题型标签：1-单选 2-多选
   */
  questionTypeLabel?: string
  /**
   * 排序
   */
  sort?: number
  /**
   * 状态：1-启用 0-禁用
   */
  status?: number
  /**
   * 状态标签：1-启用 0-禁用
   */
  statusLabel?: string
  /**
   * 更新时间
   */
  updatedAt?: string
  [property: string]: any
}

/**
 * 题目-选项 VO
 */
export interface QuestionOptionVO {
  /**
   * 题干
   */
  content?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 是否正确答案（管理端可见，用户答题时不返回）
   */
  isCorrect?: number
  /**
   * 排序
   */
  sort?: number
  [property: string]: any
}

/**
 * QuestionDTO
 */
export interface QuestionDTO {
  content: string
  /**
   * 答案解析（可选）
   */
  explanation?: string
  /**
   * 是否固定题：0-随机 1-固定
   */
  isFixed: number
  /**
   * 选项列表
   */
  options: QuestionOptionDTO[]
  /**
   * 题型：1-单选 2-多选
   */
  questionType: number
  /**
   * 排序
   */
  sort?: number
  /**
   * 状态：1-启用 0-禁用
   */
  status: number
  [property: string]: any
}

/**
 * 题目-选项 DTO
 */
export interface QuestionOptionDTO {
  content: string
  /**
   * 选项ID（编辑时用于匹配；新增时为 null）
   */
  id?: number
  /**
   * 是否正确答案：0-否 1-是
   */
  isCorrect: number
  /**
   * 排序
   */
  sort?: number
  [property: string]: any
}

/**
 * 数据
 */
export interface QuizAttemptVO {
  /**
   * 主键
   */
  attemptId?: number
  /**
   * 本次通过所需题数（配置快照）
   */
  passCount?: number
  /**
   * 问题
   */
  questions?: QuizQuestionVO[]
  /**
   * 开始时间
   */
  startedAt?: string
  /**
   * 本次总题数（配置快照）
   */
  totalCount?: number
  [property: string]: any
}

/**
 * 答题中的题目 VO（选项已随机排序、不含正确答案）
 */
export interface QuizQuestionVO {
  /**
   * 题干快照
   */
  content?: string
  /**
   * 是否固定题快照
   */
  isFixed?: number
  /**
   * 选项（按 option_order 排好，不含 isCorrect）
   */
  options?: OptionItem[]
  /**
   * 原题ID（仅参考）
   */
  questionId?: number
  /**
   * 题型快照
   */
  questionType?: number
  /**
   * 在本次答题中的顺序
   */
  sort?: number
  [property: string]: any
}

export interface OptionItem {
  content?: string
  id?: number
  [property: string]: any
}

/**
 * QuizSubmitDTO
 */
export interface QuizSubmitDTO {
  answers: QuizAnswerDTO[]
  attemptId: number
  [property: string]: any
}

/**
 * 单题答案 DTO
 */
export interface QuizAnswerDTO {
  /**
   * 用户选择的选项ID列表（多选时多个）
   */
  optionIds: number[]
  questionId: number
  [property: string]: any
}

/**
 * 数据
 */
export interface QuizSubmitResultVO {
  attemptId?: number
  /**
   * 实际答对数
   */
  correctCount?: number
  /**
   * 本次通过所需题数（配置快照）
   */
  passCount?: number
  /**
   * 是否通过
   */
  passed?: boolean
  questions?: QuestionResult[]
  /**
   * 状态：0-进行中 1-已通过 2-未通过
   */
  status?: number
  /**
   * 状态标签：0-进行中 1-已通过 2-未通过
   */
  statusLabel?: string
  /**
   * 本次总题数（配置快照）
   */
  totalCount?: number
  [property: string]: any
}

/**
 * QuestionResult
 */
export interface QuestionResult {
  content?: string
  correctAnswer?: number[]
  explanation?: string
  isCorrect?: boolean
  questionId?: number
  userAnswer?: number[]
  [property: string]: any
}

/**
 * 数据
 *
 * QuizStatusVO
 */
export interface QuizStatusVO {
  /**
   * 已用次数
   */
  attemptsUsed?: number
  /**
   * 是否还可以开始新答题
   */
  canAttempt?: boolean
  /**
   * 答题是否开启
   */
  enabled?: boolean
  /**
   * 是否有进行中的答题
   */
  hasInProgress?: boolean
  /**
   * 进行中的 attemptId
   */
  inProgressAttemptId?: number
  /**
   * 最大次数（0=不限）
   */
  maxAttempts?: number
  /**
   * 是否已通过
   */
  quizPassed?: boolean
  [property: string]: any
}
