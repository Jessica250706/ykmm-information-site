import { readonly, ref } from 'vue'
import { DEFAULT_THEME, PALETTE_KEYS, type ThemeConfig, type ThemeName, THEMES } from '@/constants'
import { generateColorScale } from '@/utils'

const STORAGE_KEY = 'app-theme'
const currentTheme = ref<ThemeName>(DEFAULT_THEME)

/** 把一个 base 色的所有色阶写到 :root */
function applyColor(name: string, base: string) {
  const scale = generateColorScale(base)
  const root = document.documentElement
  Object.entries(scale).forEach(([key, value]) => {
    const varName = key === 'base' ? `--el-color-${name}` : `--el-color-${name}-${key}`
    root.style.setProperty(varName, value)
  })
}

/** 计算菜单配色：优先用 theme.menu，否则按明/暗派生 */
function resolveMenu(theme: ThemeConfig) {
  const scale = generateColorScale(theme.primary)

  if (theme.dark) {
    return {
      bg: theme.menu?.bg ?? '#18181b',
      text: theme.menu?.text ?? '#a1a1aa',
      textActive: theme.menu?.textActive ?? theme.primary,
      hoverBg: theme.menu?.hoverBg ?? '#27272a',
      activeBg: theme.menu?.activeBg ?? '#27272a',
      border: theme.menu?.border ?? '#27272a',
    }
  }

  return {
    bg: theme.menu?.bg ?? '#ffffff',
    text: theme.menu?.text ?? '#475569',
    textActive: theme.menu?.textActive ?? theme.primary,
    hoverBg: theme.menu?.hoverBg ?? scale['light-9'],
    activeBg: theme.menu?.activeBg ?? scale['light-9'],
    border: theme.menu?.border ?? '#e2e8f0',
  }
}

function applyTheme(theme: ThemeConfig) {
  const root = document.documentElement

  // 1. Element Plus 基础色
  applyColor('primary', theme.primary)
  applyColor('success', theme.success)
  applyColor('warning', theme.warning)
  applyColor('danger', theme.danger)
  applyColor('error', theme.danger)
  applyColor('info', theme.info)

  // 4. 基础配色
  PALETTE_KEYS.forEach((key) => {
    root.style.setProperty(`--color-${key}`, theme.palette[key])
  })

  // 2. 暗色模式开关
  if (theme.dark) {
    root.classList.add('dark')
  } else {
    root.classList.remove('dark')
  }

  // 3. 菜单配色
  const menu = resolveMenu(theme)
  root.style.setProperty('--menu-bg', menu.bg)
  root.style.setProperty('--menu-text', menu.text)
  root.style.setProperty('--menu-text-active', menu.textActive)
  root.style.setProperty('--menu-hover-bg', menu.hoverBg)
  root.style.setProperty('--menu-active-bg', menu.activeBg)
  root.style.setProperty('--menu-border', menu.border)

  root.setAttribute('data-theme', theme.name)
}

function setTheme(name: ThemeName) {
  const theme = THEMES[name]
  if (!theme) return
  currentTheme.value = name
  applyTheme(theme)
  localStorage.setItem(STORAGE_KEY, name)
}

function initTheme() {
  const saved = localStorage.getItem(STORAGE_KEY) as ThemeName | null
  const name = saved && THEMES[saved] ? saved : DEFAULT_THEME
  setTheme(name)
}

export function useTheme() {
  return {
    currentTheme: readonly(currentTheme),
    themes: THEMES,
    setTheme,
    initTheme,
  }
}
