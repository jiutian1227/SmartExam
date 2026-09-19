import request from '../utils/request'

export const getGroupList = (params) => {
  return request.get('/api/user-group', { params })
}

export const getGroupById = (id) => {
  return request.get(`/api/user-group/${id}`)
}

export const createGroup = (data) => {
  return request.post('/api/user-group', data)
}

export const updateGroup = (data) => {
  return request.put('/api/user-group', data)
}

export const deleteGroup = (id) => {
  return request.delete(`/api/user-group/${id}`)
}

export const getGroupMembers = (groupId) => {
  return request.get(`/api/user-group/${groupId}/members`)
}

export const addGroupMember = (groupId, data) => {
  return request.post(`/api/user-group/${groupId}/member`, data)
}

export const removeGroupMember = (groupId, userId) => {
  return request.delete(`/api/user-group/${groupId}/member/${userId}`)
}

export const getAllUsers = () => {
  return request.get('/api/user-group/users')
}

export const joinGroup = (data) => {
  return request.post('/api/user-group/join', data)
}

export const leaveGroup = (data) => {
  return request({
    url: '/api/user-group/leave',
    method: 'delete',
    data
  })
}

export const getMyGroups = (userId) => {
  return request.get(`/api/user-group/my-groups/${userId}`)
}

export const refreshShareCode = (groupId) => {
  return request.put(`/api/user-group/${groupId}/share-code`)
}

export const joinByShareCode = (data) => {
  return request.post('/api/user-group/join-by-code', data)
}

export const getGroupByShareCode = (shareCode, userId) => {
  return request.get(`/api/user-group/by-share-code/${shareCode}`, { params: { userId } })
}
