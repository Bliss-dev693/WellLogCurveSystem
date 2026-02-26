<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessageBox, ElMessage, ElLoading } from 'element-plus'
import { useUserStore } from '@/stores'
// 导入接口服务（请确保该文件路径正确）
import { drillingPredictService } from '@/api/drilling'
import { useRouter } from 'vue-router'

const router = useRouter()
const step = ref(1)
const maxSteps = 5

const form = reactive({
  wellName: '',
  statisticName: '',
  timeSteps: Array.from({ length: 5 }).map(() => ({
    depth: '',
    AC: '',
    CAL: '',
    DEN: '',
    GR: '',
    RT: '',
    RXO: ''
  }))
})

const currentStepIndex = computed(() => step.value - 1)

const parameterRanges = {
  "AC": { min: 33, max: 180, unit: "μs/ft", required: true },
  "CAL": { min: 0, max: Infinity, unit: "inch", required: false },
  "CNL": { min: -5, max: 50, unit: "%", required: false },
  "DEN": { min: 1.5, max: 3.01, unit: "g/cm³", required: false },
  "GR": { min: 3, max: Infinity, unit: "API", required: true },
  "RT": { min: 1, max: Infinity, unit: "Ω·m", required: true },
  "RXO": { min: 1, max: Infinity, unit: "Ω·m", required: true }
}



// 存储预测结果
const predictResult = ref(null)
// 加载状态
const loading = ref(false)

const requiredParams = ['AC', 'GR', 'RT', 'RXO']

// 扩展验证规则，增加井深验证
const validateField = (param, value) => {
  if (param === 'depth') {
    if (value === '' || value === null || value === undefined) {
      return { valid: false, message: '请输入井深值' }
    }
    
    const numValue = Number(value)
    if (isNaN(numValue)) {
      return { valid: false, message: '井深必须是数值' }
    }
    
    if (numValue <= 0) {
      return { valid: false, message: '井深值必须大于0米' }
    }
    
    return { valid: true, message: '' }
  }
  
  if (value === '' || value === null || value === undefined) {
    return { valid: false, message: `请输入${getFullParamName(param)}的值` }
  }
  
  const numValue = Number(value)
  const range = parameterRanges[param]
  
  if (isNaN(numValue)) {
    return { valid: false, message: `${getFullParamName(param)}必须是数值` }
  }
  
  if (numValue < range.min) {
    return { valid: false, message: `${getFullParamName(param)}值不能小于${range.min}${range.unit}` }
  }
  
  if (numValue > range.max && range.max !== Infinity) {
    return { valid: false, message: `${getFullParamName(param)}值不能大于${range.max}${range.unit}` }
  }
  
  return { valid: true, message: '' }
}

const isFormValid = computed(() => {
  if (!form.wellName || !form.statisticName) {
    return false
  }
  
  const currentStepData = form.timeSteps[currentStepIndex.value]
  if (!validateField('depth', currentStepData.depth).valid) {
    return false
  }

  for (const param of requiredParams) {
    const validation = validateField(param, currentStepData[param])
    if (!validation.valid) {
      return false
    }
  }
  
  return true
})

// 计算深度范围（适配接口要求，取第一个时间步的井深）
const getDepthRange = () => {
  const firstDepth = form.timeSteps[0].depth
  return firstDepth ? Number(firstDepth).toString() : ''
}

// 格式化请求数据（完全匹配接口要求的嵌套结构）
const formatRequestData = () => {
  return form.timeSteps.map(item => ({
    data: {
      parameters: {
        AC: Number(item.AC),
        GR: Number(item.GR),
        RT: Number(item.RT),
        RXO: Number(item.RXO)
      }
    }
  }))
}

