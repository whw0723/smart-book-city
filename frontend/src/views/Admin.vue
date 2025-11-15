<template>
  <AdminNavbar />
  <div class="admin-container">
    <el-container>
      <el-aside width="160px">
        <el-menu
          :default-active="activeMenu"
          class="admin-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="books">
            <el-icon><Reading /></el-icon>
            <span>图书管理</span>
          </el-menu-item>
          <el-menu-item index="users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="orders">
            <el-icon><List /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="stats">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-main>
          <!-- Books Management -->
          <div v-if="activeMenu === 'books'">
            <div class="action-bar">
              <div class="left-actions">
                <el-button type="primary" @click="showAddBookDialog">
                  添加图书
                </el-button>
                <el-button
                  type="danger"
                  @click="batchDeleteBooks"
                  :disabled="selectedBooks.length === 0"
                >
                  批量删除
                </el-button>
                <span v-if="selectedBooks.length > 0" class="selection-info">
                  已选择 {{ selectedBooks.length }} 项
                </span>
              </div>

              <div class="search-container">
                <el-input
                  v-model="bookTitleSearch"
                  placeholder="书名"
                  style="width: 150px; margin-left: 16px;"
                  clearable
                  @keyup.enter="searchBooks"
                />
                <el-input
                  v-model="bookAuthorSearch"
                  placeholder="作者"
                  style="width: 150px; margin-left: 8px;"
                  clearable
                  @keyup.enter="searchBooks"
                />
                <el-select
                  v-model="bookCategorySearch"
                  placeholder="分类"
                  style="width: 150px; margin-left: 8px;"
                  clearable
                  @change="searchBooks"
                >
                  <el-option
                    v-for="item in categoryOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
                <el-button type="primary" @click="searchBooks" style="width: 80px; margin-left: 4px;">
                  搜索
                </el-button>
                <el-button type="info" @click="resetBookSearch" style="width: 80px; margin-left: 4px;">
                  重置
                </el-button>
              </div>
            </div>

            <el-table
              :data="paginatedBooks"
              style="width: 100%"
              :header-cell-style="{
                backgroundColor: '#f5f7fa',
                color: '#606266',
                fontWeight: 'bold',
                textAlign: 'center'
              }"
              :cell-style="{
                textAlign: 'center'
              }"
              class="larger-font-table"
              @selection-change="handleSelectionChange"
            >
              <el-table-column type="selection" width="55" align="center" />
              <el-table-column label="编号" width="70" align="center">
                <template #default="scope">
                  {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" align="center" />
              <el-table-column prop="author" label="作者" width="150" align="center" />
              <el-table-column prop="price" label="价格" width="100" align="center">
                <template #default="scope">
                  ¥{{ scope.row.price }}
                </template>
              </el-table-column>
              <el-table-column prop="stock" label="库存" width="100" align="center" />
              <el-table-column prop="category" label="分类" width="120" align="center">
                <template #default="scope">
                  {{ getCategoryName(scope.row.category) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" align="center">
                <template #default="scope">
                  <el-button
                    size="small"
                    type="primary"
                    @click="editBook(scope.row)"
                  >
                    编辑
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    @click="deleteBook(scope.row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页组件 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="filteredBooks.length"
                layout="total, prev, pager, next, jumper"
                @current-change="handlePageChange"
              />
            </div>
          </div>

          <!-- Users Management -->
          <div v-if="activeMenu === 'users'">
            <div class="action-bar">
              <div class="search-container">
                <el-input
                  v-model="userNameSearch"
                  placeholder="用户名"
                  style="width: 150px;"
                  clearable
                  @keyup.enter="searchUsers"
                />
                <el-input
                  v-model="userEmailSearch"
                  placeholder="邮箱"
                  style="width: 150px; margin-left: 8px;"
                  clearable
                  @keyup.enter="searchUsers"
                />
                <el-button type="primary" @click="searchUsers" style="width: 80px; margin-left: 4px;">
                  搜索
                </el-button>
                <el-button type="info" @click="resetUserSearch" style="width: 80px; margin-left: 4px;">
                  重置
                </el-button>
              </div>
            </div>

            <el-table
              :data="paginatedUsers"
              style="width: 100%"
              :header-cell-style="{
                backgroundColor: '#f5f7fa',
                color: '#606266',
                fontWeight: 'bold',
                textAlign: 'center'
              }"
              :cell-style="{
                textAlign: 'center'
              }"
              class="larger-font-table"
            >
              <el-table-column prop="id" label="ID" width="80" align="center" />
              <el-table-column prop="username" label="用户名" width="150" align="center" />
              <el-table-column prop="email" label="邮箱" align="center" />
              <el-table-column label="订单数" width="120" align="center">
                <template #default="scope">
                  {{ getUserOrderCount(scope.row.id) }}
                </template>
              </el-table-column>
            </el-table>

            <!-- 用户分页组件 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="userCurrentPage"
                :page-size="pageSize"
                :total="userTotal"
                layout="total, prev, pager, next, jumper"
                @current-change="handleUserPageChange"
              />
            </div>
          </div>

          <!-- Orders Management -->
          <div v-if="activeMenu === 'orders'">
            <div class="action-bar">
              <div class="search-container">
                <el-input
                  v-model="orderNumberSearch"
                  placeholder="订单号"
                  style="width: 150px;"
                  clearable
                  @keyup.enter="searchOrders"
                />
                <el-input
                  v-model="orderUserSearch"
                  placeholder="用户"
                  style="width: 120px; margin-left: 8px;"
                  clearable
                  @keyup.enter="searchOrders"
                />
                <el-date-picker
                  v-model="orderDateSearch"
                  type="date"
                  placeholder="下单日期"
                  style="width: 150px; margin-left: 8px;"
                  value-format="YYYY-MM-DD"
                  @change="searchOrders"
                />
                <el-button type="primary" @click="searchOrders" style="width: 80px; margin-left: 4px;">
                  搜索
                </el-button>
                <el-button type="info" @click="resetOrderSearch" style="width: 80px; margin-left: 4px;">
                  重置
                </el-button>
              </div>


            </div>

            <table class="custom-table">
              <thead>
                <tr>
                  <th>编号</th>
                  <th>订单号</th>
                  <th>用户</th>
                  <th>下单日期</th>
                  <th>金额</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(order, index) in paginatedOrders" :key="order.id">
                  <td>{{ (orderCurrentPage - 1) * pageSize + index + 1 }}</td>
                  <td>{{ order.orderNumber || order.id }}</td>
                  <td>{{ order.user?.username || '未知用户' }}</td>
                  <td>{{ formatDate(order.orderDate) }}</td>
                  <td>¥{{ order.totalAmount }}</td>
                  <td>
                    <el-tag :type="getStatusType(order.status)">
                      {{ getStatusText(order.status) }}
                    </el-tag>
                  </td>
                  <td>
                    <el-button
                      size="small"
                      type="primary"
                      @click="goToOrderDetail(order.id)"
                    >
                      查看详情
                    </el-button>
                  </td>
                </tr>
              </tbody>
            </table>

            <!-- 订单分页组件 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="orderCurrentPage"
                :page-size="pageSize"
                :total="orderTotal"
                layout="total, prev, pager, next, jumper"
                @current-change="handleOrderPageChange"
              />
            </div>
          </div>

          <!-- Stats -->
          <div v-if="activeMenu === 'stats'" class="stats-container">
            <!-- 顶部卡片 -->
            <div class="stats-cards">
              <div class="stats-card">
                <div class="stats-card-value">{{ totalSales }}元</div>
                <div class="stats-card-title">总销售额</div>
              </div>
              <div class="stats-card">
                <div class="stats-card-value">{{ totalOrders }}</div>
                <div class="stats-card-title">总订单数</div>
              </div>
              <div class="stats-card">
                <div class="stats-card-value">{{ totalUsers }}</div>
                <div class="stats-card-title">用户总数</div>
              </div>
              <div class="stats-card">
                <div class="stats-card-value">{{ totalBooks }}</div>
                <div class="stats-card-title">图书总数</div>
              </div>
            </div>

            <!-- 图表区域 -->
            <div class="stats-charts">
              <!-- 销售统计 -->
              <div class="stats-chart-container">
                <h3>销售统计</h3>
                <div class="chart-tabs">
                  <span
                    v-for="(label, value) in { 'day': '日', 'week': '周', 'month': '月', 'year': '年' }"
                    :key="value"
                    :class="{ active: salesPeriod === value }"
                    @click="changeSalesPeriod(value)"
                  >
                    {{ label }}
                  </span>
                </div>
                <div class="chart" id="sales-chart" ref="salesChart"></div>
              </div>

              <!-- 畅销书籍 -->
              <div class="stats-chart-container">
                <h3>畅销书籍排行</h3>
                <div class="chart" id="top-books-chart" ref="topBooksChart"></div>
              </div>

              <!-- 分类销售比例 -->
              <div class="stats-chart-container">
                <h3>分类书籍数量比例</h3>
                <div class="chart" id="category-chart" ref="categoryChart"></div>
              </div>

              <!-- 订单状态分布 -->
              <div class="stats-chart-container">
                <h3>订单状态分布</h3>
                <div class="chart" id="order-status-chart" ref="orderStatusChart"></div>
              </div>
            </div>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- 图书添加/编辑对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加图书' : '编辑图书'"
      v-model="showDialog"
      width="400px"
    >
      <el-form
        ref="bookFormRef"
        :model="editingBook"
        :rules="bookFormRules"
        label-width="60px"
        class="book-form"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="editingBook.title" placeholder="请输入图书标题" class="shorter-input" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="editingBook.author" placeholder="请输入作者姓名" class="shorter-input" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="editingBook.price" :min="0" :precision="2" :step="1" class="shorter-input" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="editingBook.stock" :min="0" :step="1" class="shorter-input" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="editingBook.category" placeholder="请选择图书分类" class="shorter-input">
            <el-option
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editingBook.description"
            type="textarea"
            :rows="4"
            placeholder="请输入图书描述"
            class="description-input"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="primary" @click="handleBookSubmit(bookFormRef)">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { List, Reading, User, DataAnalysis } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import AdminNavbar from '../components/AdminNavbar.vue'
import axios from 'axios'

interface Book {
  id: number;
  title: string;
  author: string;
  price: number;
  stock: number;
  category: string;
  description: string;
}

interface UserType {
  id: number;
  username: string;
  email?: string;
  role: number;
  balance?: number;
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
  };
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = ref('books')
// 旧的搜索关键字（保留以防其他地方还在使用）
const bookSearchKeyword = ref('')
// 图书搜索字段
const bookTitleSearch = ref('')
const bookAuthorSearch = ref('')
const bookCategorySearch = ref('')
// 用户搜索字段
const userNameSearch = ref('')
const userEmailSearch = ref('')
// 订单搜索字段
const orderNumberSearch = ref('')
const orderUserSearch = ref('')
const orderDateSearch = ref('')

// 分页相关
const currentPage = ref(1)
const userCurrentPage = ref(1)
const orderCurrentPage = ref(1)
const pageSize = ref(10) // 每页显示10条数据
const userTotal = ref(0) // 用户总数
const orderTotal = ref(0) // 订单总数

const books = ref<Book[]>([])
const users = ref<UserType[]>([])
const orders = ref<Order[]>([])
const userOrderCounts = ref<Record<number, number>>({})

// 图书选择相关
const selectedBooks = ref<Book[]>([])
const isAllBooksSelected = ref(false)

// 订单相关变量

// 在组件挂载时加载数据
onMounted(async () => {
  if (!userStore.isLoggedIn || !userStore.isAdmin) {
    ElMessage.warning('请先登录管理员账号')
    router.push('/login')
    return
  }

  // 检查URL查询参数中是否有指定的活动菜单
  const queryActiveMenu = route.query.activeMenu as string
  if (queryActiveMenu && ['books', 'users', 'orders', 'stats'].includes(queryActiveMenu)) {
    activeMenu.value = queryActiveMenu
    console.log('从URL查询参数设置活动菜单:', queryActiveMenu)
  }

  // 加载书籍数据
  try {
    const response = await axios.get('http://localhost:8080/api/books')
    // 适配新的API响应格式
    if (response.data && response.data.success && Array.isArray(response.data.data)) {
      books.value = response.data.data
    } else if (response.data && Array.isArray(response.data)) {
      // 兼容旧格式
      books.value = response.data
    } else {
      console.warn('书籍数据格式不正确:', response.data)
      ElMessage.error(response.data?.message || '加载书籍数据失败')
    }
  } catch (error) {
    console.error('加载书籍数据失败:', error)
    ElMessage.error('加载书籍数据失败')
  }

  // 加载用户数据
  try {
    const response = await axios.get('http://localhost:8080/api/users/paged', {
      params: {
        page: userCurrentPage.value,
        size: pageSize.value
      }
    })
    console.log('用户数据:', response.data)
    if (response.data && response.data.users) {
      users.value = response.data.users
      userTotal.value = response.data.total || 0
    } else if (response.data && Array.isArray(response.data.users)) {
      // 处理可能的数组格式
      users.value = response.data.users
      userTotal.value = response.data.users.length
    } else {
      console.warn('用户数据格式不正确:', response.data)
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
    ElMessage.error('加载用户数据失败')
  }

  // 加载订单数据
  await loadOrdersFromServer()

  // 计算每个用户的订单数 - 从服务器获取准确数据
  await calculateUserOrderCounts()

  // 如果当前是统计页面，初始化图表
  if (activeMenu.value === 'stats') {
    initCharts()
  }
})

// 菜单处理
const handleMenuSelect = (index: string) => {
  activeMenu.value = index

  // 更新URL查询参数，但不触发页面刷新
  router.replace({
    query: {
      ...route.query,
      activeMenu: index
    }
  })
}

// 图书搜索函数
const searchBooks = () => {
  console.log('执行图书搜索，条件：', {
    title: bookTitleSearch.value,
    author: bookAuthorSearch.value,
    category: bookCategorySearch.value
  })
  // 搜索逻辑已在 filteredBooks 计算属性中实现

  // 搜索时重置为第一页
  currentPage.value = 1
}

// 用户搜索函数
const searchUsers = () => {
  console.log('执行用户搜索，条件:', {
    username: userNameSearch.value,
    email: userEmailSearch.value
  })
  // 搜索逻辑已在 filteredUsers 计算属性中实现
}

// 处理图书选择变化
const handleSelectionChange = (selection: Book[]) => {
  selectedBooks.value = selection
  // 检查是否全选
  isAllBooksSelected.value = selection.length > 0 && selection.length === paginatedBooks.value.length
}

// 切换图书全选/取消全选
const toggleBookSelectAll = () => {
  const tableRef = document.querySelector('.el-table__header-wrapper th.el-table-column--selection .el-checkbox') as HTMLElement
  if (tableRef) {
    tableRef.click()
  }
}

// 批量删除图书
const batchDeleteBooks = () => {
  if (selectedBooks.value.length === 0) {
    ElMessage.warning('请先选择要删除的图书')
    return
  }

  ElMessageBox.confirm(`确定要删除选中的 ${selectedBooks.value.length} 本图书吗？此操作不可恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 获取所有选中图书的ID
      const bookIds = selectedBooks.value.map(book => book.id)

      // 调用后端API批量删除图书
      await axios.post('http://localhost:8080/api/books/batch-delete', { bookIds })

      // 从列表中移除已删除的图书
      books.value = books.value.filter(book => !bookIds.includes(book.id))

      // 清空选择
      selectedBooks.value = []

      ElMessage.success('批量删除成功')
    } catch (error: any) {
      console.error('批量删除图书失败:', error)
      if (error.response && error.response.data) {
        ElMessage.error(`批量删除图书失败: ${error.response.data}`)
      } else {
        ElMessage.error('批量删除图书失败')
      }
    }
  }).catch(() => {})
}

// 重置图书搜索
const resetBookSearch = () => {
  bookTitleSearch.value = ''
  bookAuthorSearch.value = ''
  bookCategorySearch.value = ''
  currentPage.value = 1 // 重置为第一页
}

// 重置用户搜索
const resetUserSearch = () => {
  userNameSearch.value = ''
  userEmailSearch.value = ''
  userCurrentPage.value = 1 // 重置为第一页
}

// 订单搜索函数
const searchOrders = async () => {
  console.log('执行订单搜索，条件：', {
    orderNumber: orderNumberSearch.value,
    user: orderUserSearch.value,
    date: orderDateSearch.value
  })

  // 如果没有搜索条件，重新从服务器获取数据
  if (!orderNumberSearch.value && !orderUserSearch.value && !orderDateSearch.value) {
    await loadOrdersFromServer()
  }
  // 有搜索条件时，使用本地过滤（filteredOrders计算属性）
}

// 重置订单搜索
const resetOrderSearch = async () => {
  orderNumberSearch.value = ''
  orderUserSearch.value = ''
  orderDateSearch.value = ''
  orderCurrentPage.value = 1 // 重置为第一页

  // 重新从服务器加载数据
  await loadOrdersFromServer()
}

// 从服务器加载订单数据
const loadOrdersFromServer = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/orders/paged', {
      params: {
        page: orderCurrentPage.value,
        size: pageSize.value
      }
    })
    console.log('从服务器加载订单数据:', response.data)
    if (response.data && response.data.orders) {
      orders.value = response.data.orders
      orderTotal.value = response.data.total || 0
    } else if (response.data && Array.isArray(response.data)) {
      // 处理可能的数组格式
      orders.value = response.data
      orderTotal.value = response.data.length
    } else {
      console.warn('订单数据格式不正确:', response.data)
    }
  } catch (error) {
    console.error('加载订单数据失败:', error)
    ElMessage.error('加载订单数据失败')
  }
}

// 处理图书页码变化
const handlePageChange = (page: number) => {
  currentPage.value = page
  console.log('图书当前页码:', page)
}

// 处理用户页码变化
const handleUserPageChange = async (page: number) => {
  userCurrentPage.value = page
  console.log('用户当前页码:', page)

  // 重新加载用户数据
  try {
    const response = await axios.get('http://localhost:8080/api/users/paged', {
      params: {
        page: userCurrentPage.value,
        size: pageSize.value
      }
    })
    console.log('用户分页数据:', response.data)
    if (response.data && response.data.users) {
      users.value = response.data.users
      userTotal.value = response.data.total || 0
    } else if (response.data && Array.isArray(response.data.users)) {
      // 处理可能的数组格式
      users.value = response.data.users
      userTotal.value = response.data.users.length
    } else {
      console.warn('用户数据格式不正确:', response.data)
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
    ElMessage.error('加载用户数据失败')
  }

  // 重新计算用户订单数 - 从服务器获取准确数据
  await calculateUserOrderCounts()
}

// 处理订单页码变化
const handleOrderPageChange = async (page: number) => {
  orderCurrentPage.value = page
  console.log('订单当前页码:', page)

  // 如果没有搜索条件，从服务器加载数据
  if (!orderNumberSearch.value && !orderUserSearch.value && !orderDateSearch.value) {
    await loadOrdersFromServer()
  }
  // 有搜索条件时，使用本地过滤（由paginatedOrders计算属性处理）
}

// 过滤功能
const filteredBooks = computed(() => {
  // 如果没有任何搜索条件，返回所有图书
  if (!bookTitleSearch.value && !bookAuthorSearch.value && !bookCategorySearch.value && !bookSearchKeyword.value) {
    return books.value
  }

  // 使用搜索字段进行过滤
  return books.value.filter(book => {
    // 书名搜索
    const titleMatch = !bookTitleSearch.value ||
      book.title.toLowerCase().includes(bookTitleSearch.value.toLowerCase())

    // 作者搜索
    const authorMatch = !bookAuthorSearch.value ||
      book.author.toLowerCase().includes(bookAuthorSearch.value.toLowerCase())

    // 分类搜索
    const categoryMatch = !bookCategorySearch.value ||
      (book.category && isCategoryMatch(book.category, bookCategorySearch.value))

    // 兼容旧的搜索关键字
    const keywordMatch = !bookSearchKeyword.value ||
      book.title.toLowerCase().includes(bookSearchKeyword.value.toLowerCase()) ||
      book.author.toLowerCase().includes(bookSearchKeyword.value.toLowerCase())

    // 所有条件都必须满足（AND 逻辑）
    return titleMatch && authorMatch && categoryMatch && keywordMatch
  })
})

// 判断图书分类是否匹配
const isCategoryMatch = (bookCategory: string, selectedCategory: string): boolean => {
  if (!bookCategory || !selectedCategory) return false

  // 清理分类字符串
  const cleanBookCategory = bookCategory.trim().toLowerCase()
  const cleanSelectedCategory = selectedCategory.trim().toLowerCase()

  // 直接匹配
  if (cleanBookCategory === cleanSelectedCategory) return true

  // 检查是否属于同一主分类
  // 获取书籍的主分类
  for (const key in categoryMap) {
    if (cleanBookCategory.includes(key.toLowerCase()) && key.toLowerCase() === cleanSelectedCategory) {
      return true
    }
  }

  return false
}

// 分页数据
const paginatedBooks = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredBooks.value.slice(start, end)
})

const filteredUsers = computed(() => {
  // 如果没有任何搜索条件，返回所有用户
  if (!userNameSearch.value && !userEmailSearch.value) return users.value

  return users.value.filter(user => {
    // 用户名搜索
    const usernameMatch = !userNameSearch.value ||
      (user.username && user.username.toLowerCase().includes(userNameSearch.value.toLowerCase()))

    // 邮箱搜索
    const emailMatch = !userEmailSearch.value ||
      (user.email && user.email.toLowerCase().includes(userEmailSearch.value.toLowerCase()))

    // 所有条件都必须满足（AND 逻辑）
    return usernameMatch && emailMatch
  })
})

// 用户分页数据
const paginatedUsers = computed(() => {
  const start = (userCurrentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    // 订单编号筛选
    const orderNumberMatch = !orderNumberSearch.value ||
      (order.orderNumber && order.orderNumber.toLowerCase().includes(orderNumberSearch.value.toLowerCase())) ||
      order.id.toString().includes(orderNumberSearch.value);

    // 用户筛选
    const userMatch = !orderUserSearch.value ||
      (order.user?.username && order.user.username.toLowerCase().includes(orderUserSearch.value.toLowerCase()));

    // 日期筛选
    const dateMatch = !orderDateSearch.value ||
      (order.orderDate && formatDate(order.orderDate).startsWith(orderDateSearch.value));

    // 所有条件都必须满足（AND 逻辑）
    return orderNumberMatch && userMatch && dateMatch;
  });
})

// 订单分页数据 - 使用服务器分页，不再本地分页
const paginatedOrders = computed(() => {
  // 如果有搜索条件，使用本地过滤后的数据
  if (orderNumberSearch.value || orderUserSearch.value || orderDateSearch.value) {
    const start = (orderCurrentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredOrders.value.slice(start, end)
  }
  // 没有搜索条件时，直接使用从服务器获取的数据
  return orders.value
})

// 格式化日期
const formatDate = (date: string | Date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// 获取订单状态文本和类型
const getStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '待支付',
    1: '已完成'
    // 已取消的订单直接删除，不再显示
  }
  return statusMap[status] || '未知状态'
}

const getStatusType = (status: number): string => {
  const typeMap: Record<number, string> = {
    0: 'warning',
    1: 'success'
    // 已取消的订单直接删除，不再显示
  }
  return typeMap[status] || ''
}

// 图书管理相关变量
const showDialog = ref(false)
const dialogType = ref('add')
const bookFormRef = ref()
const editingBook = ref<Book>({
  id: 0,
  title: '',
  author: '',
  price: 0,
  stock: 0,
  category: '',
  description: ''
})

// 图书分类选项
const categoryOptions = [
  { value: 'novel', label: '文学小说' },
  { value: 'technology', label: '科技计算机' },
  { value: 'education', label: '社会科学' },
  { value: 'economics', label: '经济管理' },
  { value: 'history', label: '历史传记' },
  { value: 'art', label: '艺术设计' },
  { value: 'children', label: '儿童读物' }
]

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

// 表单验证规则
const bookFormRules = {
  title: [{ required: true, message: '请输入图书标题', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者姓名', trigger: 'blur' }],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于等于0', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存必须大于等于0', trigger: 'blur' }
  ],
  category: [{ required: true, message: '请选择图书分类', trigger: 'change' }]
}

// 图书管理
const showAddBookDialog = () => {
  dialogType.value = 'add'
  editingBook.value = {
    id: 0,
    title: '',
    author: '',
    price: 0,
    stock: 0,
    category: '',
    description: ''
  }
  showDialog.value = true
}

const editBook = (book: Book) => {
  dialogType.value = 'edit'
  editingBook.value = { ...book }
  showDialog.value = true
}

const handleBookSubmit = async (formEl: any) => {
  if (!formEl) return

  await formEl.validate(async (valid: boolean) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          // 添加图书
          const response = await axios.post('http://localhost:8080/api/books', editingBook.value)
          if (response.data && response.data.success && response.data.data) {
            books.value.push(response.data.data)
            ElMessage.success('图书添加成功')
          }
        } else {
          // 更新图书
          const response = await axios.put(`http://localhost:8080/api/books/${editingBook.value.id}`, editingBook.value)
          if (response.data && response.data.success && response.data.data) {
            const index = books.value.findIndex(b => b.id === editingBook.value.id)
            if (index !== -1) {
              books.value[index] = response.data.data
            }
            ElMessage.success('图书更新成功')
          }
        }
        showDialog.value = false
      } catch (error) {
        console.error(dialogType.value === 'add' ? '添加图书失败:' : '更新图书失败:', error)
        ElMessage.error(dialogType.value === 'add' ? '添加图书失败' : '更新图书失败')
      }
    }
  })
}

