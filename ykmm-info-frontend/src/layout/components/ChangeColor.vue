<template>
  <!-- 触发按钮 -->
  <el-button size="small" plain @click="drawerVisible = true">
    主题：{{ themes[currentTheme]?.label }}
  </el-button>

  <!-- 右侧抽屉 -->
  <el-drawer
    v-model="drawerVisible"
    :append-to-body="true"
    :show-close="true"
    :size="320"
    direction="rtl"
    title="主题配色"
  >
    <p class="mb-4 text-xs text-slate-400">选择一套你喜欢的配色，立即生效</p>

    <div class="flex flex-col gap-3">
      <div
        v-for="t in themeList"
        :key="t.name"
        :class="[
          'group relative cursor-pointer rounded-xl border p-3 transition',
          t.name === currentTheme
            ? 'border-primary bg-primary-lightest/60 shadow-sm'
            : 'border-slate-200 hover:border-primary-light hover:bg-slate-50',
        ]"
        @click="handleSelect(t.name)"
      >
        <div class="flex items-center gap-3">
          <!-- 主色圆点 -->
          <span
            :style="{ backgroundColor: t.primary }"
            class="h-8 w-8 shrink-0 rounded-full ring-2 ring-white"
          />

          <!-- 主题名 -->
          <div class="min-w-0 flex-1">
            <div class="truncate text-sm font-medium text-slate-700">
              {{ t.label }}
            </div>
            <div class="mt-0.5 truncate text-xs text-slate-400">
              {{ t.primary }}
            </div>
          </div>

          <!-- 当前标记 -->
          <el-icon v-if="t.name === currentTheme" class="text-primary">
            <Check />
          </el-icon>
        </div>

        <!-- 色板预览：主色 / 成功 / 警告 / 危险 / 信息 -->
        <div class="mt-3 flex gap-1">
          <span
            v-for="(c, i) in colorDots(t)"
            :key="i"
            :style="{ backgroundColor: c }"
            class="h-1.5 flex-1 rounded-full"
          />
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Check } from '@element-plus/icons-vue'
import { useTheme } from '@/composables/useTheme'
import type { ThemeConfig, ThemeName } from '@/constants'

const { currentTheme, themes, setTheme } = useTheme()

const themeList = Object.values(themes)
const drawerVisible = ref(false)

/** 主题卡片底部的色板 */
function colorDots(t: ThemeConfig) {
  return [t.primary, t.success, t.warning, t.danger, t.info]
}

function handleSelect(name: ThemeName) {
  setTheme(name)
  drawerVisible.value = false
}
</script>
