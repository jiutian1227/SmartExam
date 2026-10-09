import request from '../utils/request'

export const getQuestionList = (params) => {
  return request.get('/api/question', { params })
}

export const getQuestionById = (id) => {
  return request.get(`/api/question/${id}`)
}

export const createQuestion = (data) => {
  return request.post('/api/question', data)
}

export const updateQuestion = (data) => {
  return request.put('/api/question', data)
}

export const deleteQuestion = (id) => {
  return request.delete(`/api/question/${id}`)
}

//AI生成题目（不单独设置timeout，超时时长由服务端决定，前端只负责提示）
export const aiGenerateQuestions = (data) => {
  return request.post('/api/question/ai-generate', data)
}

//批量入库题目（AI出题勾选后一次性落库，后端事务保证原子性）
export const batchCreateQuestions = (list) => {
  return request.post('/api/question/batch', list)
}

//AI生成题解
export const getQuestionSolution = (data) => {
  return request.post('/api/solution/generate', data, { timeout: 120000 })
}
