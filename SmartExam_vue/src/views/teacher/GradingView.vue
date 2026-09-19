<template>
  <div class="grading-page">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>{{ examInfo.title || '考试' }} - 判卷</h2>
    </div>

    <div class="main-content">
      <div class="left-panel">
        <el-card class="student-card">
          <div class="student-info-header">学生信息</div>
          <div class="student-info-item">
            <span class="label">姓名</span>
            <span class="value">{{ currentRecord.realName || currentRecord.username || '未知' }}</span>
          </div>
          <div class="student-info-item">
            <span class="label">提交时间</span>
            <span class="value">{{ formatTime(currentRecord.submitTime) }}</span>
          </div>
          <div class="student-info-item">
            <span class="label">批阅状态</span>
            <span :class="['value', currentRecord.gradingStatus === '已批阅' ? 'graded' : 'ungraded']">
              {{ currentRecord.gradingStatus || '待批阅' }}
            </span>
          </div>
        </el-card>

        <el-card class="navigation-card">
          <div class="nav-header">题型导航</div>
          <div class="nav-list">
            <div 
              v-for="(group, idx) in questionGroups" 
              :key="idx"
              :class="['nav-item', currentGroupIndex === idx ? 'active' : '']"
              @click="scrollToGroup(idx)"
            >
              <span class="nav-label">{{ group.name }}</span>
              <span class="nav-count">{{ group.questions.length }}</span>
            </div>
          </div>
        </el-card>
      </div>

      <div class="center-panel" ref="centerPanel">
        <div v-for="(group, groupIdx) in questionGroups" :key="groupIdx" :id="'group-' + groupIdx" class="question-group">
          <div class="group-header">
            <el-divider>
              <span class="group-title">{{ group.name }}（共 {{ group.questions.length }} 题，{{ group.totalScore }} 分）</span>
            </el-divider>
          </div>
          <div v-for="(item, qIdx) in group.questions" :key="item.id" class="question-item">
            <div class="question-header">
              <span class="question-num">{{ group.prefix }}{{ qIdx + 1 }}</span>
              <span class="question-score">满分：{{ item.examScore || 0 }} 分</span>
            </div>
            <div class="question-content">{{ item.content || '无题目内容' }}</div>
            <div class="question-options" v-if="item.options">
              <div 
                v-for="(opt, optIdx) in parseOptions(item.options)" 
                :key="optIdx" 
                :class="['option-item', isCorrectOption(item.answer, optIdx) ? 'correct' : '']"
              >
                {{ String.fromCharCode(65 + optIdx) }}. {{ opt }}
              </div>
            </div>
            <div class="answer-section">
              <div class="answer-item student-answer">
                <span class="label">学生答案：</span>
                <span class="value">{{ item.userAnswer || '未作答' }}</span>
              </div>
              <div class="answer-item correct-answer" v-if="item.answer">
                <span class="label">正确答案：</span>
                <span class="value">{{ item.answer }}</span>
              </div>
              <div class="score-row">
                <div class="score-input">
                  <span class="label">得分：</span>
                  <el-input-number v-model="item.givenScore" :min="0" :max="item.examScore || 100" size="default" />
                  <span class="unit">/ {{ item.examScore || 0 }} 分</span>
                </div>
              </div>
              <div class="comment-row">
                <span class="label">评语：</span>
                <el-input v-model="item.comment" type="textarea" :rows="2" placeholder="请输入评语" resize="none" />
              </div>
              <div class="ai-grade-row" v-if="item.type === 3 || item.type === 4">
                <el-button type="primary" size="small" @click="singleAiGrade(item)" :loading="isLoadingAiGrade && currentAiGradeItem?.id === item.id">
                  AI批改
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="right-panel">
        <el-card class="submit-card">
          <div class="submit-header">判卷操作</div>
          <el-button type="primary" class="ai-batch-btn" @click="openAiBatchDrawer">
            <el-icon><MagicStick /></el-icon>
            智能判卷
          </el-button>
          <div class="final-score">
            <span class="label">本次得分</span>
            <span class="final-value">{{ calculateTotalScore() }}</span>
            <span class="final-total">/ {{ examInfo.totalScore || 0 }}</span>
          </div>
          <el-button type="primary" class="submit-btn" @click="submitGrades">
            {{ currentRecord.gradingStatus === '已批阅' ? '更新成绩' : '提交成绩' }}
          </el-button>
        </el-card>
      </div>
    </div>

    <!-- 智能判卷抽屉 -->
    <ElDrawer
      title="智能判卷"
      v-model="showAiBatchDrawer"
      direction="rtl"
      size="620px"
    >
      <div class="ai-drawer-content">
        <!-- 进度统计 -->
        <div class="progress-summary">
          <div class="summary-card">
            <div class="summary-icon">
              <el-icon :size="24"><ChatDotSquare /></el-icon>
            </div>
            <div class="summary-info">
              <div class="summary-value">{{ aiGradingProgress.current }}/{{ aiGradingProgress.total }}</div>
              <div class="summary-label">已处理题目</div>
            </div>
          </div>
          <div class="summary-card success">
            <div class="summary-icon">
              <el-icon :size="24"><Check /></el-icon>
            </div>
            <div class="summary-info">
              <div class="summary-value">{{ aiGradingProgress.correct }}</div>
              <div class="summary-label">判卷成功</div>
            </div>
          </div>
          <div class="summary-card warning">
            <div class="summary-icon">
              <el-icon :size="24"><WarningFilled /></el-icon>
            </div>
            <div class="summary-info">
              <div class="summary-value">{{ aiGradingProgress.warning }}</div>
              <div class="summary-label">需人工复核</div>
            </div>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="progress-section">
          <div class="progress-header">
            <span>判卷进度</span>
            <span class="progress-percent">{{ aiGradingProgress.percentage }}%</span>
          </div>
          <div class="progress-bar-container">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: aiGradingProgress.percentage + '%' }"></div>
              <div class="progress-glow" :style="{ left: aiGradingProgress.percentage + '%' }"></div>
            </div>
          </div>
        </div>

        <!-- 当前题目信息 -->
        <div class="current-question" v-if="aiGradingStatus === 'grading'">
          <div class="question-header">
            <span class="question-badge">AI处理中</span>
            <span class="question-type">{{ getQuestionTypeName(currentAiQuestion?.type) }}</span>
          </div>
          <div class="question-preview">
            <div class="preview-title">题目内容</div>
            <div class="preview-content">{{ currentAiQuestion?.content }}</div>
          </div>
          <div class="answer-compare">
            <div class="answer-item">
              <span class="answer-label">学生答案</span>
              <span class="answer-text">{{ currentAiQuestion?.userAnswer }}</span>
            </div>
            <div class="answer-item correct">
              <span class="answer-label">正确答案</span>
              <span class="answer-text">{{ currentAiQuestion?.answer }}</span>
            </div>
          </div>
          <div class="ai-thinking">
            <div class="thinking-dots">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
            <span class="thinking-text">{{ currentAiQuestion?.type === 1 || currentAiQuestion?.type === 2 ? '自动判卷中...' : 'AI正在分析...' }}</span>
          </div>
        </div>

        <!-- 判卷结果列表 -->
        <div class="result-section" v-if="aiGradingStatus === 'completed'">
          <div class="section-header">
            <span>判卷结果</span>
            <div class="section-actions">
              <el-button size="small" @click="selectAllResults">全选</el-button>
              <el-button size="small" @click="deselectAllResults">反选</el-button>
            </div>
          </div>
          <div class="result-list">
            <div 
              v-for="(result, idx) in aiGradingResults" 
              :key="idx" 
              :class="['result-item', result.accepted ? 'accepted' : 'pending', { expanded: expandedResult === idx }]"
            >
              <div class="result-header">
                <el-checkbox 
                  v-model="result.selected" 
                  @change.stop="toggleSelect(idx)"
                  :disabled="!result.accepted"
                />
                <div class="result-index">{{ idx + 1 }}</div>
                <div class="result-content">
                  <div class="result-type">
                    {{ getQuestionTypeName(result.type) }}
                    <span :class="['grade-tag', result.isAuto ? 'auto' : 'smart']">
                      {{ result.isAuto ? '自动判卷' : '智能判卷' }}
                    </span>
                  </div>
                  <div class="result-score">
                    <span class="score-value">{{ result.score }}</span>
                    <span class="score-total">/ {{ result.examScore }}</span>
                  </div>
                </div>
                <div class="result-status">
                  <el-icon v-if="result.accepted" color="#22c55e" :size="18"><CircleCheckFilled /></el-icon>
                  <el-icon v-else color="#f59e0b" :size="18"><WarningFilled /></el-icon>
                </div>
                <div class="expand-icon" @click.stop="toggleResultExpand(idx)">
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
              <div class="result-detail" v-if="expandedResult === idx">
                <div class="detail-section">
                  <div class="detail-title">题目内容</div>
                  <div class="detail-content text-ellipsis" :class="{ 'expanded': result.contentExpanded }">
                    {{ result.content }}
                    <span v-if="result.content && result.content.length > 100" class="expand-text" @click.stop="result.contentExpanded = !result.contentExpanded">
                      {{ result.contentExpanded ? '收起' : '展开' }}
                    </span>
                  </div>
                </div>
                <div class="detail-row">
                  <div class="detail-item">
                    <div class="detail-label">学生答案</div>
                    <div class="detail-value student text-ellipsis" :class="{ 'expanded': result.userAnswerExpanded }">
                      {{ result.userAnswer || '未作答' }}
                      <span v-if="result.userAnswer && result.userAnswer.length > 100" class="expand-text" @click.stop="result.userAnswerExpanded = !result.userAnswerExpanded">
                        {{ result.userAnswerExpanded ? '收起' : '展开' }}
                      </span>
                    </div>
                  </div>
                  <div class="detail-item">
                    <div class="detail-label">正确答案</div>
                    <div class="detail-value correct text-ellipsis" :class="{ 'expanded': result.answerExpanded }">
                      {{ result.answer }}
                      <span v-if="result.answer && result.answer.length > 100" class="expand-text" @click.stop="result.answerExpanded = !result.answerExpanded">
                        {{ result.answerExpanded ? '收起' : '展开' }}
                      </span>
                    </div>
                  </div>
                </div>
                <div class="detail-section">
                  <div class="detail-title">AI评语</div>
                  <div class="detail-comment text-ellipsis" :class="{ 'expanded': result.commentExpanded }">
                    {{ result.comment || '暂无评语' }}
                    <span v-if="result.comment && result.comment.length > 150" class="expand-text" @click.stop="result.commentExpanded = !result.commentExpanded">
                      {{ result.commentExpanded ? '收起' : '展开' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 开始按钮 -->
        <div class="start-section" v-if="aiGradingStatus === 'idle'">
          <div class="start-info">
            <div class="start-icon">
              <el-icon><Lightning /></el-icon>
            </div>
            <div class="start-text">
              <div class="start-title">智能判卷</div>
              <div class="start-desc">客观题依托题库自动比对判分，主观题AI智能分析阅卷，一键批量批改试卷</div>
            </div>
          </div>
          <el-button type="primary" class="start-btn" @click="startAiBatchGrade">
            开始判卷
          </el-button>
        </div>

        <!-- 操作按钮 -->
        <div class="drawer-footer">
          <el-button @click="closeAiBatchDrawer">关闭</el-button>
          <el-button 
            v-if="aiGradingStatus === 'completed'" 
            type="primary" 
            @click="applySelectedAiResults"
          >
            应用选中
          </el-button>
        </div>
      </div>
    </ElDrawer>

    <el-dialog v-model="showAiGradeDialog" title="AI批改结果" width="600px">
      <div v-if="isLoadingAiGrade" class="ai-loading">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>AI正在批改中，请稍候...</p>
      </div>
      <div v-else-if="aiGradeResult" class="ai-result">
        <div class="result-row">
          <span class="result-label">题目：</span>
          <span class="result-value">{{ currentAiGradeItem?.content }}</span>
        </div>
        <div class="result-row">
          <span class="result-label">学生答案：</span>
          <span class="result-value">{{ currentAiGradeItem?.userAnswer }}</span>
        </div>
        <div class="result-row">
          <span class="result-label">正确答案：</span>
          <span class="result-value">{{ currentAiGradeItem?.answer }}</span>
        </div>
        <el-divider />
        <div class="result-row score-row">
          <span class="result-label">AI建议得分：</span>
          <span class="result-value score-value">{{ aiGradeResult.score }} / {{ currentAiGradeItem?.examScore }}</span>
        </div>
        <div class="result-row">
          <span class="result-label">AI评语：</span>
        </div>
        <div class="result-comment">{{ aiGradeResult.comment }}</div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rejectAiGrade">拒绝</el-button>
          <el-button type="primary" @click="acceptAiGrade">接受</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElDrawer } from 'element-plus'
import { Loading, ArrowLeft, ArrowRight, MagicStick, ChatDotSquare, Check, WarningFilled, Lightning, CircleCheckFilled } from '@element-plus/icons-vue'
import { getExamStats, getExamRecords, getRecordAnswers, submitRecordScores, autoGradeObjectiveQuestions, aiGradeSubjective } from '../../api/record'

const router = useRouter()
const route = useRoute()

const examId = ref(parseInt(route.params.examId))
const recordId = ref(parseInt(route.params.recordId))
const examInfo = ref({})
const currentRecord = ref({})
const answerList = ref([])
const centerPanel = ref(null)
const currentGroupIndex = ref(0)
const showAiGradeDialog = ref(false)
const currentAiGradeItem = ref(null)
const aiGradeResult = ref(null)
const isLoadingAiGrade = ref(false)

// AI批量判卷相关
const showAiBatchDrawer = ref(false)
const aiGradingStatus = ref('idle')
const aiGradingProgress = ref({
  current: 0,
  total: 0,
  correct: 0,
  warning: 0,
  percentage: 0
})
const aiGradingResults = ref([])
const currentAiQuestion = ref(null)
const expandedResult = ref(-1)

const questionTypeNames = ['单选题', '多选题', '判断题', '填空题', '简答题']
const questionTypeColors = ['primary', 'success', 'warning', 'info', 'danger']

const questionGroups = computed(() => {
  const groups = []
  const typeOrder = [0, 1, 2, 3, 4]
  
  typeOrder.forEach(type => {
    const questions = answerList.value.filter(q => q.type === type)
    if (questions.length > 0) {
      groups.push({
        name: questionTypeNames[type],
        type: type,
        questions: questions,
        prefix: getQuestionPrefix(type),
        totalScore: questions.reduce((sum, q) => sum + (q.examScore || 0), 0)
      })
    }
  })
  
  return groups
})

const getQuestionPrefix = (type) => {
  return ''
}

const goBack = () => {
  router.push(`/teacher/grading/${examId.value}`)
}

import { formatDateTime } from '../../utils/format'
const formatTime = formatDateTime

const parseOptions = (options) => {
  if (!options) return []
  try {
    if (typeof options === 'string') {
      return JSON.parse(options)
    }
    return options
  } catch (e) {
    return []
  }
}

const isCorrectOption = (answer, index) => {
  if (!answer) return false
  const optionLabel = String.fromCharCode(65 + index)
  return answer.split(',').includes(optionLabel)
}

const scrollToGroup = (index) => {
  currentGroupIndex.value = index
  const element = document.getElementById(`group-${index}`)
  if (element && centerPanel.value) {
    centerPanel.value.scrollTo({
      top: element.offsetTop - 20,
      behavior: 'smooth'
    })
  }
}

const loadExamInfo = async () => {
  try {
    const res = await getExamStats(examId.value)
    if (res.code === 200) {
      examInfo.value = res.data || {}
    }
  } catch (error) {
    console.error('加载考试信息失败', error)
  }
}

const loadRecordAnswers = async () => {
  try {
    const res = await getRecordAnswers(recordId.value)
    if (res.code === 200) {
      answerList.value = (res.data || []).map(item => ({
        ...item,
        givenScore: item.score || 0,
        comment: item.comment || ''
      }))
    }
  } catch (error) {
    console.error('加载答题记录失败', error)
    ElMessage.error('加载答题记录失败')
  }
}

const loadRecordInfo = async () => {
  try {
    const res = await getExamRecords(examId.value, {
      pageNum: 1,
      pageSize: 1000
    })
    if (res.code === 200) {
      const records = res.data.records || []
      currentRecord.value = records.find(r => r.id === recordId.value) || {}
    }
  } catch (error) {
    console.error('加载记录信息失败', error)
  }
}

const calculateTotalScore = () => {
  return answerList.value.reduce((sum, item) => sum + (item.givenScore || 0), 0)
}

const autoGrade = async () => {
  try {
    const res = await autoGradeObjectiveQuestions(recordId.value)
    if (res.code === 200) {
      const scoreList = res.data || []
      answerList.value = answerList.value.map(item => {
        const scoreItem = scoreList.find(s => s.id === item.id)
        if (scoreItem) {
          return {
            ...item,
            givenScore: scoreItem.score,
            comment: scoreItem.comment || ''
          }
        }
        return item
      })
      ElMessage.success('客观题判卷完成！')
    } else {
      ElMessage.error(res.message || '自动判卷失败')
    }
  } catch (error) {
    console.error('自动判卷失败', error)
    ElMessage.error('自动判卷失败')
  }
}

const submitGrades = async () => {
  const scoreList = answerList.value.map(item => ({
    answerRecordId: item.id,
    score: item.givenScore,
    comment: item.comment
  }))

  try {
    const res = await submitRecordScores(recordId.value, scoreList)
    if (res.code === 200) {
      ElMessage.success('提交成功')
      router.push(`/teacher/grading/${examId.value}`)
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交失败', error)
    ElMessage.error('提交失败')
  }
}

// 修改函数名，避免和导入的aiGradeSubjective冲突
const singleAiGrade = async (item) => {
  if (!item.userAnswer) {
    ElMessage.warning('学生未作答，无法AI批改')
    return
  }
  currentAiGradeItem.value = item
  showAiGradeDialog.value = true
  aiGradeResult.value = null
  isLoadingAiGrade.value = true

  try {
    const res = await aiGradeSubjective({
      questionContent: item.content,
      correctAnswer: item.answer,
      studentAnswer: item.userAnswer,
      type: item.type,
      score: item.examScore
    })
    if (res.code === 200) {
      aiGradeResult.value = res.data
    } else {
      ElMessage.error(res.message || 'AI批改失败')
    }
  } catch (error) {
    console.error('AI批改失败', error)
    ElMessage.error('AI批改失败')
  } finally {
    isLoadingAiGrade.value = false
  }
}

const getQuestionTypeName = (type) => {
  return questionTypeNames[type] || '未知题型'
}

const openAiBatchDrawer = () => {
  showAiBatchDrawer.value = true
  aiGradingStatus.value = 'idle'
  aiGradingResults.value = []
  aiGradingProgress.value = {
    current: 0,
    total: 0,
    correct: 0,
    warning: 0,
    percentage: 0
  }
  expandedResult.value = -1
}

const closeAiBatchDrawer = () => {
  showAiBatchDrawer.value = false
}

const toggleResultExpand = (idx) => {
  expandedResult.value = expandedResult.value === idx ? -1 : idx
}

const toggleSelect = (idx) => {
  if (aiGradingResults.value[idx]) {
    aiGradingResults.value[idx].selected = !aiGradingResults.value[idx].selected
  }
}

const selectAllResults = () => {
  aiGradingResults.value.forEach(result => {
    if (result.accepted) {
      result.selected = true
    }
  })
}

const deselectAllResults = () => {
  aiGradingResults.value.forEach(result => {
    result.selected = false
  })
}

const startAiBatchGrade = async () => {
  const allQuestions = answerList.value
  if (allQuestions.length === 0) {
    ElMessage.warning('没有需要判卷的题目')
    return
  }

  aiGradingStatus.value = 'grading'
  aiGradingResults.value = []
  aiGradingProgress.value = {
    current: 0,
    total: allQuestions.length,
    correct: 0,
    warning: 0,
    percentage: 0
  }

  for (let i = 0; i < allQuestions.length; i++) {
    const question = allQuestions[i]
    currentAiQuestion.value = question
    aiGradingProgress.value.current = i + 1
    aiGradingProgress.value.percentage = Math.round(((i + 1) / allQuestions.length) * 100)

    if (question.type === 0 || question.type === 1 || question.type === 2) {
      const userAnswer = (question.userAnswer || '').toString().trim().toUpperCase()
      const correctAnswer = (question.answer || '').toString().trim().toUpperCase()
      const isCorrect = userAnswer === correctAnswer
      const score = isCorrect ? question.examScore : 0
      
      const result = {
        ...question,
        score: score,
        comment: isCorrect ? '回答正确' : `回答错误，正确答案：${question.answer}`,
        accepted: true,
        selected: true,
        isAuto: true
      }
      aiGradingResults.value.push(result)
      aiGradingProgress.value.correct++
      
      await new Promise(resolve => setTimeout(resolve, 300))
    } else {
      try {
        const res = await aiGradeSubjective({
          questionContent: question.content,
          correctAnswer: question.answer,
          studentAnswer: question.userAnswer,
          type: question.type,
          score: question.examScore
        })

        if (res.code === 200) {
          const result = {
            ...question,
            score: res.data.score,
            comment: res.data.comment,
            accepted: true,
            selected: true,
            isAuto: false
        }
        aiGradingResults.value.push(result)
        aiGradingProgress.value.correct++
      } else {
        aiGradingResults.value.push({
          ...question,
          score: 0,
          comment: '',
          accepted: false,
          selected: false,
          isAuto: false
        })
        aiGradingProgress.value.warning++
      }
    } catch (error) {
      aiGradingResults.value.push({
        ...question,
        score: 0,
        comment: '',
        accepted: false,
        selected: false,
        isAuto: false
      })
      aiGradingProgress.value.warning++
    }
    }
  }

  aiGradingStatus.value = 'completed'
  currentAiQuestion.value = null
  ElMessage.success('智能判卷完成！')
}

const applySelectedAiResults = () => {
  const selectedResults = aiGradingResults.value.filter(r => r.selected)
  if (selectedResults.length === 0) {
    ElMessage.warning('请先选择要应用的判卷结果')
    return
  }
  
  selectedResults.forEach(result => {
    const item = answerList.value.find(q => q.id === result.id)
    if (item) {
      item.givenScore = result.score
      item.comment = result.comment
    }
  })
  showAiBatchDrawer.value = false
  ElMessage.success(`已应用 ${selectedResults.length} 个判卷结果`)
}

const applyAllAiResults = () => {
  aiGradingResults.value.forEach(result => {
    result.selected = true
  })
  applySelectedAiResults()
}

const acceptAiGrade = () => {
  if (currentAiGradeItem.value && aiGradeResult.value) {
    currentAiGradeItem.value.givenScore = aiGradeResult.value.score
    currentAiGradeItem.value.comment = aiGradeResult.value.comment
    showAiGradeDialog.value = false
    ElMessage.success('已应用AI批改结果')
  }
}

const rejectAiGrade = () => {
  showAiGradeDialog.value = false
  aiGradeResult.value = null
  currentAiGradeItem.value = null
}

onMounted(async () => {
  await loadExamInfo()
  await loadRecordAnswers()
  await loadRecordInfo()
})
</script>

<style scoped>
.grading-page {
  width: 100%;
  min-width: 0;
  min-height: 100vh;
  background: var(--color-bg-base);
  padding: var(--space-6);
  box-sizing: border-box;
  overflow-x: hidden;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-6);
  gap: var(--space-4);
  width: 100%;
  max-width: 100%;
}
.page-header h2 { margin: 0; font-size: var(--font-size-2xl); color: var(--color-text-primary); }

.main-content {
  display: flex;
  gap: var(--space-5);
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
  box-sizing: border-box;
}

.left-panel {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.student-info-header, .nav-header, .submit-header {
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-neutral-200);
  font-size: var(--font-size-sm);
}

.student-info-item {
  display: flex;
  justify-content: space-between;
  padding: var(--space-2) 0;
  font-size: var(--font-size-sm);
}
.student-info-item .label { color: var(--color-text-tertiary); }
.student-info-item .value { color: var(--color-text-primary); font-weight: var(--font-weight-medium); }
.student-info-item .value.graded { color: var(--color-success); }
.student-info-item .value.ungraded { color: var(--color-warning); }

.nav-list { display: flex; flex-direction: column; }
.nav-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-2);
  background: var(--color-neutral-50);
  border-radius: var(--radius-base);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.nav-item:hover { background: var(--color-primary-50); box-shadow: var(--shadow-sm); }
