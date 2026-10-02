// 把 components 中的所有组件通过插件的方式进行全局化注册
import type { App } from 'vue'
import ImageUpload from './ImageUpload/Index.vue'
import ImageView from './ImageView/Index.vue'
import { ProTable } from './ProTable/index.ts'

export const componentPlugin = {
  install(app: App) {
    // app.component('组件名称', 组件爱你配置对象)
    app.component('ImageView', ImageView)
    app.component('ProTable', ProTable)
    app.component('ImageUpload', ImageUpload)
  },
}
