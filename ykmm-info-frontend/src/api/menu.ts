import { type MenuTypeValue } from '@/constants'
import type { MenuDTO, MenuVO } from '@/types/menu'
import request from '@/utils/http'

// ---------- 游客 ----------

/**
 * @description: 菜单树
 */
export const getMenuTreeAPI = (menuType?: MenuTypeValue) => {
  return request.get<MenuVO[]>('/pub/menu', { params: { menuType } })
}

// ---------- 管理员 ----------

/**
 * @description: 新增菜单
 */
export const createMenuAPI = (data: MenuDTO) => {
  return request.post('/admin/menu', data)
}

/**
 * @description: 编辑菜单
 */
export const updateMenuAPI = (id: number, data: MenuDTO) => {
  return request.put(`/admin/menu/${id}`, data)
}

/**
 * @description: 删除菜单
 */
export const deleteMenuAPI = (id: number) => {
  return request.delete(`/admin/menu/${id}`)
}
