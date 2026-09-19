<template>
  <div class="chart-wrapper">
    <div class="chart-header">
      <span class="chart-title">批阅进度</span>
      <el-select 
        v-model="selectedExamId" 
        placeholder="选择考试" 
        class="exam-select"
        @change="handleExamChange"
      >
        <el-option :value="''" label="全部考试" />
        <el-option 
          v-for="exam in examOptions" 
          :key="exam.id" 
          :value="exam.id" 
          :label="exam.title" 
        />
      </el-select>
    </div>
    <div class="chart-content">
      <div ref="chartRef" class="chart-container"></div>
      <div class="stats-row">
        <div class="stat-item">
          <div class="stat-value submitted">{{ totalGraded }}</div>
          <div class="stat-label">已批阅</div>
        </div>
        <div class="stat-item">
          <div class="stat-value pending">{{ totalUngraded }}</div>
          <div class="stat-label">待批阅</div>
        </div>
        <div class="stat-item">
          <div class="stat-value rate">{{ progress }}%</div>
          <div class="stat-label">完成率</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  }
})

const chartRef = ref(null)
let chartInstance = null
const selectedExamId = ref('')

const examOptions = computed(() => {
  return props.data.map(item => ({
    id: item.id,
    title: item.title || '未知考试'
  }))
})

const filteredData = computed(() => {
  if (!selectedExamId.value) {
    return props.data
  }
  return props.data.filter(item => item.id === Number(selectedExamId.value))
})

const handleExamChange = () => {
  initChart()
}

const totalSubmitted = computed(() => {
  return filteredData.value.reduce((sum, item) => sum + (item.submittedCount || 0), 0)
})

const totalGraded = computed(() => {
  return filteredData.value.reduce((sum, item) => sum + (item.gradedCount || 0), 0)
})

const totalUngraded = computed(() => {
  return filteredData.value.reduce((sum, item) => sum + (item.ungradedCount || 0), 0)
})

const progress = computed(() => {
  return totalSubmitted.value > 0 
    ? ((totalGraded.value / totalSubmitted.value) * 100).toFixed(1) 
    : 0
})

const initChart = () => {
  if (!chartRef.value) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  chartInstance = echarts.init(chartRef.value)

  const option = {
    series: [
      {
        name: '批阅进度',
        type: 'gauge',
        radius: '95%',
        center: ['50%', '58%'],
        startAngle: 190,
        endAngle: -10,
        min: 0,
        max: 100,
        splitNumber: 10,
        axisLine: {
          lineStyle: {
            width: 28,
            color: [
              [0.3, '#E6A23C'],
              [0.7, '#67C23A'],
              [1, '#409EFF']
            ]
          }
        },
        pointer: {
          itemStyle: {
            color: '#409EFF'
          },
          length: '75%',
          width: 8
        },
        axisTick: {
          distance: -28,
          length: 10,
          lineStyle: {
            color: '#999',
            width: 2
          }
        },
        splitLine: {
          distance: -38,
          length: 28,
          lineStyle: {
            color: '#999',
            width: 4
          }
        },
        axisLabel: {
          color: '#666',
          distance: 38,
          fontSize: 12
        },
        detail: {
          valueAnimation: true,
          formatter: '{value}%',
          color: '#333',
          fontSize: 18,
          fontWeight: 'bold'
        },
        data: [{ value: parseFloat(progress.value), name: '完成率' }]
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

watch([() => props.data, selectedExamId], () => {
  initChart()
}, { deep: true })
</script>

<style scoped>
.chart-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: var(--space-4);
  box-sizing: border-box;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-neutral-200);
  margin-bottom: var(--space-3);
}

.chart-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.exam-select { width: 180px; }

.chart-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chart-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stats-row {
  display: flex;
  justify-content: center;
  gap: 70px;
  padding: var(--space-4) 0 var(--space-2);
  margin-top: var(--space-2);
  border-top: 1px dashed var(--color-neutral-300);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 28px;
  font-weight: var(--font-weight-bold);
}

.stat-value.submitted { color: var(--color-success); }
.stat-value.pending { color: var(--color-warning); }
.stat-value.rate { color: var(--color-primary-500); }

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  margin-top: var(--space-1);
}
</style>