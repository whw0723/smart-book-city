<template>
  <div class="orders-container">
    <div class="orders-layout">
      <!-- 左侧导航栏 -->
      <div class="sidebar">
        <div
          class="sidebar-item"
          :class="{ active: activeOrderType === 'pending' }"
          @click="activeOrderType = 'pending'"
        >
          <span class="sidebar-icon">
            📋
          </span>
          <span class="sidebar-text">待付款订单</span>
        </div>
        <div
          class="sidebar-item"
          :class="{ active: activeOrderType === 'completed' }"
          @click="activeOrderType = 'completed'"
        >
          <span class="sidebar-icon">
            ✅
          </span>
          <span class="sidebar-text">已完成订单</span>
        </div>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <div class="filter-bar">
          <div class="search-container">
            <el-input v-model="bookTitleQuery" placeholder="书名" clearable style="width: 120px;" />
            <el-input v-model="authorQuery" placeholder="作者" clearable style="width: 120px;" />
            <el-date-picker
              v-model="dateQuery"
              type="date"
              placeholder="下单日期"
              value-format="YYYY-MM-DD"
              style="width: 120px;"
            />
            <button @click="applySearch" class="search-btn">搜索</button>
            <button @click="resetSearch" class="reset-btn">重置</button>
          </div>
        </div>

        <div v-if="loading" class="loading">
          <p>加载中...</p>
        </div>

        <div v-else-if="(activeOrderType === 'pending' && pendingOrders.length === 0) ||
                     (activeOrderType === 'completed' && completedOrders.length === 0)" class="no-orders">
          <el-empty :description="getEmptyDescription()">
            <el-button type="primary" @click="goToHome" class="pure-btn">去首页看看</el-button>
          </el-empty>
        </div>

        <div v-else class="orders-list">
          <!-- 待付款订单 -->
          <div v-if="activeOrderType === 'pending'">
            <!-- 待付款订单控制区 -->
            <div class="section-controls">
              <div class="select-all-container">
                <div class="left-section">
                  <div class="select-all-checkbox">
                    <input
                      type="checkbox"
                      id="select-all"
                      :checked="isAllSelected"
                      @change="toggleSelectAll"
                    />
                    <label for="select-all">全选</label>
                  </div>
                  <div class="selected-count" v-if="selectedOrderIds.length > 0">
                    已选择 {{ selectedOrderIds.length }} 个订单
                  </div>
                </div>

                <div class="right-section" v-if="selectedOrderIds.length > 0">
                  <!-- 无论选中几个订单，都显示订单统计信息和批量操作按钮 -->
                  <div class="order-summary-inline">
                    <span class="total-items-count">共 {{ getSelectedItemsCount() }} 件商品</span>
                    <span class="total-amount">总计: ¥{{ getSelectedOrdersAmount().toFixed(2) }}</span>
                  </div>

                  <div class="batch-buttons">
                    <button @click="batchCancelOrders" class="batch-delete-btn">
                      批量取消
                    </button>
                    <button @click="batchPayOrders" class="batch-pay-btn">
                      批量支付
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 待付款订单列表 -->
            <div class="orders-section">
              <div class="orders-grid">
                <div v-for="order in pendingOrders" :key="order.id" class="order-card">
                  <div class="order-header">
                    <div class="order-checkbox">
                      <input
                        type="checkbox"
                        :checked="selectedOrderIds.includes(order.id)"
                        @change="toggleOrderSelection(order.id)"
                      />
                    </div>
                    <div class="order-info">
                      <p class="order-number">订单号: {{ order.orderNumber }}</p>
                      <p class="order-date">下单时间: {{ formatDate(order.orderDate) }}</p>
                    </div>
                    <div class="order-countdown" v-if="order.status === 0">
                      <span class="countdown-text">剩余支付时间: </span>
                      <span class="countdown-timer">{{ order.countdownText || '30:00' }}</span>
                    </div>
                  </div>

                  <div class="order-items">
                    <div v-for="item in order.orderItems || order.items" :key="item.id" class="order-item">
                      <div class="item-details">
                        <h3>{{ getItemTitle(item) }}</h3>
                        <p class="author">{{ getItemAuthor(item) }}</p>
                        <div class="quantity-price">
                          <span>数量: {{ item.quantity }}</span>
                          <span>单价: ¥{{ getItemPrice(item).toFixed(2) }}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="order-footer">
                    <div class="order-total">
                      总计: ¥{{ order.totalAmount.toFixed(2) }}
                    </div>
                    <div class="order-actions">
                      <button @click="cancelOrder(order.id)" class="view-btn">
                        取消订单
                      </button>
                      <button @click="payOrder(order.id)" class="pay-btn">
                        去支付
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 分页组件 -->
              <div class="pagination-container">
                <el-pagination
                  v-if="pendingTotal > 0"
                  background
                  layout="prev, pager, next, jumper"
                  :total="pendingTotal"
                  :page-size="pageSize"
                  :current-page="pendingCurrentPage"
                  @current-change="handlePendingPageChange"
                />
              </div>
            </div>
          </div>



          <!-- 已完成订单 -->
          <div v-if="activeOrderType === 'completed'">
            <!-- 已完成订单控制区 -->

            <!-- 已完成订单列表 -->
            <div class="orders-section">
              <div class="orders-grid">
                <div v-for="order in completedOrders" :key="order.id" class="order-card">
                  <div class="order-header">
                    <div class="order-info">
                      <p class="order-number">订单号: {{ order.orderNumber }}</p>
                      <p class="order-date">下单时间: {{ formatDate(order.orderDate) }}</p>
                    </div>
                  </div>

                  <div class="order-items">
                    <div v-for="item in order.orderItems || order.items" :key="item.id" class="order-item">
                      <div class="item-details">
                        <h3>{{ getItemTitle(item) }}</h3>
                        <p class="author">{{ getItemAuthor(item) }}</p>
                        <div class="quantity-price">
                          <span>数量: {{ item.quantity }}</span>
                          <span>单价: ¥{{ getItemPrice(item).toFixed(2) }}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="order-footer">
                    <div class="order-total">
                      总计: ¥{{ order.totalAmount.toFixed(2) }}
                    </div>
                    <div class="order-actions">
                      <button @click="viewOrderDetail(order.id)" class="view-btn">
                        查看详情
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 分页组件 -->
              <div class="pagination-container">
                <el-pagination
                  v-if="completedTotal > 0"
                  background
                  layout="prev, pager, next, jumper"
                  :total="completedTotal"
                  :page-size="pageSize"
                  :current-page="completedCurrentPage"
                  @current-change="handleCompletedPageChange"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 订单详情弹窗 -->
  <el-dialog
    v-model="dialogVisible"
    title="订单详情"
    width="600px"
    center
    :close-on-click-modal="false"
  >
    <div v-if="loadingDetail" class="loading-detail">
      <p>加载中...</p>
    </div>
    <div v-else-if="orderDetail" class="order-detail-content">
      <div class="detail-header">
        <div class="detail-row">
          <span class="detail-label">订单号:</span>
          <span class="detail-value">{{ orderDetail.orderNumber }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">下单时间:</span>
          <span class="detail-value">{{ formatDate(orderDetail.orderDate) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">订单状态:</span>
          <span class="detail-value status-{{ orderDetail.status }}">{{ getStatusText(orderDetail.status) }}</span>
        </div>
      </div>

      <div class="detail-body">
        <table class="order-items-table">
          <thead>
            <tr>
              <th>书名</th>
              <th>作者</th>
              <th>单价</th>
              <th>数量</th>
              <th>小计</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in orderDetail.orderItems || orderDetail.items" :key="item.id">
              <td>{{ getItemTitle(item) }}</td>
              <td>{{ getItemAuthor(item) }}</td>
              <td>¥{{ getItemPrice(item).toFixed(2) }}</td>
              <td>{{ item.quantity }}</td>
              <td>¥{{ (getItemPrice(item) * item.quantity).toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="detail-footer">
        <div class="total-amount">
          总计: <span class="amount-value">¥{{ orderDetail.totalAmount.toFixed(2) }}</span>
        </div>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElDatePicker, ElInput } from 'element-plus'
import { useUserStore } from '../store/user'
import { useWalletStore } from '../store/wallet'
import { useOrdersStore } from '../store/orders'
import axios from 'axios'

// 定义 Order 和 OrderItem 接口
interface OrderItem {
  id: number;
  bookId: number;
  bookTitle: string;
  bookAuthor: string;

  price: number;
  quantity: number;

  // 兼容旧格式
  book?: {
    id: number;
    title: string;
    author: string;
    price: number;

  };
}

interface Order {
  id: number;
  orderNumber?: string;
  orderDate: string; // 或者 Date 类型，取决于 API 返回
  status: string | number;
  totalAmount: number;
  items?: OrderItem[];
  orderItems?: OrderItem[];
  user?: any;
  remainingTime?: number; // 剩余支付时间（秒）
  countdownText?: string; // 格式化的倒计时文本
}

const router = useRouter()
const userStore = useUserStore()

const walletStore = useWalletStore()
const ordersStore = useOrdersStore()

const orders = ref<Order[]>([]) // 显式指定类型
const loading = ref(true)
// 搜索条件
const bookTitleQuery = ref('')
const authorQuery = ref('')
const dateQuery = ref('')

// 当前选中的订单类型：'pending'表示待付款订单，'completed'表示已完成订单
const activeOrderType = ref('pending')
// 选中的订单ID列表
const selectedOrderIds = ref<number[]>([]) // 选中的订单ID列表
const isAllSelected = ref(false) // 是否全选

// 分页相关
const pageSize = ref(9) // 每页显示9个订单
const pendingCurrentPage = ref(1)
const completedCurrentPage = ref(1)
const pendingTotal = ref(0)
const completedTotal = ref(0)

// 倒计时相关
const countdownTimer = ref<number | null>(null) // 倒计时定时器
const PAYMENT_TIMEOUT = 5 * 60 // 支付超时时间（5分钟，单位：秒）

// 订单详情弹窗相关
const dialogVisible = ref(false)
const loadingDetail = ref(false)
const orderDetail = ref<Order | null>(null)
const currentOrderId = ref<number>(0)

// 从购物车创建新订单


// 不再自动将购物车商品转换为订单
// 用户需要在购物车页面主动结算才会创建订单

// 监听订单类型变化
watch(() => activeOrderType.value, (newType) => {
  // 切换类型时先设置加载状态，防止闪烁
  loading.value = true

  // 切换类型时重置页码
  if (newType === 'pending') {
    pendingCurrentPage.value = 1
    loadPendingOrders().then(() => {
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  } else if (newType === 'completed') {
    completedCurrentPage.value = 1
    loadCompletedOrders().then(() => {
      loading.value = false
    }).catch(() => {
      loading.value = false
    })
  }

  // 清空选中的订单
  selectedOrderIds.value = []
  isAllSelected.value = false
})

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    console.error('请先登录')
    loading.value = false
    ElMessage.warning('请先登录后查看订单')
    router.push('/login')
    return
  }

  try {
    loading.value = true

    // 先检查并取消过期订单
    await axios.post('http://localhost:8080/api/orders/check-overdue')
    
    // 加载待付款订单（第一页）
    await loadPendingOrders()

    // 加载已完成订单（第一页）
    await loadCompletedOrders()

    loading.value = false
    
    // 更新待支付订单数量
    await ordersStore.updatePendingOrdersCount()
    
    // 启动倒计时定时器
    startCountdownTimer()

    // 不再自动将购物车商品转换为订单
    // 用户需要在购物车页面主动结算才会创建订单
  } catch (err: any) {
    console.error('获取订单失败:', err)
    // 错误处理
    loading.value = false
    orders.value = [] // 清空订单列表
    ElMessage.error(`获取订单失败: ${err.message || err}`)
  }
})

// 在组件销毁时停止定时器
onUnmounted(() => {
  stopCountdownTimer()
})

// 加载待付款订单
const loadPendingOrders = async () => {
  try {
    // 先检查并取消过期订单
    await axios.post('http://localhost:8080/api/orders/check-overdue')
    
    // 获取所有订单，以便正确计算分页
    const response = await axios.get(`http://localhost:8080/api/orders/user/${userStore.user?.id}`)

    if (response.data && Array.isArray(response.data)) {
      // 先过滤出有效订单（只保留待支付和已完成的订单）
      const validOrders = response.data.filter((order: Order) => isValidOrder(order.status))

      // 再过滤出待付款订单
      const allPendingOrders = validOrders.filter((order: Order) => order.status === 0)

      // 计算总数
      pendingTotal.value = allPendingOrders.length
      
      // 更新待支付订单数量
      ordersStore.pendingOrdersCount = allPendingOrders.length

      // 确保当前页码有效
      const maxPage = Math.ceil(allPendingOrders.length / pageSize.value) || 1
      if (pendingCurrentPage.value > maxPage) {
        pendingCurrentPage.value = maxPage
      }

      // 手动分页
      const start = (pendingCurrentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      const pendingOrdersData = allPendingOrders.slice(start, end)

      // 更新订单列表
      if (activeOrderType.value === 'pending') {
        // 如果当前是待付款订单页面，只显示待付款订单
        orders.value = pendingOrdersData
      }

      console.log('当前页的待付款订单数据:', pendingOrdersData)
    } else if (response.data && response.data.orders) {
      // 兼容分页API返回格式
      const pendingOrdersData = response.data.orders.filter((order: Order) => order.status === 0)

      // 更新订单列表
      if (activeOrderType.value === 'pending') {
        orders.value = pendingOrdersData
      }

      // 更新总数
      pendingTotal.value = response.data.total
      // 更新待支付订单数量
      ordersStore.pendingOrdersCount = pendingTotal.value
      console.log('从分页API获取到待付款订单数据:', pendingOrdersData)
    } else {
      console.warn('后端返回的订单数据格式不正确')
      if (activeOrderType.value === 'pending') {
        orders.value = [] // 清空订单列表
      }
      // 更新待支付订单数量
      ordersStore.pendingOrdersCount = 0
    }
  } catch (error) {
    console.error('加载待付款订单失败:', error)
  }
}

// 加载已完成订单
const loadCompletedOrders = async () => {
  try {
    console.log('开始加载已完成订单，用户ID:', userStore.user?.id)

    // 获取所有订单，不分页，以确保能获取到所有已完成订单
    const response = await axios.get(`http://localhost:8080/api/orders/user/${userStore.user?.id}`)
    console.log('获取到的所有订单数据:', response.data)

    if (response.data && Array.isArray(response.data)) {
      // 先过滤出有效订单（只保留待支付和已完成的订单）
      const validOrders = response.data.filter((order: Order) => isValidOrder(order.status))

      // 再过滤出已完成订单
      const allCompletedOrders = validOrders.filter((order: Order) => isCompletedOrder(order.status))
      console.log('过滤后的已完成订单:', allCompletedOrders)

      // 计算总数
      completedTotal.value = allCompletedOrders.length

      // 确保当前页码有效
      const maxPage = Math.ceil(allCompletedOrders.length / pageSize.value) || 1
      if (completedCurrentPage.value > maxPage) {
        completedCurrentPage.value = maxPage
      }

      // 手动分页
      const start = (completedCurrentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      const completedOrdersData = allCompletedOrders.slice(start, end)

      // 更新订单列表
      if (activeOrderType.value === 'completed') {
        // 如果当前是已完成订单页面，只显示已完成订单
        orders.value = completedOrdersData
      }

      console.log('当前页的已完成订单数据:', completedOrdersData)
    } else if (response.data && response.data.orders) {
      // 兼容分页API返回格式
      const completedOrdersData = response.data.orders.filter((order: Order) => isCompletedOrder(order.status))

      // 更新订单列表
      if (activeOrderType.value === 'completed') {
        orders.value = completedOrdersData
      }

      // 更新总数
      completedTotal.value = response.data.total
      console.log('从分页API获取到已完成订单数据:', completedOrdersData)
    } else {
      console.warn('后端返回的订单数据格式不正确')
    }
  } catch (error) {
    console.error('加载已完成订单失败:', error)
  }
}

// 筛选订单的通用函数
const filterOrdersByQueries = (orders: Order[]) => {
  return orders.filter(order => {
    // 如果没有任何搜索条件，返回所有订单
    if (!bookTitleQuery.value && !authorQuery.value && !dateQuery.value) {
      return true
    }

    // 检查日期
    if (dateQuery.value) {
      try {
        const orderDate = new Date(order.orderDate).toISOString().split('T')[0]
        if (orderDate !== dateQuery.value) {
          return false
        }
      } catch (error) {
        console.error('日期格式转换错误:', error)
        // 如果日期转换出错，不进行日期筛选
      }
    }

    // 检查图书标题和作者
    const items = order.orderItems || order.items || []

    // 如果有书名查询条件
    if (bookTitleQuery.value) {
      const lowercaseBookTitle = bookTitleQuery.value.toLowerCase()
      const hasMatchingTitle = items.some(item => {
        const title = getItemTitle(item).toLowerCase()
        return title.includes(lowercaseBookTitle)
      })

      if (!hasMatchingTitle) {
        return false
      }
    }

    // 如果有作者查询条件
    if (authorQuery.value) {
      const lowercaseAuthor = authorQuery.value.toLowerCase()
      const hasMatchingAuthor = items.some(item => {
        const author = getItemAuthor(item).toLowerCase()
        return author.includes(lowercaseAuthor)
      })

      if (!hasMatchingAuthor) {
        return false
      }
    }

    return true
  })
}

// 获取待付款订单
const pendingOrders = computed(() => {
  const pending = orders.value.filter(order => order.status === 0)
  return filterOrdersByQueries(pending)
})

// 获取已完成订单
const completedOrders = computed(() => {
  const completed = orders.value.filter(order => isCompletedOrder(order.status))
  return filterOrdersByQueries(completed)
})



// 计算订单剩余支付时间（秒）
const calculateRemainingTime = (orderDate: string): number => {
  const orderTime = new Date(orderDate).getTime()
  const nowTime = new Date().getTime()
  const elapsedSeconds = Math.floor((nowTime - orderTime) / 1000)
  const remainingSeconds = PAYMENT_TIMEOUT - elapsedSeconds
  return Math.max(remainingSeconds, 0) // 确保不返回负数
}

// 格式化倒计时文本
const formatCountdown = (seconds: number): string => {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
}

// 更新所有待付款订单的倒计时
const updateCountdowns = () => {
  orders.value.forEach(order => {
    if (order.status === 0) {
      const remainingTime = calculateRemainingTime(order.orderDate)
      order.remainingTime = remainingTime
      order.countdownText = formatCountdown(remainingTime)
      
      // 如果倒计时结束，刷新订单列表
      if (remainingTime <= 0) {
        // 提示用户订单已过期
        ElMessage.warning({
          message: `订单 ${order.orderNumber} 已超过5分钟未支付，已自动取消`,
          duration: 1000
        })
        // 刷新订单列表
        if (activeOrderType.value === 'pending') {
          loadPendingOrders()
        } else {
          loadCompletedOrders()
        }
      }
    }
  })
}

// 启动倒计时定时器
const startCountdownTimer = () => {
  // 清除之前的定时器
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
  }
  
  // 更新一次倒计时
  updateCountdowns()
  
  // 每秒更新一次倒计时
  countdownTimer.value = window.setInterval(() => {
    updateCountdowns()
  }, 1000)
}

// 停止倒计时定时器
const stopCountdownTimer = () => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value)
    countdownTimer.value = null
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 判断订单是否已完成
const isCompletedOrder = (status: any): boolean => {
  // 如果是数字状态码
  if (typeof status === 'number') {
    // 只有状态码1表示已完成
    return status === 1
  }

  // 如果是字符串状态
  return status === 'completed'
}

// 判断订单是否有效（只有待支付和已完成的订单是有效的）
const isValidOrder = (status: any): boolean => {
  if (typeof status === 'number') {
    // 只有状态码0（待支付）和1（已完成）是有效的
    return status === 0 || status === 1
  }

  // 如果是字符串状态
  return status === 'pending' || status === 'completed'
}

// 辅助方法，处理新的数据格式
const getItemTitle = (item: OrderItem): string => {
  return item.bookTitle || (item.book?.title || '')
}

const getItemAuthor = (item: OrderItem): string => {
  return item.bookAuthor || (item.book?.author || '')
}

// 移除了获取图书封面的函数

const getItemPrice = (item: OrderItem): number => {
  return item.price || (item.book?.price || 0)
}

const getTotalItems = (order: Order): number => {
  const items = order.orderItems || order.items || []
  return items.reduce((sum, item) => sum + item.quantity, 0)
}

// 处理中订单相关功能已移除

// 跳转到首页
const goToHome = () => {
  router.push('/home')
}

// 获取空订单提示文本
const getEmptyDescription = () => {
  if (activeOrderType.value === 'pending') {
    return '暂无待付款订单'
  } else {
    return '暂无已完成订单'
  }
}

const payOrder = (orderId: number) => {
  // 获取订单信息
  const order = orders.value.find(o => o.id === orderId)
  if (!order) {
    ElMessage.error('订单不存在')
    return
  }

  // 确认支付
  ElMessageBox.confirm(
    `确定要使用账户余额支付此订单吗？订单金额: ¥${order.totalAmount.toFixed(2)}`,
    '订单支付',
    {
      confirmButtonText: '去支付',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 检查钱包余额
      await walletStore.fetchWallet()

      if (walletStore.balance < order.totalAmount) {
        ElMessageBox.alert(
          `账户余额不足，当前余额: ¥${walletStore.balance.toFixed(2)}，需要: ¥${order.totalAmount.toFixed(2)}`,
          '余额不足',
          {
            confirmButtonText: '去充值',
            callback: () => {
              router.push('/wallet')
            }
          }
        )
        return
      }

      // 调用钱包支付API
      await walletStore.payOrder(orderId)
      ElMessage.success({
        message: '支付成功',
        duration: 1000
      })

      // 刷新订单列表
      const response = await axios.get(`http://localhost:8080/api/orders/user/${userStore.user?.id}`)
      if (response.data && Array.isArray(response.data)) {
        orders.value = response.data
        
        // 立即更新待支付订单数量
        const pendingOrders = response.data.filter((order: Order) => order.status === 0)
        ordersStore.pendingOrdersCount = pendingOrders.length
      }
    } catch (error: any) {
      console.error('支付失败:', error)
      ElMessage.error(error.response?.data || '支付失败')
    }
  }).catch(() => {})
}

// 取消订单
const cancelOrder = (orderId: number) => {
  // 获取订单信息
  const order = orders.value.find(o => o.id === orderId)
  if (!order) {
    ElMessage.error('订单不存在')
    return
  }

  // 确认取消
  ElMessageBox.confirm(
    `确定要取消此订单吗？订单号: ${order.orderNumber}`,
    '取消订单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 调用后端API取消订单（直接删除）
      await axios.put(`http://localhost:8080/api/orders/${orderId}/cancel`)
      ElMessage.success({
        message: '订单已取消',
        duration: 1000
      })

      // 刷新订单列表
      const response = await axios.get(`http://localhost:8080/api/orders/user/${userStore.user?.id}`)
      if (response.data && Array.isArray(response.data)) {
        orders.value = response.data
        
        // 立即更新待支付订单数量
        const pendingOrders = response.data.filter((order: Order) => order.status === 0)
        ordersStore.pendingOrdersCount = pendingOrders.length
      }
    } catch (error: any) {
      console.error('取消订单失败:', error)
      ElMessage.error(error.response?.data || '取消订单失败')
    }
  }).catch(() => {})
}

