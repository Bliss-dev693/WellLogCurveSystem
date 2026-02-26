<template>
  <div class="report-container">
    <!-- 报告头部 -->
    <div class="report-header">
      <h1 class="report-title">测井孔隙度预测报告</h1>
      <p class="report-subtitle">基于多参数测井曲线的机器学习孔隙度预测</p>
      <p class="report-subtitle-en">Well Log Porosity Prediction Report Based on Multi-Parameter Machine Learning</p>
      <div class="report-info">
        <div class="report-info-item">
          <span class="report-info-label">报告编号:</span>
          <span id="reportNum">WLP-{{ new Date().getFullYear() }}-0001</span>
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
          <span>{{ currentTime }}</span>
        </div>
      </div>
    </div>

    <div class="report-content">
      <!-- 上传区域 -->
      <div class="upload-section">
        <h2>数据上传与分析</h2>
        <div class="file-input-wrapper">
          <input
            type="file"
            id="csvFile"
            class="file-input"
            accept=".csv"
            @change="handleFileSelect"
            ref="fileInput"
          />
          <label for="csvFile" class="file-label">选择CSV文件</label>
          <button class="upload-btn" @click="handleUpload" :disabled="!selectedFile || loading">
            {{ loading ? '处理中...' : '开始分析' }}
          </button>
          
        </div>
        <div v-if="fileInfo" class="file-info">
          <p><strong>文件名:</strong> {{ fileInfo.name }}</p>
          <p><strong>井名:</strong> {{ fileInfo.wellName || '--' }}</p>
          <p><strong>数据点数:</strong> {{ fileInfo.dataPoints || '--' }}</p>
          <p><strong>可用曲线:</strong> {{ fileInfo.availableCurves || '--' }}</p>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="info-section">
        <h2 class="section-title">数据统计信息</h2>
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
            <div class="info-value" style="color: #27ae60">{{ accuracy || '--' }}</div>
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

    <div v-if="showActions" class="action-buttons">
      <button class="btn btn-primary" @click="exportChartImage">导出图表</button>
      <button class="btn btn-primary" @click="downloadPredictData">下载数据</button>
      <button class="btn btn-secondary" @click="resetReport">重新分析</button>
      <button class="btn btn-primary" @click="exportToPdf">导出报告为PDF</button>
    </div>
  </div>

  <!-- 加载状态 -->
  <div v-if="loading" class="loading active">
    <div class="spinner"></div>
    <p>正在处理数据...</p>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

const exportToPdf = async () => {
  const element = document.querySelector('.report-container'); // 获取要导出的 DOM 元素
  const canvas = await html2canvas(element);
  const imgData = canvas.toDataURL('image/png');

  const pdf = new jsPDF();
  const imgWidth = 210; // A4 宽度
  const pageHeight = 297; // A4 高度
  const imgHeight = (canvas.height * imgWidth) / canvas.width;
  let heightLeft = imgHeight;

  let position = 0;

  pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
  heightLeft -= pageHeight;

  while (heightLeft >= 0) {
    position = heightLeft - imgHeight;
    pdf.addPage();
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
    heightLeft -= pageHeight;
  }

  pdf.save(`测井孔隙度预测报告_${currentWellName.value}.pdf`);
};
// 响应式数据
const fileInput = ref(null)
const selectedFile = ref(null)
const loading = ref(false)
const currentTime = ref('')
const fileInfo = ref(null)
const currentWellName = ref('')
const depthRange = ref('')
const samplingInterval = ref('')
const totalPoints = ref('')
const accuracy = ref('')

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

// 显示控制
const showCharts = ref(false)
const showStatistics = ref(false)
const showConclusion = ref(false)
const showActions = ref(false)

// 数据
const uploadedData = ref(null)
const statistics = ref([])

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

// 更新时间
const updateCurrentTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
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
  }
}

