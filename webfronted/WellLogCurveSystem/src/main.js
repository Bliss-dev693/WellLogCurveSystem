import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

// 引入全局样式
import './assets/main.scss'

// 在挂载应用之前，从 localStorage 读取主题并初始化，以减少首次渲染闪烁
try {
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    document.documentElement.classList.add('dark')
  }
} catch (e) {
  // 某些环境下 localStorage 可能不可用，失败时忽略
  console.warn('无法读取 localStorage 主题设置', e)
}

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})



app.mount('#app')