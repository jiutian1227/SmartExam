<template>
  <div class="grading-detail">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-header-left">
        <el-button @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <div class="header-info">
          <h2>{{ examInfo.title || '考试' }}</h2>
          <div class="header-meta">
            <el-tag size="small" type="info">{{ examInfo.totalScore || 0 }}分</el-tag>
            <el-tag size="small" type="primary">{{ examInfo.duration || 0 }}分钟</el-tag>
            <span class="meta-time">{{ formatDateTime(examInfo.startTime) }} ~ {{ formatDateTime(examInfo.endTime) }}</span>
          </div>
        </div>
      </div>
      <div class="page-header-right">
        <el-progress
          type="circle"
          :percentage="gradingProgress"
          :status="gradingProgress === 100 ? 'success' : ''"
          :width="64"
          :stroke-width="8"
        />
        <div class="progress-label">
          <span class="progress-num">{{ examInfo.gradedCount || 0 }}/{{ totalRecords }}</span>
          <span class="progress-text">已批阅</span>
        </div>
      </div>
    </div>

    <!-- 统计卡片行 -->
    <div class="stats-row">
      <div class="stat-card stat-total">
        <div class="stat-icon"><el-icon :size="22"><UserFilled /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ totalMembers }}</div>
          <div class="stat-label">应提交人数</div>
        </div>
      </div>
      <div class="stat-card stat-submitted">
        <div class="stat-icon"><el-icon :size="22"><Upload /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ examInfo.submittedCount || 0 }}</div>
          <div class="stat-label">已提交</div>
        </div>
      </div>
      <div class="stat-card stat-graded">
        <div class="stat-icon"><el-icon :size="22"><CircleCheck /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ examInfo.gradedCount || 0 }}</div>
          <div class="stat-label">已批阅</div>
        </div>
      </div>
      <div class="stat-card stat-pending">
        <div class="stat-icon"><el-icon :size="22"><Clock /></el-icon></div>
        <div class="stat-body">
          <div class="stat-value">{{ examInfo.ungradedCount || 0 }}</div>
          <div class="stat-label">待批阅</div>
        </div>
      </div>
    </div>

    <!-- 考试信息栏 -->
    <div class="exam-info-bar">
      <div class="info-item">
        <span class="info-label">考试描述</span>
        <span class="info-value">{{ examInfo.description || '暂无描述' }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">创建时间</span>
        <span class="info-value">{{ formatDateTime(examInfo.createTime) }}</span>
      </div>
    </div>

    <!-- 学生试卷列表 -->
    <el-card class="table-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span><el-icon style="margin-right:6px"><Document /></el-icon>学生试卷（{{ total }}份）</span>
          <div class="card-header-right">
            <span class="header-stat">已批阅 <strong>{{ examInfo.gradedCount || 0 }}</strong></span>
            <span class="header-divider">|</span>
            <span class="header-stat pending">待批阅 <strong>{{ examInfo.ungradedCount || 0 }}</strong></span>
          </div>
        </div>
      </template>
      <el-table :data="recordList" stripe>
        <el-table-column type="index" label="id" width="80" />
        <el-table-column prop="username" label="用户名" width="200" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column label="提交时间" width="180">
          <template #default="scope">
            <span class="cell-time">{{ formatDateTime(scope.row.submitTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="批阅状态" width="150" align="center">
          <template #default="scope">
            <el-tag
              :type="(scope.row.gradingStatus || '待批阅') === '已批阅' ? 'success' : 'warning'"
              size="small"
              effect="dark"
              round
            >
              {{ scope.row.gradingStatus || '待批阅' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="得分" width="120" align="center">
          <template #default="scope">
            <span v-if="scope.row.score !== null" :class="['score-cell', scoreLevel(scope.row.score, examInfo.totalScore)]">
              {{ scope.row.score }}<span class="score-total">/{{ examInfo.totalScore || 0 }}</span>
            </span>
            <span v-else class="score-cell pending">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
  :type="scope.row.gradingStatus === '已批阅' ? 'primary' : 'warning'"
  size="default"
  :icon="getGradeIcon(scope.row)"
  @click="goToGrade(scope.row)"
>
  {{ scope.row.gradingStatus === '已批阅' ? '查看' : '批阅' }}
</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="recordList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无学生提交试卷" />
      </div>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadRecords"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, EditPen, View, UserFilled, Upload, CircleCheck, Clock, Document } from '@element-plus/icons-vue'
import { formatDateTime } from '../../utils/format'
import { getExamStats, getExamRecords } from '../../api/record'
import { getExamById } from '../../api/exam'

const router = useRouter()
const route = useRoute()

const examId = ref(parseInt(route.params.id))
const examInfo = ref({})
const recordList = ref([])
const loading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 应提交人数
const totalMembers = computed(() => {
  return examInfo.value.totalGroupMembers || (examInfo.value.submittedCount || 0) + (examInfo.value.ungradedCount || 0)
})

// 总记录数（已提交+待批阅）
const totalRecords = computed(() => {
  return (examInfo.value.submittedCount || 0) + (examInfo.value.ungradedCount || 0)
})

const goBack = () => {
  router.push('/teacher/grading')
}

const gradingProgress = computed(() => {
  const total = totalRecords.value
  if (total === 0) return 0
  const graded = examInfo.value.gradedCount || 0
  return Math.round((graded / total) * 100)
})

const scoreLevel = (score, totalScore) => {
  if (score == null) return ''
  const pct = score / (totalScore || 100)
  if (pct >= 0.9) return 'excellent'
  if (pct >= 0.6) return 'pass'
  return 'fail'
}

const loadExamInfo = async () => {
  try {
    // 先获取考试详细信息
    const examRes = await getExamById(examId.value)
    // 再获取批阅统计
    const statsRes = await getExamStats(examId.value)

    if (examRes.code === 200 && statsRes.code === 200) {
      examInfo.value = {
        ...examRes.data,
        ...statsRes.data
      }
    } else if (examRes.code === 200) {
      examInfo.value = examRes.data || {}
    } else if (statsRes.code === 200) {
      examInfo.value = statsRes.data || {}
    }
  } catch (error) {
    console.error('加载考试信息失败', error)
  }
}

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getExamRecords(examId.value, {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      recordList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载提交记录失败', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadRecords()
}

const goToGrade = (record) => {
  router.push(`/teacher/grading/${examId.value}/${record.id}`)
}

const getGradeIcon = (record) => {
  return record.gradingStatus === '已批阅' ? View : EditPen
}

onMounted(() => {
  loadExamInfo()
  loadRecords()
})
</script>

<style scoped>
.grading-detail {
  width: 100%;
  padding: var(--space-2) 0;
  overflow-x: hidden;
  box-sizing: border-box;
}

/* ── Page Header ──────────────────────────────────────── */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  padding: var(--space-5) var(--space-6);
  background: var(--color-bg-elevated);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-neutral-200);
  box-shadow: var(--shadow-sm);
  flex-wrap: wrap;
  gap: var(--space-4);
}
.page-header-left {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  min-width: 0;
  flex: 1;
}
.back-btn {
  width: 70px;
  flex-shrink: 0;
}
.header-info {
  min-width: 0;
}
.header-info h2 {
  margin: 0 0 var(--space-1);
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.header-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}
.meta-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}
.page-header-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}
.progress-label {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.progress-num {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary-600);
}
.progress-text {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

/* ── Stats Row ────────────────────────────────────────── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}
.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-5);
  background: var(--color-bg-elevated);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-neutral-200);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.stat-total .stat-icon { background: linear-gradient(135deg, #6366f1, #818cf8); }
.stat-submitted .stat-icon { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.stat-graded .stat-icon { background: linear-gradient(135deg, #22c55e, #4ade80); }
.stat-pending .stat-icon { background: linear-gradient(135deg, #f59e0b, #fbbf24); }

.stat-body { min-width: 0; }
.stat-value {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-primary);
  line-height: 1.2;
}
.stat-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  margin-top: 2px;
  font-weight: var(--font-weight-medium);
}

/* ── Exam Info Bar ────────────────────────────────────── */
.exam-info-bar {
  display: flex;
  gap: var(--space-6);
  padding: var(--space-4) var(--space-5);
  background: var(--color-neutral-50);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-neutral-200);
  margin-bottom: var(--space-5);
  flex-wrap: wrap;
}
.info-item {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  min-width: 0;
}
.info-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  font-weight: var(--font-weight-medium);
  white-space: nowrap;
}
.info-value {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ── Card Header ──────────────────────────────────────── */
.card-header {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.card-header-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-regular);
  color: var(--color-text-tertiary);
}
.header-stat strong {
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}
.header-stat.pending strong {
  color: var(--color-warning);
}
.header-divider {
  color: var(--color-neutral-300);
}

/* ── Table Card ───────────────────────────────────────── */
.table-card {
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--transition-base);
}
.table-card:hover {
  box-shadow: var(--shadow-md);
}

.cell-time {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  font-variant-numeric: tabular-nums;
}

.score-cell {
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
}
.score-cell .score-total {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  font-weight: var(--font-weight-regular);
}
.score-cell.excellent { color: var(--color-success); }
.score-cell.pass { color: var(--color-warning); }
.score-cell.fail { color: var(--color-danger); }
.score-cell.pending { color: var(--color-text-tertiary); }

.empty-state {
  padding: var(--space-10) 0;
}

/* ── Table styling overrides ──────────────────────────── */
:deep(.el-table th.el-table__cell) {
  background: var(--color-neutral-50) !important;
  color: var(--color-text-secondary) !important;
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
}
:deep(.el-table__body tr:hover > td) {
  background-color: var(--color-primary-50) !important;
}

/* ── Progress ─────────────────────────────────────────── */
:deep(.el-progress--circle) {
  margin-right: 0;
}

/* ── Pagination ───────────────────────────────────────── */
.pagination-container {
  margin-top: var(--space-4);
  display: flex;
  justify-content: flex-end;
}

/* ── Responsive ───────────────────────────────────────── */
@media (max-width: 900px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 600px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .page-header-right {
    align-self: flex-end;
  }
  .stats-row {
    grid-template-columns: 1fr;
  }
  .exam-info-bar {
    flex-direction: column;
    gap: var(--space-3);
  }
}
</style>
