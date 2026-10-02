import axios from 'axios'
import { getToken, removeToken, removeUser } from './auth'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 10000
})

// 统一处理登录态失效：清本地凭证并跳转登录页
const redirectToLogin = (message) => {
  if (!window.location.pathname.startsWith('/login')) {
    removeToken()
    removeUser()
    if (message) ElMessage.error(message)
    window.location.href = '/login'
  }
}

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    // 后端业务码 401：token 过期或无效（Controller 主动返回的情况）
    if (response.data && response.data.code === 401) {
      redirectToLogin(response.data.message || '登录已过期，请重新登录')
      return Promise.reject(response.data)
    }
    return response.data
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      redirectToLogin(error.response.data?.message || '登录已过期，请重新登录')
    } else if (error.response && error.response.status === 403) {
      ElMessage.error(error.response.data?.message || '无权限执行此操作')
    } else if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('请求失败，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
