<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button
            v-if="!isEditing"
            type="primary"
            @click="startEdit"
          >
            <el-icon :size="16"><Edit /></el-icon>
            编辑信息
          </el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="profile-form"
      >
        <el-row :gutter="24">
          <el-col :span="12">
            <div class="avatar-section">
              <div class="avatar-wrapper">
                <div class="avatar" :class="{ 'has-image': form.avatar }">
                  <img v-if="form.avatar" :src="form.avatar" :alt="form.username" />
                  <span v-else>{{ userInitials }}</span>

                  <!-- 上传叠加层 -->
                  <div class="avatar-overlay" @click="triggerUpload">
                    <el-icon v-if="!uploading" :size="22"><Camera /></el-icon>
                    <el-icon v-else :size="22" class="is-loading"><Loading /></el-icon>
                    <span>{{ uploading ? '上传中...' : (form.avatar ? '更换头像' : '上传头像') }}</span>
                  </div>
                </div>
                <div class="avatar-info">
                  <div class="username">{{ form.realName || form.username }}</div>
                  <div class="role-tag">
                    <el-tag :type="superAdmin ? 'warning' : isAdmin ? 'danger' : 'success'" size="small">
                      {{ superAdmin ? '系统管理员' : isAdmin ? '教师' : '学生' }}
                    </el-tag>
                  </div>
                </div>
              </div>
              <!-- 隐藏的文件选择器 -->
              <input
                ref="fileInputRef"
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp"
                style="display: none"
                @change="handleFileChange"
              />
            </div>
          </el-col>
        </el-row>

        <el-divider content-position="left">基本信息</el-divider>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input
                v-model="form.realName"
                placeholder="请输入真实姓名"
                :disabled="!isEditing"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="form.phone"
                placeholder="请输入手机号"
                :disabled="!isEditing"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="form.email"
                placeholder="请输入邮箱"
                :disabled="!isEditing"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">账号信息</el-divider>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="用户ID">
              <el-input :value="form.id" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色">
              <el-input :value="superAdmin ? '系统管理员' : isAdmin ? '教师' : '学生'" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="创建时间">
              <el-input :value="formatTime(form.createTime)" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="更新时间">
              <el-input :value="formatTime(form.updateTime)" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <template v-if="isEditing">
          <el-divider content-position="left">修改密码</el-divider>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="新密码" prop="password">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="请输入新密码（留空则不修改）"
                  show-password
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="form.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-row v-if="isEditing">
          <el-col :span="24" class="button-group">
            <el-button @click="cancelEdit">取消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">
              保存修改
            </el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Camera, Loading } from '@element-plus/icons-vue'
import { getUser, setUser, isSuperAdmin } from '../utils/auth'
import { getUserById, updateUser, uploadAvatar } from '../api/user'

const formRef = ref(null)
const user = ref(null)
const isEditing = ref(false)
const submitting = ref(false)
const fileInputRef = ref(null)
const uploading = ref(false)

