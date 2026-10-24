<template>
  <div class="min-h-screen">
    <el-container class="min-h-screen">
      <el-header
        class="sticky top-0 z-30 border-b border-slate-200 bg-white/85 backdrop-blur"
        height="64px"
      >
        <div class="mx-auto flex h-full max-w-6xl items-center px-4">
          <!-- Logo -->
          <RouterLink class="flex shrink-0 items-center gap-2" to="/cards">
            <el-avatar
              :size="32"
              :style="{ backgroundColor: 'var(--el-color-primary)' }"
              class="text-sm font-bold"
            >
              U
            </el-avatar>
            <span class="font-semibold">用户端</span>
          </RouterLink>

          <!-- 左侧目录：卡面 / 剧情 -->
          <el-menu
            :default-active="activeMenu"
            :ellipsis="false"
            class="ml-8 user-nav-menu"
            mode="horizontal"
            router
          >
            <el-menu-item v-for="nav in menuStore.userNavFlat" :key="nav.path" :index="nav.path">
              {{ nav.name }}
            </el-menu-item>
          </el-menu>

          <!-- 最右侧：管理端入口（仅管理员可见） + 个人中心 + 主题切换 -->
          <div class="ml-auto flex items-center gap-3">
            <el-button
              v-if="isAdmin"
              class="hidden sm:inline-flex"
              size="small"
              plain
              @click="router.push({ name: AdminRouteName.PERSON_MANAGE })"
            >
              前往管理端 →
            </el-button>

            <!-- 已登录：用户态下拉 -->
            <el-dropdown
              v-if="isLogin"
              placement="bottom-end"
              trigger="click"
              @command="handleUserCommand"
            >
              <div
                :class="isActive('/profile') ? 'theme-pill-active' : 'hover:bg-slate-100'"
                class="flex cursor-pointer items-center gap-2 rounded-full py-1 pl-1 pr-3 transition"
              >
                <el-badge :hidden="!hasUnread" :max="99" :value="unreadCount" class="avatar-badge">
                  <el-avatar :size="28" :src="userInfo?.avatar" class="bg-slate-200! text-xs!">
                    {{ avatarText }}
                  </el-avatar>
                </el-badge>
                <span class="text-sm">{{ displayName }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="logout" divided>
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 未登录：游客态 -->
            <RouterLink
              v-else
              :to="{ path: '/login', query: { redirect: route.fullPath } }"
              class="flex items-center gap-2 rounded-full py-1 pl-1 pr-3 transition hover:bg-slate-100"
            >
              <el-avatar :size="28" class="bg-slate-200! text-xs!">游</el-avatar>
              <span class="text-sm">登录 / 注册</span>
            </RouterLink>

            <ChangeColor />
          </div>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="p-0!">
        <div class="userLayout mx-auto w-full max-w-6xl overflow-hidden px-4 py-6">
          <RouterView v-slot="{ Component }">
            <Transition mode="out-in" name="fade">
              <component :is="Component" />
            </Transition>
          </RouterView>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import { storeToRefs } from 'pinia'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { AdminRouteName, CommonRouteName } from '@/constants'
import { useMenuStore, useMessageStore, useUserStore } from '@/stores'
import ChangeColor from './components/ChangeColor.vue'

const route = useRoute()
const router = useRouter()

const menuStore = useMenuStore()

const userStore = useUserStore()
const { logout } = userStore
const { isAdmin, isLogin, userInfo } = storeToRefs(userStore)

const messageStore = useMessageStore()
const { unreadCount, hasUnread } = storeToRefs(messageStore)

const isActive = (path: string) => route.path === path || route.path.startsWith(`${path}/`)

/** 用户态展示名：昵称 > 邮箱 > 兜底 */
const displayName = computed(
  () => userInfo?.value?.nickname || userInfo?.value?.email || '个人中心',
)

/** 无头像时的文字占位：取展示名首字符 */
const avatarText = computed(() => {
  const name = userInfo?.value?.nickname || userInfo?.value?.email || ''
  return name ? name.charAt(0).toUpperCase() : '我'
})

/** 把 isActive 逻辑适配给 el-menu 的 default-active */
const activeMenu = computed(() => {
  const matchedNav = menuStore.userNavFlat.find((nav) => {
    const p = nav.path
    return route.path === p || route.path.startsWith(`${p}/`)
  })
  return matchedNav?.path ?? ''
})

/** 下拉菜单命令分发 */
async function handleUserCommand(cmd: string) {
  if (cmd === 'profile') {
    await router.push('/profile')
    return
  }
  if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning',
        confirmButtonText: '退出',
        cancelButtonText: '取消',
      })
    } catch {
      return
    }
    logout()
    await router.replace({ name: CommonRouteName.LOGIN })
  }
}

/** 登录状态变化时同步未读数 */
watch(
  () => userStore.isLogin,
  (login) => {
    if (login) {
      void messageStore.loadUnreadCount()
    } else {
      messageStore.reset()
    }
  },
)

onMounted(() => {
  if (!menuStore.userMenu.length) {
    void menuStore.loadUserMenu()
  }
  if (userStore.isLogin) {
    void messageStore.loadUnreadCount()
  }
})
</script>

<style lang="scss" scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 个人中心激活态 */
.theme-pill-active {
  background: var(--menu-hover-bg);
  color: var(--menu-text-active);
}

.user-nav-menu {
  border-bottom: none;
  background: transparent;
  display: flex;
  justify-content: center;
  align-items: center;

  :deep(.el-menu-item) {
    height: 40px;
    line-height: 40px;
    margin-left: 24px;
    padding: 0 12px;
    border-bottom: none;
    border-radius: 0.5rem;
    font-size: 14px;
    color: var(--menu-text);

    &:hover {
      background: var(--menu-hover-bg);
      color: var(--menu-text);
    }

    &.is-active {
      background: var(--menu-active-bg);
      color: var(--menu-text-active);
      font-weight: 500;
    }
  }
}

.userLayout {
  height: calc(100dvh - 64px);
}
</style>
