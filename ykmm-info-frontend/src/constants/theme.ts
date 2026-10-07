export interface ThemeMenu {
  /** 侧栏 / 导航背景 */
  bg?: string
  /** 普通文字 */
  text?: string
  /** 激活文字 */
  textActive?: string
  /** hover 背景 */
  hoverBg?: string
  /** 激活背景 */
  activeBg?: string
  /** 边框、分割线 */
  border?: string
}

/**
 * 主题调色板：除 primary/success/warning/danger/info 之外，
 * 用于图表、标签、头像底色、卡片装饰等场景的 12 种颜色。
 *
 * 键序即色相环顺序：红 → 橙 → 琥珀 → 黄 → 青柠 → 绿 →
 *                    青绿 → 青 → 蓝 → 靛 → 紫 → 粉
 */
export interface ThemePalette {
  red: string
  orange: string
  amber: string
  yellow: string
  lime: string
  green: string
  teal: string
  cyan: string
  blue: string
  indigo: string
  purple: string
  pink: string
}

export interface ThemeConfig {
  /** 主题标识 */
  name: string
  /** 展示名 */
  label: string
  /** 主色 */
  primary: string
  /** 成功色 */
  success: string
  /** 警告色 */
  warning: string
  /** 危险色 */
  danger: string
  /** 信息色 */
  info: string
  /**
   * 是否是暗色主题。
   * true 时 useTheme 会给 <html> 加 .dark 类，
   * 并按暗色默认值派生菜单配色。
   */
  dark?: boolean
  /** 可选：菜单配色。不填则按明/暗自动派生 */
  menu?: ThemeMenu
  /** 12 色调色板，用于 primary 等之外的色彩场景 */
  palette: ThemePalette
}

export const THEMES: Record<string, ThemeConfig> = {
  // ---------- 春日新绿 ----------
  spring: {
    name: 'spring',
    label: '春日新绿',
    primary: '#3da35d',
    success: '#2e8b57',
    warning: '#e6a23c',
    danger: '#e05c5c',
    info: '#7a9e8a',
    palette: {
      red: '#e05c5c',
      orange: '#e69a3c',
      amber: '#d4a840',
      yellow: '#d4b840',
      lime: '#8cbf4a',
      green: '#3da35d',
      teal: '#4ba38c',
      cyan: '#4ba3a3',
      blue: '#4a7fc4',
      indigo: '#5c6bc4',
      purple: '#8a6bc4',
      pink: '#d46ba8',
    },
  },

  // ---------- 春日樱花 ----------
  cherry: {
    name: 'cherry',
    label: '春日樱花',
    primary: '#e86a92',
    success: '#4caf7d',
    warning: '#e8a33c',
    danger: '#d9534f',
    info: '#b08aa8',
    palette: {
      red: '#e05c5c',
      orange: '#e8905c',
      amber: '#e6a860',
      yellow: '#e6c04a',
      lime: '#a8c95c',
      green: '#6bbf7a',
      teal: '#5cb59e',
      cyan: '#5cb5b5',
      blue: '#5c8fd4',
      indigo: '#7a7ad4',
      purple: '#a06cd5',
      pink: '#e86a92',
    },
  },

  // ---------- 深林绿 ----------
  forest: {
    name: 'forest',
    label: '深林绿',
    primary: '#2f6b4f',
    success: '#3a8c63',
    warning: '#c98a2e',
    danger: '#c74a4a',
    info: '#6d8c7a',
    menu: {
      bg: '#f6faf7',
      text: '#4b5563',
      textActive: '#2f6b4f',
      hoverBg: '#e8f3ec',
      activeBg: '#d6ebde',
      border: '#e5e7eb',
    },
    palette: {
      red: '#c74a4a',
      orange: '#d68a3c',
      amber: '#c9983c',
      yellow: '#c9a52e',
      lime: '#7aa53a',
      green: '#2f6b4f',
      teal: '#3a8c7a',
      cyan: '#3a8c8c',
      blue: '#3a6ba5',
      indigo: '#5a5c9e',
      purple: '#6b5c9e',
      pink: '#b85c8c',
    },
  },

  // ---------- 海盐蓝 ----------
  ocean: {
    name: 'ocean',
    label: '海盐蓝',
    primary: '#3b82c4',
    success: '#4caf7d',
    warning: '#e6a23c',
    danger: '#e05c5c',
    info: '#7a9eb5',
    palette: {
      red: '#e05c5c',
      orange: '#e6953c',
      amber: '#d9a83c',
      yellow: '#e6c04a',
      lime: '#8cbf5c',
      green: '#4caf7d',
      teal: '#4ba89e',
      cyan: '#4bb0b5',
      blue: '#3b82c4',
      indigo: '#5c6bc4',
      purple: '#7a6bc4',
      pink: '#d46a9e',
    },
  },

  // ---------- 暗夜森林（暗黑） ----------
  dark: {
    name: 'dark',
    label: '暗夜森林',
    dark: true,
    primary: '#5cb87a',
    success: '#4caf7d',
    warning: '#d4a017',
    danger: '#e05c5c',
    info: '#7a9e8a',
    menu: {
      bg: '#18181b',
      text: '#a1a1aa',
      textActive: '#5cb87a',
      hoverBg: '#27272a',
      activeBg: '#27272a',
      border: '#27272a',
    },
    // 暗色主题下整体提亮，保证深色背景上的可读性
    palette: {
      red: '#e07070',
      orange: '#e8a65c',
      amber: '#e0b45c',
      yellow: '#e6c860',
      lime: '#a8d070',
      green: '#5cb87a',
      teal: '#5cc0a0',
      cyan: '#5cc0c0',
      blue: '#6ca0e0',
      indigo: '#8090e0',
      purple: '#a880e0',
      pink: '#e090b0',
    },
  },
}

export type ThemeName = keyof typeof THEMES
export const DEFAULT_THEME: ThemeName = 'spring'

/** 调色板的键序：按色相环排列，用于稳定遍历 / 生成图表色板 */
export const PALETTE_KEYS = [
  'red',
  'orange',
  'amber',
  'yellow',
  'lime',
  'green',
  'teal',
  'cyan',
  'blue',
  'indigo',
  'purple',
  'pink',
] as const satisfies readonly (keyof ThemePalette)[]

/** 把调色板对象转成数组，顺序与 PALETTE_KEYS 一致 */
export function paletteToArray(palette: ThemePalette): string[] {
  return PALETTE_KEYS.map((k) => palette[k])
}

export const PALETTE_COLOR_OPTIONS = [
  { label: '红色', value: 'red' },
  { label: '橙色', value: 'orange' },
  { label: '琥珀色', value: 'amber' },
  { label: '黄色', value: 'yellow' },
  { label: '青柠绿', value: 'lime' },
  { label: '绿色', value: 'green' },
  { label: '蓝绿色', value: 'teal' },
  { label: '青色', value: 'cyan' },
  { label: '蓝色', value: 'blue' },
  { label: '靛蓝', value: 'indigo' },
  { label: '紫色', value: 'purple' },
  { label: '粉色', value: 'pink' },
]

/** 调色板键名的联合类型：'red' | 'orange' | ... | 'pink' */
export type PaletteKey = (typeof PALETTE_KEYS)[number]
