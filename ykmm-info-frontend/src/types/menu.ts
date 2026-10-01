import type { MenuTypeValue } from '@/constants/index'

/**
 * 菜单表
 *
 * SysMenu
 */
export interface SysMenu {
  /**
   * 子菜单，构建树时使用
   */
  children?: SysMenu[]
  /**
   * 前端组件
   */
  component?: string
  /**
   * 创建时间
   */
  createdAt?: string
  /**
   * 图标
   */
  icon?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 1管理端 2用户端
   */
  menuType?: MenuTypeValue
  /**
   * 菜单名称
   */
  name?: string
  /**
   * 父菜单ID
   */
  parentId?: number
  /**
   * 路由路径
   */
  path?: string
  /**
   * 权限标识
   */
  permission?: string
  /**
   * 排序
   */
  sort?: number
  /**
   * 更新时间
   */
  updatedAt?: string
  /**
   * 是否显示
   */
  visible?: number
  [property: string]: any
}

/**
 * MenuDTO
 */
export interface MenuDTO {
  component?: string
  icon?: string
  /**
   * 1管理端 2用户端
   */
  menuType?: MenuTypeValue
  name?: string
  parentId?: number
  path?: string
  permission?: string
  sort?: number
  visible?: number
  [property: string]: any
}

/**
 * 菜单表
 *
 * MenuVO
 */
export interface MenuVO {
  /**
   * 子菜单，构建树时使用
   */
  children?: MenuVO[]
  /**
   * 前端组件
   */
  component?: string
  /**
   * 图标
   */
  icon?: string
  /**
   * 主键
   */
  id?: number
  /**
   * 1管理端 2用户端
   */
  menuType?: MenuTypeValue
  /**
   * 菜单名称
   */
  name?: string
  /**
   * 父菜单ID
   */
  parentId?: number
  /**
   * 路由路径
   */
  path?: string
  /**
   * 权限标识
   */
  permission?: string
  /**
   * 排序
   */
  sort?: number
  /**
   * 是否显示
   */
  visible?: number
  [property: string]: any
}
