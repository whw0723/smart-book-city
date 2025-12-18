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
  state: () => {
    // 从sessionStorage获取tabId，如果不存在则生成新的
    // 使用sessionStorage是因为它是每个标签页独立的，不会在标签页之间共享
    let tabId = sessionStorage.getItem('tabId');
    if (!tabId) {
      tabId = `tab_${Math.random().toString(36).substr(2, 9)}_${Date.now()}`;
      sessionStorage.setItem('tabId', tabId);
    }
    
    return {
      user: null as User | null,
      isLoggedIn: false,
      isAdmin: false,
      token: null as string | null,
      userType: null as string | null,
      tabId: tabId // 标签页唯一标识符
    };
  },

  actions: {
    async login(username: string, password: string, loginType: string = 'user') {
      try {
        // 根据登录类型选择不同的API端点
        const endpoint = loginType === 'admin'
          ? '/admin/login'
          : '/users/login';

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
            this.userType = loginType;
            // 使用标签页唯一标识符作为localStorage的键
            localStorage.setItem(`user_${this.tabId}`, JSON.stringify(response.data.admin));
            localStorage.setItem(`userType_${this.tabId}`, loginType);
            return { success: true };
          } else {
            return {
              success: false,
              message: response.data?.message || '管理员登录失败，请检查用户名和密码'
            };
          }
        } else {
          // 普通用户登录返回格式：{success: boolean, user: object, token: string}
          if (response.data && response.data.success && response.data.user) {
            this.user = response.data.user;
            this.isLoggedIn = true;
            this.isAdmin = false;
            this.token = response.data.token;
            this.userType = loginType;
            // 使用标签页唯一标识符作为localStorage的键
            localStorage.setItem(`user_${this.tabId}`, JSON.stringify(response.data.user));
            localStorage.setItem(`token_${this.tabId}`, response.data.token);
            localStorage.setItem(`userType_${this.tabId}`, loginType);
            return { success: true };
          } else if (response.data && (response.data.id || response.data.username)) {
            // 兼容旧格式：直接返回用户对象
            this.user = response.data;
            this.isLoggedIn = true;
            this.isAdmin = false;
            this.userType = loginType;
            // 使用标签页唯一标识符作为localStorage的键
            localStorage.setItem(`user_${this.tabId}`, JSON.stringify(response.data));
            localStorage.setItem(`userType_${this.tabId}`, loginType);
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
        const response = await axios.post('/users/register', {
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
      this.token = null
      this.userType = null
      // 使用标签页唯一标识符作为localStorage的键
      localStorage.removeItem(`user_${this.tabId}`)
      localStorage.removeItem(`token_${this.tabId}`)
      localStorage.removeItem(`userType_${this.tabId}`)
    },

    initializeFromLocalStorage() {
      // 使用标签页唯一标识符作为localStorage的键
      const userStr = localStorage.getItem(`user_${this.tabId}`)
      const token = localStorage.getItem(`token_${this.tabId}`)
      const userType = localStorage.getItem(`userType_${this.tabId}`)

      if (userStr && userType) {
        this.user = JSON.parse(userStr)
        this.isLoggedIn = true
        this.token = token || null
        this.userType = userType
        // 如果用户类型是管理员，直接设置为管理员
        this.isAdmin = userType === 'admin'
      }
    },
    
    // 更新用户余额并保存到本地存储
    updateUserBalance(balance: number) {
      if (this.user) {
        this.user.balance = balance
        // 使用标签页唯一标识符作为localStorage的键
        localStorage.setItem(`user_${this.tabId}`, JSON.stringify(this.user))
      }
    }
  }
})