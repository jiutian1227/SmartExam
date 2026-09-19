import request from '../utils/request'

export const getUserList = (params) => {
  return request.get('/api/user', { params })
}

export const getUserById = (id) => {
  return request.get(`/api/user/${id}`)
}

export const createUser = (data) => {
  return request.post('/api/user', data)
}

export const updateUser = (data) => {
  return request.put('/api/user', data)
}

export const deleteUser = (id) => {
  return request.delete(`/api/user/${id}`)
}

export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/avatar/upload', formData)
}
