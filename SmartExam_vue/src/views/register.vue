<template>
  <div class="register-page">
    <div class="register-bg">
      <div class="bg-questions">
        <div
          v-for="(q, idx) in displayQuestions"
          :key="q.id"
          class="question-card"
          :class="[`question-${(idx % 14) + 1}`, { 'fade-out': q.fadeOut }]"
        >
          <div class="q-content">{{ q.content }}</div>
          <div v-if="q.options" class="q-options">
            <div v-for="(opt, oIdx) in q.options" :key="oIdx" class="q-option">
              <span class="option-label">{{ String.fromCharCode(65 + oIdx) }}.</span>
              <span class="option-text">{{ opt }}</span>
            </div>
          </div>
          <div class="q-answer">
            <span class="answer-label">答案：</span>
            <span class="answer-value">{{ q.answer }}</span>
          </div>
        </div>
      </div>
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <div class="register-logo">
            <svg viewBox="0 0 1024 1024" width="56" height="56">
              <defs>
                <linearGradient id="regGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#3b82f6"/>
                  <stop offset="100%" stop-color="#60a5fa"/>
                </linearGradient>
              </defs>
              <path d="M512 64C264.6 64 64 264.6 64 512s200.6 448 448 448 448-200.6 448-448S759.4 64 512 64zm0 820c-205.4 0-372-166.6-372-372s166.6-372 372-372 372 166.6 372 372-166.6 372-372 372z" fill="url(#regGrad)"/>
              <path d="M512 224c-159.1 0-288 128.9-288 288s128.9 288 288 288 288-128.9 288-288S671.1 224 512 224zm0 464c-97.3 0-176-78.7-176-176s78.7-176 176-176 176 78.7 176 176-78.7 176-176 176z" fill="#93c5fd"/>
            </svg>
            <div class="logo-glow"></div>
          </div>
          <h1 class="register-title">智能考试平台</h1>
          <p class="register-subtitle">Smart Exam Platform · 高效 · 智能 · 便捷</p>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" class="register-form" @submit.prevent="handleRegister">
          <div class="role-section">
            <label class="role-label">选择角色</label>
            <div class="role-grid">
              <div class="role-card" :class="{ active: form.role === 1 }" @click="form.role = 1">
                <div class="role-icon student">
                  <el-icon :size="16"><School /></el-icon>
                </div>
                <div class="role-body">
                  <span class="role-name">学生</span>
                  <span class="role-desc">参加考试 · 查看成绩</span>
                </div>
                <div class="role-check" :class="{ checked: form.role === 1 }">
                  <el-icon v-if="form.role === 1" :size="12"><Check /></el-icon>
                </div>
              </div>
              <div class="role-card" :class="{ active: form.role === 0 }" @click="form.role = 0">
                <div class="role-icon teacher">
                  <el-icon :size="16"><User /></el-icon>
                </div>
                <div class="role-body">
                  <span class="role-name">教师</span>
                  <span class="role-desc">创建考试 · 批阅试卷</span>
                </div>
                <div class="role-check" :class="{ checked: form.role === 0 }">
                  <el-icon v-if="form.role === 0" :size="12"><Check /></el-icon>
                </div>
              </div>
            </div>
          </div>

          <div class="form-group">
            <div class="input-wrapper">
              <span class="input-icon">
                <el-icon><User /></el-icon>
              </span>
              <el-input v-model="form.username" prop="username" placeholder="用户名" class="custom-input" />
            </div>
          </div>

          <div class="form-group">
            <div class="input-wrapper">
              <span class="input-icon">
                <el-icon><UserFilled /></el-icon>
              </span>
              <el-input v-model="form.realName" prop="realName" placeholder="真实姓名" class="custom-input" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group flex-1">
              <div class="input-wrapper">
                <span class="input-icon">
                  <el-icon><Lock /></el-icon>
                </span>
                <el-input type="password" v-model="form.password" prop="password" placeholder="密码" class="custom-input" show-password />
              </div>
            </div>
            <div class="form-group flex-1">
              <div class="input-wrapper">
                <span class="input-icon">
                  <el-icon><Lock /></el-icon>
                </span>
                <el-input type="password" v-model="form.confirmPassword" prop="confirmPassword" placeholder="确认密码" class="custom-input" show-password />
              </div>
            </div>
          </div>

          <div class="form-agreement">
            <el-checkbox v-model="agreed">
              我已阅读并同意
              <a href="#" class="agree-link" @click.prevent="showAgreement = true">《用户服务协议》</a>
            </el-checkbox>
          </div>

          <div class="vcode-wrapper">
            <CaptchaSlider
              :visible="showVcode"
              @success="onCaptchaSuccess"
              @fail="onCaptchaFail"
              @close="showVcode = false"
            />
            <el-button class="register-btn" @click="handleRegisterBtnClick">
              <span class="btn-content">
                <el-icon :size="18"><CirclePlus /></el-icon>
                创建账号
              </span>
            </el-button>
          </div>

          <div class="register-footer">
            <span class="footer-text">已有账号？</span>
            <a href="javascript:void(0)" class="footer-link" @click="goLogin">立即登录</a>
          </div>
        </el-form>
      </div>
    </div>
  </div>

  <!-- 用户服务协议弹窗（页面级） -->
  <el-dialog v-model="showAgreement" title="用户服务协议" width="640px" :close-on-click-modal="false">
    <div class="agreement-content">
      <h3>智能考试平台用户服务协议</h3>
      <p>欢迎使用智能考试平台（以下简称"本平台"）。请您仔细阅读以下条款：</p>

      <h4>一、总则</h4>
      <p>1.1 本协议是您与本平台之间关于使用本平台服务所订立的协议。</p>
      <p>1.2 使用本平台服务前，您应当认真阅读并遵守本协议的全部内容。</p>
      <p>1.3 您注册成为本平台用户即视为您已充分阅读、理解并接受本协议的全部内容。</p>

      <h4>二、账号注册与使用</h4>
      <p>2.1 您在注册时应提供真实、准确、完整的个人信息。</p>
      <p>2.2 您应妥善保管账号和密码，因账号密码泄露导致的一切后果由您自行承担。</p>
      <p>2.3 每个账号仅限本人使用，禁止转让、出借或出租。</p>
      <p>2.4 如发现任何未经授权使用您账号的情况，应立即通知本平台。</p>

      <h4>三、用户行为规范</h4>
      <p>3.1 您承诺遵守国家法律法规，不得利用本平台从事违法违规活动。</p>
      <p>3.2 您不得干扰或破坏本平台的正常运行。</p>
      <p>3.3 您不得以任何方式侵犯他人合法权益。</p>
      <p>3.4 在考试过程中，您应遵守考试纪律，不得作弊或协助他人作弊。</p>

      <h4>四、隐私保护</h4>
      <p>4.1 本平台重视您的隐私保护，将依法收集、使用和保护您的个人信息。</p>
      <p>4.2 未经您的明确同意，本平台不会向第三方披露您的个人信息，法律法规另有规定的除外。</p>

      <h4>五、免责声明</h4>
      <p>5.1 因不可抗力或系统维护等原因导致服务中断的，本平台不承担责任。</p>
      <p>5.2 本平台有权根据实际情况调整、暂停或终止服务。</p>

      <h4>六、协议修改</h4>
      <p>6.1 本平台有权随时修改本协议条款，修改后的协议一经发布即生效。</p>
      <p>6.2 如您不同意修改后的协议，应停止使用本平台服务。</p>

      <p class="agreement-footer">如您对以上协议内容有任何疑问，请联系平台管理员。</p>
    </div>
    <template #footer>
      <el-button @click="showAgreement = false">关闭</el-button>
      <el-button type="primary" @click="showAgreement = false; agreed = true">我已阅读并同意</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { School, User, Lock, Check, CirclePlus, UserFilled } from '@element-plus/icons-vue'