const deleteBook = (book: Book) => {
  ElMessageBox.confirm(`确定要删除图书《${book.title}》吗？此操作不可恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 调用后端 API 删除图书
      await axios.delete(`http://localhost:8080/api/books/${book.id}`)
      // 从列表中移除已删除的图书
      const index = books.value.findIndex(b => b.id === book.id)
      if (index !== -1) {
        books.value.splice(index, 1)
      }
      ElMessage.success('删除成功')
    } catch (error: any) {
      console.error('删除图书失败:', error)
      // 显示后端返回的错误信息
      if (error.response && error.response.data) {
        ElMessage.error(`删除图书失败: ${error.response.data}`)
      } else {
        ElMessage.error('删除图书失败')
      }
    }
  }).catch(() => {})
}

// 计算每个用户的订单数
const calculateUserOrderCounts = async () => {
  try {
    // 从服务器获取用户订单数量
    const response = await axios.get('http://localhost:8080/api/users/order-counts')
    console.log('从服务器获取用户订单数量:', response.data)

    // 更新订单计数
    userOrderCounts.value = response.data
  } catch (error) {
    console.error('获取用户订单数量失败:', error)
    ElMessage.error('获取用户订单数量失败')

    // 失败时使用本地计算作为备选方案
    userOrderCounts.value = {}
    orders.value.forEach(order => {
      if (order.user && order.user.id) {
        const userId = order.user.id
        if (!userOrderCounts.value[userId]) {
          userOrderCounts.value[userId] = 0
        }
        userOrderCounts.value[userId]++
      }
    })
  }
}