.nav-item.active { background: var(--color-primary-500); box-shadow: var(--shadow-primary); }
.nav-item.active .nav-label,
.nav-item.active .nav-count { color: #fff; }
.nav-label { font-size: var(--font-size-sm); color: var(--color-text-secondary); }
.nav-count {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  background: var(--color-bg-elevated);
  padding: 2px 10px;
  border-radius: var(--radius-full);
}
.nav-item.active .nav-count { background: rgba(255,255,255,0.25); }

.center-panel {
  flex: 1;
  min-width: 0;
  max-height: calc(100vh - 140px);
  overflow-y: auto;
  overflow-x: auto;
  background: var(--color-bg-elevated);
  border-radius: var(--radius-lg);
  padding: var(--space-8);
  border: 1px solid var(--color-neutral-200);
  max-width: calc(100% - 580px);
  box-sizing: border-box;
}

.question-group { margin-bottom: var(--space-8); }
.group-header { margin-bottom: var(--space-5); }
.group-title { font-size: var(--font-size-lg); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); }

.question-item {
  border: 1px solid var(--color-neutral-200);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  margin-bottom: var(--space-5);
  transition: box-shadow var(--transition-base), border-color var(--transition-base);
}
.question-item:hover { box-shadow: var(--shadow-sm); border-color: var(--color-primary-200); }
.question-item:last-child { margin-bottom: 0; }

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}
.question-num { font-weight: var(--font-weight-semibold); color: var(--color-primary-500); font-size: var(--font-size-lg); }
.question-score { color: var(--color-success); font-size: var(--font-size-sm); }

