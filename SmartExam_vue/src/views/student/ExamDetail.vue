<template>
  <div class="exam-page">
    <div class="watermark" v-if="userInfo">
      <div class="watermark-inner">
        <div v-for="i in 36" :key="i" class="watermark-item">
          {{ userInfo.username }} - {{ userInfo.realName }}
        </div>
      </div>
    </div>
    <div class="exam-header">
      <div v-if="!isReadOnly" class="header-right">
        <div class="timer" :class="{ warning: remainingTime < 300 }">
          <el-icon :size="22"><Clock /></el-icon>
          <span>{{ formatTime(remainingTime) }}</span>
        </div>
        <el-button type="danger" @click="handleExit">
          <el-icon style="margin-right: 4px;"><SwitchButton /></el-icon>
          退出考试
        </el-button>
      </div>
      <div v-else class="header-right">
        <el-tag type="info">试卷已提交，仅供查看</el-tag>
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </div>

    <div class="exam-body">
      <div class="exam-left">
        <div class="exam-info">
          <h3>考试信息</h3>
          <div class="info-item exam-title">
            <span class="label">考试名称：</span>
            <span>{{ exam.title }}</span>
          </div>
          <div class="info-item">
            <span class="label">开始时间：</span>
            <span>{{ formatDateTime(exam.startTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">结束时间：</span>
            <span>{{ formatDateTime(exam.endTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">考试时长：</span>
            <span>{{ exam.duration }} 分钟</span>
          </div>
          <div class="info-item">
            <span class="label">总分：</span>
            <span>{{ exam.totalScore }} 分</span>
          </div>
          <div class="info-item">
            <span class="label">题目数量：</span>
            <span>{{ questions.length }} 题</span>
          </div>
        </div>
      </div>

      <div class="exam-center">
        <div class="questions-container" v-for="(item, idx) in typeGroups" :key="idx">
          <div class="section-title">{{ item.title }}</div>
          <div
            v-for="(question, qIndex) in item.list"
            :key="question.id"
            :data-question-id="question.id"
            class="question-block"
          >
            <div class="question-header">
              <span class="question-num">{{ getQuestionNumber(question) }}.</span>
              <span class="question-type">{{ getTypeLabel(question.type) }}</span>
              <span class="question-score">{{ question.score }}分</span>
            </div>
            <div class="question-content">{{ question.content }}</div>

            <div v-if="question.type === 0 || question.type === 1" class="options">
              <div
                v-for="(option, index) in parseOptions(question.options)"
                :key="index"
                class="option-item"
                :class="{ selected: isOptionSelected(question, index), disabled: isReadOnly }"
                @click="!isReadOnly && selectOption(question, index)"
              >
                <span class="option-label">{{ getOptionLabel(index) }}.</span>
                <span class="option-content">{{ option }}</span>
              </div>
            </div>

            <div v-else-if="question.type === 2" class="options">
              <div
                class="option-item"
                :class="{ selected: getAnswer(question) === '正确', disabled: isReadOnly }"
                @click="!isReadOnly && selectOption(question, '正确')"
              >
                <span class="option-label">正确</span>
              </div>
              <div
                class="option-item"
                :class="{ selected: getAnswer(question) === '错误', disabled: isReadOnly }"
                @click="!isReadOnly && selectOption(question, '错误')"
              >
                <span class="option-label">错误</span>
              </div>
            </div>

            <div v-else class="answer-area">
              <textarea
                v-model="answers[question.id]"
                placeholder="请输入答案..."
                class="answer-input"
                :disabled="isReadOnly"
                @input="!isReadOnly && saveAnswersToStorage"
              ></textarea>
            </div>
          </div>
        </div>

        <div class="submit-area" v-if="showSubmitButton">
          <el-button type="primary" size="large" @click="submitExam">提交试卷</el-button>
        </div>
      </div>

      <div class="exam-right">
        <div class="nav-panel">
          <h3>题目导航</h3>
          <div v-for="(item, idx) in typeGroups" :key="idx" class="nav-section">
            <div class="nav-section-title">{{ item.title }}</div>
            <div class="question-nav">
              <div
                v-for="(question, qIndex) in item.list"
                :key="question.id"
                class="nav-item"
                :class="{ active: currentQuestionId === question.id, answered: getAnswer(question) !== undefined && getAnswer(question) !== '' }"
                @click="scrollToQuestion(question)"
              >
                <span class="nav-number">{{ getQuestionNumber(question) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="exitDialogVisible" title="退出考试" width="400px">
      <p>退出考试将自动提交试卷，确定要退出吗？</p>
      <template #footer>
        <el-button @click="exitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExit">确定退出并提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExamById, getExamQuestions } from '../../api/exam'
import { submitExam as submitExamApi, startExam, getExamStatus } from '../../api/record'
import { getUser } from '../../utils/auth'
import { formatDateTime } from '../../utils/format'
import { Clock, SwitchButton, CircleCheck, Edit } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const examId = route.params.id

const userInfo = ref(null)
const isReadOnly = ref(false)
const showSubmitButton = ref(true)

const exam = ref({
  title: '',
  duration: 120,
  totalScore: 100
})
const questions = ref([])
const answers = ref({})
const remainingTime = ref(0)
const recordId = ref(null)
const currentQuestionId = ref(null)
const exitDialogVisible = ref(false)
let timer = null

// 中文数字转换
const getChineseNum = (num) => {
  const arr = ['一','二','三','四','五','六','七','八','九','十']
  return arr[num] || ''
}

const getStorageKey = () => {
  const user = getUser()
  return `exam_${user?.id}_${examId}_answers`
}

const saveAnswersToStorage = () => {
  try {
    localStorage.setItem(getStorageKey(), JSON.stringify(answers.value))
  } catch (e) {
    console.error('保存答案失败', e)
  }
}

const loadAnswersFromStorage = () => {
  try {
    const saved = localStorage.getItem(getStorageKey())
    if (saved) {
      answers.value = JSON.parse(saved)
    }
  } catch (e) {
    console.error('加载答案失败', e)
  }
}

const clearAnswersFromStorage = () => {
  try {
    localStorage.removeItem(getStorageKey())
  } catch (e) {
    console.error('清除答案失败', e)
  }
}

// 核心：分组 + 动态标题
const typeGroups = computed(() => {
  // 按固定顺序分组
  const list0 = []
  const list1 = []
  const list2 = []
  const list3 = []
  const list4 = []

  questions.value.forEach(q => {
    switch(q.type){
      case 0: list0.push(q); break
      case 1: list1.push(q); break
      case 2: list2.push(q); break
      case 3: list3.push(q); break
      case 4: list4.push(q); break
    }
  })

  // 基础配置
  const base = [
    { name: '单选题', list: list0 },
    { name: '多选题', list: list1 },
    { name: '判断题', list: list2 },
    { name: '填空题', list: list3 },
    { name: '简答题', list: list4 }
  ]

  // 过滤空数据，再动态生成标题
  const valid = base.filter(item => item.list.length > 0)
  return valid.map((item, index) => {
    return {
      title: `${getChineseNum(index)}、${item.name}`,
      list: item.list
    }
  })
})

const answeredCount = computed(() => {
  return Object.values(answers.value).filter(a => a !== undefined && a !== '').length
})

const getQuestionNumber = (question) => {
  return questions.value.findIndex(q => q.id === question.id) + 1
}

const getTypeLabel = (type) => {
  const labels = { 0: '单选', 1: '多选', 2: '判断', 3: '填空', 4: '简答' }
  return labels[type] || '未知'
}

const parseOptions = (options) => {
  if (!options) return []
  try {
    const parsed = JSON.parse(options)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return [options]
  }
}

const getOptionLabel = (index) => {
  return String.fromCharCode(65 + index)
}

const getAnswer = (question) => {
  return answers.value[question.id]
}

const isOptionSelected = (question, index) => {
  const ans = getAnswer(question)
  if (question.type === 1) {
    return Array.isArray(ans) && ans.includes(index)
  }
  return ans === index
}

const selectOption = (question, index) => {
  if (question.type === 1) {
    if (!Array.isArray(answers.value[question.id])) {
      answers.value[question.id] = []
    }
    const arr = answers.value[question.id]
    const idx = arr.indexOf(index)
    if (idx > -1) {
      arr.splice(idx, 1)
    } else {
      arr.push(index)
    }
  } else {
    if (answers.value[question.id] === index) {
      delete answers.value[question.id]
    } else {
      answers.value[question.id] = index
    }
  }
  saveAnswersToStorage()
}

const scrollToQuestion = (question) => {
  currentQuestionId.value = question.id
  const element = document.querySelector(`[data-question-id="${question.id}"]`)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

const loadExamData = async () => {
  try {
    const user = getUser()

    const examRes = await getExamById(examId)
    if (examRes.code === 200 && examRes.data) {
      exam.value = examRes.data
    }

    const statusRes = await getExamStatus(user.id, parseInt(examId))
    let hasSubmitted = false
    if (statusRes.code === 200 && statusRes.data) {
      const record = statusRes.data
      if (record.status === 2) {
        hasSubmitted = true
      }
      if (record.recordId) {
        recordId.value = record.recordId
      }
    }

    const now = new Date()
    const endTime = new Date(exam.value.endTime)
    const startTime = new Date(exam.value.startTime)
    
    if (hasSubmitted || now > endTime) {
      isReadOnly.value = true
      showSubmitButton.value = false
    }

    if (!hasSubmitted && !isReadOnly.value) {
      const startRes = await startExam({
        userId: user.id,
        examId: parseInt(examId)
      })

      if (startRes.data) {
        recordId.value = startRes.data.id
      }

      if (exam.value) {
        const totalSeconds = exam.value.duration * 60

        if (startRes.data && startRes.data.startTime) {
          const startTimeVal = new Date(startRes.data.startTime).getTime()
          const nowTime = Date.now()
          const elapsedSeconds = Math.floor((nowTime - startTimeVal) / 1000)
          remainingTime.value = Math.max(0, totalSeconds - elapsedSeconds)
        } else {
          remainingTime.value = totalSeconds
        }
      }
    }

    const questionsRes = await getExamQuestions(examId)
    if (questionsRes.code === 200 && questionsRes.data) {
      questions.value = questionsRes.data
    }

    loadAnswersFromStorage()
  } catch (error) {
    console.error('加载考试数据失败', error)
    setTimeout(() => {
      router.push('/student/exam-list')
    }, 2000)
  }
}

const handleExit = () => {
  exitDialogVisible.value = true
}

const goBack = () => {
  router.push('/student/exam-list')
}

const confirmExit = () => {
  submitExam(true)
  exitDialogVisible.value = false
}

const submitExam = async (isAuto = false) => {
  if (!isAuto) {
    try {
      await ElMessageBox.confirm('确定要提交试卷吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
  }

  clearInterval(timer)
  try {
    const user = getUser()

    const formattedAnswers = {}
    questions.value.forEach(q => {
      const answer = answers.value[q.id]
      if (answer !== undefined && answer !== null && answer !== '') {
        if (q.type === 0) {
          formattedAnswers[q.id] = String.fromCharCode(65 + answer)
        } else if (q.type === 1) {
          if (Array.isArray(answer)) {
            formattedAnswers[q.id] = answer.map(i => String.fromCharCode(65 + i)).sort().join(',')
          }
        } else if (q.type === 2) {
          formattedAnswers[q.id] = answer === 0 ? '正确' : '错误'
        } else if (q.type === 3) {
          if (Array.isArray(answer)) {
            formattedAnswers[q.id] = answer.join(',')
          } else {
            formattedAnswers[q.id] = answer
          }
        } else {
          formattedAnswers[q.id] = answer
        }
      }
    })

    const recordData = {
      userId: user.id,
      examId: parseInt(examId),
      answers: JSON.stringify(formattedAnswers)
    }
    await submitExamApi(recordData)
    clearAnswersFromStorage()
    ElMessage.success(isAuto ? '考试已自动提交' : '试卷提交成功！')
    router.push('/student/exam-list')
  } catch (error) {
    ElMessage.error('提交失败，请重试')
  }
}

onMounted(() => {
  userInfo.value = getUser()
  loadExamData().then(() => {
    if (!isReadOnly.value) {
      timer = setInterval(() => {
        if (remainingTime.value > 0) {
          remainingTime.value--
          if (remainingTime.value === 0) {
            submitExam(true)
          }
        }
      }, 1000)
    }
  })
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.exam-page {
  height: 100vh;
  background: linear-gradient(135deg, var(--color-bg-base) 0%, var(--color-primary-50) 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* ── Watermark ──────────────────────────────────────────── */
.watermark {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  pointer-events: none;
  z-index: 999;
  overflow: hidden;
}
.watermark-inner {
  position: absolute;
  top: -50%; left: -50%;
  width: 200%; height: 200%;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  grid-template-rows: repeat(6, 1fr);
  transform: rotate(-25deg);
}
.watermark-item {
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 600;
  color: #000000;
  opacity: 0.15;
  white-space: nowrap; user-select: none;
  letter-spacing: 2px;
}

/* ── Header ─────────────────────────────────────────────── */
.exam-header {
  background: var(--color-bg-elevated);
  padding: var(--space-3) var(--space-6);
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-bottom: 1px solid var(--color-neutral-200);
  box-shadow: var(--shadow-sm);
  z-index: 10;
  flex-shrink: 0;
}
.header-right { display: flex; align-items: center; gap: var(--space-5); }

.timer {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-4);
  background: var(--color-primary-50);
  border-radius: var(--radius-xl);
  border: 2px solid var(--color-primary-200);
  font-size: 22px;
  font-weight: var(--font-weight-bold);
  color: var(--color-primary-700);
  font-variant-numeric: tabular-nums;
  transition: all var(--transition-base);
  letter-spacing: 1px;
}
.timer .el-icon { color: var(--color-primary-500); }
.timer.warning {
  background: var(--color-danger-50);
  border-color: var(--color-danger);
  color: var(--color-danger);
  animation: timerPulse 1s ease-in-out infinite;
}
.timer.warning .el-icon { color: var(--color-danger); }

@keyframes timerPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

/* ── Body ───────────────────────────────────────────────── */
.exam-body {
  display: flex;
  flex: 1;
  padding: var(--space-5);
  gap: var(--space-5);
  min-height: 0;
}

/* ── Left Panel ─────────────────────────────────────────── */
.exam-left { width: 240px; flex-shrink: 0; }

.exam-info {
  background: var(--color-bg-elevated);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  border: 1px solid var(--color-neutral-200);
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition-base);
}
.exam-info:hover { box-shadow: var(--shadow-md); }
.exam-info h3 {
  margin: 0 0 var(--space-4) 0;
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
  padding-bottom: var(--space-3);
  border-bottom: 2px solid var(--color-primary-500);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.info-item {
  margin-bottom: var(--space-3);
  color: var(--color-text-secondary);
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  font-size: var(--font-size-sm);
  line-height: 1.6;
}
.info-item .label { font-weight: var(--font-weight-medium); color: var(--color-text-tertiary); margin-right: var(--space-1); }
.info-item.exam-title span:last-child { font-weight: var(--font-weight-semibold); color: var(--color-primary-700); }

/* ── Center Panel ───────────────────────────────────────── */
.exam-center {
  flex: 1;
  background: var(--color-bg-elevated);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  overflow-y: auto;
  border: 1px solid var(--color-neutral-200);
  box-shadow: var(--shadow-sm);
}

.questions-container {
  margin-bottom: var(--space-6);
  padding: var(--space-5);
  background: var(--color-neutral-50);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-neutral-200);
}
.questions-container:last-of-type { margin-bottom: 0; }

.section-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-2);
  border-bottom: 3px solid var(--color-primary-500);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.question-block {
  margin-bottom: var(--space-5);
  padding: var(--space-5);
  background: var(--color-bg-elevated);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-neutral-200);
  transition: all var(--transition-base);
}
.question-block:last-child { margin-bottom: 0; }
.question-block:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-primary-200);
}

.question-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}
.question-num { font-size: var(--font-size-lg); font-weight: var(--font-weight-bold); color: var(--color-text-primary); }
.question-type {
  padding: 3px 12px;
  background: linear-gradient(135deg, var(--color-primary-500), var(--color-primary-600));
  color: #fff;
  border-radius: var(--radius-xl);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.5px;
}
.question-score {
  color: var(--color-warning);
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-sm);
  margin-left: auto;
  background: var(--color-warning-50);
  padding: 2px 10px;
  border-radius: var(--radius-sm);
}

.question-content {
  font-size: var(--font-size-md);
  line-height: 1.8;
  color: var(--color-text-primary);
  margin-bottom: var(--space-4);
  padding: var(--space-3);
  background: var(--color-neutral-50);
  border-radius: var(--radius-base);
  border-left: 3px solid var(--color-primary-300);
}

.options {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.option-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--color-bg-elevated);
  border: 1.5px solid var(--color-neutral-300);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
  overflow: hidden;
}
.option-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 4px;
  height: 100%;
  background: transparent;
  transition: background var(--transition-fast);
}
.option-item:hover {
  border-color: var(--color-primary-400);
  background: var(--color-primary-50);
  transform: translateX(3px);
}
.option-item:hover::before { background: var(--color-primary-400); }
.option-item.selected {
  border-color: var(--color-primary-500);
  background: var(--color-primary-50);
  transform: translateX(3px);
  box-shadow: 0 0 0 1px var(--color-primary-100);
}
.option-item.selected::before { background: var(--color-primary-500); }
.option-label {
  font-weight: var(--font-weight-bold);
  color: var(--color-text-secondary);
  min-width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
}
.option-item.selected .option-label {
  color: var(--color-primary-600);
  background: var(--color-primary-100);
}
.option-content {
  line-height: 1.6;
  color: var(--color-text-primary);
  transition: color var(--transition-fast);
}
.option-item.selected .option-content { color: var(--color-primary-700); }

.answer-area { margin-top: var(--space-4); }
.answer-input {
  width: 100%;
  min-height: 100px;
  padding: var(--space-4);
  border: 1.5px solid var(--color-neutral-300);
  border-radius: var(--radius-lg);
  font-size: var(--font-size-sm);
  resize: vertical;
  font-family: inherit;
  transition: all var(--transition-fast);
  line-height: 1.6;
  background: var(--color-bg-elevated);
}
.answer-input:focus {
  outline: none;
  border-color: var(--color-primary-500);
  box-shadow: 0 0 0 3px var(--color-primary-100);
  background: var(--color-primary-50);
}

.submit-area {
  display: flex;
  justify-content: center;
  padding: var(--space-6) 0 var(--space-3);
}

/* ── Right Panel ────────────────────────────────────────── */
.exam-right { width: 200px; flex-shrink: 0; }

.nav-panel {
  background: var(--color-bg-elevated);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  border: 1px solid var(--color-neutral-200);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  transition: box-shadow var(--transition-base);
}
.nav-panel:hover { box-shadow: var(--shadow-md); }
.nav-panel h3 {
  margin: 0 0 var(--space-4) 0;
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
  padding-bottom: var(--space-3);
  border-bottom: 2px solid var(--color-primary-500);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.nav-section { margin-bottom: var(--space-5); }
.nav-section:last-child { margin-bottom: 0; }
.nav-section-title {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-2);
  padding-bottom: var(--space-1);
  border-bottom: 1px solid var(--color-neutral-200);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.question-nav {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-1);
}
.nav-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid var(--color-neutral-300);
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  transition: all var(--transition-fast);
  color: var(--color-text-secondary);
  position: relative;
}
.nav-item:hover {
  border-color: var(--color-primary-400);
  color: var(--color-primary-600);
  background: var(--color-primary-50);
  transform: scale(1.08);
  box-shadow: var(--shadow-sm);
}
.nav-item.active {
  border-color: var(--color-primary-500);
  background: var(--color-primary-500);
  color: #fff;
  transform: scale(1.08);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.35);
}
.nav-item.answered {
  background: var(--color-success);
  border-color: var(--color-success);
  color: #fff;
  box-shadow: 0 2px 6px rgba(34, 197, 94, 0.3);
}
.nav-item.answered:hover {
  background: var(--color-secondary-600);
  border-color: var(--color-secondary-600);
  color: #fff;
  transform: scale(1.08);
  box-shadow: 0 2px 8px rgba(34, 197, 94, 0.4);
}
.nav-item.active.answered {
  background: var(--color-primary-500);
  border-color: var(--color-primary-500);
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.35);
}

.nav-number {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: inherit;
}
</style>
