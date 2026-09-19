<template>
  <div class="question-manage">
    <div class="search-bar">
      <div class="search-left">
        <el-input v-model="searchText" placeholder="搜索题目内容">
          <template #append>
            <el-button @click="search">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>
      <div class="search-right">
        <el-select v-if="!isSuperAdmin()" v-model="selectedKnowledgePoint" placeholder="选择知识点" clearable class="knowledge-select" @change="filterByKnowledgePoint">
          <el-option v-for="kp in knowledgePointList" :key="kp.id" :value="kp.id" :label="kp.name" />
        </el-select>
        <el-button v-if="!isSuperAdmin()" type="primary" @click="showKnowledgePointModal = true">
          知识点管理
        </el-button>
        <el-button v-if="!isSuperAdmin()" type="success" @click="showAiModal = true">
          AI出题
        </el-button>
        <el-button v-if="!isSuperAdmin()" type="primary" @click="showAddModal = true">
          <el-icon><Plus /></el-icon>
          添加试题
        </el-button>
      </div>
    </div>
    
    <el-card class="table-card">
      <div v-if="questionList.length === 0" class="no-data-container">
        <div class="no-data-icon">
          <el-icon :size="60" color="#909399"><Search /></el-icon>
        </div>
        <div class="no-data-text">未搜索到相关试题</div>
        <div class="no-data-hint">请尝试更换关键词搜索</div>
      </div>
      <el-table v-else :data="questionList" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="题型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">
              {{ getTypeLabel(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" width="260" :show-overflow-tooltip="true" />
        <el-table-column v-if="!isSuperAdmin()" label="知识点" width="150">
          <template #default="scope">
            {{ getKnowledgePointName(scope.row.knowledgePointId) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="default" :icon="View" @click="viewQuestion(scope.row)">查看</el-button>
<el-button v-if="!isSuperAdmin()" size="default" type="primary" :icon="Edit" @click="editQuestion(scope.row)">编辑</el-button>
<el-button size="default" type="danger" :icon="Delete" @click="handleDeleteQuestion(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadQuestions"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-drawer v-model="showAddModal" :title="form.id ? '编辑试题' : '添加试题'" direction="rtl" size="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="题型">
          <el-select v-model="form.type" @change="onTypeChange">
            <el-option :value="0" label="单选题" />
            <el-option :value="1" label="多选题" />
            <el-option :value="2" label="判断题" />
            <el-option :value="3" label="填空题" />
            <el-option :value="4" label="简答题" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识点">
          <el-select v-model="form.knowledgePointId" placeholder="选择知识点" clearable>
            <el-option v-for="kp in knowledgePointList" :key="kp.id" :value="kp.id" :label="kp.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容">
          <el-input type="textarea" v-model="form.content" :rows="3" />
        </el-form-item>
        
        <!-- 选择题选项区域 -->
        <template v-if="form.type === 0 || form.type === 1">
          <el-form-item label="选项">
            <div class="options-list">
              <div class="option-item" v-for="(option, index) in optionList" :key="index">
                <el-checkbox v-model="option.correct" style="width: 30px">
                  <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
                </el-checkbox>
                <el-input v-model="option.value" style="flex: 1" />
                <el-button type="danger" @click="removeOption(index)" :disabled="optionList.length <= 2" size="small">
                  删除
                </el-button>
              </div>
              <el-button type="primary" style="width: 100%" @click="addOption">
                + 添加选项
              </el-button>
            </div>
          </el-form-item>
        </template>
        
        <!-- 判断题答案 -->
        <el-form-item v-if="form.type === 2" label="答案">
          <el-radio-group v-model="form.answer">
            <el-radio label="正确">正确</el-radio>
            <el-radio label="错误">错误</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 非选择题答案 -->
        <el-form-item v-else-if="form.type > 1" label="答案">
          <el-input type="textarea" v-model="form.answer" :rows="2" />
        </el-form-item>
        
        <el-form-item label="解析">
          <el-input type="textarea" v-model="form.analysis" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddModal = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-drawer>

    <!-- 查看试题详情抽屉 -->
    <el-drawer v-model="showViewModal" title="试题详情" direction="rtl" size="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ viewForm.id }}</el-descriptions-item>
        <el-descriptions-item label="题型">
          <el-tag :type="getTypeTagType(viewForm.type)">
            {{ getTypeLabel(viewForm.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="知识点">{{ getKnowledgePointName(viewForm.knowledgePointId) }}</el-descriptions-item>
        <el-descriptions-item label="题目内容">{{ viewForm.content }}</el-descriptions-item>
        
        <!-- 选择题选项 -->
        <template v-if="viewForm.type === 0 || viewForm.type === 1">
          <el-descriptions-item label="选项">
            <div class="view-options">
              <div v-for="(opt, index) in viewOptionList" :key="index" class="view-option-item">
                <span :class="['option-letter', opt.correct ? 'correct-option' : '']">
                  {{ String.fromCharCode(65 + index) }}. 
                </span>
                <span>{{ opt.value }}</span>
                <el-tag v-if="opt.correct" type="success" size="small" style="margin-left: 10px">正确答案</el-tag>
              </div>
            </div>
          </el-descriptions-item>
        </template>
        
        <el-descriptions-item label="答案">{{ viewForm.answer }}</el-descriptions-item>
        <el-descriptions-item label="解析">{{ viewForm.analysis || '无解析' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(viewForm.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showViewModal = false">关闭</el-button>
      </template>
    </el-drawer>

    <!-- AI出题弹窗 -->
    <el-dialog v-model="showAiModal" title="AI智能出题" width="800px">
      <el-form label-width="100px">
        <el-form-item label="题型">
          <el-select v-model="aiForm.type">
            <el-option :value="0" label="单选题" />
            <el-option :value="1" label="多选题" />
            <el-option :value="2" label="判断题" />
            <el-option :value="3" label="填空题" />
            <el-option :value="4" label="简答题" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目数量">
          <el-input-number v-model="aiForm.count" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="知识点" required>
          <el-select v-model="aiForm.knowledgePointId" placeholder="请选择知识点" style="width: 100%" :disabled="knowledgePointList.length === 0">
            <el-option v-for="kp in knowledgePointList" :key="kp.id" :value="kp.id" :label="kp.name" />
          </el-select>
          <span v-if="knowledgePointList.length === 0" style="color: #f56c6c; font-size: 12px;">请先添加知识点</span>
        </el-form-item>
        <el-form-item label="出题要求" required>
          <el-input 
            v-model="aiForm.requirement" 
            type="textarea" 
            :rows="3"
            placeholder="请描述您的出题要求，例如：中等难度，考察基础知识点等"
          />
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            @click="generateQuestions" 
            :loading="generating"
            :disabled="!canGenerate"
          >
            {{ generating ? '正在生成...' : '开始生成' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 生成的题目列表 -->
      <div v-if="aiQuestions.length > 0" class="ai-question-list">
        <el-divider>生成的题目（可编辑和选择）</el-divider>
        <el-table :data="aiQuestions" border max-height="400">
          <el-table-column label="选择" width="60">
            <template #default="scope">
              <el-checkbox v-model="scope.row.selected" />
            </template>
          </el-table-column>
          <el-table-column prop="type" label="题型" width="100">
            <template #default="scope">
              {{ typeLabels[scope.row.type] }}
            </template>
          </el-table-column>
          <el-table-column prop="content" label="题目内容" width="250">
            <template #default="scope">
              <el-input 
                v-model="scope.row.content" 
                type="textarea" 
                :rows="2"
                size="small"
              />
            </template>
          </el-table-column>
          <el-table-column label="选项" width="200">
            <template #default="scope">
              <div v-if="scope.row.type === 0 || scope.row.type === 1">
                <div 
                  v-for="(opt, idx) in getOptions(scope.row.options)" 
                  :key="idx"
                  class="option-preview"
                >
                  <span :class="{ 'correct-option': isCorrectOption(scope.row.answer, idx) }">
                    {{ String.fromCharCode(65 + idx) }}. {{ opt }}
                  </span>
                </div>
              </div>
              <span v-else class="no-options">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="answer" label="答案" width="100">
            <template #default="scope">
              <el-input 
                v-model="scope.row.answer" 
                size="small"
              />
            </template>
          </el-table-column>
          <el-table-column prop="analysis" label="解析" width="150">
            <template #default="scope">
              <el-input 
                v-model="scope.row.analysis" 
                type="textarea" 
                :rows="2"
                size="small"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="scope">
              <el-button type="danger" size="small" @click="removeAiQuestion(scope.$index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="showAiModal = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="addSelectedQuestions"
          :disabled="!hasSelectedQuestions"
        >
          添加选中的题目 ({{ selectedCount }})
        </el-button>
      </template>
    </el-dialog>

    <!-- 知识点管理弹窗 -->
    <el-dialog v-model="showKnowledgePointModal" title="知识点管理" width="500px">
      <div class="knowledge-point-list">
        <div class="kp-item" v-for="kp in knowledgePointList" :key="kp.id">
          <span class="kp-name">{{ kp.name }}</span>
          <span class="kp-actions">
            <el-button size="small" @click="deleteKnowledgePoint(kp.id)">删除</el-button>
          </span>
        </div>
      </div>
      <div class="add-kp" style="margin-top: 20px">
        <el-input v-model="newKnowledgePointName" placeholder="新知识点名称" style="width: 300px; margin-right: 10px" />
        <el-button type="primary" @click="addKnowledgePoint">添加</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, View, Delete } from '@element-plus/icons-vue'
import { formatDateTime } from '../../utils/format'
import { getQuestionList, createQuestion, updateQuestion, deleteQuestion, aiGenerateQuestions } from '../../api/question'
import { getKnowledgePointList, createKnowledgePoint, deleteKnowledgePoint as deleteKP } from '../../api/knowledgePoint'
import { getUser, isSuperAdmin } from '../../utils/auth'

const searchText = ref('')
const selectedKnowledgePoint = ref(null)
const showAddModal = ref(false)
const showAiModal = ref(false)
const showViewModal = ref(false)

const getCreatorId = () => {
  if (isSuperAdmin()) return null
  return getUser()?.id
}

const currentUser = getUser()
const showKnowledgePointModal = ref(false)
const questionList = ref([])
const knowledgePointList = ref([])
const optionList = ref([])
const viewOptionList = ref([])
const newKnowledgePointName = ref('')

const form = ref({
  id: null,
  type: 0,
  content: '',
  options: '',
  answer: '',
  analysis: '',
  knowledgePointId: null,
  creatorId: getCreatorId()
})

const viewForm = ref({
  id: null,
  type: 0,
  content: '',
  options: '',
  answer: '',
  analysis: '',
  knowledgePointId: null,
  createTime: ''
})

// AI出题相关
const aiForm = ref({
  type: 0,
  count: 5,
  requirement: '',
  knowledgePointId: null
})
const aiQuestions = ref([])
const generating = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const typeLabels = ['单选题', '多选题', '判断题', '填空题', '简答题']
const typeTagTypes = ['primary', 'success', 'warning', 'info', 'danger']

// AI出题相关计算属性
const selectedCount = computed(() => aiQuestions.value.filter(q => q.selected).length)
const hasSelectedQuestions = computed(() => selectedCount.value > 0)
const canGenerate = computed(() => {
  return aiForm.value.knowledgePointId && aiForm.value.requirement && aiForm.value.requirement.trim().length > 0
})

// 计算属性，根据知识点筛选题目
const filteredQuestionList = computed(() => {
  let list = questionList.value.map(q => ({
    ...q,
    knowledgePointName: getKnowledgePointName(q.knowledgePointId)
  }))
  
  if (selectedKnowledgePoint.value) {
    list = list.filter(q => q.knowledgePointId === selectedKnowledgePoint.value)
  }
  
  if (searchText.value) {
    list = list.filter(q => 
      q.content && q.content.includes(searchText.value)
    )
  }
  
  return list
})

const getTypeLabel = (type) => typeLabels[type] || '未知'
const getTypeTagType = (type) => typeTagTypes[type] || 'default'

// 获取知识点名称
const getKnowledgePointName = (id) => {
  const kp = knowledgePointList.value.find(k => k.id === id)
  return kp ? kp.name : ''
}

const search = () => {
  currentPage.value = 1
  loadQuestions()
}

const filterByKnowledgePoint = () => {
  currentPage.value = 1
  loadQuestions()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadQuestions()
}

const loadQuestions = async () => {
  try {
    const currentUser = getUser()
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      creatorId: getCreatorId()
    }
    if (searchText.value) {
      params.keyword = searchText.value
    }
    if (selectedKnowledgePoint.value) {
      params.knowledgePointId = selectedKnowledgePoint.value
    }
    
    const res = await getQuestionList(params)
    if (res.code === 200) {
      questionList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载试题列表失败')
    questionList.value = []
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

const resetForm = () => {
  form.value = {
    id: null,
    type: 0,
    content: '',
    options: '',
    answer: '',
    analysis: '',
    knowledgePointId: null,
    creatorId: getCreatorId()
  }
  optionList.value = [
    { value: '', correct: false },
    { value: '', correct: false },
    { value: '', correct: false },
    { value: '', correct: false }
  ]
}

const onTypeChange = () => {
  if (form.value.type === 0 || form.value.type === 1) {
    if (optionList.value.length === 0) {
      optionList.value = [
        { value: '', correct: false },
        { value: '', correct: false },
        { value: '', correct: false },
        { value: '', correct: false }
      ]
    }
  }
}

const addOption = () => {
  optionList.value.push({
    value: '',
    correct: false
  })
}

const removeOption = (index) => {
  optionList.value.splice(index, 1)
}

const editQuestion = (row) => {
  form.value = { ...row }
  
  // 解析选项
  if ((row.type === 0 || row.type === 1) && row.options) {
    try {
      const options = JSON.parse(row.options)
      const answers = row.answer ? row.answer.split(',') : []
      
      optionList.value = options.map((opt, idx) => ({
        value: opt,
        correct: answers.includes(String.fromCharCode(65 + idx))
      }))
    } catch (e) {
      optionList.value = [
        { value: '', correct: false },
        { value: '', correct: false },
        { value: '', correct: false },
        { value: '', correct: false }
      ]
    }
  } else {
    optionList.value = []
  }
  
  showAddModal.value = true
}

const viewQuestion = (row) => {
  viewForm.value = { ...row }
  
  // 解析选项
  if ((row.type === 0 || row.type === 1) && row.options) {
    try {
      const options = JSON.parse(row.options)
      const answers = row.answer ? row.answer.split(',') : []
      
      viewOptionList.value = options.map((opt, idx) => ({
        value: opt,
        correct: answers.includes(String.fromCharCode(65 + idx))
      }))
    } catch (e) {
      viewOptionList.value = []
    }
  } else {
    viewOptionList.value = []
  }
  
  showViewModal.value = true
}

const handleDeleteQuestion = (row) => {
  ElMessageBox.confirm(
    '此操作将永久删除该题目, 是否继续?',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteQuestion(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadQuestions()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const submitForm = async () => {
  if (!form.value.content) {
    ElMessage.error('请填写题目内容')
    return
  }
  
  // 处理选择题的选项和答案
  if (form.value.type === 0 || form.value.type === 1) {
    // 检查选项
    const validOptions = optionList.value.filter(opt => opt.value.trim())
    if (validOptions.length < 2) {
      ElMessage.error('请至少填写2个选项')
      return
    }
    
    // 检查是否选择了正确答案
    const hasCorrect = optionList.value.some(opt => opt.correct)
    if (!hasCorrect) {
      ElMessage.error('请选择至少一个正确答案')
      return
    }
    
    // 多选题检查是否选择了多个
    if (form.value.type === 0 && optionList.value.filter(opt => opt.correct).length > 1) {
      ElMessage.error('单选题只能选择一个正确答案')
      return
    }
    
    // 转换为 JSON 格式
    form.value.options = JSON.stringify(validOptions.map(opt => opt.value))
    
    // 生成答案字符串，如 A,B,C
    form.value.answer = optionList.value
      .map((opt, idx) => opt.correct ? String.fromCharCode(65 + idx) : '')
      .filter(a => a)
      .join(',')
  } else if (!form.value.answer) {
    ElMessage.error('请填写答案')
    return
  }
  
  try {
    let res
    if (form.value.id) {
      res = await updateQuestion(form.value)
    } else {
      res = await createQuestion(form.value)
    }
    
    if (res.code === 200) {
      ElMessage.success(form.value.id ? '更新成功' : '添加成功')
      showAddModal.value = false
      resetForm()
      loadQuestions()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const addKnowledgePoint = async () => {
  if (!newKnowledgePointName.value.trim()) {
    ElMessage.error('请输入知识点名称')
    return
  }
  
  try {
    const res = await createKnowledgePoint({
      name: newKnowledgePointName.value,
      creatorId: getCreatorId()
    })
    
    if (res.code === 200) {
      ElMessage.success('添加成功')
      newKnowledgePointName.value = ''
      loadKnowledgePoints()
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const deleteKnowledgePoint = (id) => {
  ElMessageBox.confirm(
    '此操作将永久删除该知识点, 是否继续?',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteKP(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadKnowledgePoints()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// AI出题辅助函数
const getOptions = (optionsStr) => {
  try {
    if (optionsStr) {
      return JSON.parse(optionsStr)
    }
  } catch (e) {
    console.error('解析选项失败', e)
  }
  return []
}

const isCorrectOption = (answer, index) => {
  if (!answer) return false
  const optionLabel = String.fromCharCode(65 + index)
  return answer.split(',').includes(optionLabel)
}

// 统一答案格式，确保只保留字母
const normalizeAnswer = (answer, options) => {
  if (!answer) return ''
  
  let normalized = ''
  
  if (options && options.length > 0) {
    const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H']
    const answerUpper = answer.toUpperCase()
    
    for (let i = 0; i < options.length; i++) {
      const label = optionLabels[i]
      if (answerUpper.includes(label)) {
        normalized += (normalized ? ',' : '') + label
      }
    }
  } else {
    normalized = answer
  }
  
  return normalized || answer
}

// AI出题相关方法
const resetAiForm = () => {
  aiForm.value = {
    type: 0,
    count: 5,
    requirement: '',
    knowledgePointId: null
  }
  aiQuestions.value = []
}

const generateQuestions = async () => {
  if (!aiForm.value.count || aiForm.value.count < 1) {
    ElMessage.warning('请输入题目数量')
    return
  }

  generating.value = true
  try {
    const res = await aiGenerateQuestions(aiForm.value)
    if (res.code === 200) {
      aiQuestions.value = res.data.map(q => {
        const options = getOptions(q.options)
        const normalizedAnswer = normalizeAnswer(q.answer, options)
        return {
          ...q,
          selected: true,
          answer: normalizedAnswer
        }
      })
      ElMessage.success(`成功生成 ${aiQuestions.value.length} 道题目`)
    } else {
      ElMessage.error(res.message || '生成题目失败')
    }
  } catch (error) {
    console.error('生成题目失败', error)
    ElMessage.error('生成题目失败，请确保Ollama服务已启动并运行gemma3:4b模型')
  } finally {
    generating.value = false
  }
}

const removeAiQuestion = (index) => {
  aiQuestions.value.splice(index, 1)
}

const addSelectedQuestions = async () => {
  const selected = aiQuestions.value.filter(q => q.selected)
  if (selected.length === 0) {
    ElMessage.warning('请先选择要添加的题目')
    return
  }

  let successCount = 0
  for (const q of selected) {
    try {
      const questionData = {
        type: q.type,
        content: q.content,
        answer: q.answer,
        analysis: q.analysis || '',
        knowledgePointId: q.knowledgePointId || aiForm.value.knowledgePointId,
        options: q.options || '',
        creatorId: getCreatorId()
      }
      
      const res = await createQuestion(questionData)
      if (res.code === 200) {
        successCount++
      }
    } catch (error) {
      console.error('添加题目失败', error)
    }
  }

  if (successCount > 0) {
    ElMessage.success(`成功添加 ${successCount} 道题目`)
    showAiModal.value = false
    resetAiForm()
    loadQuestions()
  } else {
    ElMessage.error('添加题目失败')
  }
}

// 监听AI弹窗关闭事件
watch(showAiModal, (val) => {
  if (!val) {
    resetAiForm()
  }
})

onMounted(async () => {
  resetForm()
  await loadKnowledgePoints()
  await loadQuestions()
})
</script>

<style scoped>
.question-manage {
  width: 100%;
  padding: 24px;
  background: var(--color-bg, #f0f2f5);
  min-height: 100%;
  box-sizing: border-box;
}

/* ========== Search Bar ========== */
.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: var(--color-white);
  padding: 16px 24px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  transition: var(--transition);
}

.search-bar:hover {
  box-shadow: var(--shadow-lg);
}

.search-left {
  flex: 1;
  max-width: 420px;
}

.search-left .el-input {
  width: 280px;
}

.search-left :deep(.el-input) {
  --el-input-border-radius: var(--radius-lg);
  --el-input-height: 36px;
}

/* ── Search Box & Button Merge Effect ─────────────────────── */
.search-left :deep(.el-input__wrapper) {
  border-radius: var(--radius-base) 0 0 var(--radius-base) !important;
  border-right: none !important;
  box-shadow: 0 0 0 1px var(--color-border) inset;
  transition: var(--transition);
}
.search-left :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary) inset !important;
}
.search-left :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset !important;
  z-index: 1;
}
.search-left :deep(.el-input-group__append) {
  background: var(--color-primary) !important;
  border: none !important;
  border-radius: 0 var(--radius-base) var(--radius-base) 0 !important;
  padding: 0 !important;
}
.search-left :deep(.el-input-group__append .el-button) {
  background: transparent !important;
  border: none !important;
  color: var(--color-white) !important;
  padding: 0 16px !important;
  height: 100% !important;
  margin: 0 !important;
  border-radius: 0 var(--radius-base) var(--radius-base) 0 !important;
  transition: var(--transition);
}
.search-left :deep(.el-input-group__append .el-button:hover) {
  background: rgba(255, 255, 255, 0.15) !important;
}
.search-left :deep(.el-input-group__append .el-button .el-icon) {
  font-size: 18px;
}

/* ========== Search Right ========== */
.search-right {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.search-right .knowledge-select {
  width: 200px;
}

.search-right .el-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: var(--radius-sm);
  font-weight: 500;
  transition: var(--transition);
}

.search-right .el-button--primary {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  border: none;
  box-shadow: 0 2px 8px rgba(26, 143, 232, 0.30);
}

.search-right .el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(26, 143, 232, 0.40);
}

.search-right .el-button--primary:active {
  transform: translateY(0);
}

.search-right .el-button .el-icon {
  font-size: 16px;
}

.no-data-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.no-data-icon {
  margin-bottom: 16px;
  opacity: 0.6;
}

.no-data-text {
  font-size: var(--font-size-lg);
  color: var(--color-text-secondary);
  margin-bottom: 8px;
  font-weight: var(--font-weight-medium);
}

.no-data-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
}

/* ── Options List (Drawer) ────────────────────────────── */
.options-list {
  width: 100%;
}

.option-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--color-bg-subtle);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-neutral-200);
  transition: all var(--transition-fast);
}
.option-item:hover {
  border-color: var(--color-primary-200);
  background: var(--color-primary-50);
  box-shadow: var(--shadow-xs);
}

.option-label {
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-600);
  min-width: 24px;
  font-size: var(--font-size-sm);
}

/* ── Knowledge Point Management ───────────────────────── */
.knowledge-point-list {
  max-height: 320px;
  overflow-y: auto;
  border: 1px solid var(--color-neutral-200);
  border-radius: var(--radius-md);
  background: var(--color-bg-elevated);
}

.kp-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--color-neutral-100);
  transition: all var(--transition-fast);
}
.kp-item:hover {
  background: var(--color-primary-50);
}
.kp-item:last-child {
  border-bottom: none;
}

.kp-name {
  font-size: var(--font-size-sm);
  color: var(--color-text-primary);
  font-weight: var(--font-weight-medium);
}

.kp-actions {
  display: flex;
  gap: var(--space-2);
}

.add-kp {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
}

/* ── AI Question Section ──────────────────────────────── */
.ai-question-list {
  margin-top: var(--space-6);
}

.option-preview {
  font-size: var(--font-size-xs);
  margin-bottom: 4px;
  padding: 2px 0;
}
.option-preview .correct-option {
  color: var(--color-success);
  font-weight: var(--font-weight-semibold);
}

.no-options {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-xs);
  font-style: italic;
}

/* ── View Options ───────────────────────────────────── */
.view-options {
  width: 100%;
}

.view-option-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed var(--color-neutral-200);
}

.view-option-item:last-child {
  border-bottom: none;
}

.option-letter {
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-600);
  margin-right: 8px;
  font-size: var(--font-size-sm);
}

.option-letter.correct-option {
  color: var(--color-success);
}

/* ── Deep Overrides ───────────────────────────────────── */
:deep(.el-form-item) {
  margin-bottom: var(--space-5);
}

:deep(.el-drawer__body) {
  padding: var(--space-6);
}

:deep(.el-drawer__footer) {
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--color-neutral-200);
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

:deep(.el-dialog__body) {
  padding: var(--space-6);
}

:deep(.el-divider) {
  margin: var(--space-6) 0;
}

:deep(.el-divider__text) {
  font-weight: var(--font-weight-semibold);
  color: var(--color-primary-600);
  background: var(--color-bg-elevated);
  padding: 0 var(--space-4);
}
</style>
