<template>
  <div class="score-detail">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>{{ examTitle || '成绩详情' }}</h2>
    </div>

    <el-card class="info-card">
      <div class="detail-header">
        <h3>{{ examTitle || '未知考试' }}</h3>
        <el-tag type="success" size="large">
          得分: {{ score || 0 }} / {{ totalScore || 100 }}
        </el-tag>
      </div>
      <div class="detail-info">
        <div class="info-row">
          <span class="label">考试时间：</span>
          <span>{{ formatDateTime(startTime) }} ~ {{ formatDateTime(endTime) }}</span>
        </div>
        <div class="info-row">
          <span class="label">提交时间：</span>
          <span>{{ formatDateTime(submitTime) }}</span>
        </div>
        <div class="info-row">
          <span class="label">状态：</span>
          <span :style="{ color: status === '已批阅' ? '#67C23A' : '#E6A23C' }">{{ status }}</span>
        </div>
      </div>
    </el-card>

    <el-card class="questions-card" v-if="answerList.length > 0">
      <h3>答题详情</h3>
      <div v-for="(item, index) in answerList" :key="item.id" class="question-item">
        <div class="question-header">
          <span class="question-num">第 {{ index + 1 }} 题</span>
          <span class="question-type">{{ getQuestionTypeName(item.type) }}</span>
          <span class="question-score">满分：{{ item.examScore || 0 }} 分</span>
          <span :class="['question-result', item.givenScore === (item.examScore || 0) ? 'correct' : 'wrong']">
            <el-icon style="margin-right: 2px;">
              <CircleCheckFilled v-if="item.givenScore === (item.examScore || 0)" />
              <CircleCloseFilled v-else />
            </el-icon>
            得分：{{ item.givenScore ?? 0 }} 分
          </span>
          <span class="btn-group">
            <el-button type="primary" size="small" plain @click="openSolution(item)">
              <el-icon><Reading /></el-icon>
              题解
            </el-button>
            <el-button type="warning" size="small" plain @click="openAISolution(item)">
              <el-icon><Lightning /></el-icon>
              智能解析
            </el-button>
          </span>
        </div>
        <div class="question-content">{{ item.content || '无题目内容' }}</div>
        <div class="question-options" v-if="item.options">
          <div v-for="(opt, optIdx) in parseOptions(item.options)" :key="optIdx"
               :class="['option-item', isCorrectOption(item, optIdx) ? 'correct' : isUserOption(item, optIdx) ? 'wrong' : '']">
            {{ String.fromCharCode(65 + optIdx) }}. {{ opt }}
          </div>
        </div>
        <div class="answer-section">
          <div class="student-answer">
            <span class="label">你的答案：</span>
            <span :class="['value', isAnswerCorrect(item) ? 'correct' : 'wrong']">
              {{ item.userAnswer || '未作答' }}
            </span>
          </div>
          <div class="correct-answer" v-if="item.answer">
            <span class="label">正确答案：</span>
            <span class="value correct">{{ item.answer }}</span>
          </div>
        </div>
        <div class="analysis-section" v-if="item.analysis">
          <div class="analysis-header">
            <el-icon><InfoFilled /></el-icon>
            解析
          </div>
          <div class="analysis-content">{{ item.analysis }}</div>
        </div>
        <div class="comment-section" v-if="item.comment">
          <div class="comment-header">
            <el-icon><InfoFilled /></el-icon>
            老师评语
          </div>
          <div class="comment-content">{{ item.comment }}</div>
        </div>
      </div>
    </el-card>

    <el-card v-else-if="status === '已批阅'" class="empty-card">
      <el-empty description="暂无答题记录" />
    </el-card>

    <el-card v-else class="empty-card">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>试卷尚未批阅，请等待老师批改</template>
      </el-alert>
    </el-card>

    <!-- 题解抽屉弹窗 -->
    <el-drawer
      v-model="solutionDrawerVisible"
      :title="isAIMode ? 'AI 智能解析' : '题目解析'"
      direction="rtl"
      size="620px"
    >
      <div class="solution-drawer-body">
        <template v-if="currentQuestion">
          <!-- 题目信息区 -->
          <div class="solution-question-section">
            <div class="solution-meta">
              <el-tag size="small" type="primary">{{ getQuestionTypeName(currentQuestion.type) }}</el-tag>
              <el-tag size="small" :type="currentQuestion.givenScore === (currentQuestion.examScore || 0) ? 'success' : 'danger'">
                得分：{{ currentQuestion.givenScore ?? 0 }} / {{ currentQuestion.examScore || 0 }}
              </el-tag>
            </div>
            <div class="solution-question-text">{{ currentQuestion.content || '无题目内容' }}</div>
            <div class="solution-options" v-if="currentQuestion.options">
              <div v-for="(opt, optIdx) in parseOptions(currentQuestion.options)" :key="optIdx"
                   :class="['solution-option', isCorrectOption(currentQuestion, optIdx) ? 'correct' : isUserOption(currentQuestion, optIdx) ? 'wrong' : '']">
                <span class="option-label">{{ String.fromCharCode(65 + optIdx) }}</span>
                <span class="option-text">{{ opt }}</span>
              </div>
            </div>
          </div>

          <!-- 答案对比区 -->
          <div class="solution-answer-section">
            <div class="answer-compare-row">
              <div class="answer-compare-item">
                <div class="compare-label">你的答案</div>
                <div :class="['compare-value', isAnswerCorrect(currentQuestion) ? 'correct' : 'wrong']">
                  {{ currentQuestion.userAnswer || '未作答' }}
                </div>
              </div>
              <div class="answer-compare-item" v-if="currentQuestion.answer">
                <div class="compare-label">正确答案</div>
                <div class="compare-value correct">{{ currentQuestion.answer }}</div>
              </div>
            </div>
          </div>

          <el-divider />

          <!-- 题解内容区 -->
          <div class="solution-content-section">
            <div class="solution-title">
              <el-icon><Reading /></el-icon>
              详细题解
            </div>

            <!-- AI 加载骨架屏 -->
            <div v-if="solutionLoading" class="solution-loading">
              <div class="loading-header">
                <div class="loading-spinner">
                  <el-icon class="is-loading" :size="20"><Reading /></el-icon>
                </div>
                <span class="loading-title">AI 正在分析题目...</span>
              </div>
              <div class="loading-skeleton">
                <div class="skeleton-block" style="width: 65%"></div>
                <div class="skeleton-block" style="width: 100%"></div>
                <div class="skeleton-block" style="width: 85%"></div>
                <div class="skeleton-block" style="width: 45%"></div>
                <div class="skeleton-block" style="width: 90%"></div>
                <div class="skeleton-block" style="width: 70%"></div>
                <div class="skeleton-block" style="width: 55%"></div>
                <div class="skeleton-block" style="width: 80%"></div>
              </div>
            </div>

            <!-- 题解内容 -->
            <div v-else-if="solutionText" class="solution-text" v-html="solutionHtml"></div>
            <el-empty v-else description="暂无题解" />
          </div>
        </template>

        <el-empty v-else description="暂无题目数据" />
      </div>

      <template #footer>
        <div class="solution-drawer-footer">
          <el-button @click="closeSolutionDrawer">关闭</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRecordById, getRecordAnswers } from '../../api/record'
