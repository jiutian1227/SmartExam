<template>
  <div class="chart-wrapper">
    <div ref="chartRef" class="chart-container"></div>
    <div v-if="completed === 0 && notTaken === 0" class="chart-empty">
      <el-icon :size="40" color="#d0d5dd"><PieChart /></el-icon>
      <span>暂无数据</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { PieChart } from '@element-plus/icons-vue'

const props = defineProps({
  completed: {
    type: Number,
    default: 0
  },
  notTaken: {
    type: Number,
    default: 0
  }
})

const chartRef = ref(null)
let chartInstance = null

const initChart = () => {
  if (!chartRef.value) return

  if (chartInstance) {
    chartInstance.dispose()
  }
  chartInstance = echarts.init(chartRef.value)

  const total = props.completed + props.notTaken
  const completionRate = total > 0 ? Math.round((props.completed / total) * 100) : 0

  const option = {
    title: {
      text: '考试完成情况',
      top: '5%',
      left: 'center',
      bottom: 25, // 标题下方预留间距，数值越大距离饼图越远
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 场 ({d}%)'
    },
    graphic: [
      {
        type: 'text',
        left: 'center',
        top: '58%',
        style: {
          text: completionRate + '%',
          textAlign: 'center',
          fill: '#303133',
          fontSize: 28,
          fontWeight: 'bold'
        }
      },
      {
        type: 'text',
        left: 'center',
        top: '68%',
        style: {
          text: '完成率',
          textAlign: 'center',
          fill: '#909399',
          fontSize: 12
        }
      }
    ],
    series: [
      {
        name: '考试状态',
        type: 'pie',
        radius: ['55%', '78%'],
        center: ['50%', '60%'], // 饼图整体向下偏移，远离顶部标题
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 3
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          fontSize: 12
        },
        data: [
          {
            value: props.completed || 0,
            name: '已完成',
            itemStyle: { color: '#67C23A' }
          },
          {
            value: props.notTaken || 0,
            name: '未完成',
            itemStyle: { color: '#EBEEF5' }
          }
        ]
      }
    ]
  }

  chartInstance.setOption(option)
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})

watch(() => [props.completed, props.notTaken], () => {
  initChart()
})
</script>

<style scoped>
.chart-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.chart-container {
  width: 100%;
  height: 330px;
}

.chart-empty {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #b0b7c3;
  font-size: 14px;
  user-select: none;
  background: #fff;
  z-index: 1;
}
</style>