<template>
  <div class="exam-manage">
    <div class="search-bar">
      <div class="search-left">
        <el-input v-model="searchText" placeholder="搜索考试名称">
          <template #append>
            <el-button @click="search">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="search-right">
        <el-button type="primary" @click="goToAdd" v-if="!isSuperAdmin()">
          <el-icon><Plus /></el-icon>
          添加考试
        </el-button>
      </div>
    </div>

    <el-card class="table-card">
      <div v-if="examList.length === 0" class="no-data-container">
        <div class="no-data-icon">
          <el-icon :size="60" color="#909399"><Search /></el-icon>
        </div>
        <div class="no-data-text">未搜索到相关考试</div>
        <div class="no-data-hint">请尝试更换关键词搜索</div>
      </div>
      <el-table v-else :data="examList" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="考试名称" width="200" />
        <el-table-column prop="description" label="描述" width="200" />
        <el-table-column label="考试时间" width="220">
          <template #default="scope">
            {{ formatDateTime(scope.row.startTime) }} ~ {{ formatDateTime(scope.row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="150" />
        <el-table-column prop="totalScore" label="总分" width="100" />
        <el-table-column label="操作" width="240">
          <template #default="scope">
            <el-button v-if="!isSuperAdmin()" size="default" type="primary" :icon="Edit" @click="goToEdit(scope.row)">
              编辑
            </el-button>
            <el-button size="default" type="danger" :icon="Delete" @click="handleDeleteExam(scope.row)">
              删除
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
          @size-change="loadExams"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { formatDateTime } from '../../utils/format'
import { getExamList, deleteExam } from '../../api/exam'
import { getUser, isSuperAdmin } from '../../utils/auth'

const router = useRouter()

const searchText = ref('')
const examList = ref([])

const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const search = () => {
  currentPage.value = 1
  loadExams()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadExams()
}

const loadExams = async () => {
  try {
    const currentUser = getUser()
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (!isSuperAdmin()) {
      params.creatorId = currentUser?.id
    }
    if (searchText.value) {
      params.keyword = searchText.value
    }
    const res = await getExamList(params)
    if (res.code === 200) {
      examList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载考试列表失败')
    examList.value = []
  }
}

const goToEdit = (row) => {
  router.push('/teacher/exam/edit/' + row.id)
}

const handleDeleteExam = (row) => {
  ElMessageBox.confirm(
    '此操作将永久删除该考试, 是否继续?',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteExam(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadExams()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const goToAdd = () => {
  router.push('/teacher/exam/edit')
}

onMounted(() => {
  loadExams()
})
</script>

<style scoped>
.exam-manage {
  width: 100%;
  padding: 24px;
  background: var(--color-bg, #f0f2f5);
  min-height: 100%;
  box-sizing: border-box;
}

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

/* ========== Search Bar ========== */
.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: var(--color-white);
  padding: 16px 24px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  transition: var(--transition);
}

.search-bar:hover {
  box-shadow: var(--shadow-lg);
}

.search-left {
  flex: 1;
  max-width: 420px;
}

.search-left :deep(.el-input) {
  --el-input-border-radius: var(--radius-lg);
  --el-input-height: 36px;
}

.search-left :deep(.el-input__wrapper) {
  border-radius: var(--radius-base) 0 0 var(--radius-base) !important;
  border-right: none !important;
  box-shadow: 0 0 0 1px var(--color-border) inset;
  transition: var(--transition);
}

.search-left :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.search-left :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
  z-index: 1;
}

.search-left :deep(.el-input-group__append) {
  background: var(--color-primary);
  border: none;
  border-radius: 0 var(--radius-base) var(--radius-base) 0;
}

.search-left :deep(.el-input-group__append .el-button) {
  background: transparent;
  border: none;
  color: var(--color-white);
  padding: 0 16px;
  height: 100%;
  margin: 0;
  border-radius: 0 var(--radius-base) var(--radius-base) 0;
  transition: var(--transition);
}

.search-left :deep(.el-input-group__append .el-button:hover) {
  background: rgba(255, 255, 255, 0.15);
}

.search-left :deep(.el-input-group__append .el-button .el-icon) {
  font-size: 18px;
}

/* ========== Search Right ========== */
.search-right {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.search-right .el-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: var(--radius-sm);
  font-weight: 500;
  transition: var(--transition);
}

.search-right .el-button--primary {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.30);
}

.search-right .el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.40);
}

.search-right .el-button--primary:active {
  transform: translateY(0);
}

.search-right .el-button .el-icon {
  font-size: 16px;
}

/* ========== Table Card ========== */
.table-card {
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  border: none;
  overflow: hidden;
  transition: var(--transition);
}

.table-card:hover {
  box-shadow: var(--shadow-lg);
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.table-card :deep(.el-table) {
  border: none;
  font-size: 14px;
}

.table-card :deep(.el-table th.el-table__cell) {
  background: linear-gradient(135deg, var(--color-primary-lightest), var(--color-primary-lighter));
  color: var(--color-primary-dark);
  font-weight: 600;
  font-size: 13px;
  letter-spacing: 0.3px;
  padding: 14px 16px;
  border-bottom: none;
}

.table-card :deep(.el-table .el-table__cell) {
  padding: 12px 16px;
}

.table-card :deep(.el-table__body tr.el-table__row) {
  transition: var(--transition);
}

.table-card :deep(.el-table__body tr.el-table__row--striped td) {
  background: var(--color-primary-lightest);
}

.table-card :deep(.el-table__body tr:hover > td) {
  background: var(--color-primary-lighter) !important;
}

.table-card :deep(.el-table__body td) {
  border-bottom: 1px solid #f0f0f0;
}

.table-card :deep(.el-table--border .el-table__inner-wrapper) {
  border: none;
}

.table-card :deep(.el-table--border::after) {
  display: none;
}

/* Table action buttons */
.table-card :deep(.el-table .el-button) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border-radius: var(--radius-sm);
  font-weight: 500;
  padding: 7px 14px;
  transition: var(--transition);
}

.table-card :deep(.el-table .el-button--small) {
  padding: 5px 12px;
  font-size: 12px;
}

.table-card :deep(.el-table .el-button .el-icon) {
  font-size: 16px;
}

.table-card :deep(.el-table .el-button--primary) {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-white);
}

.table-card :deep(.el-table .el-button--primary:hover) {
  background: var(--color-primary-light);
  border-color: var(--color-primary-light);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.35);
}

.table-card :deep(.el-table .el-button--danger) {
  background: var(--color-white);
  border-color: var(--color-danger);
  color: var(--color-danger);
}

.table-card :deep(.el-table .el-button--danger:hover) {
  background: var(--color-danger);
  border-color: var(--color-danger);
  color: var(--color-white);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.35);
}

/* ========== Pagination ========== */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 16px 24px;
  background: var(--color-white);
  border-top: 1px solid #f0f0f0;
}

.pagination-container :deep(.el-pagination) {
  font-weight: 400;
}

.pagination-container :deep(.el-pagination .el-pagination__total) {
  color: var(--color-text-secondary);
}

.pagination-container :deep(.el-pagination .el-pager li) {
  border-radius: var(--radius-sm);
  margin: 0 2px;
  font-weight: 500;
  transition: var(--transition);
}

.pagination-container :deep(.el-pagination .el-pager li:not(.is-active):hover) {
  color: var(--color-primary);
}

.pagination-container :deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: var(--color-white);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.30);
}

.pagination-container :deep(.el-pagination button:hover) {
  color: var(--color-primary);
}
</style>