import { register } from '../api/auth'
import CaptchaSlider from '../components/CaptchaSlider.vue'

const router = useRouter()
const agreed = ref(false)
const showAgreement = ref(false)
const formRef = ref(null)

const goLogin = () => {
  router.push('/login')
}
const form = reactive({
  username: '',
  realName: '',
  password: '',
  confirmPassword: '',
  role: 1
})
const isVerified = ref(false)
const showVcode = ref(false)

// 背景题目数据
const backgroundQuestions = [
  {
    content: '计算机系统中，操作系统的主要功能是什么？',
    options: ['提供用户操作界面', '管理硬件资源和文件', '只负责显示', '处理文档编辑'],
    answer: 'B',
    type: 'choice'
  },
  {
    content: '数据结构中，具有后进先出（LIFO）特点的是______。',
    answer: '栈',
    type: 'fill'
  },
  {
    content: 'HTTP协议中，GET方法和POST方法的主要区别是什么？',
    options: ['GET请求更安全', 'POST可以发送大量数据', '两者完全相同', 'GET可修改服务器数据'],
    answer: 'B',
    type: 'choice'
  },
  {
    content: '什么是数据库的ACID特性？请简要解释。',
    answer: '原子性、一致性、隔离性、持久性',
    type: 'short'
  },
  {
    content: 'IP地址用于______标识网络中的设备。',
    answer: '唯一',
    type: 'fill'
  },
  {
    content: '以下哪种排序算法的时间复杂度是O(n log n)？',
    options: ['冒泡排序', '插入排序', '快速排序', '选择排序'],
    answer: 'C',
    type: 'choice'
  },
  {
    content: '简述软件测试中黑盒测试和白盒测试的区别。',
    answer: '黑盒不关心内部实现，白盒测试关注程序内部结构',
    type: 'short'
  },
  {
    content: '在Python中，定义函数使用的关键字是______。',
    answer: 'def',
    type: 'fill'
  },
  {
    content: 'CSS中，设置元素背景颜色的属性是？',
    options: ['color', 'background-color', 'border-color', 'font-color'],
    answer: 'B',
    type: 'choice'
  },
  {
    content: 'HTML5新增的语义化标签有哪些？请举例说明。',
    answer: 'header、nav、section、article、footer等',
    type: 'short'
  },
  {
    content: 'Git中查看提交历史的命令是______。',
    answer: 'git log',
    type: 'fill'
  },
  {
    content: 'Vue中，实现双向数据绑定的指令是？',
    options: ['v-if', 'v-for', 'v-model', 'v-on'],
    answer: 'C',
    type: 'choice'
  },
  {
    content: 'JavaScript中，数组的常用方法有哪些？',
    answer: 'push、pop、shift、unshift、splice、map、filter、reduce等',
    type: 'short'
  },
  {
    content: 'MySQL中，用于查询数据的关键字是______。',
    answer: 'SELECT',
    type: 'fill'
  },
  {
    content: '以下哪种语言是强类型语言？',
    options: ['JavaScript', 'Python', 'TypeScript', 'PHP'],
    answer: 'C',
    type: 'choice'
  },
  {
    content: '什么是RESTful API？请简要说明其特点。',
    answer: 'RESTful API是基于REST架构风格的Web API，特点包括无状态、统一接口、资源定位等',
    type: 'short'
  },
  {
    content: '在Java中，用于声明类的关键字是______。',
    answer: 'class',
    type: 'fill'
  },
  {
    content: 'React中，用于管理组件状态的Hook是？',
    options: ['useEffect', 'useState', 'useContext', 'useRef'],
    answer: 'B',
    type: 'choice'
  },
  {
    content: '简述前端性能优化的常用方法。',
    answer: '资源压缩、懒加载、CDN加速、缓存策略、代码分割、减少重排重绘等',
    type: 'short'
  },
  {
    content: 'CSS中，实现元素居中的常用方法有______。',
    answer: 'flex布局、grid布局、绝对定位+margin、transform等',
    type: 'fill'
  },
  {
    content: 'Node.js中，用于启动HTTP服务器的模块是？',
    options: ['fs', 'http', 'path', 'url'],
    answer: 'B',
    type: 'choice'
  }
]

