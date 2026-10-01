<template>
  <!-- 分组节点 -->
  <el-sub-menu v-if="hasChildren" :index="item.id">
    <template #title>
      <span v-if="item.icon" class="text-base leading-none">{{ item.icon }}</span>
      <span>{{ item.name }}</span>
    </template>

    <AdminMenuItem v-for="child in item.children" :key="child.id" :item="child" />
  </el-sub-menu>

  <!-- 叶子节点 -->
  <el-menu-item v-else :index="item.path!">
    <span v-if="item.icon" class="text-base leading-none">{{ item.icon }}</span>
    <template #title>{{ item.name }}</template>
  </el-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { MenuVO } from '@/types/menu'

defineOptions({ name: 'AdminMenuItem' })

const props = defineProps<{
  item: MenuVO
}>()

const hasChildren = computed(() => !!props.item.children?.length)
</script>

<style lang="scss" scoped></style>
