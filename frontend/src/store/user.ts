import { defineStore } from 'pinia'
import axios from 'axios'

interface User {
  id: number;
  username: string;
  email?: string;
  role: number;
  balance?: number;
}

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null as User | null,
    isLoggedIn: false,
    isAdmin: false
  }),

  actions: {
    async login(username: string, password: string, loginType: string = 'user') {
      try {
        // 根据登录类型选择不同的API端点
        const endpoint = loginType === 'admin'
          ? 'http://localhost:8080/api/admin/login'
          : 'http://localhost:8080/api/users/login';

        const response = await axios.post(endpoint, {
          username,
          password
        })

        // 分别处理不同登录类型的响应格式
        if (loginType === 'admin') {
          // 管理员登录返回格式：{success: boolean, admin: object, message?: string}
          if (response.data && response.data.success && response.data.admin) {
            this.user = response.data.admin;
            this.isLoggedIn = true;
            this.isAdmin = true;
            localStorage.setItem('user', JSON.stringify(response.data.admin));
            localStorage.setItem('userType', loginType);
            return { success: true };
          } else {
            return {
              success: false,
              message: response.data?.message || '管理员登录失败，请检查用户名和密码'
            };
          }
        } else {
          // 普通用户登录返回格式：直接返回用户对象
          if (response.data && (response.data.id || response.data.username)) {
            this.user = response.data;
            this.isLoggedIn = true;
            this.isAdmin = false;
            localStorage.setItem('user', JSON.stringify(response.data));
            localStorage.setItem('userType', loginType);
            return { success: true };
          } else {
            return {
              success: false,
              message: response.data?.message || '登录失败，请检查用户名和密码'
            };
          }
        }

      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || '登录失败，请稍后再试'
        }
      }
    },

    async register(username: string, password: string, email: string) {
      try {
        const response = await axios.post('http://localhost:8080/api/users/register', {
          username,
          password,
          email,
          role: 0 // 默认注册为普通用户
        })

        if (response.data) {
          return { success: true };
        } else {
          return {
            success: false,
            message: '注册失败，请稍后再试'
          };
        }
      } catch (error: any) {
        return {
          success: false,
          message: error.response?.data?.message || '注册失败，请稍后再试'
        }
      }
    },

    logout() {
      this.user = null
      this.isLoggedIn = false
      this.isAdmin = false
      localStorage.removeItem('user')
      localStorage.removeItem('userType')
    },

    initializeFromLocalStorage() {
      const userStr = localStorage.getItem('user')
      const userType = localStorage.getItem('userType')

      if (userStr) {
        this.user = JSON.parse(userStr)
        this.isLoggedIn = true
        // 如果用户类型是管理员，直接设置为管理员
        this.isAdmin = userType === 'admin'
      }
    },
    
    // 更新用户余额并保存到本地存储
    updateUserBalance(balance: number) {
      if (this.user) {
        this.user.balance = balance
        localStorage.setItem('user', JSON.stringify(this.user))
      }
    }
  }
})