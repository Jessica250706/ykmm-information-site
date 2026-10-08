<template>
  <el-card
    body-class="flex flex-col h-full overflow-hidden p-0"
    class="flex w-64 shrink-0 flex-col overflow-hidden border-r bg-white"
  >
    <!-- 标题 -->
    <div class="flex shrink-0 flex-col border-b px-2 py-3 mb-2">
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
    <div class="flex-1 min-h-0 overflow-x-hidden overflow-y-auto py-2">
      <el-tree
        ref="treeRef"
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
import { nextTick, ref, watch } from 'vue'
import type { StoryCategoryVO } from '@/types/storyCategory'
import type { StoryCategoryTypeVO } from '@/types/storyCategoryType'
import type { ElTree } from 'element-plus'

const props = defineProps<{
  typeLabel: string
  categoryTree: StoryCategoryVO[]
  expandedKeys: number[]
  types: StoryCategoryTypeVO[]
  /** 外部指定当前高亮节点（例如从中间点击子分类后回传） */
  currentId?: number | null
}>()

const emit = defineEmits<{
  'node-click': [data: StoryCategoryVO]
  'switch-type': [typeId: number]
}>()

const treeRef = ref<InstanceType<typeof ElTree>>()

/**
 * 树数据或目标 id 变化时，把高亮同步到 targetId。
 * - nextTick 保证 el-tree 内部已经渲染完数据（首次加载、切换类型都靠它）
 * - undefined 会让 el-tree 清空当前高亮
 */
async function syncCurrentKey(targetId?: number | null) {
  await nextTick()
  treeRef.value?.setCurrentKey(targetId ?? undefined)
}

watch(
  () => [props.currentId, props.categoryTree] as const,
  () => {
    void syncCurrentKey(props.currentId)
  },
  { immediate: true },
)
</script>

<style lang="scss" scoped>
:deep(.el-tree-node__content) {
  width: 100%;
  min-width: 0;
  padding-right: 8px;
}
</style>
