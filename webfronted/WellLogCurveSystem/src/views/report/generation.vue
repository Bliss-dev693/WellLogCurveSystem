<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { useUserStore } from '@/stores'
import * as reportTemplateApi from '@/api/reportTemplate.js'
import * as logDataApi from '@/api/logData.js'
import * as echarts from 'echarts'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'

// 设置组件名称
defineOptions({
  name: 'ReportGeneration'
})

// 用户状态
const userStore = useUserStore()
const userId = computed(() => userStore.user?.id)

// 报告生成状态
const reportData = reactive({
  templateId: null,
  wellName: '',
  datasetName: '',
  startDate: '',
  endDate: '',
  depthRange: { min: '', max: '' },
  curveTypes: [],
  analysisType: 'standard',
  customVariables: {}
})

const loading = ref(false)
const step = ref(1)
const selectedTemplate = ref(null)
const availableTemplates = ref([])
const logDataList = ref([])
const reportPreview = ref('')
const generatedReport = ref(null)

// CSV上传相关状态
const selectedFile = ref(null)
const fileInfo = ref(null)
const currentWellName = ref('')
const depthRange = ref('')
const samplingInterval = ref('')
const totalPoints = ref('')
const accuracy = ref('')
const showCharts = ref(false)
const showStatistics = ref(false)
const showConclusion = ref(false)
const showActions = ref(false)
const uploadedData = ref(null)
const statistics = ref([])

// 图表容器引用
const mainChart = ref(null)
const grAcChart = ref(null)
const grDenChart = ref(null)
const porosityChart = ref(null)
const resistivityChart = ref(null)

// 图表实例
let mainChartInstance = null
let grAcChartInstance = null
let grDenChartInstance = null
let porosityChartInstance = null
let resistivityChartInstance = null

// 常量数据
const fieldMappings = {
  AC: { name: '声波时差', unit: 'μs/ft', color: '#2ecc71' },
  CAL: { name: '井径', unit: 'in', color: '#3498db' },
  CNL: { name: '中子', unit: '%', color: '#f39c12' },
  DEN: { name: '密度', unit: 'g/cm³', color: '#e74c3c' },
  GR: { name: '自然伽马', unit: 'API', color: '#9b59b6' },
  RT: { name: '深电阻率', unit: 'Ω·m', color: '#1abc9c' },
  RXO: { name: '浅电阻率', unit: 'Ω·m', color: '#e67e22' }
}

const featureParameters = [
  { code: 'AC', name: '声波时差 (Acoustic Time)', desc: '单位: μs/ft。测量声波在岩石中传播的时间，与孔隙度呈正相关。用于计算孔隙度的主要参数。' },
  { code: 'CAL', name: '井径 (Caliper)', desc: '单位: mm。测量井眼实际直径，用于识别井壁扩径、缩径等状况，优化测井曲线解释。' },
  { code: 'DEN', name: '密度 (Density)', desc: '单位: g/cm³。测量岩石体积密度，与孔隙度呈负相关。是孔隙度计算的重要依据。' },
  { code: 'GR', name: '自然伽马 (Gamma Ray)', desc: '单位: API。测量天然放射性强度。用于岩性识别和层位划分，辅助孔隙度预测。' },
  { code: 'RT', name: '真电阻率 (True Resistivity)', desc: '单位: Ω·m。测量原生状态下的电阻率，反映流体性质和孔隙度。油气层和水层电阻率不同。' },
  { code: 'RXO', name: '冲洗带电阻率 (Flushed Zone)', desc: '单位: Ω·m。测量钻井液冲洗后浅表部分的电阻率，反映孔隙流体侵入情况。' },
  { code: 'CNL', name: '中子测井 (Compensated Neutron Log)', desc: '单位: %。直接测量孔隙度，是孔隙度预测的重要直接指标。与AC、DEN组合可提高预测精度。' }
]

const processSteps = [
  { title: '数据输入', desc: '输入井名、井深范围和7个测井参数(AC、CAL、CNL、DEN、GR、RT、RXO)' },
  { title: '数据预处理', desc: '数据归一化、异常值处理、缺失值补全等预处理操作' },
  { title: '特征工程', desc: '特征提取、特征组合、多尺度特征融合' },
  { title: '模型预测', desc: '通过CNN-LSTM网络进行孔隙度预测，输出连续孔隙度曲线' },
  { title: '结果验证', desc: '与训练集和验证集数据对比，评估预测精度和可靠性' },
  { title: '报告生成', desc: '生成预测报告、统计分析、可视化展示' }
]

// 分析类型选项
const analysisTypes = [
  { value: 'standard', label: '标准分析', description: '基础测井数据分析' },
  { value: 'technical', label: '技术分析', description: '岩石物理参数分析' },
  { value: 'comprehensive', label: '综合分析', description: '全方位地质评价' },
  { value: 'custom', label: '自定义分析', description: '自定义分析参数' }
]

// 曲线类型选项
const curveTypes = [
  { value: 'GR', label: '自然伽马(GR)' },
  { value: 'SP', label: '自然电位(SP)' },
  { value: 'RES', label: '电阻率(RES)' },
  { value: 'CAL', label: '井径(CAL)' },
  { value: 'AC', label: '声波时差(AC)' },
  { value: 'CNL', label: '中子(CNL)' },
  { value: 'DEN', label: '密度(DEN)' }
]

// 获取可用模板
const loadTemplates = async () => {
  // 检查用户登录状态
  if (!userId.value) {
    ElMessage.warning('请先登录以获取可用模板')
    // 使用模拟数据作为降级方案
    availableTemplates.value = [
      {
        id: 1,
        name: '标准测井分析报告',
        description: '包含基础测井数据分析的标准报告模板',
        category: 'basic',
        isPublic: true,
        creator: '系统'
      }
    ]
    return
  }
  
  loading.value = true
  try {
    // 构造请求参数
    const params = {
      userId: Number(userId.value),
      isPublic: true,
      pageNum: 1,
      pageSize: 100
    }
    
    // 调用真实API接口
    const response = await reportTemplateApi.getTemplateList(params)
 
    // 处理API响应 - 根据实际返回结构调整
    if (response.data.code === 0 || response.data.code === 200) {
      
      const templates = response.data.data.records || []
      
      availableTemplates.value = templates.map(template => ({
        id: template.id,
        name: template.name || '未命名模板',
        description: template.description || '暂无描述',
        category: template.category || 'basic',
        documentUrl: template.documentUrl || '',
        isPublic: template.isPublic !== undefined ? template.isPublic : true,
        creator: template.creator || template.username || '系统',
        createTime: template.createTime,
        updateTime: template.updateTime
      }))
     
      
      // 如果没有获取到模板，提供默认模板
      if (availableTemplates.value.length === 0) {
        availableTemplates.value = [
          {
            id: 1,
            name: '标准测井分析报告',
            description: '包含基础测井数据分析的标准报告模板',
            category: 'basic',
            isPublic: true,
            creator: '系统'
          }
        ]
        ElMessage.info('暂无可用模板，已加载默认模板')
      } else {
        ElMessage.success(`成功加载 ${availableTemplates.value.length} 个模板`)
      }
    } else {
      throw new Error(response.message || response.msg || '获取模板列表失败')
    }
  } catch (error) {
    console.error('获取模板失败:', error)
    ElMessage.error('获取模板失败: ' + (error.message || '网络错误'))
    
    // 降级到模拟数据
    availableTemplates.value = [
      {
        id: 1,
        name: '标准测井分析报告',
        description: '包含基础测井数据分析的标准报告模板',
        category: 'basic',
        documentUrl: 'https://storage.example.com/templates/basic-report.docx',
        isPublic: true,
        creator: '系统'
      },
      {
        id: 2,
        name: '技术分析报告',
        description: '专业的岩石物理参数分析报告模板',
        category: 'technical',
        documentUrl: 'https://storage.example.com/templates/technical-report.docx',
        isPublic: false,
        creator: '张工程师'
      }
    ]
  } finally {
    loading.value = false
  }
}

