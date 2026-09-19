<template>
  <!-- 悬浮按钮 -->
  <div
    class="ai-float-btn"
    :style="{ left: btnPosition.x + 'px', top: btnPosition.y + 'px' }"
    @mousedown="startDragBtn"
    @click="handleBtnClick"
    v-show="!showDialog"
  >
    <div class="ai-float-btn-inner">
      <img :src="aiAvatar" alt="AI助手" class="ai-float-avatar" />
    </div>
    <!-- 第一次点击的提示气泡 -->
    <div class="ai-first-click-tip" v-if="showFirstClickTip">
      <div class="tip-content">
        再点一次开启AI助手
      </div>
      <div class="tip-arrow"></div>
    </div>
  </div>

  <!-- 对话框 -->
  <div
    class="ai-dialog"
    :style="{ left: dialogPosition.x + 'px', top: dialogPosition.y + 'px' }"
    v-show="showDialog"
  >
    <!-- 头部 -->
    <div class="ai-dialog-header" @mousedown="startDragDialog">
      <div class="header-left">
        <img :src="assistantAvatar" alt="" class="header-avatar" />
        <div class="header-info">
          <span class="header-title">智能助手</span>
          <span class="header-status">在线</span>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          :icon="MoreFilled"
          circle
          text
          @click.stop="showMoreOptions"
        />
        <el-button
          :icon="Close"
          circle
          text
          @click.stop="closeDialog"
        />
      </div>
    </div>

    <!-- 消息区域 -->
    <div class="ai-messages-wrapper" ref="messagesWrapper">
      <div class="ai-messages" ref="messagesContainer">
        <!-- 加载历史消息 -->
        <div class="load-more" v-if="loadingHistory">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载历史消息...</span>
        </div>

        <!-- 消息列表 -->
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="message-item"
          :class="{ 'message-self': msg.type === 'user' }"
        >
          <!-- 对方消息 -->
          <div class="message-left" v-if="msg.type === 'ai'">
            <img :src="assistantAvatar" alt="" class="message-avatar" />
            <div class="message-bubble-wrapper">
              <div class="message-time">{{ msg.time }}</div>
              <div class="message-content ai-message">
                <div v-if="msg.isTyping" class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
                <div v-else v-html="formatContent(msg.content)"></div>
              </div>
            </div>
          </div>

          <!-- 我的消息 -->
          <div class="message-right" v-else>
            <div class="message-bubble-wrapper">
              <div class="message-time">{{ msg.time }}</div>
              <div class="message-content user-message">
                {{ msg.content }}
              </div>
            </div>
          </div>
        </div>

        <!-- 加载占位 -->
        <!-- <div class="message-item" v-if="isLoading && messages.length > 0">
          <div class="message-left">
            <img :src="assistantAvatar" alt="" class="message-avatar" />
            <div class="message-bubble-wrapper">
              <div class="message-content ai-message">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
        </div> -->
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="ai-input-area">
      <div class="input-main">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="3"
          resize="none"
          :placeholder="'请输入您的问题...'"
          @keydown.enter.ctrl="sendMessage"
          @keydown.enter.exact.prevent
        />
      </div>
      <div class="input-footer">
        <span class="input-tip">按 Ctrl+Enter 发送</span>
        <el-button
          type="primary"
          :disabled="!inputText.trim() || isLoading"
          :loading="isLoading"
          @click="sendMessage"
          class="send-btn"
        >
          <el-icon v-if="!isLoading"><Promotion /></el-icon>
          <span>{{ isLoading ? '发送中' : '发送' }}</span>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { MoreFilled, Close, Promotion, Loading } from '@element-plus/icons-vue'

// 助手头像
const assistantAvatar = '/at.png'
const aiAvatar = '/AI.png'
// 显示状态
const showDialog = ref(false)

// 第一次点击提示
const showFirstClickTip = ref(false)
let tipTimer = null

// 按钮位置（右下角）
const btnPosition = ref({
  x: window.innerWidth - 180,
  y: window.innerHeight - 180
})

// 对话框位置
const dialogPosition = ref({
  x: window.innerWidth - 480,
  y: window.innerHeight - 600
})

// 消息列表
const messages = ref([])

// 输入内容
const inputText = ref('')

// 加载状态
const isLoading = ref(false)

// 加载历史消息状态
const loadingHistory = ref(false)

// 消息容器引用
const messagesContainer = ref(null)
const messagesWrapper = ref(null)

// 拖动状态
const isDraggingBtn = ref(false)
const isDraggingDialog = ref(false)
const dragOffset = ref({ x: 0, y: 0 })