// 获取用户订单数
const getUserOrderCount = (userId: number): number => {
  return userOrderCounts.value[userId] || 0
}

// 用户管理
const deleteUser = (user: UserType) => {
  // 如果是当前登录的用户，显示额外警告
  const warningMessage = user.id === userStore.user?.id
    ? `确定要删除用户 ${user.username} 吗？此操作不可恢复！\n注意：这是您当前登录的账户，删除后将自动登出。`
    : `确定要删除用户 ${user.username} 吗？此操作不可恢复！`;

  ElMessageBox.confirm(warningMessage, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 调用后端 API 删除用户
      await axios.delete(`http://localhost:8080/api/users/${user.id}`)
      // 从列表中移除已删除的用户
      const index = users.value.findIndex(u => u.id === user.id)
      if (index !== -1) {
        users.value.splice(index, 1)
      }

      // 如果删除的是当前登录的用户，则登出
      if (user.id === userStore.user?.id) {
        ElMessage.success('删除成功，即将登出...')
        setTimeout(() => {
          userStore.logout()
          router.push('/login')
        }, 1500)
      } else {
        ElMessage.success('删除成功')
      }
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }).catch(() => {})
}

// 订单管理相关函数

// 导航到订单详情页面
const goToOrderDetail = (orderId: number) => {
  try {
    console.log('导航到订单详情页面，订单ID:', orderId)
    router.push(`/admin/order/${orderId}`)
  } catch (error) {
    console.error('导航到订单详情页面失败:', error)
    ElMessage.error('导航失败，请重试')
  }
}

