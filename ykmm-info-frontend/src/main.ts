import { createApp } from 'vue'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import ElementPlus from 'element-plus'
// import 'element-plus/dist/index.css'
import './styles/element-theme.scss'
import 'element-plus/theme-chalk/dark/css-vars.css'
import { zhCn } from 'element-plus/es/locales.mjs'
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'
import { componentPlugin } from '@/components/index.ts' // 引入全局组件插件
import { useTheme } from '@/composables/useTheme'
import { lazyPlugin } from '@/directives/lazy.ts' // 引入懒加载指令插件，并注册
import router from '@/router'
import { useAgencyStore } from '@/stores/agencyStore'
import { useIdolGroupStore } from '@/stores/idolGroupStore'
import App from './App.vue'
import '@/styles/style.css'

const app = createApp(App)
const pinia = createPinia()
const persist = createPersistedState()
pinia.use(persist)
app.use(pinia)

// pinia 装好后就可以调用 store
const agencyStore = useAgencyStore()
const idolGroupStore = useIdolGroupStore()
Promise.all([agencyStore.loadAll(), idolGroupStore.loadAll()]).catch((e) => {
  console.error('初始化基础数据失败', e)
})

app.use(router)
app.use(componentPlugin)
app.use(lazyPlugin)
app.use(ElementPlus, {
  locale: zhCn,
})
useTheme().initTheme()
app.mount('#app')
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
