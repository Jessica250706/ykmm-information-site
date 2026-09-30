/** 把 hex 转成 [r, g, b] */
function hexToRgb(hex: string): [number, number, number] {
  const h = hex.replace('#', '')
  const full =
    h.length === 3
      ? h
          .split('')
          .map((c) => c + c)
          .join('')
      : h
  const num = parseInt(full, 16)
  return [(num >> 16) & 255, (num >> 8) & 255, num & 255]
}

/** [r, g, b] 转 hex */
function rgbToHex(r: number, g: number, b: number): string {
  const toHex = (n: number) => Math.round(n).toString(16).padStart(2, '0')
  return `#${toHex(r)}${toHex(g)}${toHex(b)}`
}

/**
 * 按 Element Plus 的规则混合颜色
 * @param base 主色
 * @param mix 混合目标，'#ffffff' 或 '#000000'
 * @param weight 混合目标占比（0-1），例如 light-3 的 weight = 0.3
 */
export function mix(base: string, target: string, weight: number): string {
  const [r1, g1, b1] = hexToRgb(base)
  const [r2, g2, b2] = hexToRgb(target)
  const w = weight

  return rgbToHex(r1 * (1 - w) + r2 * w, g1 * (1 - w) + g2 * w, b1 * (1 - w) + b2 * w)
}

/** 生成 Element Plus 风格的主色色阶（light-3 ~ light-9、dark-2） */
export function generateColorScale(base: string) {
  const WHITE = '#ffffff'
  const BLACK = '#000000'
  return {
    base,
    'light-3': mix(base, WHITE, 0.3),
    'light-5': mix(base, WHITE, 0.5),
    'light-7': mix(base, WHITE, 0.7),
    'light-8': mix(base, WHITE, 0.8),
    'light-9': mix(base, WHITE, 0.9),
    'dark-2': mix(base, BLACK, 0.2),
  }
}
