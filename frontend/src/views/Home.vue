<template>
  <div class="home">
    <div class="banner">
      <h1>欢迎来到智慧书城</h1>
      <p>探索知识的海洋，发现阅读的乐趣</p>
    </div>

    <div class="search-bar">
      <div class="search-inputs">
        <el-input
          v-model="searchTitle"
          placeholder="输入书籍名称"
          class="search-input"
          @keyup.enter="searchBooks"
        >
        </el-input>
        <el-input
          v-model="searchAuthor"
          placeholder="输入作者名称"
          class="search-input"
          @keyup.enter="searchBooks"
        >
        </el-input>
        <div class="search-buttons">
          <el-button @click="searchBooks" type="primary" class="action-button">
            搜索
          </el-button>
          <el-button @click="resetSearch" type="info" class="action-button">
            重置
          </el-button>
        </div>
      </div>
    </div>

    <div class="category-nav">
      <el-menu mode="horizontal" :default-active="activeCategory" @select="selectCategory" :ellipsis="false">
        <el-menu-item index="all">全部图书</el-menu-item>
        <el-menu-item
          v-for="category in mainCategories"
          :key="category.key"
          :index="category.key">
          {{ category.label }}
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 调试信息已移除 -->

    <!-- 错误提示 -->
    <el-alert
      v-if="bookStore.error"
      :title="bookStore.error"
      type="warning"
      show-icon
      :closable="true"
      style="margin-bottom: 20px;"
    />

    <div class="book-list" v-loading="loading">
      <el-empty v-if="books.length === 0 && !loading" description="暂无图书">
        <el-button type="primary" @click="forceFetchBooks">刷新图书</el-button>
      </el-empty>

      <div v-else class="books-container">
        <el-card
          v-for="book in books"
          :key="book.id"
          class="book-card"
        >
          <div class="book-info">
            <h3 class="book-title" @click="goToBookDetail(book.id)">{{ book.title }}</h3>
            <p class="book-author">{{ book.author }}</p>
            <p class="book-category" v-if="book.category">{{ getCategoryName(book.category) }}</p>
            <p class="book-price">¥{{ book.price }}</p>
          </div>
          <div class="card-footer">
            <div class="quantity-selector">
              <span class="quantity-label">数量：</span>
              <div class="quantity-control-group">
                <button class="quantity-btn decrease" @click.stop="decreaseQuantity(book)">-</button>
                <div class="custom-quantity-input">
                  <input
                    type="number"
                    v-model="book.orderQuantity"
                    min="1"
                    max="20"
                    @click.stop
                    class="quantity-input-field"
                  />
                </div>
                <button class="quantity-btn increase" @click.stop="increaseQuantity(book)">+</button>
              </div>
            </div>
            <div class="card-buttons">
              <el-button type="primary" size="default" @click.stop="addToOrder(book)" class="action-btn">
                加入订单
              </el-button>
              <el-button type="success" size="default" @click.stop="addToCart(book)" class="action-btn">
                加入购物车
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页组件 -->
      <div class="pagination-container" v-if="bookStore.totalPages > 0">
        <el-pagination
          background
          layout="prev, pager, next, jumper"
          :total="bookStore.totalBooks"
          :page-size="bookStore.pageSize"
          :current-page="bookStore.currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onActivated } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useBookStore } from '../store/books'
import type { Book } from '../store/books' // 使用 type-only 导入
import { useUserStore } from '../store/user'
import { useCartStore } from '../store/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 图书分类映射 - 将所有分类归并到10个以内
const categoryMap: Record<string, string> = {
  // 文学小说类
  'novel': '文学小说',
  // 科技类
  'technology': '科技计算机',
  // 社科类
  'education': '社会科学',
  // 经管类
  'economics': '经济管理',
  // 历史传记类
  'history': '历史传记',
  // 艺术类
  'art': '艺术设计',
  // 儿童类
  'children': '儿童读物'
}

// 根据分类值获取中文名称
const getCategoryName = (category: string | null | undefined): string => {
  // 处理空值情况
  if (!category) {
    return '未知分类'
  }

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

  // 默认返回值，确保函数总是返回string类型
  return '未知分类'
}

// 扩展Book类型，添加orderQuantity属性
interface BookWithQuantity extends Book {
  orderQuantity: number;
}

const router = useRouter()
const route = useRoute()
const bookStore = useBookStore()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchTitle = ref('')
const searchAuthor = ref('')
const activeCategory = ref('all')
const loading = ref(false)

const books = ref<BookWithQuantity[]>([]) // 显式指定类型为 BookWithQuantity[]