.question-content {
  font-size: var(--font-size-md);
  line-height: 1.7;
  margin-bottom: var(--space-5);
  color: var(--color-text-primary);
  word-break: break-all;
  overflow-wrap: break-word;
  max-width: 100%;
  box-sizing: border-box;
}

.question-options { margin-bottom: var(--space-4); }
.option-item {
  padding: var(--space-3) var(--space-4);
  background: var(--color-neutral-50);
  border-radius: var(--radius-sm);
  margin-bottom: var(--space-2);
  font-size: var(--font-size-sm);
  border-left: 3px solid transparent;
  transition: all var(--transition-fast);
}
.option-item.correct { background: var(--color-secondary-50); border-left-color: var(--color-success); }

.answer-section {
  padding-top: var(--space-5);
  border-top: 1px dashed var(--color-neutral-300);
}
.answer-item { display: flex; flex-direction: column; margin-bottom: var(--space-4); }
.answer-item .label {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
  margin-bottom: var(--space-2);
  font-weight: var(--font-weight-medium);
}
.answer-item .value {
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
  word-break: break-all;
  padding: var(--space-3) var(--space-4);
  background: var(--color-neutral-50);
  border-radius: var(--radius-sm);
}
.answer-item.student-answer .value { background: var(--color-primary-50); }
.correct-answer .value { color: var(--color-success); background: var(--color-secondary-50); font-weight: var(--font-weight-medium); }

