<template>
  <div class="exam-list" v-loading="loading" element-loading-text="加载考试列表...">
    <el-empty v-if="!loading && examList.length === 0" description="暂无可用考试" />
    <template v-else>
      <el-row :gutter="24">
        <el-col style="margin-bottom: 20px;" :span="8" v-for="exam in examList" :key="exam.id">
          <el-card class="exam-card" hover>
            <div class="exam-header">
              <h3 class="exam-title">{{ exam.title }}</h3>
              <el-tag :type="getExamTagType(exam)">
                {{ getExamStatusLabel(exam) }}
              </el-tag>
            </div>
            <p class="exam-desc">{{ exam.description }}</p>
            <div class="exam-info">
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatDateTime(exam.startTime) }} ~ {{ formatDateTime(exam.endTime) }}</span>
              </div>
              <div class="info-item">
                <el-icon><Calendar /></el-icon>
                <span>时长: {{ exam.duration }}分钟</span>
              </div>
              <div class="info-item">
                <el-icon><Star /></el-icon>
                <span>总分: {{ exam.totalScore }}分</span>
              </div>
            </div>
            <div class="exam-actions">
              <el-button
                type="primary"
                :disabled="isExamSubmitted(exam.id) || isExamEnded(exam) || isExamNotStarted(exam)"
                @click="goToExam(exam.id)"
              >
                {{ getButtonText(exam) }}
                <el-icon style="margin-left: 4px;"><ArrowRight /></el-icon>
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[6, 12, 24]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadExams"
          @current-change="handlePageChange"
        />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getStudentExamList } from '../../api/exam'
import { getMyRecords } from '../../api/record'
import { getUser } from '../../utils/auth'
import { formatDateTime } from '../../utils/format'
import { Clock, Calendar, Star, ArrowRight, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const examList = ref([])
const examRecordMap = ref({})
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(0)
const loading = ref(false)

const isExamSubmitted = (examId) => {
  const record = examRecordMap.value[examId]
  return record && (record.status === 2 || record.score != null)
}

const isExamInProgress = (examId) => {
  const record = examRecordMap.value[examId]
  return record && record.status === 1
}

const getExamStatusLabel = (exam) => {
  if (isExamSubmitted(exam.id)) return '已提交'
  if (isExamInProgress(exam.id)) return '继续考试'

  const now = new Date().getTime()
  const start = new Date(exam.startTime).getTime()
  const end = new Date(exam.endTime).getTime()

  if (now < start) return '未开始'
  if (now >= start && now <= end) return '进行中'
  return '未参与'
}

const getExamTagType = (exam) => {
  if (isExamSubmitted(exam.id)) return 'info'
  if (isExamInProgress(exam.id)) return 'warning'

  const now = new Date().getTime()
  const start = new Date(exam.startTime).getTime()
  const end = new Date(exam.endTime).getTime()

  if (now < start) return 'warning'
  if (now >= start && now <= end) return 'success'
  return 'danger'
}

const isExamEnded = (exam) => {
  if (!exam.endTime) return false
  const now = new Date()
  const endTime = new Date(exam.endTime)
  return now > endTime
}

const isExamNotStarted = (exam) => {
  if (!exam.startTime) return false
  const now = new Date()
  const startTime = new Date(exam.startTime)
  return now < startTime
}

const getButtonText = (exam) => {
  if (isExamSubmitted(exam.id)) return '已提交'
  if (isExamEnded(exam)) return '未参与'
  if (isExamNotStarted(exam)) return '未开始'
  if (isExamInProgress(exam.id)) return '继续考试'
  return '进入考试'
}

const goToExam = (examId) => {
  const exam = examList.value.find(e => e.id === examId)
  if (exam && isExamNotStarted(exam)) {
    ElMessage.warning('考试尚未开始，请在开始时间后进入')
    return
  }

  if (exam && isExamEnded(exam)) {
    ElMessage.warning('该考试已结束，不能再进入')
    return
  }

  if (isExamSubmitted(examId)) {
    ElMessage.warning('您已经提交过该考试')
    return
  }
  router.push(`/student/exam-detail/${examId}`)
}

const loadExams = async () => {
  loading.value = true
  try {
    const user = getUser()
    const userId = user?.id || 1
    const res = await getStudentExamList(userId, {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      examList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      examList.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('加载考试列表失败')
    examList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const loadExamStatuses = async () => {
  try {
    const user = getUser()
    const userId = user?.id || 1
    const res = await getMyRecords(userId)
    if (res.code === 200) {
      const records = res.data.records || res.data || []
      if (Array.isArray(records)) {
        const map = {}
        records.forEach(record => {
          if (record.examId) {
            map[record.examId] = record
          }
        })
        examRecordMap.value = map
      }
    }
  } catch (error) {
    console.error('加载考试状态失败', error)
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadExams().then(() => {
    loadExamStatuses()
  })
}

onMounted(() => {
  loadExams().then(() => {
    loadExamStatuses()
  })
})
</script>

<style scoped>
.exam-list {
  width: 100%;
}

.el-row {
  margin-bottom: 0 !important;
}

.exam-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--color-neutral-200);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  background: #fff;
}
.exam-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(59, 130, 246, 0.1), 0 4px 12px rgba(0, 0, 0, 0.08) !important;
  border-color: var(--color-primary-200);
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-3);
  gap: var(--space-3);
}

.exam-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  margin: 0;
  line-height: 1.3;
  transition: color var(--transition-base);
}
.exam-card:hover .exam-title {
  color: var(--color-primary-600);
}

.exam-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  margin-bottom: var(--space-4);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.5;
}

.exam-info { margin-bottom: var(--space-4); }

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-2);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.info-item .el-icon {
  font-size: 16px;
  margin-right: var(--space-2);
  color: var(--color-primary-500);
  flex-shrink: 0;
  transition: color var(--transition-base);
}
.exam-card:hover .info-item .el-icon {
  color: var(--color-primary-400);
}

.exam-actions {
  margin-top: auto;
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-neutral-200);
}

.exam-actions .el-button {
  width: 100%;
  border-radius: var(--radius-base);
  font-weight: var(--font-weight-medium);
  transition: all 0.25s ease;
}

.pagination-container {
  margin-top: var(--space-8);
  display: flex;
  justify-content: center;
}

.el-col {
  margin-bottom: var(--space-5) !important;
}
</style>
