import type { App } from 'vue'
import ImageUpload from './ImageUpload/Index.vue'
import ImageView from './ImageView/Index.vue'
import { ProTable } from './ProTable/index.ts'
import RichContent from './RichContent/Index.vue'
import RichEditor from './RichEditor/Index.vue'
import SvgIcon from './SvgIcon/Index.vue'
import TableToolbar from './TableToolbar/Index.vue'

export const componentPlugin = {
  install(app: App) {
    // app.component('组件名称', 组件配置对象)
    app.component('ImageView', ImageView)
    app.component('ProTable', ProTable)
    app.component('ImageUpload', ImageUpload)
    app.component('RichContent', RichContent)
    app.component('TableToolbar', TableToolbar)
    app.component('RichEditor', RichEditor)
    app.component('SvgIcon', SvgIcon)
  },
}