// 每轮显示14道题
const displayQuestions = ref([])
let timer = null
let currentRound = 0
let isTransitioning = false

const shuffleArray = (array) => {
  const shuffled = [...array]
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]]
  }
  return shuffled
}

const updateQuestions = () => {
  if (isTransitioning) return
  isTransitioning = true
  
  // 先把所有题目淡出
  displayQuestions.value.forEach(q => {
    q.fadeOut = true
  })
  
  // 等待淡出动画完成后再切换
  setTimeout(() => {
    const shuffled = shuffleArray(backgroundQuestions)
    const startIdx = (currentRound * 14) % backgroundQuestions.length
    let selected = []
    for (let i = 0; i < 14; i++) {
      const question = shuffled[(startIdx + i) % backgroundQuestions.length]
      selected.push({
        ...question,
        id: `q-${currentRound}-${i}`,
        fadeOut: false // 新题目不淡出
      })
    }
    displayQuestions.value = selected
    currentRound++
    isTransitioning = false
  }, 600)
}

// 组件挂载时初始化并启动定时切换
onMounted(() => {
  updateQuestions()
  timer = setInterval(updateQuestions, 10000)
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 6, max: 15, message: '用户名长度必须在6-15位之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9]+$/, message: '用户名只能包含英文和数字', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度必须在2-20位之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度必须在6位及以上', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
    if (!form.role && form.role !== 0) {
      ElMessage.warning('请选择角色')
      return
    }
    if (!agreed.value) {
      ElMessage.warning('请阅读并同意用户服务协议')
      return
    }

    const submitData = {
      username: form.username,
      realName: form.realName,
      password: form.password,
      role: form.role
    }

    const response = await register(submitData)
    if (response.code === 200) {
      ElMessage.success('注册成功，即将跳转到登录页面')
      setTimeout(() => router.push('/login'), 800)
    } else {
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('网络异常，请检查网络连接后重试')
    }
  }
}

