<template>
  <header class="app-header" :class="{ collapsed: collapsed }">
    <div class="header-left">
      <el-button class="collapse-btn" text @click="$emit('toggleCollapse')">
        <el-icon><Expand v-if="collapsed" /><Fold v-else /></el-icon>
      </el-button>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">
          <el-icon><HomeFilled /></el-icon>
        </el-breadcrumb-item>
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path || undefined">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="header-right">
      <el-button class="notif-btn" text @click="$emit('openAnnouncement')">
        <el-badge :value="0" :hidden="true" class="notif-badge">
          <el-icon class="header-icon"><Bell /></el-icon>
        </el-badge>
      </el-button>
      <el-button class="logout-btn" text @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        <span>退出登录</span>
      </el-button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { removeToken, removeUser } from '../../utils/auth'
import {
  HomeFilled, Fold, Expand, Bell, SwitchButton
} from '@element-plus/icons-vue'

defineProps({
  collapsed: { type: Boolean, default: false }
})

defineEmits(['toggleCollapse', 'openAnnouncement'])

const route = useRoute()
const router = useRouter()

const breadcrumbs = computed(() => {
  const matched = route.matched
  const crumbs = []
  // Skip root layout route, start from children
  for (let i = 1; i < matched.length; i++) {
    const meta = matched[i].meta
    if (meta?.title) {
      crumbs.push({
        title: meta.title,
        path: matched[i].path
      })
    }
  }
  // Handle edit/create distinction for dynamic routes
  const routeName = route.name
  if (routeName === 'ExamEdit' || routeName === 'GroupEdit') {
    const isEdit = !!route.params.id
    if (isEdit) {
      crumbs[crumbs.length - 1].title = '编辑'
    } else {
      crumbs[crumbs.length - 1].title = '新增'
    }
  }
  if (routeName === 'GradingView') {
    crumbs.push({ title: '批阅试卷', path: '' })
  }
  if (routeName === 'ScoreDetail') {
    crumbs.push({ title: '成绩详情', path: '' })
  }
  return crumbs
})

const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    removeToken()
    removeUser()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-6);
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border-bottom: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  z-index: var(--z-sticky);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex: 1;
  min-width: 0;
}

.collapse-btn {
  font-size: 20px;
  color: var(--color-text-secondary);
  padding: var(--space-2);
  border-radius: var(--radius-base);
  transition: all var(--transition-fast);
}

.collapse-btn:hover {
  color: var(--color-primary-500);
  background: var(--color-primary-50);
}

:deep(.el-breadcrumb) {
  font-size: var(--font-size-sm);
}

:deep(.el-breadcrumb__inner) {
  color: var(--color-text-tertiary) !important;
  font-weight: var(--font-weight-regular) !important;
}

:deep(.el-breadcrumb__inner:hover) {
  color: var(--color-primary-500) !important;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--color-text-primary) !important;
  font-weight: var(--font-weight-medium) !important;
}

:deep(.el-breadcrumb__inner .el-icon) {
  font-size: 16px;
  vertical-align: middle;
  margin-bottom: 2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.notif-btn {
  position: relative;
  padding: var(--space-2);
  border-radius: var(--radius-base);
  font-size: 18px;
  color: var(--color-text-secondary);
  transition: all var(--transition-fast);
}

.notif-btn:hover {
  color: var(--color-primary-500);
  background: var(--color-primary-50);
}

.notif-badge :deep(.el-badge__content) {
  border: 2px solid var(--color-bg-elevated);
}

.logout-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-base);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
}

.logout-btn:hover {
  color: var(--color-danger);
  background: var(--color-danger-50);
}

.logout-btn .el-icon {
  font-size: 16px;
  transition: transform var(--transition-fast);
}

.logout-btn:hover .el-icon {
  transform: translateX(2px);
}

.header-icon {
  font-size: 18px;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .app-header {
    padding: 0 var(--space-4);
  }
  .logout-btn span {
    display: none;
  }
}
</style>