// 数据统计相关
const salesPeriod = ref('month') // 使用英文，与后端API对应
const salesChart = ref(null)
const topBooksChart = ref(null)
const categoryChart = ref(null)
const orderStatusChart = ref(null)

// 统计数据类型定义
interface SalesDataItem {
  date: string;
  value: number;
}

interface TopBookItem {
  title: string;
  sales: number;
}

interface CategoryItem {
  name: string;
  value: number;
}

interface OrderStatusItem {
  name: string;
  value: number;
}

// 统计数据
const salesData = ref<SalesDataItem[]>([])
const topBooksData = ref<TopBookItem[]>([])
const categoryData = ref<CategoryItem[]>([])
const orderStatusData = ref<OrderStatusItem[]>([])

// 统计数据
const totalSales = ref('0.00')
const totalOrders = ref(0)
const totalUsers = ref(0)
const totalBooks = ref(0)

// 生成模拟销售数据
const generateMockSalesData = (period: string): SalesDataItem[] => {
  const data: SalesDataItem[] = []
  const now = new Date()
  
  switch (period) {
    case 'day':
      // 生成最近7天的数据
      for (let i = 6; i >= 0; i--) {
        const date = new Date(now)
        date.setDate(date.getDate() - i)
        data.push({
          date: `${date.getMonth() + 1}/${date.getDate()}`,
          value: Math.floor(Math.random() * 5000) + 1000
        })
      }
      break
    case 'week':
      // 生成最近8周的数据
      for (let i = 7; i >= 0; i--) {
        const date = new Date(now)
        date.setDate(date.getDate() - i * 7)
        data.push({
          date: `第${8-i}周`,
          value: Math.floor(Math.random() * 15000) + 5000
        })
      }
      break
    case 'month':
      // 生成最近12个月的数据
      for (let i = 11; i >= 0; i--) {
        const date = new Date(now)
        date.setMonth(date.getMonth() - i)
        data.push({
          date: `${date.getFullYear()}/${date.getMonth() + 1}`,
          value: Math.floor(Math.random() * 50000) + 20000
        })
      }
      break
    case 'year':
      // 生成最近5年的数据
      for (let i = 4; i >= 0; i--) {
        const year = now.getFullYear() - i
        data.push({
          date: year.toString(),
          value: Math.floor(Math.random() * 200000) + 100000
        })
      }
      break
  }
  
  return data
}