// 查看订单详情
const viewOrderDetail = async (orderId: number) => {
  currentOrderId.value = orderId
  dialogVisible.value = true
  loadingDetail.value = true
  orderDetail.value = null

  try {
    // 从现有订单列表中查找订单
    const existingOrder = [...pendingOrders.value, ...completedOrders.value].find(order => order.id === orderId)
    if (existingOrder) {
      orderDetail.value = existingOrder
    } else {
      // 如果找不到，从API获取
      const response = await axios.get(`http://localhost:8080/api/orders/${orderId}`)
      if (response.data) {
        orderDetail.value = response.data as Order
      }
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loadingDetail.value = false
  }
}

// 获取订单状态文本
const getStatusText = (status: string | number): string => {
  if (typeof status === 'number') {
    switch (status) {
      case 0:
        return '待付款'
      case 1:
        return '已完成'
      default:
        return '未知状态'
    }
  }

  switch (status.toLowerCase()) {
    case 'pending':
      return '待付款'
    case 'completed':
      return '已完成'
    default:
      return '未知状态'
  }
}

// 切换单个订单的选择状态
const toggleOrderSelection = (orderId: number) => {
  const index = selectedOrderIds.value.indexOf(orderId)
  if (index === -1) {
    // 如果不在选中列表中，添加
    selectedOrderIds.value.push(orderId)
  } else {
    // 如果已在选中列表中，移除
    selectedOrderIds.value.splice(index, 1)
  }

  // 更新全选状态
  updateSelectAllState()
}

// 更新全选状态
const updateSelectAllState = () => {
  isAllSelected.value = pendingOrders.value.length > 0 &&
    selectedOrderIds.value.length === pendingOrders.value.length
}

// 切换全选/取消全选
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    // 如果当前是全选状态，则取消全选
    selectedOrderIds.value = []
  } else {
    // 如果当前不是全选状态，则全选
    selectedOrderIds.value = pendingOrders.value.map(order => order.id)
  }
  isAllSelected.value = !isAllSelected.value
}

