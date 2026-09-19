<template>
  <div class="grading-list">
    <!-- <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>判卷管理</h2>
    </div> -->

    <el-card class="stats-card">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ total }}</div>
            <div class="stat-label">考试总数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ totalSubmitted }}</div>
            <div class="stat-label">总提交数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ totalGraded }}</div>
            <div class="stat-label">已批阅</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-value">{{ totalUngraded }}</div>
            <div class="stat-label">待批阅</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <div v-if="examList.length === 0" class="no-data-container">
        <div class="no-data-icon">
          <el-icon :size="60" color="#909399"><Files /></el-icon>
        </div>
        <div class="no-data-text">暂无待批阅的考试</div>
        <div class="no-data-hint">当前没有需要批阅的考试记录</div>
      </div>
      <el-table v-else :data="examList" border>
        <el-table-column prop="title" label="考试名称" />
        <el-table-column prop="duration" label="时长(分钟)" width="150" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column label="提交情况" width="180">
          <template #default="scope">
            <span class="submit-info">
              {{ scope.row.submittedCount }} / {{ scope.row.totalGroupMembers || (scope.row.submittedCount + scope.row.ungradedCount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="批阅情况" width="150">
          <template #default="scope">
            <el-tag type="success" v-if="scope.row.ungradedCount === 0 && scope.row.submittedCount > 0">
              全部完成
            </el-tag>
            <el-tag type="warning" v-else-if="scope.row.ungradedCount > 0">
              待批阅 {{ scope.row.ungradedCount }}
            </el-tag>
            <el-tag type="info" v-else>
              暂无提交
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" size="default" :icon="View" @click="goToDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="examList.length > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadGradingStats"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View, Files } from '@element-plus/icons-vue'
import { getGradingStats } from '../../api/record'
import { getUser, isSuperAdmin } from '../../utils/auth'

const router = useRouter()
const examList = ref([])
const allExams = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const totalSubmitted = computed(() => {
  return allExams.value.reduce((sum, exam) => sum + exam.submittedCount, 0)
})

const totalGraded = computed(() => {
  return allExams.value.reduce((sum, exam) => sum + exam.gradedCount, 0)
})

const totalUngraded = computed(() => {
  return allExams.value.reduce((sum, exam) => sum + exam.ungradedCount, 0)
})

const goBack = () => {
  router.push('/home')
}

const loadGradingStats = async () => {
  try {
    const currentUser = getUser()
    const res = await getGradingStats({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      creatorId: isSuperAdmin() ? null : currentUser?.id
    })
    if (res.code === 200) {
      examList.value = res.data.records || []
      total.value = res.data.total || 0
      const allRes = await getGradingStats({ pageNum: 1, pageSize: 10000, creatorId: isSuperAdmin() ? null : currentUser?.id })
      if (allRes.code === 200) {
        allExams.value = allRes.data.records || []
      }
    }
  } catch (error) {
    ElMessage.error('加载判卷统计失败')
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadGradingStats()
}

const goToDetail = (exam) => {
  router.push(`/teacher/grading/${exam.id}`)
}

onMounted(() => {
  loadGradingStats()
})
</script>

<style scoped>
.grading-list {
  width: 100%;
  padding: var(--space-2) 0;
  overflow-x: hidden;
  box-sizing: border-box;
}

/* ── Stats Card ───────────────────────────────────────── */
.stats-card {
  margin-bottom: var(--space-6);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
}
.stats-card:hover {
  box-shadow: var(--shadow-md);
}

.stats-card :deep(.el-card__body) {
  padding: var(--space-4);
}

.stat-item {
  text-align: center;
  padding: var(--space-6) var(--space-4);
  position: relative;
}
.stat-item::after {
  content: '';
  position: absolute;
  right: 0;
  top: 25%;
  height: 50%;
  width: 1px;
  background: linear-gradient(180deg, transparent, var(--color-neutral-200), transparent);
}
.el-col:last-child .stat-item::after {
  display: none;
}

.stat-value {
  font-size: 38px;
  font-weight: var(--font-weight-bold);
  background: var(--color-primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: var(--space-2);
  line-height: 1.2;
  letter-spacing: var(--letter-spacing-tight);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  font-weight: var(--font-weight-medium);
  text-transform: uppercase;
  letter-spacing: var(--letter-spacing-wide);
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

/* ── Submit Info ──────────────────────────────────────── */
.submit-info {
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-500);
  font-size: var(--font-size-sm);
}

/* ── Table scroll ─────────────────────────────────────── */
.table-card :deep(.el-table__body-wrapper) {
  overflow-x: auto;
}
.table-card :deep(.el-table) {
  width: 100%;
}

/* ── No Data Container ───────────────────────────────── */
.no-data-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.no-data-icon {
  margin-bottom: 16px;
  opacity: 0.6;
}

.no-data-text {
  font-size: var(--font-size-lg);
  color: var(--color-text-secondary);
  margin-bottom: 8px;
  font-weight: var(--font-weight-medium);
}

.no-data-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
}
</style>
