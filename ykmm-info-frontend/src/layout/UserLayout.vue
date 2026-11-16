<template>
  <div class="min-h-screen">
    <el-container class="min-h-screen">
      <el-header
        class="sticky top-0 z-30 border-b border-slate-200 bg-white/85 backdrop-blur px-0! h-13.5! sm:h-16!"
      >
        <div class="mx-auto flex h-full max-w-6xl items-center px-3 sm:px-4">
          <!-- Logo -->
          <RouterLink
            :to="{ name: UserRouteName.CARD_LIST }"
            class="flex shrink-0 items-center gap-2"
          >
            <el-avatar
              :size="isMobile ? 28 : 32"
              :style="{ backgroundColor: 'var(--el-color-primary)' }"
              class="text-sm font-bold"
            >
              U
            </el-avatar>
            <span class="hidden font-semibold sm:inline">用户端</span>
          </RouterLink>

          <!-- 移动端：汉堡按钮 -->
          <button
            aria-label="打开菜单"
            class="md:hidden mr-1 flex h-9 w-9 items-center justify-center rounded-md text-slate-600 transition hover:bg-slate-100"
            type="button"
            @click="mobileMenuVisible = true"
          >
            <el-icon :size="20"><Menu /></el-icon>
          </button>

          <!-- 桌面端水平导航 -->
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

          <!-- 右侧操作区 -->
          <div class="ml-auto flex items-center gap-2">
            <el-button
              v-if="isAdmin"
              class="hidden sm:inline-flex"
              size="small"
              plain
              @click="goAdmin"
            >
              前往管理端 →
            </el-button>

            <!-- 已登录 -->
            <el-dropdown
              v-if="isLogin"
              placement="bottom-end"
              trigger="click"
              @command="handleUserCommand"
            >
              <div
                :class="isActive('/profile') ? 'theme-pill-active' : 'hover:bg-slate-100'"
                class="flex cursor-pointer items-center gap-2 rounded-full py-1 pl-1 pr-2 transition"
              >
                <el-badge :hidden="!hasUnread" :max="99" :value="unreadCount" class="avatar-badge">
                  <el-avatar :size="28" :src="userInfo?.avatar" class="bg-slate-200! text-xs!">
                    {{ avatarText }}
                  </el-avatar>
                </el-badge>
                <span class="hidden sm:inline text-sm">{{ displayName }}</span>
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

            <!-- 未登录 -->
            <RouterLink
              v-else
              :to="{ path: '/login', query: { redirect: route.fullPath } }"
              class="flex items-center gap-2 rounded-full py-1 pl-1 pr-2 sm:pr-3 transition hover:bg-slate-100"
            >
              <el-avatar :size="28" class="bg-slate-200! text-xs!">游</el-avatar>
              <span class="hidden sm:inline text-sm">登录 / 注册</span>
            </RouterLink>

            <ChangeColor />
          </div>
        </div>
      </el-header>

      <!-- 移动端导航抽屉 -->
      <el-drawer v-model="mobileMenuVisible" :size="260" :with-header="false" direction="ltr">
        <div class="flex h-full flex-col">
          <!-- 抽屉头部 -->
          <div class="mb-2 flex items-center gap-2 border-b border-slate-100 px-2 pb-4">
            <el-avatar
              :size="28"
              :style="{ backgroundColor: 'var(--el-color-primary)' }"
              class="text-xs font-bold"
            >
              U
            </el-avatar>
            <span class="font-semibold">用户端</span>
          </div>

          <!-- 垂直导航 -->
          <el-menu
            :default-active="activeMenu"
            class="user-mobile-menu"
            mode="vertical"
            router
            @select="mobileMenuVisible = false"
          >
            <el-menu-item v-for="nav in menuStore.userNavFlat" :key="nav.path" :index="nav.path">
              {{ nav.name }}
            </el-menu-item>
          </el-menu>

          <!-- 底部操作 -->
          <div
            class="mobile-menu-actions mt-auto flex flex-col gap-2 border-t border-slate-100 pt-4"
          >
            <el-button v-if="isAdmin" plain @click="goAdmin">前往管理端 →</el-button>
            <template v-if="isLogin">
              <el-button type="info" plain @click="handleUserCommand('profile')">
                个人中心
              </el-button>
              <el-button class="mt-2" plain @click="handleUserCommand('logout')">
                退出登录
              </el-button>
            </template>
            <el-button v-else @click="handleGuestLogin">登录 / 注册</el-button>
          </div>
        </div>
      </el-drawer>

      <!-- 内容区 -->
      <el-main class="p-0!">
        <div class="userLayout mx-auto w-full max-w-6xl overflow-hidden px-3 py-2 sm:px-4 sm:py-6">
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
import { computed, onMounted, ref, watch } from 'vue'
import { Menu } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { storeToRefs } from 'pinia'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useIsMobile } from '@/composables/useIsMobile'
import { AdminRouteName, CommonRouteName, UserRouteName } from '@/constants'
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

