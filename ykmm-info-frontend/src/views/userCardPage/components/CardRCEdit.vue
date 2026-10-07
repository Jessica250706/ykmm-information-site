<template>
  <CardBrowse
    :card-id="cardId"
    :episodes="episodes"
    :source-label="'卡面RC'"
    :source-type="3"
    @back="handleBack"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listCardRcByCardAPI } from '@/api/cardRc'
import CardBrowse from '@/views/userBrowse/CardBrowse.vue'

const route = useRoute()
const router = useRouter()

/** 卡面ID：从 query 取，返回时用 */
const cardId = computed(() => Number(route.query.cardId ?? 0))

const episodes = ref<{ id?: number; episodeNo?: number; title?: string }[]>([])

async function loadEpisodes() {
  if (!cardId.value) return
  const res = await listCardRcByCardAPI(cardId.value)
  episodes.value = res.data ?? []
}

function handleBack() {
  router.push({
    name: 'UserCardDetail',
    params: { id: String(cardId.value) },
  })
}

onMounted(() => {
  void loadEpisodes()
})
</script>
