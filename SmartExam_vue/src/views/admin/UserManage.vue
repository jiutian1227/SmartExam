<template>
  <div class="user-manage">
    <div class="search-bar">
      <div class="search-left">
        <el-input v-model="searchText" placeholder="搜索用户名">
          <template #append>
            <el-button @click="search">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="search-right">
        <el-button type="primary" @click="showAddModal = true">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
      </div>
    </div>
    
    <el-card class="table-card">
      <div v-if="userList.length === 0" class="no-data-container">
        <div class="no-data-icon">
          <el-icon :size="60" color="#909399"><Search /></el-icon>
        </div>
        <div class="no-data-text">未搜索到相关用户</div>
        <div class="no-data-hint">请尝试更换关键词搜索</div>
      </div>
      <el-table v-else :data="userList" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="180"/>
        <el-table-column prop="realName" label="真实姓名" width="180" />
        <el-table-column prop="role" label="角色" width="150">
          <template #default="scope">
            <el-tag :type="scope.row.role === 2 ? 'warning' : scope.row.role === 0 ? 'danger' : 'success'">
              {{ scope.row.role === 2 ? '系统管理员' : scope.row.role === 0 ? '教师' : '学生' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="default" type="primary" :icon="Edit" @click="editUser(scope.row)">
              编辑
            </el-button>
            <el-button size="default" type="danger" :icon="Delete" @click="handleDeleteUser(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="form.id ? '编辑用户' : '添加用户'" v-model="showAddModal" @close="resetForm">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="form.password" :placeholder="form.id ? '留空则不修改密码' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role">
            <el-option :value="0" label="教师" />
            <el-option :value="1" label="学生" />
            <el-option :value="2" label="系统管理员" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getUserList, createUser, updateUser, deleteUser } from '../../api/user'
import { formatDateTime } from '../../utils/format'

const searchText = ref('')
const showAddModal = ref(false)
const userList = ref([])
const form = ref({
  id: null,
  username: '',
  password: '',
  realName: '',
  role: 1
})

const search = async () => {
  try {
    // 只有当搜索词不为空时才传递keyword参数
    const params = searchText.value.trim() ? { keyword: searchText.value.trim() } : {}
    const res = await getUserList(params)
    userList.value = res.code === 200 ? res.data : []
  } catch (error) {
    ElMessage.error('搜索失败')
    loadUsers()
  }
}

const loadUsers = async () => {
  try {
    const res = await getUserList()
    userList.value = res.code === 200 ? res.data : []
  } catch (error) {
    ElMessage.error('加载用户列表失败')
    userList.value = []
  }
}

const resetForm = () => {
  form.value = {
    id: null,
    username: '',
    password: '',
    realName: '',
    role: 1
  }
}

const editUser = (row) => {
  form.value = { ...row }
  showAddModal.value = true
}

const handleDeleteUser = (row) => {
  ElMessageBox.confirm(
    '此操作将永久删除该用户, 是否继续?',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteUser(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadUsers()
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

const submitForm = async () => {
  if (!form.value.username) {
    ElMessage.error('请填写用户名')
    return
  }
  if (!form.value.id && !form.value.password) {
    ElMessage.error('请填写密码')
    return
  }
  
  try {
    let res
    if (form.value.id) {
      res = await updateUser(form.value)
    } else {
      res = await createUser(form.value)
    }
    
    if (res.code === 200) {
      ElMessage.success(form.value.id ? '更新成功' : '添加成功')
      showAddModal.value = false
      resetForm()
      loadUsers()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-manage {
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

/* Table role tags */
.table-card :deep(.el-tag) {
  border-radius: 4px;
  font-weight: 500;
  padding: 0 10px;
  height: 26px;
  line-height: 26px;
  font-size: 12px;
}

.table-card :deep(.el-tag--success) {
  background: #e1f3d8;
  border-color: #b3e19d;
  color: #67c23a;
}

.table-card :deep(.el-tag--warning) {
  background: #faecd8;
  border-color: #f3d19e;
  color: #e6a23c;
}

.table-card :deep(.el-tag--danger) {
  background: #fde2e2;
  border-color: #fbc4c4;
  color: #f56c6c;
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

/* Table edit icon button */
.table-card :deep(.el-table .el-button:not(.el-button--primary):not(.el-button--danger)) {
  background: var(--color-primary-lightest);
  border-color: var(--color-primary-lighter);
  color: var(--color-primary);
}

.table-card :deep(.el-table .el-button:not(.el-button--primary):not(.el-button--danger):hover) {
  background: var(--color-primary-lighter);
  border-color: var(--color-primary-light);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.25);
}

/* ========== Dialog ========== */
.el-dialog {
  border-radius: var(--radius-md) !important;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12) !important;
}

.el-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, var(--color-primary-lightest), var(--color-primary-lighter));
  margin: 0;
  padding: 18px 24px;
  border-bottom: 1px solid #e8edf5;
  border-radius: var(--radius-md) var(--radius-md) 0 0;
}

.el-dialog :deep(.el-dialog__title) {
  color: var(--color-primary-dark);
  font-weight: 600;
  font-size: 16px;
}

.el-dialog :deep(.el-dialog__headerbtn) {
  top: 18px;
  right: 20px;
  font-size: 18px;
}

.el-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--color-primary);
}

.el-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.el-dialog :deep(.el-form-item) {
  margin-bottom: 20px;
}

.el-dialog :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--color-text-regular);
}

.el-dialog :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
  box-shadow: 0 0 0 1px var(--color-border) inset;
  transition: var(--transition);
}

.el-dialog :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.el-dialog :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.el-dialog :deep(.el-select) {
  width: 100%;
}

.el-dialog :deep(.el-select .el-input__wrapper) {
  border-radius: var(--radius-sm);
}

.el-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}

.el-dialog :deep(.el-dialog__footer .el-button) {
  border-radius: var(--radius-sm);
  font-weight: 500;
  padding: 8px 20px;
  transition: var(--transition);
}

.el-dialog :deep(.el-dialog__footer .el-button--primary) {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  border: none;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.30);
}

.el-dialog :deep(.el-dialog__footer .el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.40);
}

.el-dialog :deep(.el-dialog__footer .el-button--default:hover) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
</style>
