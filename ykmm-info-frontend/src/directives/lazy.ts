import type { App, DirectiveBinding } from 'vue'
import { useIntersectionObserver } from '@vueuse/core'

// 定义懒加载插件
export const lazyPlugin = {
  install(app: App) {
    // 定义全局指令
    app.directive('img-lazy', {
      mounted(el: HTMLImageElement, binding: DirectiveBinding<string>) {
        // el：指令绑定的那个元素 img
        // binding.value 指令等于号后面绑定的表达式的值 图片url

        const { stop } = useIntersectionObserver(el, ([entry]) => {
          if (entry?.isIntersecting) {
            // 进入视口区域
            el.src = binding.value
            stop()
          }
        })
      },
    })
  },
}