import { getQuestionSolution } from '../../api/question'
import { formatDateTime } from '../../utils/format'
import { ArrowLeft, CircleCheckFilled, CircleCloseFilled, InfoFilled, Reading, Lightning } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const examTitle = ref('')
const score = ref(0)
const totalScore = ref(100)
const startTime = ref('')
const endTime = ref('')
const submitTime = ref('')
const status = ref('')
const answerList = ref([])

//题解抽屉状态
const solutionDrawerVisible = ref(false)
const solutionLoading = ref(false)
const solutionText = ref('')
const currentQuestion = ref(null)
const isAIMode = ref(false) //是否为AI生成的解析

//打开题解抽屉（显示数据库已有解析）
const openSolution = (item) => {
  currentQuestion.value = item
  isAIMode.value = false
  solutionDrawerVisible.value = true
  solutionLoading.value = false
  solutionText.value = item.analysis || '暂无题解'
}

//AI智能解析
const openAISolution = async (item) => {
  currentQuestion.value = item
  isAIMode.value = true
  solutionDrawerVisible.value = true
  solutionLoading.value = true
  solutionText.value = ''

  try {
    const res = await getQuestionSolution({
      questionContent: item.content,
      options: item.options || '',
      correctAnswer: item.answer || '',
      type: item.type,
      studentAnswer: item.userAnswer || '',
      existingAnalysis: '' //传入空字符串，强制AI生成
    })
    if (res.code === 200) {
      solutionText.value = res.data.solution || '暂无题解'
    } else {
      solutionText.value = '题解获取失败：' + (res.message || '未知错误')
    }
  } catch (error) {
    solutionText.value = '题解获取失败，请稍后重试'
    console.error('获取题解失败:', error)
  } finally {
    solutionLoading.value = false
  }
}