// 批量取消订单
const batchCancelOrders = () => {
  if (selectedOrderIds.value.length === 0) {
    ElMessage.warning('请先选择要取消的订单')
    return
  }

  ElMessageBox.confirm(
    `确定要取消选中的 ${selectedOrderIds.value.length} 个订单吗？`,
    '批量取消订单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 批量取消订单（直接删除）
      await axios.put(`http://localhost:8080/api/orders/batch/cancel`, selectedOrderIds.value)
      ElMessage.success({
        message: `已成功取消 ${selectedOrderIds.value.length} 个订单`,
        duration: 1000
      })

      // 清空选择
      selectedOrderIds.value = []

      // 刷新订单列表
      const response = await axios.get(`http://localhost:8080/api/orders/user/${userStore.user?.id}`)
      if (response.data && Array.isArray(response.data)) {
        orders.value = response.data
        
        // 立即更新待支付订单数量
        const pendingOrders = response.data.filter((order: Order) => order.status === 0)
        ordersStore.pendingOrdersCount = pendingOrders.length
      }
    } catch (error: any) {
      console.error('批量取消订单失败:', error)
      ElMessage.error(error.response?.data || '批量取消订单失败')
    }
  }).catch(() => {})
}

// 批量支付订单
const batchPayOrders = async () => {
  if (selectedOrderIds.value.length === 0) {
    ElMessage.warning('请先选择要支付的订单')
    return
  }

  // 获取选中订单的总金额
  const totalAmount = getSelectedOrdersAmount()

  ElMessageBox.confirm(
    `确定要支付选中的 ${selectedOrderIds.value.length} 个订单吗？总金额: ¥${totalAmount.toFixed(2)}`,
    '批量支付订单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 检查钱包余额
      await walletStore.fetchWallet()

      if (walletStore.balance < totalAmount) {
        ElMessageBox.alert(
          `账户余额不足，当前余额: ¥${walletStore.balance.toFixed(2)}，需要: ¥${totalAmount.toFixed(2)}`,
          '余额不足',
          {
            confirmButtonText: '去充值',
            callback: () => {
              router.push('/wallet')
            }
          }
        )
        return
      }

      // 调用批量支付API
      await walletStore.batchPayOrders(selectedOrderIds.value)
      ElMessage.success({
        message: '批量支付成功',
        duration: 1000
      })

      // 清空选择
      selectedOrderIds.value = []

      // 刷新订单列表
      const response = await axios.get(`http://localhost:8080/api/orders/user/${userStore.user?.id}`)
      if (response.data && Array.isArray(response.data)) {
        orders.value = response.data
        
        // 立即更新待支付订单数量
        const pendingOrders = response.data.filter((order: Order) => order.status === 0)
        ordersStore.pendingOrdersCount = pendingOrders.length
      }
    } catch (error: any) {
      console.error('批量支付失败:', error)
      ElMessage.error(error.response?.data || '批量支付失败')
    }
  }).catch(() => {})
}

