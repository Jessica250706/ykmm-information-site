<template>
  <el-popover
    v-model:visible="visible"
    :width="380"
    placement="bottom-end"
    popper-class="sticker-picker-popper"
    trigger="click"
  >
    <template #reference>
      <slot name="reference">
        <el-button size="small" plain>
          <div class="flex items-center gap-1">
            <SvgIcon iconName="icon-emoji" />
            表情
          </div>
        </el-button>
      </slot>
    </template>

    <div class="sticker-picker">
      <!-- 加载中 -->
      <div v-if="loading" class="flex justify-center py-8">
        <el-icon :size="22" class="is-loading"><Loading /></el-icon>
      </div>

      <!-- 无数据 -->
      <el-empty v-else-if="!groups.length" :image-size="60" description="暂无表情包" />

      <template v-else>
        <!-- 分组切换：横向滚动 + 左右箭头 -->
        <div class="sticker-picker__tabs-wrap">
          <!-- 左箭头 -->
          <button
            v-show="canScrollLeft"
            class="sticker-picker__tabs-nav sticker-picker__tabs-nav--prev"
            type="button"
            @click="scrollTabs(-1)"
          >
            <el-icon><ArrowLeft /></el-icon>
          </button>

          <!-- 分组列表 -->
          <div ref="tabsRef" class="sticker-picker__tabs">
            <button
              v-for="g in groups"
              :key="g.id"
              :ref="(el) => setTabRef(el, g.id)"
              :class="{ 'is-active': activeGroupId === g.id }"
              class="sticker-picker__tab"
              type="button"
              @click="activeGroupId = g.id ?? null"
            >
              {{ g.name }}
            </button>
          </div>

          <!-- 右箭头 -->
          <button
            v-show="canScrollRight"
            class="sticker-picker__tabs-nav sticker-picker__tabs-nav--next"
            type="button"
            @click="scrollTabs(1)"
          >
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>

        <!-- 表情网格 -->
        <div class="sticker-picker__grid-wrap">
          <el-empty
            v-if="!currentStickers.length"
            :image-size="50"
            description="该分组暂无表情包"
          />
          <div v-else class="sticker-picker__grid">
            <button
              v-for="s in currentStickers"
              :key="s.id"
              :title="s.label"
              class="sticker-picker__cell"
              type="button"
              @click="handlePick(s)"
            >
              <img
                v-if="s.stickerType === STICKER_TYPE.IMAGE && s.imageUrl"
                :src="s.imageUrl"
                alt=""
                draggable="false"
              />
              <span v-else class="sticker-picker__emoji">
                {{ s.emoji || s.label }}
              </span>
            </button>
          </div>
        </div>
      </template>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { type ComponentPublicInstance, computed, nextTick, ref, watch } from 'vue'
import { ArrowLeft, ArrowRight, Loading } from '@element-plus/icons-vue'
import { useScroll } from '@vueuse/core'
import { listStickerGroupWithStickersAPI } from '@/api/stickerGroup'
import { STICKER_TYPE } from '@/constants'
import type { StickerGroupWithStickersVO, StickerVO } from '@/types/sticker'

const visible = ref(false)
const loading = ref(false)
const groups = ref<StickerGroupWithStickersVO[]>([])
const activeGroupId = ref<number | null>(null)

/* -------- 分组横向滚动（参考 ImageView 缩略图滚动） -------- */
const tabsRef = ref<HTMLElement | null>(null)
const { arrivedState } = useScroll(tabsRef)
const canScrollLeft = computed(() => !arrivedState.left)
const canScrollRight = computed(() => !arrivedState.right)

/** 点击箭头：滚动一屏的 80% */
function scrollTabs(direction: -1 | 1) {
  const el = tabsRef.value
  if (!el) return
  const step = el.clientWidth * 0.8
  el.scrollBy({ left: direction * step, behavior: 'smooth' })
}

/** 分组 DOM 引用，用于切分组时确保激活 tab 可见 */
const tabRefs = ref<Map<number, HTMLElement>>(new Map())

function setTabRef(el: Element | ComponentPublicInstance | null, id?: number) {
  if (id == null) return
  if (el instanceof HTMLElement) {
    tabRefs.value.set(id, el)
  } else {
    tabRefs.value.delete(id)
  }
}

/**
 * 保证指定分组的 tab 在可视范围内。
 * 靠近边缘时多留一点 padding，避免刚好"露头"就停止。
 */
