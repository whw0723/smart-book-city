import { defineStore } from 'pinia'
import axios from '../utils/axios'

// 导出 Book 接口
export interface Book {
  id: number;
  title: string;
  author: string;
  price: number;
  stock: number;
  category: string;
  description: string;
}

// 分页数据接口
export interface PagedData {
  books: Book[];
  total: number;
  page: number;
  size: number;
  pages: number;
}

// API响应接口
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  code: number;
  data: T;
  timestamp: string;
}

export const useBookStore = defineStore('books', {
  state: () => ({
    books: [] as Book[],
    currentBook: null as Book | null,
    loading: false,
    error: null as string | null,
    // 分页相关状态
    currentPage: 1,
    pageSize: 20,
    totalBooks: 0,
    totalPages: 0,
    // 新增：存储分类列表
    categories: [] as string[],
  }),

  actions: {
    async fetchAllBooks() {
      this.loading = true
      this.error = null
      console.log('开始获取所有图书')

      try {
        // 尝试从后端API获取数据
        console.log('正在从API获取数据...')
        const response = await axios.get('/books', { timeout: 5000 })
        console.log('后端API返回数据:', response.data)

        // 处理新的ApiResponse格式
        if (response.data && response.data.success && Array.isArray(response.data.data)) {
          this.books = response.data.data
          console.log(`成功获取${this.books.length}本图书`)
        } else {
          console.warn('后端返回的数据格式不正确')
          this.books = []
          this.error = response.data?.message || '从后端加载数据失败，请稍后再试'
        }
      } catch (error: any) {
        console.error('获取图书列表失败:', error.message)
        console.log('获取数据失败')
        this.books = []
        this.error = '从后端加载数据失败，请稍后再试'
      } finally {
        this.loading = false
        console.log('图书获取完成，当前图书数量:', this.books.length)
      }
    },

    async fetchPagedBooks(page: number = 1, size: number = 20) {
      this.loading = true
      this.error = null
      console.log(`开始获取第${page}页图书，每页${size}本`)

      try {
        const response = await axios.get(`/books/paged?page=${page}&size=${size}`, { timeout: 5000 })
        console.log('后端API返回分页数据:', response.data)

        // 处理新的ApiResponse格式
        if (response.data && response.data.success && response.data.data && response.data.data.books) {
          const pagedData = response.data.data as PagedData
          this.books = pagedData.books
          this.currentPage = pagedData.page
          this.pageSize = pagedData.size
          this.totalBooks = pagedData.total
          this.totalPages = pagedData.pages
          console.log(`成功获取第${page}页图书，共${pagedData.total}本，${pagedData.pages}页`)
        } else {
          console.warn('后端返回的分页数据格式不正确')
          this.books = []
          this.error = response.data?.message || '从后端加载数据失败，请稍后再试'
        }
      } catch (error: any) {
        console.error('获取分页图书列表失败:', error.message)
        this.books = []
        this.error = '从后端加载数据失败，请稍后再试'
      } finally {
        this.loading = false
      }
    },

    async fetchBookById(id: number) {
      this.loading = true
      this.error = null

      try {
        const response = await axios.get(`/books/${id}`, { timeout: 5000 })
        // 处理新的ApiResponse格式
        if (response.data && response.data.success && response.data.data) {
          this.currentBook = response.data.data
        } else {
          this.currentBook = null
          this.error = response.data?.message || '图书不存在'
        }
      } catch (error) {
        console.error('获取图书详情失败:', error)
        this.currentBook = null
        this.error = '从后端加载数据失败，请稍后再试'
      } finally {
        this.loading = false
      }
    },

    async searchBooks(title?: string, author?: string) {
      this.loading = true
      this.error = null

      try {
        let url = '/books/search?'

        if (title) {
          url += `title=${encodeURIComponent(title)}`
        }

        if (author) {
          if (title) url += '&'
          url += `author=${encodeURIComponent(author)}`
        }

        const response = await axios.get(url, { timeout: 5000 })
        // 处理新的ApiResponse格式
        if (response.data && response.data.success && Array.isArray(response.data.data)) {
          this.books = response.data.data
        } else {
          this.books = []
          this.error = response.data?.message || '从后端加载数据失败，请稍后再试'
        }
      } catch (error) {
        console.error('搜索图书失败:', error)
        this.books = []
        this.error = '从后端加载数据失败，请稍后再试'
      } finally {
        this.loading = false
      }
    },

    async fetchBooksByCategory(category: string) {
      this.loading = true
      this.error = null

      try {
        const response = await axios.get(`/books/category/${category}`, { timeout: 5000 })
        // 处理新的ApiResponse格式
        if (response.data && response.data.success && Array.isArray(response.data.data)) {
          this.books = response.data.data
        } else {
          this.books = []
          this.error = response.data?.message || '从后端加载数据失败，请稍后再试'
        }
      } catch (error) {
        console.error('获取分类图书失败:', error)
        this.books = []
        this.error = '从后端加载数据失败，请稍后再试'
      } finally {
        this.loading = false
      }
    },

    async fetchBooksByCategoryPaged(category: string, page: number = 1, size: number = 20) {
      this.loading = true
      this.error = null
      console.log(`开始获取${category}分类第${page}页图书，每页${size}本`)

      try {
        const response = await axios.get(`/books/category/${category}/paged?page=${page}&size=${size}`, { timeout: 5000 })
        console.log('后端API返回分类分页数据:', response.data)

        // 处理新的ApiResponse格式
        if (response.data && response.data.success && response.data.data && response.data.data.books) {
          const pagedData = response.data.data as PagedData
          this.books = pagedData.books
          this.currentPage = pagedData.page
          this.pageSize = pagedData.size
          this.totalBooks = pagedData.total
          this.totalPages = pagedData.pages
          console.log(`成功获取${category}分类第${page}页图书，共${pagedData.total}本，${pagedData.pages}页`)
        } else {
          console.warn('后端返回的分类分页数据格式不正确')
          this.books = []
          this.error = response.data?.message || '从后端加载数据失败，请稍后再试'
        }
      } catch (error: any) {
        console.error('获取分类分页图书列表失败:', error.message)
        this.books = []
        this.error = '从后端加载数据失败，请稍后再试'
      } finally {
        this.loading = false
      }
    },

    // 获取所有分类
    async fetchAllCategories() {
      // 避免重复加载，如果已有数据则不请求
      if (this.categories.length > 0) {
          console.log('分类已加载，跳过请求');
          return;
      }
      // 简单起见，这里不设置 loading 状态，通常很快
      this.error = null
      console.log('开始获取所有分类');
      try {
        const response = await axios.get('/books/categories', { timeout: 3000 });
        // 处理新的ApiResponse格式（如果接口已更新）
        const categoriesData = response.data.success ? response.data.data : response.data;
        if (categoriesData && Array.isArray(categoriesData)) {
          // 过滤掉空分类
          this.categories = categoriesData.filter(category => category && category.trim() !== '');
          console.log('成功获取分类:', this.categories);
        } else {
          console.warn('分类接口返回数据格式不正确');
          this.categories = [];
        }
      } catch (error: any) {
        console.error('获取分类列表失败:', error.message);
        this.categories = [];
        // 可以选择性地设置错误状态
        // this.error = '获取分类失败';
      }
    }
  }
})