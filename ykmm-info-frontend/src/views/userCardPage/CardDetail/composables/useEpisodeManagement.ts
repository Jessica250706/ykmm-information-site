import { computed, ref, type Ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createCardRabitterAPI,
  deleteCardRabitterAPI,
  listCardRabitterByCardAPI,
  updateCardRabitterAPI,
} from '@/api/cardRabitter'
import {
  createCardRcAPI,
  deleteCardRcAPI,
  listCardRcByCardAPI,
  updateCardRcAPI,
} from '@/api/cardRc'
import {
  createCardRtvAPI,
  deleteCardRtvAPI,
  listCardRtvByCardAPI,
  updateCardRtvAPI,
} from '@/api/cardRtv'
import { SOURCE_TYPE, SOURCE_TYPE_SMALL_LABEL, type SourceTypeSmallLabelValue } from '@/constants'
import type { CardRabitterVO } from '@/types/cardRabitter'
import type { CardRcVO } from '@/types/cardRc'
import type { CardRtvVO } from '@/types/cardRtv'
import type { EpisodeFormData } from '../components/AddEpisodeDialog.vue'

export function useEpisodeManagement(cardId: Ref<number>, attachedType: Ref<number>) {
  const rcList = ref<CardRcVO[]>([])
  const rtvList = ref<CardRtvVO[]>([])
  const rabitterList = ref<CardRabitterVO[]>([])

  const isList = computed(
    () => rcList.value.length > 0 || rtvList.value.length > 0 || rabitterList.value.length > 0,
  )

  /** 当前 attachedType 对应的话数列表，用于 dialog 里计算默认话数 */
  const currentEpisodes = computed(() =>
    attachedType.value === SOURCE_TYPE.RC ? rcList.value : rtvList.value,
  )

  async function load() {
    if (!cardId.value || !attachedType.value) return
    try {
      if (attachedType.value === SOURCE_TYPE.RC) {
        const res = await listCardRcByCardAPI(cardId.value)
        rcList.value = res.data ?? []
      } else if (attachedType.value === SOURCE_TYPE.RTV) {
        const res = await listCardRtvByCardAPI(cardId.value)
        rtvList.value = res.data ?? []
      } else if (attachedType.value === SOURCE_TYPE.RABITTER) {
        const res = await listCardRabitterByCardAPI(cardId.value)
        rabitterList.value = res.data ?? []
      }
    } catch {
      // 忽略
    }
  }

  /** 新增 */
  async function create(mode: SourceTypeSmallLabelValue, data: EpisodeFormData) {
    if (mode === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC]) {
      await createCardRcAPI({
        cardId: cardId.value,
        episodeNo: data.episodeNo,
        title: data.title,
        roleId: data.roleId,
      })
      ElMessage.success('新增 RC 成功')
    } else if (mode === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RTV]) {
      await createCardRtvAPI({
        cardId: cardId.value,
        episodeNo: data.episodeNo,
        title: data.title,
      })
      ElMessage.success('新增 RTV 成功')
    } else if (mode === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RABITTER]) {
      await createCardRabitterAPI({
        cardId: cardId.value,
        episodeNo: data.episodeNo,
        title: data.title,
      })
      ElMessage.success('新增 Rabitter 成功')
    }
    await load()
  }

  /** 更新 */
  async function update(mode: SourceTypeSmallLabelValue, id: number, data: EpisodeFormData) {
    if (mode === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC]) {
      await updateCardRcAPI(id, {
        cardId: cardId.value,
        episodeNo: data.episodeNo,
        title: data.title,
        roleId: data.roleId,
      })
      ElMessage.success('更新 RC 成功')
    } else if (mode === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RTV]) {
      await updateCardRtvAPI(id, {
        cardId: cardId.value,
        episodeNo: data.episodeNo,
        title: data.title,
      })
      ElMessage.success('更新 RTV 成功')
    } else if (mode === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RABITTER]) {
      await updateCardRabitterAPI(id, {
        cardId: cardId.value,
        episodeNo: data.episodeNo,
        title: data.title,
      })
      ElMessage.success('更新 Rabitter 成功')
    }
    await load()
  }

  /** 删除 */
  async function remove(
    mode: SourceTypeSmallLabelValue,
    ep: CardRcVO | CardRtvVO | CardRabitterVO,
  ) {
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
      if (mode === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC]) await deleteCardRcAPI(ep.id!)
      else if (mode === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RTV]) await deleteCardRtvAPI(ep.id!)
      else if (mode === SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RABITTER])
        await deleteCardRabitterAPI(ep.id!)
      ElMessage.success('删除成功')
      await load()
    } catch {
      // 忽略
    }
  }

  return {
    isList,
    rcList,
    rtvList,
    rabitterList,
    currentEpisodes,
    load,
    create,
    update,
    remove,
  }
}
