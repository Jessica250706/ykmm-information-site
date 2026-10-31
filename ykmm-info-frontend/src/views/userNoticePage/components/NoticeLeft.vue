<template>
  <el-card
    body-class="flex flex-col h-full overflow-hidden p-0"
    class="flex w-80 flex-col overflow-hidden border-r bg-white"
  >
    <!-- 搜索 -->
    <div class="shrink-0 p-3">
      <el-input v-model="keyword" :prefix-icon="Search" placeholder="搜索公告标题" clearable />
    </div>

    <!-- 分类 + 公告列表 -->
    <el-scrollbar class="min-h-0 flex-1">
      <el-menu
        v-if="displayCategories.length"
        :default-active="selectedNoticeId"
        class="notice-menu"
        @select="handleMenuSelect"
      >
        <el-sub-menu v-for="cat in displayCategories" :key="cat.id" :index="`cat-${cat.id}`">
          <template #title>
            <span>{{ cat.name }}</span>
          </template>

          <el-menu-item v-for="notice in cat.notices" :key="notice.id" :index="String(notice.id)">
            <div class="flex w-full min-w-0 items-center gap-1">
              <el-tag v-if="notice.isTop === 1" effect="plain" size="small" type="danger">
                置顶
              </el-tag>
              <span class="truncate">{{ notice.title }}</span>
            </div>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>

      <el-empty v-else :image-size="80" description="暂无公告" />
    </el-scrollbar>
  </el-card>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { NoticeCategoryVO, NoticeVO } from '@/types/notice'

interface GroupedCategory extends NoticeCategoryVO {
  notices: NoticeVO[]
}

const props = defineProps<{
  categories: NoticeCategoryVO[]
  groupedCategories: GroupedCategory[]
  selectedNoticeId: string
}>()

const emit = defineEmits<{
  'select-notice': [id: number]
}>()

const keyword = ref('')

/**
 * 关键词过滤：
 * - 无关键词：直接用父组件已分组好的分类
 * - 有关键词：按标题模糊匹配过滤，只保留有匹配公告的分类
 * 前端过滤足够，不用打后端接口
 */
const displayCategories = computed<GroupedCategory[]>(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return props.groupedCategories

  return props.groupedCategories
    .map((cat) => ({
      ...cat,
      notices: cat.notices.filter((n) => (n.title ?? '').toLowerCase().includes(kw)),
    }))
    .filter((cat) => cat.notices.length > 0)
})

function handleMenuSelect(index: string) {
  // 只有点击公告项（数字 id）才触发；点击分类 sub-menu 标题不触发 select
  const id = Number(index)
  if (!Number.isFinite(id) || id <= 0) return
  emit('select-notice', id)
}
</script>

<style lang="scss" scoped>
.notice-menu {
  border-right: none;
  background: transparent;

  :deep(.el-menu-item) {
    height: auto;
    min-height: 40px;
    line-height: 1.4;
    padding-top: 8px;
    padding-bottom: 8px;
    font-size: 14px;

    &.is-active {
      font-weight: 500;
    }
  }

  :deep(.el-sub-menu__title) {
    font-weight: 500;
  }
}
</style>
