<template>
  <div class="order-detail-container">
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    <div v-else-if="!order" class="not-found">
      <h2>订单不存在</h2>
      <el-button type="primary" @click="goBack">返回</el-button>
    </div>
    <div v-else class="order-detail">
      <div class="page-header">
        <h2>订单详情</h2>
        <el-button type="primary" @click="goBack">返回</el-button>
      </div>

      <div class="order-info">
        <div class="order-header">
          <div class="order-number">
            <span class="label">订单号:</span>
            <span class="value">{{ order.orderNumber }}</span>
          </div>
          <div class="order-date">
            <span class="label">下单时间:</span>
            <span class="value">{{ formatDate(order.orderDate) }}</span>
          </div>
          <div class="order-status">
            <span class="label">订单状态:</span>
            <span class="value">
              <el-tag :type="getStatusType(order.status)">
                {{ getStatusText(order.status) }}
              </el-tag>
            </span>
          </div>
        </div>

        <div class="user-info" v-if="order.user">
          <h3>用户信息</h3>
          <div class="info-item">
            <span class="label">用户名:</span>
            <span class="value">{{ order.user.username }}</span>
          </div>
          <div class="info-item" v-if="order.user.email">
            <span class="label">邮箱:</span>
            <span class="value">{{ order.user.email }}</span>
          </div>
        </div>

        <div class="items-section">
          <table class="items-table">
            <thead>
              <tr>
                <th>书籍名称</th>
                <th>作者</th>
                <th>单价</th>
                <th>数量</th>
                <th>总计</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in orderItems" :key="item.id">
                <td>{{ item.bookTitle || item.book?.title }}</td>
                <td>{{ item.bookAuthor || item.book?.author }}</td>
                <td>¥{{ ((item.price || item.book?.price) || 0).toFixed(2) }}</td>
                <td>{{ item.quantity }}</td>
                <td>¥{{ (((item.price || item.book?.price) || 0) * item.quantity).toFixed(2) }}</td>
              </tr>
            </tbody>
          </table>
        </div>


      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useUserStore } from '../store/user'

interface OrderItem {
  id: number;
  bookId?: number;
  bookTitle?: string;
  bookAuthor?: string;
  price?: number;
  quantity: number;
  book?: {
    id: number;
    title: string;
    author: string;
    price: number;
  };
}

interface Order {
  id: number;
  orderNumber: string;
  orderDate: string;
  status: number;
  totalAmount: number;
  user?: {
    id: number;
    username: string;
    email?: string;
  };
  orderItems?: OrderItem[];
  items?: OrderItem[];
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const order = ref<Order | null>(null)
const loading = ref(true)

// 检查是否是管理员
const isAdmin = computed(() => userStore.isAdmin)

// 获取订单项
const orderItems = computed(() => {
  if (!order.value) return []
  return order.value.orderItems || order.value.items || []
})

onMounted(async () => {
  console.log('OrderDetail组件已挂载')
  console.log('路由参数:', route.params)

  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  const orderId = route.params.id
  if (!orderId) {
    ElMessage.error('订单ID不存在')
    router.push(isAdmin.value ? '/admin' : '/orders')
    return
  }

  console.log('正在获取订单详情，ID:', orderId)

  try {
    loading.value = true
    const response = await axios.get(`http://localhost:8080/api/orders/${orderId}`)
    console.log('订单详情API响应:', response.data)

    if (response.data) {
      order.value = response.data
    } else {
      ElMessage.error('订单不存在')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
})

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取状态文本
const getStatusText = (status: number): string => {
  const statusMap: { [key: number]: string } = {
    0: '待付款',
    1: '已完成'
    // 已取消的订单直接删除，不再显示
  }
  return statusMap[status] || `未知状态(${status})`
}

// 获取状态类型（用于 el-tag 的类型）
const getStatusType = (status: number): string => {
  const statusTypeMap: { [key: number]: string } = {
    0: 'warning',   // 待付款
    1: 'success'    // 已完成
    // 已取消的订单直接删除，不再显示
  }
  return statusTypeMap[status] || ''
}

// 返回上一页
const goBack = () => {
  router.back()
}


</script>

<style scoped>
.order-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading, .not-found {
  text-align: center;
  padding: 40px 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.order-info {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.order-number, .order-date, .order-status {
  display: flex;
  flex-direction: column;
}

.user-info {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.info-item {
  margin-bottom: 10px;
}

.label {
  font-weight: bold;
  color: #606266;
  margin-right: 10px;
}

.value {
  color: #303133;
}

.items-section h3 {
  margin-bottom: 15px;
}

.items-table {
  width: 100%;
  border-collapse: collapse;
}

.items-table th, .items-table td {
  padding: 12px 8px;
  text-align: center;
  border-bottom: 1px solid #ebeef5;
}

.items-table th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: bold;
}

.items-table tbody tr:hover {
  background-color: #f5f7fa;
}

.total-label {
  text-align: right;
  font-weight: bold;
}

.total-value {
  font-weight: bold;
  color: #f56c6c;
}

.order-actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