const handleRegisterBtnClick = () => {
  if (!form.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!form.realName.trim()) {
    ElMessage.warning('请输入真实姓名')
    return
  }
  if (!form.password.trim()) {
    ElMessage.warning('请输入密码')
    return
  }
  if (!form.confirmPassword.trim()) {
    ElMessage.warning('请再次输入密码')
    return
  }
  // 验证用户名长度
  if (form.username.length < 6 || form.username.length > 15) {
    ElMessage.warning('用户名长度必须在6-15位之间')
    return
  }
  // 验证用户名格式
  if (!/^[a-zA-Z0-9]+$/.test(form.username)) {
    ElMessage.warning('用户名只能包含英文和数字')
    return
  }
  // 验证真实姓名长度
  if (form.realName.length < 2 || form.realName.length > 20) {
    ElMessage.warning('真实姓名长度必须在2-20位之间')
    return
  }
  // 验证密码长度
  if (form.password.length < 6) {
    ElMessage.warning('密码长度必须在6位及以上')
    return
  }
  // 验证两次密码一致
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  showVcode.value = true
}

const onCaptchaSuccess = async () => {
  showVcode.value = false
  await handleRegister()
}

const onCaptchaFail = () => {
  ElMessage.warning('验证失败，请重试')
}
</script>

<style scoped>
.register-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e0e7ff 0%, #dbeafe 50%, #e8f0fe 100%);
  position: relative;
  overflow: hidden;
}

