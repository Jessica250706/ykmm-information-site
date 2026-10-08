import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { deleteStoryAPI } from '@/api/story'
import { deleteStoryCategoryAPI } from '@/api/storyCategory'
import { AdminRouteName, UserRouteName } from '@/constants'
import type { StoryVO } from '@/types/story'
import type { StoryCategoryVO } from '@/types/storyCategory'

interface Options {
  /** 当前详情分类 id（用函数取值，保持响应式） */
  detailId: () => number | null
  /** 当前详情分类对象 */
  detail: () => StoryCategoryVO | null
  /** 删除分类后：刷新详情 + 刷新表格 */
  onCategoryDeleted: () => Promise<void> | void
  /** 删除剧情后：只刷新表格 */
  onStoryDeleted: () => Promise<void> | void
}

export function useDetailActions(opts: Options) {
  const router = useRouter()

  /* -------- 分类导航 -------- */
  function goCreateCategory(parent?: StoryCategoryVO) {
    router.push({
      name: AdminRouteName.STORY_CATEGORY_CREATE,
      query: {
        parentId: String(parent?.id ?? opts.detailId()),
        categoryType: String(parent?.categoryType ?? opts.detail()?.categoryType ?? ''),
        from: 'detail',
        detailId: String(opts.detailId()),
      },
    })
  }

  function goCategoryDetail(row: StoryCategoryVO) {
    router.push({
      name: AdminRouteName.STORY_CATEGORY_DETAIL,
      params: { id: String(row.id) },
    })
  }

  function goEditCategory(row: StoryCategoryVO) {
    router.push({
      name: AdminRouteName.STORY_CATEGORY_EDIT,
      params: { id: String(row.id) },
      query: { from: 'detail', detailId: String(opts.detailId()) },
    })
  }

  function goEditCurrentCategory() {
    const current = opts.detail()
    if (!current?.id) return
    router.push({
      name: AdminRouteName.STORY_CATEGORY_EDIT,
      params: { id: String(current.id) },
      query: { from: 'detail', detailId: String(current.id) },
    })
  }

  async function removeCategory(row: StoryCategoryVO) {
    try {
      await ElMessageBox.confirm(`确定要删除分类「${row.name}」吗？`, '删除确认', {
        type: 'warning',
        confirmButtonText: '删除',
        confirmButtonClass: 'el-button--danger',
      })
    } catch {
      return
    }
    await deleteStoryCategoryAPI(row.id!)
    ElMessage.success('删除成功')
    await opts.onCategoryDeleted()
  }

  /* -------- 剧情导航 -------- */
  function goCreateStory(row?: StoryCategoryVO) {
    router.push({
      name: AdminRouteName.STORY_CREATE,
      query: {
        categoryId: String(row?.id ?? opts.detailId()),
        categoryType: String(row?.categoryType ?? opts.detail()?.categoryType ?? ''),
        from: 'storyCategoryDetail',
        detailId: String(opts.detailId()),
      },
    })
  }

  function goEditStory(row: StoryVO) {
    router.push({
      name: AdminRouteName.STORY_EDIT,
      params: { id: String(row.id) },
      query: { from: 'storyCategoryDetail', detailId: String(opts.detailId()) },
    })
  }

  /** 新标签页打开用户端剧情浏览 */
  function openStoryInNewTab(row: StoryVO) {
    const { href } = router.resolve({
      name: UserRouteName.STORY_BROWSE_DETAIL,
      params: { type: String(row.categoryType ?? 1), kind: 'story', id: String(row.id) },
    })
    window.open(href, '_blank', 'noopener,noreferrer')
  }

  async function removeStory(row: StoryVO) {
    try {
      await ElMessageBox.confirm(`确定要删除剧情「${row.title ?? row.id}」吗？`, '删除确认', {
        type: 'warning',
        confirmButtonText: '删除',
        confirmButtonClass: 'el-button--danger',
      })
    } catch {
      return
    }
    await deleteStoryAPI(row.id!)
    ElMessage.success('删除成功')
    await opts.onStoryDeleted()
  }

  /* -------- 返回列表 -------- */
  function goBack() {
    router.push({ name: AdminRouteName.STORY_CATEGORY_MANAGE })
  }

  return {
    goCreateCategory,
    goCategoryDetail,
    goEditCategory,
    goEditCurrentCategory,
    removeCategory,
    goCreateStory,
    goEditStory,
    openStoryInNewTab,
    removeStory,
    goBack,
  }
}