const userId = ref(null)
onMounted(() => {
  const userStore = useUserStore()
  // 确保用户已登录且存在userId
  if (userStore.user && userStore.user.id) {
    userId.value = Number(userStore.user.id)
    
  } else {
    // 如果用户未登录，可以从其他途径获取或提示登录
    console.error('用户未登录或缺少用户ID')
    
    ElMessage.error('请先登录系统')
    router.push('/login') // 跳转到登录页
  }
})
// 调用预测接口（核心修复：兼容后端响应格式）
const callPredictApi = async () => {
  try {
    loading.value = true
    predictResult.value = null
    

    // 1. 准备请求参数
    const depthRange = getDepthRange()
    if (!depthRange) {
      ElMessage.error('无法计算深度范围，请检查井深数据')
      return
    }
    
    const requestData = formatRequestData()
 
    
    // 2. 调用接口
    const response = await drillingPredictService({
      userId: userId.value,
      datasetName: form.statisticName,
      wellName: form.wellName,
      depthRange: depthRange,
      requestData: requestData
    })
    
   
    
    // 3. 兼容处理后端响应（核心修复）
    // 情况1：后端返回标准格式 {code:0, message:"xxx", data:数值}
    if (response.code === 0 || response.code === '0') {
      predictResult.value = response.data
      ElMessage.success(`钻井工程预测完成，预测结果：${response.data}`)
      
      // 展示预测结果弹窗
      ElMessageBox.alert(
        `<div style="text-align:center;">
          <h3>钻井工程预测结果</h3>
          <p style="font-size:24px;color:#409eff;margin:20px 0;">${response.data}</p>
          <p>预测完成时间：${new Date().toLocaleString()}</p>
        </div>`,
        '预测结果',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '确认',
          type: 'success'
        }
      )
    } 
    // 情况2：后端返回自定义成功标识（如status:success）
    else if (response.status === 'success' || response.data.message?.includes('成功')) {
      // 提取预测结果（适配后端可能的字段名）
      const result = response.predictResult || response.data.data || response.result
      if (result !== undefined && result !== null) {
        predictResult.value = result
        ElMessage.success(`钻井工程预测完成，预测结果：${result}`)
        
        ElMessageBox.alert(
          `<div style="text-align:center;">
            <h3>钻井工程预测结果</h3>
            <p style="font-size:24px;color:#409eff;margin:20px 0;">${result}</p>
            <p>预测完成时间：${new Date().toLocaleString()}</p>
          </div>`,
          '预测结果',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '确认',
            type: 'success'
          }
        )
      } else {
        // 有成功标识但无结果
        ElMessage.warning('预测成功，但未返回具体结果')
      }
    } 
    // 其他情况（后端返回错误）
    else {
      ElMessage.error(`预测失败：${response.data.message || '未知错误'}`)
    }
    
  } catch (error) {
    console.error('调用预测接口异常:', error)
    // 优化异常处理：即使进入catch，也检查是否有后端返回的成功数据
    if (error.response) {
     
      // 后端返回了数据但axios认为是错误（如非200状态码但实际成功）
      const resData = error.response.data
      if (resData.status === 'success' || resData.data.message?.includes('成功')) {
        const result = resData.predictResult || resData.data.data || resData.result
        if (result) {
          predictResult.value = result
          ElMessage.success(`钻井工程预测完成，预测结果：${result}`)
          // 展示弹窗
          ElMessageBox.alert(
            `<div style="text-align:center;">
              <h3>钻井工程预测结果</h3>
              <p style="font-size:24px;color:#409eff;margin:20px 0;">${result}</p>
              <p>预测完成时间：${new Date().toLocaleString()}</p>
            </div>`,
            '预测结果',
            {
              dangerouslyUseHTMLString: true,
              confirmButtonText: '确认',
              type: 'success'
            }
          )
          return
        }
      }
      // 真正的错误
      const errMsg = resData.message || '接口返回错误'
      ElMessage.error(`接口调用失败：${errMsg}`)
    } else {
      // 网络错误等
      ElMessage.error(`接口调用失败：${error.message || '网络异常'}`)
    }
  } finally {
    loading.value = false
  }
}

