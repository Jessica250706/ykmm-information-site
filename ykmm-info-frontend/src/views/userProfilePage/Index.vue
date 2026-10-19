<template>
  <section class="flex h-full min-h-0 flex-col gap-4">
    <ProfileHeader />

    <el-tabs v-model="activeTab" class="min-h-0 flex-1 flex flex-col">
      <el-tab-pane class="h-full" label="我的卡面" name="cards">
        <MyCardsTab v-if="activeTab === 'cards'" class="h-full" />
      </el-tab-pane>
      <el-tab-pane class="h-full" label="我的版本" name="versions">
        <MyVersionsTab v-if="activeTab === 'versions'" class="h-full" />
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import MyCardsTab from './components/MyCardsTab.vue'
import MyVersionsTab from './components/MyVersionsTab.vue'
import ProfileHeader from './components/ProfileHeader.vue'

const activeTab = ref<'cards' | 'versions'>('cards')
</script>

<style scoped>
/* 让 tab 内容区能占满高度并滚动 */
:deep(.el-tabs__content) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
:deep(.el-tab-pane) {
  height: 100%;
}
</style>