/* ── Background Questions ── */
.register-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.bg-questions {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.question-card {
  position: absolute;
  user-select: none;
  opacity: 0.6;
  transition: opacity 0.8s ease-in-out;
}

.question-card.fade-out {
  opacity: 0;
}

.q-content {
  color: rgba(30, 41, 59, 0.95);
  font-size: 14px;
  font-weight: 800;
  line-height: 1.5;
  margin-bottom: 6px;
}

.q-options {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 6px;
}

.q-option {
  display: flex;
  align-items: flex-start;
  gap: 4px;
}

.option-label {
  color: rgba(59, 130, 246, 0.9);
  font-weight: 700;
  font-size: 12px;
  flex-shrink: 0;
}

.option-text {
  color: rgba(51, 65, 85, 0.9);
  font-weight: 600;
  font-size: 12px;
  line-height: 1.4;
}

.q-answer {
  display: flex;
  align-items: center;
  gap: 4px;
}

.answer-label {
  color: rgba(71, 85, 105, 0.8);
  font-weight: 600;
  font-size: 12px;
}

.answer-value {
  color: #059669;
  font-weight: 800;
  font-size: 12px;
}

/* 随机位置和动画 */
.question-1 { top: 6%; left: 4%; width: 260px; animation: float1 15s ease-in-out infinite; }
.question-2 { top: 16%; right: 6%; width: 280px; animation: float2 18s ease-in-out infinite; }
.question-3 { top: 28%; left: 10%; width: 220px; animation: float3 14s ease-in-out infinite; }
.question-4 { top: 40%; right: 4%; width: 250px; animation: float1 20s ease-in-out infinite reverse; }
.question-5 { top: 52%; left: 6%; width: 240px; animation: float2 16s ease-in-out infinite; }
.question-6 { top: 66%; right: 8%; width: 230px; animation: float3 12s ease-in-out infinite reverse; }
.question-7 { top: 20%; left: 32%; width: 210px; animation: float1 17s ease-in-out infinite; }
.question-8 { top: 58%; right: 25%; width: 270px; animation: float2 13s ease-in-out infinite reverse; }
.question-9 { top: 10%; right: 30%; width: 200px; animation: float3 19s ease-in-out infinite; }
.question-10 { top: 78%; left: 18%; width: 245px; animation: float1 15s ease-in-out infinite reverse; }
.question-11 { top: 36%; left: 40%; width: 225px; animation: float2 22s ease-in-out infinite; }
.question-12 { top: 48%; right: 38%; width: 260px; animation: float3 16s ease-in-out infinite reverse; }
.question-13 { top: 74%; left: 40%; width: 235px; animation: float1 18s ease-in-out infinite; }
.question-14 { top: 30%; right: 45%; width: 215px; animation: float2 14s ease-in-out infinite reverse; }

@keyframes float1 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(8px, -10px) rotate(1deg); }
  50% { transform: translate(-5px, 8px) rotate(-1deg); }
  75% { transform: translate(6px, 4px) rotate(0.5deg); }
}

@keyframes float2 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(-12px, 8px) rotate(-1.5deg); }
  66% { transform: translate(8px, -6px) rotate(1deg); }
}

@keyframes float3 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  20% { transform: translate(6px, 10px) rotate(0.8deg); }
  40% { transform: translate(-10px, -5px) rotate(-0.8deg); }
  60% { transform: translate(5px, -8px) rotate(1.5deg); }
  80% { transform: translate(-6px, 6px) rotate(-0.5deg); }
}

/* ── Background Shapes ── */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.12;
}

.shape-1 {
  width: 600px;
  height: 600px;
  background: #60a5fa;
  top: -20%;
  left: -10%;
  animation: float 20s ease-in-out infinite;
}

.shape-2 {
  width: 500px;
  height: 500px;
  background: #93c5fd;
  bottom: -15%;
  right: -10%;
  animation: float 20s ease-in-out infinite;
  animation-delay: -7s;
}