// 生成模拟畅销书籍数据
const generateMockTopBooksData = (): TopBookItem[] => {
  return [
    { title: '三体', sales: 156 },
    { title: 'JavaScript高级程序设计', sales: 142 },
    { title: '活着', sales: 138 },
    { title: '人类简史', sales: 125 },
    { title: '小王子', sales: 118 },
    { title: '百年孤独', sales: 112 },
    { title: '深入理解计算机系统', sales: 98 },
    { title: '哈利·波特与魔法石', sales: 95 },
    { title: '经济学原理', sales: 87 },
    { title: '平凡的世界', sales: 82 }
  ]
}

// 生成模拟分类数据
const generateMockCategoryData = (): CategoryItem[] => {
  return [
    { name: '小说', value: 1167 },
    { name: '科技', value: 856 },
    { name: '教育', value: 743 },
    { name: '经济', value: 625 },
    { name: '历史', value: 512 },
    { name: '艺术', value: 398 },
    { name: '儿童', value: 687 }
  ]
}

// 生成模拟订单状态数据
const generateMockOrderStatusData = (): OrderStatusItem[] => {
  return [
    { name: '待付款', value: 23 },
    { name: '已付款', value: 45 },
    { name: '已发货', value: 67 },
    { name: '已完成', value: 189 },
    { name: '已取消', value: 12 }
  ]
}

