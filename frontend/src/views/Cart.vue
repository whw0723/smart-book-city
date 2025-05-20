<template>
  <div class="cart-container">
    <h1 class="page-title">我的购物车</h1>

    <div v-if="cartStore.items.length === 0" class="empty-cart">
      <el-empty description="购物车为空">
        <el-button type="primary" @click="$router.push('/home')">去浏览图书</el-button>
      </el-empty>
    </div>

    <div v-else class="cart-content">
      <el-table :data="cartStore.items" style="width: 100%">
        <el-table-column label="图书">
          <template #default="scope">
            <div class="book-info">
              <div>
                <div class="book-title">{{ scope.row.book.title }}</div>
                <div class="book-author">{{ scope.row.book.author }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="单价" width="120">
          <template #default="scope">
            ¥{{ scope.row.book.price }}
          </template>
        </el-table-column>

        <el-table-column label="数量" width="150">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.quantity"
              :min="1"
              :max="20"
              :step="1"
              @change="(value: number) => handleQuantityChange(scope.row.book.id, value)"
              size="small"
            />
          </template>
        </el-table-column>

        <el-table-column label="小计" width="120">
          <template #default="scope">
            ¥{{ (scope.row.book.price * scope.row.quantity).toFixed(2) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              @click="removeFromCart(scope.row.book.id)"
              text
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="cart-footer">
        <div class="cart-summary">
          <div class="total-items">共 {{ cartStore.totalItems }} 件商品</div>
          <div class="total-price">总计：<span>¥{{ cartStore.totalPrice.toFixed(2) }}</span></div>
        </div>

        <div class="cart-actions">
          <el-button @click="clearCart" plain>清空购物车</el-button>
          <el-button type="primary" @click="checkout" :disabled="checkoutLoading">
            {{ checkoutLoading ? '提交中...' : '结算' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import axios from 'axios'
import { useCartStore } from '../store/cart'
import { useUserStore } from '../store/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const checkoutLoading = ref(false)
const loading = ref(false)

// 页面加载时获取购物车数据
onMounted(async () => {
  if (userStore.isLoggedIn) {
    loading.value = true
    try {
      await cartStore.fetchCart()
    } catch (error) {
      console.error('获取购物车失败:', error)
    } finally {
      loading.value = false
    }
  } else {
    // 如果未登录，从本地存储加载
    cartStore.loadFromLocalStorage()
  }
})

// 更新购物车项数量
const handleQuantityChange = async (bookId: number, quantity: number) => {
  try {
    await cartStore.updateQuantity(bookId, quantity)
  } catch (error) {
    console.error('更新购物车失败:', error)
    ElMessage.error('更新购物车失败')
  }
}

// 从购物车中移除商品
const removeFromCart = (bookId: number) => {
  ElMessageBox.confirm('确定要从购物车中移除此商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cartStore.removeFromCart(bookId)
      ElMessage.success('商品已从购物车中移除')
    } catch (error) {
      console.error('从购物车移除失败:', error)
      ElMessage.error('从购物车移除失败')
    }
  }).catch(() => {})
}

// 清空购物车
const clearCart = () => {
  ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cartStore.clearCart()
      ElMessage.success('购物车已清空')
    } catch (error) {
      console.error('清空购物车失败:', error)
      ElMessage.error('清空购物车失败')
    }
  }).catch(() => {})
}

// 结算购物车
const checkout = async () => {
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('请先登录后再结算', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      router.push('/login')
    }).catch(() => {})
    return
  }

  if (cartStore.items.length === 0) {
    ElMessage.warning('购物车为空，无法结算')
    return
  }

  checkoutLoading.value = true
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在创建订单...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    // 为购物车中的每个商品创建订单
    for (const item of cartStore.items) {
      await axios.post('http://localhost:8080/api/orders/book', {
        userId: userStore.user?.id,
        bookId: item.book.id,
        quantity: item.quantity
      })
    }

    // 清空购物车
    await cartStore.clearCart()

    ElMessage.success('订单创建成功！')
    router.push('/orders')
  } catch (error: any) {
    console.error('创建订单失败:', error)
    ElMessage.error(`创建订单失败: ${error.response?.data || error.message || '未知错误'}`)
  } finally {
    loadingInstance.close()
    checkoutLoading.value = false
  }
}
</script>

<style scoped>
.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  margin-bottom: 30px;
  font-size: 24px;
  color: #303133;
}

.empty-cart {
  padding: 60px 0;
}

.book-info {
  display: flex;
  align-items: center;
}

.book-cover {
  width: 60px;
  height: 80px;
  object-fit: cover;
  margin-right: 15px;
  border-radius: 4px;
}

.book-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.book-author {
  color: #909399;
  font-size: 13px;
}

.cart-footer {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-summary {
  display: flex;
  align-items: baseline;
  gap: 20px;
}

.total-items {
  color: #606266;
}

.total-price {
  font-size: 16px;
  color: #606266;
}

.total-price span {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}

.cart-actions {
  display: flex;
  gap: 15px;
}
</style>