// 获取选中订单的商品总数
const getSelectedItemsCount = () => {
  const selectedOrders = pendingOrders.value.filter(order =>
    selectedOrderIds.value.includes(order.id)
  )

  return selectedOrders.reduce((total, order) => {
    return total + getTotalItems(order)
  }, 0)
}

// 获取选中订单的总金额
const getSelectedOrdersAmount = () => {
  const selectedOrders = pendingOrders.value.filter(order =>
    selectedOrderIds.value.includes(order.id)
  )

  return selectedOrders.reduce((total, order) => {
    return total + order.totalAmount
  }, 0)
}

// 监听订单列表变化，更新选择状态
watch(() => pendingOrders.value, () => {
  // 移除已不存在的订单ID
  selectedOrderIds.value = selectedOrderIds.value.filter(id =>
    pendingOrders.value.some(order => order.id === id)
  )
  // 更新全选状态
  updateSelectAllState()
}, { deep: true })

// 处理待付款订单页码变化
const handlePendingPageChange = (page: number) => {
  pendingCurrentPage.value = page
  loadPendingOrders()
}

// 处理已完成订单页码变化
const handleCompletedPageChange = (page: number) => {
  completedCurrentPage.value = page
  loadCompletedOrders()
}

// 应用搜索
const applySearch = () => {
  // 重置页码
  pendingCurrentPage.value = 1
  completedCurrentPage.value = 1

  // 重新加载数据
  if (activeOrderType.value === 'pending') {
    loadPendingOrders()
  } else {
    loadCompletedOrders()
  }

  ElMessage.success({
    message: '搜索条件已应用',
    duration: 1000
  })
}

