<template>
  <div class="image-viewer">
    <!-- 主图区域 -->
    <div ref="target" :style="mainStyle" class="main">
      <el-image
        :fit="fit"
        :initial-index="activeIndex"
        :preview-src-list="previewList"
        :src="currentUrl"
        class="main-image"
        hide-on-click-modal
        preview-teleported
        @switch="onPreviewSwitch"
      />

      <!-- 左右切换按钮 -->
      <template v-if="imageList.length > 1">
        <button class="nav nav--prev" type="button" @click.stop="goPrev">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <button class="nav nav--next" type="button" @click.stop="goNext">
          <el-icon><ArrowRight /></el-icon>
        </button>
      </template>

      <!-- 放大镜滑块 -->
      <div v-show="magnifier && !isOutside && ready" :style="layerStyle" class="layer" />
    </div>

    <!-- 缩略图：横向滚动 + 渐变按钮 -->
    <div v-if="showThumbs && imageList.length > 1" class="thumbs-wrap">
      <!-- 左渐变按钮 -->
      <button
        v-show="canScrollLeft"
        class="thumbs-nav thumbs-nav--prev"
        type="button"
        @click="scrollThumbs(-1)"
      >
        <el-icon><ArrowLeft /></el-icon>
      </button>

      <!-- 缩略图滚动区 -->
      <ul ref="thumbsRef" class="thumbs">
        <li
          v-for="(img, index) in imageList"
          :key="index"
          :ref="(el) => setThumbRef(el, index)"
          :class="{ active: index === activeIndex }"
          :style="thumbStyle"
          @click="selectIndex(index)"
        >
          <img :src="img" alt="" />
        </li>
      </ul>

      <!-- 右渐变按钮 -->
      <button
        v-show="canScrollRight"
        class="thumbs-nav thumbs-nav--next"
        type="button"
        @click="scrollThumbs(1)"
      >
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>

    <!-- 放大镜大图 -->
    <div v-show="magnifier && !isOutside && ready" :style="largeStyle" class="large" />
  </div>
</template>

<script lang="ts" setup>
import { type ComponentPublicInstance, computed, nextTick, ref, watch } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { useElementSize, useMouseInElement } from '@vueuse/core'
import { useScroll } from '@vueuse/core'
import { clamp } from 'lodash-es'

/* -------- Props -------- */
interface Props {
  /** 图片列表（URL 数组） */
  imageList?: string[]
  /** 是否开启放大镜功能，默认关闭 */
  magnifier?: boolean
  /**
   * 主图宽度：
   * - number → px
   * - string → 直接作为 CSS 值（'100%'、'20rem'）
   * - 不传 → 100%（铺满父容器）
   */
  width?: number | string
  /**
   * 主图高度：
   * - number → px
   * - string → 直接作为 CSS 值
   * - 不传 → 高度由图片自然比例撑开
   */
  height?: number | string
  /** 放大倍数，默认 2 */
  scale?: number
  /** 缩略图边长（px），默认 68 */
  thumbSize?: number
  /** 主图 object-fit，默认 contain */
  fit?: 'fill' | 'contain' | 'cover' | 'none' | 'scale-down'
  /** 自定义预览列表，不传则用 imageList */
  previewList?: string[]
  /** 是否展示缩略图导航，默认展示 */
  showThumbs?: boolean
  /** 放大镜大图的位置，默认右侧 */
  magnifierPosition?: 'left' | 'right'
}

const props = withDefaults(defineProps<Props>(), {
  imageList: () => [],
  magnifier: false,
  width: undefined,
  height: undefined,
  scale: 2,
  thumbSize: 68,
  fit: 'contain',
  previewList: undefined,
  showThumbs: true,
  magnifierPosition: 'right',
})

/* -------- 当前图 & 预览列表 -------- */
const activeIndex = ref(0)

const currentUrl = computed(() => props.imageList[activeIndex.value] ?? '')
const previewList = computed(() =>
  props.previewList?.length ? props.previewList : props.imageList,
)

function selectIndex(index: number) {
  if (index < 0 || index >= props.imageList.length) return
  activeIndex.value = index
}

function goPrev() {
  const len = props.imageList.length
  if (len <= 1) return
  activeIndex.value = (activeIndex.value - 1 + len) % len
}

function goNext() {
  const len = props.imageList.length
  if (len <= 1) return
  activeIndex.value = (activeIndex.value + 1) % len
}

/** el-image 预览内翻页时同步索引 */
function onPreviewSwitch(index: number) {
  activeIndex.value = index
}

/** imageList 变化时，避免索引越界 */
watch(
  () => props.imageList.length,
  (len) => {
    if (len === 0) activeIndex.value = 0
    else if (activeIndex.value >= len) activeIndex.value = len - 1
  },
)

/* -------- 主图内联样式 -------- */
function toCssSize(v: number | string | undefined): string | undefined {
  if (v == null) return undefined
  return typeof v === 'number' ? `${v}px` : v
}

