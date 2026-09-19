<template>
  <div class="exam-edit">
    <div class="page-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>{{ isEditMode ? '编辑考试' : '添加考试' }}</h2>
    </div>

    <el-card class="form-card">
      <div class="form-section">
        <h4>基本信息</h4>
        <el-form :model="form" label-width="100px" class="exam-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="考试名称">
                <el-input v-model="form.title" placeholder="请输入考试名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="考试时长">
                <el-input-number v-model="form.duration" :min="1" :max="300" />
                <span class="unit">分钟</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="开始时间">
                <el-date-picker v-model="form.startTime" type="datetime" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束时间">
                <el-date-picker v-model="form.endTime" type="datetime" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="总分">
                <el-input-number v-model="form.totalScore" :min="1" :max="1000" @change="onTotalScoreChange" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="开放用户组">
                <el-select v-model="form.selectedGroupIds" multiple placeholder="选择可参加考试的用户组" style="width: 100%">
                  <el-option
                    v-for="group in groupList"
                    :key="group.id"
                    :label="group.name"
                    :value="group.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="考试描述">
                <el-input v-model="form.description" type="textarea" :rows="2" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <el-form-item>
                <el-button type="primary" @click="handleSaveBasic">
                  {{ isEditMode ? '保存基本信息' : '保存并继续' }}
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </el-card>

    <el-card v-if="isEditMode && examId" class="question-card">
      <div class="form-section">
        <div class="section-header">
          <h4>题目管理</h4>
          <div class="section-header-right">
            <span class="score-info">已分配：{{ selectedTotalScore }} / {{ form.totalScore }} 分</span>
            <el-button type="primary" @click="batchSaveScores" :loading="savingAll">
              保存试卷
            </el-button>
          </div>
        </div>

        <el-tabs v-model="activeTab">
          <el-tab-pane label="已选题库" name="selected">
            <div class="auto-assign-section">
              <h5>按题型设置分值（设置后点击"一键分配"自动平均分配）</h5>
              <div class="type-score-row">
                <div class="type-item">
                  <el-tag size="small">单选题</el-tag>
                  <el-input-number v-model="typeConfig.single.total" :min="0" :max="100" size="small" />
                  <span class="type-unit">分</span>
                  <span class="type-count">（{{ typeCount.single }}道）</span>
                  <span class="type-per">→ 每题 {{ getPerScore(typeCount.single, typeConfig.single.total) }} 分</span>
                </div>
              </div>
              <div class="type-score-row">
                <div class="type-item">
                  <el-tag size="small" type="success">多选题</el-tag>
                  <el-input-number v-model="typeConfig.multi.total" :min="0" :max="100" size="small" />
                  <span class="type-unit">分</span>
                  <span class="type-count">（{{ typeCount.multi }}道）</span>
                  <span class="type-per">→ 每题 {{ getPerScore(typeCount.multi, typeConfig.multi.total) }} 分</span>
                </div>
              </div>
              <div class="type-score-row">
                <div class="type-item">
                  <el-tag size="small" type="warning">判断题</el-tag>
                  <el-input-number v-model="typeConfig.judge.total" :min="0" :max="100" size="small" />
                  <span class="type-unit">分</span>
                  <span class="type-count">（{{ typeCount.judge }}道）</span>
                  <span class="type-per">→ 每题 {{ getPerScore(typeCount.judge, typeConfig.judge.total) }} 分</span>
                </div>
              </div>
              <div class="type-score-row">
                <div class="type-item">
                  <el-tag size="small" type="info">填空题</el-tag>
                  <el-input-number v-model="typeConfig.blank.total" :min="0" :max="100" size="small" />
                  <span class="type-unit">分</span>
                  <span class="type-count">（{{ typeCount.blank }}道）</span>
                  <span class="type-per">→ 每题 {{ getPerScore(typeCount.blank, typeConfig.blank.total) }} 分</span>
                </div>
              </div>
              <div class="type-score-row">
                <div class="type-item">
                  <el-tag size="small" type="danger">简答题</el-tag>
                  <el-input-number v-model="typeConfig.essay.total" :min="0" :max="100" size="small" />
                  <span class="type-unit">分</span>
                  <span class="type-count">（{{ typeCount.essay }}道）</span>
                  <span class="type-per">→ 每题 {{ getPerScore(typeCount.essay, typeConfig.essay.total) }} 分</span>
                </div>
              </div>
              <div class="auto-actions">
                <el-button type="primary" @click="autoAssignScores">
                  一键分配分数
                </el-button>
                <span class="assign-hint">根据上方设置，自动分配已选题库的分数</span>
              </div>
            </div>

            <el-divider />

            <div class="selected-questions">
              <el-table :data="selectedQuestions" border max-height="300">
                <el-table-column label="序号" width="100" type="index" />
                <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
                <el-table-column label="题型" width="120">
                  <template #default="scope">
                    <el-tag size="small" :type="getTypeTagType(scope.row.type)">
                      {{ getQuestionTypeName(scope.row.type) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="分值" width="180">
                  <template #default="scope">
                    <el-input-number
                      v-model="scope.row.examScore"
                      :min="0"
                      :max="100"
                      size="small"
                      style="width: 80px"
                    />
                    <span class="score-unit">分</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button type="danger" size="small" @click="removeQuestion(scope.row)">
                      移除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="score-summary">
                <el-progress
                  :text-inside="true"
                  :stroke-width="18"
                  :percentage="scorePercentage"
                  :color="scoreColor"
                />
                <p v-if="selectedTotalScore !== form.totalScore" class="warning-text">
                  请确保总分等于 {{ form.totalScore }} 分（还差 {{ form.totalScore - selectedTotalScore }} 分）
                </p>
                <p v-else class="success-text">分值分配完成</p>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="添加题目" name="add">
            <div class="question-bank">
              <div class="bank-header">
                <div class="header-left">
                  <span>题目列表（点击添加）</span>
                  <el-select v-model="selectedType" placeholder="按题型筛选" clearable style="width: 140px; margin-left: 16px">
                    <el-option v-for="type in typeOptions" :key="type.value" :value="type.value" :label="type.label" />
                  </el-select>
                  <el-select v-model="selectedKnowledgePoint" placeholder="按知识点筛选" clearable style="width: 180px; margin-left: 16px" @change="filterQuestions">
                    <el-option v-for="kp in knowledgePointList" :key="kp.id" :value="kp.id" :label="kp.name" />
                  </el-select>
                </div>
                <span class="type-count">
                  单选 {{ typeCount.single }} | 多选 {{ typeCount.multi }} | 判断 {{ typeCount.judge }} | 填空 {{ typeCount.blank }} | 简答 {{ typeCount.essay }}
                </span>
              </div>
              <el-table :data="filteredAvailableQuestions" border height="350" @row-click="handleAddQuestion">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
                <el-table-column prop="knowledgePointName" label="知识点" width="140" />
                <el-table-column label="题型" width="120">
                  <template #default="scope">
                    <el-tag size="small" :type="getTypeTagType(scope.row.type)">
                      {{ getQuestionTypeName(scope.row.type) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button type="primary" size="small" @click.stop="handleAddQuestion(scope.row)">
                      添加
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>

    <el-card v-else-if="!isEditMode" class="hint-card">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          请先保存考试基本信息，然后再编辑题目
        </template>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
import { getExamById, createExam, updateExam, getExamQuestions, addQuestionToExam, removeQuestionFromExam, updateQuestionScore } from '../../api/exam'
import { getQuestionList } from '../../api/question'
import { getGroupList } from '../../api/group'
import { getKnowledgePointList } from '../../api/knowledgePoint'
import { getUser } from '../../utils/auth'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const examId = ref(null)
const isEditMode = ref(false)
const activeTab = ref('selected')
const questionList = ref([])
const selectedQuestions = ref([])
const groupList = ref([])
const knowledgePointList = ref([])
const selectedKnowledgePoint = ref(null)
const selectedType = ref(null)

const typeOptions = [
  { value: 0, label: '单选题' },
  { value: 1, label: '多选题' },
  { value: 2, label: '判断题' },
  { value: 3, label: '填空题' },
  { value: 4, label: '简答题' }
]

const typeConfig = ref({
  single: { total: 10 },
  multi: { total: 10 },
  judge: { total: 10 },
  blank: { total: 10 },
  essay: { total: 10 }
})

const savingAll = ref(false)
const currentUser = getUser()

const form = ref({
  id: null,
  title: '',
  description: '',
  userGroupIds: '',
  selectedGroupIds: [],
  startTime: '',
  endTime: '',
  duration: 120,
  totalScore: 100,
  creatorId: currentUser?.id || 1
})

const getPerScore = (count, total) => {
  if (count === 0) return 0
  return Math.floor(total / count)
}

const typeCount = computed(() => {
  return {
    single: selectedQuestions.value.filter(q => q.type === 0).length,
    multi: selectedQuestions.value.filter(q => q.type === 1).length,
    judge: selectedQuestions.value.filter(q => q.type === 2).length,
    blank: selectedQuestions.value.filter(q => q.type === 3).length,
    essay: selectedQuestions.value.filter(q => q.type === 4).length
  }
})

// 获取知识点名称
const getKnowledgePointName = (id) => {
  const kp = knowledgePointList.value.find(k => k.id === id)
  return kp ? kp.name : ''
}

// 过滤后的可用题目
const filteredAvailableQuestions = computed(() => {
  const selectedIds = selectedQuestions.value.map(q => q.id)
  let list = questionList.value.filter(q => !selectedIds.includes(q.id))
  
  if (selectedKnowledgePoint.value) {
    list = list.filter(q => q.knowledgePointId === selectedKnowledgePoint.value)
  }
  
  if (selectedType.value !== null && selectedType.value !== undefined) {
    list = list.filter(q => q.type === selectedType.value)
  }
  
  return list.map(q => ({
    ...q,
    knowledgePointName: getKnowledgePointName(q.knowledgePointId)
  }))
})

const filterQuestions = () => {
  // 筛选逻辑在 computed 中处理
}

const selectedTotalScore = computed(() => {
  return selectedQuestions.value.reduce((sum, q) => sum + (q.examScore || 0), 0)
})

const scorePercentage = computed(() => {
  if (!form.value.totalScore) return 0
  return Math.min(100, Math.round((selectedTotalScore.value / form.value.totalScore) * 100))
})

const scoreColor = computed(() => {
  if (selectedTotalScore.value === form.value.totalScore) {
    return '#67C23A'
  }
  return '#E6A23C'
})

const getQuestionTypeName = (type) => {
  const typeMap = { 0: '单选', 1: '多选', 2: '判断', 3: '填空', 4: '简答' }
  return typeMap[type] || '未知'
}

const getTypeTagType = (type) => {
  const typeMap = { 0: '', 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' }
  return typeMap[type] || ''
}

const onTotalScoreChange = () => {
  if (isEditMode.value) {
    ElMessage.info('总分已修改，请重新分配题目分值')
  }
}

const goBack = () => {
  router.push('/teacher/exam')
}

const loadGroups = async () => {
  try {
    const res = await getGroupList()
    if (res.code === 200) {
      groupList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载用户组列表失败', error)
  }
}

const loadKnowledgePoints = async () => {
  try {
    const user = getUser()
    const res = await getKnowledgePointList(user?.id || 1)
    if (res.code === 200) {
      knowledgePointList.value = res.data
    }
  } catch (error) {
    console.error('加载知识点失败', error)
  }
}

const loadQuestions = async () => {
  try {
    // 不分页获取所有题目，方便添加
    const res = await getQuestionList({ pageNum: 1, pageSize: 1000 })
    if (res.code === 200) {
      questionList.value = res.data?.records || []
    }
  } catch (error) {
    ElMessage.error('加载题库失败')
  }
}

const loadSelectedQuestions = async (id) => {
  try {
    const res = await getExamQuestions(id)
    if (res.code === 200) {
      selectedQuestions.value = res.data || []
    }
  } catch (error) {
    console.error('加载已选题库失败', error)
  }
}

const loadExam = async (id) => {
  try {
    const res = await getExamById(id)
    if (res.code === 200 && res.data) {
      form.value = res.data
      
      if (res.data.userGroupIds) {
        form.value.selectedGroupIds = res.data.userGroupIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id))
      } else {
        form.value.selectedGroupIds = []
      }
    }
  } catch (error) {
    ElMessage.error('加载考试信息失败')
  }
}

const handleSaveBasic = async () => {
  if (!form.value.title) {
    ElMessage.error('请填写考试名称')
    return
  }
  if (!form.value.startTime) {
    ElMessage.error('请选择开始时间')
    return
  }
  if (!form.value.endTime) {
    ElMessage.error('请选择结束时间')
    return
  }
  if (new Date(form.value.startTime) >= new Date(form.value.endTime)) {
    ElMessage.error('开始时间不能晚于或等于结束时间')
    return
  }

  form.value.userGroupIds = form.value.selectedGroupIds.join(',')

  try {
    let res
    if (isEditMode.value) {
      res = await updateExam(form.value)
      if (res.code === 200) {
        ElMessage.success('保存成功')
      }
    } else {
      res = await createExam(form.value)
      if (res.code === 200) {
        ElMessage.success('创建成功')
        examId.value = res.data?.id
        form.value.id = res.data?.id
        isEditMode.value = true
        await loadQuestions()
        await loadSelectedQuestions(res.data?.id)
      }
    }

    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleAddQuestion = async (question) => {
  if (!examId.value) {
    ElMessage.warning('请先保存考试基本信息')
    return
  }

  const exists = selectedQuestions.value.find(q => q.id === question.id)
  if (exists) {
    ElMessage.warning('该题目已添加')
    return
  }

  const perScore = getDefaultPerScore(question.type)

  try {
    const res = await addQuestionToExam(examId.value, {
      questionId: question.id,
      score: perScore,
      sortOrder: selectedQuestions.value.length
    })

    if (res.code === 200) {
      selectedQuestions.value.push({
        ...question,
        examQuestionId: res.data?.id || 0,
        examScore: perScore
      })
      ElMessage.success('添加成功')
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const getDefaultPerScore = (type) => {
  const configMap = { 0: 'single', 1: 'multi', 2: 'judge', 3: 'blank', 4: 'essay' }
  const key = configMap[type]
  if (!key) return 5
  const count = typeCount.value[key]
  const total = typeConfig.value[key].total
  if (count === 0) return Math.floor(total / 1) || 5
  return Math.floor(total / (count + 1))
}

const autoAssignScores = async () => {
  for (const question of selectedQuestions.value) {
    let count, total
    if (question.type === 0) {
      count = typeCount.value.single
      total = typeConfig.value.single.total
    } else if (question.type === 1) {
      count = typeCount.value.multi
      total = typeConfig.value.multi.total
    } else if (question.type === 2) {
      count = typeCount.value.judge
      total = typeConfig.value.judge.total
    } else if (question.type === 3) {
      count = typeCount.value.blank
      total = typeConfig.value.blank.total
    } else if (question.type === 4) {
      count = typeCount.value.essay
      total = typeConfig.value.essay.total
    } else {
      continue
    }

    const perScore = count > 0 ? Math.floor(total / count) : 0
    question.examScore = perScore
    await updateQuestionScoreFn(question)
  }
  ElMessage.success('分数已自动分配')
}

const updateQuestionScoreFn = async (question) => {
  try {
    await updateQuestionScore(examId.value, {
      examQuestionId: question.examQuestionId,
      score: question.examScore
    })
  } catch (error) {
    ElMessage.error('更新分值失败')
  }
}

const batchSaveScores = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('暂无疑目可保存')
    return
  }
  if (selectedTotalScore.value !== form.value.totalScore) {
    ElMessage.error(`已分配分数（${selectedTotalScore.value}）与总分（${form.value.totalScore}）不一致，请调整后再保存`)
    return
  }
  savingAll.value = true
  try {
    for (const question of selectedQuestions.value) {
      await updateQuestionScoreFn(question)
    }
    ElMessage.success('全部题目分数保存成功')
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    savingAll.value = false
  }
}

const removeQuestion = async (question) => {
  try {
    await removeQuestionFromExam(examId.value, question.id)
    selectedQuestions.value = selectedQuestions.value.filter(q => q.id !== question.id)
    ElMessage.success('移除成功')
  } catch (error) {
    ElMessage.error('移除失败')
  }
}

onMounted(async () => {
  await loadGroups()
  await loadKnowledgePoints()
  const id = route.params.id
  if (id) {
    examId.value = parseInt(id)
    isEditMode.value = true
    await loadExam(id)
    await loadQuestions()
    await loadSelectedQuestions(id)
  } else {
    await loadQuestions()
  }
})
</script>

<style scoped>
.exam-edit {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-4);
}

.page-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.page-header :deep(.el-button) {
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.25s ease;
}

.page-header h2 {
  margin: 0;
  font-size: var(--font-size-2xl);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-primary);
}

.form-card,
.question-card,
.hint-card {
  margin-bottom: var(--space-6);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

:deep(.el-card) {
  transition: box-shadow 0.3s ease;
}

:deep(.el-card:hover) {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

:deep(.el-card__body) {
  padding: var(--space-6);
}

.form-section {
  padding: var(--space-1);
}

.form-section h4 {
  margin: 0 0 var(--space-5) 0;
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
}

.section-header h4 {
  margin: 0;
}

.section-header-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.score-info {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-medium);
}

/* Auto-assign section */
.auto-assign-section {
  background: linear-gradient(135deg, var(--color-primary-50), var(--color-bg-elevated));
  border: 1px solid var(--color-primary-100);
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-5);
  transition: box-shadow 0.3s ease;
}

.auto-assign-section:hover {
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
}

.auto-assign-section h5 {
  margin: 0 0 var(--space-4) 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.type-score-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
  flex-wrap: wrap;
}

.type-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  background: var(--color-bg-elevated);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius);
  border: 1px solid var(--color-neutral-200);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.type-item:hover {
  border-color: var(--color-primary-300);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08);
}

.type-unit {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.type-count {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-xs);
}

.type-per {
  color: var(--color-primary-500);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
}

.auto-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px dashed var(--color-primary-200);
}

.assign-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

/* Question bank tab */
.bank-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  flex-wrap: wrap;
  gap: var(--space-3);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.header-left > span {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-primary);
}

.selected-questions {
  margin-top: var(--space-2);
}

/* Score summary */
.score-summary {
  margin-top: var(--space-5);
  padding: var(--space-5);
  background: var(--color-neutral-50);
  border: 1px solid var(--color-neutral-200);
  border-radius: var(--radius-lg);
  transition: box-shadow 0.3s ease;
}

.score-summary:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.score-summary p {
  margin: var(--space-3) 0 0 0;
  font-size: var(--font-size-sm);
}

.warning-text {
  color: var(--color-warning);
  font-weight: var(--font-weight-medium);
}

.success-text {
  color: var(--color-success);
  font-weight: var(--font-weight-medium);
}

.score-unit {
  margin-left: 4px;
  color: var(--color-text-tertiary);
  font-size: var(--font-size-xs);
}

.unit {
  margin-left: var(--space-2);
  color: var(--color-text-tertiary);
}

/* Form adjustments */
.exam-form :deep(.el-input-number) {
  width: 120px;
}

:deep(.el-form-item__label) {
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}

/* Tab styling */
:deep(.el-tabs__item) {
  font-weight: var(--font-weight-medium);
  transition: color 0.2s ease;
  font-size: var(--font-size-sm);
}

:deep(.el-tabs__item:hover) {
  color: var(--color-primary-500);
}

:deep(.el-tabs__item.is-active) {
  color: var(--color-primary-600);
  font-weight: var(--font-weight-semibold);
}

:deep(.el-tabs__active-bar) {
  background-color: var(--color-primary-500);
  height: 3px;
  border-radius: 2px;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: var(--color-neutral-200);
}

/* Table styling */
:deep(.el-table) {
  border-radius: var(--radius);
  overflow: hidden;
}

:deep(.el-table th.el-table__cell) {
  background-color: var(--color-neutral-50);
  color: var(--color-text-secondary);
  font-weight: var(--font-weight-semibold);
}

:deep(.el-table__body tr:hover > td) {
  background-color: var(--color-primary-50);
  transition: background-color 0.2s ease;
}

:deep(.el-table__body tr) {
  transition: background-color 0.2s ease;
}

/* Progress bar styling */
:deep(.el-progress-bar__outer) {
  border-radius: 12px;
  background-color: var(--color-neutral-200);
}

:deep(.el-progress-bar__inner) {
  border-radius: 12px;
  transition: width 0.4s ease, background 0.3s ease;
}

:deep(.el-progress__text) {
  font-weight: var(--font-weight-semibold);
}

/* Button transitions */
:deep(.el-button) {
  transition: all 0.25s ease;
}

/* Divider spacing */
:deep(.el-divider) {
  margin: var(--space-5) 0;
}

/* Alert styling */
.hint-card :deep(.el-alert) {
  border-radius: var(--radius);
}

/* Row click cursor for question bank */
:deep(.el-table__body-wrapper .el-table__body tr) {
  cursor: pointer;
}
</style>
