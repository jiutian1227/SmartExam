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

export const aiGenerateQuestions = (data) => {
  return request.post('/api/question/ai-generate', data)
}

//AI生成题解
export const getQuestionSolution = (data) => {
  return request.post('/api/solution/generate', data, { timeout: 120000 })
}
