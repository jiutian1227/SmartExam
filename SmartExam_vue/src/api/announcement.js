import request from '../utils/request'

// 获取当前角色的公告列表
export function getAnnouncementsByRole(role) {
  return request({
    url: `/api/announcement/role/${role}`,
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