const form = ref({
  id: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: '',
  createTime: null,
  updateTime: null
})

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
  ],
  password: [
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (rule, value, callback) => {
        if (form.value.password && value !== form.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const isAdmin = computed(() => user.value && user.value.role === 0)
const superAdmin = computed(() => user.value && user.value.role === 2)

const userInitials = computed(() => {
  const name = form.value.realName || form.value.username || '?'
  return name.slice(0, 1).toUpperCase()
})

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleFileChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  // 前端快速校验
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.warning('仅支持 JPG、PNG、GIF、WebP 格式的图片')
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 2MB')
    return
  }

  uploading.value = true
  try {
    const res = await uploadAvatar(file)
    if (res.code === 200) {
      const avatarUrl = res.data
      form.value.avatar = avatarUrl

      // 同步更新 localStorage 中的用户信息
      const storedUser = getUser()
      if (storedUser) {
        storedUser.avatar = avatarUrl
        setUser(storedUser)
        user.value = storedUser
      }

      ElMessage.success('头像上传成功')
      setTimeout(() => window.location.reload(), 500)
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch (err) {
    console.error('头像上传失败', err)
    ElMessage.error('头像上传失败，请检查网络后重试')
  } finally {
    uploading.value = false
    // 清空 input 以便再次选择同一文件
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

const loadUserInfo = async () => {
  try {
    user.value = getUser()
    if (user.value?.id) {
      const res = await getUserById(user.value.id)
      if (res.code === 200) {
        form.value = {
          id: res.data.id,
          username: res.data.username,
          realName: res.data.realName,
          phone: res.data.phone,
          email: res.data.email,
          avatar: res.data.avatar,
          password: '',
          confirmPassword: '',
          createTime: res.data.createTime,
          updateTime: res.data.updateTime
        }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败', error)
    ElMessage.error('加载用户信息失败')
  }
}

const startEdit = () => {
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  form.value.password = ''
  form.value.confirmPassword = ''
  loadUserInfo()
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const submitData = {
      id: form.value.id,
      realName: form.value.realName,
      phone: form.value.phone,
      email: form.value.email
    }

    if (form.value.password) {
      submitData.password = form.value.password
    }

    const res = await updateUser(submitData)
    if (res.code === 200) {
      ElMessage.success('个人信息更新成功')

      const updatedUser = {
        ...user.value,
        realName: form.value.realName,
        phone: form.value.phone,
        email: form.value.email
      }
      setUser(updatedUser)
      user.value = updatedUser

      isEditing.value = false
      form.value.password = ''
      form.value.confirmPassword = ''

      await loadUserInfo()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    if (error !== false) {
      console.error('更新失败', error)
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: var(--space-4);
}

.profile-card {
  border-radius: 16px !important;
  box-shadow: 0 8px 32px rgba(30, 64, 175, 0.08), 0 2px 8px rgba(30, 64, 175, 0.04) !important;
  border: 1px solid rgba(59, 130, 246, 0.06) !important;
  transition: box-shadow var(--transition-base);
}
.profile-card:hover {
  box-shadow: 0 12px 40px rgba(30, 64, 175, 0.12), 0 4px 12px rgba(30, 64, 175, 0.06) !important;
}

.profile-card :deep(.el-card__header) {
  padding: 16px 24px;
}

.card-header {
  display: flex !important;
  justify-content: space-between !important;
  align-items: center;
  width: 100%;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
  padding: 0;
}

.btn-icon { width: 16px; height: 16px; margin-right: 6px; vertical-align: middle; }

.profile-form {
  padding: var(--space-6) var(--space-2);
}

.avatar-section {
  padding: var(--space-8) var(--space-6);
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 40%, #60a5fa 100%);
  border-radius: 14px;
  margin-bottom: var(--space-6);
  position: relative;
  overflow: hidden;
  transition: transform var(--transition-base);
}
.avatar-section::before {
  content: '';
  position: absolute;
  top: -60%;
  right: -10%;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.10) 0%, rgba(255,255,255,0.03) 70%, transparent 100%);
  pointer-events: none;
}
.avatar-section::after {
  content: '';
  position: absolute;
  bottom: -40%;
  left: -5%;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.07) 0%, transparent 70%);
  pointer-events: none;
}
.avatar-section:hover {
  transform: scale(1.005);
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  position: relative;
  z-index: 1;
}

.avatar {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #60a5fa, #93c5fd);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: var(--font-weight-bold);
  color: #1e3a5f;
  margin-right: var(--space-5);
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.35), inset 0 2px 4px rgba(255,255,255,0.3);
  border: 3px solid rgba(255,255,255,0.35);
  flex-shrink: 0;
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}
.avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 28px rgba(59, 130, 246, 0.5), inset 0 2px 4px rgba(255,255,255,0.3);
}

.avatar.has-image {
  background: transparent;
  border: 3px solid rgba(255,255,255,0.5);
  overflow: hidden;
}

.avatar.has-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  gap: 4px;
  opacity: 0;
  cursor: pointer;
  transition: opacity 0.25s ease;
}

.avatar:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay .el-icon {
  filter: drop-shadow(0 1px 3px rgba(0,0,0,0.3));
}

.avatar-info { flex: 1; }
.username {
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  color: #fff;
  margin-bottom: var(--space-2);
  text-shadow: 0 2px 8px rgba(0,0,0,0.12);
  letter-spacing: 0.02em;
}
.role-tag {
  display: inline-block;
}
.role-tag :deep(.el-tag) {
  border: 1px solid rgba(255,255,255,0.25);
  background: rgba(255,255,255,0.15) !important;
  color: #fff !important;
  font-weight: var(--font-weight-medium);
  padding: 0 12px;
  height: 26px;
  line-height: 24px;
  border-radius: 20px;
  backdrop-filter: blur(4px);
}

:deep(.el-divider) {
  margin: var(--space-7) 0;
  border-top: 1px solid var(--color-neutral-100);
}
:deep(.el-divider__text) {
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-500);
  font-size: var(--font-size-sm);
  letter-spacing: 0.04em;
  text-transform: uppercase;
  background: transparent;
  padding-left: 0;
  position: relative;
}
:deep(.el-divider__text)::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 28px;
  height: 2px;
  background: var(--color-primary-400);
  border-radius: 2px;
}

