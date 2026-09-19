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