// 重置搜索
const resetSearch = () => {
  bookTitleQuery.value = ''
  authorQuery.value = ''
  dateQuery.value = ''

  // 重置页码
  pendingCurrentPage.value = 1
  completedCurrentPage.value = 1

  // 重新加载数据
  if (activeOrderType.value === 'pending') {
    loadPendingOrders()
  } else {
    loadCompletedOrders()
  }

  ElMessage.success({
    message: '搜索条件已重置',
    duration: 1000
  })
}

</script>

<style scoped>
.orders-container {
  max-width: 1800px;
  margin: 0 auto;
  padding: 20px;
}

.orders-layout {
  display: flex;
  margin-top: 0;
  min-height: 600px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  background-color: #f5f7fa;
  border-right: 1px solid #e9ecef;
  padding: 20px 0;
  transition: background-color 0.3s ease;
}

.sidebar-item {
  padding: 15px 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}

.sidebar-item:hover {
  background-color: #e9ecef;
}

.sidebar-item.active {
  background-color: #409eff;
  color: white;
}

.sidebar-icon {
  margin-right: 10px;
  font-size: 18px;
}

.sidebar-text {
  font-size: 16px;
}

.badge {
  position: absolute;
  right: 20px;
  background-color: #f56c6c;
  color: white;
  border-radius: 10px;
  padding: 2px 8px;
  font-size: 12px;
}