// 主分类列表 - 用于导航栏显示
const mainCategories = [
  { key: 'novel', label: '文学小说' },
  { key: 'technology', label: '科技计算机' },
  { key: 'education', label: '社会科学' },
  { key: 'economics', label: '经济管理' },
  { key: 'history', label: '历史传记' },
  { key: 'art', label: '艺术设计' },
  { key: 'children', label: '儿童读物' }
];

// 获取图书数据的方法
const forceFetchBooks = async () => {
  loading.value = true
  try {
    // 使用分页API获取图书
    await bookStore.fetchPagedBooks(1, 20)
    // 将书籍数据转换为BookWithQuantity类型，添加orderQuantity属性
    books.value = bookStore.books.map(book => ({
      ...book,
      orderQuantity: 1 // 默认数量为1
    }))
  } catch (error) {
    console.error('获取图书数据出错:', error)
  } finally {
    loading.value = false
  }
}

// 处理分页变化
const handlePageChange = async (page: number) => {
  loading.value = true
  try {
    // 更新URL查询参数，但不触发页面刷新
    if (activeCategory.value === 'all') {
      router.replace({
        query: { page: page.toString() }
      })
      await bookStore.fetchPagedBooks(page, 20)
    } else {
      router.replace({
        query: {
          category: activeCategory.value,
          page: page.toString()
        }
      })
      await bookStore.fetchBooksByCategoryPaged(activeCategory.value, page, 20)
    }

    // 将书籍数据转换为BookWithQuantity类型
    books.value = bookStore.books.map(book => ({
      ...book,
      orderQuantity: 1
    }))
  } catch (error) {
    console.error('分页获取图书数据出错:', error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await bookStore.fetchAllCategories() // 获取分类

  // 检查URL查询参数中是否有分类和页码信息
  const categoryParam = route.query.category as string
  const pageParam = route.query.page ? parseInt(route.query.page as string) : 1

  if (categoryParam) {
    // 如果有分类参数，设置当前分类并获取对应分类的图书
    activeCategory.value = categoryParam
    await bookStore.fetchBooksByCategoryPaged(categoryParam, pageParam, 20)
  } else {
    // 否则获取所有图书或指定页码的图书
    if (pageParam > 1) {
      await bookStore.fetchPagedBooks(pageParam, 20)
    } else {
      await forceFetchBooks()
    }
  }

  // 将书籍数据转换为BookWithQuantity类型
  books.value = bookStore.books.map(book => ({
    ...book,
    orderQuantity: 1
  }))
})

// 当组件被keep-alive激活时触发
onActivated(() => {
  // 检查URL查询参数中是否有分类和页码信息
  const categoryParam = route.query.category as string
  const pageParam = route.query.page ? parseInt(route.query.page as string) : 1

  // 只在需要时刷新数据，避免不必要的重复加载
  if (books.value.length === 0 || categoryParam || pageParam > 1) {
    bookStore.fetchAllCategories() // 获取分类

    if (categoryParam) {
      // 如果有分类参数，设置当前分类并获取对应分类的图书
      activeCategory.value = categoryParam
      bookStore.fetchBooksByCategoryPaged(categoryParam, pageParam, 20)
    } else {
      // 否则获取所有图书或指定页码的图书
      if (pageParam > 1) {
        bookStore.fetchPagedBooks(pageParam, 20)
      } else {
        forceFetchBooks() // 强制获取图书数据
      }
    }
  }
})

// 监听bookStore.books的变化
watch(() => bookStore.books, (newBooks) => {
  // 将书籍数据转换为BookWithQuantity类型，添加orderQuantity属性
  books.value = newBooks.map(book => ({
    ...book,
    orderQuantity: 1 // 默认数量为1
  }))
}, { deep: true })

const searchBooks = async () => {
  if (searchTitle.value.trim() || searchAuthor.value.trim()) {
    loading.value = true
    await bookStore.searchBooks(
      searchTitle.value.trim() || undefined,
      searchAuthor.value.trim() || undefined
    )
    books.value = bookStore.books.map(book => ({
      ...book,
      orderQuantity: 1
    }))
    loading.value = false
  } else {
    await forceFetchBooks()
  }
}

const resetSearch = async () => {
  searchTitle.value = ''
  searchAuthor.value = ''
  activeCategory.value = 'all' // Reset category selection

  // 重置URL查询参数
  router.replace({
    query: { page: '1' }
  })

  await forceFetchBooks()
}

const selectCategory = async (category: string) => {
  activeCategory.value = category
  loading.value = true

  // 更新URL查询参数，但不触发页面刷新
  if (category === 'all') {
    router.replace({
      query: { page: '1' } // 重置为第一页
    })
    await bookStore.fetchPagedBooks(1, 20)
  } else {
    router.replace({
      query: {
        category: category,
        page: '1' // 重置为第一页
      }
    })
    // 直接使用分类查询，无需复杂的逻辑判断
    await bookStore.fetchBooksByCategoryPaged(category, 1, 20)
  }

  // 将书籍数据转换为BookWithQuantity类型，添加orderQuantity属性
  books.value = bookStore.books.map(book => ({
    ...book,
    orderQuantity: 1 // 默认数量为1
  }))
  loading.value = false
}

const goToBookDetail = (id: number) => {
  // 导航到图书详情页时，传递当前的分类和页码信息
  router.push({
    path: `/book/${id}`,
    query: {
      fromCategory: activeCategory.value,
      fromPage: bookStore.currentPage.toString()
    }
  })
}

// 增加和减少数量的函数
const increaseQuantity = (book: BookWithQuantity) => {
  if (book.orderQuantity < 20) {
    book.orderQuantity++
  }
}

const decreaseQuantity = (book: BookWithQuantity) => {
  if (book.orderQuantity > 1) {
    book.orderQuantity--
  }
}

const addToOrder = async (book: BookWithQuantity) => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再进行操作')
    router.push('/login')
    return
  }

  // 获取用户选择的数量
  const quantity = book.orderQuantity || 1

  // 显示确认对话框
  ElMessageBox.confirm(
    `确定要将 ${quantity} 本《${book.title}》加入订单吗？`,
    '加入订单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 调用后端 API 创建订单
      await axios.post('http://localhost:8080/api/orders/book', {
        userId: userStore.user?.id,
        bookId: book.id,
        quantity: quantity
      })

      // 成功提示，但不跳转页面
      ElMessage.success({
        message: `已将 ${quantity} 本《${book.title}》加入订单`,
        duration: 1000,
        showClose: true,
        type: 'success'
      })

      // 重置数量为1
      book.orderQuantity = 1
    } catch (error) {
      console.error('创建订单失败:', error)
      ElMessage.error({
        message: `添加《${book.title}》到订单失败，请重试`,
        duration: 5000,
        showClose: true,
        type: 'error'
      })
    }
  }).catch(() => {
    // 用户取消，不执行任何操作
  })
}

