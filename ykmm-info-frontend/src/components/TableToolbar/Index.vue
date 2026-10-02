<template>
  <component :is="card ? ElCard : 'div'" v-bind="card ? { class: cardClass, shadow: 'never' } : {}">
    <el-form
      :inline="inline"
      :label-width="labelWidth"
      :model="model"
      class="table-toolbar"
      @submit.prevent
    >
      <!-- 用户自定义的表单项 -->
      <slot />

      <!-- 搜索 / 重置 -->
      <el-form-item v-if="showSearch || showReset" class="table-toolbar__btns">
        <el-button v-if="showSearch" :loading="loading" type="primary" @click="emit('search')">
          {{ searchText }}
        </el-button>
        <el-button v-if="showReset" @click="emit('reset')">
          {{ resetText }}
        </el-button>
      </el-form-item>

      <!-- 右侧操作按钮：跟着表单一起换行 -->
      <div v-if="$slots.actions" class="table-toolbar__actions">
        <slot name="actions" />
      </div>
    </el-form>
  </component>
</template>

<script setup lang="ts">
import { ElCard } from 'element-plus'

interface Props {
  /** 是否用 el-card 包裹，默认 true */
  card?: boolean
  /** 卡片外层 class，默认 'search mb-4' */
  cardClass?: string

  /** 表单 model，传给内部 el-form */
  model?: Record<string, unknown>
  /** el-form 是否 inline，默认 true */
  inline?: boolean
  /** el-form label-width */
  labelWidth?: string

  /** 搜索按钮 loading */
  loading?: boolean
  /** 是否显示搜索按钮 */
  showSearch?: boolean
  /** 是否显示重置按钮 */
  showReset?: boolean
  /** 搜索按钮文案 */
  searchText?: string
  /** 重置按钮文案 */
  resetText?: string
}

withDefaults(defineProps<Props>(), {
  card: true,
  cardClass: 'search mb-4',
  inline: true,
  showSearch: true,
  showReset: true,
  searchText: '搜索',
  resetText: '重置',
})

const emit = defineEmits<{
  (e: 'search'): void
  (e: 'reset'): void
}>()
</script>

<style lang="scss" scoped>
.table-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  column-gap: 16px;
  row-gap: 16px;

  /* el-form-item 默认有 margin-right / margin-bottom，会和 gap 叠加，去掉 */
  :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: 0;
  }
}

.table-toolbar__btns {
  margin-right: 0;
}

/* 右侧操作按钮：允许内部再换行，和表单同一行流 */
.table-toolbar__actions {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-left: auto;
}
</style>
