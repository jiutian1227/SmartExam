<template>
  <div class="chart-wrapper">
    <div class="chart-header">
      <span class="chart-title">提交情况</span>
      <el-select 
        v-model="selectedExamId" 
        placeholder="选择考试" 
        class="exam-select"
        @change="handleExamChange"
      >
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
          <div class="stat-value submitted">{{ totalSubmitted }}</div>
          <div class="stat-label">已提交</div>
        </div>
        <div class="stat-item">
          <div class="stat-value pending">{{ totalNotSubmitted }}</div>
          <div class="stat-label">未提交</div>
        </div>
        <div class="stat-item">
          <div class="stat-value rate">{{ submitRate }}%</div>
          <div class="stat-label">提交率</div>
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
const selectedExamId = ref(null)

const examOptions = computed(() => {
  return props.data.map(item => ({
    id: item.id,
    title: item.title || '未知考试'
  }))
})

const filteredData = computed(() => {
  if (!selectedExamId.value) {
    return props.data.slice(0, 1)
  }
  return props.data.filter(item => item.id === Number(selectedExamId.value))
})

const totalSubmitted = computed(() => {
  return filteredData.value.reduce((sum, item) => sum + (item.submittedCount || 0), 0)
})

const totalGroupMembers = computed(() => {
  return filteredData.value.reduce((sum, item) => sum + (item.totalGroupMembers || 0), 0)
})

const totalNotSubmitted = computed(() => {
  return Math.max(0, totalGroupMembers.value - totalSubmitted.value)
})

const submitRate = computed(() => {
  if (totalGroupMembers.value === 0) return 0
  return ((totalSubmitted.value / totalGroupMembers.value) * 100).toFixed(1)
})

const handleExamChange = () => {
  initChart()
}

const initChart = () => {
  if (!chartRef.value) return
  
  if (chartInstance) {
    chartInstance.dispose()
  }
  chartInstance = echarts.init(chartRef.value)

  const pieData = filteredData.value.map((item, index) => {
    const submitted = item.submittedCount || 0
    const notSubmitted = (item.totalGroupMembers || 0) - submitted
    return [
      { 
        value: submitted, 
        name: `${item.title || '未知考试'}-已提交`, 
        itemStyle: { color: getColor(index, true) } 
      },
      { 
        value: notSubmitted > 0 ? notSubmitted : 0, 
        name: `${item.title || '未知考试'}-未提交`, 
        itemStyle: { color: getColor(index, false) } 
      }
    ]
  }).flat()

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}人 ({d}%)'
    },
    legend: {
      show: false
    },
    series: [
      {
        name: '提交情况',
        type: 'pie',
        radius: ['25%', '85%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 12,
            fontWeight: 'bold',
            formatter: function(params) {
              let name = params.name
              if (name.length > 10) {
                name = name.substring(0, 8) + '...'
              }
              return name + '\n' + params.value + '人'
            },
            width: 100,
            overflow: 'break'
          }
        },
        data: pieData.length > 0 ? pieData : [{ value: 1, name: '暂无数据', itemStyle: { color: '#ccc' } }]
      }
    ]
  }

  chartInstance.setOption(option)
}

const getColor = (index, isSubmitted) => {
  const colors = [
    ['#409EFF', '#B3D1FF'],
    ['#67C23A', '#B8E0B8'],
    ['#E6A23C', '#F5D9A8'],
    ['#F56C6C', '#FBC4C4'],
    ['#909399', '#D9D9D9'],
    ['#7232DD', '#C4A7E7']
  ]
  return colors[index % colors.length][isSubmitted ? 0 : 1]
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

watch(() => props.data, (newData) => {
  if (newData.length > 0 && !selectedExamId.value) {
    selectedExamId.value = newData[0].id
  }
  initChart()
}, { deep: true, immediate: true })
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