.sidebar-item.active .badge {
  background-color: white;
  color: #409eff;
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: white;
  overflow-y: auto;
  min-height: 600px;
  transition: all 0.3s ease;
}

h1 {
  margin-bottom: 20px;
  color: #333;
}

.order-count {
  font-size: 16px;
  color: #909399;
  font-weight: normal;
  margin-left: 10px;
}

.filter-bar {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 20px;
}

.search-container {
  display: flex;
  gap: 5px;
  width: 100%;
  align-items: center;
}

.search {
  flex: 0 0 auto;
  width: 120px;
}

.search input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  width: 100%;
}



.search-buttons {
  display: flex;
  gap: 10px;
}

.search-btn, .reset-btn {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  margin-left: 5px;
}

.search-btn {
  background-color: #409eff;
  color: white;
}

.search-btn:hover {
  background-color: #66b1ff;
}

.reset-btn {
  background-color: #909399;
  color: white;
}

.reset-btn:hover {
  background-color: #a6a9ad;
}

.loading, .no-orders {
  text-align: center;
  padding: 40px 0;
  color: #666;
}

/* 覆盖 el-empty 组件的样式 */
.no-orders :deep(.el-empty__image) {
  height: 160px;
}

.no-orders :deep(.el-empty__description) {
  margin-top: 10px;
}