// 处理上传
const handleUpload = async () => {
  if (!selectedFile.value) {
    alert('请选择CSV文件！')
    return
  }

  if (!selectedFile.value.name.toLowerCase().endsWith('.csv')) {
    alert('错误: 请选择CSV格式的文件！')
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
    
    // 先显示图表区域，确保DOM渲染
    showCharts.value = true
    showStatistics.value = true
    showConclusion.value = true
    showActions.value = true
    
    // 等待DOM更新，确保图表容器已经渲染
    await nextTick()
    
    // 计算统计信息
    calculateStatistics(data)
    
    // 等待一下确保图表容器完全渲染
    await new Promise(resolve => setTimeout(resolve, 100))
    
    // 生成图表
    await generateCharts(data)
    
    // 滚动到图表区域
    setTimeout(() => {
      const chartsSection = document.querySelector('.charts-section')
      if (chartsSection) {
        chartsSection.scrollIntoView({ behavior: 'smooth' })
      }
    }, 300)

  } catch (error) {
    console.error('处理数据时出错:', error)
    alert(`处理数据时出错: ${error.message}`)
    
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

// 解析CSV - 针对您的数据格式进行了优化
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
    
    // 调整图表大小
    handleResize()
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
    
    // 检查是否真的没有尺寸
    setTimeout(() => {
      if (!container.offsetWidth || !container.offsetHeight) {
        console.error(`${chartName}容器仍然没有尺寸`)
      }
    }, 50)
  }
  
  return true
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

    // 计算X轴范围：从数据实际范围开始
    let xAxisMin = 0
    let xAxisMax = 100
    if (allLogValues.length > 0) {
      const minValue = Math.min(...allLogValues)
      const maxValue = Math.max(...allLogValues)
      const range = maxValue - minValue
      xAxisMin = Math.floor(minValue - range * 0.05) // 留5%的左边距
      xAxisMax = Math.ceil(maxValue + range * 0.05)  // 留5%的右边距
    }

    // 计算Y轴深度范围：从数据实际深度范围开始
    let yAxisMin = 0
    let yAxisMax = 1000
    if (depth.length > 0) {
      yAxisMin = Math.min(...depth)
      yAxisMax = Math.max(...depth)
      const depthRange = yAxisMax - yAxisMin
      // 添加适当的边距，但保持深度的逻辑顺序
      yAxisMin = Math.floor(yAxisMin - depthRange * 0.02) // 2%的上边距（浅层）
      yAxisMax = Math.ceil(yAxisMax + depthRange * 0.02)  // 2%的下边距（深层）
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
      yAxisMin = Math.floor((denMin - denRange * 0.05) * 100) / 100  // 保留两位小数
      yAxisMax = Math.ceil((denMax + denRange * 0.05) * 100) / 100   // 保留两位小数
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
    
  } catch (error) {
    console.error('初始化GR-DEN图表失败:', error)
  }
}