.shape-3 {
  width: 350px;
  height: 350px;
  background: #3b82f6;
  top: 50%;
  right: 15%;
  animation: float 20s ease-in-out infinite;
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(40px, -30px) scale(1.05); }
  66% { transform: translate(-20px, 20px) scale(0.95); }
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(45, 55, 72, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(45, 55, 72, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

/* ── Container ── */
.register-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 520px;
  animation: cardIn 0.6s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(30px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.register-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(45, 55, 72, 0.08);
  border-radius: 24px;
  padding: 36px 36px 32px;
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.05),
    0 20px 40px -4px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.8) inset;
}

/* ── Header ── */
.register-header {
  text-align: center;
  margin-bottom: 28px;
}

.register-logo {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}

.register-logo svg {
  position: relative;
  z-index: 1;
  filter: drop-shadow(0 4px 12px rgba(59, 130, 246, 0.3));
  transition: transform 0.4s ease;
}

.register-logo:hover svg {
  transform: scale(1.05) rotate(2deg);
}

.logo-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.3) 0%, transparent 70%);
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 0.4; }
  50% { transform: translate(-50%, -50%) scale(1.2); opacity: 0.7; }
}

.register-title {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #60a5fa 0%, #93c5fd 50%, #60a5fa 100%);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: gradShift 4s ease infinite;
  letter-spacing: 2px;
  margin-bottom: 8px;
}

@keyframes gradShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.register-subtitle {
  font-size: 12px;
  color: rgba(107, 114, 128, 0.6);
  letter-spacing: 3px;
  font-weight: 300;
  text-transform: uppercase;
}

/* ── Role Section ── */
.role-section { margin-bottom: 20px; }
.role-label { display: block; font-size: 12px; color: rgba(75, 85, 99, 0.8); margin-bottom: 10px; font-weight: 500; }
.role-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }

.role-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: rgba(248, 250, 252, 0.9);
  border: 1.5px solid rgba(209, 213, 219, 0.8);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.role-card:hover {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(59, 130, 246, 0.3);
}

