import request from '../utils/request'

// 获取当前登录用户的公告列表（角色由后端从JWT解析，前端不再传参）
export function getAnnouncementsByRole() {
  return request({
    url: '/api/announcement/role',
    method: 'get'
  })
}

// 获取所有公告（管理员）
export function getAllAnnouncements() {
  return request({
    url: '/api/announcement',
    method: 'get'
  })
}

// 创建公告（管理员）
export function createAnnouncement(data) {
  return request({
    url: '/api/announcement',
    method: 'post',
    data
  })
}

// 更新公告（管理员）
export function updateAnnouncement(data) {
  return request({
    url: `/api/announcement`,
    method: 'put',
    data
  })
}

// 删除公告（管理员）
export function deleteAnnouncement(id) {
  return request({
    url: `/api/announcement/${id}`,
    method: 'delete'
  })
}