.score-row { display: flex; margin-bottom: var(--space-4); }
.score-input { display: flex; align-items: center; gap: var(--space-2); }
.score-input .label { color: var(--color-text-tertiary); font-size: var(--font-size-sm); }
.score-input .unit { color: var(--color-text-tertiary); font-size: var(--font-size-xs); }

.comment-row { display: flex; gap: var(--space-3); }
.comment-row .label { color: var(--color-text-tertiary); font-size: var(--font-size-sm); flex-shrink: 0; padding-top: var(--space-2); }
.comment-row .el-input { flex: 1; }

.right-panel { width: 300px; flex-shrink: 0; }
.submit-card { position: sticky; top: var(--space-6); }
.submit-header { margin-bottom: var(--space-5); }

.auto-grade-btn { width: 100%; height: 44px; font-size: var(--font-size-sm); margin-bottom: var(--space-4); }

.final-score { text-align: center; padding: var(--space-6) 0; margin-bottom: var(--space-5); }
.final-score .label { display: block; font-size: var(--font-size-sm); color: var(--color-text-tertiary); margin-bottom: var(--space-3); }
.final-value { font-size: 56px; font-weight: var(--font-weight-bold); color: var(--color-success); line-height: 1; }
.final-total { font-size: var(--font-size-xl); color: var(--color-text-tertiary); }