// 获取数据集列表
const loadDataSets = async () => {
  // 检查用户登录状态
  if (!userId.value) {
    console.warn('用户未登录，无法获取数据集列表')
    return
  }
  
  loading.value = true
  try {
    // 构造请求参数
    const userIdParam = Number(userId.value)
    
    // 调用真实API接口获取用户数据集（注意：该API只接受userId参数）
    const response = await logDataApi.getUserDatasetsService(userIdParam)
 
  
    if (response.code === 0 || response.code === 200) {
      
      // 根据实际响应结构调整数据处理逻辑
      const rawDatasets = response.records || response.data?.records || response.data?.list || response.data || []
      
      // 如果返回的是字符串数组，则转换为对象格式
      const datasets = Array.isArray(rawDatasets) 
        ? rawDatasets.map((datasetName, index) => ({
            id: `dataset-${index + 1}`,
            wellName: '默认井名',
            datasetName: datasetName,
            createTime: new Date().toISOString(),
            updateTime: new Date().toISOString()
          }))
        : rawDatasets // 如果已经是对象数组，则直接使用
      
      // 数据格式化
      logDataList.value = datasets.map(dataset => ({
        id: dataset.id || dataset.datasetId,
        wellName: dataset.wellName || dataset.name || '未知井名',
        datasetName: dataset.datasetName || dataset.name || '未知数据集',
        createTime: dataset.createTime,
        updateTime: dataset.updateTime
      }))
      
      if (logDataList.value.length > 0) {
        ElMessage.success(`成功加载 ${logDataList.value.length} 个数据集`)
      } else {
        ElMessage.info('暂无可用数据集')
      }
    } else {
      throw new Error(response.message || response.msg || '获取数据集列表失败')
    }
  } catch (error) {
    console.error('获取数据集失败:', error)
    ElMessage.error('获取数据集失败: ' + (error.message || '网络错误'))
    // 保持空数组状态
    logDataList.value = []
  } finally {
    loading.value = false
  }
}

// 选择模板
const selectTemplate = (template) => {
  reportData.templateId = template.id
  selectedTemplate.value = template
  step.value = 2
  ElMessage.success(`已选择模板: ${template.name}`)
}

// 上一步
const prevStep = () => {
  if (step.value > 1) {
    step.value--
  }
}

// 下一步
const nextStep = async () => {
  if (step.value === 2) {
    // 验证第二步表单
    if (!selectedFile.value) {
      ElMessage.warning('请上传CSV数据文件')
      return
    }
    step.value = 3
  } else if (step.value === 3) {
    step.value = 4
  }
}

// 文件选择
const handleFileSelect = (event) => {
  
  const file = event.target.files[0]
 
  
  if (file) {
    selectedFile.value = file
    fileInfo.value = {
      name: file.name,
      wellName: '--',
      dataPoints: '--',
      availableCurves: '--'
    }
    ElMessage.success(`已选择文件: ${file.name}`)
    
  } else {
    ElMessage.warning('未选择文件')
    
  }
}

// 处理上传和分析
const handleUploadAndAnalyze = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择CSV文件！')
    return
  }

  if (!selectedFile.value.name.toLowerCase().endsWith('.csv')) {
    ElMessage.error('错误: 请选择CSV格式的文件！')
    return
  }

  loading.value = true

  try {
    const text = await readFile(selectedFile.value)
    const data = parseCSV(text)
    
    if (Object.keys(data).length === 0) {
      throw new Error('CSV文件中没有找到有效的数据！')
    }

    uploadedData.value = data
    updateReportInfo(data)
    
    // 显示报告内容
    showCharts.value = true
    showStatistics.value = true
    showConclusion.value = true
    showActions.value = true
    
    // 等待DOM更新
    await nextTick()
    
    // 计算统计信息
    calculateStatistics(data)
    
    // 等待一下确保图表容器完全渲染
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // 生成图表
    await generateCharts(data)
    
    step.value = 5
    ElMessage.success('数据分析完成，报告已生成')

  } catch (error) {
    console.error('处理数据时出错:', error)
    ElMessage.error(`处理数据时出错: ${error.message}`)
    
    // 发生错误时隐藏图表区域
    showCharts.value = false
    showStatistics.value = false
    showConclusion.value = false
    showActions.value = false
  } finally {
    loading.value = false
  }
}

// 读取文件
const readFile = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target.result)
    reader.onerror = reject
    reader.readAsText(file)
  })
}

// 解析CSV
const parseCSV = (csvText) => {
  const lines = csvText.trim().split('\n')
  if (lines.length < 3) {
    throw new Error('CSV文件行数不足，至少需要3行（表头、单位行、数据行）')
  }

  // 第一行是表头
  const headers = lines[0].split(',').map(h => h.trim())
  
  // 第二行是单位行，跳过
  // 从第三行开始是数据
  const data = {}
  const fieldIndices = {}
  let wellName = ''
  
  // 初始化数据结构
  data.Depth = []
  
  // 找到需要的字段索引
  Object.keys(fieldMappings).forEach(field => {
    const index = headers.indexOf(field)
    if (index !== -1) {
      fieldIndices[field] = index
      data[field] = []
    }
  })
  
  // 找到wellName和Depth的索引
  const wellNameIndex = headers.indexOf('wellName')
  const depthIndex = headers.indexOf('Depth')
  
  if (depthIndex === -1) {
    throw new Error('未找到Depth字段，请检查CSV格式')
  }
  
  // 从第三行开始解析数据
  for (let i = 2; i < lines.length; i++) {
    const line = lines[i].trim()
    if (!line) continue

    const values = line.split(',')
    
    // 获取井名（只取第一个有效值）
    if (i === 2 && wellNameIndex !== -1 && values[wellNameIndex] && values[wellNameIndex].trim()) {
      wellName = values[wellNameIndex].trim()
    }

    // 解析深度
    const depthValue = parseFloat(values[depthIndex])
    if (isNaN(depthValue)) continue

    data.Depth.push(depthValue)

    // 解析其他字段
    Object.keys(fieldIndices).forEach(field => {
      const index = fieldIndices[field]
      if (index < values.length) {
        const value = parseFloat(values[index])
        data[field].push(isNaN(value) ? null : value)
      } else {
        data[field].push(null)
      }
    })
  }

  if (data.Depth.length === 0) {
    throw new Error('没有找到有效的数据点')
  }

  data.wellName = wellName || 'Unknown'
  
  // 验证数据完整性
  let validFields = 0
  Object.keys(fieldIndices).forEach(field => {
    const validValues = data[field].filter(v => v !== null).length
    if (validValues > 0) {
      validFields++
    }
  })
  
  if (validFields === 0) {
    throw new Error('未找到任何有效的测井数据字段')
  }

  return data
}

// 更新报告信息
const updateReportInfo = (data) => {
  const depth = data.Depth
  const minDepth = Math.min(...depth)
  const maxDepth = Math.max(...depth)
  const wellName = data.wellName || 'Unknown'

  currentWellName.value = wellName
  depthRange.value = `${minDepth.toFixed(1)} - ${maxDepth.toFixed(1)} m`
  totalPoints.value = `${depth.length} 点`
  
  if (depth.length > 1) {
    const interval = (maxDepth - minDepth) / (depth.length - 1)
    samplingInterval.value = `${interval.toFixed(1)} m`
  }

  // 计算有效数据点比例
  let totalValidPoints = 0
  let totalPossiblePoints = 0
  
  Object.keys(fieldMappings).forEach(field => {
    if (data[field]) {
      const validCount = data[field].filter(v => v !== null).length
      totalValidPoints += validCount
      totalPossiblePoints += data[field].length
    }
  })
  
  if (totalPossiblePoints > 0) {
    const dataQuality = (totalValidPoints / totalPossiblePoints * 100).toFixed(1)
    accuracy.value = `${dataQuality}%`
  } else {
    accuracy.value = '0%'
  }

  const availableCurves = Object.keys(fieldMappings)
    .filter(f => data[f] && data[f].some(v => v !== null))
    .map(f => fieldMappings[f].name)
    .join(', ')
  
  fileInfo.value = {
    name: selectedFile.value.name,
    wellName,
    dataPoints: `${depth.length} 个`,
    availableCurves: availableCurves || '无可用曲线'
  }
}