async function ensureTabVisible(id: number | null) {
  if (id == null) return
  await nextTick()
  const container = tabsRef.value
  const tab = tabRefs.value.get(id)
  if (!container || !tab) return

  const padding = 24
  const containerRect = container.getBoundingClientRect()
  const tabRect = tab.getBoundingClientRect()

  const tabLeftRel = tabRect.left - containerRect.left
  const tabRightRel = tabRect.right - containerRect.left

  if (tabLeftRel < padding) {
    container.scrollBy({ left: tabLeftRel - padding, behavior: 'smooth' })
  } else if (tabRightRel > containerRect.width - padding) {
    container.scrollBy({
      left: tabRightRel - (containerRect.width - padding),
      behavior: 'smooth',
    })
  }
}

/* -------- 表情列表 -------- */
const emit = defineEmits<{
  /**
   * 选中表情包
   *
   * @param text 带方括号的标签文本，如 "[国王布丁点赞表情包]"
   * @param sticker 原始表情包对象
   */
  pick: [text: string, sticker: StickerVO]
}>()

const currentStickers = computed<StickerVO[]>(() => {
  if (activeGroupId.value == null) return []
  return groups.value.find((g) => g.id === activeGroupId.value)?.stickers ?? []
})

/** 首次展开时加载数据 */
async function ensureLoaded() {
  if (groups.value.length > 0) return
  loading.value = true
  try {
    const res = await listStickerGroupWithStickersAPI()
    groups.value = res.data ?? []
    if (groups.value.length > 0 && activeGroupId.value == null) {
      activeGroupId.value = groups.value[0]?.id ?? null
    }
  } finally {
    loading.value = false
  }
}

watch(visible, (val) => {
  if (val) void ensureLoaded()
})

/** 激活分组变化时，滚动 tab 到可视区域 */
watch(activeGroupId, (id) => {
  void ensureTabVisible(id)
})

function handlePick(s: StickerVO) {
  if (!s.label) return
  emit('pick', `[${s.label}]`, s)
  visible.value = false
}
</script>

<style lang="scss" scoped>
.sticker-picker {
  display: flex;
  flex-direction: column;
  min-height: 180px;
}

/* ============ 分组切换（横向滚动 + 箭头） ============ */
.sticker-picker__tabs-wrap {
  position: relative; /* 为绝对定位的箭头做参照 */
  padding-bottom: 8px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

/* 分组列表：一行、不换行、横向滚动、隐藏滚动条 */
.sticker-picker__tabs {
  display: flex;
  flex-wrap: nowrap; /* 强制一行 */
  gap: 4px;
  padding: 2px 0; /* 上下留一点，给箭头渐变的呼吸感 */
  overflow-x: auto;
  overflow-y: hidden;
  scroll-behavior: smooth;

  /* 隐藏滚动条（三种写法兼容） */
  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.sticker-picker__tab {
  flex: 0 0 auto; /* 不收缩，宽度由内容决定 */
  padding: 4px 10px;
  border-radius: 12px;
  border: none;
  background: transparent;
  color: var(--el-text-color-regular);
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap; /* 分组名过长时不换行，让滚动处理 */
  transition:
    background-color 0.15s,
    color 0.15s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &.is-active {
    background: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
    font-weight: 600;
  }
}

/* 左右箭头：绝对定位覆盖在分组列表两侧，配渐变背景 */
.sticker-picker__tabs-nav {
  position: absolute;
  top: 0;
  bottom: 8px; /* 和 tabs-wrap 的 padding-bottom 对齐 */
  z-index: 2;
  display: flex;
  align-items: center;
  width: 28px;
  padding: 0;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--el-text-color-primary);
  transition: color 0.2s;

  &:hover {
    color: var(--el-color-primary);
  }
}

.sticker-picker__tabs-nav--prev {
  left: 0;
  justify-content: flex-start;
  background: linear-gradient(
    to right,
    var(--el-bg-color) 0%,
    var(--el-bg-color) 50%,
    transparent 100%
  );
}

.sticker-picker__tabs-nav--next {
  right: 0;
  justify-content: flex-end;
  background: linear-gradient(
    to left,
    var(--el-bg-color) 0%,
    var(--el-bg-color) 50%,
    transparent 100%
  );
}

/* ============ 表情网格 ============ */
.sticker-picker__grid-wrap {
  max-height: 260px;
  overflow-y: auto;
}

.sticker-picker__grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
}

.sticker-picker__cell {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 56px;
  padding: 4px;
  border-radius: 6px;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: background-color 0.15s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
    user-select: none;
    -webkit-user-drag: none;
  }
}

.sticker-picker__emoji {
  font-size: 22px;
  line-height: 1;
}
</style>
