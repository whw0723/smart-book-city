// API URL配置，使用Vite环境变量
// 开发环境默认使用本地地址，生产环境使用环境变量配置的地址
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://smart-book-city.onrender.com/api';

// 生成完整API URL的工具函数
export const apiUrl = (path: string): string => {
  // 确保path以/开头
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `${API_BASE_URL}${normalizedPath}`;
};
