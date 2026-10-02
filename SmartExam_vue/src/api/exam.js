import request from '../utils/request'

//========================================//
//  1. 考卷CRUD
//========================================//

export const getExamList = (params) => {
  return request.get('/api/exams', { params })
}

export const getExamById = (id) => {
  return request.get(`/api/exams/${id}`)
}

export const createExam = (data) => {
  return request.post('/api/exams', data)
}

export const updateExam = (data) => {
  return request.put('/api/exams', data)
}

export const deleteExam = (id) => {
  return request.delete(`/api/exams/${id}`)
}

//========================================//
//  2. 学生入口
//========================================//

//获取当前学生可参加的考试列表（身份从JWT解析）
export const getStudentExamList = (params = {}) => {
  return request.get('/api/exams/available', { params })
}

//========================================//
//  3. 考试题目关联
//========================================//

export const getExamQuestions = (examId) => {
  return request.get(`/api/exams/${examId}/questions`)
}

export const addQuestionToExam = (examId, data) => {
  return request.post(`/api/exams/${examId}/questions`, data)
}

export const updateQuestionScore = (examId, data) => {
  return request.put(`/api/exams/${examId}/questions/score`, data)
}

export const removeQuestionFromExam = (examId, questionId) => {
  return request.delete(`/api/exams/${examId}/questions/${questionId}`)
}