// 添加到购物车
const addToCart = async (book: BookWithQuantity) => {
  // 获取用户选择的数量
  const quantity = book.orderQuantity || 1

  // 显示确认对话框
  ElMessageBox.confirm(
    `确定要将 ${quantity} 本《${book.title}》加入购物车吗？`,
    '加入购物车',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      // 用户确认后，调用购物车 store 添加商品
      await cartStore.addToCart(book, quantity)

      // 成功提示
      ElMessage.success({
        message: `已将 ${quantity} 本《${book.title}》加入购物车`,
        duration: 1000,
        showClose: true,
        type: 'success'
      })

      // 重置数量为1
      book.orderQuantity = 1
    } catch (error: any) {
      console.error('添加到购物车失败:', error)
      ElMessage.error(`添加《${book.title}》到购物车失败: ${error.message || '未知错误'}`)
    }
  }).catch(() => {
    // 用户取消，不执行任何操作
  })
}

// 定义组件名称，使keep-alive能够正确缓存
defineOptions({
  name: 'Home'
})
</script>

<style scoped>
.home {
  max-width: 1800px;
  margin: 0 auto;
  padding: 10px;
}

.banner {
  text-align: center;
  margin-bottom: 20px;
  padding: 25px 0;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.banner h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
  color: #303133;
}

.banner p {
  font-size: 1.2em;
  color: #606266;
}

.search-bar {
  margin: 0 auto 20px;
  display: flex;
  justify-content: center;
  width: 100%;
  max-width: 650px;
  padding: 0 15px;
  box-sizing: border-box;
}

.search-inputs {
  display: flex;
  gap: 15px;
  width: 100%;
  max-width: 600px;
  align-items: center;
  justify-content: center;
}

.search-input {
  flex: 1;
  max-width: 180px;
  width: 180px;
}

.search-buttons {
  display: flex;
  gap: 5px;
  justify-content: center;
}

.action-button {
  width: 70px;
  padding: 6px 10px;
  font-size: 14px;
  height: 32px;
  margin: 0 2px;
}

