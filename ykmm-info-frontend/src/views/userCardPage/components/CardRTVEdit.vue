<template>
  <CardBrowse
    :card-id="cardId"
    :episodes="episodes"
    :source-label="'卡面RTV'"
    :source-type="2"
    @back="handleBack"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listCardRtvByCardAPI } from '@/api/cardRtv'
import CardBrowse from '@/views/userBrowse/CardBrowse.vue'

const route = useRoute()
const router = useRouter()

const cardId = computed(() => Number(route.query.cardId ?? 0))

const episodes = ref<{ id?: number; episodeNo?: number; title?: string }[]>([])

async function loadEpisodes() {
  if (!cardId.value) return
  const res = await listCardRtvByCardAPI(cardId.value)
  episodes.value = res.data ?? []
}

function handleBack() {
  router.push({
    name: 'AdminCardRtvManage',
    query: { cardId: String(cardId.value) },
  })
}

onMounted(() => {
  void loadEpisodes()
})
</script>
