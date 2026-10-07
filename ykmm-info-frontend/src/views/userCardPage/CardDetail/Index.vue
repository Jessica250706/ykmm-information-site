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

        <!-- RC / RTV 列表 -->
        <CardEpisodeList
          v-if="isRc"
          :list="rcList"
          mode="rc"
          @delete="(ep) => handleRemove('rc', ep)"
          @view="(id) => handleView('rc', id)"
        />
        <CardEpisodeList
          v-if="isRtv"
          :list="rtvList"
          mode="rtv"
          @delete="(ep) => handleRemove('rtv', ep)"
          @view="(id) => handleView('rtv', id)"
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
import { CARD_IMAGE_TYPE_LABEL, SOURCE_TYPE } from '@/constants'
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

/* -------- 卡面 -------- */
const { loading, card, attachedType, imageUrls, isRc, isRtv, load } = useCardDetail()

/* -------- 话数管理 -------- */
const {
  rcList,
  rtvList,
  load: loadEpisodes,
  create: createEpisode,
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

function handleAddEpisode(mode: 'rc' | 'rtv') {
  episodeDialogRef.value?.open(mode)
}

async function handleSubmitEpisode(payload: { mode: 'rc' | 'rtv'; data: EpisodeFormData }) {
  episodeSaving.value = true
  try {
    await createEpisode(payload.mode, payload.data)
    episodeDialogRef.value?.close()
  } finally {
    episodeSaving.value = false
  }
}

/* -------- 查看 / 删除 -------- */
function handleView(mode: 'rc' | 'rtv', id: number) {
  router.push({
    name: mode === 'rc' ? 'AdminCardRcEdit' : 'AdminCardRtvEdit',
    query: { cardId: String(cardId.value), episodeId: String(id) },
  })
}

function handleRemove(mode: 'rc' | 'rtv', ep: any) {
  void removeEpisode(mode, ep)
}

/* -------- 返回 / 跳转 -------- */
function handleBack() {
  router.back()
}

function handleGoto() {
  router.push({
    name: 'UserCardRcEdit',
    query: { sourceType: SOURCE_TYPE.RC, cardId: String(cardId.value) },
  })
}

/* -------- 初始化 -------- */
onMounted(async () => {
  await load(cardId.value)
  await loadEpisodes()
})
</script>