const mainStyle = computed<Record<string, string>>(() => {
  const style: Record<string, string> = {}
  const w = toCssSize(props.width)
  const h = toCssSize(props.height)
  // 不传 width → CSS 里默认 100%
  if (w) style.width = w
  // 不传 height → CSS 里默认 auto，让图片自然撑开
  if (h) style.height = h
  return style
})

/* -------- 实时尺寸 -------- */
const target = ref<HTMLElement | null>(null)
const { width: mainWidth, height: mainHeight } = useElementSize(target)
const ready = computed(() => mainWidth.value > 0 && mainHeight.value > 0)

/* -------- 放大镜尺寸 -------- */
const layerWidth = computed(() => mainWidth.value / props.scale)
const layerHeight = computed(() => mainHeight.value / props.scale)
const bgWidth = computed(() => mainWidth.value * props.scale)
const bgHeight = computed(() => mainHeight.value * props.scale)
const largeWidth = computed(() => mainWidth.value)
const largeHeight = computed(() => mainHeight.value)
const maxLeft = computed(() => Math.max(0, mainWidth.value - layerWidth.value))
const maxTop = computed(() => Math.max(0, mainHeight.value - layerHeight.value))

/* -------- 鼠标位置 -------- */
const { elementX, elementY, isOutside } = useMouseInElement(target)
const left = ref(0)
const top = ref(0)

const layerStyle = computed(() => ({
  width: `${layerWidth.value}px`,
  height: `${layerHeight.value}px`,
  left: `${left.value}px`,
  top: `${top.value}px`,
  transform: 'translate(-50%, -50%)',
}))

watch([elementX, elementY, isOutside, mainWidth, mainHeight], () => {
  if (!props.magnifier) return
  if (isOutside.value || !ready.value) return

  const halfW = layerWidth.value / 2
  const halfH = layerHeight.value / 2
  left.value = clamp(elementX.value, halfW, mainWidth.value - halfW)
  top.value = clamp(elementY.value, halfH, mainHeight.value - halfH)
})

/* -------- 放大镜大图样式 -------- */
const bgX = computed(() => {
  if (maxLeft.value <= 0) return 0
  const ratio = (left.value - layerWidth.value / 2) / maxLeft.value
  return -ratio * (bgWidth.value - largeWidth.value)
})

const bgY = computed(() => {
  if (maxTop.value <= 0) return 0
  const ratio = (top.value - layerHeight.value / 2) / maxTop.value
  return -ratio * (bgHeight.value - largeHeight.value)
})

const largeStyle = computed<Record<string, string>>(() => {
  const style: Record<string, string> = {
    backgroundImage: `url(${currentUrl.value})`,
    backgroundSize: `${bgWidth.value}px ${bgHeight.value}px`,
    backgroundPositionX: `${bgX.value}px`,
    backgroundPositionY: `${bgY.value}px`,
    width: `${largeWidth.value}px`,
    height: `${largeHeight.value}px`,
  }
  if (props.magnifierPosition === 'left') {
    style.right = 'calc(100% + 12px)'
    style.left = 'auto'
  } else {
    style.left = 'calc(100% + 12px)'
    style.right = 'auto'
  }
  return style
})

/* -------- 缩略图样式 -------- */
const thumbStyle = computed(() => ({
  width: `${props.thumbSize}px`,
  height: `${props.thumbSize}px`,
}))

/* -------- 缩略图横向滚动 -------- */
/* -------- 缩略图横向滚动 -------- */
const thumbsRef = ref<HTMLElement | null>(null)
const { arrivedState } = useScroll(thumbsRef)
const canScrollLeft = computed(() => !arrivedState.left)
const canScrollRight = computed(() => !arrivedState.right)

/** 手动点击左右按钮：滚一屏的 80% */
function scrollThumbs(direction: -1 | 1) {
  const el = thumbsRef.value
  if (!el) return
  const step = el.clientWidth * 0.8
  el.scrollBy({ left: direction * step, behavior: 'smooth' })
}

/**
 * 保证第 index-1 / index / index+1 张缩略图在可视范围内。
 * 额外行为：如果当前缩略图已经贴到右/左边缘，
 * 主动多滚一点，让相邻的那张也露出来。
 */
