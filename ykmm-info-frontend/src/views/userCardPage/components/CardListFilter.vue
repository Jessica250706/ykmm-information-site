<template>
  <div class="mb-5 flex flex-col gap-3 shrink-0">
    <div class="flex items-center gap-3 col-4 shrink-0">
      <el-select
        :max-collapse-tags="2"
        :model-value="filter.personIds"
        class="w-64"
        placeholder="人物"
        clearable
        collapse-tags
        collapse-tags-tooltip
        filterable
        multiple
        @update:model-value="(v: number[]) => update('personIds', v ?? [])"
      >
        <el-option
          v-for="p in personStore.persons"
          :key="p.id"
          :label="p.nameCn || p.nameJp || `#${p.id}`"
          :value="p.id!"
        />
      </el-select>

      <el-select
        :model-value="filter.seriesId"
        class="w-48"
        placeholder="系列"
        clearable
        filterable
        @update:model-value="(v: number | undefined) => update('seriesId', v)"
      >
        <el-option v-for="s in seriesOptions" :key="s.id" :label="s.name" :value="s.id!" />
      </el-select>

      <el-select
        :model-value="filter.maxRarity"
        class="w-32"
        placeholder="最高等级"
        clearable
        @update:model-value="(v: number | undefined) => update('maxRarity', v)"
      >
        <el-option
          v-for="opt in CARD_MAX_RARITY_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>

      <div class="ml-auto">
        <el-button text @click="showMore = !showMore">
          {{ showMore ? '收起' : '更多' }}
          <el-icon
            :class="{ 'rotate-180': showMore }"
            class="ml-1 transition-transform duration-200"
          >
            <ArrowDown />
          </el-icon>
        </el-button>
      </div>
    </div>

    <el-collapse-transition>
      <div v-show="showMore" class="flex items-center gap-3 col-4 shrink-0">
        <el-select
          :model-value="filter.attribute"
          class="w-32"
          placeholder="属性"
          clearable
          @update:model-value="(v: number | undefined) => update('attribute', v)"
        >
          <el-option
            v-for="opt in CARD_ATTRIBUTE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>

        <el-select
          :model-value="filter.category"
          class="w-44"
          placeholder="类别"
          clearable
          filterable
          @update:model-value="(v: number | undefined) => update('category', v)"
        >
          <el-option
            v-for="c in categoryOptions"
            :key="c.id"
            :label="c.name ?? `#${c.id}`"
            :value="c.id!"
          />
        </el-select>

        <el-select
          :model-value="filter.attachedStoryType"
          class="w-36"
          placeholder="附属剧情"
          clearable
          @update:model-value="(v: number | undefined) => update('attachedStoryType', v)"
        >
          <el-option
            v-for="opt in SOURCE_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>

        <el-select
          :model-value="filter.costumeType"
          class="w-36"
          placeholder="服装类型"
          clearable
          @update:model-value="(v: number | undefined) => update('costumeType', v)"
        >
          <el-option
            v-for="opt in CARD_COSTUME_TYPE_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script setup lang="ts">
import { ArrowDown } from '@element-plus/icons-vue'
import {
  CARD_ATTRIBUTE_OPTIONS,
  CARD_COSTUME_TYPE_OPTIONS,
  CARD_MAX_RARITY_OPTIONS,
  SOURCE_TYPE_OPTIONS,
} from '@/constants'
import { usePersonStore } from '@/stores'
import type { CardCategoryVO } from '@/types/cardCategory'
import type { CardSeriesVO } from '@/types/cardSeries'
import type { CardFilter } from '../composables/useCardFilterSync'

defineProps<{
  filter: CardFilter
  seriesOptions: CardSeriesVO[]
  categoryOptions: CardCategoryVO[]
}>()

const showMore = defineModel<boolean>('showMore', { required: true })

const emit = defineEmits<{
  /** 单个筛选字段变更：由父组件合并到自己的 filter */
  (e: 'update', payload: Partial<CardFilter>): void
}>()

const personStore = usePersonStore()

/**
 * 统一把字段变更通过事件交给父组件处理。
 * 子组件不直接修改 props.filter.xxx（违反单向数据流 + ESLint 报错）。
 */
function update<K extends keyof CardFilter>(key: K, value: CardFilter[K]) {
  emit('update', { [key]: value } as Partial<CardFilter>)
}
</script>
