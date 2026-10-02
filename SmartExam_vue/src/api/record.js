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

//保存答题草稿（自动存卷）—— 作答过程中定时上报，交卷前答案在服务端也有留存
export const saveDraft = (data) => {
  return request.post('/api/records/draft', data)
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

//我的成绩列表（当前登录用户，身份从JWT解析）
export const getRecordListByUserId = (params = {}) => {
  return request.get('/api/records/my', { params })
}

export const getMyRecords = (params = {}) => {
  return request.get('/api/records/my', { params })
}

//获取当前用户已提交的考试ID列表
export const getUserExamIds = () => {
  return request.get('/api/records/my/exam-ids')
}

//获取当前用户的考试状态（withAnswers=true 时额外回带已保存的草稿答案）
export const getExamStatus = (examId, withAnswers = false) => {
  return request.get(`/api/records/my/exam/${examId}/status`, {
    params: { withAnswers }
  })
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