// 格式化时间
const formatTime = (date) => {
  const d = date || new Date()
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// 初始化欢迎消息
const initMessages = () => {
  messages.value = [
    {
      type: 'ai',
      content: '您好，我是智能助手。有什么可以帮您的吗？',
      time: formatTime(),
      isTyping: false
    }
  ]
}

// 处理按钮点击（双击确认）
const handleBtnClick = () => {
  if (isDraggingBtn.value) return

  if (showFirstClickTip.value) {
    clearTimeout(tipTimer)
    showFirstClickTip.value = false
    showDialog.value = true
    if (messages.value.length === 0) {
      initMessages()
    }
  } else {
    showFirstClickTip.value = true
    tipTimer = setTimeout(() => {
      showFirstClickTip.value = false
    },1000)
  }
}

// 打开对话框
const openDialog = () => {
  showDialog.value = true
  if (messages.value.length === 0) {
    initMessages()
  }
}

// 关闭对话框
const closeDialog = () => {
  showDialog.value = false
}

// 显示更多选项
const showMoreOptions = () => {
  // 可以扩展清空聊天等选项
}

// 格式化消息内容（Markdown → HTML 原生渲染）
const formatContent = (content) => {
  if (!content) return ''
  let html = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // 表格（必须在其他转换之前，避免干扰）
  html = html.replace(/^\|(.+)\|$/gm, (m) => {
    const cells = m.slice(1, -1).split('|').map(c => c.trim())
    return '<tr><td>' + cells.join('</td><td>') + '</td></tr>'
  })
  html = html.replace(/(<tr>.*<\/tr>\n?)+/g, '<table><tbody>$&</tbody></table>')
  // 表头：如果第一行后跟分割行 ---
  html = html.replace(/<table><tbody>(<tr><td>[^<]+<\/td>(?:<\/tr>)?)\n?<tr><td>[-:]+\|?(?:[-:| ]+)<\/td>(?:<\/tr>)?\n?(.*?)<\/tbody><\/table>/g,
    '<table><thead><tr>$1</tr></thead><tbody>$2</tbody></table>'.replace(/<\/?td>/g, m => m === '</td>' ? '</th>' : '<th>'))

  // **bold** (必须先于 *italic* 处理)
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  // *italic*
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
  // `code`
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')
  // ## heading
  html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>')
  html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>')
  // - list
  html = html.replace(/^- (.+)$/gm, '<li>$1</li>')
  html = html.replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')
  // 换行
  html = html.replace(/\n/g, '<br>')
  // 【关键词】高亮
  html = html.replace(/【(.+?)】/g, '<span class="highlight">$1</span>')
  return html
}

// 滚动到底部
const scrollToBottom = (smooth = true) => {
  nextTick(() => {
    if (messagesWrapper.value) {
      messagesWrapper.value.scrollTo({
        top: messagesWrapper.value.scrollHeight,
        behavior: smooth ? 'smooth' : 'auto'
      })
    }
  })
}

// 加载历史消息（模拟）
const loadHistory = async () => {
  loadingHistory.value = true
  // 模拟加载延迟
  await new Promise(resolve => setTimeout(resolve, 1000))
  loadingHistory.value = false
}

// 发送消息
const sendMessage = async () => {
  if (!inputText.value.trim() || isLoading.value) return

  const question = inputText.value.trim()

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: question,
    time: formatTime(),
    isTyping: false
  })

  inputText.value = ''

  // 滚动到底部
  scrollToBottom()

  isLoading.value = true

  // 添加AI消息占位
  const aiMessageIndex = messages.value.push({
    type: 'ai',
    content: '',
    time: formatTime(),
    isTyping: true
  }) - 1

  try {
    // 30秒超时
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 30000)

    const response = await fetch('/api/ai-assistant/ask-stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({ question: question }),
      signal: controller.signal
    })

    clearTimeout(timeoutId)

    if (!response.ok) {
      throw new Error('网络请求失败')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()

      if (done) break

      buffer += decoder.decode(value, { stream: true })

      while (true) {
        const eventEnd = buffer.indexOf('\n\n')
        if (eventEnd === -1) break

        const eventStr = buffer.substring(0, eventEnd)
        buffer = buffer.substring(eventEnd + 2)

        if (!eventStr.trim()) continue

        const eventLines = eventStr.split('\n')
        let eventName = 'message'
        let eventData = ''

        for (const line of eventLines) {
          if (line.startsWith('event:')) {
            eventName = line.substring(6).trim()
          } else if (line.startsWith('data:')) {
            eventData = line.substring(5).trim()
          }
        }

        if (eventName === 'message' && eventData) {
          messages.value[aiMessageIndex].content += eventData
          messages.value[aiMessageIndex].isTyping = false
          scrollToBottom()
        } else if (eventName === 'complete') {
          messages.value[aiMessageIndex].isTyping = false
          break
        } else if (eventName === 'error') {
          messages.value[aiMessageIndex].content = eventData || '服务异常，请稍后重试'
          messages.value[aiMessageIndex].isTyping = false
          break
        }
      }
    }

    await reader.closed

  } catch (error) {
    if (error.name === 'AbortError') {
      messages.value[aiMessageIndex].content = '请求超时，请稍后重试'
    } else {
      messages.value[aiMessageIndex].content = '网络连接出现问题，请检查网络后重试'
    }
    messages.value[aiMessageIndex].isTyping = false
    console.error('AI助手错误:', error)
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

// 按钮拖动开始
const startDragBtn = (e) => {
  isDraggingBtn.value = true
  dragOffset.value = {
    x: e.clientX - btnPosition.value.x,
    y: e.clientY - btnPosition.value.y
  }
  e.preventDefault()
}

// 对话框拖动开始
const startDragDialog = (e) => {
  isDraggingDialog.value = true
  dragOffset.value = {
    x: e.clientX - dialogPosition.value.x,
    y: e.clientY - dialogPosition.value.y
  }
  e.preventDefault()
}

// 拖动移动
const handleDrag = (e) => {
  if (isDraggingBtn.value) {
    btnPosition.value = {
      x: Math.max(0, Math.min(window.innerWidth - 150, e.clientX - dragOffset.value.x)),
      y: Math.max(0, Math.min(window.innerHeight - 150, e.clientY - dragOffset.value.y))
    }
  }
  if (isDraggingDialog.value) {
    dialogPosition.value = {
      x: Math.max(0, Math.min(window.innerWidth - 450, e.clientX - dragOffset.value.x)),
      y: Math.max(0, Math.min(window.innerHeight - 550, e.clientY - dragOffset.value.y))
    }
  }
}

// 拖动结束
const endDrag = () => {
  isDraggingBtn.value = false
  isDraggingDialog.value = false
}

// 监听消息变化，滚动到底部
watch(messages, () => {
  scrollToBottom()
}, { deep: true })

// 挂载和卸载事件监听
onMounted(() => {
  window.addEventListener('mousemove', handleDrag)
  window.addEventListener('mouseup', endDrag)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleDrag)
  window.removeEventListener('mouseup', endDrag)
  if (tipTimer) {
    clearTimeout(tipTimer)
  }
})
</script>

