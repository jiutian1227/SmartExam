<template>
  <div v-if="visible" class="captcha-overlay" @click="close">
    <div class="captcha-modal" @click.stop>
      <div class="captcha-header">
        <span class="captcha-title">滑块验证</span>
        <button type="button" class="close-btn" @click="close">×</button>
      </div>
      
      <div class="captcha-body">
        <div class="image-container">
          <canvas
            ref="canvasRef"
            class="captcha-canvas"
            :width="300"
            :height="150"
          ></canvas>
          <canvas
            ref="sliderCanvasRef"
            class="slider-canvas"
            :width="shapeSize"
            :height="shapeSize"
            :style="sliderCanvasStyle"
          ></canvas>
          <button type="button" class="refresh-btn" @click="loadCaptcha" title="换一张">
            ↻
          </button>
        </div>

        <div class="slider-track">
          <div
            class="slider-bar"
            :class="{ done: verified }"
            :style="{ width: sliderX + 'px' }"
          ></div>
          <div
            class="slider-handle"
            :class="{ done: verified }"
            :style="{ left: sliderX + 'px' }"
            @mousedown="startDrag"
            @touchstart="startDrag"
        >
            <el-icon class="handle-icon" v-if="!verified"><ArrowRight /></el-icon>
            <span class="handle-check" v-else>✓</span>
        </div>
          <span class="track-hint" v-if="sliderX === 0">{{ displayCode || '验证' }}</span>
        </div>

        <div
          class="captcha-tip"
          :class="{ success: verified, fail: verifyFailed }"
        >{{ tipText }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import request from '../utils/request'

const props = defineProps({
  visible: Boolean
})

const emit = defineEmits(['success', 'fail', 'close'])

const shapes = [
  { name: 'circle', draw: (ctx, x, y, r) => { ctx.arc(x, y, r, 0, Math.PI * 2); } },
  { name: 'square', draw: (ctx, x, y, r) => { ctx.rect(x - r, y - r, r * 2, r * 2); } },
  { name: 'triangle', draw: (ctx, x, y, r) => { ctx.moveTo(x, y - r); ctx.lineTo(x + r, y + r); ctx.lineTo(x - r, y + r); ctx.closePath(); } },
  { name: 'heart', draw: drawHeart },
  { name: 'star', draw: drawStar },
  { name: 'diamond', draw: (ctx, x, y, r) => { ctx.moveTo(x, y - r); ctx.lineTo(x + r, y); ctx.lineTo(x, y + r); ctx.lineTo(x - r, y); ctx.closePath(); } }
]

function drawHeart(ctx, x, y, r) {
  ctx.moveTo(x, y);
  ctx.bezierCurveTo(x - r, y - r, x - r * 1.5, y, x, y + r);
  ctx.bezierCurveTo(x + r * 1.5, y, x + r, y - r, x, y);
}

function drawStar(ctx, x, y, r) {
  const spikes = 5;
  const outerRadius = r;
  const innerRadius = r / 2;
  let rot = Math.PI / 2 * 3;
  let x1, y1;
  const step = Math.PI / spikes;

  ctx.moveTo(x, y - outerRadius);
  for (let i = 0; i < spikes; i++) {
    x1 = x + Math.cos(rot) * outerRadius;
    y1 = y + Math.sin(rot) * outerRadius;
    ctx.lineTo(x1, y1);
    rot += step;

    x1 = x + Math.cos(rot) * innerRadius;
    y1 = y + Math.sin(rot) * innerRadius;
    ctx.lineTo(x1, y1);
    rot += step;
  }
  ctx.lineTo(x, y - outerRadius);
  ctx.closePath();
}

const token = ref('')
const imageUrl = ref('')
const gapX = ref(100)
const sliderX = ref(0)
const isDragging = ref(false)
const verified = ref(false)
const verifyFailed = ref(false)
const tipText = ref('请完成滑块验证')
const displayCode = ref('')
const currentShape = ref(null)
const canvasRef = ref(null)
const sliderCanvasRef = ref(null)
const imgObj = ref(null)

let startX = 0
let containerWidth = 300
const shapeSize = ref(45 + Math.floor(Math.random() * 21))  // 45-65px
const gapY = ref((150 - shapeSize.value) / 2)

const sliderCanvasStyle = computed(() => ({
  left: sliderX.value + 'px',
  top: gapY.value + 'px',
  width: shapeSize.value + 'px',
  height: shapeSize.value + 'px'
}))

const drawCaptcha = () => {
  const canvas = canvasRef.value
  const sliderCanvas = sliderCanvasRef.value
  
  if (!canvas || !sliderCanvas || !currentShape.value || !imgObj.value) return
  
  const ctx = canvas.getContext('2d')
  const sliderCtx = sliderCanvas.getContext('2d')
  
  const size = shapeSize.value
  const y = gapY.value
  
  ctx.clearRect(0, 0, 300, 150)
  sliderCtx.clearRect(0, 0, size, size)
  
  const img = imgObj.value
  const scaleX = 300 / img.width
  const scaleY = 150 / img.height
  const scale = Math.min(scaleX, scaleY)
  
  const w = img.width * scale
  const h = img.height * scale
  const offsetX = (300 - w) / 2
  const offsetY = (150 - h) / 2
  
  ctx.drawImage(img, offsetX, offsetY, w, h)
  
  const centerX = gapX.value + size / 2
  const centerY = y + size / 2
  const radius = size / 2 - 2
  
  ctx.fillStyle = 'rgba(0, 0, 0, 0.7)'
  ctx.beginPath()
  currentShape.value.draw(ctx, centerX, centerY, radius)
  ctx.fill()
  
  const srcX = (centerX - radius - 2 - offsetX) / scale
  const srcY = (centerY - radius - 2 - offsetY) / scale
  const srcSize = (radius * 2 + 4) / scale
  
  sliderCtx.save()
  sliderCtx.beginPath()
  currentShape.value.draw(sliderCtx, size / 2, size / 2, radius)
  sliderCtx.clip()
  
  sliderCtx.drawImage(img, srcX, srcY, srcSize, srcSize, 0, 0, size, size)
  
  sliderCtx.restore()
  
  sliderCtx.strokeStyle = '#fff'
  sliderCtx.lineWidth = 1
  sliderCtx.beginPath()
  currentShape.value.draw(sliderCtx, size / 2, size / 2, radius)
  sliderCtx.stroke()
}

const loadImage = (url) => {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      imgObj.value = img
      drawCaptcha()
      resolve(img)
    }
    img.onerror = reject
    img.src = url
  })
}