// 加载统计摘要数据
const loadStatisticsSummary = async () => {
  try {
    // 如果有真实订单数据，计算真实的总销售额
    if (orders.value.length > 0) {
      totalSales.value = orders.value.reduce((sum, order) => {
        // 只计算已支付和已完成的订单
        if (order.status === 1 || order.status === 2 || order.status === 3) {
          return sum + order.totalAmount
        }
        return sum
      }, 0).toFixed(2)
    } else {
      // 使用模拟数据
      totalSales.value = '125680.50'
    }

    // 获取总订单数、用户数和图书数
    totalOrders.value = orderTotal.value > 0 ? orderTotal.value : 336
    totalUsers.value = userTotal.value > 0 ? userTotal.value : 1
    totalBooks.value = books.value.length > 0 ? books.value.length : 1167
  } catch (error) {
    console.error('加载统计摘要数据失败:', error)
    // 使用默认模拟数据
    totalSales.value = '125680.50'
    totalOrders.value = 336
    totalUsers.value = 1
    totalBooks.value = 1167
  }
}

// 加载统计数据
const loadStatisticsData = async () => {
  try {
    // 加载统计摘要数据
    await loadStatisticsSummary()

    // 直接使用模拟数据，确保数据显示
    console.log('使用模拟数据进行展示')
    
    // 加载销售统计数据
    salesData.value = generateMockSalesData(salesPeriod.value)
    console.log('销售统计数据:', salesData.value)

    // 加载畅销书籍排行数据
    topBooksData.value = generateMockTopBooksData()
    console.log('畅销书籍排行数据:', topBooksData.value)

    // 加载分类销售比例数据
    categoryData.value = generateMockCategoryData()
    console.log('分类数据:', categoryData.value)

    // 加载订单状态分布数据
    orderStatusData.value = generateMockOrderStatusData()
    console.log('订单状态数据:', orderStatusData.value)

  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 初始化图表
const initCharts = async () => {
  if (activeMenu.value !== 'stats') return

  // 清空全局图表实例数组，避免重复添加
  chartInstances.length = 0

  // 显示加载中提示
  const loading = ElLoading.service({
    lock: true,
    text: '加载统计数据中...',
    background: 'rgba(255, 255, 255, 0.7)'
  })

  try {
    // 先加载统计数据
    await loadStatisticsData()
    await nextTick()
  } catch (error) {
    console.error('初始化图表失败:', error)
    ElMessage.error('初始化图表失败')
  } finally {
    // 关闭加载提示
    loading.close()
  }

  // 使用原生 DOM API 和脚本方式加载 ECharts
  if (!(window as any).echarts) {
    const script = document.createElement('script')
    script.src = 'https://cdn.jsdelivr.net/npm/echarts@5.4.3/dist/echarts.min.js'
    script.async = true
    document.head.appendChild(script)

    // 等待脚本加载完成
    await new Promise(resolve => {
      script.onload = resolve
    })
  }

  // 使用全局 echarts 对象
  const echarts = (window as any).echarts

  // 销售统计图表
  if (document.getElementById('sales-chart')) {
    const salesChartInstance = echarts.init(document.getElementById('sales-chart'), null, {
      renderer: 'canvas',
      useDirtyRect: false,
      devicePixelRatio: window.devicePixelRatio // 使用设备像素比，保持清晰度
    })

    // 将图表实例添加到全局数组中
    chartInstances.push(salesChartInstance)

    // 根据当前选择的周期显示对应的标题
    let periodText = '日'
    switch (salesPeriod.value) {
      case 'day': periodText = '日'; break;
      case 'week': periodText = '周'; break;
      case 'month': periodText = '月'; break;
      case 'year': periodText = '年'; break;
    }

    salesChartInstance.setOption({
      title: {
        text: `${periodText}销售额趋势`,
        left: 'center',
        top: 5, // 增加顶部距离，避免遮挡
        padding: [0, 0, 5, 0], // 保持内边距
        textStyle: {
          fontSize: 14 // 保持字体大小
        }
      },
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '15%',    // 保持左侧空间
        right: '5%',    // 保持右侧空间
        bottom: '25%',  // 增加底部空间，使图表底部往下拉
        top: '18%',     // 保持顶部空间
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: salesData.value.map(item => item.date),
        axisLabel: {
          interval: 'auto',
          rotate: salesPeriod.value === 'month' ? 45 : 0, // 保持旋转角度
          margin: 15, // 增加与轴线的距离，使标签更靠下
          fontSize: 10, // 保持字体大小
          align: 'center', // 保持居中对齐
          hideOverlap: true // 保持隐藏重叠的标签
        },
        axisTick: {
          length: 4, // 增加刻度线长度
          lineStyle: {
            width: 1 // 保持刻度线宽度
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '销售额 (元)',
        nameTextStyle: {
          padding: [0, 0, 0, 40], // 保持左侧内边距
          fontSize: 12 // 保持字体大小
        },
        axisLabel: {
          margin: 12, // 保持与轴线的距离
          formatter: '{value}', // 确保显示为整数
          fontSize: 11, // 保持字体大小
          align: 'right', // 右对齐，避免被遮挡
          padding: [0, 0, 0, 0] // 移除内边距
        },
        splitLine: {
          lineStyle: {
            color: '#eee' // 使分隔线颜色更浅
          }
        },
        offset: 0, // 减小Y轴与图表的距离
        position: 'left' // 确保Y轴在左侧
      },
      series: [{
        name: '销售额',
        type: 'line',
        data: salesData.value.map(item => item.value),
        smooth: true,
        lineStyle: {
          width: 3,
          color: '#409EFF'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(64, 158, 255, 0.5)'
            }, {
              offset: 1, color: 'rgba(64, 158, 255, 0.1)'
            }]
          }
        }
      }]
    })

    // 监听容器大小变化，自动调整图表大小
    const resizeObserver = new ResizeObserver(() => {
      salesChartInstance.resize()
    })
    const chartElement = document.getElementById('sales-chart')
    if (chartElement) {
      resizeObserver.observe(chartElement)
    }

    window.addEventListener('resize', () => {
      salesChartInstance.resize()
    })
  }

  // 畅销书籍排行图表
  if (document.getElementById('top-books-chart')) {
    const topBooksChartInstance = echarts.init(document.getElementById('top-books-chart'), null, {
      renderer: 'canvas',
      useDirtyRect: false,
      devicePixelRatio: window.devicePixelRatio // 使用设备像素比，保持清晰度
    })

    // 将图表实例添加到全局数组中
    chartInstances.push(topBooksChartInstance)

    topBooksChartInstance.setOption({
      tooltip: {
        trigger: 'item', // 改为item触发，减小高亮区域
        axisPointer: {
          type: 'none' // 禁用坐标轴指示器，减小高亮区域
        },
        formatter: '{b}: {c}', // 简化提示内容
        padding: 5, // 减小提示框内边距
        textStyle: {
          fontSize: 12 // 减小提示文字大小
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '10%',  // 增加底部空间，确保数字刻度显示正常
        top: '2%',      // 保持顶部空间
        containLabel: true
      },
      xAxis: {
        type: 'value',
        name: '', // 名称为空，不显示"销量"两个字
        nameLocation: 'middle', // 保持名称位置设置
        nameGap: 25, // 保持与轴线的距离设置
        minInterval: 1, // 最小间隔为1，确保刻度只显示整数
        max: function(value: any) {
          // 动态设置最大值，确保有合适的显示范围
          return Math.ceil(value.max * 1.1)
        },
        axisLabel: {
          formatter: '{value}', // 确保显示为整数
          fontSize: 11, // 保持字体大小
          margin: 8 // 保持与轴线的距离
        },
        nameTextStyle: {
          fontSize: 12, // 保持字体大小设置
          fontWeight: 'bold',
          padding: [5, 0, 0, 0], // 保持内边距设置
          align: 'center' // 保持对齐方式
        },
        splitLine: {
          show: true,
          lineStyle: {
            color: '#f0f0f0',
            type: 'dashed'
          }
        }
      },
      yAxis: {
        type: 'category',
        data: topBooksData.value.map(item => item.title),
        inverse: true, // 反转Y轴，使销量最高的书籍显示在最上方
        axisLabel: {
          width: 150, // 保持宽度，确保标签完整显示
          overflow: 'truncate',
          interval: 0, // 显示所有标签
          margin: 10, // 增加与轴线的距离
          fontSize: 12, // 保持字体大小
          lineHeight: 12, // 保持行高
          padding: [1, 0, 1, 0], // 保持上下内边距
          height: 14, // 保持标签高度
          fontWeight: 'bold' // 保持加粗显示
        },
        axisTick: {
          alignWithLabel: true, // 刻度线与标签对齐
          length: 4, // 保持刻度线长度
          lineStyle: {
            width: 1.5 // 保持刻度线宽度
          }
        },
        splitLine: {
          show: false // 不显示分隔线，使图表更紧凑
        },
        // 设置Y轴的间隔，减小类目高度
        axisPointer: {
          type: 'none' // 禁用坐标轴指示器，减小高亮区域
        }
      },
      series: [{
        name: '销量',
        type: 'bar',
        data: topBooksData.value.map(item => item.sales),
        barWidth: 28, // 进一步增加条形宽度，使柱子更粗
        barGap: '0%', // 减小条形之间的间距
        barCategoryGap: '5%', // 保持类目间距，使柱子之间非常靠近
        itemStyle: {
          color: '#67C23A',
          borderRadius: [0, 4, 4, 0] // 右侧添加圆角，使条形更美观
        },
        emphasis: {
          // 控制鼠标悬停时的高亮效果范围
          itemStyle: {
            // 保持与原来相同的颜色，只是稍微亮一点
            color: '#85ce61'
          },
          // 减小高亮区域
          scale: false // 禁用鼠标悬停时的缩放效果
        }
      }]
    })

    // 监听容器大小变化，自动调整图表大小
    const resizeObserver = new ResizeObserver(() => {
      topBooksChartInstance.resize({
        animation: {
          duration: 300 // 添加动画效果，使调整更平滑
        }
      })
    })
    const chartElement = document.getElementById('top-books-chart')
    if (chartElement) {
      resizeObserver.observe(chartElement)
    }

    // 监听窗口大小变化和缩放
    window.addEventListener('resize', () => {
      topBooksChartInstance.resize({
        animation: {
          duration: 300
        }
      })
    })

    // 监听浏览器缩放
    window.addEventListener('zoom', () => {
      topBooksChartInstance.resize({
        animation: {
          duration: 300
        }
      })
    })
  }

  // 分类销售比例图表
  if (document.getElementById('category-chart')) {
    const categoryChartInstance = echarts.init(document.getElementById('category-chart'), null, {
      renderer: 'canvas',
      useDirtyRect: false,
      devicePixelRatio: window.devicePixelRatio // 使用设备像素比，保持清晰度
    })

    // 将图表实例添加到全局数组中
    chartInstances.push(categoryChartInstance)

    // 确保数据有效
    if (!categoryData.value || categoryData.value.length === 0) {
      categoryData.value = [{ name: '暂无数据', value: 0 }]
    }

    categoryChartInstance.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c}本 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center',
        data: categoryData.value.map(item => item.name || '未分类')
      },
      series: [{
        name: '书籍数量',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['40%', '50%'], // 将饼图向左移动，为图例腾出空间
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%',
          position: 'outside'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true
        },
        data: categoryData.value.map(item => ({
          name: item.name || '未分类',
          value: item.value || 0
        }))
      }]
    })

    // 监听容器大小变化，自动调整图表大小
    const resizeObserver = new ResizeObserver(() => {
      categoryChartInstance.resize()
    })
    const chartElement = document.getElementById('category-chart')
    if (chartElement) {
      resizeObserver.observe(chartElement)
    }

    window.addEventListener('resize', () => {
      categoryChartInstance.resize()
    })
  }

  // 订单状态分布图表
  if (document.getElementById('order-status-chart')) {
    const orderStatusChartInstance = echarts.init(document.getElementById('order-status-chart'), null, {
      renderer: 'canvas',
      useDirtyRect: false,
      devicePixelRatio: window.devicePixelRatio // 使用设备像素比，保持清晰度
    })

    // 将图表实例添加到全局数组中
    chartInstances.push(orderStatusChartInstance)

    // 定义状态颜色
    const statusColors: Record<string, string> = {
      '待付款': '#E6A23C', // 黄色
      '已付款': '#409EFF', // 蓝色
      '已发货': '#67C23A', // 绿色
      '已完成': '#67C23A', // 绿色
      '已取消': '#F56C6C'  // 红色
    }

    // 使用完整的订单状态数据
    let filteredStatusData = orderStatusData.value

    // 确保数据有效
    if (!filteredStatusData || filteredStatusData.length === 0) {
      filteredStatusData = [
        { name: '待付款', value: 23 },
        { name: '已付款', value: 45 },
        { name: '已发货', value: 67 },
        { name: '已完成', value: 189 },
        { name: '已取消', value: 12 }
      ]
    }

    orderStatusChartInstance.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center',
        data: filteredStatusData.map(item => item.name || '未知状态')
      },
      series: [{
        name: '订单状态',
        type: 'pie',
        radius: '55%',
        center: ['40%', '50%'], // 将饼图向左移动，为图例腾出空间
        data: filteredStatusData.map(item => ({
          name: item.name || '未知状态',
          value: item.value || 0,
          itemStyle: {
            color: statusColors[item.name] || '#409EFF' // 使用预定义颜色或默认蓝色
          }
        })),
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })

    // 监听容器大小变化，自动调整图表大小
    const resizeObserver = new ResizeObserver(() => {
      orderStatusChartInstance.resize()
    })
    const chartElement = document.getElementById('order-status-chart')
    if (chartElement) {
      resizeObserver.observe(chartElement)
    }

    window.addEventListener('resize', () => {
      orderStatusChartInstance.resize()
    })
  }
}