.submit-btn { width: 100%; height: 48px; font-size: var(--font-size-lg); }

.ai-grade-row { margin-top: var(--space-3); }

.ai-loading { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: var(--space-10) 0; gap: var(--space-4); }
.ai-loading p { margin: 0; color: var(--color-text-secondary); font-size: var(--font-size-sm); }

.ai-result { padding: var(--space-2) 0; }
.result-row { display: flex; margin-bottom: var(--space-3); }
.result-label { font-weight: var(--font-weight-semibold); color: var(--color-text-secondary); flex-shrink: 0; width: 90px; font-size: var(--font-size-sm); }
.result-value { color: var(--color-text-primary); flex: 1; word-break: break-all; font-size: var(--font-size-sm); }
.score-value { font-size: var(--font-size-xl); font-weight: var(--font-weight-bold); color: var(--color-primary-500); }
.result-comment { background: var(--color-neutral-50); padding: var(--space-4) var(--space-5); border-radius: var(--radius-base); color: var(--color-text-primary); line-height: 1.6; margin-top: var(--space-1); font-size: var(--font-size-sm); }
.dialog-footer { display: flex; justify-content: flex-end; gap: var(--space-3); }

/* ── AI Batch Drawer ────────────────────────────────────── */
.ai-batch-btn { width: 100%; height: 44px; font-size: var(--font-size-sm); margin-bottom: var(--space-4); display: flex; align-items: center; justify-content: center; gap: var(--space-2); }
.ai-drawer-content { padding: var(--space-5); height: calc(100vh - 100px); overflow-y: auto; }

