import type { DialogueVersionVO } from './dialogueVersion'

/**
 * 对话来源详情的最小结构
 *
 * StoryDetailVO（剧情）和 CardEpisodeVO（卡面话数）都隐式满足此结构。
 * useDialogueEdit / useVersionSelection 等泛型 composable 以它做约束，
 * 从而同时适配剧情和卡面话数。
 */
export interface DialogueSourceDetail {
  /** 主键：story.id 或 card_rc.id / card_rtv.id / card_rabbiter.id */
  id?: number
  /** 标题：剧情话标题 或 卡面话标题 */
  title?: string
  /** 对话版本列表 */
  versions?: DialogueVersionVO[]
}
