import type { PageRequest } from './common'
import type { AgencyVO } from './agency'
import type { PersonIdolGroupVO } from './idolGroup'

/**
 * 人物分页查询条件
 */
export interface PersonPageQueryDTO extends PageRequest {
  /**
   * 所属公司ID（仅经纪人有效）
   */
  agencyId?: number
  /**
   * 所属团体ID（仅偶像有效）
   */
  groupId?: number
  /**
   * 关键字：中文名 / 日文名 / 罗马音
   */
  keyword?: string
  /**
   * 1偶像 2经纪人
   */
  personType?: number
  [property: string]: any
}

/**
 * 人物列表/详情返回
 *
 * PersonVO
 */
export interface PersonVO {
  /**
   * 年龄
   */
  age?: number
  /**
   * 所属公司（经纪人）
   */
  agencies?: AgencyVO[]
  /**
   * 头像
   */
  avatar?: string
  /**
   * 生日，如 12-24
   */
  birthday?: string
  /**
   * 1-A 2-B 3-O 4-AB 5-其他
   */
  bloodType?: number
  /**
   * 文本：1-A 2-B 3-O 4-AB 5-其他
   */
  bloodTypeLabel?: string
  /**
   * 不擅长的事物
   */
  dislikes?: string
  /**
   * 所属团体（偶像）
   */
  groups?: PersonIdolGroupVO[]
  /**
   * 身高
   */
  height?: number
  /**
   * 主键
   */
  id?: number
  /**
   * 展示图片列表
   */
  images?: string[]
  /**
   * 角色简介
   */
  intro?: string
  /**
   * 喜欢的事物
   */
  likes?: string
  /**
   * 中文名
   */
  nameCn?: string
  /**
   * 日文名
   */
  nameJp?: string
  /**
   * 罗马音
   */
  nameRomaji?: string
  /**
   * 1偶像 2经纪人
   */
  personType?: number
  /**
   * 文本：1偶像 2经纪人
   */
  personTypeLabel?: string
  /**
   * 鞋码
   */
  shoeSize?: number
  /**
   * 代表符号
   */
  symbol?: string
  /**
   * 应援色
   */
  themeColor?: string
  /**
   * 体重
   */
  weight?: number
  /**
   * 声优
   */
  cv?: string
  [property: string]: any
}

/**
 * PersonDTO
 */
export interface PersonDTO {
  /**
   * 年龄
   */
  age?: number
  /**
   * 所属公司ID列表（经纪人）
   */
  agencyIds?: number[]
  /**
   * 头像
   */
  avatar?: string
  /**
   * 生日，如 12-24
   */
  birthday?: string
  /**
   * 1-A 2-B 3-O 4-AB 5-其他
   */
  bloodType?: number
  /**
   * 不擅长的事物
   */
  dislikes?: string
  /**
   * 所属团体ID列表（偶像）
   */
  groupIds?: number[]
  /**
   * 身高
   */
  height?: number
  /**
   * 展示图片列表
   */
  images?: string[]
  /**
   * 角色简介
   */
  intro?: string
  /**
   * 喜欢的事物
   */
  likes?: string
  /**
   * 中文名
   */
  nameCn?: string
  /**
   * 日文名
   */
  nameJp?: string
  /**
   * 罗马音
   */
  nameRomaji?: string
  /**
   * 1偶像 2经纪人
   */
  personType?: number
  /**
   * 鞋码
   */
  shoeSize?: number
  /**
   * 代表符号
   */
  symbol?: string
  /**
   * 应援色
   */
  themeColor?: string
  /**
   * 体重
   */
  weight?: number
  /**
   * 声优
   */
  cv?: string
  [property: string]: any
}
