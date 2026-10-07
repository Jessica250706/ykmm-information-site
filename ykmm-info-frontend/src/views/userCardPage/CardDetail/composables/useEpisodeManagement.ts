import { computed, ref, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCardRcAPI, deleteCardRcAPI, listCardRcByCardAPI } from '@/api/cardRc'
import { createCardRtvAPI, deleteCardRtvAPI, listCardRtvByCardAPI } from '@/api/cardRtv'
import { CARD_ATTACHED_STORY_TYPE } from '@/constants/card'
import type { CardRcVO } from '@/types/cardRc'
import type { CardRtvVO } from '@/types/cardRtv'
import type { EpisodeFormData } from '../components/AddEpisodeDialog.vue'

export function useEpisodeManagement(cardId: Ref<number>, attachedType: Ref<number>) {
  const rcList = ref<CardRcVO[]>([])
  const rtvList = ref<CardRtvVO[]>([])

  /** 当前 attachedType 对应的话数列表，用于 dialog 里计算默认话数 */
  const currentEpisodes = computed(() =>
    attachedType.value === CARD_ATTACHED_STORY_TYPE.RC ? rcList.value : rtvList.value,
  )

  async function load() {
    if (!cardId.value || !attachedType.value) return
    try {
      if (attachedType.value === CARD_ATTACHED_STORY_TYPE.RC) {
        const res = await listCardRcByCardAPI(cardId.value)
        rcList.value = res.data ?? []
      } else if (attachedType.value === CARD_ATTACHED_STORY_TYPE.RTV) {
        const res = await listCardRtvByCardAPI(cardId.value)
        rtvList.value = res.data ?? []
      }
    } catch {
      // 忽略
    }
  }

  async function create(mode: 'rc' | 'rtv', data: EpisodeFormData) {
    if (mode === 'rc') {
      await createCardRcAPI({
        cardId: cardId.value,
        episodeNo: data.episodeNo,
        title: data.title,
        roleId: data.roleId,
      })
      ElMessage.success('新增 RC 成功')
    } else {
      await createCardRtvAPI({
        cardId: cardId.value,
        episodeNo: data.episodeNo,
        title: data.title,
      })
      ElMessage.success('新增 RTV 成功')
    }
    await load()
  }

  async function remove(mode: 'rc' | 'rtv', ep: CardRcVO | CardRtvVO) {
    try {
      await ElMessageBox.confirm(
        `确定要删除第 ${ep.episodeNo} 话「${ep.title ?? ''}」吗？删除后不可恢复。`,
        '删除确认',
        { type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger' },
      )
    } catch {
      return
    }

    try {
      if (mode === 'rc') await deleteCardRcAPI(ep.id!)
      else await deleteCardRtvAPI(ep.id!)
      ElMessage.success('删除成功')
      await load()
    } catch {
      // 忽略
    }
  }

  return {
    rcList,
    rtvList,
    currentEpisodes,
    load,
    create,
    remove,
  }
}
