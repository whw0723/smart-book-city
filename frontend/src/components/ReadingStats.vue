<template>
  <div class="reading-stats">
    <!-- 统计卡片 -->
    <div class="stat-cards">
      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <span>购买图书总数</span>
          </div>
        </template>
        <div class="card-content">
          <span class="stat-number">{{ stats.totalBooks }}</span>
          <span class="stat-unit">本</span>
        </div>
      </el-card>

      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <span>总消费金额</span>
          </div>
        </template>
        <div class="card-content">
          <span class="stat-number">¥{{ stats.totalSpent.toFixed(2) }}</span>
        </div>
      </el-card>

      <el-card class="stat-card">
        <template #header>
          <div class="card-header">
            <span>本月购书数</span>
          </div>
        </template>
        <div class="card-content">
          <span class="stat-number">{{ stats.booksThisMonth }}</span>
          <span class="stat-unit">本</span>
        </div>
      </el-card>
    </div>

    <!-- 阅读偏好分析 -->
    <el-card class="preference-card">
      <template #header>
        <div class="card-header">
          <span>阅读偏好分析</span>
        </div>
      </template>
      <div class="chart-container" ref="chartContainer">
        <div id="categoryChart" ref="categoryChartRef" style="width: 100%; height: 400px; min-height: 300px;"></div>
      </div>
    </el-card>

    <!-- 最近购买的图书 -->
    <el-card class="recent-books">
      <template #header>
        <div class="card-header">
          <span>最近购买的图书</span>
        </div>
      </template>
      <el-table :data="recentBooks" style="width: 100%">
          <el-table-column prop="title" label="书名"></el-table-column>
          <el-table-column prop="author" label="作者" width="150"></el-table-column>
          <el-table-column label="分类" width="120">
            <template #default="scope">
              {{ getCategoryName(scope.row.category) }}
            </template>
          </el-table-column>
          <el-table-column prop="purchaseDate" label="购买日期" width="180"></el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="text" @click="viewBookDetail(scope.row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import axios from '../utils/axios'
import * as echarts from 'echarts'

const router = useRouter()
const userStore = useUserStore()
const chartContainer = ref(null)
const categoryChartRef = ref(null)
let categoryChart: any = null

// 定义类型
interface Book {
  id: number
  title: string
  author: string
  category: string
  purchaseDate: string
}

interface CategoryItem {
  name: string
  value: number
}

// 统计数据
const stats = ref({
  totalBooks: 0,
  totalSpent: 0,
  booksThisMonth: 0
})

// 最近购买的图书
const recentBooks = ref<Book[]>([])

// 分类分布数据
const categoryDistribution = ref<CategoryItem[]>([])

// ResizeObserver引用
let resizeObserver: ResizeObserver | null = null

// 图书分类映射 - 从Home.vue复制并扩展
const categoryMap: Record<string, string> = {
  // 文学小说类
  'novel': '文学小说',
  'fiction': '文学小说',
  'classical': '文学小说',
  'foreign': '外国文学',
  'foreignliterature': '外国文学',
  'chinese': '文学小说',
  'ancient': '文学小说',
  // 科技类
  'technology': '科技计算机',
  'computer': '科技计算机',
  'it': '科技计算机',
  'science': '科技计算机',
  'tech': '科技计算机',
  // 社科类
  'education': '社会科学',
  'society': '社会科学',
  'philosophy': '社会科学',
  'psychology': '社会科学',
  'politics': '社会科学',
  // 经管类
  'economics': '经济管理',
  'management': '经济管理',
  'business': '经济管理',
  'finance': '经济管理',
  // 历史传记类
  'history': '历史传记',
  'biography': '历史传记',
  'memoir': '历史传记',
  // 艺术类
  'art': '艺术设计',
  'design': '艺术设计',
  'music': '艺术设计',
  'painting': '艺术设计',
  // 儿童类
  'children': '儿童读物',
  'kids': '儿童读物',
  'juvenile': '儿童读物',
  // 其他
  'scifi': '科幻',
  'fantasy': '科幻',
  'romance': '文学小说',
  'mystery': '文学小说',
  'thriller': '文学小说'
}

// 根据分类值获取中文名称，修复转换逻辑
const getCategoryName = (category: string | null | undefined): string => {
  // 处理空值情况
  if (!category) {
    return '未知分类'
  }

  // 清理分类字符串，去除多余的空格、换行符等
  const cleanCategory = category.trim().replace(/\s+/g, '')
  
  // 转换为小写
  const lowerCategory = cleanCategory.toLowerCase()
  
  // 调试信息
  console.log('原始分类:', category, '清理后:', cleanCategory, '小写:', lowerCategory)

  // 直接查找映射
  if (categoryMap[lowerCategory]) {
    return categoryMap[lowerCategory]
  }

  // 如果没有直接匹配，尝试更宽松的匹配
  for (const key in categoryMap) {
    if (lowerCategory.includes(key.toLowerCase())) {
      return categoryMap[key]
    }
  }

  // 特殊处理：如果分类已经是中文，直接返回
  if (/[\u4e00-\u9fa5]/.test(category)) {
    return category
  }

  // 默认返回值，确保函数总是返回string类型
  return '未知分类'
}

