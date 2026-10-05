<template>
  <div class="story-category-detail flex h-full flex-col">
    <el-card class="flex-1" shadow="never">
      <template #header>
        <DetailHeader
          :detail="detail"
          :is-leaf="isLeafCategory"
          @back="goBack"
          @create-category="goCreateCategory()"
          @create-story="goCreateStory()"
          @edit-current="goEditCurrentCategory"
        />
      </template>

      <el-skeleton v-if="loading" :rows="4" animated />

      <template v-else-if="detail">
        <DetailBasicInfo :detail="detail" class="mb-4 shrink-0" />

        <div class="flex-1 min-h-0">
          <!-- 叶子分类：剧情列表 -->
          <StoryListTable
            v-if="isLeafCategory"
            ref="tableRef"
            :category-id="detail.id!"
            @delete="removeStory"
            @edit="goEditStory"
            @view="openStoryInNewTab"
          />

          <!-- 非叶子分类：子分类表格 -->
          <CategoryChildTable
            v-else
            ref="tableRef"
            :parent-id="detail.id!"
            @create-category="goCreateCategory"
            @create-story="goCreateStory"
            @delete="removeCategory"
            @detail="goCategoryDetail"
            @edit="goEditCategory"
          />
        </div>
      </template>

      <el-empty v-else description="分类不存在" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { getStoryCategoryDetailTreeAPI } from '@/api/storyCategory'
import type { StoryCategoryVO } from '@/types/storyCategory'
import CategoryChildTable from './CategoryChildTable.vue'
import { useDetailActions } from './composables/useDetailActions'
import DetailBasicInfo from './DetailBasicInfo.vue'
import DetailHeader from './DetailHeader.vue'
import StoryListTable from './StoryListTable.vue'

const route = useRoute()

const loading = ref(false)
const detail = ref<StoryCategoryVO | null>(null)

const detailId = computed(() => (route.params.id ? Number(route.params.id) : null))

/** 是否为叶子节点：没有子分类时，展示剧情列表 */
const isLeafCategory = computed(() => (detail.value?.children?.length ?? -1) === 0)

/** 表格 ref，只用到 refresh */
const tableRef = ref<{ refresh: () => Promise<void> | void } | null>(null)

/* -------- 详情加载 -------- */
async function loadDetail() {
  if (!detailId.value) return
  loading.value = true
  try {
    const res = await getStoryCategoryDetailTreeAPI(detailId.value)
    detail.value = res.data ?? null
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

/* -------- 动作 -------- */
const {
  goCreateCategory,
  goCategoryDetail,
  goEditCategory,
  goEditCurrentCategory,
  removeCategory,
  goCreateStory,
  goEditStory,
  openStoryInNewTab,
  removeStory,
  goBack,
} = useDetailActions({
  detailId: () => detailId.value,
  detail: () => detail.value,
  onCategoryDeleted: async () => {
    await loadDetail()
    await tableRef.value?.refresh()
  },
  onStoryDeleted: async () => {
    await tableRef.value?.refresh()
  },
})

/* -------- 路由变化时重新加载 -------- */
watch(
  () => route.params.id,
  async (val) => {
    if (!val) return
    detail.value = null
    await loadDetail()
  },
  { immediate: true },
)
</script>

<style lang="scss" scoped>
.story-category-detail {
  :deep(.el-card) {
    display: flex;
    flex-direction: column;
    min-height: 0;
  }

  :deep(.el-card__body) {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
    overflow: hidden;
    padding: 16px;
  }
}
</style>