<style scoped>
/* 悬浮按钮 */
.ai-float-btn {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 150px;
  height: 150px;
  background: transparent;
  border: none;
  cursor: pointer;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
  user-select: none;
}

.ai-float-btn:hover {
  transform: scale(1.1);
}

.ai-float-btn-inner {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-float-avatar {
  width: 150px;
  height: 150px;
  object-fit: contain;
}

/* 第一次点击提示气泡 */
.ai-first-click-tip {
  position: absolute;
  bottom: calc(100% - 20px);
  left: 50%;
  transform: translateX(-50%);
  z-index: 10001;
  animation: tipFadeIn 0.3s ease;
}

.tip-content {
  background: #303133;
  color: #fff;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 13px;
  white-space: nowrap;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.tip-arrow {
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 6px solid #303133;
}

@keyframes tipFadeIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

/* 对话框 */
.ai-dialog {
  position: fixed;
  width: 420px;
  height: 560px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.12), 0 0 1px rgba(0, 0, 0, 0.08);
  z-index: 10000;
  display: flex;
  flex-direction: column;
  user-select: none;
  overflow: hidden;
}

/* 头部 */
.ai-dialog-header {
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  cursor: move;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  object-fit: contain;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.header-status {
  font-size: 12px;
  color: #67c23a;
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-status::before {
  content: '';
  width: 6px;
  height: 6px;
  background: #67c23a;
  border-radius: 50%;
}

.header-actions {
  display: flex;
  gap: 4px;
}

/* 消息区域 */
.ai-messages-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background: #f5f7fa;
}

.ai-messages-wrapper::-webkit-scrollbar {
  width: 6px;
}

.ai-messages-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.ai-messages-wrapper::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.ai-messages-wrapper::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.ai-messages {
  padding: 16px;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.load-more {
  text-align: center;
  padding: 12px;
  color: #909399;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 消息项 */
.message-item {
  display: flex;
  width: 100%;
}

.message-item.message-self {
  justify-content: flex-end;
}

.message-left {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 80%;
}

.message-right {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  max-width: 80%;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  object-fit: contain;
  flex-shrink: 0;
}

.message-bubble-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-time {
  font-size: 11px;
  color: #c0c4cc;
  padding: 0 4px;
}

/* .message-left .message-time {
  margin-left: 42px;
} */

.message-right .message-time {
  text-align: right;
  /* margin-right: 42px; */
}

.message-content {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.ai-message {
  background: #fff;
  color: #303133;
  border: 1px solid #ebeef5;
  border-radius: 8px 8px 8px 2px;
}

.user-message {
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  color: #fff;
  border-radius: 8px 8px 2px 8px;
}

/* Markdown 格式（:deep穿透v-html） */
.ai-message :deep(h1),
.ai-message :deep(h2),
.ai-message :deep(h3),
.ai-message :deep(h4) {
  margin: 0.8em 0 0.4em;
  font-weight: 600;
  color: #303133;
}
.ai-message :deep(h2) {
  font-size: 15px;
  padding-bottom: 4px;
  border-bottom: 1px solid #ebeef5;
}
.ai-message :deep(h3) { font-size: 14px; }
.ai-message :deep(p) { margin: 0 0 0.6em; }
.ai-message :deep(p:last-child) { margin-bottom: 0; }
.ai-message :deep(ul),
.ai-message :deep(ol) {
  padding-left: 1.4em;
  margin-bottom: 0.6em;
}
.ai-message :deep(li) { margin-bottom: 0.2em; }
.ai-message :deep(strong) { font-weight: 600; }
.ai-message :deep(em) { font-style: italic; }
.ai-message :deep(code) {
  background: #f0f2f5;
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 0.9em;
  color: #d56161;
}
.ai-message :deep(pre) {
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 10px 12px;
  overflow-x: auto;
  margin-bottom: 0.6em;
}
.ai-message :deep(pre code) {
  background: none;
  padding: 0;
  color: inherit;
  font-size: 13px;
}
.ai-message :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 0.6em;
  font-size: 13px;
}
.ai-message :deep(th),
.ai-message :deep(td) {
  border: 1px solid #dcdfe6;
  padding: 5px 8px;
  text-align: left;
}
.ai-message :deep(th) {
  background: #f0f2f5;
  font-weight: 600;
}
.ai-message :deep(tr:nth-child(even)) {
  background: #fafafa;
}
.ai-message :deep(blockquote) {
  border-left: 3px solid #409eff;
  margin: 0.6em 0;
  padding: 4px 12px;
  color: #606266;
  background: #f0f7ff;
  border-radius: 0 4px 4px 0;
}
.ai-message :deep(a) {
  color: #409eff;
  text-decoration: none;
}
.ai-message :deep(a:hover) {
  text-decoration: underline;
}
.ai-message :deep(hr) {
  border: none;
  border-top: 1px solid #ebeef5;
  margin: 0.8em 0;
}

/* 高亮 */
.highlight {
  color: #409eff;
  font-weight: 500;
}

/* 加载动画 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  background: #909399;
  border-radius: 50%;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingBounce {
  0%, 80%, 100% {
    transform: scale(1);
    opacity: 0.4;
  }
  40% {
    transform: scale(1.2);
    opacity: 1;
  }
}

/* 输入区域 */
.ai-input-area {
  background: #fff;
  border-top: 1px solid #ebeef5;
  padding: 12px 16px;
  flex-shrink: 0;
}

.input-main {
  margin-bottom: 8px;
}

.input-main :deep(.el-textarea__inner) {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
}

.input-main :deep(.el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

.input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.input-tip {
  font-size: 12px;
  color: #909399;
}

.send-btn {
  min-width: 80px;
  border-radius: 6px;
}

/* 响应式 */
@media screen and (max-width: 768px) {
  .ai-dialog {
    width: calc(100vw - 32px);
    height: calc(100vh - 120px);
    left: 16px !important;
    right: 16px;
    bottom: 80px;
    border-radius: 12px;
  }

  .ai-float-btn {
    right: 16px;
    bottom: 16px;
  }

  .ai-float-btn-inner,
  .ai-float-avatar {
    width: 100px;
    height: 100px;
  }

  .message-left,
  .message-right {
    max-width: 90%;
  }
}

@media screen and (max-width: 480px) {
  .ai-dialog {
    width: 100%;
    height: 70vh;
    left: 0 !important;
    right: 0;
    bottom: 0;
    border-radius: 16px 16px 0 0;
  }

  .ai-float-btn {
    right: 12px;
    bottom: 12px;
  }

  .ai-float-btn-inner,
  .ai-float-avatar {
    width: 80px;
    height: 80px;
  }

  .header-actions {
    display: none;
  }
}
</style>
