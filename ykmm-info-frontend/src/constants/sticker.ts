// ---------- 表情包类型 ----------
export const STICKER_TYPE = {
  /** 自定义图片 */
  IMAGE: 1,
  /** emoji */
  EMOJI: 2,
} as const

export type StickerTypeValue = (typeof STICKER_TYPE)[keyof typeof STICKER_TYPE]

export const STICKER_TYPE_LABEL: Record<StickerTypeValue, string> = {
  [STICKER_TYPE.IMAGE]: '自定义图片',
  [STICKER_TYPE.EMOJI]: 'emoji',
}

export const STICKER_TYPE_OPTIONS: Array<{ label: string; value: StickerTypeValue }> = [
  { label: '自定义图片', value: STICKER_TYPE.IMAGE },
  { label: 'emoji', value: STICKER_TYPE.EMOJI },
]
