<template>
  <section v-loading="loading" class="card-detail flex h-full min-h-0 flex-col">
    <CardDetailHeader
      :attached-type="attachedType"
      :card="card"
      @add-episode="handleAddEpisode"
      @back="handleBack"
      @goto="handleGoto"
    />

    <div v-if="card" class="grid min-h-0 flex-1 grid-cols-1 gap-6 lg:grid-cols-[1fr_320px]">
      <!-- 左侧：图片 -->
      <div class="min-h-0">
        <ImageView
          v-if="imageUrls.length"
          :image-list="imageUrls"
          :magnifier="false"
          :thumb-size="72"
          fit="contain"
          width="100%"
        />
        <el-empty v-else description="暂无图片" />
      </div>

      <!-- 右侧：信息 -->
      <aside class="flex min-h-0 flex-col gap-4 overflow-y-auto app-scrollbar pr-1">
        <CardBasicInfo :card="card" />
        <CardPersons :persons="card.persons ?? []" />
        <CardSkillDesc :text="card.skillDesc" />
        <CardImageLegend :items="imageLegend" />

        <!-- RC / RTV / Rabitter 列表 -->
        <CardEpisodeList
          v-if="isRc"
          :list="rcList"
          mode="rc"
          @delete="(ep) => handleRemove(RC_MODE, ep)"
          @edit="(ep) => handleEdit(RC_MODE, ep)"
          @view="(id) => handleView(RC_MODE, id)"
        />
        <CardEpisodeList
          v-if="isRtv"
          :list="rtvList"
          mode="rtv"
          @delete="(ep) => handleRemove(RTV_MODE, ep)"
          @edit="(ep) => handleEdit(RTV_MODE, ep)"
          @view="(id) => handleView(RTV_MODE, id)"
        />
        <CardEpisodeList
          v-if="isRabitter"
          :list="rabitterList"
          mode="rabitter"
          @delete="(ep) => handleRemove(RABITTER_MODE, ep)"
          @edit="(ep) => handleEdit(RABITTER_MODE, ep)"
          @view="(id) => handleView(RABITTER_MODE, id)"
        />
      </aside>
    </div>

    <el-empty v-else-if="!loading" description="卡面不存在" />

    <AddEpisodeDialog
      ref="episodeDialogRef"
      :existing-episodes="currentEpisodes"
      :saving="episodeSaving"
      @submit="handleSubmitEpisode"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ImageView from '@/components/ImageView/Index.vue'
import {
  CARD_IMAGE_TYPE_LABEL,
  SOURCE_TYPE,
  SOURCE_TYPE_LABEL,
  SOURCE_TYPE_SMALL_LABEL,
  type SourceTypeLabelValue,
  type SourceTypeSmallLabelValue,
  UserRouteName,
} from '@/constants'
import type { CardRcVO } from '@/types/cardRc'
import type { CardRtvVO } from '@/types/cardRtv'
import AddEpisodeDialog, { type EpisodeFormData } from './components/AddEpisodeDialog.vue'
import CardBasicInfo from './components/CardBasicInfo.vue'
import CardDetailHeader from './components/CardDetailHeader.vue'
import CardEpisodeList from './components/CardEpisodeList.vue'
import CardImageLegend from './components/CardImageLegend.vue'
import CardPersons from './components/CardPersons.vue'
import CardSkillDesc from './components/CardSkillDesc.vue'
import { useCardDetail } from './composables/useCardDetail'
import { useEpisodeManagement } from './composables/useEpisodeManagement'

const route = useRoute()
const router = useRouter()

const cardId = computed(() => Number(route.params.id ?? 0))

const RC_MODE = SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC]
const RTV_MODE = SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RTV]
const RABITTER_MODE = SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RABITTER]

/* -------- 卡面 -------- */
const { loading, card, attachedType, imageUrls, isRc, isRtv, isRabitter, load } = useCardDetail()

/* -------- 话数管理 -------- */
const {
  rcList,
  rtvList,
  rabitterList,
  load: loadEpisodes,
  create: createEpisode,
  update: updateEpisode,
  remove: removeEpisode,
  currentEpisodes,
} = useEpisodeManagement(cardId, attachedType)

/* -------- 图片图例 -------- */
const imageLegend = computed(() =>
  (card.value?.images ?? [])
    .filter((i) => i.url)
    .map((i) => ({
      type: i.imageType!,
      label:
        i.imageTypeLabel ?? CARD_IMAGE_TYPE_LABEL[i.imageType as 1 | 2 | 3 | 4 | 5 | 6] ?? '未知',
    })),
)

/* -------- 弹窗 -------- */
const episodeDialogRef = ref<InstanceType<typeof AddEpisodeDialog> | null>(null)
const episodeSaving = ref(false)

function handleAddEpisode(mode: SourceTypeSmallLabelValue) {
  episodeDialogRef.value?.open(mode)
}

/** 提交：根据 id 是否为空区分新增 / 编辑 */
async function handleSubmitEpisode(payload: {
  mode: SourceTypeSmallLabelValue
  id: number | null
  data: EpisodeFormData
}) {
  episodeSaving.value = true
  try {
    if (payload.id == null) {
      await createEpisode(payload.mode, payload.data)
    } else {
      await updateEpisode(payload.mode, payload.id, payload.data)
    }
    episodeDialogRef.value?.close()
  } finally {
    episodeSaving.value = false
  }
}

/* -------- 查看 / 删除 -------- */
function chooseMode(mode: SourceTypeLabelValue) {
  if (mode === SOURCE_TYPE_LABEL[SOURCE_TYPE.RC]) {
    return UserRouteName.CARD_RC_BROWSE
  } else if (mode === SOURCE_TYPE_LABEL[SOURCE_TYPE.RTV]) {
    return UserRouteName.CARD_RTV_BROWSE
  } else if (mode === SOURCE_TYPE_LABEL[SOURCE_TYPE.RABITTER]) {
    return UserRouteName.CARD_RABITTER_BROWSE
  }
}

function handleView(mode: SourceTypeLabelValue, id: number) {
  router.push({
    name: chooseMode(mode),
    query: { cardId: String(cardId.value), episodeId: String(id) },
  })
}

/** 打开编辑弹窗 */
function handleEdit(mode: SourceTypeSmallLabelValue, ep: CardRcVO | CardRtvVO) {
  episodeDialogRef.value?.open(mode, {
    id: ep.id,
    episodeNo: ep.episodeNo,
    title: ep.title,
    // RTV 没有 roleId，RC 有；类型断言或 optional 都行
    roleId: (ep as CardRcVO).roleId,
  })
}

function handleRemove(mode: SourceTypeSmallLabelValue, ep: CardRcVO | CardRtvVO) {
  void removeEpisode(mode, ep)
}

/* -------- 返回 / 跳转 -------- */
function handleBack() {
  router.back()
}

function handleGoto() {
  router.push({
    name: UserRouteName.CARD_RC_BROWSE,
    query: { sourceType: SOURCE_TYPE.RC, cardId: String(cardId.value) },
  })
}

/* -------- 初始化 -------- */
onMounted(async () => {
  await load(cardId.value)
  await loadEpisodes()
})
</script>
