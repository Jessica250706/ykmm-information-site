<template>
  <div class="mb-5 flex items-center justify-between gap-2 sm:flex-wrap sm:gap-3 shrink-0">
    <!-- 左：标题 + 电脑端尺寸切换 -->
    <div class="flex items-center gap-3 shrink-0">
      <h1 class="text-lg sm:text-xl font-semibold whitespace-nowrap">卡面</h1>
      <el-radio-group v-model="cardSize" class="hidden! md:inline-flex!" size="small">
        <el-radio-button value="small">
          <el-icon><Grid /></el-icon>
        </el-radio-button>
        <el-radio-button value="normal">
          <el-icon><Menu /></el-icon>
        </el-radio-button>
        <el-radio-button value="big">
          <el-icon><FullScreen /></el-icon>
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 中：搜索框（移动端自适应占满剩余，电脑端固定 320px） -->
    <el-input
      v-model="keyword"
      :prefix-icon="Search"
      class="card-list-search min-w-0 flex-1"
      placeholder="搜索卡面"
      clearable
      @clear="emit('search')"
      @keyup.enter="emit('search')"
    />

    <!-- 右：新增按钮（移动端仅图标，电脑端图标 + 文字） -->
    <el-button
      v-if="showCreate"
      aria-label="新增卡面"
      class="shrink-0"
      type="primary"
      @click="emit('create')"
    >
      <el-icon><Plus /></el-icon>
      <span class="ml-1 hidden sm:inline">新增卡面</span>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { FullScreen, Grid, Menu, Plus, Search } from '@element-plus/icons-vue'
import type { CardSize } from '../composables/useCardSize'

defineProps<{ showCreate: boolean }>()
const emit = defineEmits<{ (e: 'search'): void; (e: 'create'): void }>()

const cardSize = defineModel<CardSize>('cardSize', { required: true })
const keyword = defineModel<string>('keyword', { required: true })
</script>

<style lang="scss" scoped>
/* 手机端：flex-1 撑满剩余 */
.card-list-search {
  flex: 1;
  min-width: 0;
  margin: 0 30px;
}

/* ≥ 640px：固定 320px，不参与 flex */
@media (min-width: 640px) {
  .card-list-search {
    flex: none;
    width: 320px;
  }
}
</style>
