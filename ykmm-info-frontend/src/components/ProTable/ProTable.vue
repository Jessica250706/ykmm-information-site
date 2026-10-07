<template>
  <div class="pro-table flex h-full flex-col">
    <!-- 表格 -->
    <el-table
      ref="tableRef"
      v-bind="$attrs"
      v-loading="loading"
      :data="tableData"
      class="min-h-0 flex-1"
      @selection-change="(val: T[]) => emit('selection-change', val)"
      @sort-change="(val: any) => emit('sort-change', val)"
    >
      <template v-for="col in visibleColumns" :key="colKey(col)">
        <!-- 类型列：多选 / 序号 / 展开 -->
        <el-table-column v-if="col.type" v-bind="columnProps(col)" />

        <!-- 插槽列：slot 名来自列配置，但模板里的 #default / #header 是静态的 -->
        <el-table-column v-else-if="col.slot" v-bind="columnProps(col)">
          <template #default="scope">
            <span
              v-if="col.displayFlex"
              :style="{ justifyContent: alignMap[col.align ?? ''] ?? 'flex-start' }"
              class="flex items-center"
            >
              <slot :name="col.slot" v-bind="scope" />
            </span>
            <slot v-else :name="col.slot" v-bind="scope" />
          </template>
          <template v-if="col.headerSlot" #header="scope">
            <slot :name="col.headerSlot" v-bind="scope" />
          </template>
        </el-table-column>

        <!-- render 列 -->
        <el-table-column v-else-if="col.render" v-bind="columnProps(col)">
          <template #default="scope">
            <RenderCell :render="col.render" :scope="scope" />
          </template>
        </el-table-column>

        <!-- 普通列 -->
        <el-table-column v-else v-bind="columnProps(col)" />
      </template>

      <!-- 空状态 -->
      <template #empty>
        <slot name="empty">
          <el-empty :image-size="80" description="暂无数据" />
        </slot>
      </template>

      <!-- 追加行 -->
      <template v-if="$slots.append" #append>
        <slot name="append" />
      </template>
    </el-table>

    <!-- 分页 -->
    <div v-if="showPagination" class="mt-4 flex shrink-0 justify-end">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :background="true"
        :layout="paginationLayout"
        :page-sizes="pageSizes"
        :total="total"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script
  setup
  lang="ts"
  generic="T extends Record<string, any>, P extends Record<string, any> = Record<string, any>"
>
import { computed, defineComponent, type PropType, ref, watch } from 'vue'
import { useTable } from './useTable'
import type {
  ProTableColumn,
  ProTableExpose,
  ProTableProps,
  ProTableRenderFn,
  ProTableScope,
} from './types'
import type { TableInstance } from 'element-plus'

/* -------- 内部组件：渲染 render 函数 -------- */
const RenderCell = defineComponent({
  name: 'RenderCell',
  props: {
    render: {
      type: Function as PropType<ProTableRenderFn>,
      required: true,
    },
    scope: {
      type: Object as PropType<ProTableScope>,
      required: true,
    },
  },
  setup(props) {
    return () => props.render(props.scope)
  },
})

/* -------- props & emits -------- */
defineOptions({
  name: 'ProTable',
  inheritAttrs: false,
})

const props = withDefaults(defineProps<ProTableProps<T, P>>(), {
  columns: () => [],
  pagination: true,
  pageSizes: () => [10, 20, 50, 100],
  defaultPageSize: 20,
  defaultPageNum: 1,
  paginationLayout: 'total, sizes, prev, pager, next, jumper',
  immediate: true,
  displayFlex: false,
})

const emit = defineEmits<{
  (e: 'selection-change', val: T[]): void
  (e: 'sort-change', val: { prop: string; order: string | null }): void
}>()

/* -------- 数据逻辑 -------- */
const { loading, tableData, total, currentPage, pageSize, refresh, search, reset, setData } =
  useTable<T, P>({
    request: props.request,
    data: () => props.data,
    defaultPageSize: props.defaultPageSize,
    defaultPageNum: props.defaultPageNum,
  })

const tableRef = ref<TableInstance>()

/* -------- 分页显示 -------- */
const showPagination = computed(() => props.pagination !== false)

/* -------- 可见列 -------- */
const visibleColumns = computed(() => props.columns.filter((c) => !c.hidden))

/* -------- 列属性 -------- */
function columnProps(col: ProTableColumn<T>) {
  const { slot, headerSlot, render, headerRender, hidden, children, ...rest } = col
  return rest
}

function colKey(col: ProTableColumn<T>) {
  return col.prop || col.label || col.type || JSON.stringify(col)
}

const alignMap: Record<string, string> = {
  left: 'flex-start',
  center: 'center',
  right: 'flex-end',
}

/* -------- 分页事件 -------- */
function handleCurrentChange() {
  void refresh()
}

function handleSizeChange() {
  currentPage.value = 1
  void refresh()
}

/* -------- 请求参数变化时重新搜索 -------- */
watch(
  () => props.params,
  (val) => {
    void search(val)
  },
  { deep: true },
)

/* -------- 挂载后立即请求 -------- */
if (props.immediate && props.request) {
  void refresh()
}

/* -------- 对外暴露 -------- */
defineExpose<ProTableExpose<T>>({
  tableRef,
  refresh,
  search,
  reset,
  setData,
  loading,
  tableData,
  total,
})
</script>
