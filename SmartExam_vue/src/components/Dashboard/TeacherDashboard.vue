<template>
  <div>
    <div class="welcome-card welcome-card--teacher">
      <div class="welcome-bg">
        <div class="welcome-orb orb-1"></div>
        <div class="welcome-orb orb-2"></div>
      </div>
      <div class="welcome-body">
        <div class="welcome-text">
          <h1>{{ greeting }}，{{ user?.realName || user?.username }} 👋</h1>
          <p>管理您的考试、试题和学生成绩</p>
        </div>
        <div class="welcome-decoration">
          <el-icon :size="35" style="height: 100%;justify-content: center;"><Setting /></el-icon>
        </div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card stat-groups">
        <div class="stat-icon"><el-icon :size="24"><Avatar /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ groupCount }}</div>
          <div class="stat-label">我的用户组</div>
        </div>
      </div>
      <div class="stat-card stat-exams">
        <div class="stat-icon"><el-icon :size="24"><Notebook /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ examCount }}</div>
          <div class="stat-label">我的考试</div>
        </div>
      </div>
      <div class="stat-card stat-questions">
        <div class="stat-icon"><el-icon :size="24"><QuestionFilled /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ questionCount }}</div>
          <div class="stat-label">我的试题</div>
        </div>
      </div>
      <div class="stat-card stat-grading">
        <div class="stat-icon"><el-icon :size="24"><Document /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ gradingCount }}</div>
          <div class="stat-label">待判卷</div>
        </div>
      </div>
    </div>

    <div class="charts-grid">
      <div class="chart-card"><ExamPieChart :data="examStats" /></div>
      <div class="chart-card"><GradeProgressChart :data="examStats" /></div>
    </div>

    <div class="chart-card" style="margin-top: var(--space-5);">
      <ExamBarChart :data="examStats" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUser, isSuperAdmin } from '../../utils/auth'
import { getGroupList } from '../../api/group'
import { getExamList } from '../../api/exam'
import { getQuestionList } from '../../api/question'
import { getGradingStats } from '../../api/record'
import ExamPieChart from '../Charts/ExamPieChart.vue'
import ExamBarChart from '../Charts/ExamBarChart.vue'
import GradeProgressChart from '../Charts/GradeProgressChart.vue'
import { Avatar, Notebook, QuestionFilled, Document, Setting } from '@element-plus/icons-vue'

const user = getUser()
const greeting = (() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})()

const groupCount = ref(0)
const examCount = ref(0)
const questionCount = ref(0)
const gradingCount = ref(0)
const examStats = ref([])

const loadData = async () => {
  try {
    const userId = Number(user.id)
    const superAdmin = isSuperAdmin()
    const [groupRes, examRes, questionRes, gradingStatsRes] = await Promise.all([
      getGroupList(),
      getExamList({ pageNum: 1, pageSize: 100 }),
      getQuestionList({ pageNum: 1, pageSize: 100 }),
      getGradingStats({ pageNum: 1, pageSize: 100, creatorId: superAdmin ? null : userId })
    ])
    if (groupRes.code === 200) {
      const groups = groupRes.data.records || groupRes.data || []
      groupCount.value = superAdmin ? groups.length : groups.filter(g => Number(g.creatorId) === userId).length
    }
    if (examRes.code === 200) {
      const exams = examRes.data.records || examRes.data || []
      examCount.value = superAdmin ? exams.length : exams.filter(e => Number(e.creatorId) === userId).length
    }
    if (questionRes.code === 200) {
      const questions = questionRes.data.records || questionRes.data || []
      questionCount.value = superAdmin ? questions.length : questions.filter(q => Number(q.creatorId) === userId).length
    }
    if (gradingStatsRes.code === 200) {
      const stats = gradingStatsRes.data.records || gradingStatsRes.data || []
      examStats.value = stats
      gradingCount.value = stats.reduce((sum, exam) => sum + (exam.ungradedCount || 0), 0)
    }
  } catch (error) { console.error('加载数据失败', error) }
}

onMounted(() => { loadData() })
</script>
