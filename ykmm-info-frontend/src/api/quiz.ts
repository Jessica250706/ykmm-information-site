import type { PageResult } from '@/types/common'
import type {
  QuestionDTO,
  QuestionListVO,
  QuestionPageQueryDTO,
  QuestionVO,
  QuizAttemptVO,
  QuizConfigDTO,
  QuizConfigVO,
  QuizStatusVO,
  QuizSubmitDTO,
  QuizSubmitResultVO,
} from '@/types/quiz'
import request from '@/utils/http'

// ---------- 公共端 ----------

/** 当前用户是否需要答题 */
export function isQuizRequiredAPI() {
  return request.get<boolean>('/pub/quiz/required')
}

// ---------- 用户端 - 答题 ----------

/** 开始答题 */
export function startQuizAPI() {
  return request.post<QuizAttemptVO>('/user/quiz/start')
}

/** 提交答案 */
export function submitQuizAPI(data: QuizSubmitDTO) {
  return request.post<QuizSubmitResultVO>('/user/quiz/submit', data)
}

/** 查询当前用户答题状态 */
export function getQuizStatusAPI() {
  return request.get<QuizStatusVO>('/user/quiz/status')
}

// ---------- 管理端 - 题目 ----------

/** 分页查询题目 */
export function pageQuestionsAPI(params: QuestionPageQueryDTO) {
  return request.get<PageResult<QuestionListVO>>('/admin/quiz/question/page', { params })
}

/** 题目详情 */
export function getQuestionDetailAPI(id: number) {
  return request.get<QuestionVO>(`/admin/quiz/question/${id}`)
}

/** 新增题目 */
export function createQuestionAPI(data: QuestionDTO) {
  return request.post<number>('/admin/quiz/question', data)
}

/** 编辑题目 */
export function updateQuestionAPI(id: number, data: QuestionDTO) {
  return request.put<void>(`/admin/quiz/question/${id}`, data)
}

/** 删除题目 */
export function deleteQuestionAPI(id: number) {
  return request.delete<void>(`/admin/quiz/question/${id}`)
}

// ---------- 管理端 - 答题配置 ----------

/** 获取答题配置 */
export function getQuizConfigAPI() {
  return request.get<QuizConfigVO>('/admin/quiz/config')
}

/** 修改答题配置 */
export function updateQuizConfigAPI(data: QuizConfigDTO) {
  return request.put<void>('/admin/quiz/config', data)
}