.progress-summary { display: flex; gap: var(--space-3); margin-bottom: var(--space-6); }
.summary-card {
  flex: 1; border-radius: var(--radius-lg); padding: var(--space-4);
  display: flex; flex-direction: column; align-items: center; color: #fff;
}
.summary-card { background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-700)); }
.summary-card.success { background: linear-gradient(135deg, #059669, var(--color-success)); }
.summary-card.warning { background: linear-gradient(135deg, var(--color-warning), #d97706); }

.summary-icon { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; margin-bottom: var(--space-2); opacity: 0.9; }
.summary-icon .el-icon { font-size: 24px; }
.summary-info { text-align: center; }
.summary-value { font-size: 24px; font-weight: bold; line-height: 1.2; }
.summary-label { font-size: var(--font-size-xs); opacity: 0.85; margin-top: 4px; }

.progress-section { background: var(--color-neutral-50); border-radius: var(--radius-lg); padding: var(--space-5); margin-bottom: var(--space-6); }
.progress-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-4); }
.progress-header span { font-size: var(--font-size-sm); color: var(--color-text-secondary); }
.progress-percent { font-weight: var(--font-weight-semibold); color: var(--color-primary-500) !important; font-size: var(--font-size-lg) !important; }
.progress-bar-container { height: 8px; background: var(--color-neutral-200); border-radius: var(--radius-full); overflow: visible; }
.progress-bar { position: relative; height: 100%; border-radius: var(--radius-full); overflow: hidden; }
.progress-fill { height: 100%; background: linear-gradient(90deg, var(--color-primary-500), var(--color-success)); border-radius: var(--radius-full); transition: width var(--transition-base); box-shadow: 0 0 8px rgba(26, 143, 232, 0.4); }
.progress-glow {
  position: absolute; top: 50%; transform: translate(-50%, -50%);
  width: 16px; height: 16px; background: #fff; border-radius: 50%;
  box-shadow: 0 0 10px rgba(26, 143, 232, 0.6);
  transition: left var(--transition-base);
}

.current-question { background: var(--color-bg-elevated); border: 1px solid var(--color-neutral-200); border-radius: var(--radius-lg); padding: var(--space-5); margin-bottom: var(--space-6); }
.question-badge { background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-700)); color: #fff; font-size: var(--font-size-xs); padding: 4px 12px; border-radius: var(--radius-full); font-weight: var(--font-weight-medium); }
.question-type { font-size: var(--font-size-sm); color: var(--color-text-tertiary); }
.question-preview { margin-bottom: var(--space-4); }
.preview-title { font-size: var(--font-size-xs); color: var(--color-text-tertiary); margin-bottom: var(--space-2); font-weight: var(--font-weight-medium); text-transform: uppercase; letter-spacing: 0.3px; }
.preview-content { font-size: var(--font-size-sm); color: var(--color-text-primary); line-height: 1.6; background: var(--color-neutral-50); padding: var(--space-3); border-radius: var(--radius-base); }

.answer-compare { display: flex; gap: var(--space-3); margin-bottom: var(--space-5); }
.answer-compare .answer-item { flex: 1; }
.answer-compare .answer-label { font-size: var(--font-size-xs); color: var(--color-text-tertiary); margin-bottom: var(--space-1); display: block; font-weight: var(--font-weight-medium); text-transform: uppercase; letter-spacing: 0.3px; }
.answer-compare .answer-text { font-size: var(--font-size-sm); color: var(--color-text-primary); background: var(--color-neutral-50); padding: var(--space-3); border-radius: var(--radius-sm); word-break: break-all; line-height: 1.5; }
.answer-compare .answer-item.correct .answer-text { background: var(--color-secondary-50); color: var(--color-success); }

.ai-thinking {
  display: flex; align-items: center; justify-content: center;
  gap: var(--space-3); padding: var(--space-5);
  background: var(--color-primary-50);
  border-radius: var(--radius-base);
}
.thinking-dots { display: flex; gap: 6px; }
.thinking-dots .dot {
  width: 8px; height: 8px; background: var(--color-primary-500);
  border-radius: 50%; animation: dotPulse 1.4s infinite ease-in-out;
}
.thinking-dots .dot:nth-child(2) { animation-delay: 0.2s; }
.thinking-dots .dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotPulse {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}
.thinking-text { font-size: var(--font-size-sm); color: var(--color-text-secondary); }

.result-section {
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-neutral-200);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
}
.result-list { display: flex; flex-direction: column; gap: var(--space-4); }