.no-orders :deep(.el-button) {
  border: none !important;
  box-shadow: none !important;
  outline: none !important;
}

.shop-now {
  display: inline-block;
  margin-top: 10px;
  text-decoration: none;
  border: none;
  outline: none;
}

.select-all-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 15px; /* 统一上下内边距 */
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
  height: 52px; /* 固定高度 */
  box-sizing: border-box;
}

.left-section {
  display: flex;
  align-items: center;
}

.right-section {
  display: flex;
  align-items: center;
}

.select-all-checkbox {
  display: flex;
  align-items: center;
  margin-right: 20px;
  height: 22px; /* 固定高度 */
}

.select-all-checkbox input[type="checkbox"] {
  width: 18px;
  height: 18px;
  margin: 0 8px 0 0;
  padding: 0;
  cursor: pointer;
}

.select-all-checkbox label {
  font-weight: bold;
  cursor: pointer;
}

.selected-count {
  margin-right: 20px;
}

.order-summary-inline {
  display: flex;
  align-items: center;
  margin-right: 20px;
}

.total-items-count {
  margin-right: 20px;
  font-weight: bold;
  color: #409EFF;
  font-size: 16px;
}

.batch-buttons {
  display: flex;
  align-items: center;
}

.order-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
}

.total-amount {
  font-weight: bold;
  color: #F56C6C;
  font-size: 16px;
}

.batch-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e9ecef;
}

.batch-buttons {
  display: flex;
  align-items: center;
}

.selected-count {
  font-weight: bold;
  color: #606266;
}

.batch-delete-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
  font-size: 14px;
}

.batch-delete-btn:hover {
  background-color: #66b1ff;
}

.batch-pay-btn {
  background-color: #67C23A;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  font-size: 14px;
}