// 切换销售周期
const changeSalesPeriod = (period: string) => {
  salesPeriod.value = period
  initCharts()
}

// 全局图表实例数组，用于统一管理
const chartInstances: any[] = []

// 全局图表重绘函数
const resizeAllCharts = () => {
  chartInstances.forEach(chart => {
    if (chart && typeof chart.resize === 'function') {
      chart.resize({
        animation: {
          duration: 300
        }
      })
    }
  })
}

// 监听浏览器缩放和窗口大小变化
onMounted(() => {
  window.addEventListener('resize', resizeAllCharts)
  // 监听浏览器缩放级别变化
  window.matchMedia('(resolution: 1dppx)').addEventListener('change', resizeAllCharts)
})

// 组件卸载时移除监听器
onUnmounted(() => {
  window.removeEventListener('resize', resizeAllCharts)
  window.matchMedia('(resolution: 1dppx)').removeEventListener('change', resizeAllCharts)
})



// 监听菜单切换，初始化图表并更新URL
watch(activeMenu, (newValue) => {
  if (newValue === 'stats') {
    initCharts()
  }
})

// 监听URL查询参数变化
watch(() => route.query.activeMenu, (newActiveMenu) => {
  if (newActiveMenu && ['books', 'users', 'orders', 'stats'].includes(newActiveMenu as string)) {
    activeMenu.value = newActiveMenu as string
  }
}, { immediate: true })

