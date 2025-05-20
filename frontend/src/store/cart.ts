import { defineStore } from 'pinia'
import axios from 'axios'
import { useUserStore } from './user'

interface Book {
  id: number;
  title: string;
  author: string;
  price: number;
  stock?: number;
  category?: string;
  description?: string;
}

interface CartItem {
  id?: number;
  book: Book;
  quantity: number;
  userId?: number;
}

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [] as CartItem[],
    loading: false,
    error: null as string | null
  }),

  getters: {
    totalItems: (state) => {
      return state.items.reduce((total, item) => total + item.quantity, 0)
    },

    totalPrice: (state) => {
      return state.items.reduce((total, item) => {
        return total + (item.book.price * item.quantity)
      }, 0)
    }
  },

  actions: {
    // 从后端加载购物车
    async fetchCart() {
      const userStore = useUserStore()
      if (!userStore.isLoggedIn) return

      this.loading = true
      this.error = null

      try {
        const response = await axios.get(`http://localhost:8080/api/cart/user/${userStore.user?.id}`)
        this.items = response.data
        this.saveToLocalStorage() // 同步到本地存储
      } catch (error: any) {
        console.error('获取购物车失败:', error)
        this.error = error.response?.data?.message || '获取购物车失败'
        // 如果获取失败，尝试从本地存储加载
        this.loadFromLocalStorage()
      } finally {
        this.loading = false
      }
    },

    // 添加商品到购物车
    async addToCart(book: Book, quantity: number = 1) {
      const userStore = useUserStore()

      // 先在本地更新
      const existingItem = this.items.find(item => item.book.id === book.id)
      if (existingItem) {
        existingItem.quantity += quantity
      } else {
        this.items.push({ book, quantity })
      }

      // 保存到本地存储
      this.saveToLocalStorage()

      // 如果用户已登录，同步到后端
      if (userStore.isLoggedIn) {
        try {
          await axios.post('http://localhost:8080/api/cart/add', {
            userId: userStore.user?.id,
            bookId: book.id,
            quantity: quantity
          })
        } catch (error: any) {
          console.error('添加到购物车失败:', error)
          this.error = error.response?.data?.message || '添加到购物车失败'
        }
      }
    },

    // 从购物车中移除商品
    async removeFromCart(bookId: number) {
      const userStore = useUserStore()

      // 先在本地更新
      const index = this.items.findIndex(item => item.book.id === bookId)
      if (index !== -1) {
        this.items.splice(index, 1)
        this.saveToLocalStorage()
      }

      // 如果用户已登录，同步到后端
      if (userStore.isLoggedIn) {
        try {
          await axios.delete(`http://localhost:8080/api/cart/user/${userStore.user?.id}/book/${bookId}`)
        } catch (error: any) {
          console.error('从购物车移除失败:', error)
          this.error = error.response?.data?.message || '从购物车移除失败'
        }
      }
    },

    // 更新购物车项数量
    async updateQuantity(bookId: number, quantity: number) {
      const userStore = useUserStore()

      // 先在本地更新
      const item = this.items.find(item => item.book.id === bookId)
      if (item) {
        item.quantity = quantity
        this.saveToLocalStorage()
      }

      // 如果用户已登录，同步到后端
      if (userStore.isLoggedIn) {
        try {
          await axios.put(`http://localhost:8080/api/cart/user/${userStore.user?.id}/book/${bookId}`, {
            quantity: quantity
          })
        } catch (error: any) {
          console.error('更新购物车失败:', error)
          this.error = error.response?.data?.message || '更新购物车失败'
        }
      }
    },

    // 清空购物车
    async clearCart() {
      const userStore = useUserStore()

      // 先在本地更新
      this.items = []
      this.saveToLocalStorage()

      // 如果用户已登录，同步到后端
      if (userStore.isLoggedIn) {
        try {
          await axios.delete(`http://localhost:8080/api/cart/user/${userStore.user?.id}/clear`)
        } catch (error: any) {
          console.error('清空购物车失败:', error)
          this.error = error.response?.data?.message || '清空购物车失败'
        }
      }
    },

    // 将购物车同步到后端
    async syncCartToServer() {
      const userStore = useUserStore()
      if (!userStore.isLoggedIn || this.items.length === 0) return

      try {
        // 先清空服务器上的购物车
        await axios.delete(`http://localhost:8080/api/cart/user/${userStore.user?.id}/clear`)

        // 然后添加本地购物车中的所有商品
        for (const item of this.items) {
          await axios.post('http://localhost:8080/api/cart/add', {
            userId: userStore.user?.id,
            bookId: item.book.id,
            quantity: item.quantity
          })
        }
      } catch (error: any) {
        console.error('同步购物车失败:', error)
        this.error = error.response?.data?.message || '同步购物车失败'
      }
    },

    // 保存到本地存储
    saveToLocalStorage() {
      localStorage.setItem('cart', JSON.stringify(this.items))
    },

    // 从本地存储加载
    loadFromLocalStorage() {
      const cartStr = localStorage.getItem('cart')
      if (cartStr) {
        try {
          this.items = JSON.parse(cartStr)
        } catch (e) {
          console.error('解析购物车数据失败:', e)
          this.items = []
        }
      }
    }
  }
})