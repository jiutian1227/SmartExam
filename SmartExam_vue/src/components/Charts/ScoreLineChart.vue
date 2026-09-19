<template>
  <div class="chart-wrapper">
    <div ref="chartRef" class="chart-container"></div>
    <div v-if="data.length === 0" class="chart-empty">
      <el-icon :size="40" color="#d0d5dd"><TrendCharts /></el-icon>
      <span>暂无数据</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { TrendCharts } from '@element-plus/icons-vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
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

  const titles = props.data.map(item => item.title || '考试')
  const scores = props.data.map(item => item.score || 0)
  const totalScores = props.data.map(item => item.totalScore || 100)

  const option = {
    title: {
      text: '成绩趋势',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        const item = params[0]
        return `${item.name}<br/>得分：${item.value} 分`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '40%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: titles,
      axisLabel: {
        interval: 0,
        rotate: 30,
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '分数',
      max: function(value) {
        return Math.max(100, value.max + 10)
      }
    },
    series: [
      {
        name: '得分',
        type: 'line',
        data: scores,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: '#409EFF'
        },
        lineStyle: {
          width: 3,
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        markLine: {
          silent: true,
          data: [
            {
              type: 'average',
              name: '平均分',
              label: {
                formatter: '平均 {c} 分'
              }
            }
          ],
          lineStyle: {
            color: '#E6A23C',
            type: 'dashed'
          }
        }
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

watch(() => props.data, () => {
  initChart()
}, { deep: true })
</script>

<style scoped>
.chart-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.chart-container {
  width: 100%;
  height: 300px;
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