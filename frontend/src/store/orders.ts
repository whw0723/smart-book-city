import { defineStore } from 'pinia'
import axios from 'axios'
import { useUserStore } from './user'

export const useOrdersStore = defineStore('orders', {
  state: () => {
    return {
      pendingOrdersCount: 0, // 待支付订单数量
      lastUpdated: 0 // 最后更新时间，用于防抖
    }
  },

  actions: {
    // 更新待支付订单数量
    async updatePendingOrdersCount() {
      const userStore = useUserStore()
      if (!userStore.isLoggedIn || !userStore.user) return

      try {
        const response = await axios.get(`/orders/user/${userStore.user.id}`)
        
        if (response.data && Array.isArray(response.data)) {
          // 计算待支付订单数量
          const pendingOrders = response.data.filter(order => order.status === 0)
          this.pendingOrdersCount = pendingOrders.length
          this.lastUpdated = Date.now()
        }
      } catch (error) {
        console.error('获取待支付订单数量失败:', error)
      }
    },

    // 重置待支付订单数量
    resetPendingOrdersCount() {
      this.pendingOrdersCount = 0
      this.lastUpdated = Date.now()
    },

    // 防抖更新，避免频繁请求
    debouncedUpdatePendingOrdersCount() {
      const now = Date.now()
      // 如果距离上次更新超过5秒，则更新
      if (now - this.lastUpdated > 5000) {
        this.updatePendingOrdersCount()
      }
    }
  }
})