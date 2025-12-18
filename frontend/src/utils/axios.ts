import axios from 'axios'
import { useUserStore } from '../store/user'
import { API_BASE_URL } from './apiUrl'

// 创建axios实例
const instance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 导出实例，方便其他文件使用
export default instance

// 同时替换全局axios实例的baseURL，确保所有直接使用axios的地方也能生效
axios.defaults.baseURL = API_BASE_URL

// 请求拦截器
instance.interceptors.request.use(
  config => {
    // 从sessionStorage获取当前标签页的tabId
    const tabId = sessionStorage.getItem('tabId');
    if (tabId) {
      // 使用标签页唯一标识符从localStorage获取token
      const token = localStorage.getItem(`token_${tabId}`);
      if (token) {
        // 添加Authorization请求头
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  error => {
    // 处理请求错误
    return Promise.reject(error);
  }
);

// 响应拦截器
instance.interceptors.response.use(
  response => {
    // 直接返回响应数据
    return response
  },
  error => {
    // 处理响应错误
    console.error('API Error:', error)
    
    // 如果是401未授权，可能是token过期，清除本地存储并跳转到登录页
    if (error.response && error.response.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      // 这里可以添加跳转到登录页的逻辑
      // router.push('/login')
    }
    
    return Promise.reject(error)
  }
)

export default instance