//关闭题解抽屉
const closeSolutionDrawer = () => {
  solutionDrawerVisible.value = false
  currentQuestion.value = null
  solutionText.value = ''
  isAIMode.value = false
}

//将Markdown格式的题解转为HTML（原生渲染）
const renderMarkdown = (text) => {
  if (!text) return ''
  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 表格
  html = html.replace(/^\|(.+)\|$/gm, (m) => {
    const cells = m.slice(1, -1).split('|').map(c => c.trim())
    return '<tr><td>' + cells.join('</td><td>') + '</td></tr>'
  })
  html = html.replace(/(<tr>.*<\/tr>\n?)+/g, '<table><tbody>$&</tbody></table>')
  html = html.replace(/<table><tbody>(<tr><td>[^<]+<\/td>(?:<\/tr>)?)\n?<tr><td>[-:]+\|?(?:[-:| ]+)<\/td>(?:<\/tr>)?\n?(.*?)<\/tbody><\/table>/g,
    '<table><thead><tr>$1</tr></thead><tbody>$2</tbody></table>'.replace(/<\/?td>/g, m => m === '</td>' ? '</th>' : '<th>'))

  // **bold** → *italic* → `code`
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')

  // ## heading
  html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>')
  html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>')

  // - list
  html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
  html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')

  // 换行
  html = html.replace(/\n/g, '<br>')
  return html
}

const solutionHtml = computed(() => renderMarkdown(solutionText.value))

const getQuestionTypeName = (type) => {
  const typeMap = { 0: '单选题', 1: '多选题', 2: '判断题', 3: '填空题', 4: '简答题' }
  return typeMap[type] || '未知'
}

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

const isCorrectOption = (item, optIdx) => {
  if (!item.answer || item.type !== 0) return false
  return item.answer === String.fromCharCode(65 + optIdx)
}

const isUserOption = (item, optIdx) => {
  if (!item.userAnswer || item.type !== 0) return false
  return item.userAnswer === String.fromCharCode(65 + optIdx)
}

const isAnswerCorrect = (item) => {
  if (!item.userAnswer || !item.answer) return false
  return item.userAnswer === item.answer
}

const goBack = () => {
  router.push('/student/my-score')
}

const loadRecord = async () => {
  const recordId = route.params.id
  if (!recordId) {
    ElMessage.error('无效的记录ID')
    return
  }

  try {
    const res = await getRecordById(recordId)
    if (res.code === 200) {
      const data = res.data
      examTitle.value = data.examTitle || ''
      score.value = data.score || 0
      totalScore.value = data.totalScore || 100
      startTime.value = data.startTime || ''
      endTime.value = data.endTime || ''
      submitTime.value = data.submitTime || ''
      status.value = data.gradingStatus || ''

      if (status.value === '已批阅') {
        const answersRes = await getRecordAnswers(recordId)
        if (answersRes.code === 200) {
          answerList.value = (answersRes.data || []).map(item => ({
            ...item,
            givenScore: item.score
          }))
        }
      }
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载成绩详情失败')
  }
}

onMounted(() => {
  loadRecord()
})
</script>

<style scoped>
.score-detail {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}
.page-header h2 {
  margin: 0;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}
.page-header .el-button {
  border-radius: var(--radius-base);
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
  gap: var(--space-1);
}
.page-header .el-button:hover {
  transform: translateX(-2px);
}

.info-card {
  margin-bottom: var(--space-5);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-neutral-200);
  overflow: hidden;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--color-neutral-200);
}
.detail-header h3 {
  font-size: var(--font-size-xl);
  color: var(--color-text-primary);
  margin: 0;
  font-weight: var(--font-weight-semibold);
}
.detail-header :deep(.el-tag--success) {
  border-radius: var(--radius-base);
  padding: 0 var(--space-4);
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-base);
  border: none;
  background: var(--color-secondary-50);
  color: var(--color-success);
}

.detail-info {
  padding: var(--space-5);
  background: linear-gradient(135deg, var(--color-primary-50) 0%, var(--color-neutral-50) 100%);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-neutral-200);
}

.info-row {
  margin-bottom: var(--space-3);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
}
.info-row:last-child { margin-bottom: 0; }
.info-row .label {
  color: var(--color-text-tertiary);
  margin-right: var(--space-2);
  min-width: 80px;
  font-weight: var(--font-weight-medium);
}

.questions-card {
  margin-top: var(--space-5);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-neutral-200);
}
.questions-card > :deep(.el-card__body) {
  padding: var(--space-6);
}
.questions-card h3 {
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
  margin: 0 0 var(--space-5) 0;
  font-weight: var(--font-weight-semibold);
  position: relative;
  padding-left: var(--space-4);
}
.questions-card h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: var(--color-primary-500);
  border-radius: 2px;
}

