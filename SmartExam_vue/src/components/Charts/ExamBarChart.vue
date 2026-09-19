<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

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
  
  chartInstance = echarts.init(chartRef.value)
  
  const titles = props.data.map(item => item.title || '未知考试').slice(0, 6)
  const submitted = props.data.map(item => item.submittedCount || 0).slice(0, 6)
  const graded = props.data.map(item => item.gradedCount || 0).slice(0, 6)
  const ungraded = props.data.map(item => item.ungradedCount || 0).slice(0, 6)

  const option = {
    title: {
      text: '各考试提交与批阅统计',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['已提交', '已批阅', '未批阅'],
      bottom: '5%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: titles,
      axisLabel: {
        interval: 0,
        rotate: 30,
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      name: '人数'
    },
    series: [
      {
        name: '已提交',
        type: 'bar',
        data: submitted,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '已批阅',
        type: 'bar',
        data: graded,
        itemStyle: {
          color: '#67C23A'
        }
      },
      {
        name: '未批阅',
        type: 'bar',
        data: ungraded,
        itemStyle: {
          color: '#E6A23C'
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
.chart-container {
  width: 100%;
  height: 300px;
}
</style>