const loadCaptcha = async () => {
  verified.value = false
  verifyFailed.value = false
  try {
    const res = await request.post('/api/captcha/generate', {})
    if (res.code === 200) {
      token.value = res.data.token
      imageUrl.value = '/captcha-images/' + res.data.imageUrl.split('/').pop()
      gapX.value = res.data.gapX
      sliderX.value = 0
      
      const shapeIndex = res.data.shapeIndex || Math.floor(Math.random() * shapes.length)
      currentShape.value = shapes[shapeIndex]

      tipText.value = '请完成滑块验证'
      displayCode.value = token.value.substring(0, 4).toUpperCase() + '-' + token.value.substring(4, 8).toUpperCase()

      await loadImage(imageUrl.value)
    }
  } catch (err) {
    tipText.value = '加载验证码失败'
      currentShape.value = shapes[Math.floor(Math.random() * shapes.length)]
      displayCode.value = 'FALL-BACK'
      // 确保gapX不会超出边界：最小10px，最大(300 - shapeSize - 10)px
      const maxX = 300 - shapeSize.value - 10
      gapX.value = Math.floor(Math.random() * (maxX - 10)) + 10
      drawCaptchaFallback()
  }
}

const drawCaptchaFallback = () => {
  const canvas = canvasRef.value
  const sliderCanvas = sliderCanvasRef.value
  
  if (!canvas || !sliderCanvas || !currentShape.value) return
  
  const ctx = canvas.getContext('2d')
  const sliderCtx = sliderCanvas.getContext('2d')
  
  ctx.clearRect(0, 0, 300, 150)
  sliderCtx.clearRect(0, 0, shapeSize, shapeSize)
  
  ctx.fillStyle = '#e8e8e8'
  ctx.fillRect(0, 0, 300, 150)
  
  ctx.fillStyle = '#999'
  for (let i = 0; i < 300; i += 20) {
    for (let j = 0; j < 150; j += 20) {
      ctx.fillRect(i, j, 10, 10)
    }
  }
  
  const centerX = gapX.value + shapeSize / 2
  const centerY = gapY + shapeSize / 2
  const radius = shapeSize / 2 - 2
  
  ctx.fillStyle = '#666'
  ctx.beginPath()
  currentShape.value.draw(ctx, centerX, centerY, radius)
  ctx.fill()
  
  sliderCtx.save()
  sliderCtx.beginPath()
  currentShape.value.draw(sliderCtx, shapeSize / 2, shapeSize / 2, radius)
  sliderCtx.clip()
  
  sliderCtx.drawImage(canvas, gapX.value, gapY, shapeSize, shapeSize, 0, 0, shapeSize, shapeSize)
  
  sliderCtx.restore()
  
  sliderCtx.strokeStyle = '#3b82f6'
  sliderCtx.lineWidth = 2
  sliderCtx.beginPath()
  currentShape.value.draw(sliderCtx, shapeSize / 2, shapeSize / 2, radius)
  sliderCtx.stroke()
}

const startDrag = (e) => {
  isDragging.value = true
  startX = e.clientX || e.touches?.[0]?.clientX || 0
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', stopDrag)
}

