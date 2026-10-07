<template>
  <section v-loading="loading" class="card-detail flex h-full min-h-0 flex-col">
    <div class="mb-5 flex shrink-0 items-center gap-3">
      <el-button @click="handleBack">← 返回</el-button>
      <h1 class="text-xl font-semibold">{{ card?.name || '卡面详情' }}</h1>
      <el-tag
        v-if="card?.maxRarity"
        :type="card.maxRarity === CARD_MAX_RARITY.UR ? 'danger' : 'warning'"
        effect="plain"
      >
        {{ card.maxRarityLabel ?? cardMaxRarityLabel(card.maxRarity) }}
      </el-tag>
    </div>

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
      <aside class="flex min-h-0 flex-col gap-4">
        <!-- 基本信息 -->
        <div>
          <h2 class="mb-3 text-sm font-medium text-slate-500">基本信息</h2>
          <dl class="space-y-2 text-sm">
            <div class="flex justify-between gap-3">
              <dt class="shrink-0 text-slate-400">名称</dt>
              <dd class="truncate text-right">
                {{ card.name || '-' }}[{{ card.seriesName || '-' }}]
              </dd>
            </div>
            <div class="flex justify-between gap-3">
              <dt class="shrink-0 text-slate-400">系列</dt>
              <dd class="truncate text-right">{{ card.seriesName || '-' }}</dd>
            </div>
            <div class="flex justify-between gap-3">
              <dt class="shrink-0 text-slate-400">属性</dt>
              <dd class="text-right">
                <el-tag
                  v-if="card.attribute"
                  :style="getAttributeTagStyle(card.attribute)"
                  effect="plain"
                  size="small"
                >
                  {{ card.attributeLabel ?? cardAttributeLabel(card.attribute) }}
                </el-tag>
                <span v-else>-</span>
              </dd>
            </div>
            <div class="flex justify-between gap-3">
              <dt class="shrink-0 text-slate-400">首次入池</dt>
              <dd class="text-right">{{ card.firstPoolTime || '未知' }}</dd>
            </div>
            <div class="flex justify-between gap-3">
              <dt class="shrink-0 text-slate-400">附属剧情</dt>
              <dd class="text-right">
                <el-tag
                  v-if="card.attachedStoryType"
                  :type="attachedStoryTypeTag(card.attachedStoryType)"
                  effect="plain"
                  size="small"
                >
                  {{
                    card.attachedStoryTypeLabel ??
                    cardAttachedStoryTypeLabel(card.attachedStoryType)
                  }}
                </el-tag>
                <span v-else>无</span>
              </dd>
            </div>
            <div class="flex justify-between gap-3">
              <dt class="shrink-0 text-slate-400">服装</dt>
              <dd class="text-right">
                {{ card.costumeTypeLabel ?? cardCostumeTypeLabel(card.costumeType) }}
              </dd>
            </div>
          </dl>
        </div>

        <!-- 关联人物 -->
        <div v-if="card.persons?.length">
          <h2 class="mb-3 text-sm font-medium text-slate-500">关联人物</h2>
          <div class="flex flex-wrap gap-2">
            <el-tag
              v-for="p in card.persons"
              :key="p.personId"
              :style="personTagStyle(p.themeColor)"
              effect="plain"
            >
              <div class="flex items-center justify-center">
                <el-avatar :size="18" :src="p.avatar" class="mr-2 align-middle">
                  {{ p.nameCn?.charAt(0) || '?' }}
                </el-avatar>
                <span>{{ p.nameCn }}</span>
              </div>
            </el-tag>
          </div>
        </div>

        <!-- 魅力技能 -->
        <div v-if="card.skillDesc">
          <h2 class="mb-3 text-sm font-medium text-slate-500">魅力技能</h2>
          <p class="whitespace-pre-line text-sm leading-relaxed text-slate-700">
            {{ card.skillDesc }}
          </p>
        </div>

        <!-- 图片类型图例 -->
        <div v-if="imageLegend.length">
          <h2 class="mb-3 text-sm font-medium text-slate-500">图片类型</h2>
          <div class="flex flex-wrap gap-2">
            <el-tag v-for="l in imageLegend" :key="l.type" effect="plain" size="small" type="info">
              {{ l.label }}
            </el-tag>
          </div>
          <p class="mt-2 text-xs text-slate-400">左侧图片顺序与上方编号一致，点击缩略图切换</p>
        </div>
      </aside>
    </div>

    <el-empty v-else-if="!loading" description="卡面不存在" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserCardDetailAPI } from '@/api/card'
import ImageView from '@/components/ImageView/Index.vue'
import {
  CARD_IMAGE_TYPE_LABEL,
  CARD_MAX_RARITY,
  cardAttachedStoryTypeLabel,
  cardAttributeLabel,
  cardCostumeTypeLabel,
  cardMaxRarityLabel,
} from '@/constants/card'
import type { CardVO } from '@/types/card'
import { attachedStoryTypeTag, getAttributeTagStyle, personTagStyle } from '@/utils'

const route = useRoute()
const router = useRouter()

const cardId = computed(() => Number(route.params.id ?? 0))

const loading = ref(false)
const card = ref<CardVO | null>(null)

/** 图片 URL 列表（按 image_type 已排序，来自后端） */
const imageUrls = computed(() => (card.value?.images ?? []).map((i) => i.url!).filter((u) => !!u))

/** 图片类型图例，告诉用户第几张是什么形态 */
const imageLegend = computed(() => {
  const images = card.value?.images ?? []
  return images
    .filter((i) => i.url)
    .map((i) => ({
      type: i.imageType!,
      label:
        i.imageTypeLabel ?? CARD_IMAGE_TYPE_LABEL[i.imageType as 1 | 2 | 3 | 4 | 5 | 6] ?? '未知',
    }))
})

async function loadDetail() {
  if (!cardId.value) return
  loading.value = true
  try {
    const res = await getUserCardDetailAPI(cardId.value)
    card.value = res.data ?? null
  } catch {
    card.value = null
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.back()
}

onMounted(() => {
  void loadDetail()
})
</script>
