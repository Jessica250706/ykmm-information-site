<template>
  <!-- 分组节点 -->
  <button
    v-if="hasChildren"
    type="button"
    class="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm transition"
    :class="isActive ? 'text-white' : 'text-slate-400 hover:bg-slate-800 hover:text-white'"
    :style="{ paddingLeft: `${12 + level * 12}px` }"
    @click="open = !open"
  >
    <span class="text-base leading-none">{{ item.icon }}</span>
    <span class="flex-1 text-left">{{ item.title }}</span>
    <span
      class="text-xs transition-transform duration-200"
      :class="open ? 'rotate-90' : 'rotate-0'"
    >
      ›
    </span>
  </button>

  <!-- 叶子节点 -->
  <RouterLink
    v-else
    :to="item.path!"
    class="block rounded-lg py-2 text-sm transition"
    :class="
      isActive
        ? 'bg-indigo-600 font-medium text-white'
        : 'text-slate-400 hover:bg-slate-800 hover:text-white'
    "
    :style="{ paddingLeft: `${12 + level * 12}px` }"
  >
    {{ item.title }}
  </RouterLink>

  <!-- 子菜单 -->
  <div v-if="hasChildren && open" class="mt-1 space-y-1">
    <AdminMenuItem
      v-for="child in item.children"
      :key="child.title"
      :item="child"
      :level="level + 1"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import type { MenuItem } from '@/config/adminMenu'

const props = withDefaults(
  defineProps<{
    item: MenuItem
    level?: number
  }>(),
  { level: 0 },
)

const route = useRoute()

const hasChildren = computed(() => !!props.item.children?.length)

const isActive = computed(() => {
  if (props.item.children?.length) {
    return props.item.children.some((c) => c.path && route.path.startsWith(c.path))
  }
  return route.path === props.item.path
})

// 有子项时默认展开
const open = ref(true)
</script>

<style lang="scss" scoped></style>
