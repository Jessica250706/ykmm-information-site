import { type MenuTypeValue } from '@/constants/index'
import type { MenuDTO, MenuVO } from '@/types/menu'
import request from '@/utils/http'

/**
 * @description: 菜单树
 */
export const getMenuTreeAPI = (menuType?: MenuTypeValue) => {
  return request.get<MenuVO[]>('/admin/menus', { params: { menuType } })
}

/**
 * @description: 新增菜单
 */
export const createMenuAPI = (data: MenuDTO) => {
  return request.post('/admin/menus', data)
}

/**
 * @description: 编辑菜单
 */
export const updateMenuAPI = (id: number, data: MenuDTO) => {
  return request.put(`/admin/menus/${id}`, data)
}

/**
 * @description: 删除菜单
 */
export const deleteMenuAPI = (id: number) => {
  return request.delete(`/admin/menus/${id}`)
}