function prev() {
  if (step.value > 1) {
    step.value--
  }
}

function next() {
  if (!isFormValid.value) {
    ElMessage.warning('请先填写当前时间步的所有必填参数并确保格式正确')
    return
  }
  
  if (step.value < maxSteps) {
    step.value++
  } else {
    submitAllData()
  }
}

// 提交当前时间步数据
function submitCurrentStep() {
  if (!isFormValid.value) {
    ElMessage.warning('请先填写当前时间步的所有必填参数并确保格式正确')
    return
  }
  
  ElMessage.success(`第${step.value}时间步数据提交成功`)
  console.log(`提交第${step.value}时间步数据:`, form.timeSteps[currentStepIndex.value])
  
  if (step.value < maxSteps) {
    step.value++
  }
}

// 提交所有数据并调用预测接口
function submitAllData() {
  // 数据验证
  if (!form.wellName || !form.statisticName) {
    ElMessage.error('请填写井名和数据集名称')
    return
  }
  
  for (let i = 0; i < maxSteps; i++) {
    const stepData = form.timeSteps[i]
    
    // 验证井深
    const depthValidation = validateField('depth', stepData.depth)
    if (!depthValidation.valid) {
      ElMessage.error(`时间步 ${i+1}: ${depthValidation.message}`)
      return
    }
    
    // 验证必填参数
    for (const param of requiredParams) {
      const validation = validateField(param, stepData[param])
      if (!validation.valid) {
        ElMessage.error(`时间步 ${i+1}: ${validation.message}`)
        return
      }
    }
  }
  
  // 所有数据有效，调用预测接口
  ElMessageBox.confirm(
    '所有数据验证通过，是否确认提交并进行钻井工程预测？',
    '确认预测',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(() => {
    callPredictApi()
  })
}

function resetForm() {
  ElMessageBox.confirm(
    '确定要重置表单吗？所有已输入的数据将被清空。',
    '确认重置',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    form.wellName = ''
    form.statisticName = ''
    form.timeSteps = Array.from({ length: 5 }).map(() => ({
      depth: '',
      AC: '',
      CAL: '',
      DEN: '',
      GR: '',
      RT: '',
      RXO: ''
    }))
    step.value = 1
    predictResult.value = null // 重置预测结果
    ElMessage.success('表单已重置')
  })
}

function saveDraft() {
  const draftData = {
    step: step.value,
    form: JSON.parse(JSON.stringify(form)),
    predictResult: predictResult.value
  }
  localStorage.setItem('manual_input_draft', JSON.stringify(draftData))
  ElMessage.success('已保存草稿到本地')
}

function loadDraft() {
  const draft = localStorage.getItem('manual_input_draft')
  if (draft) {
    try {
      const draftData = JSON.parse(draft)
      step.value = draftData.step
      form.wellName = draftData.form.wellName
      form.statisticName = draftData.form.statisticName
      form.timeSteps = JSON.parse(JSON.stringify(draftData.form.timeSteps))
      predictResult.value = draftData.predictResult
      ElMessage.success('已从本地加载草稿')
    } catch (e) {
      console.error('加载草稿失败:', e)
      ElMessage.error('草稿数据格式错误，无法加载')
    }
  } else {
    ElMessage.info('没有找到本地草稿')
  }
}

function getFullParamName(param) {
  const names = {
    "AC": "声波时差",
    "CAL": "井径",
    "CNL": "补偿中子",
    "DEN": "密度",
    "GR": "自然伽马",
    "RT": "电阻率",
    "RXO": "冲洗带电阻率"
  }
  return names[param] || param
}

