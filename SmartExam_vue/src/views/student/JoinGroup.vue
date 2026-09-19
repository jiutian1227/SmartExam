<template>
  <div class="join-group">
    <el-card class="code-card">
      <div class="code-header">
        <h4>通过分享码加入用户组</h4>
        <p class="code-hint">请输入老师提供的分享码，查询用户组信息后确认加入</p>
      </div>
      <div class="code-input-row">
        <el-input
          v-model="shareCode"
          placeholder="请输入分享码"
          class="share-code-input"
          size="large"
          @keyup.enter="handleQueryByCode"
        />
        <el-button type="primary" size="large" @click="handleQueryByCode" :loading="querying">
          <el-icon style="margin-right: 4px;"><Search /></el-icon>
          查询
        </el-button>
      </div>

      <div v-if="queriedGroup" class="group-info-card">
        <el-divider />
        <div class="group-info-header">
          <h5>
            <el-icon style="margin-right: 4px;"><Avatar /></el-icon>
            用户组信息
          </h5>
          <el-tag v-if="queriedGroup.isMember" type="success" size="large">已加入</el-tag>
        </div>
        <el-descriptions :column="2" border class="group-descriptions">
          <el-descriptions-item label="用户组名称" :span="2">
            <span class="group-name">{{ queriedGroup.name }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ queriedGroup.description || '暂无描述' }}
          </el-descriptions-item>
          <el-descriptions-item label="分享码">
            <el-tag>{{ queriedGroup.shareCode }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="成员数">
            {{ queriedGroup.memberCount || 0 }} 人
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(queriedGroup.createTime) }}
          </el-descriptions-item>
        </el-descriptions>
        <div class="join-actions">
          <el-button
            v-if="!queriedGroup.isMember"
            type="primary"
            size="large"
            @click="handleJoinQueriedGroup"
            :loading="joining"
          >
            <el-icon style="margin-right: 4px;"><CirclePlus /></el-icon>
            加入该用户组
          </el-button>
          <el-button v-else type="default" size="large" disabled>
            已加入该用户组
          </el-button>
        </div>
      </div>

      <div v-if="queryError" class="query-error">
        <el-alert :title="queryError" type="error" :closable="false" show-icon />
      </div>
    </el-card>

    <el-card class="my-groups-card">
      <div class="section-header">
        <h4>
          <el-icon style="margin-right: 4px;"><Avatar /></el-icon>
          已加入的用户组
        </h4>
      </div>
      <el-table :data="myGroups" border v-loading="loading">
        <el-table-column prop="name" label="用户组名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="danger" size="default" :icon="Close" @click="handleLeaveGroup(scope.row)" >退出</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="myGroups.length === 0 && !loading" class="empty-text">
        暂未加入任何用户组
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { joinGroup as joinGroupApi, getMyGroups, joinByShareCode, leaveGroup, getGroupByShareCode } from '../../api/group'
import { getUser } from '../../utils/auth'
import { formatDateTime } from '../../utils/format'
import { Search, CirclePlus, Avatar, CircleClose, InfoFilled, Close } from '@element-plus/icons-vue'

const myGroups = ref([])
const loading = ref(false)
const shareCode = ref('')
const querying = ref(false)
const joining = ref(false)
const queriedGroup = ref(null)
const queryError = ref('')

const loadMyGroups = async () => {
  loading.value = true
  try {
    const user = getUser()
    const userId = user?.id
    if (!userId) {
      ElMessage.error('用户未登录')
      return
    }
    const myGroupsRes = await getMyGroups(userId)
    if (myGroupsRes.code === 200) {
      myGroups.value = myGroupsRes.data || []
    }
  } catch (error) {
    console.error('加载用户组列表失败', error)
    ElMessage.error('加载用户组列表失败')
  } finally {
    loading.value = false
  }
}

const handleQueryByCode = async () => {
  if (!shareCode.value.trim()) {
    ElMessage.warning('请输入分享码')
    return
  }

  queryError.value = ''
  queriedGroup.value = null
  querying.value = true

  try {
    const user = getUser()
    const userId = user?.id
    const res = await getGroupByShareCode(shareCode.value.trim(), userId)
    if (res.code === 200) {
      queriedGroup.value = res.data
    } else {
      queryError.value = res.message || '查询失败'
    }
  } catch (error) {
    queryError.value = '查询失败，请检查分享码是否正确'
  } finally {
    querying.value = false
  }
}

