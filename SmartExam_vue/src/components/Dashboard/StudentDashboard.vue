<template>
  <div>
    <div class="welcome-card welcome-card--student">
      <div class="welcome-bg">
        <div class="welcome-orb orb-1"></div>
        <div class="welcome-orb orb-2"></div>
      </div>
      <div class="welcome-body">
        <div class="welcome-text">
          <h1>{{ greeting }}，{{ user?.realName || user?.username }} 👋</h1>
          <p>查看您的考试、成绩和学习进度</p>
        </div>
        <div class="welcome-decoration">
          <el-icon :size="35" style="height: 100%;justify-content: center;"><School /></el-icon>
        </div>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card stat-exams">
        <div class="stat-icon"><el-icon :size="24"><Notebook /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ availableExamCount }}</div>
          <div class="stat-label">我的考试总数</div>
        </div>
      </div>
      <div class="stat-card stat-completed">
        <div class="stat-icon"><el-icon :size="24"><CircleCheckFilled /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ completedExamCount }}</div>
          <div class="stat-label">已完成考试</div>
        </div>
      </div>
      <div class="stat-card stat-score">
        <div class="stat-icon"><el-icon :size="24"><StarFilled /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ avgScore }}</div>
          <div class="stat-label">平均分数</div>
        </div>
      </div>
      <div class="stat-card stat-groups">
        <div class="stat-icon"><el-icon :size="24"><Avatar /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ joinedGroupCount }}</div>
          <div class="stat-label">已加入用户组</div>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <div class="section-card">
        <div class="section-card-header">
          <h3>我的最近考试</h3>
          <el-button size="small" text @click="$router.push('/student/exam-list')">查看全部 →</el-button>
        </div>
        <el-table :data="recentExamOverview" stripe empty-text="暂无数据">
          <el-table-column prop="title" label="考试名称" />
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getExamTagType(scope.row)" size="small">{{ getExamStatusLabel(scope.row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="得分" width="130">
            <template #default="scope">
              <span v-if="scope.row.score != null" :class="['score-badge', scoreLevel(scope.row.score)]">
                {{ scope.row.score }} / {{ scope.row.totalScore || 100 }}
              </span>
              <span v-else class="score-badge pending">待批阅</span>
            </template>
          </el-table-column>
          <el-table-column label="开始时间" width="230">
            <template #default="scope">{{ formatTime(scope.row.startTime) }}</template>
          </el-table-column>
          <el-table-column label="结束时间" width="230">
            <template #default="scope">{{ formatTime(scope.row.endTime) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>

<div class="charts-grid" style="margin-top: var(--space-5); display: flex; gap: var(--space-5);">
  <!-- 左侧：自动占满剩余宽度，min-width:0 必加防止图表被压缩截断 -->
  <div class="chart-card" style="height: 350px; flex: 1; min-width: 0; overflow: hidden;">
    <ScoreLineChart :data="myRecentScores" />
  </div>
  <!-- 右侧固定60% -->
  <div class="chart-card" style="height: 350px; width: 40%; min-width: 0; overflow: hidden;">
    <StudentExamPieChart :completed="completedExamCount" :not-taken="notTakenCount" />
  </div>
</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUser } from '../../utils/auth'
import { getStudentExamList } from '../../api/exam'
import { getRecordListByUserId } from '../../api/record'
import { getMyGroups } from '../../api/group'
import ScoreLineChart from '../Charts/ScoreLineChart.vue'
import StudentExamPieChart from '../Charts/StudentExamPieChart.vue'
import { Notebook, CircleCheckFilled, StarFilled, Avatar, School } from '@element-plus/icons-vue'
import { formatDateTime } from '../../utils/format'

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

const formatTime = formatDateTime

const getExamTagType = (exam) => {
  const now = new Date()
  if (now < new Date(exam.startTime)) return 'info'
  if (now > new Date(exam.endTime)) return 'success'
  return 'warning'
}

const getExamStatusLabel = (exam) => {
  const now = new Date()
  if (now < new Date(exam.startTime)) return '未开始'
  if (now > new Date(exam.endTime)) return '已结束'
  return '进行中'
}

const scoreLevel = (score) => {
  if (score >= 90) return 'excellent'
  if (score >= 60) return 'pass'
  return 'fail'
}

const availableExamCount = ref(0)
const completedExamCount = ref(0)
const notTakenCount = ref(0)
const avgScore = ref(0)
const joinedGroupCount = ref(0)
const myRecentExams = ref([])
const myRecentScores = ref([])

const recentExamOverview = computed(() => {
  const scoreMap = {}
  myRecentScores.value.forEach(s => { scoreMap[s.title] = { score: s.score, totalScore: s.totalScore } })
  return myRecentExams.value.map(e => ({
    ...e,
    score: scoreMap[e.title]?.score ?? null,
    totalScore: scoreMap[e.title]?.totalScore ?? 100
  }))
})

const loadData = async () => {
  try {
    const userId = Number(user.id)
    const [examRes, recordRes, groupRes] = await Promise.all([
      getStudentExamList(userId),
      getRecordListByUserId(userId),
      getMyGroups(userId)
    ])
    let totalExamCount = 0
    if (examRes.code === 200) {
      const exams = examRes.data.records || examRes.data || []
      totalExamCount = exams.length
      availableExamCount.value = totalExamCount
      myRecentExams.value = exams.slice(0, 5)
    }
    if (recordRes.code === 200) {
      const records = recordRes.data.records || recordRes.data || []
      // 跟考试列表保持一致：status === 2 或 score != null 算已提交
      const submitted = records.filter(r => r.status === 2 || r.score != null)
      const graded = records.filter(r => r.score != null)
      completedExamCount.value = submitted.length
      notTakenCount.value = Math.max(0, totalExamCount - submitted.length)
      if (graded.length > 0) {
        avgScore.value = Math.round(graded.reduce((s, r) => s + (r.score || 0), 0) / graded.length)
        myRecentScores.value = graded.slice(0, 5).map(r => ({
          title: r.examTitle || '考试',
          score: r.score,
          totalScore: r.totalScore || 100
        }))
      }
    }
    if (groupRes.code === 200) {
      joinedGroupCount.value = groupRes.data ? groupRes.data.length : 0
    }
  } catch (error) {
    console.error('加载数据失败', error)
    ElMessage.error('加载数据失败')
  }
}

onMounted(() => { loadData() })
</script>