/* 响应式样式，在小屏幕上调整搜索框布局 */
@media (max-width: 768px) {
  .search-bar {
    width: 90%;
    max-width: none;
  }

  .search-inputs {
    flex-direction: column;
    width: 100%;
    align-items: center;
  }

  .search-input {
    margin-bottom: 10px;
    width: 100%;
    max-width: 250px;
  }

  .search-buttons {
    width: 100%;
    justify-content: center;
    margin-top: 5px;
  }

  .action-button {
    flex: 0 0 auto;
    width: 80px;
  }
}

.category-nav {
  margin-bottom: 25px;
  display: flex;
  justify-content: center;
  width: 100%;
  background: linear-gradient(to right, #f8f9fa, #e9f2ff, #f8f9fa);
  padding: 8px 0;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.category-nav::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 5%;
  width: 90%;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(64, 158, 255, 0.3), transparent);
}

.category-nav::-webkit-scrollbar {
  height: 5px;
}

.category-nav::-webkit-scrollbar-thumb {
  background: linear-gradient(to right, #c2e0ff, #409EFF, #c2e0ff);
  border-radius: 5px;
}

.category-nav::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.03);
  border-radius: 5px;
}

.category-nav .el-menu {
  border-bottom: none;
  display: flex;
  white-space: nowrap;
  width: 80%;
  max-width: 1000px;
  margin: 0 auto;
  justify-content: center;
  background: transparent;
  padding: 0;
}

.category-nav .el-menu-item {
  padding: 0 12px;
  height: 55px;
  line-height: 55px;
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
  letter-spacing: 0.5px;
  color: #606266;
  transition: all 0.3s ease;
  position: relative;
  margin: 0 1px;
  border-radius: 6px;
}

.category-nav .el-menu-item.is-active {
  font-weight: bold;
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
  border-bottom: none;
}

.category-nav .el-menu-item.is-active::after {
  content: '';
  position: absolute;
  bottom: 10px;
  left: 25%;
  width: 50%;
  height: 3px;
  background-color: #409EFF;
  border-radius: 3px;
}

.category-nav .el-menu-item:hover {
  background-color: rgba(64, 158, 255, 0.05);
  color: #409EFF;
  transform: translateY(-2px);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .category-nav .el-menu {
    width: 95%;
    justify-content: space-around;
  }

  .category-nav .el-menu-item {
    padding: 0 8px;
    font-size: 16px;
  }
}

@media (min-width: 769px) and (max-width: 1200px) {
  .category-nav .el-menu {
    width: 90%;
  }
}

.books-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-top: 15px;
}

.book-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
}

.book-card:hover {
  box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-5px);
}

.book-info {
  padding: 15px;
  flex-grow: 1;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 150px;
}

.book-title {
  margin: 8px 0;
  font-size: 1.2em;
  font-weight: bold;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  color: #409EFF;
  transition: color 0.3s;
  padding-bottom: 5px;
  border-bottom: 1px dashed #ebeef5;
}

.book-title:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.book-author {
  color: #606266;
  font-size: 1em;
  margin: 8px 0;
}

.book-category {
  color: #67c23a;
  font-size: 0.9em;
  margin: 8px 0;
  background-color: #f0f9eb;
  padding: 3px 8px;
  border-radius: 4px;
  display: inline-block;
}

.book-price {
  color: #f56c6c;
  font-weight: bold;
  margin: 10px 0;
  font-size: 1.3em;
}

.card-footer {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  padding: 10px;
  background-color: #f9f9f9;
  border-top: 1px solid #ebeef5;
}

.quantity-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
}

.quantity-label {
  margin-right: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.quantity-control-group {
  display: flex;
  align-items: center;
  justify-content: center;
}

.custom-quantity-input {
  display: flex;
  align-items: center;
  width: 40px;
  height: 32px;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  margin: 0 5px;
}

.quantity-btn {
  width: 24px;
  height: 32px;
  background-color: #f5f7fa;
  border: 1px solid #dcdfe6;
  color: #606266;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  border-radius: 4px;
}

.quantity-btn:hover {
  background-color: #e4e7ed;
  color: #409EFF;
}

.quantity-input-field {
  width: 100%;
  height: 100%;
  border: none;
  text-align: center;
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  padding: 0;
  appearance: textfield;
  -moz-appearance: textfield; /* Firefox */
  box-sizing: border-box;
}

.quantity-input-field::-webkit-outer-spin-button,
.quantity-input-field::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.card-buttons {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.action-btn {
  flex: 1;
  padding: 10px 0;
  font-size: 14px;
  font-weight: bold;
  border-radius: 4px;
  transition: all 0.3s;
}

/* Add styles for pagination container */
.pagination-container {
  margin-top: 30px; /* Add top margin for spacing */
  display: flex;
  justify-content: center; /* Center the pagination component */
}
</style>
