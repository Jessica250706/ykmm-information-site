<template>
  <div class="mb-5 flex flex-wrap items-center justify-between gap-3 shrink-0">
    <div class="flex items-center gap-3">
      <h1 class="text-xl font-semibold">卡面</h1>
      <el-radio-group v-model="cardSize" size="small">
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
    <el-input
      v-model="keyword"
      :prefix-icon="Search"
      placeholder="搜索卡面"
      style="width: 320px"
      clearable
      @clear="emit('search')"
      @keyup.enter="emit('search')"
    />
    <el-button v-if="showCreate" type="primary" @click="emit('create')">
      <el-icon class="mr-1"><Plus /></el-icon>
      新增卡面
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
