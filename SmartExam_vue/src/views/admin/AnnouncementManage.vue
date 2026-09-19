<template>
  <div class="announcement-manage">
    <el-card class="header-card">
      <div class="header-actions">
        <h2>公告管理</h2>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          发布公告
        </el-button>
      </div>
    </el-card>

    <el-card class="table-card" v-loading="loading">
      <el-table :data="announcements" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="100" />
        <el-table-column prop="type" label="类型" width="200">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.type)" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetRoles" label="可见角色" width="180">
          <template #default="{ row }">
            <span>{{ formatRoles(row.targetRoles) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="{ row }">
            <span>{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="default" :icon="Edit" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="default" :icon="Delete" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '发布公告'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择公告类型" style="width: 100%">
            <el-option label="系统通知" value="系统通知" />
            <el-option label="考试通知" value="考试通知" />
            <el-option label="培训通知" value="培训通知" />
            <el-option label="会议通知" value="会议通知" />
            <el-option label="校园通知" value="校园通知" />
          </el-select>
        </el-form-item>
        <el-form-item label="可见角色" prop="targetRoles">
          <el-select
            v-model="form.targetRoles"
            multiple
            placeholder="选择可见角色（不选则所有角色可见）"
            style="width: 100%"
            clearable
          >
            <el-option label="教师" :value="0" />
            <el-option label="学生" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getAllAnnouncements,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement
} from '../../api/announcement'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const announcements = ref([])
const formRef = ref(null)

const form = ref({
  id: null,
  title: '',
  type: '',
  targetRoles: [],
  content: ''
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
}

onMounted(() => {
  loadAnnouncements()
})

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAllAnnouncements()
    if (res.code === 200) {
      announcements.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取公告列表失败')
    }
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  isEdit.value = false
  form.value = {
    id: null,
    title: '',
    type: '',
    targetRoles: [],
    content: ''
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  // 将 targetRoles 从字符串转换为数组
  let roles = []
  if (row.targetRoles) {
    roles = row.targetRoles.split(',').map(r => parseInt(r.trim())).filter(r => !isNaN(r))
  }
  form.value = {
    id: row.id,
    title: row.title,
    type: row.type,
    targetRoles: roles,
    content: row.content
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    // 将数组转换为逗号分隔的字符串
    const data = {
      ...form.value,
      targetRoles: form.value.targetRoles.length > 0 ? form.value.targetRoles.join(',') : ''
    }

    let res
    if (isEdit.value) {
      res = await updateAnnouncement(data)
    } else {
      res = await createAnnouncement(data)
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '发布成功')
      dialogVisible.value = false
      loadAnnouncements()
    } else {
      ElMessage.error(res.message || (isEdit.value ? '修改失败' : '发布失败'))
    }
  } catch (error) {
    console.error(isEdit.value ? '修改公告失败:' : '发布公告失败:', error)
    ElMessage.error(isEdit.value ? '修改公告失败' : '发布公告失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这条公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteAnnouncement(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadAnnouncements()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除公告失败:', error)
      ElMessage.error('删除公告失败')
    }
  }).catch(() => {})
}

const getTagType = (type) => {
  const typeMap = {
    '系统通知': 'danger',
    '考试通知': 'warning',
    '培训通知': 'success',
    '会议通知': 'info',
    '校园通知': 'primary'
  }
  return typeMap[type] || 'info'
}

const formatRoles = (roles) => {
  if (!roles || roles === '') return '所有角色'
  const roleMap = { '0': '教师', '1': '学生', '2': '管理员' }
  return roles.split(',').map(r => roleMap[r.trim()] || r).join('、')
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.announcement-manage {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.table-card {
  margin-top: 0;
}
</style>
