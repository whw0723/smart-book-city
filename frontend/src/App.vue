<script setup lang="ts">
import Navbar from './components/Navbar.vue'
import { useRoute } from 'vue-router'
import { useUserStore } from './store/user'
import { onMounted, computed } from 'vue'

const route = useRoute()
const userStore = useUserStore()

onMounted(() => {
  userStore.initializeFromLocalStorage()
})

// 判断当前路由是否是登录或注册页面
const isAuthPage = () => {
  return route.path === '/login' || route.path === '/register'
}

// 判断当前是否是管理员页面
const isAdminPage = computed(() => {
  return route.path.startsWith('/admin')
})

// 判断是否显示普通导航栏
const showUserNavbar = computed(() => {
  return !isAuthPage() && !isAdminPage.value
})
</script>

<template>
  <div class="app">
    <Navbar v-if="showUserNavbar" />
    <div class="main-content">
      <keep-alive include="Home">
        <router-view />
      </keep-alive>
    </div>
    <footer v-if="showUserNavbar" class="footer">
      <div class="footer-content">
        <p>&copy; 2025 智慧书城管理系统 | 基于springboot的智慧书城管理系统的设计与实现</p>
      </div>
    </footer>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background-color: #f5f7fa;
  color: #303133;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.footer {
  background-color: #ffffff;
  box-shadow: 0 -2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 15px 0;
  margin-top: 20px;
}

.footer-content {
  max-width: 1800px;
  margin: 0 auto;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style>
