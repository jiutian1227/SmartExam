import request from '../utils/request'

//========================================//
//  1. 考试流程（开始/提交）
//========================================//

export const startExam = (data) => {
  return request.post('/api/records/start', data)
}

export const submitExam = (data) => {
  return request.post('/api/records/submit', data)
}

export const createRecord = (data) => {
  return request.post('/api/records', data)
}

//========================================//
//  2. 成绩查询
//========================================//

//获取所有考试记录列表（分页）
export const getRecordList = (params = {}) => {
  return request.get('/api/records', { params })
}

export const getAllRecords = (params = {}) => {
  return request.get('/api/records', { params })
}

//根据ID查询考试记录详情
export const getRecordById = (id) => {
  return request.get(`/api/records/${id}`)
}

//我的成绩列表
export const getRecordListByUserId = (userId, params = {}) => {
  return request.get(`/api/records/my/${userId}`, { params })
}

export const getMyRecords = (userId) => {
  return request.get(`/api/records/my/${userId}`)
}

//获取用户已提交的考试ID列表
export const getUserExamIds = (userId) => {
  return request.get(`/api/records/my/${userId}/exam-ids`)
}

//获取用户的考试状态
export const getExamStatus = (userId, examId) => {
  return request.get(`/api/records/my/${userId}/exam/${examId}/status`)
}

//获取考试的所有记录（分页）—— 老师批阅时看
export const getExamRecords = (examId, params = {}) => {
  return request.get(`/api/records/by-exam/${examId}`, { params })
}

//删除考试记录
export const deleteRecord = (id) => {
  return request.delete(`/api/records/${id}`)
}

//========================================//
//  3. 批阅功能
//========================================//

//获取考试记录的答案列表
export const getRecordAnswers = (recordId) => {
  return request.get(`/api/records/${recordId}/answers`)
}

//自动批改客观题
export const autoGradeObjectiveQuestions = (recordId) => {
  return request.post(`/api/records/${recordId}/auto-grade`)
}

//提交分数
export const submitRecordScores = (recordId, scoreList) => {
  return request.post(`/api/records/${recordId}/scores`, scoreList)
}

//AI批改主观题
export const aiGradeSubjective = (data) => {
  return request.post('/api/records/ai-grade-subjective', data)
}

//========================================//
//  4. 统计数据
//========================================//

//批阅统计（分页）
export const getGradingStats = (params = {}) => {
  return request.get('/api/records/stats', { params })
}

//考试统计（注意：这个接口在ExamController，路径不同）
export const getExamStats = (examId) => {
  return request.get(`/api/exams/${examId}/stats`)
}