function getParamMeaning(param) {
  const meanings = {
    "AC": "声波在地层中传播单位距离所需时间，反映地层孔隙度",
    "CAL": "井眼直径，反映井壁坍塌或缩径情况",
    "CNL": "中子孔隙度，反映地层含氢量（与孔隙度相关）",
    "DEN": "地层体积密度，反映岩石骨架和孔隙流体的综合密度",
    "GR": "地层天然放射性强度，反映泥质含量",
    "RT": "地层未被泥浆侵入的原状地层电阻率，反映含油气性",
    "RXO": "井周泥浆侵入带电阻率，反映侵入程度"
  }
  return meanings[param] || ""
}

// 定义参数显示顺序，确保两行三列的布局
const paramDisplayOrder = [
  ['AC', 'CAL', 'DEN'],
  ['GR', 'RT', 'RXO']
]

onMounted(() => {
  loadDraft()
  // 绑定快捷键 Ctrl + Enter 提交当前步骤
  document.addEventListener('keydown', (e) => {
    if (e.ctrlKey && e.key === 'Enter') {
      submitCurrentStep()
    }
  })
})
</script>

<template>
  <el-card class="main-card">
    <template #header>
      <div class="card-header">
        <span class="header-title"> 手动录入</span>
        <div class="header-actions">
          <el-button @click="saveDraft" type="default" icon="el-icon-save">保存草稿</el-button>
          <el-button @click="resetForm" type="danger" icon="el-icon-refresh">重置表单</el-button>
        </div>
      </div>
    </template>

    <!-- 基本信息 -->
    <el-form label-position="top" class="basic-info-form">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="input-card">
            <el-form-item label="井名 *" class="form-item">
              <el-input 
                v-model="form.wellName" 
                placeholder="请输入井名（如：Well-A1"
                :disabled="step > 1"
                class="form-input"
              />
            </el-form-item>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="input-card">
            <el-form-item label="数据集 *" class="form-item">
              <el-input 
                v-model="form.statisticName" 
                placeholder="请输入数据集名称（如：Dataset-2026-1）"
                :disabled="step > 1"
                class="form-input"
              />
            </el-form-item>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="input-card">
            <el-form-item label="井深(m) *" class="form-item">
              <el-input 
                v-model="form.timeSteps[currentStepIndex].depth" 
                type="number" 
                step="0.125"
                placeholder="请输入井深数值"
                class="form-input"
                :class="{ 'input-error': !validateField('depth', form.timeSteps[currentStepIndex].depth).valid }"
              />
              <div v-if="!validateField('depth', form.timeSteps[currentStepIndex].depth).valid" class="error-tip">
                {{ validateField('depth', form.timeSteps[currentStepIndex].depth).message }}
              </div>
            </el-form-item>
          </div>
        </el-col>
      </el-row>
    </el-form>

    <!-- 时间步导航 -->
    <el-steps :active="step" finish-status="success" align-center class="steps-container">
      <el-step 
        v-for="n in maxSteps" 
        :key="n" 
        :title="`时间步 ${n}`" 
        class="step-item"
      />
    </el-steps>

    <!-- 参数输入区域 - 按照图片样式重新布局 -->
    <div class="params-container">
      <!-- 第一行参数：AC, CAL, DEN -->
      <el-row :gutter="16" class="param-row">
        <el-col :span="8" v-for="param in paramDisplayOrder[0]" :key="param" class="param-col">
          <div class="param-input-card">
            <span class="param-label">
              {{ param }} ({{ getFullParamName(param) }})
              <span class="required-mark" v-if="parameterRanges[param].required">*</span>
            </span>
            <el-input 
              v-model="form.timeSteps[currentStepIndex][param]" 
              type="number"
              :placeholder="`请输入${getFullParamName(param)}值`"
              class="param-input"
              :class="{ 'input-error': !validateField(param, form.timeSteps[currentStepIndex][param]).valid && parameterRanges[param].required }"
            />
            <div 
              v-if="!validateField(param, form.timeSteps[currentStepIndex][param]).valid && parameterRanges[param].required" 
              class="error-tip"
            >
              {{ validateField(param, form.timeSteps[currentStepIndex][param]).message }}
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 第二行参数：GR, RT, RXO -->
      <el-row :gutter="16" class="param-row">
        <el-col :span="8" v-for="param in paramDisplayOrder[1]" :key="param" class="param-col">
          <div class="param-input-card">
            <span class="param-label">
              {{ param }} ({{ getFullParamName(param) }})
              <span class="required-mark" v-if="parameterRanges[param].required">*</span>
            </span>
            <el-input 
              v-model="form.timeSteps[currentStepIndex][param]" 
              type="number"
              :placeholder="`请输入${getFullParamName(param)}值`"
              class="param-input"
              :class="{ 'input-error': !validateField(param, form.timeSteps[currentStepIndex][param]).valid && parameterRanges[param].required }"
            />
            <div 
              v-if="!validateField(param, form.timeSteps[currentStepIndex][param]).valid && parameterRanges[param].required" 
              class="error-tip"
            >
              {{ validateField(param, form.timeSteps[currentStepIndex][param]).message }}
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- CNL理论值展示区域 -->
      <!-- <div class="cnl-theory-card">
        {{ cnlTheoreticalValue }}
      </div> -->

      <!-- 提交当前时间步按钮 -->
      <div class="submit-btn-container">
        <el-button 
          type="primary" 
          @click="submitCurrentStep"
          :disabled="!isFormValid"
          class="submit-step-btn"
        >
          提交当前时间步数据
        </el-button>
        <div class="shortcut-tip">快捷键: Ctrl + Enter 提交数据</div>
      </div>
    </div>

    <!-- 底部导航按钮 -->
    <div class="nav-bar">
      <el-button 
        :disabled="step === 1" 
        @click="prev"
        icon="el-icon-arrow-left"
        class="nav-btn"
      >
        上一步
      </el-button>
      <!-- 添加加载状态 -->
      <el-button 
        type="primary" 
        @click="submitAllData"
        :disabled="!isFormValid || step < maxSteps || loading"
        icon="el-icon-check"
        class="nav-btn submit-all-btn"
        :loading="loading"
      >
        提交所有数据并预测
      </el-button>
    </div>

    <!-- 预测结果展示区域 -->
    <div v-if="predictResult" class="result-card">
      <el-card 
        shadow="hover" 
        class="predict-result-card"
        header="钻井工程预测结果"
      >
        <div class="result-content">
          <span class="result-label">预测值：</span>
          <span class="result-value">{{ predictResult }}</span>
          <span class="result-tip">（结果已保存，可重置表单重新预测）</span>
        </div>
      </el-card>
    </div>

    <!-- 参数参考表格 -->
    <el-divider content-position="left" class="divider" />
    <h3 class="table-title">常见测井参数及意义</h3>
    <el-table 
      :data="Object.entries(parameterRanges).map(([key, value]) => ({ 
        param: key, 
        full_name: getFullParamName(key),
        meaning: getParamMeaning(key),
        range: `[${value.min}, ${value.max === Infinity ? '∞' : value.max}] ${value.unit}`,
        required: value.required ? '是' : '否'
      }))" 
      class="params-table"
      border
      stripe
    >
      <el-table-column prop="param" label="参数代码" width="100" align="center" />
      <el-table-column prop="full_name" label="参数全称" width="120" align="center" />
      <el-table-column prop="meaning" label="物理意义" min-width="300" />
      <el-table-column prop="range" label="正常范围" width="180" align="center" />
      <el-table-column prop="required" label="是否必填" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.required === '是' ? 'success' : 'info'">
            {{ scope.row.required }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<style scoped>
/* 全局样式重置 */
:deep(.el-card) {
  --el-card-border-radius: 8px;
  --el-card-box-shadow: none;
  border: 1px solid #e8e8e8;
  overflow: hidden;
}

/* 卡片头部样式 */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.header-actions {
  display: flex;
  gap: 10px;
}

:deep(.el-card__header) {
  background: linear-gradient(90deg, #1989fa 0%, #409eff 100%);
  padding: 12px 20px;
  border-bottom: none;
}

/* 基本信息表单样式 */
.basic-info-form {
  margin: 20px;
}

.form-item {
  margin-bottom: 0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.input-card {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  height: 100%;
}

.form-input {
  width: 100%;
}

.input-error {
  border-color: #f56c6c !important;
}

.error-tip {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 4px;
  line-height: 1.4;
}

/* 步骤条样式 */
.steps-container {
  margin: 0 20px 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e9ecef;
}

/* 参数输入区域核心样式 - 匹配图片效果 */
.params-container {
  margin: 0 20px 30px;
}

.param-row {
  margin-bottom: 10px;
}

.param-col {
  padding: 0;
}

/* 参数输入卡片样式 - 关键修正 */
.param-input-card {
  display: flex;
  align-items: center;
  background: #f0f7ff;
  border: 1px solid #d1e7ff;
  border-radius: 4px;
  padding: 8px 12px;
  gap: 10px;
  height: 50px;
}

.param-label {
  color: #1989fa;
  font-weight: 500;
  min-width: 80px;
  flex-shrink: 0;
}

.required-mark {
  color: #f56c6c;
  margin-left: 2px;
}

.param-input {
  flex: 1;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  height: 34px;
  padding: 0 10px;
}

:deep(.param-input:focus) {
  border-color: #409eff;
  outline: none;
}

/* CNL理论值卡片 */
.cnl-theory-card {
  background: #f0f7ff;
  border: 1px solid #d1e7ff;
  border-radius: 4px;
  padding: 12px;
  margin: 15px 0;
  color: #1989fa;
  font-weight: 500;
  border-left: 4px solid #409eff;
}

/* 提交按钮样式 */
.submit-btn-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20px;
}

.submit-step-btn {
  background: linear-gradient(90deg, #1989fa 0%, #409eff 100%);
  border: none;
  border-radius: 20px;
  padding: 8px 30px;
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.shortcut-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* 底部导航按钮 */
.nav-bar {
  display: flex;
  justify-content: space-between;
  margin: 0 20px 20px;
  padding: 10px 20px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.nav-btn {
  padding: 8px 20px;
  border-radius: 4px;
}

.submit-all-btn {
  background-color: #409eff;
  border-color: #409eff;
}

/* 表格样式 */
.divider {
  margin: 0 20px 15px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 20px 16px;
}

.params-table {
  --el-table-header-text-color: #fff;
  --el-table-header-text-align: center;
  --el-table-row-hover-bg-color: #e8e8e8;
  --el-table-border-color: #e9ecef;
  width: calc(100% - 40px);
  margin: 0 20px 20px;
}

:deep(.el-table__header) {
  background: #409eff;
}

:deep(.el-table th) {
  background-color: #409eff !important;
  color: #fff !important;
  font-weight: 500;
}

/* 预测结果卡片样式 */
.result-card {
  margin: 0 20px 20px;
}

.predict-result-card {
  border: 1px solid #e1f5fe;
}

:deep(.predict-result-card .el-card__header) {
  background: #e1f5fe;
  color: #0288d1;
  font-weight: 600;
}

.result-content {
  text-align: center;
  padding: 10px 0;
}

.result-label {
  font-size: 16px;
  color: #333;
}

.result-value {
  font-size: 28px;
  color: #0288d1;
  font-weight: 600;
  margin: 0 10px;
}

.result-tip {
  font-size: 12px;
  color: #999;
  display: block;
  margin-top: 10px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .param-input-card {
    flex-direction: column;
    height: auto;
    padding: 10px;
    align-items: flex-start;
  }
  
  .param-label {
    min-width: auto;
    margin-bottom: 5px;
  }
  
  .param-input {
    width: 100%;
  }
  
  .result-value {
    font-size: 24px;
  }
}
</style>