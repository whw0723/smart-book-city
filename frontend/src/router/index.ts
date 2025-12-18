import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/book/:id',
    name: 'BookDetail',
    component: () => import('../views/BookDetail.vue')
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/Cart.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/Orders.vue'),
    meta: { requiresAuth: true }
  },

  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('../views/UserProfile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/wallet',
    redirect: '/profile',
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/Admin.vue'),
    meta: { requiresAdmin: true }
  },
  {
    path: '/admin/order/:id(\\d+)',
    name: 'AdminOrderDetail',
    component: () => import('../views/AdminOrderDetail.vue'),
    meta: { requiresAdmin: true },
    props: true
  },
  {
    path: '/order/:id(\\d+)',
    name: 'OrderDetail',
    component: () => import('../views/OrderDetail.vue'),
    meta: { requiresAuth: true },
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 导航守卫
router.beforeEach((to, from, next) => {
  // 从sessionStorage获取当前标签页的tabId
  const tabId = sessionStorage.getItem('tabId');
  
  // 使用标签页唯一标识符获取用户信息和用户类型
  const userKey = tabId ? `user_${tabId}` : 'user';
  const userTypeKey = tabId ? `userType_${tabId}` : 'userType';
  
  const userStr = localStorage.getItem(userKey);
  const user = userStr ? JSON.parse(userStr) : null;
  const userType = localStorage.getItem(userTypeKey);

  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin) {
    // 如果需要管理员权限，但用户不是管理员或未登录
    if (!user || userType !== 'admin') {
      ElMessage.error('您没有管理员权限')
      next({ name: 'Login' })
      return
    }
  }
  // 检查是否需要登录权限
  else if (to.meta.requiresAuth && !user) {
    next({ name: 'Login' })
    return
  }

  // 如果是管理员访问用户页面，重定向到管理页面
  if (userType === 'admin' && !to.meta.requiresAdmin && to.name !== 'Login' && to.name !== 'Register') {
    next({ name: 'Admin' })
    return
  }

  next()
})

export default router