// 生成所有图表
const generateCharts = async (data) => {
  try {
    // 销毁现有图表
    destroyCharts()
    
    // 等待DOM更新
    await nextTick()
    
    // 创建新图表
    await initMainChart(data)
    await initGrAcChart(data)
    await initGrDenChart(data)
    await initPorosityChart(data)
    await initResistivityChart(data)
    
  } catch (error) {
    console.error('生成图表失败:', error)
    throw error
  }
}

// 销毁所有图表
const destroyCharts = () => {
  const charts = [
    { instance: mainChartInstance, name: '主图表' },
    { instance: grAcChartInstance, name: 'GR-AC图表' },
    { instance: grDenChartInstance, name: 'GR-DEN图表' },
    { instance: porosityChartInstance, name: '孔隙度图表' },
    { instance: resistivityChartInstance, name: '电阻率图表' }
  ]
  
  charts.forEach(({ instance, name }) => {
    if (instance && typeof instance.dispose === 'function') {
      try {
        instance.dispose()
      } catch (error) {
        console.warn(`销毁${name}失败:`, error)
      }
    }
  })
  
  // 重置图表实例
  mainChartInstance = null
  grAcChartInstance = null
  grDenChartInstance = null
  porosityChartInstance = null
  resistivityChartInstance = null
}

// 初始化图表容器
const initChartContainer = (containerRef, chartName) => {
  if (!containerRef || !containerRef.value) {
    console.warn(`${chartName}容器未找到`)
    return false
  }
  
  const container = containerRef.value
  
  // 确保容器有尺寸
  if (!container.offsetWidth || !container.offsetHeight) {
    console.warn(`${chartName}容器尺寸为0，强制设置尺寸`)

    // 设置默认尺寸
    if (chartName === '主图表') {
      container.style.height = '380px'
      container.style.minHeight = '380px'
    } else {
      container.style.height = '250px'
      container.style.minHeight = '250px'
    }

    container.style.width = '100%'
  }

  // 如果子节点中存在 .chart-canvas，确保其高度占满父容器
  try {
    const canvasChild = container.querySelector('.chart-canvas')
    if (canvasChild) {
      canvasChild.style.width = '100%'
      // 如果父容器明确设置了高度，子元素应填满
      if (container.style.height) canvasChild.style.height = '100%'
      if (container.style.minHeight) canvasChild.style.minHeight = '100%'
    }
  } catch (e) {
    // ignore
  }

  return true
}

// 确保图表实例在容器渲染后能够正确 resize
const ensureChartResized = (chartInstance) => {
  if (!chartInstance) return
  // 多次尝试 resize，兼容不同渲染时机
  try { chartInstance.resize() } catch (e) {}
  setTimeout(() => { try { chartInstance.resize() } catch (e) {} }, 60)
  setTimeout(() => { try { chartInstance.resize() } catch (e) {} }, 300)
}

const resizeAllCharts = () => {
  [mainChartInstance, grAcChartInstance, grDenChartInstance, porosityChartInstance, resistivityChartInstance]
    .forEach(inst => { if (inst && typeof inst.resize === 'function') try { inst.resize() } catch (e) {} })
}

// 1. 主测井曲线图
const initMainChart = async (data) => {
  try {
    if (!initChartContainer(mainChart, '主图表')) {
      console.error('主图表容器无效')
      return
    }
    
    mainChartInstance = echarts.init(mainChart.value)
    
    const depth = data.Depth
    const series = []
    
    // 收集所有测井值用于计算X轴范围
    const allLogValues = []
    
    // 为每个字段创建数据系列
    Object.keys(fieldMappings).forEach(field => {
      if (data[field]) {
        const fieldData = []
        const fieldValues = data[field]
        
        for (let i = 0; i < depth.length; i++) {
          if (fieldValues[i] !== null) {
            // ECharts coordinate: [x, y] — x 为测井值，y 为深度
            fieldData.push([fieldValues[i], depth[i]])
            allLogValues.push(fieldValues[i])
          }
        }
        
        if (fieldData.length > 0) {
          series.push({
            name: `${field} (${fieldMappings[field].name})`,
            type: 'line',
            data: fieldData,
            lineStyle: {
              color: fieldMappings[field].color,
              width: 2
            },
            symbol: 'none',
            smooth: true,
            showSymbol: false
          })
        }
      }
    })
    
    if (series.length === 0) {
      console.warn('没有有效的数据系列')
      return
    }

    // 计算X轴范围
    let xAxisMin = 0
    let xAxisMax = 100
    if (allLogValues.length > 0) {
      const minValue = Math.min(...allLogValues)
      const maxValue = Math.max(...allLogValues)
      const range = maxValue - minValue
      xAxisMin = Math.floor(minValue - range * 0.05)
      xAxisMax = Math.ceil(maxValue + range * 0.05)
    }

    // 计算Y轴深度范围
    let yAxisMin = 0
    let yAxisMax = 1000
    if (depth.length > 0) {
      yAxisMin = Math.min(...depth)
      yAxisMax = Math.max(...depth)
      const depthRange = yAxisMax - yAxisMin
      yAxisMin = Math.floor(yAxisMin - depthRange * 0.02)
      yAxisMax = Math.ceil(yAxisMax + depthRange * 0.02)
    }
    
    const option = {
      title: {
        text: '多参数测井曲线',
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold',
          color: '#1a4d7a'
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        },
        formatter: (params) => {
          if (!params || params.length === 0) return ''
          const depthValue = params[0].value[1]
          let result = `<div style="margin-bottom:5px"><strong>深度: ${depthValue}m</strong></div>`
          params.forEach(param => {
            if (param.value && param.value[0] !== undefined) {
              const value = param.value[0]
              const field = param.seriesName.split(' ')[0]
              const unit = fieldMappings[field]?.unit || ''
              result += `<div>${param.seriesName}: ${value.toFixed(3)} ${unit}</div>`
            }
          })
          return result
        }
      },
      legend: {
        top: 30,
        data: series.map(s => s.name),
        type: 'scroll'
      },
      grid: {
        left: '60',
        right: '80',
        top: '80',
        bottom: '60',
        containLabel: false
      },
      xAxis: {
        type: 'value',
        name: '测井值',
        nameLocation: 'middle',
        nameGap: 30,
        min: xAxisMin,
        max: xAxisMax,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '深度 (m)',
        min: yAxisMin,
        max: yAxisMax,
        inverse: true,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        },
        nameTextStyle: {
          padding: [0, 0, 0, -40]
        }
      },
      series: series
    }
    
    mainChartInstance.setOption(option)
    ensureChartResized(mainChartInstance)
    
  } catch (error) {
    console.error('初始化主图表失败:', error)
    throw error
  }
}

