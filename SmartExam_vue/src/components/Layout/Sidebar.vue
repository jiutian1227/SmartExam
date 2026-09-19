<template>
  <aside class="sidebar" :class="{ collapsed: collapsed }">
    <div class="sidebar-header">
      <div class="logo">
        <svg class="logo-icon" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
          <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z" fill="#60a5fa"/>
          <path d="M512 224c-159.1 0-288 128.9-288 288s128.9 288 288 288 288-128.9 288-288S671.1 224 512 224zm0 464c-97.3 0-176-78.7-176-176s78.7-176 176-176 176 78.7 176 176-78.7 176-176 176z" fill="#93c5fd"/>
        </svg>
        <span v-show="!collapsed" class="logo-text">智能考试平台</span>
      </div>
    </div>

    <nav class="sidebar-nav">
      <el-menu
        :default-active="activeMenu"
        class="nav-menu"
        mode="vertical"
        :collapse="collapsed"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <template v-if="superAdmin">
          <el-menu-item index="/admin/exam">
            <el-icon><Notebook /></el-icon>
            <span>考试管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/question">
            <el-icon><QuestionFilled /></el-icon>
            <span>试题管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/announcement">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/user">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/group">
            <el-icon><Avatar /></el-icon>
            <span>用户组管理</span>
          </el-menu-item>
        </template>

        <template v-else-if="isAdminRole">
          <el-sub-menu index="teacher-group">
            <template #title>
              <el-icon><Management /></el-icon>
              <span>教师功能</span>
            </template>
            <el-menu-item index="/teacher/exam">
              <el-icon><Notebook /></el-icon><span>考试管理</span>
            </el-menu-item>
            <el-menu-item index="/teacher/grading">
              <el-icon><List /></el-icon><span>判卷管理</span>
            </el-menu-item>
            <el-menu-item index="/teacher/question">
              <el-icon><QuestionFilled /></el-icon><span>试题管理</span>
            </el-menu-item>
            <el-menu-item index="/teacher/group">
              <el-icon><Avatar /></el-icon><span>用户组管理</span>
            </el-menu-item>
          </el-sub-menu>
        </template>

        <template v-else>
          <el-sub-menu index="student-group">
            <template #title>
              <el-icon><School /></el-icon>
              <span>学生功能</span>
            </template>
            <el-menu-item index="/student/exam-list">
              <el-icon><Notebook /></el-icon><span>考试列表</span>
            </el-menu-item>
            <el-menu-item index="/student/my-score">
              <el-icon><StarFilled /></el-icon><span>成绩查询</span>
            </el-menu-item>
            <el-menu-item index="/student/join-group">
              <el-icon><Avatar /></el-icon><span>加入用户组</span>
            </el-menu-item>
          </el-sub-menu>
        </template>

        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>
    </nav>

    <div class="sidebar-footer" @click="goToProfile">
      <div class="user-avatar">
        <img v-if="user?.avatar" :src="user.avatar" :alt="userInitials" class="avatar-img" />
        <span v-else>{{ userInitials }}</span>
        <span class="status-dot"></span>
      </div>
      <div v-show="!collapsed" class="user-meta">
        <div class="user-name">{{ user?.realName || user?.username }}</div>
        <div class="user-role">{{ superAdmin ? '系统管理员' : isAdmin ? '教师' : '学生' }}</div>
      </div>
      <el-icon v-show="!collapsed" class="settings-icon"><Setting /></el-icon>
    </div>
  </aside>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUser } from '../../utils/auth'
import {
  HomeFilled, Notebook, QuestionFilled, UserFilled,
  Avatar, School, User, Setting, Management, Bell,
  List, StarFilled
} from '@element-plus/icons-vue'

defineProps({
  collapsed: { type: Boolean, default: false }
})

const route = useRoute()
const router = useRouter()
const user = ref(null)

const activeMenu = computed(() => route.path)
const isAdmin = computed(() => user.value && (user.value.role === 0 || user.value.role === 2))
const superAdmin = computed(() => user.value && user.value.role === 2)
const isAdminRole = computed(() => user.value && user.value.role === 0)
const userInitials = computed(() => {
  const name = user.value?.realName || user.value?.username || '?'
  return name.slice(0, 1)
})

const handleMenuSelect = (index) => {
  router.push(index)
}

const goToProfile = () => {
  router.push('/profile')
}

onMounted(() => {
  user.value = getUser()
})
</script>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 260px;
  background: linear-gradient(180deg, #1e40af 0%, #1e3a8a 50%, #1e40af 100%);
  color: #fff;
  z-index: var(--z-fixed);
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 20px rgba(30, 58, 138, 0.35);
  overflow: hidden;
  transition: width var(--transition-base);
}

