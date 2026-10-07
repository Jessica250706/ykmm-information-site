<template>
  <div v-if="persons.length">
    <h2 class="mb-3 text-sm font-medium text-slate-500">关联人物</h2>
    <div class="flex flex-wrap gap-2">
      <el-tag
        v-for="p in persons"
        :key="p.personId"
        :style="personTagStyle(p.themeColor)"
        effect="plain"
      >
        <div class="flex items-center justify-center">
          <el-avatar :size="18" :src="p.avatar" class="mr-2 align-middle">
            {{ p.nameCn?.charAt(0) || '?' }}
          </el-avatar>
          <span>{{ p.nameCn }}</span>
        </div>
      </el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CardPersonVO } from '@/types/card'
import { personTagStyle } from '@/utils'

defineProps<{
  persons: CardPersonVO[]
}>()
</script>
