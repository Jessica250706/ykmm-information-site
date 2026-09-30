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
  },
}

export type ThemeName = keyof typeof THEMES
export const DEFAULT_THEME: ThemeName = 'spring'
