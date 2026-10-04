/**
 * 对话版本相关枚举
 * - 形式 format：1文字 2图片
 * - 语言 language：1中文 2日文
 * - 范围 scope：1全部 2节选
 */

/* ============================================================
 * 形式：format
 * ============================================================ */

export const VERSION_FORMAT = {
  TEXT: 1,
  IMAGE: 2,
} as const

export type VersionFormatValue = (typeof VERSION_FORMAT)[keyof typeof VERSION_FORMAT]

export const VERSION_FORMAT_LABEL: Record<VersionFormatValue, string> = {
  [VERSION_FORMAT.TEXT]: '文字',
  [VERSION_FORMAT.IMAGE]: '图片',
}

/** 下拉选项，给 el-select 直接用 */
export const VERSION_FORMAT_OPTIONS = [
  { value: VERSION_FORMAT.TEXT, label: VERSION_FORMAT_LABEL[VERSION_FORMAT.TEXT] },
  { value: VERSION_FORMAT.IMAGE, label: VERSION_FORMAT_LABEL[VERSION_FORMAT.IMAGE] },
] as const

/** el-tag 的 type 映射（可选，视视觉需要调整） */
export const VERSION_FORMAT_TAG_TYPE: Record<
  VersionFormatValue,
  'primary' | 'success' | 'warning' | 'danger' | 'info'
> = {
  [VERSION_FORMAT.TEXT]: 'primary',
  [VERSION_FORMAT.IMAGE]: 'success',
}

/** 类型守卫 */
export function isVersionFormat(v: unknown): v is VersionFormatValue {
  return v === VERSION_FORMAT.TEXT || v === VERSION_FORMAT.IMAGE
}

/** 安全取文案（插槽 row 是 any 时用它兜底） */
export function versionFormatLabel(v?: number): string {
  return v != null && isVersionFormat(v) ? VERSION_FORMAT_LABEL[v] : '未知'
}

/* ============================================================
 * 语言：language
 * ============================================================ */

export const VERSION_LANGUAGE = {
  ZH: 1,
  JA: 2,
} as const

export type VersionLanguageValue = (typeof VERSION_LANGUAGE)[keyof typeof VERSION_LANGUAGE]

export const VERSION_LANGUAGE_LABEL: Record<VersionLanguageValue, string> = {
  [VERSION_LANGUAGE.ZH]: '中文',
  [VERSION_LANGUAGE.JA]: '日文',
}

/** 下拉选项，给 el-select 直接用 */
export const VERSION_LANGUAGE_OPTIONS = [
  { value: VERSION_LANGUAGE.ZH, label: VERSION_LANGUAGE_LABEL[VERSION_LANGUAGE.ZH] },
  { value: VERSION_LANGUAGE.JA, label: VERSION_LANGUAGE_LABEL[VERSION_LANGUAGE.JA] },
] as const

/** el-tag 的 type 映射（可选，视视觉需要调整） */
export const VERSION_LANGUAGE_TAG_TYPE: Record<
  VersionLanguageValue,
  'primary' | 'success' | 'warning' | 'danger' | 'info'
> = {
  [VERSION_LANGUAGE.ZH]: 'primary',
  [VERSION_LANGUAGE.JA]: 'warning',
}

/** 类型守卫 */
export function isVersionLanguage(v: unknown): v is VersionLanguageValue {
  return v === VERSION_LANGUAGE.ZH || v === VERSION_LANGUAGE.JA
}

/** 安全取文案 */
export function versionLanguageLabel(v?: number): string {
  return v != null && isVersionLanguage(v) ? VERSION_LANGUAGE_LABEL[v] : '未知'
}

/* ============================================================
 * 范围：scope
 * ============================================================ */

export const VERSION_SCOPE = {
  ALL: 1,
  EXCERPT: 2,
} as const

export type VersionScopeValue = (typeof VERSION_SCOPE)[keyof typeof VERSION_SCOPE]

export const VERSION_SCOPE_LABEL: Record<VersionScopeValue, string> = {
  [VERSION_SCOPE.ALL]: '全部',
  [VERSION_SCOPE.EXCERPT]: '节选',
}

/** 下拉选项，给 el-select 直接用 */
export const VERSION_SCOPE_OPTIONS = [
  { value: VERSION_SCOPE.ALL, label: VERSION_SCOPE_LABEL[VERSION_SCOPE.ALL] },
  { value: VERSION_SCOPE.EXCERPT, label: VERSION_SCOPE_LABEL[VERSION_SCOPE.EXCERPT] },
] as const

/** el-tag 的 type 映射（可选，视视觉需要调整） */
export const VERSION_SCOPE_TAG_TYPE: Record<
  VersionScopeValue,
  'primary' | 'success' | 'warning' | 'danger' | 'info'
> = {
  [VERSION_SCOPE.ALL]: 'success',
  [VERSION_SCOPE.EXCERPT]: 'info',
}

/** 类型守卫 */
export function isVersionScope(v: unknown): v is VersionScopeValue {
  return v === VERSION_SCOPE.ALL || v === VERSION_SCOPE.EXCERPT
}

/** 安全取文案 */
export function versionScopeLabel(v?: number): string {
  return v != null && isVersionScope(v) ? VERSION_SCOPE_LABEL[v] : '未知'
}

/* ============================================================
 * 组合工具
 * ============================================================ */

/**
 * 生成完整版本文案：语言 · 形式 · 范围
 * 缺失的枚举会跳过，全都缺失时返回 fallback。
 *
 * @example
 * buildVersionLabel({ language: 1, format: 1, scope: 1 })
 * // => '中文 · 文字 · 全部'
 */
export function buildVersionLabel(input: {
  language?: number
  format?: number
  scope?: number
  fallback?: string
}): string {
  const parts = [
    input.language != null ? versionLanguageLabel(input.language) : '',
    input.format != null ? versionFormatLabel(input.format) : '',
    input.scope != null ? versionScopeLabel(input.scope) : '',
  ].filter((s) => s && s !== '未知')

  return parts.join(' · ') || input.fallback || '未知版本'
}
