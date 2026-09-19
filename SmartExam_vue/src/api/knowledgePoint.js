import request from '../utils/request'

export const getKnowledgePointList = (creatorId) => {
  return request.get(`/api/knowledge-point/creator/${creatorId}`)
}

export const getAllKnowledgePoints = () => {
  return request.get('/api/knowledge-point/all')
}

export const getKnowledgePointById = (id) => {
  return request.get(`/api/knowledge-point/${id}`)
}

export const createKnowledgePoint = (data) => {
  return request.post('/api/knowledge-point', data)
}

export const updateKnowledgePoint = (data) => {
  return request.put('/api/knowledge-point', data)
}

export const deleteKnowledgePoint = (id) => {
  return request.delete(`/api/knowledge-point/${id}`)
}
