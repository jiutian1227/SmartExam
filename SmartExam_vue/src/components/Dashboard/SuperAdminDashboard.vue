<template>
  <div>
    <!-- Welcome banner -->
    <div class="welcome-card welcome-card--super">
      <div class="welcome-bg">
        <div class="welcome-orb orb-1"></div>
        <div class="welcome-orb orb-2"></div>
      </div>
      <div class="welcome-body">
        <div class="welcome-text">
          <h1>{{ greeting }}，{{ user?.realName || user?.username }} 👋</h1>
          <p>欢迎回到智能考试平台管理系统，您拥有全部管理权限</p>
        </div>
        <div class="welcome-decoration">
          <el-icon :size="35" style="height: 100%;justify-content: center;"><Lock /></el-icon>
        </div>
      </div>
    </div>

    <!-- Stats row -->
    <div class="stats-grid">
      <div class="stat-card stat-users" @click="$router.push('/admin/user')">
        <div class="stat-icon"><el-icon :size="24"><UserFilled /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ superUserCount }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      <div class="stat-card stat-exams" @click="$router.push('/admin/exam')">
        <div class="stat-icon"><el-icon :size="24"><Notebook /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ superExamCount }}</div>
          <div class="stat-label">考试总数</div>
        </div>
      </div>
      <div class="stat-card stat-questions" @click="$router.push('/admin/question')">
        <div class="stat-icon"><el-icon :size="24"><QuestionFilled /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ superQuestionCount }}</div>
          <div class="stat-label">试题总数</div>
        </div>
      </div>
      <div class="stat-card stat-groups" @click="$router.push('/admin/group')">
        <div class="stat-icon"><el-icon :size="24"><Avatar /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ superGroupCount }}</div>
          <div class="stat-label">用户组总数</div>
        </div>
      </div>
    </div>

    <!-- Charts row -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="chart-card-header"><h3>用户角色分布</h3></div>
        <div class="chart-card-body">
          <div ref="roleChartRef" class="chart-container"></div>
        </div>
      </div>
      <div class="chart-card">
        <ExamBarChart :data="superExamStats" />
      </div>
    </div>

    <!-- Exam overview table
    <div class="section-card">
      <div class="section-card-header">
        <h3>全部考试概览</h3>
        <el-tag type="primary">{{ superExamCount }} 场</el-tag>
      </div>
      <el-table :data="superRecentExams" stripe>
        <el-table-column prop="title" label="考试名称" min-width="200" />
        <el-table-column prop="creatorName" label="创建者" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getExamTagType(scope.row)" size="small">{{ getExamStatusLabel(scope.row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="scope">{{ formatTime(scope.row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="170">
          <template #default="scope">{{ formatTime(scope.row.endTime) }}</template>
        </el-table-column>
      </el-table>
    </div> -->
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getUser, isSuperAdmin } from '../../utils/auth'
import { getGroupList } from '../../api/group'
import { getExamList } from '../../api/exam'
import { getQuestionList } from '../../api/question'
import { getGradingStats } from '../../api/record'
import { getUserList } from '../../api/user'
import ExamBarChart from '../Charts/ExamBarChart.vue'
import { UserFilled, Notebook, QuestionFilled, Avatar, Lock } from '@element-plus/icons-vue'
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

const superUserCount = ref(0)
const superExamCount = ref(0)
const superQuestionCount = ref(0)
const superGroupCount = ref(0)
const superExamStats = ref([])
const superRecentExams = ref([])
const roleChartRef = ref(null)
let roleChartInstance = null

const superRoleTeacherCount = ref(0)
const superRoleStudentCount = ref(0)
const superRoleSuperAdminCount = ref(0)

const initRoleChart = () => {
  if (!roleChartRef.value) return
  if (roleChartInstance) roleChartInstance.dispose()
  roleChartInstance = echarts.init(roleChartRef.value)

  const raw = [
    { value: superRoleTeacherCount.value, name: '教师' },
    { value: superRoleStudentCount.value, name: '学生' },
    { value: superRoleSuperAdminCount.value, name: '管理员' }
  ].filter(item => item.value > 0)

  roleChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { orient: 'horizontal', bottom: '0%', textStyle: { color: '#606266', fontSize: 13 } },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
      label: { show: true, formatter: '{b}\n{c}人 ({d}%)', fontSize: 13 },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' }, scaleSize: 6 },
      data: raw.length ? raw : [{ value: 1, name: '暂无数据', itemStyle: { color: '#ddd' } }],
      color: ['#3b82f6', '#22c55e', '#f59e0b']
    }]
  })
}

const roleChartResize = () => roleChartInstance?.resize()

const loadData = async () => {
  try {
    const [userRes, examRes, questionRes, groupRes, gradingStatsRes] = await Promise.all([
      getUserList(),
      getExamList({ pageNum: 1, pageSize: 100 }),
      getQuestionList({ pageNum: 1, pageSize: 100 }),
      getGroupList(),
      getGradingStats({ pageNum: 1, pageSize: 100 })
    ])
    if (userRes.code === 200) {
      const users = userRes.data.records || userRes.data || []
      superUserCount.value = users.length
      superRoleTeacherCount.value = users.filter(u => u.role === 0).length
      superRoleStudentCount.value = users.filter(u => u.role === 1).length
      superRoleSuperAdminCount.value = users.filter(u => u.role === 2).length
      nextTick(() => initRoleChart())
    }
    if (examRes.code === 200) {
      const exams = examRes.data.records || examRes.data || []
      superExamCount.value = exams.length
      superRecentExams.value = exams
    }
    if (questionRes.code === 200) {
      superQuestionCount.value = (questionRes.data.records || questionRes.data || []).length
    }
    if (groupRes.code === 200) {
      superGroupCount.value = (groupRes.data.records || groupRes.data || []).length
    }
    if (gradingStatsRes.code === 200) {
      superExamStats.value = gradingStatsRes.data.records || gradingStatsRes.data || []
    }
  } catch (error) { console.error('加载系统数据失败', error) }
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', roleChartResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', roleChartResize)
  roleChartInstance?.dispose()
})
</script>
