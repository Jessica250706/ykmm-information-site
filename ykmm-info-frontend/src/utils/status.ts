import { STATUS, STATUS_TAG_TYPE, type StatusValue } from '@/constants'

/**
 * 判断值是否为合法的状态枚举
 *
 * @param v 待判断值
 */
export function isStatus(v: unknown): v is StatusValue {
  return v === STATUS.PUBLISHED || v === STATUS.PENDING || v === STATUS.REJECTED
}

/**
 * 根据状态值生成 el-tag 的 type
 *
 * @param status 状态值：1已发布 2待审核 3已拒绝
 * @returns el-tag 的 type，无效值返回 'info'
 */
export function getStatusTagType(status?: number | null) {
  if (status == null) return 'info'
  const key = status as StatusValue
  return STATUS_TAG_TYPE[key] ?? 'info'
}
