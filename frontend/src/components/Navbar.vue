<template>
  <el-header class="navbar">
    <div class="nav-container">
      <div class="nav-left">
        <!-- 左侧空白区域，保持布局平衡 -->
      </div>

      <div class="nav-menu">
        <a @click.prevent="refreshHome" href="/home" class="nav-item" :class="{ 'active': isActive('/home') }">首页</a>
        <router-link v-if="userStore.isLoggedIn" to="/cart" class="nav-item cart-item" :class="{ 'active': isActive('/cart') }">
          <i class="el-icon-shopping-cart-2"></i>
          购物车
          <span v-if="cartStore.totalItems > 0" class="cart-badge">{{ cartStore.totalItems }}</span>
        </router-link>
        <router-link v-if="userStore.isLoggedIn" to="/orders" class="nav-item order-item" :class="{ 'active': isActive('/orders') }">
          我的订单
          <span v-if="ordersStore.pendingOrdersCount > 0" class="order-badge">{{ ordersStore.pendingOrdersCount }}</span>
        </router-link>
        <router-link v-if="userStore.isLoggedIn" to="/profile" class="nav-item" :class="{ 'active': isActive('/profile') }">个人中心</router-link>
      </div>

      <div class="nav-right">
        <template v-if="!userStore.isLoggedIn">
          <router-link to="/login" class="login-btn">登录</router-link>
          <router-link to="/register" class="register-btn">注册</router-link>
        </template>
        <div class="welcome-section" v-if="userStore.isLoggedIn">
          <span class="welcome-text">欢迎，{{ userStore.user?.username }}</span>
          <a @click.prevent="logout" href="#" class="logout-btn">退出登录</a>
        </div>
      </div>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
// 不再需要图标导入
import { useUserStore } from '../store/user'
import { useCartStore } from '../store/cart'
import { useBookStore } from '../store/books'
import { useOrdersStore } from '../store/orders'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()
const bookStore = useBookStore()
const ordersStore = useOrdersStore()
const isRefreshing = ref(false)

// 检查当前路由是否匹配
const isActive = (path: string) => {
  return route.path === path
}

onMounted(async () => {
  userStore.initializeFromLocalStorage()
  cartStore.loadFromLocalStorage()
  // 初始化待支付订单数量
  if (userStore.isLoggedIn) {
    await ordersStore.updatePendingOrdersCount()
  }
})

// 点击首页时触发，强制重新获取图书数据
const refreshHome = async () => {
  console.log('刷新首页被点击')

  // 防止重复点击
  if (isRefreshing.value) return
  isRefreshing.value = true

  try {
    if (router.currentRoute.value.path === '/home') {
      // 如果已经在首页，则刷新数据
      console.log('已在首页，刷新数据')
      await bookStore.fetchAllBooks()
      // 强制刷新页面
      window.location.reload()
    } else {
      // 否则导航到首页
      console.log('导航到首页')
      router.push('/home')
    }
  } catch (error) {
    console.error('刷新首页时出错:', error)
  } finally {
    isRefreshing.value = false
  }
}

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success({
      message: '已退出登录',
      duration: 1000 // 设置为1秒
    })
    router.push('/login')
  }).catch(() => {
    // 用户取消退出登录，不做任何操作
  })
}
</script>

<style scoped>
.navbar {
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 999;
}

.nav-container {
  max-width: 1800px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  height: 60px;
  padding: 0 10px;
}



.welcome-section {
  display: flex;
  align-items: center;
}

.welcome-section .welcome-text {
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
  cursor: default;
  margin-right: 10px;
}

.welcome-section .logout-btn {
  font-size: 14px;
  color: #606266;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.3s;
}

.welcome-section .logout-btn:hover {
  color: #F56C6C;
}

.nav-menu {
  display: flex;
  align-items: center;
  justify-content: center;
  grid-column: 2;
}

.nav-item {
  margin: 0 20px;
  color: #606266;
  text-decoration: none;
  cursor: pointer;
  padding: 0 5px;
  position: relative;
  height: 60px;
  line-height: 60px;
  transition: all 0.3s;
  font-size: 16px;
}

.nav-item:hover {
  color: #409EFF;
}

.nav-item.active {
  color: #409EFF;
  font-weight: bold;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #409EFF;
}

.cart-item, .order-item {
  position: relative;
  display: flex;
  align-items: center;
}

.cart-item i {
  margin-right: 5px;
  font-size: 18px;
}

.cart-badge, .order-badge {
  position: absolute;
  top: 10px;
  right: -8px;
  background-color: #F56C6C;
  color: white;
  border-radius: 50%;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  font-size: 12px;
  padding: 0 4px;
}

.nav-left {
  grid-column: 1;
  justify-self: start;
}

.nav-right {
  display: flex;
  align-items: center;
  grid-column: 3;
  justify-self: end;
}



.login-btn, .register-btn {
  margin-left: 15px;
  text-decoration: none;
}

.login-btn {
  color: #606266;
}

.register-btn {
  color: #409EFF;
}

.el-dropdown-menu a {
  text-decoration: none;
  color: #606266;
  display: block;
}
</style>