.result-item {
  display: flex; flex-direction: column;
  padding: var(--space-4) var(--space-5);
  background: #fff; border-radius: var(--radius-lg);
  transition: all var(--transition-base);
  border: 1px solid var(--color-neutral-200);
}
.result-item:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.result-item.accepted { border-left: 4px solid var(--color-success); background: var(--color-secondary-50); }
.result-item.pending { border-left: 4px solid var(--color-warning); background: var(--color-accent-50); }

.result-header { display: flex; align-items: center; width: 100%; gap: var(--space-3); }
.result-index {
  width: 28px; height: 28px; background: var(--color-neutral-300);
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: var(--font-size-sm); color: var(--color-text-secondary); flex-shrink: 0;
}
.result-item.accepted .result-index { background: var(--color-secondary-100); color: var(--color-success); }
.result-content { flex: 1; min-width: 0; }
.result-type { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-bottom: var(--space-1); display: flex; align-items: center; gap: var(--space-2); }
.grade-tag { font-size: 11px; padding: 2px 8px; border-radius: var(--radius-full); font-weight: var(--font-weight-semibold); }
.grade-tag.auto { background: var(--color-primary-50); color: var(--color-primary-600); }
.grade-tag.smart { background: var(--color-secondary-50); color: var(--color-secondary-600); }
.result-score { display: flex; align-items: baseline; gap: var(--space-1); }
.result-score .score-value { font-size: var(--font-size-xl); font-weight: var(--font-weight-bold); color: var(--color-text-primary); }
.result-score .score-total { font-size: var(--font-size-sm); color: var(--color-text-tertiary); }
.result-status { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; border-radius: 50%; flex-shrink: 0; }
.result-item.accepted .result-status { color: var(--color-success); background: rgba(34, 197, 94, 0.1); }
.result-item.pending .result-status { color: var(--color-warning); background: rgba(245, 158, 11, 0.1); }

