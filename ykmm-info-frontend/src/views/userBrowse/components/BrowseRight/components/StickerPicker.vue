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
        <el-button size="small" plain>😀 表情</el-button>
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
        <!-- 分组切换 -->
        <div class="sticker-picker__tabs">
          <button
            v-for="g in groups"
            :key="g.id"
            :class="{ 'is-active': activeGroupId === g.id }"
            class="sticker-picker__tab"
            type="button"
            @click="activeGroupId = g.id ?? null"
          >
            {{ g.name }}
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
import { computed, ref, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { listStickerGroupWithStickersAPI } from '@/api/stickerGroup'
import { STICKER_TYPE } from '@/constants'
import type { StickerGroupWithStickersVO, StickerVO } from '@/types/sticker'

const visible = ref(false)
const loading = ref(false)
const groups = ref<StickerGroupWithStickersVO[]>([])
const activeGroupId = ref<number | null>(null)

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

.sticker-picker__tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding-bottom: 8px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.sticker-picker__tab {
  padding: 4px 10px;
  border-radius: 12px;
  border: none;
  background: transparent;
  color: var(--el-text-color-regular);
  font-size: 12px;
  cursor: pointer;
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