.role-card.active {
  background: rgba(59, 130, 246, 0.08);
  border-color: rgba(59, 130, 246, 0.5);
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.role-icon {
  width: 34px; height: 34px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.role-icon.student { background: linear-gradient(135deg, #3b82f6, #60a5fa); }
.role-icon.teacher { background: linear-gradient(135deg, #f59e0b, #f97316); }
.role-icon .el-icon { font-size: 16px; color: #fff; }

.role-body { flex: 1; min-width: 0; }
.role-name { font-size: 14px; font-weight: 600; color: rgba(45, 55, 72, 0.9); display: block; }
.role-desc { font-size: 11px; color: rgba(107, 114, 128, 0.7); margin-top: 1px; display: block; }

.role-check {
  width: 20px; height: 20px; border-radius: 50%;
  border: 2px solid rgba(209, 213, 219, 0.8);
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s ease; flex-shrink: 0;
}

.role-check.checked { background: var(--color-primary-500); border-color: var(--color-primary-500); }
.role-check .el-icon { font-size: 12px; color: #fff; }

/* ── Form ── */
.register-form { width: 100%; }

.form-group { margin-bottom: 14px; }
.form-row { display: flex; gap: 12px; }
.flex-1 { flex: 1; }

.input-wrapper {
  display: flex;
  align-items: center;
  height: 50px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(209, 213, 219, 0.8);
  border-radius: 12px;
  padding: 0 16px;
  transition: all 0.25s ease;
}

.input-wrapper:focus-within {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(59, 130, 246, 0.5);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(107, 114, 128, 0.6);
  transition: color 0.25s ease;
  flex-shrink: 0;
}

.input-wrapper:focus-within .input-icon {
  color: rgba(59, 130, 246, 0.8);
}

.input-icon .el-icon {
  font-size: 18px;
}

.custom-input {
  flex: 1;
}

.custom-input :deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
  padding: 0 !important;
}

.custom-input :deep(.el-input__inner) {
  border: none !important;
  background: transparent !important;
  height: 50px !important;
  color: rgba(45, 55, 72, 0.95) !important;
  font-size: 14px !important;
  box-shadow: none !important;
  padding: 0 !important;
}

.custom-input :deep(.el-input__inner)::placeholder {
  color: rgba(156, 163, 175, 0.8);
}

.custom-input :deep(.el-input__suffix) {
  color: rgba(107, 114, 128, 0.6);
}

/* ── Agreement ── */
.form-agreement { margin-bottom: 20px; }
.form-agreement :deep(.el-checkbox__label) { color: rgba(75, 85, 99, 0.8); font-size: 13px; }
.form-agreement :deep(.el-checkbox__inner) { background: rgba(255, 255, 255, 0.9); border-color: rgba(209, 213, 219, 0.9); }
.form-agreement :deep(.el-checkbox__input.is-checked .el-checkbox__inner) { background: var(--color-primary-500); border-color: var(--color-primary-500); }
.agree-link { color: rgba(59, 130, 246, 0.8); }
.agree-link:hover { color: rgba(96, 165, 250, 1); }

/* ── Button ── */
.vcode-wrapper {
  position: relative;
}

.register-btn {
  width: 100% !important;
  height: 50px !important;
  font-size: 16px !important;
  font-weight: 600 !important;
  border-radius: 12px !important;
  background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%) !important;
  border: none !important;
  color: #fff !important;
  transition: all 0.25s ease !important;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
  position: relative;
  overflow: hidden;
  letter-spacing: 2px;
}

.register-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, transparent 50%);
  opacity: 0;
  transition: opacity 0.25s ease;
}

.register-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 8px 28px rgba(59, 130, 246, 0.4) !important;
}

.register-btn:hover::before {
  opacity: 1;
}

.register-btn:active {
  transform: translateY(0) scale(0.98) !important;
}

.register-btn.btn-disabled {
  background: linear-gradient(135deg, #94a3b8 0%, #cbd5e1 100%) !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: not-allowed;
}

.register-btn.btn-disabled:hover {
  transform: none !important;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-icon {
  width: 18px;
  height: 18px;
}

/* ── Footer ── */
.register-footer {
  text-align: center;
  margin-top: 24px;
}

.footer-text {
  color: rgba(107, 114, 128, 0.7);
  font-size: 13px;
}

.footer-link {
  color: rgba(59, 130, 246, 0.8);
  margin-left: 4px;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.footer-link:hover {
  color: rgba(96, 165, 250, 1);
}

/* ── Responsive ── */
@media (max-width: 480px) {
  .register-card { padding: 28px 20px; border-radius: 20px; }
  .register-title { font-size: 24px; }
  .role-grid { grid-template-columns: 1fr; }
  .form-row { flex-direction: column; gap: 0; }
  .input-wrapper { height: 46px; }
  .custom-input :deep(.el-input__inner) { height: 46px !important; }
  .register-btn { height: 46px !important; }
}

/* ── 用户协议弹窗 ── */
.agree-link {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.agree-link:hover {
  color: #60a5fa;
  text-decoration: underline;
}

.agreement-content {
  max-height: 400px;
  overflow-y: auto;
  padding: 0 4px;
  line-height: 1.8;
  color: #333;
  font-size: 14px;
}

.agreement-content h3 {
  text-align: center;
  font-size: 18px;
  margin-bottom: 20px;
  color: #1a1a2e;
}

.agreement-content h4 {
  font-size: 15px;
  margin: 16px 0 8px;
  color: #2c3e50;
  border-left: 3px solid #3b82f6;
  padding-left: 10px;
}

.agreement-content p {
  margin: 6px 0;
  text-indent: 2em;
}

.agreement-content .agreement-footer {
  margin-top: 16px;
  font-weight: 500;
  color: #666;
  text-indent: 0;
  text-align: center;
}
</style>
