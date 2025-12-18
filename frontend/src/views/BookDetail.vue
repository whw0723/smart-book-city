<template>
  <div class="book-detail-container">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>

    <div v-else-if="error" class="error">
      {{ error }}
    </div>

    <div v-else-if="book" class="book-detail">
      <div class="page-header">
        <el-button type="primary" @click="goBack" class="back-button">返回</el-button>
      </div>

      <!-- 书名居中显示 -->
      <div class="title-container">
        <h1 class="book-title">{{ book.title }}</h1>
      </div>

      <!-- 移除了书籍图片 -->
      <div class="book-info">
        <p class="book-author">作者：{{ book.author }}</p>
        <p class="book-price">价格：¥{{ book.price }}</p>
        <p class="book-category">分类：{{ getCategoryName(book.category) }}</p>
        <div class="book-description">
          <h3>图书简介</h3>
          <p>{{ book.description }}</p>
        </div>

        <div class="actions">
          <el-input-number v-model="quantity" :min="1" :max="book.stock" :step="1" />
          <el-button type="primary" @click="addToOrder" :disabled="book.stock <= 0">
            加入订单
          </el-button>
          <el-button type="success" @click="addToCart" :disabled="book.stock <= 0">
            加入购物车
          </el-button>
        </div>
      </div>
    </div>

    <div v-else class="not-found">
      <h2>图书不存在</h2>
      <el-button type="primary" @click="goBack">返回</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useBookStore, type Book } from '../store/books'
import { useUserStore } from '../store/user'
import { useCartStore } from '../store/cart'
import { useOrdersStore } from '../store/orders'
import axios from '../utils/axios'

// 图书分类映射 - 将所有分类归并到7个主分类
const categoryMap: Record<string, string> = {
  // 文学小说类
  'novel': '文学小说',
  'novell': '文学小说',
  'literature': '文学小说',
  'drama': '文学小说',
  'poetry': '文学小说',

  // 科技类
  'technology': '科技计算机',
  'science': '科技计算机',
  'design': '科技计算机',
  'environment': '科技计算机',

  // 社科类
  'education': '社会科学',
  'sociology': '社会科学',
  'philosophy': '社会科学',
  'psychology': '社会科学',

  // 经管类
  'economics': '经济管理',
  'management': '经济管理',
  'marketing': '经济管理',

  // 历史传记类
  'history': '历史传记',
  'biography': '历史传记',
  'military': '历史传记',

  // 艺术类
  'art': '艺术设计',
  'film': '艺术设计',
  'game': '艺术设计',

  // 儿童类
  'children': '儿童读物'
}

const route = useRoute()
const router = useRouter()
const bookStore = useBookStore()
const userStore = useUserStore()
const cartStore = useCartStore()
const ordersStore = useOrdersStore()

// 根据分类值获取中文名称
const getCategoryName = (category: string | null | undefined): string => {
  if (!category) return '未分类'

  // 清理分类字符串，去除多余的空格、换行符等
  const cleanCategory = category.trim().replace(/\s+/g, '')

  // 直接查找映射
  if (categoryMap[cleanCategory.toLowerCase()]) {
    return categoryMap[cleanCategory.toLowerCase()]
  }

  // 如果没有直接匹配，尝试部分匹配
  for (const key in categoryMap) {
    if (cleanCategory.toLowerCase().includes(key.toLowerCase())) {
      return categoryMap[key]
    }
  }

  return '未分类'
}

// 使用正确的类型定义
const book = ref<Book | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)
const quantity = ref(1)

onMounted(async () => {
  const bookId = Number(route.params.id)
  if (isNaN(bookId)) {
    error.value = '无效的图书ID'
    loading.value = false
    return
  }

  try {
    await bookStore.fetchBookById(bookId)
    book.value = bookStore.currentBook
  } catch (err) {
    error.value = '获取图书信息失败'
    console.error(err)
  } finally {
    loading.value = false
  }
})

// 路由器已在上方导入和初始化

const addToOrder = async () => {
  if (!book.value) return

  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再进行操作')
    router.push('/login')
    return
  }

  try {
    // 调用后端 API 创建订单
    await axios.post('http://localhost:8080/api/orders/book', {
      userId: userStore.user?.id,
      bookId: book.value.id,
      quantity: quantity.value
    })

    // 成功提示，但不跳转页面
      ElMessage.success({
        message: `已将 ${quantity.value} 本《${book.value.title}》加入订单`,
        duration: 1000,
        showClose: true,
        type: 'success'
      })

      // 立即更新待支付订单数量
      await ordersStore.updatePendingOrdersCount()

      // 重置数量为1
      quantity.value = 1

  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error({
      message: `添加《${book.value.title}》到订单失败，请重试`,
      duration: 5000,
      showClose: true,
      type: 'error'
    })
  }
}

// 添加到购物车
const addToCart = async () => {
  if (!book.value) return

  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再进行操作')
    router.push('/login')
    return
  }

  try {
    // 调用购物车 store 添加商品
    await cartStore.addToCart(book.value, quantity.value)

    // 成功提示
    ElMessage.success({
      message: `已将 ${quantity.value} 本《${book.value.title}》加入购物车`,
      duration: 1000,
      showClose: true,
      type: 'success'
    })

    // 重置数量为1
    quantity.value = 1

  } catch (error) {
    console.error('添加到购物车失败:', error)
    ElMessage.error({
      message: `添加《${book.value.title}》到购物车失败，请重试`,
      duration: 5000,
      showClose: true,
      type: 'error'
    })
  }
}

// 返回上一页，保留分类和页码信息
const goBack = () => {
  // 获取查询参数中的来源信息
  const fromCategory = route.query.fromCategory as string
  const fromPage = route.query.fromPage as string

  if (fromCategory) {
    // 如果有来源分类信息，返回到对应的分类和页码
    if (fromCategory === 'all') {
      // 返回到全部图书
      router.push({
        path: '/home',
        query: { page: fromPage || '1' }
      })
    } else {
      // 返回到特定分类
      router.push({
        path: '/home',
        query: {
          category: fromCategory,
          page: fromPage || '1'
        }
      })
    }
  } else {
    // 如果没有来源信息，使用浏览器的返回功能
    router.back()
  }
}
</script>

<style scoped>
.book-detail-container {
  max-width: 1600px;
  margin: 20px auto;
  padding: 20px;
}

.book-detail {
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: 10px;
}

.back-button {
  margin-left: 0;
}

.title-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
  width: 100%;
}

.book-info {
  flex: 1;
}

.book-title {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 28px;
  color: #303133;
  text-align: center;
}

.book-author, .book-price, .book-category {
  margin: 10px 0;
  color: #606266;
}

.book-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.book-description {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.book-description h3 {
  font-size: 18px;
  margin-bottom: 10px;
}

.book-description p {
  line-height: 1.6;
  color: #606266;
}

.actions {
  margin-top: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.loading, .error, .not-found {
  padding: 50px;
  text-align: center;
}

.error, .not-found {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .book-detail {
    flex-direction: column;
  }

  .book-cover {
    flex: none;
    margin-bottom: 20px;
  }
}
</style>