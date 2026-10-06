/**
 * 贡献者角色
 * 1内容作者 2代传管理员 3协作者
 */
export const CONTRIBUTOR_ROLE = {
  AUTHOR: 1,
  DELEGATE: 2,
  COLLABORATOR: 3,
} as const

export type ContributorRoleValue = (typeof CONTRIBUTOR_ROLE)[keyof typeof CONTRIBUTOR_ROLE]

export const CONTRIBUTOR_ROLE_LABEL: Record<ContributorRoleValue, string> = {
  [CONTRIBUTOR_ROLE.AUTHOR]: '内容作者',
  [CONTRIBUTOR_ROLE.DELEGATE]: '代传管理员',
  [CONTRIBUTOR_ROLE.COLLABORATOR]: '协作者',
}

/** 下拉选项 */
export const CONTRIBUTOR_ROLE_OPTIONS = [
  { value: CONTRIBUTOR_ROLE.AUTHOR, label: CONTRIBUTOR_ROLE_LABEL[CONTRIBUTOR_ROLE.AUTHOR] },
  { value: CONTRIBUTOR_ROLE.DELEGATE, label: CONTRIBUTOR_ROLE_LABEL[CONTRIBUTOR_ROLE.DELEGATE] },
  {
    value: CONTRIBUTOR_ROLE.COLLABORATOR,
    label: CONTRIBUTOR_ROLE_LABEL[CONTRIBUTOR_ROLE.COLLABORATOR],
  },
] as const

/** el-tag 的 type 映射 */
export const CONTRIBUTOR_ROLE_TAG_TYPE: Record<
  ContributorRoleValue,
  'primary' | 'success' | 'warning' | 'danger' | 'info'
> = {
  [CONTRIBUTOR_ROLE.AUTHOR]: 'primary',
  [CONTRIBUTOR_ROLE.DELEGATE]: 'warning',
  [CONTRIBUTOR_ROLE.COLLABORATOR]: 'info',
}

/** 类型守卫 */
export function isContributorRole(v: unknown): v is ContributorRoleValue {
  return (
    v === CONTRIBUTOR_ROLE.AUTHOR ||
    v === CONTRIBUTOR_ROLE.DELEGATE ||
    v === CONTRIBUTOR_ROLE.COLLABORATOR
  )
}

/** 安全取文案 */
export function contributorRoleLabel(v?: number | null): string {
  return v != null && isContributorRole(v) ? CONTRIBUTOR_ROLE_LABEL[v] : '未知'
}
