<template>
  <div class="my-score">
    <el-card class="score-card">
      <div class="score-header">
        <h3>我的成绩</h3>
      </div>
      <el-table :data="scoreList" border v-loading="loading">
        <el-table-column label="考试名称">
          <template #default="scope">
            {{ scope.row.examTitle || '未知考试' }}
          </template>
        </el-table-column>
        <el-table-column label="考试时间" width="220">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }} ~ {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="得分">
          <template #default="scope">
            <el-tag v-if="scope.row.gradingStatus !== '已批阅'" type="info" size="large">
              -
            </el-tag>
            <el-tag v-else :type="getScoreTagType(scope.row.score)" size="large">
              {{ scope.row.score ?? 0 }} / {{ scope.row.totalScore ?? 100 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column label="批阅状态">
          <template #default="scope">
            <span :style="{ color: scope.row.gradingStatus === '已批阅' ? '#67C23A' : '#E6A23C' }">
              {{ scope.row.gradingStatus || '待批阅' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="default" type="primary" :icon="View" @click="goToDetail(scope.row.id)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadScores"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRecordListByUserId } from '../../api/record'
import { getUser } from '../../utils/auth'
import { formatDateTime } from '../../utils/format'
import { View, StarFilled } from '@element-plus/icons-vue'

const router = useRouter()
const scoreList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getScoreTagType = (score) => {
  if (score >= 90) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
}

const goToDetail = (id) => {
  router.push(`/student/my-score/${id}`)
}

const loadScores = async () => {
  loading.value = true
  try {
    const user = getUser()
    const userId = user?.id
    if (!userId) {
      ElMessage.error('用户未登录')
      scoreList.value = []
      total.value = 0
      return
    }
    const res = await getRecordListByUserId(userId, {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    if (res.code === 200) {
      scoreList.value = res.data.records || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '加载失败')
      scoreList.value = []
      total.value = 0
    }
  } catch (error) {
    ElMessage.error('加载成绩列表失败')
    scoreList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadScores()
}

onMounted(() => {
  loadScores()
})
</script>

<style scoped>
.my-score {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.score-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-neutral-200);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.score-card :deep(.el-card__body) {
  padding: 0;
}

.score-header {
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--color-neutral-200);
  background: linear-gradient(135deg, var(--color-primary-50) 0%, #fff 100%);
}
.score-header h3 {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-700);
  position: relative;
  padding-left: var(--space-4);
}
.score-header h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--color-primary-500);
  border-radius: 2px;
}

.score-card :deep(.el-table) {
  border: none;
}
.score-card :deep(.el-table th.el-table__cell) {
  background: var(--color-neutral-50);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
}
.score-card :deep(.el-table td.el-table__cell) {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}
.score-card :deep(.el-table--border td.el-table__cell),
.score-card :deep(.el-table--border th.el-table__cell) {
  border-right: 1px solid var(--color-neutral-100);
}
.score-card :deep(.el-table__body tr:hover td) {
  background: var(--color-primary-50);
  cursor: pointer;
}

.score-card :deep(.el-tag) {
  border-radius: var(--radius-base);
  padding: 0 var(--space-3);
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
  border: none;
}
.score-card :deep(.el-tag--success) {
  background: var(--color-secondary-50);
  color: var(--color-success);
}
.score-card :deep(.el-tag--warning) {
  background: #fff7e6;
  color: var(--color-warning);
}
.score-card :deep(.el-tag--danger) {
  background: var(--color-accent-50);
  color: var(--color-danger);
}

.score-card :deep(.el-button--primary) {
  border-radius: var(--radius-base);
  font-weight: var(--font-weight-medium);
  transition: all 0.25s ease;
}
.score-card :deep(.el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.pagination-container {
  padding: var(--space-5) var(--space-6);
  display: flex;
  justify-content: center;
  border-top: 1px solid var(--color-neutral-200);
  background: var(--color-neutral-50);
}
</style>