.batch-pay-btn:hover {
  background-color: #85ce61;
}

.confirm-btn {
  background-color: #67C23A;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

.confirm-btn:hover {
  background-color: #85ce61;
}

.order-total {
  font-weight: bold;
  color: #F56C6C;
  font-size: 16px;
}

.section-controls {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 15px; /* 添加底部间距 */
}

.orders-section {
  margin-bottom: 30px;
  border-radius: 8px;
  overflow: hidden;
}

.section-title {
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-left: 4px solid #409eff;
  margin-bottom: 15px;
  font-size: 18px;
  color: #333;
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.order-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.order-checkbox {
  margin-right: 15px;
  display: flex;
  align-items: center;
  height: 22px; /* 固定高度 */
}

.order-checkbox input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  margin: 0;
  padding: 0;
}

.order-info {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.order-number {
  font-weight: bold;
  margin: 0;
  text-align: center;
}

.order-date {
  color: #666;
  margin: 5px 0 0;
  font-size: 14px;
  text-align: center;
}

.order-countdown {
  background-color: #fff3cd;
  color: #856404;
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: bold;
  margin-left: 10px;
  text-align: center;
}

.countdown-text {
  font-size: 14px;
}

.countdown-timer {
  font-size: 16px;
  color: #dc3545;
  margin-left: 5px;
  font-weight: bold;
}

.order-status {
  padding: 6px 12px;
  border-radius: 4px;
  font-weight: bold;
}

.pending {
  background-color: #ffeeba;
  color: #856404;
}

.paid {
  background-color: #d1ecf1;
  color: #0c5460;
}

.shipped {
  background-color: #c3e6cb;
  color: #155724;
}

.delivered {
  background-color: #c3e6cb;
  color: #155724;
}

.completed {
  background-color: #b8daff;
  color: #004085;
}

.cancelled {
  background-color: #f8d7da;
  color: #721c24;
}

.order-items {
  padding: 15px;
  flex-grow: 1;
}

.order-item {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.order-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.item-details h3 {
  margin: 0 0 5px;
  font-size: 16px;
}

.author {
  color: #666;
  margin: 0 0 10px;
  font-size: 14px;
}

.quantity-price {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 14px;
}



.total-price {
  font-weight: bold;
  color: #e53935;
  font-size: 16px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f8f9fa;
  border-top: 1px solid #eee;
}


.order-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.order-actions button {
  margin: 5px;
  padding: 6px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;
}

.confirm-btn {
  background-color: #4caf50;
  color: white;
}

.waiting-shipment {
  color: #ff9800;
  font-size: 14px;
  padding: 6px 10px;
  border: 1px dashed #ff9800;
  border-radius: 4px;
  display: inline-block;
}

.pay-btn {
  background-color: #f44336;
  color: white;
}

.view-btn {
  background-color: #409eff;
  color: white;
}

.cancel-btn {
  background-color: #409eff;
  color: white;
}

.delete-btn {
  background-color: #409eff;
  color: white;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .orders-grid {
    grid-template-columns: 1fr;
  }

  .order-header {
    flex-direction: column;
    align-items: center;
  }

  .order-status {
    margin-top: 10px;
    align-self: flex-start;
  }

  .order-actions {
    justify-content: center;
  }
}

/* 这个样式已经在上面定义过了 */

.pure-btn {
  background-color: #409eff !important;
  color: white !important;
  border: none !important;
  border-radius: 4px !important;
  padding: 10px 20px !important;
  font-size: 16px !important;
  cursor: pointer;
  transition: background-color 0.3s;
  outline: none !important;
  box-shadow: none !important;
}

.pure-btn:hover,
.pure-btn:focus,
.pure-btn:active {
  background-color: #66b1ff !important;
  color: white !important;
  outline: none !important;
  box-shadow: none !important;
  border: none !important;
}

/* 分页容器样式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  padding: 10px 0;
}

/* 订单详情弹窗样式 */
.order-detail-content {
  padding: 10px 0;
}

.loading-detail {
  text-align: center;
  padding: 20px;
}

.detail-header {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.detail-row {
  display: flex;
  margin-bottom: 10px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  width: 80px;
  font-weight: bold;
  color: #606266;
}

.detail-value {
  color: #303133;
}

.status-0 {
  color: #e6a23c;
}

.status-1 {
  color: #67c23a;
}

.order-items-table {
  width: 100%;
  border-collapse: collapse;
  background-color: #fff;
}

.order-items-table th,
.order-items-table td {
  padding: 12px;
  text-align: center;
  border-bottom: 1px solid #ebeef5;
}

.order-items-table th {
  background-color: #f5f7fa;
  font-weight: bold;
  color: #303133;
}

.order-items-table td {
  color: #606266;
}

.order-items-table tr:last-child td {
  border-bottom: none;
}

.detail-footer {
  margin-top: 20px;
  padding: 15px;
  text-align: right;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.total-amount {
  font-weight: bold;
  color: #303133;
  font-size: 16px;
}

.amount-value {
  color: #f56c6c;
  font-size: 18px;
}
</style>