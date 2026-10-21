<template>
  <section class="flex h-full min-h-0 flex-col gap-4">
    <ProfileHeader />

    <el-tabs v-model="activeTab" class="min-h-0 flex flex-1 flex-col">
      <el-tab-pane class="h-full" label="我的卡面" name="cards">
        <MyCardsTab v-if="activeTab === 'cards'" class="h-full" />
      </el-tab-pane>
      <el-tab-pane class="h-full" label="我的版本" name="versions">
        <MyVersionsTab v-if="activeTab === 'versions'" class="h-full" />
      </el-tab-pane>
      <el-tab-pane class="h-full" label="我的消息" name="messages">
        <MyMessagesTab v-if="activeTab === 'messages'" class="h-full" />
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MyCardsTab from './components/MyCardsTab.vue'
import MyMessagesTab from './components/MyMessagesTab/Index.vue'
import MyVersionsTab from './components/MyVersionsTab.vue'
import ProfileHeader from './components/ProfileHeader/Index.vue'

type TabName = 'cards' | 'versions' | 'messages'
const TAB_NAMES: TabName[] = ['cards', 'versions', 'messages']
const DEFAULT_TAB: TabName = 'cards'

const route = useRoute()
const router = useRouter()

function resolveTab(): TabName {
  const raw = route.query.tab
  if (typeof raw === 'string' && (TAB_NAMES as string[]).includes(raw)) {
    return raw as TabName
  }
  return DEFAULT_TAB
}

const activeTab = ref<TabName>(resolveTab())

/**
 * tab → URL
 * 默认 tab 不写入 query，保持 URL 干净（/profile），
 * 非默认 tab 写入 ?tab=versions
 */
watch(activeTab, (v) => {
  const currentQuery = route.query.tab
  const nextQuery = v === DEFAULT_TAB ? undefined : v
  if (currentQuery === nextQuery) return

  const query = { ...route.query }
  if (nextQuery == null) {
    delete query.tab
  } else {
    query.tab = nextQuery
  }
  void router.replace({ query })
})

/**
 * URL → tab（浏览器后退 / 前进 / 手动改地址时反向同步）
 */
watch(
  () => route.query.tab,
  () => {
    const next = resolveTab()
    if (activeTab.value !== next) {
      activeTab.value = next
    }
  },
)
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