// 2. GR-AC岩性分析散点图
const initGrAcChart = async (data) => {
  if (!data.GR || !data.AC) {
    console.warn('缺少GR或AC数据')
    return
  }
  
  try {
    if (!initChartContainer(grAcChart, 'GR-AC图表')) {
      console.error('GR-AC图表容器无效')
      return
    }
    
    grAcChartInstance = echarts.init(grAcChart.value)
    
    const scatterData = []
    const grValues = []
    const acValues = []
    
    for (let i = 0; i < data.Depth.length; i++) {
      if (data.GR[i] !== null && data.AC[i] !== null) {
        scatterData.push([data.GR[i], data.AC[i]])
        grValues.push(data.GR[i])
        acValues.push(data.AC[i])
      }
    }
    
    if (scatterData.length === 0) {
      console.warn('GR-AC数据为空')
      return
    }

    // 计算坐标轴范围
    let xAxisMin = 0
    let xAxisMax = 150
    let yAxisMin = 40
    let yAxisMax = 200
    
    if (grValues.length > 0 && acValues.length > 0) {
      const grMin = Math.min(...grValues)
      const grMax = Math.max(...grValues)
      const grRange = grMax - grMin
      
      const acMin = Math.min(...acValues)
      const acMax = Math.max(...acValues)
      const acRange = acMax - acMin
      
      xAxisMin = Math.floor(grMin - grRange * 0.05)
      xAxisMax = Math.ceil(grMax + grRange * 0.05)
      yAxisMin = Math.floor(acMin - acRange * 0.05)
      yAxisMax = Math.ceil(acMax + acRange * 0.05)
    }
    
    const option = {
      title: {
        text: 'GR-AC岩性识别',
        left: 'center',
        textStyle: {
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: (params) => {
          return `GR: ${params.value[0].toFixed(2)} API<br/>AC: ${params.value[1].toFixed(2)} μs/ft`
        }
      },
      xAxis: {
        name: 'GR (API)',
        nameLocation: 'middle',
        nameGap: 25,
        min: xAxisMin,
        max: xAxisMax,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        }
      },
      yAxis: {
        name: 'AC (μs/ft)',
        nameLocation: 'middle',
        nameGap: 30,
        min: yAxisMin,
        max: yAxisMax,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        }
      },
      series: [{
        type: 'scatter',
        data: scatterData,
        symbolSize: 8,
        itemStyle: {
          color: '#3498db',
          opacity: 0.7
        }
      }]
    }
    
    grAcChartInstance.setOption(option)
    ensureChartResized(grAcChartInstance)
    
  } catch (error) {
    console.error('初始化GR-AC图表失败:', error)
  }
}

// 3. GR-DEN岩性分析散点图
const initGrDenChart = async (data) => {
  if (!data.GR || !data.DEN) {
    console.warn('缺少GR或DEN数据')
    return
  }
  
  try {
    if (!initChartContainer(grDenChart, 'GR-DEN图表')) {
      console.error('GR-DEN图表容器无效')
      return
    }
    
    grDenChartInstance = echarts.init(grDenChart.value)
    
    const scatterData = []
    const grValues = []
    const denValues = []
    
    for (let i = 0; i < data.Depth.length; i++) {
      if (data.GR[i] !== null && data.DEN[i] !== null) {
        scatterData.push([data.GR[i], data.DEN[i]])
        grValues.push(data.GR[i])
        denValues.push(data.DEN[i])
      }
    }
    
    if (scatterData.length === 0) {
      console.warn('GR-DEN数据为空')
      return
    }

    // 计算坐标轴范围
    let xAxisMin = 0
    let xAxisMax = 150
    let yAxisMin = 1.9
    let yAxisMax = 2.9
    
    if (grValues.length > 0 && denValues.length > 0) {
      const grMin = Math.min(...grValues)
      const grMax = Math.max(...grValues)
      const grRange = grMax - grMin
      
      const denMin = Math.min(...denValues)
      const denMax = Math.max(...denValues)
      const denRange = denMax - denMin
      
      xAxisMin = Math.floor(grMin - grRange * 0.05)
      xAxisMax = Math.ceil(grMax + grRange * 0.05)
      yAxisMin = Math.floor((denMin - denRange * 0.05) * 100) / 100
      yAxisMax = Math.ceil((denMax + denRange * 0.05) * 100) / 100
    }
    
    const option = {
      title: {
        text: 'GR-DEN岩性分析',
        left: 'center',
        textStyle: {
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: (params) => {
          return `GR: ${params.value[0].toFixed(2)} API<br/>DEN: ${params.value[1].toFixed(3)} g/cm³`
        }
      },
      xAxis: {
        name: 'GR (API)',
        nameLocation: 'middle',
        nameGap: 25,
        min: xAxisMin,
        max: xAxisMax,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        }
      },
      yAxis: {
        name: 'DEN (g/cm³)',
        nameLocation: 'middle',
        nameGap: 30,
        min: yAxisMin,
        max: yAxisMax,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        }
      },
      series: [{
        type: 'scatter',
        data: scatterData,
        symbolSize: 8,
        itemStyle: {
          color: '#e74c3c',
          opacity: 0.7
        }
      }]
    }
    
    grDenChartInstance.setOption(option)
    ensureChartResized(grDenChartInstance)
    
  } catch (error) {
    console.error('初始化GR-DEN图表失败:', error)
  }
}

// 4. 孔隙度对比图 - 修复后的版本
const initPorosityChart = async (data) => {
  try {
    if (!initChartContainer(porosityChart, '孔隙度图表')) {
      console.error('孔隙度图表容器无效')
      return
    }
    
    porosityChartInstance = echarts.init(porosityChart.value)
    
    const depth = data.Depth
    const acPoro = []
    const denPoro = []
    const cnlPoro = []
    
    // 参数设置
    const matrixAC = 55
    const fluidAC = 189
    const matrixDEN = 2.65
    const fluidDEN = 1.0

    // 收集所有孔隙度数据用于计算范围
    const allPoroValues = []

    for (let i = 0; i < depth.length; i++) {
      // Wyllie公式计算声波孔隙度
      if (data.AC && data.AC[i] !== null) {
        const poro = Math.max(0, Math.min(40, ((data.AC[i] - matrixAC) / (fluidAC - matrixAC)) * 100))
        // [x=孔隙度, y=深度]
        acPoro.push([poro, depth[i]])
        allPoroValues.push(poro)
      }

      // 密度公式计算孔隙度
      if (data.DEN && data.DEN[i] !== null) {
        const poro = Math.max(0, Math.min(40, ((matrixDEN - data.DEN[i]) / (matrixDEN - fluidDEN)) * 100))
        denPoro.push([poro, depth[i]])
        allPoroValues.push(poro)
      }

      if (data.CNL && data.CNL[i] !== null) {
        const poro = Math.max(0, Math.min(40, data.CNL[i]))
        cnlPoro.push([poro, depth[i]])
        allPoroValues.push(poro)
      }
    }
    
    const series = []
    
    if (acPoro.length > 0) {
      series.push({
        name: '声波孔隙度 (%)',
        type: 'line',
        data: acPoro,
        lineStyle: { color: '#2ecc71', width: 2 },
        symbol: 'none',
        smooth: true,
        showSymbol: false
      })
    }
    
    if (denPoro.length > 0) {
      series.push({
        name: '密度孔隙度 (%)',
        type: 'line',
        data: denPoro,
        lineStyle: { color: '#e74c3c', width: 2 },
        symbol: 'none',
        smooth: true,
        showSymbol: false
      })
    }
    
    if (cnlPoro.length > 0) {
      series.push({
        name: '中子孔隙度 (%)',
        type: 'line',
        data: cnlPoro,
        lineStyle: { color: '#f39c12', width: 2 },
        symbol: 'none',
        smooth: true,
        showSymbol: false
      })
    }
    
    if (series.length === 0) {
      console.warn('没有孔隙度数据')
      return
    }

    // 计算坐标轴范围
    let xAxisMin = 0
    let xAxisMax = 40
    let yAxisMin = Math.min(...depth)
    let yAxisMax = Math.max(...depth)
    
    if (allPoroValues.length > 0) {
      xAxisMin = Math.max(0, Math.floor(Math.min(...allPoroValues) - 2))
      xAxisMax = Math.min(40, Math.ceil(Math.max(...allPoroValues) + 2))
    }
    
    // 深度范围调整
    const depthRange = yAxisMax - yAxisMin
    yAxisMin = Math.floor(yAxisMin - depthRange * 0.02)
    yAxisMax = Math.ceil(yAxisMax + depthRange * 0.02)
    
    const option = {
      title: {
        text: '多种孔隙度对比',
        left: 'center',
        textStyle: {
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        },
        formatter: (params) => {
          if (!params || params.length === 0) return ''
          // 数据格式：[x=孔隙度, y=深度]
          const depthValue = params[0].value[1]
          let result = `<div style="margin-bottom:5px"><strong>深度: ${depthValue.toFixed(1)}m</strong></div>`
          params.forEach(param => {
            if (param.value && param.value[0] !== undefined) {
              result += `<div>${param.seriesName}: ${param.value[0].toFixed(2)}%</div>`
            }
          })
          return result
        }
      },
      legend: {
        top: 30,
        data: series.map(s => s.name)
      },
      grid: {
        left: '60',
        right: '80',
        top: '80',
        bottom: '60',
        containLabel: false
      },
      xAxis: {
        type: 'value',
        name: '孔隙度 (%)',
        nameLocation: 'middle',
        nameGap: 30,
        min: xAxisMin,
        max: xAxisMax,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        },
        axisLabel: {
          formatter: function(value) {
            // 简化显示，只显示整数刻度
            return Number.isInteger(value) ? value + '%' : ''
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '深度 (m)',
        min: yAxisMin,
        max: yAxisMax,
        inverse: true,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        },
        nameTextStyle: {
          padding: [0, 0, 0, -40]
        },
        axisLabel: {
          formatter: function(value) {
            // 简化深度显示
            return value.toFixed(0)
          }
        }
      },
      series: series
    }
    
    porosityChartInstance.setOption(option)
    ensureChartResized(porosityChartInstance)
    
  } catch (error) {
    console.error('初始化孔隙度图表失败:', error)
  }
}