// 获取阅读统计数据
const fetchReadingStats = async () => {
  try {
    // 调用后端API，添加空值检查
    if (!userStore.user) {
      console.error('用户未登录，无法获取阅读统计数据')
      return
    }
    const response = await axios.get(`http://localhost:8080/api/reading-stats/user/${userStore.user.id}`)

    // 更新统计数据
    stats.value = response.data.stats

    // 更新最近购买的图书
    recentBooks.value = response.data.recentBooks

    // 更新分类分布数据，转换为中文分类名称
    console.log('后端返回的分类数据:', response.data.categoryDistribution)
    categoryDistribution.value = response.data.categoryDistribution.map((item: any) => ({
      name: getCategoryName(item.name),
      value: item.value
    }))

    // 初始化图表
    initCategoryChart()
  } catch (error) {
    console.error('获取阅读统计数据失败:', error)
    // 如果API调用失败，使用模拟数据
    stats.value = {
      totalBooks: 12,
      totalSpent: 856.50,
      booksThisMonth: 3
    }

    recentBooks.value = [
      {
        id: 1,
        title: '红楼梦',
        author: '曹雪芹',
        category: '古典文学',
        purchaseDate: '2025-04-29'
      },
      {
        id: 2,
        title: '平凡的世界',
        author: '路遥',
        category: '当代文学',
        purchaseDate: '2025-04-27'
      },
      {
        id: 3,
        title: '飘',
        author: '玛格丽特·米切尔',
        category: '外国文学',
        purchaseDate: '2025-04-24'
      },
      {
        id: 4,
        title: '人类简史',
        author: '尤瓦尔·赫拉利',
        category: '历史',
        purchaseDate: '2025-04-20'
      },
      {
        id: 5,
        title: '三体',
        author: '刘慈欣',
        category: '科幻',
        purchaseDate: '2025-04-15'
      }
    ]

    categoryDistribution.value = [
      { name: '文学小说', value: 5 },
      { name: '科技计算机', value: 2 },
      { name: '历史传记', value: 2 },
      { name: '科幻', value: 1 },
      { name: '外国文学', value: 2 }
    ]

    // 初始化图表
    initCategoryChart()
  }
}

// 初始化分类分布图表
const initCategoryChart = async () => {
  try {
    // 等待DOM更新完成
    await nextTick()

    // 检查是否有数据
    if (!categoryDistribution.value || categoryDistribution.value.length === 0) {
      console.warn('没有分类数据可供显示')
      // 添加一些默认数据，以便显示"暂无数据"
      categoryDistribution.value = [{ name: '暂无数据', value: 1 }]
    }

    // 确保DOM元素已经渲染
    await new Promise(resolve => setTimeout(resolve, 100))

    // 使用ref获取DOM元素
    const chartDom = categoryChartRef.value
    if (!chartDom) {
      console.error('找不到图表DOM元素')
      return
    }

    // 如果图表已经存在，先销毁
    if (categoryChart) {
      categoryChart.dispose()
      categoryChart = null
    }

    // 创建新图表
    categoryChart = echarts.init(chartDom, null, {
      renderer: 'canvas',
      useDirtyRect: false,
      devicePixelRatio: window.devicePixelRatio // 使用设备像素比，保持清晰度
    })

    // 打印一下数据，便于调试
    console.log('最终分类数据:', categoryDistribution.value)

    const option = {
      title: {
        text: '图书分类分布',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center',
        data: categoryDistribution.value.map(item => item.name)
      },
      series: [
        {
          name: '购书分类',
          type: 'pie',
          radius: categoryDistribution.value.length === 1 && categoryDistribution.value[0].name === '暂无数据'
            ? '50%' // 如果只有"暂无数据"，使用普通饼图
            : ['40%', '70%'], // 否则使用环形图
          center: ['40%', '50%'], // 将饼图向左移动，为图例腾出空间
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}: {c} ({d}%)'
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
          data: categoryDistribution.value.map(item => ({
            value: item.value,
            name: item.name
          }))
        }
      ]
    }

    // 设置图表选项
    categoryChart.setOption(option)

    // 监听窗口大小变化，调整图表大小
    window.addEventListener('resize', resizeChart)

    // 创建ResizeObserver监听容器大小变化
    resizeObserver = new ResizeObserver(() => {
      if (categoryChart) {
        categoryChart.resize()
      }
    })

    // 监听图表容器大小变化
    if (chartContainer.value) {
      resizeObserver.observe(chartContainer.value)
    }

    // 手动触发一次resize以确保图表正确渲染
    setTimeout(() => {
      if (categoryChart) {
        categoryChart.resize()
      }
    }, 200)

  } catch (error) {
    console.error('初始化图表失败:', error)
  }
}

// 调整图表大小
const resizeChart = () => {
  if (categoryChart) {
    categoryChart.resize()
  }
}

// 查看图书详情
const viewBookDetail = (bookId: number) => {
  router.push(`/book/${bookId}`)
}

onMounted(() => {
  fetchReadingStats()
})

// 组件卸载时销毁图表实例和清理资源
onUnmounted(() => {
  // 销毁图表实例
  if (categoryChart) {
    categoryChart.dispose()
    categoryChart = null
  }

  // 移除事件监听器
  window.removeEventListener('resize', resizeChart)

  // 清理ResizeObserver（如果存在）
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
})
</script>

<style scoped>
.reading-stats {
  padding: 10px 0;
  width: 100%;
  max-width: 100%;
}

.stat-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 200px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  display: flex;
  justify-content: center;
  align-items: baseline;
  padding: 20px 0;
}

.stat-number {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
}

.stat-unit {
  font-size: 16px;
  margin-left: 5px;
  color: #606266;
}

.preference-card, .recent-books {
  margin-bottom: 20px;
}

.chart-container {
  height: 400px;
  min-height: 300px;
  margin: 10px 0;
  width: 100%;
  max-width: 100%;
  position: relative;
  overflow: visible;
}

/* 确保图表在小屏幕上也能正常显示 */
@media (max-width: 768px) {
  .chart-container {
    height: 350px;
  }
}

/* 确保图表容器内的元素可见 */
#categoryChart {
  position: relative;
  z-index: 1;
}
</style>