// 监听销售周期变化，更新销售图表
watch(salesPeriod, () => {
  initCharts()
})
</script>

<style scoped>
.admin-container {
  height: calc(100vh - 60px);
  margin-top: 0;
}

.admin-container .el-container {
  height: 100%;
}

.custom-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #ebeef5;
  margin-top: 20px;
}

.custom-table th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: bold;
  text-align: center;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
  font-size: 16px;
}

.custom-table td {
  padding: 12px 0;
  text-align: center;
  border-bottom: 1px solid #ebeef5;
  font-size: 16px;
}

.custom-table tr:hover {
  background-color: #f5f7fa;
}






.admin-menu {
  height: auto; /* 改为自适应高度 */
  border-right: none;
}



.el-aside {
  background-color: #545c64;
  color: white;
  overflow: hidden; /* 防止滚动条出现 */
}

.el-menu {
  background-color: #545c64;
}

.el-menu-item {
  color: #fff;
  height: auto;
  line-height: 50px; /* 确保每个菜单项高度一致 */
}

.el-menu-item:hover, .el-menu-item.is-active {
  background-color: #434a50;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  justify-content: space-between;
}

.left-actions {
  display: flex;
  align-items: center;
  gap: 0px;
  flex-wrap: wrap;
}

.selection-info {
  margin-left: 10px;
  background-color: #f0f9eb;
  color: #67c23a;
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
}

.search-container {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-top: 8px;
  margin-bottom: 8px;
}

/* 订单搜索容器样式 */
.action-bar .search-container {
  flex: 1;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-right: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.book-cover {
  width: 50px;
  height: 70px;
  object-fit: cover;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-card {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.dashboard-summary {
  margin-top: 30px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.dashboard-summary h3 {
  margin-top: 0;
  margin-bottom: 16px;
}

.dashboard-summary p {
  margin: 8px 0;
  color: #606266;
}

/* 图书表单样式 */
.book-form {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding-left: 10px;
  width: 100%;
}

.shorter-input {
  width: 160px !important;
}

.description-input {
  width: 280px !important;
}

/* 确保表单项靠左对齐 */
.el-form-item {
  width: 100%;
  margin-right: 0;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
}

.el-form-item__content {
  margin-left: 0 !important;
  display: flex;
  justify-content: flex-start;
}

/* 覆盖Element Plus的默认样式 */
.el-dialog__body {
  padding-left: 10px;
  padding-right: 10px;
}

/* 增大表格字体 */
.larger-font-table {
  font-size: 16px;
}

.larger-font-table th,
.larger-font-table td {
  font-size: 16px !important;
}

/* 保留字体大小设置，移除邮箱列宽度限制 */

/* 表格行样式 */
.larger-font-table .el-table__row {
  border-bottom: 1px solid #ebeef5;
}

.larger-font-table .el-table__row:hover {
  background-color: #f5f7fa;
}

/* 表头底部边框 */
.larger-font-table .el-table__header-wrapper th {
  border-bottom: 2px solid #dcdfe6;
}

/* 数据统计页面样式 */
.stats-container {
  padding: 20px;
}



.stats-cards {
  display: flex;
  justify-content: space-between;
  margin-bottom: 30px;
}

.stats-card {
  flex: 1;
  margin: 0 10px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stats-card:first-child {
  margin-left: 0;
}

.stats-card:last-child {
  margin-right: 0;
}

.stats-card-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stats-card-title {
  font-size: 16px;
  color: #606266;
}

.stats-charts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 20px;
  margin-bottom: 20px;
  width: 100%;
  height: auto; /* 改为自适应高度 */
  min-height: 500px; /* 减小最小高度，使整体更紧凑 */
  aspect-ratio: 2 / 1; /* 保持整体2:1的宽高比 */
}

.stats-chart-container {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 2px 15px 10px 15px; /* 进一步减小上边距 */
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden; /* 防止内容溢出 */
  position: relative; /* 为绝对定位提供参考 */
}

.stats-chart-container h3 {
  margin-top: 0;
  margin-bottom: 3px; /* 进一步减小标题下方的边距 */
  font-size: 16px; /* 保持标题字体大小 */
  color: #303133;
  text-align: center;
  font-weight: bold; /* 加粗标题 */
}

.chart {
  flex: 1;
  width: 100%;
  min-height: 300px; /* 增加最小高度，使折线图更高 */
  position: relative; /* 为绝对定位提供参考 */
  aspect-ratio: 16 / 9; /* 保持16:9的宽高比 */
}

.chart-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 8px; /* 减小底部边距 */
  margin-top: 0; /* 移除顶部边距 */
}

.chart-tabs span {
  padding: 3px 15px; /* 减小上下内边距 */
  margin: 0 5px;
  cursor: pointer;
  border-radius: 4px;
  font-size: 13px; /* 减小字体大小 */
  color: #606266;
}

.chart-tabs span.active {
  background-color: #409EFF;
  color: #fff;
}

/* 分页容器样式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