// 5. 电阻率对比图 - 修复后的版本
const initResistivityChart = async (data) => {
  try {
    if (!initChartContainer(resistivityChart, '电阻率图表')) {
      console.error('电阻率图表容器无效')
      return
    }
    
    resistivityChartInstance = echarts.init(resistivityChart.value)
    
    const depth = data.Depth
    const rtData = []
    const rxoData = []
    
    // 收集电阻率数据用于计算范围
    const allResistivityValues = []

    for (let i = 0; i < depth.length; i++) {
      if (data.RT && data.RT[i] !== null) {
        // [x=电阻率, y=深度]
        rtData.push([data.RT[i], depth[i]])
        allResistivityValues.push(data.RT[i])
      }
      if (data.RXO && data.RXO[i] !== null) {
        rxoData.push([data.RXO[i], depth[i]])
        allResistivityValues.push(data.RXO[i])
      }
    }
    
    const series = []
    
    if (rtData.length > 0) {
      series.push({
        name: 'RT (Ω·m)',
        type: 'line',
        data: rtData,
        lineStyle: { color: '#1abc9c', width: 2 },
        symbol: 'none',
        smooth: true,
        showSymbol: false
      })
    }
    
    if (rxoData.length > 0) {
      series.push({
        name: 'RXO (Ω·m)',
        type: 'line',
        data: rxoData,
        lineStyle: { color: '#e67e22', width: 2 },
        symbol: 'none',
        smooth: true,
        showSymbol: false
      })
    }
    
    if (series.length === 0) {
      console.warn('没有电阻率数据')
      return
    }

    // 计算坐标轴范围
    let xAxisMin = 0.1
    let xAxisMax = 10000
    let yAxisMin = Math.min(...depth)
    let yAxisMax = Math.max(...depth)
    
    if (allResistivityValues.length > 0) {
      const minValue = Math.min(...allResistivityValues)
      const maxValue = Math.max(...allResistivityValues)
      xAxisMin = Math.max(0.1, minValue * 0.5)
      xAxisMax = maxValue * 2
    }
    
    // 深度范围调整
    const depthRange = yAxisMax - yAxisMin
    yAxisMin = Math.floor(yAxisMin - depthRange * 0.02)
    yAxisMax = Math.ceil(yAxisMax + depthRange * 0.02)
    
    const option = {
      title: {
        text: '电阻率对比分析',
        left: 'center',
        textStyle: {
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        },
        formatter: (params) => {
          if (!params || params.length === 0) return ''
          // 数据格式：[x=电阻率, y=深度]
          const depthValue = params[0].value[1]
          let result = `<div style="margin-bottom:5px"><strong>深度: ${depthValue.toFixed(1)}m</strong></div>`
          params.forEach(param => {
            if (param.value && param.value[0] !== undefined) {
              result += `<div>${param.seriesName}: ${param.value[0].toFixed(3)} Ω·m</div>`
            }
          })
          return result
        }
      },
      legend: {
        top: 30,
        data: series.map(s => s.name)
      },
      grid: {
        left: '60',
        right: '80',
        top: '80',
        bottom: '60',
        containLabel: false
      },
      xAxis: {
        type: 'log',
        name: '电阻率 (Ω·m)',
        nameLocation: 'middle',
        nameGap: 30,
        logBase: 10,
        min: xAxisMin,
        max: xAxisMax,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        },
        axisLabel: {
          formatter: function(value) {
            // 简化对数坐标显示
            if (value >= 1000) return (value/1000).toFixed(0) + 'k'
            if (value >= 1) return value.toFixed(0)
            return value.toFixed(1)
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '深度 (m)',
        min: yAxisMin,
        max: yAxisMax,
        inverse: true,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        },
        nameTextStyle: {
          padding: [0, 0, 0, -40]
        },
        axisLabel: {
          formatter: function(value) {
            // 简化深度显示
            return value.toFixed(0)
          }
        }
      },
      series: series
    }
    
    resistivityChartInstance.setOption(option)
    ensureChartResized(resistivityChartInstance)
    
  } catch (error) {
    console.error('初始化电阻率图表失败:', error)
  }
}

// 计算统计信息
const calculateStatistics = (data) => {
  statistics.value = []
  
  Object.keys(fieldMappings).forEach(field => {
    if (data[field]) {
      const values = data[field].filter(v => v !== null)
      
      if (values.length > 0) {
        const min = Math.min(...values)
        const max = Math.max(...values)
        const mean = values.reduce((a, b) => a + b, 0) / values.length
        const variance = values.reduce((a, b) => a + Math.pow(b - mean, 2), 0) / values.length
        const std = Math.sqrt(variance)
        
        statistics.value.push({
          code: field,
          name: fieldMappings[field].name,
          unit: fieldMappings[field].unit,
          min: min.toFixed(3),
          max: max.toFixed(3),
          mean: mean.toFixed(3),
          std: std.toFixed(3),
          validPoints: values.length
        })
      }
    }
  })
}

// 导出为PDF
const exportToPdf = async () => {
  if (!uploadedData.value) {
    ElMessage.warning('请先上传并分析数据！')
    return
  }

  try {
    const element = document.querySelector('.report-preview-content')
    if (!element) {
      ElMessage.error('找不到报告内容元素')
      return
    }

    const canvas = await html2canvas(element, {
      scale: 2,
      useCORS: true,
      logging: false
    })
    
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    
    const imgWidth = 210 - 20 // A4宽度减去边距
    const pageHeight = 297 - 20 // A4高度减去边距
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    let heightLeft = imgHeight
    let position = 10

    pdf.addImage(imgData, 'PNG', 10, position, imgWidth, imgHeight)
    heightLeft -= pageHeight

    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 10, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }

    const fileName = `测井孔隙度预测报告_${currentWellName.value || 'unknown'}_${new Date().getTime()}.pdf`
    pdf.save(fileName)
    
    ElMessage.success('报告已导出为PDF文件！')
  } catch (error) {
    console.error('导出PDF失败:', error)
    ElMessage.error('导出PDF失败，请重试')
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(reportData, {
    templateId: null,
    wellName: '',
    datasetName: '',
    startDate: '',
    endDate: '',
    depthRange: { min: '', max: '' },
    curveTypes: [],
    analysisType: 'standard',
    customVariables: {}
  })
  selectedTemplate.value = null
  reportPreview.value = ''
  generatedReport.value = null
  step.value = 1
  
  // 重置CSV上传相关状态
  selectedFile.value = null
  fileInfo.value = null
  uploadedData.value = null
  statistics.value = []
  showCharts.value = false
  showStatistics.value = false
  showConclusion.value = false
  showActions.value = false
  currentWellName.value = ''
  depthRange.value = ''
  samplingInterval.value = ''
  totalPoints.value = ''
  accuracy.value = ''
  
  destroyCharts()
}