.question-item {
  border: 1px solid var(--color-neutral-200);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  margin-bottom: var(--space-4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: #fff;
}
.question-item:hover {
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.08);
  border-color: var(--color-primary-200);
}
.question-item:last-child { margin-bottom: 0; }

.question-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-3);
  flex-wrap: wrap;
  padding-bottom: var(--space-3);
  border-bottom: 1px dashed var(--color-neutral-150);
}
.question-num {
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-500);
  font-size: var(--font-size-sm);
  background: var(--color-primary-50);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
}
.question-type {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
  background: var(--color-neutral-100);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
}
.question-score {
  color: var(--color-success);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}
.question-result {
  margin-left: auto;
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
  padding: 2px var(--space-3);
  border-radius: var(--radius-base);
  display: flex;
  align-items: center;
  gap: 2px;
}
.question-result.correct {
  color: var(--color-success);
  background: var(--color-secondary-50);
}
.question-result.wrong {
  color: var(--color-danger);
  background: var(--color-accent-50);
}
.btn-group {
  display: flex;
  gap: var(--space-2);
}

.question-content {
  font-size: var(--font-size-md);
  line-height: 1.7;
  margin-bottom: var(--space-4);
  color: var(--color-text-primary);
  padding: var(--space-3) var(--space-4);
  background: var(--color-neutral-50);
  border-radius: var(--radius-base);
  border: 1px solid var(--color-neutral-100);
}

.question-options { margin-bottom: var(--space-3); }
.option-item {
  padding: var(--space-2) var(--space-4);
  background: var(--color-neutral-50);
  border-radius: var(--radius-base);
  margin-bottom: var(--space-2);
  border-left: 3px solid var(--color-neutral-300);
  font-size: var(--font-size-sm);
  transition: all var(--transition-base);
}
.option-item.correct {
  background: var(--color-secondary-50);
  border-left-color: var(--color-success);
  color: var(--color-success);
  font-weight: var(--font-weight-medium);
}
.option-item.wrong {
  background: var(--color-accent-50);
  border-left-color: var(--color-danger);
  color: var(--color-danger);
  font-weight: var(--font-weight-medium);
}

.answer-section {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  padding-top: var(--space-3);
  border-top: 1px dashed var(--color-neutral-200);
  margin-bottom: var(--space-3);
}
.answer-section > div {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.answer-section .label {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
  white-space: nowrap;
}
.answer-section .value {
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
  padding: 1px var(--space-2);
  border-radius: var(--radius-sm);
}
.answer-section .value.correct {
  color: var(--color-success);
  background: var(--color-secondary-50);
}
.answer-section .value.wrong {
  color: var(--color-danger);
  background: var(--color-accent-50);
}

.analysis-section {
  padding: var(--space-4);
  background: var(--color-primary-50);
  border-radius: var(--radius-base);
  border-left: 3px solid var(--color-primary-400);
  margin-top: var(--space-2);
}
.analysis-header {
  font-size: var(--font-size-sm);
  color: var(--color-primary-600);
  margin-bottom: var(--space-2);
  font-weight: var(--font-weight-semibold);
  display: flex;
  align-items: center;
  gap: var(--space-1);
}
.analysis-content {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.comment-section {
  padding: var(--space-4);
  background: #fffbe6;
  border-radius: var(--radius-base);
  margin-top: var(--space-3);
  border-left: 3px solid var(--color-warning);
}
.comment-header {
  font-size: var(--font-size-sm);
  color: #d48806;
  margin-bottom: var(--space-2);
  font-weight: var(--font-weight-semibold);
  display: flex;
  align-items: center;
  gap: var(--space-1);
}
.comment-content {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.empty-card {
  margin-top: var(--space-5);
  border-radius: var(--radius-lg);
}

/* ── 题解抽屉 ────────────────────────────────── */
.solution-drawer-body {
  padding: var(--space-2) var(--space-1);
  height: calc(100vh - 140px);
  overflow-y: auto;
}

.solution-question-section {
  background: var(--color-neutral-50);
  border: 1px solid var(--color-neutral-200);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  margin-bottom: var(--space-4);
}

.solution-meta {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.solution-question-text {
  font-size: var(--font-size-md);
  line-height: 1.7;
  color: var(--color-text-primary);
  margin-bottom: var(--space-3);
  padding: var(--space-3);
  background: #fff;
  border-radius: var(--radius-base);
  border: 1px solid var(--color-neutral-100);
}

.solution-options {
  margin-top: var(--space-2);
}

.solution-option {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-base);
  margin-bottom: var(--space-2);
  background: #fff;
  border: 1px solid var(--color-neutral-150);
  transition: all var(--transition-base);
}

.solution-option.correct {
  background: var(--color-secondary-50);
  border-color: var(--color-success);
}

.solution-option.wrong {
  background: var(--color-accent-50);
  border-color: var(--color-danger);
}

.solution-option .option-label {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
  background: var(--color-neutral-100);
  color: var(--color-text-secondary);
  flex-shrink: 0;
}

.solution-option.correct .option-label {
  background: var(--color-success);
  color: #fff;
}

.solution-option.wrong .option-label {
  background: var(--color-danger);
  color: #fff;
}

.solution-option .option-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}

.solution-answer-section {
  background: #fff;
  border: 1px solid var(--color-neutral-200);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  margin-bottom: var(--space-2);
}

.answer-compare-row {
  display: flex;
  gap: var(--space-4);
}

.answer-compare-item {
  flex: 1;
}

.compare-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-1);
  font-weight: var(--font-weight-medium);
}

