<template>
  <section class="flex h-full min-h-0 flex-col gap-4">
    <ProfileHeader />

    <!-- 已通过答题：显示 tabs -->
    <el-tabs v-if="isPassedUser" v-model="activeTab" class="min-h-0 flex flex-1 flex-col">
      <el-tab-pane class="h-full" label="我的卡面" name="cards">
        <MyCardsTab v-if="activeTab === 'cards'" class="h-full" />
      </el-tab-pane>
      <el-tab-pane class="h-full" label="我的版本" name="versions">
        <MyVersionsTab v-if="activeTab === 'versions'" class="h-full" />
      </el-tab-pane>
      <el-tab-pane class="h-full" name="messages">
        <template #label>
          <el-badge :is-dot="unreadCount" :offset="[5, 3]">
            <span>我的消息</span>
          </el-badge>
        </template>
        <MyMessagesTab v-if="activeTab === 'messages'" class="h-full" />
      </el-tab-pane>
    </el-tabs>

    <!-- 未通过答题：引导去答题 -->
    <div v-else class="flex min-h-0 flex-1 items-center justify-center">
      <el-card class="w-full max-w-md" shadow="never">
        <div class="flex flex-col items-center gap-4 py-6 text-center">
          <div class="text-5xl">
            <el-icon><Lock /></el-icon>
          </div>
          <h2 class="text-lg font-semibold text-slate-800">尚未通过答题</h2>
          <p class="text-sm leading-relaxed text-slate-500">
            完成答题后即可查看「我的卡面」「我的版本」和「我的消息」。 请先完成答题再来吧。
          </p>
          <el-button class="mt-2 w-full" type="primary" @click="handleGoQuiz">前往答题 →</el-button>
        </div>
      </el-card>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'
import { CommonRouteName } from '@/constants'
import { useMessageStore, useUserStore } from '@/stores'
import MyCardsTab from './components/MyCardsTab.vue'
import MyMessagesTab from './components/MyMessagesTab/Index.vue'
import MyVersionsTab from './components/MyVersionsTab.vue'
import ProfileHeader from './components/ProfileHeader/Index.vue'

type TabName = 'cards' | 'versions' | 'messages'
const TAB_NAMES: TabName[] = ['cards', 'versions', 'messages']
const DEFAULT_TAB: TabName = 'cards'

const route = useRoute()
const router = useRouter()

/** ★ 从 userStore 拿 isPassedUser（storeToRefs 保持响应性） */
const { isPassedUser } = storeToRefs(useUserStore())

const messageStore = useMessageStore()
const { unreadCount } = storeToRefs(messageStore)

function resolveTab(): TabName {
  const raw = route.query.tab
  if (typeof raw === 'string' && (TAB_NAMES as string[]).includes(raw)) {
    return raw as TabName
  }
  return DEFAULT_TAB
}

const activeTab = ref<TabName>(resolveTab())

/** tab → URL */
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

/** URL → tab（浏览器后退 / 前进 / 手动改地址时反向同步） */
watch(
  () => route.query.tab,
  () => {
    const next = resolveTab()
    if (activeTab.value !== next) {
      activeTab.value = next
    }
  },
)

/** 前往答题页 */
function handleGoQuiz() {
  void router.push({ name: CommonRouteName.QUIZ })
}
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