const isMobile = useIsMobile()

/** 移动端导航抽屉展开状态 */
const mobileMenuVisible = ref(false)

const isActive = (path: string) => route.path === path || route.path.startsWith(`${path}/`)

/** 用户态展示名：昵称 > 邮箱 > 兜底 */
const displayName = computed(
  () => userInfo?.value?.nickname || userInfo?.value?.email || '个人中心',
)

/** 无头像时的文字占位 */
const avatarText = computed(() => {
  const name = userInfo?.value?.nickname || userInfo?.value?.email || ''
  return name ? name.charAt(0).toUpperCase() : '我'
})

/** 导航菜单激活项（桌面 + 移动共用） */
const activeMenu = computed(() => {
  const matchedNav = menuStore.userNavFlat.find((nav) => {
    const p = nav.path
    return route.path === p || route.path.startsWith(`${p}/`)
  })
  return matchedNav?.path ?? ''
})

async function handleUserCommand(cmd: string) {
  mobileMenuVisible.value = false
  if (cmd === 'profile') {
    await router.push({ name: UserRouteName.PROFILE })
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

function goAdmin() {
  mobileMenuVisible.value = false
  router.push({ name: AdminRouteName.PERSON_MANAGE })
}

function handleGuestLogin() {
  mobileMenuVisible.value = false
  router.push({ name: CommonRouteName.LOGIN, query: { redirect: route.fullPath } })
}

/** 路由跳转后自动关闭移动端抽屉 */
watch(
  () => route.fullPath,
  () => {
    mobileMenuVisible.value = false
  },
)

/** 登录状态变化时同步未读数 + 关闭抽屉 */
watch(
  () => userStore.isLogin,
  (login) => {
    if (login) {
      void messageStore.loadUnreadCount()
    } else {
      messageStore.reset()
      mobileMenuVisible.value = false
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

/* 桌面端水平导航 */
.user-nav-menu {
  border-bottom: none;
  background: transparent;
  /* 恢复 flex 让 40px 的菜单项在 64px header 里垂直居中 */
  display: flex;
  align-items: center;

  /* 移动端隐藏：!important 覆盖 base 的 display: flex */
  @media (max-width: 767px) {
    display: none !important;
  }

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

/* 移动端垂直导航：更宽松的行高，好点击 */
.user-mobile-menu {
  border-right: none;

  :deep(.el-menu-item) {
    height: 44px;
    line-height: 44px;
    margin: 2px 0;
    border-radius: 0.5rem;
  }
}

.mobile-menu-actions {
  /* 消除 el-button 相邻 12px 左间距 */
  :deep(.el-button + .el-button) {
    margin-left: 0;
  }

  /* 按钮撑满容器 */
  :deep(.el-button) {
    width: 100%;
  }
}

.userLayout {
  height: calc(100dvh - 64px);
}
</style>