.expand-icon { width: 22px; height: 22px; display: flex; align-items: center; justify-content: center; color: var(--color-text-tertiary); transition: all var(--transition-base); cursor: pointer; flex-shrink: 0; }
.expand-icon .el-icon { font-size: 16px; }
.result-item.expanded .expand-icon { transform: rotate(180deg); }

.result-detail {
  border-top: 1px solid var(--color-neutral-200);
  padding: var(--space-5);
  animation: slideDown 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  margin-top: var(--space-4);
  width: 100%;
}
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.detail-section { margin-bottom: var(--space-5); }
.detail-section:last-child { margin-bottom: 0; }
.detail-title { font-size: var(--font-size-xs); color: var(--color-text-tertiary); margin-bottom: var(--space-2); font-weight: var(--font-weight-semibold); text-transform: uppercase; letter-spacing: 0.5px; display: flex; align-items: center; gap: var(--space-2); }
.detail-title::before { content: ''; width: 8px; height: 8px; background: var(--color-primary-500); border-radius: 50%; flex-shrink: 0; }
.detail-content { font-size: var(--font-size-sm); color: var(--color-text-primary); line-height: 1.7; padding: var(--space-4); border-radius: var(--radius-base); word-break: break-all; border: 1px solid var(--color-neutral-200); background: var(--color-bg-elevated); }

.detail-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--space-4); margin-bottom: var(--space-5); }
.detail-item { border-radius: var(--radius-base); padding: var(--space-4); border: 1px solid var(--color-neutral-200); background: var(--color-bg-elevated); }
.detail-label { font-size: var(--font-size-xs); color: var(--color-text-tertiary); margin-bottom: var(--space-2); display: block; font-weight: var(--font-weight-medium); text-transform: uppercase; letter-spacing: 0.3px; }
.detail-value { font-size: var(--font-size-sm); padding: var(--space-3); border-radius: var(--radius-sm); word-break: break-all; line-height: 1.6; min-height: 60px; display: flex; align-items: center; }
.detail-value.student { background: var(--color-primary-50); color: var(--color-primary-700); border: 1px solid rgba(26, 143, 232, 0.2); }
.detail-value.correct { background: var(--color-secondary-50); color: var(--color-secondary-600); border: 1px solid rgba(34, 197, 94, 0.2); }

.detail-comment {
  font-size: var(--font-size-sm); color: var(--color-text-primary); line-height: 1.7;
  background: var(--color-primary-50); padding: var(--space-4);
  border-radius: var(--radius-base); border-left: 4px solid var(--color-primary-500);
}

.text-ellipsis { display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.text-ellipsis.expanded { -webkit-line-clamp: unset; overflow: visible; white-space: pre-wrap; word-break: break-all; }
.expand-text { display: block; text-align: right; margin-top: var(--space-2); color: var(--color-primary-500); font-size: var(--font-size-xs); cursor: pointer; padding-top: var(--space-2); border-top: 1px dashed var(--color-neutral-200); }

.section-actions { display: flex; gap: var(--space-2); }

.start-section { text-align: center; padding: var(--space-10) var(--space-5); }
.start-info { display: flex; flex-direction: column; align-items: center; margin-bottom: var(--space-6); }
.start-icon {
  width: 80px; height: 80px;
  background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-700));
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  margin-bottom: var(--space-4); color: #fff;
  box-shadow: var(--shadow-primary);
}
.start-icon .el-icon { font-size: 40px; color: #fff; }
.start-text { text-align: center; }
.start-title { font-size: var(--font-size-lg); font-weight: var(--font-weight-semibold); color: var(--color-text-primary); margin-bottom: var(--space-1); }
.start-desc { font-size: var(--font-size-sm); color: var(--color-text-tertiary); max-width: 360px; line-height: 1.5; }
.start-btn { width: 100%; height: 48px; font-size: var(--font-size-lg); background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-700)); border: none; box-shadow: var(--shadow-primary); }
.start-btn:hover { background: linear-gradient(135deg, var(--color-primary-600), var(--color-primary-800)); box-shadow: none; transform: translateY(-1px); }

.drawer-footer { display: flex; justify-content: flex-end; gap: var(--space-3); padding-top: var(--space-5); border-top: 1px solid var(--color-neutral-200); }
</style>