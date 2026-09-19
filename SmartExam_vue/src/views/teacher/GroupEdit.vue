<template>
  <div class="group-edit">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>{{ isEditMode ? '编辑用户组' : '添加用户组' }}</h2>
    </div>

    <el-card class="form-card">
      <h4>基本信息</h4>
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户组名称">
          <el-input v-model="form.name" placeholder="请输入用户组名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input type="textarea" v-model="form.description" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="分享码" v-if="form.shareCode">
          <div class="share-code-row">
            <el-input :value="form.shareCode" disabled class="share-code-input" />
            <el-button type="warning" size="small" @click="handleRefreshShareCode">
              刷新分享码
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSaveBasic">
            {{ isEditMode ? '保存基本信息' : '创建用户组' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="isEditMode && form.id" class="member-card">
      <div class="section-header">
        <h4>成员列表</h4>
        <span class="member-count">当前成员：{{ memberList.length }} 人</span>
      </div>

      <el-table :data="memberList" border style="margin-top: 16px">
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="joinTime" label="加入时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.joinTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="danger" size="small" :icon="Remove" circle @click="handleRemoveMember(scope.row)" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-else-if="!isEditMode" class="hint-card">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          请先创建用户组，用户可通过分享码加入
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Remove } from '@element-plus/icons-vue'
import { getGroupById, createGroup, updateGroup, getGroupMembers, removeGroupMember, refreshShareCode } from '../../api/group'
import { getUser } from '../../utils/auth'
import { formatDateTime } from '../../utils/format'

const router = useRouter()
const route = useRoute()

const currentUser = getUser()
const isEditMode = ref(false)
const memberList = ref([])

const form = ref({
  id: null,
  name: '',
  description: '',
  shareCode: '',
  creatorId: currentUser?.id || 1
})

const formatTime = formatDateTime

const goBack = () => {
  router.push('/teacher/group')
}

const loadGroup = async (id) => {
  try {
    const res = await getGroupById(id)
    if (res.code === 200) {
      form.value = {
        id: res.data.id,
        name: res.data.name,
        description: res.data.description,
        shareCode: res.data.shareCode,
        creatorId: res.data.creatorId
      }
    }
  } catch (error) {
    ElMessage.error('加载用户组信息失败')
  }
}

const loadMembers = async (id) => {
  try {
    const res = await getGroupMembers(id)
    if (res.code === 200) {
      memberList.value = res.data || []
    }
  } catch (error) {
    console.error('加载成员列表失败', error)
  }
}

const handleSaveBasic = async () => {
  if (!form.value.name) {
    ElMessage.error('请填写用户组名称')
    return
  }

  try {
    let res
    if (isEditMode.value) {
      res = await updateGroup(form.value)
      if (res.code === 200) {
        ElMessage.success('保存成功')
      }
    } else {
      res = await createGroup(form.value)
      if (res.code === 200) {
        ElMessage.success('创建成功')
        form.value.id = res.data?.id
        form.value.shareCode = res.data?.shareCode
        isEditMode.value = true
        await loadMembers(res.data?.id)
      }
    }

    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleRefreshShareCode = async () => {
  if (!form.value.id) return

  try {
    const res = await refreshShareCode(form.value.id)
    if (res.code === 200) {
      form.value.shareCode = res.data?.shareCode
      ElMessage.success('分享码已更新')
    } else {
      ElMessage.error(res.message || '刷新失败')
    }
  } catch (error) {
    ElMessage.error('刷新失败')
  }
}

const handleRemoveMember = async (row) => {
  try {
    const res = await removeGroupMember(form.value.id, row.userId)
    if (res.code === 200) {
      ElMessage.success('移除成功')
      await loadMembers(form.value.id)
    } else {
      ElMessage.error(res.message || '移除失败')
    }
  } catch (error) {
    ElMessage.error('移除失败')
  }
}

onMounted(async () => {
  const id = route.query.id
  if (id) {
    form.value.id = parseInt(id)
    isEditMode.value = true
    await loadGroup(id)
    await loadMembers(id)
  }
})
</script>

<style scoped>
.group-edit {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: var(--space-4);
}

.page-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.page-header :deep(.el-button) {
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.25s ease;
}

.page-header h2 {
  margin: 0;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.form-card,
.member-card,
.hint-card {
  margin-bottom: var(--space-6);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.form-card:hover,
.member-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

:deep(.el-card__body) {
  padding: var(--space-6);
}

.form-card h4 {
  margin: 0 0 var(--space-5) 0;
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.section-header h4 {
  margin: 0;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.member-count {
  color: var(--color-primary-600);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  background: var(--color-primary-50);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid var(--color-primary-100);
}

/* Share code display */
.share-code-row {
  display: flex;
  gap: var(--space-3);
  align-items: center;
}

.share-code-input {
  width: 220px;
}

.share-code-input :deep(.el-input__inner) {
  background: var(--color-neutral-50);
  color: var(--color-primary-600);
  font-weight: var(--font-weight-semibold);
  font-family: monospace;
  letter-spacing: 1px;
}

.share-code-input :deep(.el-input__wrapper) {
  border-color: var(--color-primary-200);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.share-code-input :deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary-400);
  box-shadow: 0 0 0 1px var(--color-primary-200);
}

/* Table styling */
:deep(.el-table) {
  border-radius: var(--radius);
  overflow: hidden;
}

:deep(.el-table th.el-table__cell) {
  background-color: var(--color-primary-50);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-semibold);
}

:deep(.el-table__body tr:hover > td) {
  background-color: var(--color-primary-50);
  transition: background-color 0.2s ease;
}

:deep(.el-table__body tr) {
  transition: background-color 0.2s ease;
}

/* Button transitions */
:deep(.el-button) {
  transition: all 0.25s ease;
}

/* Alert styling */
.hint-card :deep(.el-alert) {
  border-radius: var(--radius);
}

/* Form label styling */
:deep(.el-form-item__label) {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}
</style>
