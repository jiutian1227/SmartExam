import request from '../utils/request'

export const login = (data) => {
  return request.post('/api/auth/login', data)
}

export const register = (data) => {
  return request.post('/api/auth/register', data)
}

export const logout = () => {
  return request.post('/api/auth/logout')
}

// 按JWT换取当前登录用户身份（角色以token为准，避免本地缓存被篡改）
export const getCurrentUser = () => {
  return request.get('/api/auth/me')
}