.sidebar.collapsed {
  width: 68px;
}

/* ── Header / Logo ── */
.sidebar-header {
  padding: var(--space-6) var(--space-5) var(--space-5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  white-space: nowrap;
  overflow: hidden;
}

.logo-icon {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  filter: drop-shadow(0 0 8px rgba(96, 165, 250, 0.4));
}

.logo-text {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  background: linear-gradient(90deg, #60a5fa 0%, #93c5fd 25%, #bfdbfe 50%, #93c5fd 75%, #60a5fa 100%);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
  text-shadow: 0 0 20px rgba(96, 165, 250, 0.3);
  animation: shimmer 2.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 0% 50%;
  }
  100% {
    background-position: 200% 50%;
  }
}

/* ── Nav ── */
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-3) 0;
}

.sidebar-nav::-webkit-scrollbar { width: 4px; }
.sidebar-nav::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.15); border-radius: 2px; }

.nav-menu {
  border: none !important;
  background: transparent !important;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 44px;
  line-height: 44px;
  margin: 2px var(--space-3);
  padding: 0 var(--space-3) !important;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.9) !important;
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
  position: relative;
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  width: 20px;
  height: 20px;
  margin-right: 10px;
  color: rgba(255, 255, 255, 0.8);
  transition: color var(--transition-fast);
}

:deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
}

:deep(.el-menu-item:hover .el-icon) {
  color: #60a5fa !important;
}

:deep(.el-menu-item.is-active) {
  background: rgba(96, 165, 250, 0.12) !important;
  color: #fff !important;
  font-weight: var(--font-weight-semibold);
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: var(--color-primary-400);
  border-radius: 0 3px 3px 0;
  box-shadow: 0 0 8px rgba(96, 165, 250, 0.5);
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #60a5fa !important;
}

:deep(.el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
}

:deep(.el-sub-menu__title:hover .el-icon) {
  color: #60a5fa !important;
}

:deep(.el-sub-menu .el-menu) {
  background: transparent !important;
  padding: 0;
}

:deep(.el-sub-menu .el-menu .el-menu-item) {
  padding-left: 44px !important;
  height: 40px;
  line-height: 40px;
  font-size: var(--font-size-sm);
  margin: 1px var(--space-3);
  border-radius: 8px;
}

:deep(.el-sub-menu .el-menu .el-menu-item.is-active) {
  background: rgba(96, 165, 250, 0.12) !important;
}

:deep(.el-menu--popup) {
  background: #1e293b !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  border-radius: var(--radius-lg);
  padding: var(--space-2) !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

/* Collapsed state menu */
:deep(.el-menu--collapse) {
  width: auto !important;
}

:deep(.el-menu--collapse .el-menu-item) {
  padding: 0 !important;
  justify-content: center;
  margin: 2px var(--space-2);
}

:deep(.el-menu--collapse .el-menu-item .el-icon) {
  margin-right: 0;
}

:deep(.el-menu--collapse .el-sub-menu__title) {
  padding: 0 !important;
  justify-content: center;
  margin: 2px var(--space-2);
}

:deep(.el-menu--collapse .el-sub-menu__title .el-icon) {
  margin-right: 0;
}

:deep(.el-menu--collapse .el-sub-menu__title span) {
  display: none;
}

/* ── Footer ── */
.sidebar-footer {
  display: flex;
  align-items: center;
  padding: var(--space-4) var(--space-5);
  margin: var(--space-2) var(--space-3) var(--space-4);
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-base);
  flex-shrink: 0;
}

.sidebar.collapsed .sidebar-footer {
  justify-content: center;
  padding: var(--space-3);
  margin: var(--space-2) var(--space-2) var(--space-4);
}

.sidebar-footer:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(96, 165, 250, 0.3);
  transform: translateY(-1px);
}

.user-avatar {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.status-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-success);
  border: 2px solid #1e293b;
}

.user-meta {
  flex: 1;
  min-width: 0;
  margin-left: var(--space-3);
}

.user-name {
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.user-role {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: var(--font-weight-medium);
}

.settings-icon {
  color: rgba(255, 255, 255, 0.3);
  transition: all var(--transition-base);
  flex-shrink: 0;
  font-size: 16px;
}

.sidebar-footer:hover .settings-icon {
  color: #93c5fd;
  transform: rotate(45deg);
}

.collapsed .user-avatar {
  margin-right: 0;
}
</style>
