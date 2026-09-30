<template>
  <!-- 分组节点 -->
  <el-sub-menu v-if="hasChildren" :index="item.title">
    <template #title>
      <span v-if="item.icon" class="text-base leading-none">{{ item.icon }}</span>
      <span>{{ item.title }}</span>
    </template>

    <AdminMenuItem v-for="child in item.children" :key="child.title" :item="child" />
  </el-sub-menu>

  <!-- 叶子节点 -->
  <el-menu-item v-else :index="item.path!">
    <span v-if="item.icon" class="text-base leading-none">{{ item.icon }}</span>
    <template #title>{{ item.title }}</template>
  </el-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { MenuItem } from '@/config/adminMenu'

defineOptions({ name: 'AdminMenuItem' })

const props = defineProps<{
  item: MenuItem
}>()

const hasChildren = computed(() => !!props.item.children?.length)
</script>

<style lang="scss" scoped></style>
