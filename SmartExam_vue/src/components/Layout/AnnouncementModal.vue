<template>
  <el-dialog
    v-model="dialogVisible"
    title="公告通知"
    width="800px"
    :close-on-click-modal="false"
  >
    <div class="announcement-list" v-loading="loading">
      <el-empty v-if="!loading && announcements.length === 0" description="暂无公告" />

      <el-row :gutter="16" v-else>
        <el-col :span="12" v-for="item in announcements" :key="item.id">
          <div class="announcement-item">
            <div class="announcement-header">
              <el-tag :type="getTagType(item.type)" size="small">
                {{ item.type }}
              </el-tag>
              <span class="announcement-title">{{ item.title }}</span>
            </div>
            <div class="announcement-content">{{ item.content }}</div>
            <div class="announcement-footer">
              <span class="announcement-time">{{ formatTime(item.createTime) }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAnnouncementsByRole } from '../../api/announcement'
import { getUserRole } from '../../utils/auth'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible'])

const dialogVisible = ref(false)
const loading = ref(false)
const announcements = ref([])

watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val) {
    loadAnnouncements()
  }
})

watch(dialogVisible, (val) => {
  emit('update:visible', val)
})

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const role = getUserRole()
    const res = await getAnnouncementsByRole(role)
    if (res.code === 200) {
      announcements.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取公告失败')
    }
  } catch (error) {
    console.error('获取公告失败:', error)
    ElMessage.error('获取公告失败')
  } finally {
    loading.value = false
  }
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
.announcement-list {
  max-height: 500px;
  overflow-y: auto;
}

.announcement-item {
  padding: 16px;
  margin-bottom: 12px;
  background: var(--color-bg-page);
  border-radius: 8px;
  border: 1px solid var(--color-border-light);
  transition: all 0.3s;
  height: calc(100% - 12px);
  box-sizing: border-box;
}

.announcement-item:hover {
  border-color: var(--color-primary-200);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.announcement-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.announcement-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.announcement-content {
  font-size: 13px;
  color: var(--color-text-regular);
  line-height: 1.6;
  white-space: pre-wrap;
  margin-bottom: 8px;
  max-height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.announcement-footer {
  display: flex;
  justify-content: flex-end;
}

.announcement-time {
  font-size: 12px;
  color: var(--color-text-tertiary);
}
</style>