const handleJoinQueriedGroup = async () => {
  if (!queriedGroup.value) return

  joining.value = true
  try {
    const user = getUser()
    const userId = user?.id
    const res = await joinByShareCode({
      shareCode: queriedGroup.value.shareCode,
      userId
    })
    if (res.code === 200) {
      ElMessage.success('加入成功')
      queriedGroup.value.isMember = true
      await loadMyGroups()
    } else {
      ElMessage.error(res.message || '加入失败')
    }
  } catch (error) {
    ElMessage.error('加入失败')
  } finally {
    joining.value = false
  }
}

const handleLeaveGroup = async (group) => {
  try {
    const user = getUser()
    const userId = user?.id
    if (!userId) {
      ElMessage.error('用户未登录')
      return
    }

    await ElMessageBox.confirm(
      `确定要退出用户组「${group.name}」吗？`,
      '退出确认',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    await leaveGroup({
      userGroupId: group.id,
      userId
    })
    ElMessage.success(`已退出 ${group.name}`)
    await loadMyGroups()
    if (queriedGroup.value && queriedGroup.value.id === group.id) {
      queriedGroup.value.isMember = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出用户组失败', error)
      ElMessage.error('退出用户组失败')
    }
  }
}

onMounted(() => {
  loadMyGroups()
})
</script>

<style scoped>
.join-group {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
}

.code-card {
  margin-bottom: var(--space-6);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-neutral-200);
  overflow: hidden;
}

.code-header {
  margin-bottom: var(--space-5);
}
.code-header h4 {
  margin: 0 0 var(--space-2) 0;
  color: var(--color-text-primary);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
}
.code-hint {
  margin: 0;
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
}

.code-input-row {
  display: flex;
  gap: var(--space-3);
}
.share-code-input {
  flex: 1;
  max-width: 400px;
}
.share-code-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-base);
  transition: all var(--transition-base);
}
.share-code-input :deep(.el-input__wrapper:hover),
.share-code-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary-400) inset;
}
.code-input-row .el-button {
  border-radius: var(--radius-base);
  font-weight: var(--font-weight-medium);
  padding: 0 var(--space-6);
  transition: all 0.25s ease;
}
.code-input-row .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.group-info-card {
  margin-top: var(--space-1);
  padding: var(--space-3) 0;
}

.group-info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}
.group-info-header h5 {
  margin: 0;
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-semibold);
  display: flex;
  align-items: center;
}
.group-info-header :deep(.el-tag--success) {
  border-radius: var(--radius-base);
  font-weight: var(--font-weight-medium);
  border: none;
}
.group-name {
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-md);
  color: var(--color-primary-600);
}

.group-descriptions {
  margin-bottom: var(--space-5);
}
.group-descriptions :deep(.el-descriptions__title) {
  font-weight: var(--font-weight-semibold);
}
.group-descriptions :deep(.el-descriptions__label) {
  color: var(--color-text-tertiary);
  font-weight: var(--font-weight-medium);
}
.group-descriptions :deep(.el-tag) {
  border-radius: var(--radius-sm);
  font-weight: var(--font-weight-medium);
  border: 1px solid var(--color-primary-200);
  background: var(--color-primary-50);
  color: var(--color-primary-600);
}

.join-actions {
  display: flex;
  justify-content: center;
  padding-top: var(--space-3);
}
.join-actions .el-button {
  min-width: 200px;
  border-radius: var(--radius-base);
  font-weight: var(--font-weight-medium);
  transition: all 0.25s ease;
}
.join-actions .el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.query-error {
  margin-top: var(--space-4);
}
.query-error :deep(.el-alert) {
  border-radius: var(--radius-base);
}

.my-groups-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-neutral-200);
  overflow: hidden;
}

.section-header {
  margin-bottom: var(--space-4);
}
.section-header h4 {
  margin: 0;
  font-size: var(--font-size-lg);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-semibold);
  position: relative;
  padding-left: var(--space-4);
  display: flex;
  align-items: center;
}
.section-header h4::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: var(--color-primary-500);
  border-radius: 2px;
}

.my-groups-card :deep(.el-table) {
  border: none;
}
.my-groups-card :deep(.el-table th.el-table__cell) {
  background: var(--color-neutral-50);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-sm);
}
.my-groups-card :deep(.el-table td.el-table__cell) {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
}
.my-groups-card :deep(.el-table__body tr:hover td) {
  background: var(--color-primary-50);
}
.my-groups-card :deep(.el-button--danger) {
  border-radius: var(--radius-base);
  font-weight: var(--font-weight-medium);
  transition: all 0.25s ease;
}

.empty-text {
  text-align: center;
  padding: var(--space-10) 0;
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
}
</style>