// 监听用户状态变化
watch(() => userStore.user, (newUser) => {
  if (newUser?.id) {
    loadTemplates()
    loadDataSets()
  }
}, { immediate: true })

// 组件挂载
onMounted(async () => {
  await Promise.all([
    loadTemplates(),
    loadDataSets()
  ])
  
  // 检查URL参数中的模板ID
  const urlParams = new URLSearchParams(window.location.hash.split('?')[1])
  const templateId = urlParams.get('templateId')
  if (templateId) {
    const template = availableTemplates.value.find(t => t.id == templateId)
    if (template) {
      selectTemplate(template)
    }
  }
  // 在窗口大小变化时确保图表自适应
  window.addEventListener('resize', resizeAllCharts)
})

onUnmounted(() => {
  // 清理窗口事件与图表实例
  try { window.removeEventListener('resize', resizeAllCharts) } catch (e) {}
  destroyCharts()
})
</script>

<template>
  <page-container title="分析报告生成">
    <div class="report-generation-container">
      <!-- 步骤指示器 -->
      <div class="steps-container">
        <el-steps :active="step - 1" finish-status="success" simple>
          <el-step title="选择模板" />
          <el-step title="上传数据" />
          <el-step title="配置参数" />
          <el-step title="生成预览" />
          <el-step title="完成报告" />
        </el-steps>
      </div>

      <!-- 第一步：选择模板 -->
      <div v-show="step === 1" class="step-content">
        <el-card class="template-selection-card">
          <template #header>
            <div class="card-header">
              <span>选择报告模板</span>
              <el-button @click="loadTemplates" :loading="loading">
                <el-icon><Refresh /></el-icon>
                刷新模板
              </el-button>
            </div>
          </template>
          
          <div class="templates-grid">
            <el-card 
              v-for="template in availableTemplates" 
              :key="template.id"
              class="template-card"
              :class="{ selected: selectedTemplate?.id === template.id }"
              @click="selectTemplate(template)"
            >
              <div class="template-content">
                <h3>{{ template.name }}</h3>
                <p class="template-desc">{{ template.description }}</p>
                <div class="template-meta">
                  <el-tag :type="template.isPublic ? 'success' : 'info'" size="small">
                    {{ template.isPublic ? '公开' : '私有' }}
                  </el-tag>
                  <span class="creator">创建者: {{ template.creator || '系统' }}</span>
                </div>
              </div>
            </el-card>
          </div>
          
          <div v-if="availableTemplates.length === 0" class="empty-templates">
            <el-empty description="暂无可用模板">
              <el-button type="primary">创建模板</el-button>
            </el-empty>
          </div>
        </el-card>
      </div>

      <!-- 第二步：上传CSV数据文件 -->
      <div v-show="step === 2" class="step-content">
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>上传CSV数据文件</span>
              <div class="step-actions">
                <el-button @click="prevStep">上一步</el-button>
                <el-button 
                  type="primary" 
                  @click="nextStep" 
                  :loading="loading"
                  :disabled="!selectedFile"
                >
                  下一步
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="upload-section">
            <h3>数据上传与分析</h3>
            <div class="file-input-wrapper">
              <input
                type="file"
                id="csvFileInput"
                ref="csvFile"
                class="file-input"
                accept=".csv"
                @change="handleFileSelect"
              />
              <label for="csvFileInput" class="file-label">选择CSV文件</label>
            </div>
            
            <div v-if="fileInfo" class="file-info">
              <p><strong>文件名:</strong> {{ fileInfo.name }}</p>
              <p><strong>井名:</strong> {{ fileInfo.wellName || '--' }}</p>
              <p><strong>数据点数:</strong> {{ fileInfo.dataPoints || '--' }}</p>
              <p><strong>可用曲线:</strong> {{ fileInfo.availableCurves || '--' }}</p>
            </div>
          </div>
          
          <div class="info-section" v-if="uploadedData">
            <h3>数据统计信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <div class="info-label">井号</div>
                <div class="info-value">{{ currentWellName || 'X-001' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">预测算法</div>
                <div class="info-value">CNN-LSTM神经网络</div>
              </div>
              <div class="info-item">
                <div class="info-label">采样间距</div>
                <div class="info-value">{{ samplingInterval || '--' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">总数据点数</div>
                <div class="info-value">{{ totalPoints || '--' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">预测精度</div>
                <div class="info-value" style="color: #27ae60;">{{ accuracy || '--' }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 第三步：配置分析参数 -->
      <div v-show="step === 3" class="step-content">
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>配置分析参数</span>
              <div class="step-actions">
                <el-button @click="prevStep">上一步</el-button>
                <el-button type="primary" @click="handleUploadAndAnalyze" :disabled="!selectedFile">开始分析</el-button>
              </div>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="分析类型">
                <el-select 
                  v-model="reportData.analysisType" 
                  placeholder="请选择分析类型"
                  style="width: 100%"
                >
                  <el-option
                    v-for="type in analysisTypes"
                    :key="type.value"
                    :label="type.label"
                    :value="type.value"
                  >
                    <span style="float: left">{{ type.label }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">
                      {{ type.description }}
                    </span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="曲线类型">
                <el-select
                  v-model="reportData.curveTypes"
                  multiple
                  placeholder="请选择曲线类型"
                  style="width: 100%"
                >
                  <el-option
                    v-for="curve in curveTypes"
                    :key="curve.value"
                    :label="curve.label"
                    :value="curve.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <div class="custom-variables">
            <h4>自定义变量</h4>
            <el-input
              v-model="reportData.customVariables.remarks"
              type="textarea"
              :rows="4"
              placeholder="请输入备注或其他自定义信息..."
            />
          </div>
        </el-card>
      </div>

      <!-- 第四步：生成预览（显示分析结果） -->
      <div v-show="step === 4" class="step-content">
        <el-card class="preview-card">
          <template #header>
            <div class="card-header">
              <span>分析结果预览</span>
              <div class="step-actions">
                <el-button @click="prevStep">上一步</el-button>
                <el-button 
                  type="primary" 
                  @click="step = 5"
                >
                  查看完整报告
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="preview-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="模板名称">
                {{ selectedTemplate?.name }}
              </el-descriptions-item>
              <el-descriptions-item label="井名">
                {{ currentWellName || reportData.wellName }}
              </el-descriptions-item>
              <el-descriptions-item label="数据点数">
                {{ totalPoints || '--' }}
              </el-descriptions-item>
              <el-descriptions-item label="分析类型">
                {{ analysisTypes.find(t => t.value === reportData.analysisType)?.label }}
              </el-descriptions-item>
              <el-descriptions-item label="深度范围">
                {{ depthRange || '--' }}
              </el-descriptions-item>
              <el-descriptions-item label="预测精度">
                {{ accuracy || '--' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </div>

      <!-- 第五步：完整报告展示 -->
      <div v-show="step === 5" class="step-content">
        <el-card class="preview-result-card">
          <template #header>
            <div class="card-header">
              <span>测井孔隙度预测报告</span>
              <div class="step-actions">
                <el-button @click="resetForm">重新开始</el-button>
                <el-button @click="step = 4">返回预览</el-button>
                <el-button 
                  type="primary" 
                  @click="exportToPdf"
                >
                  <el-icon><Download /></el-icon>
                  导出为PDF
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="report-preview-content">
            <!-- 报告头部信息 -->
            <div class="report-header">
              <h1 class="report-title">测井孔隙度预测报告</h1>
              <p class="report-subtitle">基于多参数测井曲线的机器学习孔隙度预测</p>
              <div class="report-info">
                <div class="report-info-item">
                  <span class="report-info-label">报告编号:</span>
                  <span>WLP-{{ new Date().getFullYear() }}-0001</span>
                </div>
                <div class="report-info-item">
                  <span class="report-info-label">井名:</span>
                  <span>{{ currentWellName || '--' }}</span>
                </div>
                <div class="report-info-item">
                  <span class="report-info-label">井深范围:</span>
                  <span>{{ depthRange || '--' }}</span>
                </div>
                <div class="report-info-item">
                  <span class="report-info-label">生成时间:</span>
                  <span>{{ new Date().toLocaleString('zh-CN') }}</span>
                </div>
              </div>
            </div>

            <!-- 预测特征参数说明 -->
            <div class="feature-section">
              <h2 class="section-title">预测特征参数说明</h2>
              <p style="font-size: 13px; color: #555; margin-bottom: 15px">
                本模型采用6个关键测井参数作为输入特征，通过多层深度学习网络进行孔隙度预测。以下为各参数的详细说明：
              </p>
              <div class="feature-grid">
                <div v-for="feature in featureParameters" :key="feature.code" class="feature-item">
                  <div class="feature-code">{{ feature.code }}</div>
                  <div class="feature-name">{{ feature.name }}</div>
                  <div class="feature-desc">{{ feature.desc }}</div>
                </div>
              </div>
            </div>

            <!-- 预测过程说明 -->
            <div class="process-section">
              <h2 class="section-title">孔隙度预测流程</h2>
              <div class="process-steps">
                <div
                  v-for="(step, index) in processSteps"
                  :key="index"
                  class="process-step"
                >
                  <div class="step-number">{{ index + 1 }}</div>
                  <div class="step-title">{{ step.title }}</div>
                  <div class="step-desc">{{ step.desc }}</div>
                </div>
              </div>
            </div>

            <!-- 曲线可视化 -->
            <div v-if="showCharts" class="charts-section">
              <h2 class="section-title">孔隙度预测结果分析</h2>
              
              <!-- 主预测曲线对比图 -->
              <div class="chart-container main-chart" style="margin-bottom: 20px; height: 520px">
                <h3 class="chart-title">原始测井曲线对比（已上传数据）</h3>
                <p class="chart-description">
                  展示所有上传的测井参数曲线（AC、CAL、CNL、DEN、GR、RT、RXO），用于孔隙度预测的多参数输入。
                </p>
                <div ref="mainChart" class="chart-canvas"></div>
              </div>

              <!-- 子图表网格 -->
              <div class="charts-grid">
                <div class="chart-container">
                  <h3 class="chart-title">GR-AC岩性识别分析</h3>
                  <p class="chart-description">
                    自然伽马与声波时差交会图，用于岩性识别。砂岩通常GR值低、泥岩GR值高。
                  </p>
                  <div ref="grAcChart" class="chart-canvas"></div>
                </div>
                <div class="chart-container">
                  <h3 class="chart-title">GR-DEN岩性分析</h3>
                  <p class="chart-description">
                    自然伽马与密度交会图分析。不同岩性在此图上有不同的分布特征。
                  </p>
                  <div ref="grDenChart" class="chart-canvas"></div>
                </div>
                <div class="chart-container">
                  <h3 class="chart-title">多种孔隙度对比</h3>
                  <p class="chart-description">
                    基于声波、密度、中子测井数据计算的孔隙度对比，展示不同方法的一致性。
                  </p>
                  <div ref="porosityChart" class="chart-canvas"></div>
                </div>
                <div class="chart-container">
                  <h3 class="chart-title">电阻率对比分析</h3>
                  <p class="chart-description">
                    深电阻率(RT)与浅电阻率(RXO)对比，反映地层流体性质和侵入情况。
                  </p>
                  <div ref="resistivityChart" class="chart-canvas"></div>
                </div>
              </div>
            </div>

            <!-- 数据集统计信息 -->
            <div v-if="showStatistics" class="info-section">
              <h2 class="section-title">训练数据集统计</h2>
              <table class="statistics-table">
                <thead>
                  <tr>
                    <th>参数代码</th>
                    <th>参数名称</th>
                    <th>单位</th>
                    <th>最小值</th>
                    <th>最大值</th>
                    <th>平均值</th>
                    <th>标准差</th>
                    <th>有效数据点</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="stat in statistics" :key="stat.code">
                    <td class="feature-name-col">{{ stat.code }}</td>
                    <td>{{ stat.name }}</td>
                    <td>{{ stat.unit }}</td>
                    <td>{{ stat.min }}</td>
                    <td>{{ stat.max }}</td>
                    <td>{{ stat.mean }}</td>
                    <td>{{ stat.std }}</td>
                    <td>{{ stat.validPoints }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="showConclusion" class="info-section">
              <h2 class="section-title">结论与应用建议</h2>
              <div style="font-size: 14px; line-height: 1.8; color: #333">
                <p style="margin-bottom: 12px">
                  <strong>1. 预测质量评估:</strong> 本报告基于CNN-LSTM深度学习模型，利用上传的测井数据进行孔隙度预测。通过对AC、CAL、CNL、DEN、GR、RT、RXO等7个关键测井参数的综合分析，生成预测孔隙度曲线。
                </p>
                <p style="margin-bottom: 12px">
                  <strong>2. 特征参数贡献度:</strong> 声波时差(AC)和密度(DEN)是预测孔隙度的主要特征参数。中子测井(CNL)提供了重要的孔隙度直接指示，电阻率参数(RT、RXO)提供了重要的流体识别信息。
                </p>
                <p style="margin-bottom: 12px">
                  <strong>3. 预测可靠性:</strong> 预测结果的精度取决于上传数据的质量和完整性。建议使用经过质量控制的测井数据，以获得更准确的预测结果。
                </p>
                <p>
                  <strong>4. 应用推荐:</strong> 预测孔隙度可用于储层评估、含油气预测、孔隙度含水饱和度计算等专业应用。特别适用于孔隙度测井缺失或质量低劣地段的补全和重建。
                </p>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </page-container>
</template>

<style scoped lang="scss">
.report-generation-container {
  .steps-container {
    margin-bottom: 30px;
  }
  
  .step-content {
    animation: fadeIn 0.3s ease-in-out;
  }
  
  @keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .step-actions {
    display: flex;
    gap: 12px;
  }
  
  // 模板选择样式
  .template-selection-card {
    .templates-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
      margin-top: 20px;
    }
    
    .template-card {
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
      }
      
      &.selected {
        border-color: #409eff;
        box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
      }
      
      .template-content {
        h3 {
          margin: 0 0 10px 0;
          color: #303133;
        }
        
        .template-desc {
          color: #606266;
          font-size: 14px;
          line-height: 1.5;
          margin-bottom: 15px;
        }
        
        .template-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .creator {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
    
    .empty-templates {
      text-align: center;
      padding: 60px 0;
    }
  }
  
  // 表单卡片样式
  .form-card {
    .custom-variables {
      margin-top: 30px;
      
      h4 {
        margin-bottom: 15px;
        color: #303133;
      }
    }
    
    // 上传区域样式
    .upload-section {
      background: linear-gradient(135deg, #f0f5fa 0%, #e8f1f7 100%);
      border-radius: 6px;
      padding: 25px;
      margin-bottom: 30px;
      border-left: 5px solid #e67e22;
      box-shadow: 0 2px 8px rgba(0,0,0,0.05);
      
      h3 {
        font-size: 22px;
        font-weight: bold;
        color: #1a4d7a;
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        padding-bottom: 10px;
        border-bottom: 2px solid #e0e0e0;
      }
      
      .file-input-wrapper {
        display: flex;
        gap: 15px;
        flex-wrap: wrap;
        align-items: center;
        margin-bottom: 15px;
      }
      
      .file-input {
        display: none;
      }
      
      .file-label {
        background: linear-gradient(135deg, #3498db, #2980b9);
        color: white;
        padding: 12px 28px;
        border-radius: 5px;
        cursor: pointer;
        font-weight: 600;
        transition: all 0.3s ease;
        display: inline-block;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        border: none;
        font-size: 14px;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 20px rgba(26, 77, 122, 0.4);
        }
      }
      
      .file-info {
        background: white;
        padding: 15px;
        border-radius: 6px;
        border-left: 4px solid #3498db;
        
        p {
          margin: 5px 0;
          font-size: 14px;
          color: #555;
        }
        
        strong {
          color: #1a4d7a;
        }
      }
    }
    
    // 基本信息板块
    .info-section {
      background: #f8f9fa;
      border-radius: 6px;
      padding: 25px;
      margin-bottom: 30px;
      border-left: 5px solid #1a4d7a;
      box-shadow: 0 2px 8px rgba(0,0,0,0.05);
      
      h3 {
        font-size: 22px;
        font-weight: bold;
        color: #1a4d7a;
        margin-bottom: 20px;
        display: flex;
        align-items: center;
        padding-bottom: 10px;
        border-bottom: 2px solid #e0e0e0;
      }
      
      .info-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 18px;
      }
      
      .info-item {
        background: white;
        padding: 18px;
        border-radius: 6px;
        border: 1px solid #ddd;
        transition: all 0.3s ease;
        box-shadow: 0 1px 4px rgba(0,0,0,0.05);
        
        &:hover {
          border-color: #2e7aac;
          box-shadow: 0 4px 12px rgba(46, 122, 172, 0.15);
          transform: translateY(-2px);
        }
        
        .info-label {
          font-weight: bold;
          color: #555;
          margin-bottom: 8px;
          font-size: 12px;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }
        
        .info-value {
          color: #1a4d7a;
          font-size: 18px;
          font-weight: 600;
        }
      }
    }
  }
  
  // 预览样式
  .preview-card {
    .preview-info {
      margin-top: 20px;
    }
  }
  
  .preview-result-card {
    .report-preview-content {
      // 报告头部
      .report-header {
        background: linear-gradient(135deg, #1a4d7a 0%, #2e7aac 100%);
        color: white;
        padding: 30px;
        text-align: center;
        border-radius: 6px;
        margin-bottom: 30px;
        
        .report-title {
          font-size: 28px;
          font-weight: bold;
          margin-bottom: 8px;
          text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
          letter-spacing: 1px;
        }
        
        .report-subtitle {
          font-size: 16px;
          opacity: 0.95;
          margin-bottom: 20px;
        }
        
        .report-info {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
          gap: 15px;
          font-size: 13px;
          
          .report-info-item {
            display: flex;
            align-items: center;
            justify-content: center;
            
            .report-info-label {
              font-weight: bold;
              margin-right: 8px;
            }
          }
        }
      }
      
      // 特征参数板块
      .feature-section {
        background: linear-gradient(135deg, #f0f5fa 0%, #e8f1f7 100%);
        border-radius: 6px;
        padding: 25px;
        margin-bottom: 30px;
        border-left: 5px solid #e67e22;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        
        .section-title {
          font-size: 22px;
          font-weight: bold;
          color: #1a4d7a;
          margin-bottom: 20px;
          display: flex;
          align-items: center;
          padding-bottom: 10px;
          border-bottom: 2px solid #e0e0e0;
        }
        
        .feature-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
          gap: 15px;
          margin-top: 15px;
        }
        
        .feature-item {
          background: white;
          padding: 16px;
          border-radius: 6px;
          border: 1px solid #f0e0d0;
          border-left: 4px solid #e67e22;
          
          .feature-code {
            font-family: 'Courier New', monospace;
            font-weight: bold;
            color: #e67e22;
            font-size: 14px;
            margin-bottom: 5px;
          }
          
          .feature-name {
            font-size: 13px;
            color: #333;
            font-weight: 600;
            margin-bottom: 3px;
          }
          
          .feature-desc {
            font-size: 12px;
            color: #666;
            line-height: 1.5;
          }
        }
      }
      
      // 预测过程说明
      .process-section {
        background: #fff9e6;
        border-radius: 6px;
        padding: 20px;
        margin-bottom: 30px;
        border-left: 5px solid #f39c12;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        
        .section-title {
          font-size: 22px;
          font-weight: bold;
          color: #1a4d7a;
          margin-bottom: 20px;
          display: flex;
          align-items: center;
          padding-bottom: 10px;
          border-bottom: 2px solid #e0e0e0;
        }
        
        .process-steps {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
          gap: 15px;
          margin-top: 15px;
        }
        
        .process-step {
          background: white;
          padding: 15px;
          border-radius: 4px;
          border: 1px solid #f0d890;
          position: relative;
          padding-left: 35px;
          
          .step-number {
            position: absolute;
            left: 10px;
            top: 50%;
            transform: translateY(-50%);
            width: 24px;
            height: 24px;
            background: #f39c12;
            color: white;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            font-size: 12px;
          }
          
          .step-title {
            font-size: 13px;
            font-weight: bold;
            color: #333;
            margin-bottom: 5px;
          }
          
          .step-desc {
            font-size: 12px;
            color: #666;
            line-height: 1.5;
          }
        }
      }
      
      // 曲线可视化区域 - 关键修复区域
      .charts-section {
        margin-bottom: 30px;
      }
      
      .charts-grid {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 20px;
        margin-bottom: 20px;
      }
      
      /* 图表样式 - 确保图表宽度与卡片一致 */
      .chart-container {
        background: white;
        border: 1px solid #ddd;
        border-radius: 6px;
        padding: 20px;
        position: relative;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        transition: all 0.3s ease;
        width: 100%; /* 确保卡片宽度占满容器 */
        min-height: 350px;
      }
      
      .chart-container:hover {
        box-shadow: 0 4px 16px rgba(0,0,0,0.1);
        border-color: #2e7aac;
      }
      
      .chart-title {
        font-size: 16px;
        font-weight: bold;
        color: #1a4d7a;
        margin-bottom: 15px;
        text-align: center;
        padding-bottom: 10px;
        border-bottom: 1px solid #e0e0e0;
      }
      
      .chart-description {
        font-size: 12px;
        color: #666;
        margin-bottom: 12px;
        line-height: 1.5;
        font-style: italic;
      }
      
      .chart-canvas {
        width: 100%;
        height: 100%;
        min-height: 250px;
        display: block;
      }
      
      .main-chart {
        grid-column: 1 / -1;
        height: 520px;
        min-height: 520px;
        display: flex;
        flex-direction: column;
      }

      .main-chart .chart-canvas {
        height: 100%;
        min-height: 380px;
      }
      
      
      // 统计数据表格
      .info-section {
        background: #f8f9fa;
        border-radius: 6px;
        padding: 25px;
        margin-bottom: 30px;
        border-left: 5px solid #1a4d7a;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        
        .section-title {
          font-size: 22px;
          font-weight: bold;
          color: #1a4d7a;
          margin-bottom: 20px;
          display: flex;
          align-items: center;
          padding-bottom: 10px;
          border-bottom: 2px solid #e0e0e0;
        }
        
        .statistics-table {
          width: 100%;
          border-collapse: collapse;
          margin-top: 15px;
          background: white;
          border-radius: 6px;
          overflow: hidden;
          box-shadow: 0 2px 10px rgba(0,0,0,0.1);
          
          th {
            background: linear-gradient(135deg, #1a4d7a 0%, #2e7aac 100%);
            color: white;
            padding: 14px;
            text-align: center;
            font-weight: bold;
            font-size: 13px;
          }
          
          td {
            padding: 14px;
            border-bottom: 1px solid #e0e0e0;
            text-align: center;
            font-size: 13px;
          }
          
          tr:hover {
            background: #f9f9f9;
          }
          
          .feature-name-col {
            text-align: left;
            font-weight: 600;
            color: #1a4d7a;
          }
        }
      }
    }
  }
  
  // 响应式设计
  @media (max-width: 768px) {
    .templates-grid {
      grid-template-columns: 1fr !important;
    }
    
    .card-header {
      flex-direction: column;
      gap: 15px;
      align-items: stretch;
    }
    
    .step-actions {
      justify-content: center;
    }
    
    .charts-grid {
      grid-template-columns: 1fr !important;
    }
    
    .info-grid {
      grid-template-columns: 1fr !important;
    }
    
    .report-info {
      grid-template-columns: 1fr !important;
    }
    
    .feature-grid {
      grid-template-columns: 1fr !important;
    }
    
    .process-steps {
      grid-template-columns: 1fr !important;
    }
  }
}
</style>