const onDrag = (e) => {
  if (!isDragging.value) return
  const currentX = e.clientX || e.touches?.[0]?.clientX || 0
  const deltaX = currentX - startX
  sliderX.value = Math.max(0, Math.min(containerWidth - shapeSize.value, deltaX))
}

const stopDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
  
  if (sliderX.value > 0) {
    verifyCaptcha()
  }
}

const verifyCaptcha = async () => {
  tipText.value = '验证中...'
  try {
    const res = await request.post('/api/captcha/verify', {
      token: token.value,
      userX: sliderX.value
    })
    if (res.code === 200 && res.data) {
      verified.value = true
      verifyFailed.value = false
      tipText.value = '✓ 验证成功'
      setTimeout(() => {
        emit('success')
        close()
      }, 600)
    } else {
      verifyFailed.value = true
      tipText.value = '✗ 验证失败，请重试'
      setTimeout(() => {
        sliderX.value = 0
        verifyFailed.value = false
        loadCaptcha()
        emit('fail')
      }, 600)
    }
  } catch (err) {
    verifyFailed.value = true
    tipText.value = '✗ 验证失败，请重试'
    setTimeout(() => {
      sliderX.value = 0
      verifyFailed.value = false
      loadCaptcha()
      emit('fail')
    }, 600)
  }
}

const close = () => {
  emit('close')
}

watch(() => props.visible, (val) => {
  if (val) {
    loadCaptcha()
  }
})

onMounted(() => {
  if (props.visible) {
    loadCaptcha()
  }
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
})
</script>

<style scoped>
.captcha-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
}

.captcha-modal {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  width: 348px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalIn 0.25s ease-out;
}

@keyframes modalIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.captcha-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.captcha-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a2e;
  letter-spacing: 0.5px;
}

.close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
  color: #999;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #ffebee;
  color: #e53935;
}

.image-container {
  position: relative;
  width: 300px;
  height: 150px;
  margin: 0 auto;
  border-radius: 10px;
  overflow: hidden;
  background: #f5f5f5;
  box-shadow: inset 0 1px 4px rgba(0, 0, 0, 0.08);
}

.captcha-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.slider-canvas {
  position: absolute;
  pointer-events: none;
  z-index: 10;
}

/* ===== 滑块拖动条 全新样式 ===== */
.slider-track {
  position: relative;
  width: 300px;
  height: 44px;
  margin: 18px auto 12px;
  background: linear-gradient(135deg, #e8ecf1, #f0f2f5);
  border-radius: 22px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.06);
  cursor: default;
  user-select: none;
}

.slider-bar {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 0;
  transition: background 0.3s ease;
  box-shadow: inset 0 1px 2px rgba(255, 255, 255, 0.2);
}

/* 拖到最右成功时变色 */
.slider-bar.done {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.slider-handle {
  position: absolute;
  top: 4px;
  width: 36px;
  height: 36px;
  background: #fff;
  border-radius: 6px;
  box-shadow:
    0 2px 8px rgba(102, 126, 234, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.8);
  cursor: grab;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
  z-index: 2;
  user-select: none;
  touch-action: none;
}

.slider-handle:hover {
  box-shadow:
    0 4px 16px rgba(102, 126, 234, 0.4),
    0 0 0 2px rgba(255, 255, 255, 0.9);
  transform: scale(1.05);
}

.slider-handle:active {
  cursor: grabbing;
  transform: scale(1.15);
  box-shadow:
    0 6px 24px rgba(102, 126, 234, 0.5),
    0 0 0 2px rgba(255, 255, 255, 0.9);
}

/* 成功时把手变绿色调 */
.slider-handle.done {
  box-shadow:
    0 2px 12px rgba(67, 233, 123, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.8);
}

.handle-icon {
  font-size: 18px;
  color: #667eea;
  font-weight: bold;
  transition: color 0.3s ease;
}

/* 验证成功时图标变勾 */
.slider-handle.done .handle-icon {
  color: #43e97b;
}

.handle-check {
  font-size: 20px;
  color: #43e97b;
  font-weight: bold;
  line-height: 1;
}

/* 轨道上的文字提示 */
.track-hint {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  font-size: 13px;
  color: #999;
  letter-spacing: 0.5px;
  pointer-events: none;
  z-index: 1;
  transition: opacity 0.3s;
}

.captcha-tip {
  text-align: center;
  font-size: 13px;
  color: #888;
  margin-top: 4px;
  min-height: 20px;
  transition: color 0.3s;
}

.captcha-tip.success {
  color: #43e97b;
  font-weight: 500;
}

.captcha-tip.fail {
  color: #e53935;
  font-weight: 500;
}

/* 刷新按钮（在图片右下角） */
.refresh-btn {
  position: absolute;
  right: 8px;
  bottom: 8px;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #666;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  transition: all 0.2s;
  z-index: 20;
}

.refresh-btn:hover {
  background: #fff;
  color: #667eea;
  transform: rotate(90deg);
}
</style>
