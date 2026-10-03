<template>
  <el-card class="flex w-64 shrink-0 flex-col border-r bg-white">
    <!-- 标题 -->
    <div class="flex flex-col border-b px-2 py-3">
      <div class="text-xs text-slate-400">剧情类型</div>
      <div class="flex items-center justify-between">
        <div class="min-w-0 flex-1 truncate font-medium">{{ typeLabel }}</div>
        <el-dropdown placement="bottom" trigger="click">
          <el-button>切换</el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="item in types"
                :key="item.id"
                :disabled="item.name === typeLabel"
                @click="emit('switch-type', item.id!)"
              >
                {{ item.name }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 目录树 -->
    <div class="flex-1 overflow-x-hidden overflow-y-auto py-2">
      <el-tree
        :data="categoryTree"
        :default-expanded-keys="expandedKeys"
        :expand-on-click-node="false"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        highlight-current
        @node-click="emit('node-click', $event)"
      >
        <template #default="{ data }">
          <el-tooltip :content="data.name" :show-after="300" placement="right">
            <span class="min-w-0 flex-1 truncate">{{ data.name }}</span>
          </el-tooltip>
        </template>
      </el-tree>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import type { StoryCategoryVO } from '@/types/storyCategory'
import type { StoryCategoryTypeVO } from '@/types/storyCategoryType'

defineProps<{
  typeLabel: string
  categoryTree: StoryCategoryVO[]
  expandedKeys: number[]
  types: StoryCategoryTypeVO[]
}>()

const emit = defineEmits<{
  'node-click': [data: StoryCategoryVO]
  'switch-type': [typeId: number]
}>()
</script>

<style lang="scss" scoped>
:deep(.el-tree-node__content) {
  width: 100%;
  min-width: 0;
  padding-right: 8px;
}
</style>