.compare-value {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-base);
  background: var(--color-neutral-50);
  border: 1px solid var(--color-neutral-200);
}

.compare-value.correct {
  color: var(--color-success);
  background: var(--color-secondary-50);
  border-color: var(--color-success);
}

.compare-value.wrong {
  color: var(--color-danger);
  background: var(--color-accent-50);
  border-color: var(--color-danger);
}

.solution-content-section {
  padding: var(--space-2) 0;
}

.solution-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-600);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 2px solid var(--color-primary-100);
}

.solution-text {
  font-size: var(--font-size-sm);
  line-height: 1.8;
  color: var(--color-text-primary);
  white-space: normal;
  word-break: break-word;
  padding: var(--space-3);
  background: #fff;
  border-radius: var(--radius-base);
  border: 1px solid var(--color-neutral-100);
}
.solution-text :deep(h1),
.solution-text :deep(h2),
.solution-text :deep(h3),
.solution-text :deep(h4) {
  margin: 1em 0 0.5em;
  color: var(--color-text-primary);
  font-weight: var(--font-weight-semibold);
}
.solution-text :deep(h2) {
  font-size: var(--font-size-md);
  padding-bottom: 6px;
  border-bottom: 1px solid var(--color-neutral-150);
}
.solution-text :deep(h3) { font-size: var(--font-size-sm); }
.solution-text :deep(p) { margin: 0 0 0.8em; }
.solution-text :deep(ul),
.solution-text :deep(ol) {
  padding-left: 1.5em;
  margin-bottom: 0.8em;
}
.solution-text :deep(li) { margin-bottom: 0.3em; }
.solution-text :deep(strong) { font-weight: var(--font-weight-semibold); }
.solution-text :deep(em) { font-style: italic; }
.solution-text :deep(code) {
  background: var(--color-neutral-100);
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 0.9em;
}
.solution-text :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 0.8em;
  font-size: var(--font-size-sm);
}
.solution-text :deep(th),
.solution-text :deep(td) {
  border: 1px solid var(--color-neutral-300);
  padding: 6px 10px;
  text-align: left;
}
.solution-text :deep(th) {
  background: var(--color-neutral-100);
  font-weight: var(--font-weight-semibold);
}
.solution-text :deep(tr:nth-child(even)) {
  background: var(--color-neutral-50);
}

.solution-drawer-footer {
  display: flex;
  justify-content: flex-end;
  padding: var(--space-3) 0;
}

/* ── 骨架屏加载 ──────────────────────────────── */
.solution-loading {
  padding: var(--space-6) var(--space-4);
  text-align: center;
}
.loading-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  margin-bottom: var(--space-6);
}
.loading-spinner {
  color: var(--color-primary-500);
  animation: spin 1.2s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.loading-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-600);
  background: linear-gradient(90deg, var(--color-primary-500), var(--color-primary-300));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.loading-skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: 0 var(--space-2);
}
.skeleton-block {
  height: 14px;
  border-radius: 7px;
  background: linear-gradient(90deg,
    var(--color-neutral-100) 25%,
    var(--color-neutral-50) 50%,
    var(--color-neutral-100) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.6s ease-in-out infinite;
}
.skeleton-block:nth-child(2) { height: 40px; border-radius: 6px; }
.skeleton-block:nth-child(4) { width: 55% !important; }
.skeleton-block:nth-child(5) { height: 20px; border-radius: 6px; }
.skeleton-block:nth-child(7) { width: 60% !important; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
