import { defineStore } from 'pinia'
import axios from 'axios'
import { useUserStore } from './user'

export const useWalletStore = defineStore('wallet', {
  state: () => ({
    balance: 0,
    loading: false,
    error: null as string | null
  }),

  getters: {
    hasBalance: (state) => state.balance > 0
  },

  actions: {
    async fetchWallet() {
      const userStore = useUserStore()
      if (!userStore.isLoggedIn) return

      this.loading = true
      this.error = null

      try {
        const response = await axios.get(`/wallet/${userStore.user?.id}`)
        this.balance = response.data.balance
        userStore.updateUserBalance(response.data.balance)
      } catch (error: any) {
        console.error('获取余额失败:', error)
        this.error = error.response?.data || '获取余额失败'
      } finally {
        this.loading = false
      }
    },

    async deposit(amount: number) {
      const userStore = useUserStore()
      if (!userStore.isLoggedIn) return

      this.loading = true
      this.error = null

      try {
        const response = await axios.post('/wallet/deposit', {
          userId: userStore.user?.id,
          amount
        })

        // 确保余额更新为最新值
        this.balance = response.data.balance
        userStore.updateUserBalance(response.data.balance)

        return response.data
      } catch (error: any) {
        console.error('充值失败:', error)
        this.error = error.response?.data || '充值失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    async withdraw(amount: number) {
      const userStore = useUserStore()
      if (!userStore.isLoggedIn) return

      this.loading = true
      this.error = null

      try {
        const response = await axios.post('/wallet/withdraw', {
          userId: userStore.user?.id,
          amount
        })

        // 确保余额更新为最新值
        this.balance = response.data.balance
        userStore.updateUserBalance(response.data.balance)

        return response.data
      } catch (error: any) {
        console.error('提现失败:', error)
        this.error = error.response?.data || '提现失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    async payOrder(orderId: number) {
      const userStore = useUserStore()
      if (!userStore.isLoggedIn) return

      this.loading = true
      this.error = null

      try {
        const response = await axios.post('/wallet/pay', {
          userId: userStore.user?.id,
          orderId
        })

        // 更新余额
        this.balance = response.data.balance
        // 同步更新用户Store中的余额
        userStore.updateUserBalance(response.data.balance)

        return response.data
      } catch (error: any) {
        console.error('支付失败:', error)
        this.error = error.response?.data || '支付失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    async refundOrder(orderId: number) {
      const userStore = useUserStore()
      this.loading = true
      this.error = null

      try {
        const response = await axios.post('/wallet/refund', {
          orderId
        })

        // 更新余额
        this.balance = response.data.balance
        // 同步更新用户Store中的余额
        userStore.updateUserBalance(response.data.balance)

        return response.data
      } catch (error: any) {
        console.error('退款失败:', error)
        this.error = error.response?.data || '退款失败'
        throw error
      } finally {
        this.loading = false
      }
    },

    async batchPayOrders(orderIds: number[]) {
      const userStore = useUserStore()
      if (!userStore.isLoggedIn) return

      this.loading = true
      this.error = null

      try {
        const response = await axios.post('/wallet/batch-pay', {
          userId: userStore.user?.id,
          orderIds
        })

        // 更新余额
        this.balance = response.data.balance
        // 同步更新用户Store中的余额
        userStore.updateUserBalance(response.data.balance)

        return response.data
      } catch (error: any) {
        console.error('批量支付失败:', error)
        this.error = error.response?.data || '批量支付失败'
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})
