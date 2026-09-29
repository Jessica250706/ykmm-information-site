<template>
  <div class="goods-image">
    <!-- 左侧大图-->
    <div
      ref="target"
      class="middle"
      :style="{
        width: `${IMAGE_CONFIG.middleWidth}px`,
        height: `${IMAGE_CONFIG.middleHeight}px`,
      }"
    >
      <img alt="" :src="imageList[activeIndex] as string" />
      <!-- 蒙层小滑块 -->
      <div
        v-show="!isOutside"
        class="layer"
        :style="{
          width: `${layerWidth}px`,
          height: `${layerHeight}px`,
          left: `${left}px`,
          top: `${top}px`,
          transform: 'translate(-50%, -50%)',
        }"
      ></div>
    </div>
    <!-- 小图列表 -->
    <ul class="small">
      <li
        v-for="(img, index) in imageList"
        :key="index"
        :class="{ active: index === activeIndex }"
        @mouseenter="handleEnter(index)"
      >
        <img alt="" :src="img as string" />
      </li>
    </ul>
    <!-- 放大镜大图 -->
    <div
      v-show="!isOutside"
      class="large"
      :style="{
        backgroundImage: `url(${imageList[activeIndex]})`,
        backgroundSize: `${bgWidth}px ${bgHeight}px`,
        backgroundPositionX: `${bgX}px`,
        backgroundPositionY: `${bgY}px`,
        width: `${IMAGE_CONFIG.largeWidth}px`,
        height: `${IMAGE_CONFIG.largeHeight}px`,
      }"
    ></div>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import { useMouseInElement } from '@vueuse/core'
import { clamp } from 'lodash-es'

defineProps({
  imageList: {
    type: Array,
    default: () => [],
  },
})

// —— 尺寸配置 ——
const IMAGE_CONFIG = {
  middleWidth: 400,
  middleHeight: 400,
  largeWidth: 400,
  largeHeight: 400,
  scale: 2,
} as const

const layerWidth = IMAGE_CONFIG.middleWidth / IMAGE_CONFIG.scale
const layerHeight = IMAGE_CONFIG.middleHeight / IMAGE_CONFIG.scale
const bgWidth = IMAGE_CONFIG.middleWidth * IMAGE_CONFIG.scale
const bgHeight = IMAGE_CONFIG.middleHeight * IMAGE_CONFIG.scale
const maxLeft = IMAGE_CONFIG.middleWidth - layerWidth
const maxTop = IMAGE_CONFIG.middleHeight - layerHeight

// —— 状态 ——
const activeIndex = ref(0)
const target = ref<HTMLElement | null>(null)
const left = ref(0)
const top = ref(0)

const { elementX, elementY, isOutside } = useMouseInElement(target)

// —— 大图背景位置 ——
const bgX = computed(() => {
  const ratio = (left.value - layerWidth / 2) / maxLeft
  return -ratio * (bgWidth - IMAGE_CONFIG.largeWidth)
})

const bgY = computed(() => {
  const ratio = (top.value - layerHeight / 2) / maxTop
  return -ratio * (bgHeight - IMAGE_CONFIG.largeHeight)
})

// —— 滑块跟随鼠标 ——
watch([elementX, elementY, isOutside], () => {
  if (isOutside.value) return

  const halfLayerW = layerWidth / 2 // 滑块宽度的一半
  const halfLayerH = layerHeight / 2 // 滑块高度的一半

  // clamp(x, min, max) 把 x 限制在 [min, max] 范围内
  left.value = clamp(elementX.value, halfLayerW, IMAGE_CONFIG.middleWidth - halfLayerW)
  top.value = clamp(elementY.value, halfLayerH, IMAGE_CONFIG.middleHeight - halfLayerH)
})

const handleEnter = (index: number) => {
  activeIndex.value = index
}
</script>

<style scoped lang="scss">
.goods-image {
  width: 480px;
  height: 400px;
  position: relative;
  display: flex;

  .middle {
    background: #f5f5f5;
  }

  .large {
    position: absolute;
    top: 0;
    left: 412px;
    width: 400px;
    height: 400px;
    z-index: 500;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
    background-repeat: no-repeat;
    background-color: #f8f8f8;
  }

  .layer {
    background: rgba(0, 0, 0, 0.2);
    position: absolute;
  }

  .small {
    width: 80px;

    li {
      width: 68px;
      height: 68px;
      margin-left: 12px;
      margin-bottom: 15px;
      cursor: pointer;

      &:hover,
      &.active {
        border: 2px solid $xtxColor;
      }
    }
  }
}
</style>