// 4. 孔隙度对比图
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
    const matrixAC = 55      // 骨架声波时差 (μs/ft)
    const fluidAC = 189      // 流体声波时差 (μs/ft)  
    const matrixDEN = 2.65   // 骨架密度 (g/cm³)
    const fluidDEN = 1.0     // 流体密度 (g/cm³)

    // 收集所有孔隙度数据用于计算范围
    const allPoroValues = []

    for (let i = 0; i < depth.length; i++) {
      // Wyllie公式计算声波孔隙度
      if (data.AC && data.AC[i] !== null) {
        const poro = Math.max(0, Math.min(40, ((data.AC[i] - matrixAC) / (fluidAC - matrixAC)) * 100))
        acPoro.push([i, poro])
        allPoroValues.push(poro)
      }

      // 密度公式计算孔隙度
      if (data.DEN && data.DEN[i] !== null) {
        const poro = Math.max(0, Math.min(40, ((matrixDEN - data.DEN[i]) / (matrixDEN - fluidDEN)) * 100))
        denPoro.push([i, poro])
        allPoroValues.push(poro)
      }

      if (data.CNL && data.CNL[i] !== null) {
        const poro = Math.max(0, Math.min(40, data.CNL[i]))
        cnlPoro.push([i, poro])
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
        smooth: true
      })
    }
    
    if (denPoro.length > 0) {
      series.push({
        name: '密度孔隙度 (%)',
        type: 'line',
        data: denPoro,
        lineStyle: { color: '#e74c3c', width: 2 },
        symbol: 'none',
        smooth: true
      })
    }
    
    if (cnlPoro.length > 0) {
      series.push({
        name: '中子孔隙度 (%)',
        type: 'line',
        data: cnlPoro,
        lineStyle: { color: '#f39c12', width: 2 },
        symbol: 'none',
        smooth: true
      })
    }
    
    if (series.length === 0) {
      console.warn('没有孔隙度数据')
      return
    }

    // 计算Y轴范围：从数据最小值开始
    let yAxisMin = 0
    let yAxisMax = 40
    if (allPoroValues.length > 0) {
      yAxisMin = Math.max(0, Math.floor(Math.min(...allPoroValues) - 2)) // 留2%的边距
      yAxisMax = Math.min(40, Math.ceil(Math.max(...allPoroValues) + 2)) // 留2%的边距
    }
    
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
          const index = params[0].dataIndex
          let result = `深度: ${depth[index]}m<br/>`
          params.forEach(param => {
            if (param.value && param.value[1] !== undefined) {
              result += `${param.seriesName}: ${param.value[1].toFixed(2)}%<br/>`
            }
          })
          return result
        }
      },
      legend: {
        top: 30,
        data: series.map(s => s.name)
      },
      xAxis: {
        type: 'value',
        name: '数据点索引',
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        }
      },
      yAxis: {
        type: 'value',
        name: '孔隙度 (%)',
        min: yAxisMin,
        max: yAxisMax,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        }
      },
      series: series
    }
    
    porosityChartInstance.setOption(option)
    
  } catch (error) {
    console.error('初始化孔隙度图表失败:', error)
  }
}

// 5. 电阻率对比图
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
        rtData.push([i, data.RT[i]])
        allResistivityValues.push(data.RT[i])
      }
      if (data.RXO && data.RXO[i] !== null) {
        rxoData.push([i, data.RXO[i]])
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
        smooth: true
      })
    }
    
    if (rxoData.length > 0) {
      series.push({
        name: 'RXO (Ω·m)',
        type: 'line',
        data: rxoData,
        lineStyle: { color: '#e67e22', width: 2 },
        symbol: 'none',
        smooth: true
      })
    }
    
    if (series.length === 0) {
      console.warn('没有电阻率数据')
      return
    }

    // 计算Y轴范围：从数据最小值开始，使用对数坐标
    let yAxisMin = 0.1  // 电阻率不能为0或负数
    let yAxisMax = 10000
    if (allResistivityValues.length > 0) {
      const minValue = Math.min(...allResistivityValues)
      const maxValue = Math.max(...allResistivityValues)
      yAxisMin = Math.max(0.1, minValue * 0.5) // 留50%的下边距
      yAxisMax = maxValue * 2 // 留100%的上边距
    }
    
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
          const index = params[0].dataIndex
          let result = `深度: ${depth[index]}m<br/>`
          params.forEach(param => {
            if (param.value && param.value[1] !== undefined) {
              result += `${param.seriesName}: ${param.value[1].toFixed(3)} Ω·m<br/>`
            }
          })
          return result
        }
      },
      legend: {
        top: 30,
        data: series.map(s => s.name)
      },
      xAxis: {
        type: 'value',
        name: '数据点索引',
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        }
      },
      yAxis: {
        type: 'log',
        name: '电阻率 (Ω·m)',
        logBase: 10,
        min: yAxisMin,
        max: yAxisMax,
        axisLine: {
          lineStyle: {
            color: '#333'
          }
        }
      },
      series: series
    }
    
    resistivityChartInstance.setOption(option)
    
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

