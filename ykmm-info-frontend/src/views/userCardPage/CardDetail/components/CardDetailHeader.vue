<template>
  <div class="mb-3 flex shrink-0 items-center justify-between gap-2 sm:mb-5">
    <!-- 左侧：返回 + 名称 + 标签 -->
    <div class="flex min-w-0 items-center gap-2 sm:gap-3">
      <el-button class="shrink-0" @click="emit('back')">
        <span class="hidden sm:inline">← 返回</span>
        <span class="sm:hidden">
          <el-icon><ArrowLeft /></el-icon>
        </span>
      </el-button>

      <h1 class="min-w-0 truncate text-base font-semibold sm:text-xl">
        {{ card?.name || '卡面详情' }}
      </h1>

      <el-tag
        v-if="card?.maxRarity"
        :type="getCardRarityTagType(card.maxRarity)"
        class="shrink-0"
        effect="plain"
      >
        {{ card.maxRarityLabel ?? cardMaxRarityLabel(card.maxRarity) }}
      </el-tag>

      <el-tag
        v-if="card?.status === STATUS.PENDING || card?.status === STATUS.REJECTED"
        :type="STATUS_TAG_TYPE[card.status]"
        class="shrink-0"
        effect="dark"
      >
        {{ STATUS_LABEL[card.status] }}
      </el-tag>
    </div>

    <!-- 右侧：桌面端按钮组 / 移动端下拉 -->
    <div class="flex shrink-0 items-center gap-2">
      <!-- ==================== 桌面端：原样平铺 ==================== -->
      <div class="hidden items-center gap-2 sm:flex">
        <div v-if="canOperate" class="flex gap-1">
          <el-button
            v-if="isAdmin && card?.status === STATUS.PENDING"
            type="warning"
            plain
            @click="emit('review')"
          >
            <el-icon class="mr-1"><Checked /></el-icon>
            审核
          </el-button>

          <el-button type="primary" plain @click="emit('edit')">
            <el-icon class="mr-1"><Edit /></el-icon>
            编辑卡面
          </el-button>

          <el-button
            v-if="attachedType === SOURCE_TYPE.RC"
            type="primary"
            @click="emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC])"
          >
            <el-icon class="mr-1"><Plus /></el-icon>
            新增 RC
          </el-button>

          <el-button
            v-if="attachedType === SOURCE_TYPE.RTV"
            type="primary"
            @click="emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RTV])"
          >
            <el-icon class="mr-1"><Plus /></el-icon>
            新增 RTV
          </el-button>

          <el-button
            v-if="attachedType === SOURCE_TYPE.RABITTER"
            type="primary"
            @click="emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RABITTER])"
          >
            <el-icon class="mr-1"><Plus /></el-icon>
            新增 Rabitter
          </el-button>
        </div>

        <el-button
          v-if="card?.attachedStoryTypeLabel && isList"
          type="primary"
          @click="emit('goto')"
        >
          查看{{ card.attachedStoryTypeLabel }}
        </el-button>
      </div>

      <!-- ==================== 移动端：全部收进下拉 ==================== -->
      <div class="sm:hidden">
        <el-dropdown placement="bottom-end" trigger="click" @command="handleMobileCommand">
          <el-button>
            <el-icon class="mr-1"><Menu /></el-icon>
            操作
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <template v-if="canOperate">
                <el-dropdown-item
                  v-if="isAdmin && card?.status === STATUS.PENDING"
                  command="review"
                >
                  <el-icon><Checked /></el-icon>
                  审核
                </el-dropdown-item>
                <el-dropdown-item command="edit">
                  <el-icon><Edit /></el-icon>
                  编辑卡面
                </el-dropdown-item>
                <el-dropdown-item v-if="attachedType === SOURCE_TYPE.RC" command="add-rc">
                  <el-icon><Plus /></el-icon>
                  新增 RC
                </el-dropdown-item>
                <el-dropdown-item v-if="attachedType === SOURCE_TYPE.RTV" command="add-rtv">
                  <el-icon><Plus /></el-icon>
                  新增 RTV
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="attachedType === SOURCE_TYPE.RABITTER"
                  command="add-rabitter"
                >
                  <el-icon><Plus /></el-icon>
                  新增 Rabitter
                </el-dropdown-item>
              </template>

              <el-dropdown-item v-if="card?.attachedStoryTypeLabel && isList" command="goto">
                <el-icon><ZoomIn /></el-icon>
                查看 {{ card.attachedStoryTypeLabel }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft, Checked, Edit, Menu, Plus } from '@element-plus/icons-vue'
import {
  cardMaxRarityLabel,
  SOURCE_TYPE,
  SOURCE_TYPE_SMALL_LABEL,
  type SourceTypeSmallLabelValue,
  STATUS,
  STATUS_LABEL,
  STATUS_TAG_TYPE,
} from '@/constants'
import type { CardVO } from '@/types/card'
import { getCardRarityTagType } from '@/utils'

defineProps<{
  card: CardVO | null
  attachedType: number
  isList: boolean
  canOperate: boolean
  isAdmin: boolean
}>()

const emit = defineEmits<{
  back: []
  goto: []
  addEpisode: [mode: SourceTypeSmallLabelValue]
  edit: []
  review: []
}>()

/** 移动端下拉菜单命令分发 */
function handleMobileCommand(cmd: string) {
  switch (cmd) {
    case 'review':
      emit('review')
      break
    case 'edit':
      emit('edit')
      break
    case 'goto':
      emit('goto')
      break
    case 'add-rc':
      emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RC])
      break
    case 'add-rtv':
      emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RTV])
      break
    case 'add-rabitter':
      emit('addEpisode', SOURCE_TYPE_SMALL_LABEL[SOURCE_TYPE.RABITTER])
      break
  }
}
</script>