:deep(.el-form-item) {
  margin-bottom: var(--space-5);
  transition: margin-bottom var(--transition-base);
}
:deep(.el-form-item__label) {
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  padding-bottom: 2px;
}
:deep(.el-form-item.is-error .el-form-item__label) {
  color: var(--color-danger-500);
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02), inset 0 1px 2px rgba(0,0,0,0.02);
  border: 1.5px solid var(--color-neutral-100);
  transition: border-color var(--transition-base), box-shadow var(--transition-base);
  padding: 2px 14px;
}
:deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary-300);
}
:deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary-500);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.10);
}
:deep(.el-input__inner) {
  height: 38px;
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
}
:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: var(--color-neutral-50);
  cursor: not-allowed;
  border-color: var(--color-neutral-100);
  opacity: 0.7;
}
:deep(.el-input.is-disabled .el-input__inner) {
  color: var(--color-text-tertiary);
}

:deep(.el-form-item.is-error .el-input__wrapper) {
  border-color: var(--color-danger-500);
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.10);
}

.button-group {
  display: flex;
  justify-content: center;
  gap: var(--space-4);
  margin-top: var(--space-8);
  padding-top: var(--space-6);
  border-top: 1px solid var(--color-neutral-100);
}

:deep(.el-button) {
  border-radius: 10px;
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-sm);
  padding: 10px 24px;
  height: auto;
  transition: all var(--transition-base);
}
:deep(.el-button:hover) {
  transform: translateY(-1px);
}
:deep(.el-button:active) {
  transform: translateY(0);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  color: #fff;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.25);
}
:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  box-shadow: 0 6px 24px rgba(37, 99, 235, 0.35);
  transform: translateY(-2px);
}
:deep(.el-button--primary:active) {
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%);
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
  transform: translateY(0);
}
:deep(.el-button--primary.is-loading) {
  background: linear-gradient(135deg, #93c5fd 0%, #60a5fa 100%);
  box-shadow: none;
  transform: none;
}

:deep(.el-button--default) {
  background: var(--color-neutral-50);
  border: 1.5px solid var(--color-neutral-200);
  color: var(--color-text-secondary);
}
:deep(.el-button--default:hover) {
  background: var(--color-neutral-100);
  border-color: var(--color-neutral-300);
  color: var(--color-text-primary);
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

:deep(.el-tag--danger) {
  background: linear-gradient(135deg, #ef4444, #dc2626) !important;
  border: none !important;
  color: #fff !important;
}
:deep(.el-tag--warning) {
  background: linear-gradient(135deg, #f59e0b, #d97706) !important;
  border: none !important;
  color: #fff !important;
}
:deep(.el-tag--success) {
  background: linear-gradient(135deg, #10b981, #059669) !important;
  border: none !important;
  color: #fff !important;
}
</style>