async function ensureThumbVisible(index: number) {
  await nextTick()
  const container = thumbsRef.value
  const thumb = thumbRefs.value[index]
  if (!container || !thumb) return

  const padding = 24
  const containerRect = container.getBoundingClientRect()
  const visibleWidth = containerRect.width - padding * 2

  const thumbRect = thumb.getBoundingClientRect()
  const prevRect = thumbRefs.value[index - 1]?.getBoundingClientRect() ?? null
  const nextRect = thumbRefs.value[index + 1]?.getBoundingClientRect() ?? null

  // ★ 三张缩略图整体的包围盒（相对容器左边缘的坐标）
  // 首张用 prev，末张用 next；边界情况自动退化
  const groupLeftRel = (prevRect ?? thumbRect).left - containerRect.left
  const groupRightRel = (nextRect ?? thumbRect).right - containerRect.left
  const groupWidth = groupRightRel - groupLeftRel

  // -------- 情况 A：三张整体能装下 --------
  if (groupWidth <= visibleWidth) {
    // 整体偏左 → 向左滚
    if (groupLeftRel < padding) {
      container.scrollBy({
        left: groupLeftRel - padding,
        behavior: 'smooth',
      })
      return
    }
    // 整体偏右 → 向右滚
    if (groupRightRel > containerRect.width - padding) {
      container.scrollBy({
        left: groupRightRel - (containerRect.width - padding),
        behavior: 'smooth',
      })
    }
    return
  }

  // -------- 情况 B：三张装不下 → 退化为保证 index 自身可见 --------
  const thumbLeftRel = thumbRect.left - containerRect.left
  const thumbRightRel = thumbRect.right - containerRect.left

  if (thumbLeftRel < padding) {
    container.scrollBy({
      left: thumbLeftRel - padding,
      behavior: 'smooth',
    })
  } else if (thumbRightRel > containerRect.width - padding) {
    container.scrollBy({
      left: thumbRightRel - (containerRect.width - padding),
      behavior: 'smooth',
    })
  }
}

/** 主图切换 / 点击缩略图 时，跟随滚动 */
watch(activeIndex, (index) => {
  ensureThumbVisible(index)
})

/* -------- 缩略图 DOM 引用 -------- */
const thumbRefs = ref<(HTMLElement | null)[]>([])

function setThumbRef(el: Element | ComponentPublicInstance | null, index: number) {
  thumbRefs.value[index] = el instanceof HTMLElement ? el : null
}
</script>

<style scoped lang="scss">
.image-viewer {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  height: 100%;
}

/* -------- 主图 -------- */
.main {
  position: relative;
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
  height: 100%;
  width: 100%; /* 默认铺满；内联样式可覆盖 */
  flex: 1;

  .main-image {
    display: block;
    width: 100%;
    height: 100%;
  }
}

/* -------- 左右切换按钮 -------- */
.nav {
  position: absolute;
  top: 50%;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.85);
  color: var(--el-text-color-primary);
  cursor: pointer;
  opacity: 0;
  transform: translateY(-50%);
  transition:
    opacity 0.2s,
    background-color 0.2s;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);

  &:hover {
    background: #fff;
  }
}

.main:hover .nav {
  opacity: 1;
}

.nav--prev {
  left: 8px;
}

.nav--next {
  right: 8px;
}

/* -------- 放大镜滑块 -------- */
.layer {
  position: absolute;
  z-index: 10;
  background: rgba(0, 0, 0, 0.2);
  pointer-events: none;
}

/* -------- 放大镜大图 -------- */
.large {
  position: absolute;
  top: 0;
  z-index: 500;
  background-repeat: no-repeat;
  background-color: #f8f8f8;
  border-radius: 4px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  pointer-events: none;
}

/* -------- 缩略图区（渐变按钮覆盖） -------- */
.thumbs-wrap {
  position: relative; /* 为绝对定位的渐变按钮做参照 */
  width: 100%;
}

.thumbs {
  display: flex;
  flex-wrap: nowrap; /* 强制一行 */
  gap: 8px;
  padding: 2px; /* 给激活态 border 留空间 */
  margin: 0;
  list-style: none;
  overflow-x: auto;
  overflow-y: hidden;
  scroll-behavior: smooth;

  /* 隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;

  li {
    flex: 0 0 auto;
    cursor: pointer;
    border: 2px solid transparent;
    border-radius: 4px;
    overflow: hidden;
    transition: border-color 0.2s;

    img {
      display: block;
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    &:hover,
    &.active {
      border-color: var(--menu-border-bg, var(--el-color-primary));
      border-width: 4px;
    }
  }
}

/* -------- 渐变按钮：绝对定位覆盖在滚动区两侧 -------- */
.thumbs-nav {
  position: absolute;
  top: 0;
  bottom: 0;
  z-index: 2;
  display: flex;
  align-items: center;
  width: 40px;
  padding: 0;
  border: none;
  cursor: pointer;
  color: var(--el-text-color-primary);
  transition: color 0.2s;

  &:hover {
    color: var(--el-color-primary);
  }
}

.thumbs-nav--prev {
  left: 0;
  justify-content: flex-start;
  padding-left: 6px;
  background: linear-gradient(
    to right,
    var(--el-bg-color) 0%,
    var(--el-bg-color) 30%,
    transparent 100%
  );
}

.thumbs-nav--next {
  right: 0;
  justify-content: flex-end;
  padding-right: 6px;
  background: linear-gradient(
    to left,
    var(--el-bg-color) 0%,
    var(--el-bg-color) 30%,
    transparent 100%
  );
}
</style>
