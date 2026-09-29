import { createApp } from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { zhCn } from 'element-plus/es/locales.mjs'
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'
// import { componentPlugin } from '@/components/index.ts' // 引入全局组件插件
// import { lazyPlugin } from '@/directives/lazy.ts' // 引入懒加载指令插件，并注册
import router from '@/router'
import App from './App.vue'
import '@/styles/common.scss'

const app = createApp(App)
const pinia = createPinia()
const persist = createPersistedState()
pinia.use(persist)
app.use(pinia)
app.use(router)
// app.use(componentPlugin)
// app.use(lazyPlugin)
app.use(ElementPlus, {
  locale: zhCn,
})
app.mount('#app')
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
