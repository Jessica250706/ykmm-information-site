<template>
  <el-descriptions :column="2" label-width="80px" border>
    <el-descriptions-item label="分类名">{{ detail.name ?? '-' }}</el-descriptions-item>
    <el-descriptions-item label="分类类型">
      <el-tag
        :style="categoryTagStyle(storyCategoryTypeStore.getTypeColor(detail.categoryType))"
        effect="plain"
      >
        {{ detail.categoryTypeLabel ?? '未知' }}
      </el-tag>
    </el-descriptions-item>
    <el-descriptions-item label="父分类">
      {{ detail.parentName ?? '顶级分类' }}
    </el-descriptions-item>
    <el-descriptions-item label="排序">{{ detail.sort ?? 0 }}</el-descriptions-item>
    <el-descriptions-item label="创建时间">{{ detail.createdAt ?? '-' }}</el-descriptions-item>
    <el-descriptions-item label="更新时间">{{ detail.updatedAt ?? '-' }}</el-descriptions-item>
    <el-descriptions-item label="简介">
      <span class="whitespace-pre-line">{{ detail.description ?? '-' }}</span>
    </el-descriptions-item>
  </el-descriptions>
</template>

<script setup lang="ts">
import { useStoryCategoryTypeStore } from '@/stores/storyCategoryTypeStore'
import type { StoryCategoryVO } from '@/types/storyCategory'
import { categoryTagStyle } from '@/utils/helpers'

defineProps<{
  detail: StoryCategoryVO
}>()

const storyCategoryTypeStore = useStoryCategoryTypeStore()
</script>
