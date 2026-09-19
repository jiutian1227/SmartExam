import { createRouter, createWebHistory } from 'vue-router'
import { getToken, isAdmin, isSuperAdmin, isStudent } from '../utils/auth'

const routes = [
  { path: '/login', name: 'Login', meta: { title: '登录' }, component: () => import('../views/login.vue') },
  { path: '/register', name: 'Register', meta: { title: '注册' }, component: () => import('../views/register.vue') },
  {
    path: '/student/exam-detail/:id',
    name: 'ExamDetail',
    meta: { title: '考试', role: 'student' },
    component: () => import('../views/student/ExamDetail.vue')
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../components/layout/AppLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'HomePage', meta: { title: '首页' }, component: () => import('../views/home.vue') },

      // ── Super Admin 路由 (role=2) ──
      { path: 'admin/exam', name: 'AdminExamManage', meta: { title: '考试管理', role: 'admin' }, component: () => import('../views/admin/ExamManage.vue') },
      { path: 'admin/exam/edit/:id?', name: 'AdminExamEdit', meta: { title: '编辑考试', role: 'admin' }, component: () => import('../views/admin/ExamEdit.vue') },
      { path: 'admin/question', name: 'AdminQuestionManage', meta: { title: '试题管理', role: 'admin' }, component: () => import('../views/admin/QuestionManage.vue') },
      { path: 'admin/group', name: 'AdminGroupManage', meta: { title: '用户组管理', role: 'admin' }, component: () => import('../views/admin/GroupManage.vue') },
      { path: 'admin/group/edit/:id?', name: 'AdminGroupEdit', meta: { title: '编辑用户组', role: 'admin' }, component: () => import('../views/admin/GroupEdit.vue') },
      { path: 'admin/user', name: 'UserManage', meta: { title: '用户管理', role: 'admin' }, component: () => import('../views/admin/UserManage.vue') },
      { path: 'admin/announcement', name: 'AdminAnnouncementManage', meta: { title: '公告管理', role: 'admin' }, component: () => import('../views/admin/AnnouncementManage.vue') },

      // ── 教师路由 (role=0) ──
      { path: 'teacher/exam', name: 'TeacherExamManage', meta: { title: '考试管理', role: 'teacher' }, component: () => import('../views/teacher/ExamManage.vue') },
      { path: 'teacher/exam/edit/:id?', name: 'TeacherExamEdit', meta: { title: '编辑考试', role: 'teacher' }, component: () => import('../views/teacher/ExamEdit.vue') },
      { path: 'teacher/question', name: 'TeacherQuestionManage', meta: { title: '试题管理', role: 'teacher' }, component: () => import('../views/teacher/QuestionManage.vue') },
      { path: 'teacher/group', name: 'TeacherGroupManage', meta: { title: '用户组管理', role: 'teacher' }, component: () => import('../views/teacher/GroupManage.vue') },
      { path: 'teacher/group/edit/:id?', name: 'TeacherGroupEdit', meta: { title: '编辑用户组', role: 'teacher' }, component: () => import('../views/teacher/GroupEdit.vue') },
      { path: 'teacher/grading', name: 'TeacherGradingList', meta: { title: '判卷管理', role: 'teacher' }, component: () => import('../views/teacher/GradingList.vue') },
      { path: 'teacher/grading/:id', name: 'TeacherGradingDetail', meta: { title: '判卷详情', role: 'teacher' }, component: () => import('../views/teacher/GradingDetail.vue') },
      { path: 'teacher/grading/:examId/:recordId', name: 'TeacherGradingView', meta: { title: '判卷', role: 'teacher' }, component: () => import('../views/teacher/GradingView.vue') },
      
      // ── 学生路由 (role=1) ──
      { path: 'student/exam-list', name: 'ExamList', meta: { title: '考试列表', role: 'student' }, component: () => import('../views/student/ExamList.vue') },
      { path: 'student/my-score', name: 'MyScore', meta: { title: '我的成绩', role: 'student' }, component: () => import('../views/student/MyScore.vue') },
      { path: 'student/my-score/:id', name: 'ScoreDetail', meta: { title: '成绩详情', role: 'student' }, component: () => import('../views/student/ScoreDetail.vue') },
      { path: 'student/join-group', name: 'JoinGroup', meta: { title: '加入组', role: 'student' }, component: () => import('../views/student/JoinGroup.vue') },

      // ── 共享路由 ──
      { path: 'profile', name: 'Profile', meta: { title: '个人信息' }, component: () => import('../views/Profile.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const hasToken = getToken()
  const publicPaths = ['/login', '/register']

  if (publicPaths.includes(to.path)) {
    if (hasToken) {
      next('/home')
    } else {
      next()
    }
  } else {
    if (!hasToken) {
      next('/login')
    } else {
      const userIsAdmin = isAdmin()
      const userIsSuperAdmin = isSuperAdmin()
      const userIsStudent = isStudent()
      const path = to.path

      // 用户管理只能超级管理员
      if (path === '/admin/user' && !userIsSuperAdmin) {
        return next('/home')
      }
      // /admin/* 只能超级管理员
      if (path.startsWith('/admin') && !userIsSuperAdmin) {
        return next('/home')
      }
      // /teacher/* 只能教师
      if (path.startsWith('/teacher') && !userIsAdmin) {
        return next('/home')
      }
      // /student/* 只能学生
      if ((path.startsWith('/student') || path.startsWith('/student/exam-detail')) && !userIsStudent) {
        return next('/home')
      }
      next()
    }
  }
})

export default router