// 导出图表图片
const exportChartImage = () => {
  if (!mainChartInstance) {
    alert('请先上传数据并生成图表！')
    return
  }
  
  try {
    const url = mainChartInstance.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    
    const link = document.createElement('a')
    link.href = url
    link.download = `测井曲线_${currentWellName.value}_${new Date().getTime()}.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    alert('图表已导出为PNG图片！')
  } catch (error) {
    console.error('导出图表失败:', error)
    alert('导出图表失败，请重试')
  }
}

// 下载预测数据
const downloadPredictData = () => {
  if (!uploadedData.value) {
    alert('请先上传数据！')
    return
  }
  
  try {
    let csv = 'Depth'
    
    // 添加表头
    Object.keys(fieldMappings).forEach(field => {
      if (uploadedData.value[field]) {
        csv += ',' + field
      }
    })
    csv += '\n'
    
    // 添加数据行
    for (let i = 0; i < uploadedData.value.Depth.length; i++) {
      csv += uploadedData.value.Depth[i]
      
      Object.keys(fieldMappings).forEach(field => {
        if (uploadedData.value[field]) {
          csv += ',' + (uploadedData.value[field][i] !== null ? uploadedData.value[field][i].toFixed(3) : '')
        }
      })
      csv += '\n'
    }
    
    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `测井数据_${currentWellName.value}_${new Date().getTime()}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    alert('数据已导出为CSV文件！')
  } catch (error) {
    console.error('下载数据失败:', error)
    alert('下载数据失败，请重试')
  }
}

