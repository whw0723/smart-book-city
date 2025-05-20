import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import App from './App.vue'
import router from './router/index.ts'
import './style.css'
import { useUserStore } from './store/user'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

// 在挂载应用前初始化用户状态
const userStore = useUserStore(pinia)
userStore.initializeFromLocalStorage()

app.use(router)
app.use(ElementPlus, {
  locale: zhCn
})

app.mount('#app')