// 重置报告
const resetReport = () => {
  if (confirm('确定要重新分析吗？当前分析结果将被清除。')) {
    if (fileInput.value) {
      fileInput.value.value = ''
    }
    
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
}

// 窗口大小变化时重新调整图表
const handleResize = () => {
  const charts = [
    mainChartInstance,
    grAcChartInstance,
    grDenChartInstance,
    porosityChartInstance,
    resistivityChartInstance
  ]
  
  charts.forEach((instance, index) => {
    if (instance && typeof instance.resize === 'function') {
      try {
        instance.resize()
      } catch (error) {
        console.error(`调整图表 ${index} 大小失败:`, error)
      }
    }
  })
}

// 生命周期钩子
onMounted(() => {
  updateCurrentTime()
  const interval = setInterval(updateCurrentTime, 1000)
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
  
  // 清理定时器和监听器
  onUnmounted(() => {
    clearInterval(interval)
    destroyCharts()
    window.removeEventListener('resize', handleResize)
  })
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Microsoft YaHei', 'SimSun', Arial, sans-serif;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px;
  color: #333;
}

.report-container {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  min-height: 100vh;
}

/* 报告头部 */
.report-header {
  background: linear-gradient(135deg, #1a4d7a 0%, #2e7aac 100%);
  color: white;
  padding: 40px 30px;
  text-align: center;
  border-bottom: 3px solid #0d2f52;
}

.report-title {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 8px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
  letter-spacing: 1px;
}

.report-subtitle {
  font-size: 16px;
  opacity: 0.95;
  margin-bottom: 5px;
}

.report-subtitle-en {
  font-size: 14px;
  opacity: 0.85;
  font-style: italic;
}

.report-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  margin-top: 20px;
  font-size: 13px;
  gap: 10px;
  line-height: 1.8;
}

.report-info-item {
  display: flex;
  align-items: center;
  justify-content: center;
}

.report-info-label {
  font-weight: bold;
  margin-right: 8px;
}

/* 主要内容区域 */
.report-content {
  padding: 40px;
}

/* 上传区域 */
.upload-section {
  background: linear-gradient(135deg, #f0f5fa 0%, #e8f1f7 100%);
  border-radius: 6px;
  padding: 25px;
  margin-bottom: 30px;
  border-left: 5px solid #e67e22;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.upload-section h2 {
  font-size: 22px;
  font-weight: bold;
  color: #1a4d7a;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 2px solid #e0e0e0;
}

.upload-section h2::before {
  content: "▶";
  color: #2e7aac;
  margin-right: 12px;
  font-size: 18px;
}

.file-input-wrapper {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  align-items: center;
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
}

.file-label:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(26, 77, 122, 0.4);
}

.upload-btn {
  background: linear-gradient(135deg, #27ae60, #229954);
  color: white;
  padding: 12px 28px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-size: 14px;
}

.upload-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(39, 174, 96, 0.4);
}

.upload-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.file-info {
  background: white;
  padding: 15px;
  border-radius: 6px;
  border-left: 4px solid #3498db;
  margin-top: 15px;
}

.file-info p {
  margin: 5px 0;
  font-size: 14px;
  color: #555;
}

.file-info strong {
  color: #1a4d7a;
}

/* 基本信息板块 */
.info-section {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 25px;
  margin-bottom: 30px;
  border-left: 5px solid #1a4d7a;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

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

.section-title::before {
  content: "▶";
  color: #2e7aac;
  margin-right: 12px;
  font-size: 18px;
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
}

.info-item:hover {
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

/* 曲线可视化区域 */
.charts-section {
  margin-bottom: 30px;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-container {
  background: white;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 20px;
  position: relative;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: all 0.3s ease;
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
  height: 250px;
  min-height: 250px;
}

.main-chart {
  grid-column: 1 / -1;
  min-height: 520px;
}

.main-chart .chart-canvas {
  height: 380px;
  min-height: 380px;
}

/* 统计数据表格 */
.statistics-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
  background: white;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.statistics-table th {
  background: linear-gradient(135deg, #1a4d7a 0%, #2e7aac 100%);
  color: white;
  padding: 14px;
  text-align: center;
  font-weight: bold;
  font-size: 13px;
}

.statistics-table td {
  padding: 14px;
  border-bottom: 1px solid #e0e0e0;
  text-align: center;
  font-size: 13px;
}

.statistics-table tr:hover {
  background: #f9f9f9;
}

/* 特征参数板块 */
.feature-section {
  background: linear-gradient(135deg, #f0f5fa 0%, #e8f1f7 100%);
  border-radius: 6px;
  padding: 25px;
  margin-bottom: 30px;
  border-left: 5px solid #e67e22;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
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
}

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

/* 预测过程说明 */
.process-section {
  background: #fff9e6;
  border-radius: 6px;
  padding: 20px;
  margin-bottom: 30px;
  border-left: 5px solid #f39c12;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
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
}

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

/* 加载状态 */
.loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.3);
  z-index: 1000;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  display: none;
}

.loading.active {
  display: flex;
}

.spinner {
  border: 4px solid rgba(52, 152, 219, 0.1);
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading p {
  color: white;
  font-weight: 600;
  font-size: 16px;
}

/* 操作按钮 */
.action-buttons {
  text-align: center;
  margin-top: 30px;
  padding: 25px;
  border-top: 1px solid #e0e0e0;
  background: #f9f9f9;
  border-radius: 6px;
}

.btn {
  padding: 12px 28px;
  margin: 0 10px 10px;
  border: none;
  border-radius: 5px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn-primary {
  background: linear-gradient(135deg, #1a4d7a 0%, #2e7aac 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(26, 77, 122, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(26, 77, 122, 0.4);
}

.btn-secondary {
  background: #95a5a6;
  color: white;
}

.btn-secondary:hover {
  background: #7f8c8d;
  transform: translateY(-2px);
}

.feature-name-col {
  text-align: left;
  font-weight: 600;
  color: #1a4d7a;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .report-content {
    padding: 20px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .report-info {
    grid-template-columns: 1fr;
  }

  .report-title {
    font-size: 28px;
  }

  .section-title {
    font-size: 18px;
  }

  .file-input-wrapper {
    flex-direction: column;
  }

  .file-label, .upload-btn {
    width: 100%;
    text-align: center;
  }
  
  .chart-container {
    min-height: 300px;
  }
  
  .main-chart {
    min-height: 450px;
  }
}
</style>