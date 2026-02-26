<template>
    <PageContainer title="可视化与分析面板"> 
    <div class="container">
      <!-- 主图表区域 -->
      <div class="chart-container">
        <div class="chart-title" id="chartTitle">测井曲线图</div>
        <div class="chart-toolbar" id="chartToolbar" v-if="hasData">
          <button class="toolbar-btn" id="zoomResetBtn" @click="resetZoom">重置缩放</button>
          <button class="toolbar-btn" @click="exportChartImage">导出图像</button>
          <button class="toolbar-btn" id="resetView" @click="resetAllData">清空视图</button>
        </div>

        <!-- 主测井曲线图 -->
        <div ref="chartContainer" class="log-chart"></div>

        <!-- 分析图表区域 -->
        <div class="analysis-charts" v-if="hasData && showAnalysisCharts">
          <div class="analysis-tabs">
            <button
              v-for="tab in analysisTabs"
              :key="tab.id"
              :class="{ active: activeAnalysisTab === tab.id }"
              @click="switchAnalysisTab(tab.id)"
              class="analysis-tab"
            >
              {{ tab.name }}
            </button>
          </div>

          <div class="analysis-content">
            <!-- 岩性分析 -->
            <div v-if="activeAnalysisTab === 'lithology'" class="analysis-section">
              <div class="analysis-charts-grid">
                <div class="chart-wrapper">
                  <div class="chart-title-sm">GR-AC岩性交会图</div>
                  <div ref="grAcChartRef" class="analysis-chart"></div>
                  <div class="chart-description">
                    <p><span class="dot mudstone"></span> 泥岩：高GR，高AC</p>
                    <p><span class="dot sandstone"></span> 砂岩：中低GR，中AC</p>
                    <p><span class="dot carbonate"></span> 碳酸盐岩：低GR，低AC</p>
                  </div>
                </div>
                <div class="chart-wrapper">
                  <div class="chart-title-sm">GR-DEN岩性交会图</div>
                  <div ref="grDenChartRef" class="analysis-chart"></div>
                  <div class="chart-description">
                    <p><span class="dot mudstone"></span> 泥岩：高GR，低DEN</p>
                    <p><span class="dot sandstone"></span> 砂岩：中低GR，中DEN</p>
                    <p><span class="dot dolomite"></span> 白云岩：低GR，高DEN</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 物性分析 -->
            <div v-if="activeAnalysisTab === 'physical'" class="analysis-section">
              <div class="analysis-charts-grid">
                <div class="chart-wrapper">
                  <div class="chart-title-sm">孔隙度分析</div>
                  <div ref="porosityChartRef" class="analysis-chart"></div>
                  <div class="chart-description">
                    <p><span class="line den-porosity"></span> φ_DEN: 密度孔隙度</p>
                    <p><span class="line ac-porosity"></span> φ_AC: 声波孔隙度</p>
                    <p><span class="line cnl"></span> CNL: 中子孔隙度</p>
                  </div>
                </div>
                <div class="chart-wrapper">
                  <div class="chart-title-sm">CAL与GR分析</div>
                  <div ref="calGrChartRef" class="analysis-chart"></div>
                  <div class="chart-description">
                    <p><span class="line cal"></span> CAL: 井径曲线</p>
                    <p><span class="line gr"></span> GR: 自然伽马曲线</p>
                    <p>• 泥岩扩径：GR高，CAL增大</p>
                    <p>• 渗透层：CAL规则或缩径</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 含油气性分析 -->
            <div v-if="activeAnalysisTab === 'hydrocarbon'" class="analysis-section">
              <div class="analysis-charts-grid">
                <div class="chart-wrapper">
                  <div class="chart-title-sm">Pickett图 (RT-孔隙度)</div>
                  <div ref="pickettChartRef" class="analysis-chart"></div>
                  <div class="chart-description">
                    <p><span class="dot oil"></span> 油层：高RT，高孔隙度</p>
                    <p><span class="dot water"></span> 水层：低RT，沿趋势线分布</p>
                    <p><span class="dot gas"></span> 气层：极高RT，低孔隙度</p>
                  </div>
                </div>
                <div class="chart-wrapper">
                  <div class="chart-title-sm">电阻率侵入分析</div>
                  <div ref="resistivityChartRef" class="analysis-chart"></div>
                  <div class="chart-description">
                    <p><span class="line rt"></span> RT: 深电阻率</p>
                    <p><span class="line rxo"></span> RXO: 浅电阻率</p>
                    <p>• RT > RXO: 可能含油气</p>
                    <p>• RT ≈ RXO: 可能为水层</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 综合交会图 -->
            <div v-if="activeAnalysisTab === 'crossplots'" class="analysis-section">
              <div class="analysis-charts-grid">
                <div class="chart-wrapper">
                  <div class="chart-title-sm">CNL-DEN交会图</div>
                  <div ref="cnlDenChartRef" class="analysis-chart"></div>
                  <div class="chart-description">
                    <p>• 识别岩性：不同矿物聚类区域</p>
                    <p>• 识别气层：向左上方偏移</p>
                    <p>• 计算孔隙度：沿水线分布</p>
                  </div>
                </div>
                <div class="chart-wrapper">
                  <div class="chart-title-sm">气层指示图</div>
                  <div ref="gasIndicatorChartRef" class="analysis-chart"></div>
                  <div class="chart-description">
                    <p>• 气层指示 = φ_DEN - φ_CNL</p>
                    <p>• 正值：可能为气层</p>
                    <p>• 负值：可能为水层</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div class="loading" :class="{ active: isLoading }">
          <div class="spinner"></div>
          <p>正在加载数据...</p>
        </div>
      </div>

      <!-- 控制面板 -->
      <div class="controls">
        <!-- 数据上传区域 -->
        <div class="upload-section">
          <div class="file-input-wrapper">
            <input type="file" id="csvFile" class="file-input" accept=".csv"
                   ref="fileInputRef" @change="handleFileUpload">
            <label for="csvFile" class="file-label">选择CSV文件</label>
          </div>

          <div id="fileInfo" class="selected-file" v-if="hasData">
            <h4>已加载文件</h4>
            <p> {{ fileInfo.name }}</p>
            <p> {{ fileInfo.size }}</p>
            <p> {{ fileInfo.dataPoints }}</p>
          </div>
        </div>

        <!-- 曲线显示控制 -->
        <div class="control-section" v-if="hasData">
          <h3>曲线显示控制</h3>
          <div id="curveControls">
            <div class="curve-control" v-for="config in curveConfigs" :key="config.name">
              <div class="curve-color" :style="{ backgroundColor: config.color }"></div>
              <label :title="`范围: ${config.originalMin.toFixed(2)} - ${config.originalMax.toFixed(2)}`"
                     @click="toggleCurveVisibility(config)">
                {{ config.name }} ({{ config.unit }})
              </label>
              <input type="checkbox" class="checkbox" :checked="config.visible"
                     @change="toggleCurveVisibility(config)">
            </div>
          </div>
        </div>

<!--        &lt;!&ndash; 分析参数设置 &ndash;&gt;-->
<!--        <div class="control-section" v-if="hasData">-->
<!--          <h3> 分析参数设置</h3>-->
<!--          <div class="param-control">-->
<!--            <label>骨架密度 (g/cm³):</label>-->
<!--            <div class="param-input-wrapper">-->
<!--              <input type="number" v-model="matrixDensity" step="0.01" min="2.0" max="3.0"-->
<!--                     class="param-input" @change="updateAnalysisCharts">-->
<!--              <span class="param-hint">砂岩: 2.65 | 灰岩: 2.71</span>-->
<!--            </div>-->
<!--          </div>-->
<!--          <div class="param-control">-->
<!--            <label>流体密度 (g/cm³):</label>-->
<!--            <div class="param-input-wrapper">-->
<!--              <input type="number" v-model="fluidDensity" step="0.01" min="0.8" max="1.2"-->
<!--                     class="param-input" @change="updateAnalysisCharts">-->
<!--              <span class="param-hint">水: 1.0 | 油: 0.8</span>-->
<!--            </div>-->
<!--          </div>-->
<!--          <div class="param-control">-->
<!--            <label>地层水电阻率 (Ω·m):</label>-->
<!--            <input type="number" v-model="waterResistivity" step="0.1" min="0.01" max="10"-->
<!--                   class="param-input" @change="updateAnalysisCharts">-->
<!--          </div>-->
<!--          <button class="btn btn-secondary" @click="updateAnalysisCharts">更新分析图表</button>-->
<!--        </div>-->

        <!-- 测井曲线说明 -->
        <div class="data-info">
          <h4> 测井曲线说明</h4>
          <p><strong>AC:</strong>声波时差 (μs/ft) - 计算孔隙度</p>
          <p><strong>CAL:</strong>井径 (in) - 井眼直径变化</p>
          <p><strong>CNL:</strong>补偿中子孔隙度 (%)</p>
          <p><strong>DEN:</strong>密度 (g/cm³) - 地层密度</p>
          <p><strong>GR:</strong>自然伽马 (API) - 识别岩性</p>
          <p><strong>RT:</strong>深电阻率 (Ω·m) - 地层真电阻率</p>
          <p><strong>RXO:</strong>浅电阻率 (Ω·m) - 冲洗带电阻率</p>
        </div>

        <div class="report-section">
          <button class="btn btn-knowledge" @click="toggleReport">
            <span class="btn-icon"></span> 查看图表分析参考
          </button>
          <button class="btn btn-knowledge" @click="toggleAnalysisCharts">{{ showAnalysisCharts ? '隐藏分析图表' : '显示分析图表' }}</button>
          <button class="btn btn-knowledge" @click="downloadAllAnalysisCharts" v-if="hasData && showAnalysisCharts">
            <span class="btn-icon"></span> 下载所有已查看分析图表
          </button>
        </div>
      </div>
    </div>

    <!-- 图表分析知识报告弹窗 -->
    <div class="report-modal" :class="{ active: showReport }">
      <div class="report-content">
        <div class="report-header">
          <h2><span class="header-icon"></span> 测井图表分析知识参考</h2>
          <button class="close-btn" @click="toggleReport">×</button>
        </div>

        <div class="report-body">
          <!-- 报告内容动态渲染 -->
          <div v-if="reportContent" v-html="reportContent"></div>
        </div>

        <div class="report-footer">
          <button class="btn btn-primary" @click="downloadReport">
            <span class="btn-icon"></span> 下载
          </button>
          <button class="btn btn-secondary" @click="toggleReport">
            <span class="btn-icon"></span> 关闭
          </button>
        </div>
      </div>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import PageContainer from '@/components/PageContainer.vue'

// ==================== ECharts实例 ====================
const chartContainer = ref(null)
let myChart = null

// 分析图表实例
const grAcChart = ref(null)
const grDenChart = ref(null)
const porosityChart = ref(null)
const calGrChart = ref(null)
const pickettChart = ref(null)
const resistivityChart = ref(null)
const cnlDenChart = ref(null)
const gasIndicatorChart = ref(null)

// 分析图表DOM引用
const grAcChartRef = ref(null)
const grDenChartRef = ref(null)
const porosityChartRef = ref(null)
const calGrChartRef = ref(null)
const pickettChartRef = ref(null)
const resistivityChartRef = ref(null)
const cnlDenChartRef = ref(null)
const gasIndicatorChartRef = ref(null)

// 文件输入框ref
const fileInputRef = ref(null)

// ==================== 响应式数据 ====================
const isLoading = ref(false)
const logData = ref(null)
const curveConfigs = ref([])
const currentWellName = ref('')
const currentDatasetName = ref('')
const fileInfo = ref({
  name: '',
  size: '',
  dataPoints: ''
})

// 报告相关
const showReport = ref(false)
const reportContent = ref('')

// 分析图表相关
const showAnalysisCharts = ref(false)
const activeAnalysisTab = ref('lithology')
const analysisTabs = [
  { id: 'lithology', name: '岩性分析' },
  { id: 'physical', name: '物性分析' },
  { id: 'hydrocarbon', name: '含油气性分析' },
  { id: 'crossplots', name: '综合交会图' }
]

// 分析参数
const matrixDensity = ref(2.65) // 砂岩骨架密度
const fluidDensity = ref(1.0)   // 流体密度
// const waterResistivity = ref(0.1) // 地层水电阻率

// ==================== 报告相关方法 ====================

// 切换报告显示
const toggleReport = () => {
  showReport.value = !showReport.value
  if (showReport.value) {
    generateReportContent()
  }
}

// 生成报告内容
const generateReportContent = () => {
  reportContent.value = `
    <div class="knowledge-report">
      <div class="report-summary">
        <h3><span class="icon"></span> 摘要</h3>
        <p>本参考基于测井曲线解读核心三属性（岩性/物性/含油气性），解释各分析图表的逻辑、原理和应用方法。</p>
        <div class="summary-stats"></div>
      </div>

      <div class="section" id="section-lithology">
        <h3><span class="section-icon"></span> 一、岩性分析图</h3>
        <p class="section-desc">通过GR、AC、DEN等曲线特征识别地下岩石类型</p>

        <div class="chart-explain">
          <h4>1. GR-AC岩性交会图</h4>
          <div class="explain-grid">
            <div class="explain-item">
              <div class="item-title"> 图表逻辑</div>
              <ul>
                <li><strong>X轴</strong>：GR（自然伽马），值越高表示泥质含量越高</li>
                <li><strong>Y轴</strong>：AC（声波时差），值越高表示岩石越疏松</li>
              </ul>
            </div>
            <div class="explain-item">
              <div class="item-title"> 分类标准</div>
              <ul>
                <li><span class="color-dot mudstone"></span> <strong>泥岩</strong>：GR > 100 API 且 AC > 300 μs/ft</li>
                <li><span class="color-dot sandstone"></span> <strong>砂岩</strong>：GR < 80 API，AC在250-350 μs/ft</li>
                <li><span class="color-dot carbonate"></span> <strong>碳酸盐岩</strong>：GR < 50 API 且 AC < 250 μs/ft</li>
              </ul>
            </div>
          </div>
        </div>

        <div class="chart-explain">
          <h4>2. GR-DEN岩性交会图</h4>
          <div class="explain-grid">
            <div class="explain-item">
              <div class="item-title"> 图表逻辑</div>
              <ul>
                <li><strong>X轴</strong>：GR（自然伽马）</li>
                <li><strong>Y轴</strong>：DEN（密度，反向显示），值越低表示密度越小</li>
              </ul>
            </div>
            <div class="explain-item">
              <div class="item-title"> 区分要点</div>
              <ul>
                <li><strong>泥岩</strong>：高GR，低DEN（2.2-2.5g/cm³）</li>
                <li><strong>砂岩</strong>：中低GR，中DEN（2.0-2.6g/cm³）</li>
                <li><strong>白云岩</strong>：低GR，高DEN（2.7-2.9g/cm³）</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="section" id="section-physical">
        <h3><span class="section-icon"></span> 二、物性分析图</h3>
        <p class="section-desc">量化储层孔隙度和渗透性，评估储集能力</p>

        <div class="chart-explain">
          <h4>1. 孔隙度分析图</h4>
          <div class="explain-grid">
            <div class="explain-item">
              <div class="item-title"> 图表逻辑</div>
              <ul>
                <li><strong>X轴</strong>：孔隙度百分比（%）</li>
                <li><strong>Y轴</strong>：深度（m），反向显示</li>
              </ul>
            </div>
            <div class="explain-item">
              <div class="item-title"> 三条曲线</div>
              <ul>
                <li><span class="line den-porosity"></span> <strong>φ_DEN</strong>：密度孔隙度，公式：(2.65 - DEN)/(2.65 - 1.0) × 100%</li>
                <li><span class="line ac-porosity"></span> <strong>φ_AC</strong>：声波孔隙度，公式：(AC - 55.5)/(189 - 55.5) × 100%</li>
                <li><span class="line cnl"></span> <strong>CNL</strong>：中子孔隙度直接值</li>
              </ul>
            </div>
            <div class="explain-item">
              <div class="item-title"> 储层评估</div>
              <ul>
                <li><strong>高孔储层</strong>：孔隙度 > 20%（优质）</li>
                <li><strong>中孔储层</strong>：孔隙度 10-20%（中等）</li>
                <li><strong>低孔储层</strong>：孔隙度 < 10%（差）</li>
              </ul>
            </div>
          </div>
        </div>

        <div class="chart-explain">
          <h4>2. CAL与GR分析图</h4>
          <div class="explain-grid">
            <div class="explain-item">
              <div class="item-title"> 图表逻辑</div>
              <ul>
                <li><strong>左侧X轴</strong>：CAL（井径，蓝色曲线）</li>
                <li><strong>右侧X轴</strong>：GR（自然伽马，红色曲线）</li>
                <li><strong>Y轴</strong>：深度（m）</li>
              </ul>
            </div>
            <div class="explain-item">
              <div class="item-title"> 判断标准</div>
              <ul>
                <li><span class="badge warning">泥岩扩径</span>：GR高（>100API）且CAL增大</li>
                <li><span class="badge success">渗透层</span>：CAL规则或缩径（井壁稳定）</li>
                <li><span class="badge info">泥岩缩径</span>：高压泥岩，CAL减小</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="section" id="section-hydrocarbon">
        <h3><span class="section-icon"></span> 三、含油气性分析图</h3>
        <p class="section-desc">识别孔隙中流体类型，判断油、气、水层</p>

        <div class="chart-explain">
          <h4>1. Pickett图（RT-孔隙度交会图）</h4>
          <div class="explain-grid">
            <div class="explain-item">
              <div class="item-title"> 图表逻辑</div>
              <ul>
                <li><strong>X轴</strong>：孔隙度（%），线性坐标</li>
                <li><strong>Y轴</strong>：RT（深电阻率），对数坐标</li>
              </ul>
            </div>
            <div class="explain-item">
              <div class="item-title"> 流体识别</div>
              <ul>
                <li><span class="color-dot oil"></span> <strong>油层</strong>：高RT（水层5-10倍），高孔隙度</li>
                <li><span class="color-dot water"></span> <strong>水层</strong>：低RT，沿水线分布</li>
                <li><span class="color-dot gas"></span> <strong>气层</strong>：极高RT（水层10-20倍）</li>
              </ul>
            </div>
          </div>
        </div>

        <div class="chart-explain">
          <h4>2. 电阻率侵入分析图</h4>
          <div class="explain-grid">
            <div class="explain-item">
              <div class="item-title"> 图表逻辑</div>
              <ul>
                <li><strong>X轴</strong>：电阻率（Ω·m），对数坐标</li>
                <li><strong>Y轴</strong>：深度（m）</li>
              </ul>
            </div>
            <div class="explain-item">
              <div class="item-title"> 两条曲线</div>
              <ul>
                <li><span class="line rt"></span> <strong>RT</strong>：深电阻率，反映原状地层</li>
                <li><span class="line rxo"></span> <strong>RXO</strong>：浅电阻率，反映冲洗带</li>
              </ul>
            </div>
            <div class="explain-item">
              <div class="item-title"> 侵入特征</div>
              <ul>
                <li><span class="badge success">RT > RXO</span>：钻井液滤液侵入，可能含油气</li>
                <li><span class="badge info">RT ≈ RXO</span>：无侵入或水层</li>
                <li><span class="badge warning">RT < RXO</span>：低阻环带，可能为低阻油层</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="section" id="section-crossplots">
        <h3><span class="section-icon"></span> 四、综合交会图</h3>
        <p class="section-desc">多参数综合分析，提高解释准确性</p>

        <div class="chart-explain">
          <h4>1. CNL-DEN交会图</h4>
          <div class="explain-grid">
            <div class="explain-item">
              <div class="item-title"> 图表逻辑</div>
              <ul>
                <li><strong>X轴</strong>：CNL（中子孔隙度，%）</li>
                <li><strong>Y轴</strong>：DEN（密度，g/cm³），反向显示</li>
              </ul>
            </div>
            <div class="explain-item">
              <div class="item-title"> 多重功能</div>
              <ul>
                <li><strong>岩性识别</strong>：不同矿物聚类区域</li>
                <li><strong>气层识别</strong>：向左上方偏移（低CNL，低DEN）</li>
                <li><strong>孔隙度计算</strong>：数据点沿水线分布</li>
              </ul>
            </div>
            <div class="explain-item">
              <div class="item-title"> 气层特征</div>
              <p><strong>"三低"特征</strong>：低AC、低CNL、低DEN，是识别气层的关键</p>
            </div>
          </div>
        </div>

        <div class="chart-explain">
          <h4>2. 气层指示图</h4>
          <div class="explain-grid">
            <div class="explain-item">
              <div class="item-title"> 图表逻辑</div>
              <ul>
                <li><strong>左侧X轴</strong>：气层指示值 = φ_DEN - φ_CNL</li>
                <li><strong>右侧X轴</strong>：GR（自然伽马）</li>
                <li><strong>Y轴</strong>：深度（m）</li>
              </ul>
            </div>
            <div class="explain-item">
              <div class="item-title"> 计算原理</div>
              <p>气层指示 = 密度孔隙度 - 中子孔隙度<br>正值：可能为气层<br>负值：可能为水层</p>
            </div>
            <div class="explain-item">
              <div class="item-title"> 气层判断</div>
              <ul>
                <li><span class="badge success">气层指示 > 0</span>：DEN孔隙度 > CNL孔隙度</li>
                <li><span class="badge warning">结合GR</span>：排除高GR泥岩段干扰</li>
                <li><span class="badge info">定量化</span>：数值越大，气层可能性越高</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="report-footer-note">
        <p><strong> 时间：</strong> ${new Date().toLocaleString()}</p>
      </div>
    </div>
  `
}

// 下载报告
const downloadReport = () => {
  const element = document.createElement('a')
  const blob = new Blob([`
# 测井图表分析知识报告
## 生成时间：${new Date().toLocaleString()}

${reportContent.value.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, ' ')}
  `], { type: 'text/markdown' })

  element.href = URL.createObjectURL(blob)
  element.download = `测井图表分析报告_${new Date().getTime()}.md`
  document.body.appendChild(element)
  element.click()
  document.body.removeChild(element)
}

// 下载所有分析图表
const downloadAllAnalysisCharts = async () => {
  if (!hasData.value || !showAnalysisCharts.value) {
    alert('请先加载数据并显示分析图表！')
    return
  }

  // 获取所有分析图表的DOM引用数组
  const chartInstances = [
    { chart: grAcChart.value, name: 'GR-AC岩性交会图' },
    { chart: grDenChart.value, name: 'GR-DEN岩性交会图' },
    { chart: porosityChart.value, name: '孔隙度分析图' },
    { chart: calGrChart.value, name: 'CAL与GR分析图' },
    { chart: pickettChart.value, name: 'Pickett图' },
    { chart: resistivityChart.value, name: '电阻率侵入分析图' },
    { chart: cnlDenChart.value, name: 'CNL-DEN交会图' },
    { chart: gasIndicatorChart.value, name: '气层指示图' }
  ].filter(item => item.chart !== null)

  if (chartInstances.length === 0) {
    alert('没有可下载的分析图表！')
    return
  }

  // 逐个下载图表
  chartInstances.forEach((item, index) => {
    setTimeout(() => {
      try {
        const imgData = item.chart.getDataURL({
          type: 'png',
          pixelRatio: 2,
          backgroundColor: '#fff'
        })

        const link = document.createElement('a')
        link.href = imgData
        link.download = `${index + 1}_${item.name}_${currentWellName.value || '未知井'}.png`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      } catch (error) {
        console.error(`下载图表 ${item.name} 失败:`, error)
      }
    }, index * 300) // 每个图表间隔300ms，避免浏览器阻止
  })

  alert(`开始下载 ${chartInstances.length} 个图表，请稍等...`)
}

// ==================== 计算属性 ====================
const hasData = computed(() => {
  return logData.value !== null && logData.value.Depth && logData.value.Depth.length > 0
})

// ==================== 监听器 ====================
// 监听分析图表显示状态
watch(showAnalysisCharts, (newVal) => {
  if (newVal && hasData.value) {
    nextTick(() => {
      initAnalysisCharts()
      updateAnalysisCharts()
    })
  } else {
    // 隐藏时销毁图表实例
    destroyAnalysisCharts()
  }
})

// 监听分析标签切换
watch(activeAnalysisTab, () => {
  if (showAnalysisCharts.value && hasData.value) {
    nextTick(() => {
      initAnalysisCharts()
      updateAnalysisCharts()
    })
  }
})

// ==================== CSV文件字段映射 ====================
const fieldMappings = {
  'AC': { name: 'AC', unit: 'μs/ft', color: '#2ecc71' },
  'CAL': { name: 'CAL', unit: 'in', color: '#3498db' },
  'CNL': { name: 'CNL', unit: '%', color: '#e67e22' },
  'DEN': { name: 'DEN', unit: 'g/cm³', color: '#f39c12' },
  'GR': { name: 'GR', unit: 'API', color: '#e74c3c' },
  'RT': { name: 'RT', unit: 'Ω·m', color: '#9b59b6' },
  'RXO': { name: 'RXO', unit: 'Ω·m', color: '#1abc9c' }
}

// ==================== 核心函数 ====================

// 初始化主图表
const initChart = () => {
  if (!chartContainer.value) return

  myChart = echarts.init(chartContainer.value)

  const option = {
    title: {
      text: '请上传CSV格式的测井数据文件',
      left: 'center',
      top: 'center',
      textStyle: {
        color: '#7f8c8d',
        fontSize: 16,
        fontWeight: 'normal'
      },
      subtext: '支持标准测井曲线格式：Depth, AC, CAL, CNL, DEN, GR, RT, RXO',
      subtextStyle: {
        color: '#95a5a6',
        fontSize: 12,
        marginTop: 10
      }
    },
    graphic: {
      type: 'text',
      left: 'center',
      top: '60%',
      style: {
        text: '点击"选择CSV文件"按钮开始',
        fill: '#3498db',
        fontSize: 14,
        fontWeight: 'bold'
      }
    }
  }

  myChart.setOption(option)

  // 添加图表点击事件
  myChart.on('click', function(params) {
    if (params.componentType === 'series') {
      console.log('点击了曲线:', params.seriesName, '深度:', params.value[1], '值:', params.value[0])
    }
  })
}

// 初始化分析图表
const initAnalysisCharts = () => {
  try {
    // 先销毁当前标签下的图表实例
    destroyCurrentTabCharts()

    // 根据当前标签初始化对应的图表
    switch (activeAnalysisTab.value) {
      case 'lithology':
        if (grAcChartRef.value) {
          grAcChart.value = echarts.init(grAcChartRef.value)
        }
        if (grDenChartRef.value) {
          grDenChart.value = echarts.init(grDenChartRef.value)
        }
        break

      case 'physical':
        if (porosityChartRef.value) {
          porosityChart.value = echarts.init(porosityChartRef.value)
        }
        if (calGrChartRef.value) {
          calGrChart.value = echarts.init(calGrChartRef.value)
        }
        break

      case 'hydrocarbon':
        if (pickettChartRef.value) {
          pickettChart.value = echarts.init(pickettChartRef.value)
        }
        if (resistivityChartRef.value) {
          resistivityChart.value = echarts.init(resistivityChartRef.value)
        }
        break

      case 'crossplots':
        if (cnlDenChartRef.value) {
          cnlDenChart.value = echarts.init(cnlDenChartRef.value)
        }
        if (gasIndicatorChartRef.value) {
          gasIndicatorChart.value = echarts.init(gasIndicatorChartRef.value)
        }
        break
    }


  } catch (error) {
    console.error('分析图表初始化失败:', error)
  }
}

// 销毁当前标签的图表
const destroyCurrentTabCharts = () => {
  switch (activeAnalysisTab.value) {
    case 'lithology':
      if (grAcChart.value) {
        grAcChart.value.dispose()
        grAcChart.value = null
      }
      if (grDenChart.value) {
        grDenChart.value.dispose()
        grDenChart.value = null
      }
      break

    case 'physical':
      if (porosityChart.value) {
        porosityChart.value.dispose()
        porosityChart.value = null
      }
      if (calGrChart.value) {
        calGrChart.value.dispose()
        calGrChart.value = null
      }
      break

    case 'hydrocarbon':
      if (pickettChart.value) {
        pickettChart.value.dispose()
        pickettChart.value = null
      }
      if (resistivityChart.value) {
        resistivityChart.value.dispose()
        resistivityChart.value = null
      }
      break

    case 'crossplots':
      if (cnlDenChart.value) {
        cnlDenChart.value.dispose()
        cnlDenChart.value = null
      }
      if (gasIndicatorChart.value) {
        gasIndicatorChart.value.dispose()
        gasIndicatorChart.value = null
      }
      break
  }
}

// 销毁所有分析图表
const destroyAnalysisCharts = () => {
  const charts = [
    grAcChart.value,
    grDenChart.value,
    porosityChart.value,
    calGrChart.value,
    pickettChart.value,
    resistivityChart.value,
    cnlDenChart.value,
    gasIndicatorChart.value
  ]

  charts.forEach(chart => {
    if (chart && typeof chart.dispose === 'function') {
      chart.dispose()
    }
  })

  // 重置图表实例
  grAcChart.value = null
  grDenChart.value = null
  porosityChart.value = null
  calGrChart.value = null
  pickettChart.value = null
  resistivityChart.value = null
  cnlDenChart.value = null
  gasIndicatorChart.value = null
}

// 计算孔隙度
const calculatePorosity = () => {
  if (!logData.value) return { phiDEN: [], phiAC: [] }

  const phiDEN = []
  const phiAC = []

  for (let i = 0; i < logData.value.Depth.length; i++) {
    // 密度孔隙度
    const denValue = logData.value.DEN[i]
    if (denValue !== null && !isNaN(denValue)) {
      const phi = (matrixDensity.value - denValue) / (matrixDensity.value - fluidDensity.value) * 100
      phiDEN.push(phi >= 0 && phi <= 100 ? phi : null)
    } else {
      phiDEN.push(null)
    }

    // 声波孔隙度 (AC单位: μs/ft)
    const acValue = logData.value.AC[i]
    if (acValue !== null && !isNaN(acValue)) {
      const phi = (acValue - 55.5) / (189 - 55.5) * 100
      phiAC.push(phi >= 0 && phi <= 100 ? phi : null)
    } else {
      phiAC.push(null)
    }
  }

  return { phiDEN, phiAC }
}

// 更新分析图表
const updateAnalysisCharts = () => {
  if (!logData.value || !showAnalysisCharts.value) return

  try {
    const { phiDEN, phiAC } = calculatePorosity()
    const depth = logData.value.Depth

    // 确保图表已初始化
    initAnalysisCharts()

    // 根据当前标签更新对应的图表
    switch (activeAnalysisTab.value) {
      case 'lithology':
        updateLithologyCharts()
        break
      case 'physical':
        updatePhysicalCharts(phiDEN, phiAC, depth)
        break
      case 'hydrocarbon':
        updateHydrocarbonCharts(phiDEN, depth)
        break
      case 'crossplots':
        updateCrossplotCharts(phiDEN, depth)
        break
    }

    // 调整图表大小
    resizeAnalysisCharts()
  } catch (error) {
    console.error('更新分析图表失败:', error)
  }
}

// 更新岩性分析图表
const updateLithologyCharts = () => {
  // GR-AC交会图
  if (grAcChart.value && logData.value.GR && logData.value.AC) {
    const data = []
    const categories = []

    for (let i = 0; i < Math.min(logData.value.Depth.length, 1000); i++) {
      const gr = logData.value.GR[i]
      const ac = logData.value.AC[i]

      if (gr !== null && !isNaN(gr) && ac !== null && !isNaN(ac)) {
        let category = 0 // 0:未知
        let color = '#95a5a6'

        if (gr > 100 && ac > 300) {
          category = 1 // 泥岩
          color = '#8e44ad'
        } else if (gr < 80 && ac > 250 && ac < 350) {
          category = 2 // 砂岩
          color = '#3498db'
        } else if (gr < 50 && ac < 250) {
          category = 3 // 碳酸盐岩
          color = '#e67e22'
        }

        data.push([gr, ac, category])
        categories.push({ category, color })
      }
    }

    const seriesData = [[], [], [], []]
    const colors = ['#95a5a6', '#8e44ad', '#3498db', '#e67e22']
    const names = ['未知', '泥岩', '砂岩', '碳酸盐岩']

    data.forEach((item) => {
      const category = item[2]
      seriesData[category].push([item[0], item[1]])
    })

    const series = []
    for (let i = 0; i < 4; i++) {
      if (seriesData[i].length > 0) {
        series.push({
          name: names[i],
          type: 'scatter',
          data: seriesData[i],
          symbolSize: 6,
          itemStyle: {
            color: colors[i]
          },
          emphasis: {
            scale: true,
            scaleSize: 10
          }
        })
      }
    }

    const option = {
      title: {
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 'bold' }
      },
      tooltip: {
        trigger: 'item',
        formatter: function(params) {
          return `${params.seriesName}<br>GR: ${params.value[0].toFixed(1)} API<br>AC: ${params.value[1].toFixed(1)} μs/ft`
        }
      },
      xAxis: {
        name: 'GR (API)',
        nameLocation: 'middle',
        nameGap: 25,
        nameTextStyle: { fontWeight: 'bold' },
        min: 0,
        max: 200
      },
      yAxis: {
        name: 'AC (μs/ft)',
        nameLocation: 'middle',
        nameGap: 30,
        nameTextStyle: { fontWeight: 'bold' },
        min: 50,
        max: 400
      },
      legend: {
        data: names.filter((_, i) => seriesData[i].length > 0),
        bottom: -5
      },
      series: series,
      grid: {
        left: '12%',
        right: '8%',
        top: '5%',
        bottom: '25%'
      }
    }

    grAcChart.value.setOption(option, true)
  }

  // GR-DEN交会图
  if (grDenChart.value && logData.value.GR && logData.value.DEN) {
    const data = []

    for (let i = 0; i < Math.min(logData.value.Depth.length, 1000); i++) {
      const gr = logData.value.GR[i]
      const den = logData.value.DEN[i]

      if (gr !== null && !isNaN(gr) && den !== null && !isNaN(den)) {
        data.push([gr, den])
      }
    }

    const option = {
      title: {
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 'bold' }
      },
      tooltip: {
        trigger: 'item',
        formatter: function(params) {
          return `GR: ${params.value[0].toFixed(1)} API<br>DEN: ${params.value[1].toFixed(3)} g/cm³`
        }
      },
      xAxis: {
        name: 'GR (API)',
        nameLocation: 'middle',
        nameGap: 25,
        nameTextStyle: { fontWeight: 'bold' },
        min: 0,
        max: 200
      },
      yAxis: {
        name: 'DEN (g/cm³)',
        nameLocation: 'middle',
        nameGap: 30,
        nameTextStyle: { fontWeight: 'bold' },
        min: 1.8,
        max: 3.0,
        inverse: true
      },
      series: [{
        type: 'scatter',
        data: data,
        symbolSize: 6,
        itemStyle: {
          color: '#3498db'
        },
        emphasis: {
          scale: true,
          scaleSize: 10
        }
      }],
      grid: {
        left: '12%',
        right: '8%',
        top: '5%',
        bottom: '25%'
      }
    }

    grDenChart.value.setOption(option, true)
  }
}

// 更新物性分析图表
const updatePhysicalCharts = (phiDEN, phiAC, depth) => {
  // 孔隙度分析图
  if (porosityChart.value) {
    const cnl = logData.value.CNL || []
    const series = []

    // 密度孔隙度
    if (phiDEN.length > 0) {
      const phiDENData = []
      for (let i = 0; i < depth.length; i++) {
        if (phiDEN[i] !== null) {
          phiDENData.push([phiDEN[i], depth[i]])
        }
      }

      if (phiDENData.length > 0) {
        series.push({
          name: 'φ_DEN',
          type: 'line',
          data: phiDENData,
          lineStyle: {
            color: '#e74c3c',
            width: 2
          },
          symbol: 'none',
          smooth: true,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: 'rgba(231, 76, 60, 0.1)' },
              { offset: 1, color: 'rgba(231, 76, 60, 0.05)' }
            ])
          }
        })
      }
    }

    // 声波孔隙度
    if (phiAC.length > 0) {
      const phiACData = []
      for (let i = 0; i < depth.length; i++) {
        if (phiAC[i] !== null) {
          phiACData.push([phiAC[i], depth[i]])
        }
      }

      if (phiACData.length > 0) {
        series.push({
          name: 'φ_AC',
          type: 'line',
          data: phiACData,
          lineStyle: {
            color: '#3498db',
            width: 2
          },
          symbol: 'none',
          smooth: true
        })
      }
    }

    // 中子孔隙度
    if (cnl.length > 0) {
      const cnlData = []
      for (let i = 0; i < depth.length; i++) {
        if (cnl[i] !== null && !isNaN(cnl[i])) {
          cnlData.push([cnl[i], depth[i]])
        }
      }

      if (cnlData.length > 0) {
        series.push({
          name: 'CNL',
          type: 'line',
          data: cnlData,
          lineStyle: {
            color: '#2ecc71',
            width: 2
          },
          symbol: 'none',
          smooth: true
        })
      }
    }

    const option = {
      title: {
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 'bold' }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        },
        formatter: function(params) {
          let result = `<div style="font-weight:bold;margin-bottom:5px">深度: ${params[0].value[1].toFixed(1)} m</div>`
          params.forEach(param => {
            result += `<div style="margin:3px 0">
              <span style="display:inline-block;width:10px;height:2px;background-color:${param.color};margin-right:5px"></span>
              ${param.seriesName}: <span style="font-weight:bold">${param.value[0].toFixed(2)} %</span>
            </div>`
          })
          return result
        }
      },
      xAxis: {
        name: '孔隙度 (%)',
        nameLocation: 'middle',
        nameGap: 25,
        nameTextStyle: { fontWeight: 'bold' },
        min: 0,
        max: 40
      },
      yAxis: {
        name: '深度 (m)',
        nameLocation: 'middle',
        nameGap: 40,
        nameTextStyle: { fontWeight: 'bold' },
        inverse: true
      },
      legend: {
        data: series.map(s => s.name),
        top: 10,
        right: 10
      },
      grid: {
        left: '15%',
        right: '10%',
        bottom: '15%',
        top: '20%'
      },
      series: series,
      dataZoom: [{
        type: 'inside',
        yAxisIndex: 0,
        start: 0,
        end: 100
      }]
    }

    porosityChart.value.setOption(option, true)
  }

  // CAL与GR分析图
  if (calGrChart.value && logData.value.CAL && logData.value.GR) {
    const calData = []
    const grData = []

    for (let i = 0; i < depth.length; i++) {
      const cal = logData.value.CAL[i]
      const gr = logData.value.GR[i]

      if (cal !== null && !isNaN(cal)) {
        calData.push([cal, depth[i]])
      }

      if (gr !== null && !isNaN(gr)) {
        grData.push([gr, depth[i]])
      }
    }

    const option = {
      title: {
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 'bold' }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          label: {
            backgroundColor: '#6a7985'
          }
        }
      },
      legend: {
        data: ['CAL', 'GR'],
        top: 10,
        right: 10
      },
      grid: {
        left: '10%',
        right: '10%',
        bottom: '15%',
        top: '20%'
      },
      xAxis: [
        {
          type: 'value',
          name: 'CAL (in)',
          nameLocation: 'middle',
          nameGap: 25,
          axisLine: {
            lineStyle: {
              color: '#3498db'
            }
          },
          axisLabel: {
            color: '#3498db'
          }
        },
        {
          type: 'value',
          name: 'GR (API)',
          nameLocation: 'middle',
          nameGap: 25,
          position: 'top',
          axisLine: {
            lineStyle: {
              color: '#e74c3c'
            }
          },
          axisLabel: {
            color: '#e74c3c'
          }
        }
      ],
      yAxis: {
        type: 'value',
        name: '深度 (m)',
        nameLocation: 'middle',
        nameGap: 40,
        inverse: true,
        axisLine: {
          show: true
        }
      },
      series: [
        {
          name: 'CAL',
          type: 'line',
          xAxisIndex: 0,
          data: calData,
          lineStyle: {
            color: '#3498db',
            width: 2
          },
          symbol: 'none',
          smooth: true
        },
        {
          name: 'GR',
          type: 'line',
          xAxisIndex: 1,
          data: grData,
          lineStyle: {
            color: '#e74c3c',
            width: 2
          },
          symbol: 'none',
          smooth: true
        }
      ]
    }

    calGrChart.value.setOption(option, true)
  }
}

// 更新含油气性分析图表
const updateHydrocarbonCharts = (phiDEN, depth) => {
  // Pickett图
  if (pickettChart.value && logData.value.RT) {
    const data = []
    const rt = logData.value.RT

    for (let i = 0; i < Math.min(phiDEN.length, 1000); i++) {
      const phi = phiDEN[i]
      const rtValue = rt[i]

      if (phi !== null && !isNaN(phi) && phi > 0 && phi < 40 &&
          rtValue !== null && !isNaN(rtValue) && rtValue > 0) {
        data.push([phi, rtValue])
      }
    }

    const option = {
      title: {
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 'bold' }
      },
      tooltip: {
        trigger: 'item',
        formatter: function(params) {
          return `孔隙度: ${params.value[0].toFixed(2)} %<br>RT: ${params.value[1].toFixed(2)} Ω·m`
        }
      },
      xAxis: {
        name: '孔隙度 (%)',
        nameLocation: 'middle',
        nameGap: 25,
        nameTextStyle: { fontWeight: 'bold' },
        type: 'value',
        min: 0,
        max: 40,
        splitNumber: 8
      },
      yAxis: {
        name: 'RT (Ω·m)',
        nameLocation: 'middle',
        nameGap: 30,
        nameTextStyle: { fontWeight: 'bold' },
        type: 'log',
        logBase: 10,
        axisLabel: {
          formatter: function(value) {
            return value.toFixed(1)
          }
        }
      },
      series: [{
        type: 'scatter',
        data: data,
        symbolSize: 6,
        itemStyle: {
          color: '#9b59b6'
        },
        emphasis: {
          scale: true,
          scaleSize: 10
        }
      }],
      grid: {
        left: '12%',
        right: '8%',
        top: '15%',
        bottom: '20%'
      }
    }

    pickettChart.value.setOption(option, true)
  }

  // 电阻率侵入分析
  if (resistivityChart.value && logData.value.RT && logData.value.RXO) {
    const rtData = []
    const rxoData = []

    for (let i = 0; i < depth.length; i++) {
      const rt = logData.value.RT[i]
      const rxo = logData.value.RXO[i]

      if (rt !== null && !isNaN(rt)) {
        rtData.push([rt, depth[i]])
      }

      if (rxo !== null && !isNaN(rxo)) {
        rxoData.push([rxo, depth[i]])
      }
    }

    const option = {
      title: {
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 'bold' }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        },
        formatter: function(params) {
          let result = `<div style="font-weight:bold;margin-bottom:5px">深度: ${params[0].value[1].toFixed(1)} m</div>`
          params.forEach(param => {
            const value = param.value[0] > 1000
              ? (param.value[0] / 1000).toFixed(2) + 'k'
              : param.value[0].toFixed(2)
            result += `<div style="margin:3px 0">
              <span style="display:inline-block;width:10px;height:2px;background-color:${param.color};margin-right:5px"></span>
              ${param.seriesName}: <span style="font-weight:bold">${value} Ω·m</span>
            </div>`
          })
          return result
        }
      },
      xAxis: {
        name: '电阻率 (Ω·m)',
        nameLocation: 'middle',
        nameGap: 25,
        nameTextStyle: { fontWeight: 'bold' },
        type: 'log',
        logBase: 10
      },
      yAxis: {
        name: '深度 (m)',
        nameLocation: 'middle',
        nameGap: 40,
        nameTextStyle: { fontWeight: 'bold' },
        inverse: true
      },
      legend: {
        data: ['RT', 'RXO'],
        top: 10
      },
      grid: {
        left: '15%',
        right: '10%',
        bottom: '15%',
        top: '20%'
      },
      series: [
        {
          name: 'RT',
          type: 'line',
          data: rtData,
          lineStyle: {
            color: '#9b59b6',
            width: 2
          },
          symbol: 'none',
          smooth: true
        },
        {
          name: 'RXO',
          type: 'line',
          data: rxoData,
          lineStyle: {
            color: '#1abc9c',
            width: 2,
            type: 'dashed'
          },
          symbol: 'none',
          smooth: true
        }
      ]
    }

    resistivityChart.value.setOption(option, true)
  }
}

// 更新综合交会图
const updateCrossplotCharts = (phiDEN, depth) => {
  // CNL-DEN交会图
  if (cnlDenChart.value && logData.value.CNL && logData.value.DEN) {
    const data = []

    for (let i = 0; i < Math.min(logData.value.Depth.length, 1000); i++) {
      const cnl = logData.value.CNL[i]
      const den = logData.value.DEN[i]

      if (cnl !== null && !isNaN(cnl) && den !== null && !isNaN(den)) {
        data.push([cnl, den])
      }
    }

    const option = {
      title: {
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 'bold' }
      },
      tooltip: {
        trigger: 'item',
        formatter: function(params) {
          return `CNL: ${params.value[0].toFixed(1)} %<br>DEN: ${params.value[1].toFixed(3)} g/cm³`
        }
      },
      xAxis: {
        name: 'CNL (%)',
        nameLocation: 'middle',
        nameGap: 25,
        nameTextStyle: { fontWeight: 'bold' },
        min: 0,
        max: 45
      },
      yAxis: {
        name: 'DEN (g/cm³)',
        nameLocation: 'middle',
        nameGap: 30,
        nameTextStyle: { fontWeight: 'bold' },
        min: 1.8,
        max: 3.0,
        inverse: true
      },
      series: [{
        type: 'scatter',
        data: data,
        symbolSize: 6,
        itemStyle: {
          color: '#e67e22'
        },
        emphasis: {
          scale: true,
          scaleSize: 10
        }
      }],
      grid: {
        left: '12%',
        right: '8%',
        top: '15%',
        bottom: '20%'
      }
    }

    cnlDenChart.value.setOption(option, true)
  }

  // 气层指示图
  if (gasIndicatorChart.value && phiDEN.length > 0 && logData.value.CNL) {
    const cnl = logData.value.CNL
    const gasIndicatorData = []
    const grData = []

    for (let i = 0; i < depth.length; i++) {
      const phi = phiDEN[i]
      const cnlValue = cnl[i]
      const gr = logData.value.GR[i]

      if (phi !== null && !isNaN(phi) && cnlValue !== null && !isNaN(cnlValue)) {
        const indicator = phi - cnlValue
        gasIndicatorData.push([indicator, depth[i]])
      }

      if (gr !== null && !isNaN(gr)) {
        grData.push([gr, depth[i]])
      }
    }

    const option = {
      title: {
        left: 'center',
        textStyle: { fontSize: 14, fontWeight: 'bold' }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        },
        formatter: function(params) {
          let result = `<div style="font-weight:bold;margin-bottom:5px">深度: ${params[0].value[1].toFixed(1)} m</div>`
          params.forEach(param => {
            const value = param.seriesName === 'GR'
              ? param.value[0].toFixed(1) + ' API'
              : param.value[0].toFixed(2)
            result += `<div style="margin:3px 0">
              <span style="display:inline-block;width:10px;height:2px;background-color:${param.color};margin-right:5px"></span>
              ${param.seriesName}: <span style="font-weight:bold">${value}</span>
            </div>`
          })
          return result
        }
      },
      legend: {
        data: ['气层指示', 'GR'],
        top: 10,
        right: 10
      },
      grid: {
        left: '10%',
        right: '10%',
        bottom: '15%',
        top: '20%'
      },
      xAxis: [
        {
          type: 'value',
          name: '气层指示 (φ_DEN - φ_CNL)',
          nameLocation: 'middle',
          nameGap: 25,
          axisLine: {
            lineStyle: {
              color: '#3498db'
            }
          },
          axisLabel: {
            color: '#3498db'
          }
        },
        {
          type: 'value',
          name: 'GR (API)',
          nameLocation: 'middle',
          nameGap: 25,
          position: 'top',
          axisLine: {
            lineStyle: {
              color: '#e74c3c'
            }
          },
          axisLabel: {
            color: '#e74c3c'
          }
        }
      ],
      yAxis: {
        type: 'value',
        name: '深度 (m)',
        nameLocation: 'middle',
        nameGap: 40,
        inverse: true,
        axisLine: {
          show: true
        }
      },
      series: [
        {
          name: '气层指示',
          type: 'line',
          xAxisIndex: 0,
          data: gasIndicatorData,
          lineStyle: {
            color: '#3498db',
            width: 2
          },
          symbol: 'none',
          smooth: true,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: 'rgba(52, 152, 219, 0.3)' },
              { offset: 1, color: 'rgba(52, 152, 219, 0.1)' }
            ])
          }
        },
        {
          name: 'GR',
          type: 'line',
          xAxisIndex: 1,
          data: grData,
          lineStyle: {
            color: '#e74c3c',
            width: 1.5
          },
          symbol: 'none',
          smooth: true
        }
      ]
    }

    gasIndicatorChart.value.setOption(option, true)
  }
}

// 调整分析图表大小
const resizeAnalysisCharts = () => {
  const charts = [
    grAcChart.value,
    grDenChart.value,
    porosityChart.value,
    calGrChart.value,
    pickettChart.value,
    resistivityChart.value,
    cnlDenChart.value,
    gasIndicatorChart.value
  ]

  charts.forEach(chart => {
    if (chart) {
      setTimeout(() => chart.resize(), 50)
    }
  })
}

// 解析CSV数据
const parseCSV = (csvText) => {


  const lines = csvText.trim().split('\n')


  if (lines.length < 2) {
    throw new Error('CSV文件行数太少，至少需要2行（标题行+数据行）')
  }

  const headers = lines[0].split(',').map(h => h.trim())


  // 查找关键字段的索引
  const depthIndex = headers.indexOf('Depth')
  const acIndex = headers.indexOf('AC')
  const calIndex = headers.indexOf('CAL')
  const cnlIndex = headers.indexOf('CNL')
  const denIndex = headers.indexOf('DEN')
  const grIndex = headers.indexOf('GR')
  const rtIndex = headers.indexOf('RT')
  const rxoIndex = headers.indexOf('RXO')

  
  if (depthIndex === -1) {
    throw new Error('CSV文件中未找到Depth字段')
  }

  // 获取井名和数据集名称
  let dataStartIndex = 1 // 跳过标题行

  // 检查第二行是否是单位行（包含"M,"等）
  if (lines.length > 1 && lines[1].includes('M,')) {

    dataStartIndex = 2
  }

  const data = {
    Depth: [],
    AC: [], CAL: [], CNL: [], DEN: [], GR: [], RT: [], RXO: []
  }

  let validDataCount = 0

  // 解析数据行
  for (let i = dataStartIndex; i < lines.length; i++) {
    const line = lines[i].trim()
    if (!line) continue // 跳过空行

    const values = line.split(',')

    // 尝试解析深度值
    let depthValue = null
    if (depthIndex < values.length) {
      depthValue = parseFloat(values[depthIndex])
    }

    if (isNaN(depthValue)) {

      continue // 跳过无效的深度行
    }

    data.Depth.push(depthValue)
    validDataCount++

    // 解析各测井曲线数据
    const parseField = (index) => {
      if (index !== -1 && index < values.length) {
        const val = parseFloat(values[index])
        return isNaN(val) ? null : val
      }
      return null
    }

    data.AC.push(parseField(acIndex))
    data.CAL.push(parseField(calIndex))
    data.CNL.push(parseField(cnlIndex))
    data.DEN.push(parseField(denIndex))
    data.GR.push(parseField(grIndex))
    data.RT.push(parseField(rtIndex))
    data.RXO.push(parseField(rxoIndex))
  }



  if (validDataCount === 0) {
    throw new Error('CSV文件中没有找到有效的数据点')
  }

  // 从第一行数据获取井名和数据集名称
  if (dataStartIndex < lines.length) {
    const firstDataValues = lines[dataStartIndex].split(',')
    if (firstDataValues.length >= 2) {
      currentWellName.value = firstDataValues[0] || '未知井'
      currentDatasetName.value = firstDataValues[1] || '未知数据集'
    }
  }

  return data
}

// 生成曲线配置
const generateCurveConfigs = (data) => {

  const configs = []

  // 为每个有数据的字段创建配置
  Object.keys(fieldMappings).forEach(field => {
    if (data[field] && data[field].length > 0) {
      // 过滤掉null值
      const fieldData = data[field].filter(v => v !== null)



      if (fieldData.length > 0) {
        const min = Math.min(...fieldData)
        const max = Math.max(...fieldData)
        const range = max - min

        // 设置合理的范围
        let axisMin = min
        let axisMax = max

        if (range > 0) {
          // 根据数据类型设置不同的扩展比例
          const expandRatio = (field === 'GR' || field === 'AC') ? 0.1 : 0.05
          axisMin = min - range * expandRatio
          axisMax = max + range * expandRatio

          // 确保最小值不为负数（对于某些测井数据）
          if (field === 'RT' || field === 'RXO' || field === 'DEN') {
            axisMin = Math.max(0, axisMin)
          }
        } else {
          // 如果所有值都相同，扩展一个小范围
          axisMin = min * 0.9
          axisMax = max * 1.1
        }

        // 为电阻率数据设置对数坐标
        const logScale = false

        configs.push({
          name: fieldMappings[field].name,
          unit: fieldMappings[field].unit,
          color: fieldMappings[field].color,
          data: data[field],
          min: axisMin,
          max: axisMax,
          axisIndex: configs.length,
          visible: true,
          logScale: logScale,
          originalMin: min,
          originalMax: max,
          field: field
        })

       
      }
    }
  })


  return configs
}

// 创建坐标轴配置
const createAxisConfigs = () => {
  const axes = []
  const series = []

  curveConfigs.value.forEach((config, index) => {
    // 创建X轴（测井曲线轴）
    const xAxis = {
      name: `${config.name} (${config.unit})`,
      nameLocation: 'middle',
      nameGap: 25,
      nameTextStyle: {
        color: config.color,
        fontWeight: 'bold',
        fontSize: 12
      },
      type: 'value',
      min: config.min,
      max: config.max,
      axisLine: {
        show: true,
        lineStyle: {
          color: config.color,
          width: 1.5
        }
      },
      axisTick: {
        show: true,
        length: 4,
        lineStyle: {
          color: config.color
        }
      },
      axisLabel: {
        show: true,
        color: config.color,
        fontSize: 10,
        margin: 2,
        formatter: function(value) {
          if (config.field === 'RT' || config.field === 'RXO') {
            // 电阻率值较大时使用更简洁的格式
            if (value >= 1000) {
              return (value / 1000).toFixed(1) + 'k'
            }
          }
          return value.toFixed(1)
        }
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: '#e0e0e0',
          width: 0.5,
          type: 'dashed'
        }
      },
      position: 'bottom',
      gridIndex: index,
      splitNumber: 4
    }

    axes.push(xAxis)

    // 创建系列数据
    const serieData = []
    for (let i = 0; i < logData.value.Depth.length; i++) {
      const value = config.data[i]
      if (value !== null && !isNaN(value)) {
        // 竖图中X是测井值，Y是深度
        serieData.push([value, logData.value.Depth[i]])
      }
    }

   

    // 创建系列
    const serie = {
      name: config.name,
      type: 'line',
      data: serieData,
      xAxisIndex: index,
      yAxisIndex: index,
      symbol: 'none',
      lineStyle: {
        color: config.color,
        width: 1.2
      },
      smooth: false,
      showSymbol: false,
      hoverAnimation: true,
      zlevel: 1,
      connectNulls: false
    }

    series.push(serie)
  })

  return { axes, series }
}

// 生成主图表配置
const createChartOption = () => {
  if (!logData.value || logData.value.Depth.length === 0) {
    return {}
  }

  const { axes, series } = createAxisConfigs()

  if (axes.length === 0 || series.length === 0) {
 
    return {}
  }

  // 计算需要多少个grid
  const gridCount = curveConfigs.value.length
  const gridWidth = 85 / gridCount // 每个grid的宽度百分比

  // 创建grid配置
  const grids = curveConfigs.value.map((config, index) => ({
    left: `${(index * gridWidth) + 5}%`,
    right: `${(100 - ((index + 1) * gridWidth)) - 5}%`,
    top: '8%',
    bottom: '12%',
    containLabel: true,
    borderWidth: 0
  }))

  // 获取深度范围
  const minDepth = Math.min(...logData.value.Depth)
  const maxDepth = Math.max(...logData.value.Depth)
  const depthRange = maxDepth - minDepth



  // 创建Y轴（深度轴）
  const yAxes = curveConfigs.value.map((config, index) => {
    // 计算合适的深度标注间隔
    let interval = 50 // 默认50米
    if (depthRange > 0) {
      if (depthRange > 500) interval = 100
      else if (depthRange > 200) interval = 50
      else if (depthRange > 100) interval = 25
      else if (depthRange > 50) interval = 10
      else interval = 5
    }

    return {
      type: 'value',
      min: minDepth,
      max: maxDepth,
      inverse: true,
      axisLine: {
        show: index === 0,
        lineStyle: {
          color: '#2c3e50',
          width: 1.5
        }
      },
      axisTick: {
        show: index === 0,
        length: 4
      },
      axisLabel: {
        show: index === 0,
        color: '#2c3e50',
        fontSize: 11,
        formatter: function(value) {
          const diff = value - minDepth
          if (Math.abs(diff % interval) < 0.1 ||
              Math.abs(value - maxDepth) < 0.1 ||
              Math.abs(value - minDepth) < 0.1) {
            return value.toFixed(0)
          }
          return ''
        }
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: '#e0e0e0',
          width: 1,
          type: 'dashed'
        }
      },
      position: 'left',
      gridIndex: index,
      scale: true,
      name: index === 0 ? '深度 (m)' : '',
      nameLocation: 'middle',
      nameGap: 40,
      nameTextStyle: {
        fontWeight: 'bold',
        color: '#2c3e50',
        fontSize: 12
      }
    }
  })

  const option = {
    animation: false,
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ddd',
      borderWidth: 1,
      textStyle: {
        color: '#333',
        fontSize: 12
      },
      formatter: function(params) {
        const depth = params[0].value[1]
        let result = `<div style="font-weight:bold;margin-bottom:5px;font-size:13px">深度: ${depth.toFixed(2)} m</div>`
        params.forEach(param => {
          const value = param.value[0]
          const config = curveConfigs.value.find(c => c.name === param.seriesName)
          const unit = config ? config.unit : ''

          if (value !== null && !isNaN(value)) {
            result += `<div style="margin:3px 0;font-size:12px">
                <span style="display:inline-block;width:10px;height:10px;background-color:${param.color};border-radius:50%;margin-right:5px"></span>
                ${param.seriesName}: <span style="font-weight:bold">${value.toFixed(2)} ${unit}</span>
              </div>`
          }
        })
        return result
      }
    },
    grid: grids,
    xAxis: axes,
    yAxis: yAxes,
    dataZoom: [
      {
        type: 'inside',
        yAxisIndex: Array.from({length: curveConfigs.value.length}, (_, i) => i),
        filterMode: 'none',
        zoomOnMouseWheel: true,
        moveOnMouseWheel: false
      }
    ],
    series: series,
    legend: {
      data: curveConfigs.value.map(c => c.name),
      right: 10,
      top: 10,
      textStyle: {
        color: '#2c3e50',
        fontSize: 11
      },
      selected: curveConfigs.value.reduce((obj, c) => {
        obj[c.name] = c.visible
        return obj
      }, {}),
      itemWidth: 18,
      itemHeight: 10,
      itemGap: 5
    }
  }

  return option
}

// ==================== 界面操作函数 ====================

// 切换分析图表显示
const toggleAnalysisCharts = () => {
  showAnalysisCharts.value = !showAnalysisCharts.value
}

// 切换分析标签
const switchAnalysisTab = (tabId) => {
  activeAnalysisTab.value = tabId
}

// 文件上传处理
const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (!file.name.toLowerCase().endsWith('.csv')) {
    alert('错误: 请选择CSV格式的文件！')
    event.target.value = ''
    return
  }

  isLoading.value = true

  try {
    const text = await file.text()
    const data = parseCSV(text)

    if (data.Depth.length === 0) {
      throw new Error('CSV文件中没有找到有效的数据！')
    }

    logData.value = data
    curveConfigs.value = generateCurveConfigs(data)

    if (curveConfigs.value.length === 0) {
      throw new Error('CSV文件中没有找到有效的测井曲线数据！')
    }

    // 更新文件信息
    fileInfo.value = {
      name: `文件: ${file.name}`,
      size: `井名: ${currentWellName.value}`,
      dataPoints: `数据点: ${data.Depth.length}个 | 曲线数: ${curveConfigs.value.length}条`
    }

    // 绘制主图表
    const option = createChartOption()
    if (Object.keys(option).length > 0) {
      myChart.setOption(option, true)
  
    } else {
      throw new Error('无法创建主图表配置')
    }

    // 调整图表大小
    setTimeout(() => {
      myChart.resize()
      if (showAnalysisCharts.value) {
        updateAnalysisCharts()
      }
    }, 100)

  } catch (error) {
    console.error('加载和绘制数据时出错:', error)
    alert(`加载数据时出错: ${error.message}`)
  } finally {
    isLoading.value = false
  }
}

// 切换曲线可见性
const toggleCurveVisibility = (config) => {
  config.visible = !config.visible
  if (myChart) {
    myChart.dispatchAction({
      type: 'legendToggleSelect',
      name: config.name
    })
  }
}

// 重置缩放
const resetZoom = () => {
  if (myChart && curveConfigs.value.length > 0) {
    myChart.dispatchAction({
      type: 'dataZoom',
      yAxisIndex: Array.from({length: curveConfigs.value.length}, (_, i) => i),
      start: 0,
      end: 100
    })
  }
}

// 重置所有数据
const resetAllData = () => {
  if (confirm('确定要清空所有数据并重置视图吗？')) {
    clearAllData()
  }
}

// 清空所有数据
const clearAllData = () => {
  // 重置响应式数据
  logData.value = null
  curveConfigs.value = []
  currentWellName.value = ''
  currentDatasetName.value = ''
  fileInfo.value = {
    name: '',
    size: '',
    dataPoints: ''
  }
  showAnalysisCharts.value = false

  // 清空文件输入框
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }

  // 销毁所有图表实例
  destroyAnalysisCharts()

  // 重置主图表
  if (myChart) {
    myChart.clear()
    initChart()
  }
}

// 导出图表图像
const exportChartImage = () => {
  if (!logData.value) {
    alert('请先加载数据！')
    return
  }

  const imgData = myChart.getDataURL({
    type: 'png',
    pixelRatio: 3,
    backgroundColor: '#fff'
  })

  const link = document.createElement('a')
  link.href = imgData
  link.download = `测井曲线_${currentWellName.value}_${new Date().getTime()}.png`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 窗口大小变化处理
const handleResize = () => {
  const charts = [
    myChart,
    grAcChart.value,
    grDenChart.value,
    porosityChart.value,
    calGrChart.value,
    pickettChart.value,
    resistivityChart.value,
    cnlDenChart.value,
    gasIndicatorChart.value
  ]

  charts.forEach(chart => {
    if (chart) {
      setTimeout(() => chart.resize(), 50)
    }
  })
}

// ==================== 生命周期钩子 ====================
onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)

  // 销毁所有图表实例
  destroyAnalysisCharts()

  if (myChart) {
    myChart.dispose()
    myChart = null
  }
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
}

.container {
  display: flex;
  flex: 1;
  gap: 25px;
  max-width: 1800px;
  margin: 0 auto;
  width: 100%;
  min-height: 700px;
}

.chart-container {
  flex: 1;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  padding: 20px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0; /* 防止flex项目溢出 */
}

.chart-title {
  font-size: 1.4rem;
  color: #2c3e50;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eaeaea;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.chart-title::before {
  content: "📈";
  margin-right: 10px;
  font-size: 1.2rem;
}

.log-chart {
  flex: 1;
  min-height: 600px;
  width: 100%;
  position: relative;
}

.chart-toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  flex-wrap: wrap;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.toolbar-btn {
  padding: 8px 16px;
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.toolbar-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.toolbar-btn:active {
  transform: translateY(0);
}

.icon-reset::before { content: "🔄"; }
.icon-chart::before { content: "📊"; }
.icon-export::before { content: "💾"; }

/* 分析图表区域 */
.analysis-charts {
  margin-top: 20px;
  border-top: 1px solid #eaeaea;
  padding-top: 15px;
  width: 100%;
  min-height: 500px;
  display: flex;
  flex-direction: column;
}

.analysis-tabs {
  display: flex;
  gap: 5px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  background: #f8f9fa;
  padding: 8px;
  border-radius: 8px;
}

.analysis-tab {
  padding: 10px 20px;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
  color: #546e7a;
  transition: all 0.3s ease;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.analysis-tab:hover {
  background-color: #f0f7ff;
  border-color: #3498db;
  color: #3498db;
}

.analysis-tab.active {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border-color: #2980b9;
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.2);
}

.analysis-content {
  flex: 1;
  width: 100%;
  min-height: 400px;
  padding: 10px 0;
}

.analysis-section {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.analysis-section h3 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 1.3rem;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 10px;
  border-bottom: 2px solid #f0f0f0;
}

.icon-lithology::before { content: "🪨"; }
.icon-physical::before { content: "📏"; }
.icon-hydrocarbon::before { content: "⛽"; }
.icon-crossplot::before { content: "📈"; }

.analysis-charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
  flex: 1;
  min-height: 400px;
}

@media (max-width: 1200px) {
  .analysis-charts-grid {
    grid-template-columns: 1fr;
  }
}

.chart-wrapper {
  background-color: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 350px;
  transition: transform 0.3s ease;
}

.chart-wrapper:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
}

.chart-title-sm {
  font-size: 1.1rem;
  color: #2c3e50;
  margin-bottom: 15px;
  text-align: center;
  font-weight: 600;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.analysis-chart {
  flex: 1;
  width: 100%;
  min-height: 250px;
  position: relative;
}

.chart-description {
  margin-top: 15px;
  font-size: 0.85rem;
  color: #546e7a;
  line-height: 1.5;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.chart-description p {
  margin: 5px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 5px;
}

.dot.mudstone { background-color: #8e44ad; }
.dot.sandstone { background-color: #3498db; }
.dot.carbonate { background-color: #e67e22; }
.dot.dolomite { background-color: #f39c12; }
.dot.oil { background-color: #2ecc71; }
.dot.water { background-color: #3498db; }
.dot.gas { background-color: #e74c3c; }

.line {
  display: inline-block;
  width: 20px;
  height: 3px;
  margin-right: 5px;
}

.line.den-porosity { background-color: #e74c3c; }
.line.ac-porosity { background-color: #3498db; }
.line.cnl { background-color: #2ecc71; }
.line.cal { background-color: #3498db; }
.line.gr { background-color: #e74c3c; }
.line.rt { background-color: #9b59b6; }
.line.rxo { background-color: #1abc9c; }

/* 控制面板 */
.controls {
  width: 320px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 25px;
  min-width: 300px;
}

.upload-section {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 10px;
  padding: 20px;
  border: 2px dashed #c5d9f1;
  transition: all 0.3s ease;
  position: relative;
}

.upload-section:hover {
  border-color: #3498db;
  background: linear-gradient(135deg, #f0f7ff, #e3f2fd);
  transform: translateY(-2px);
}

.upload-section h3 {
  color: #2c3e50;
  margin-bottom: 15px;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-input-wrapper {
  position: relative;
  margin-bottom: 15px;
}

.file-input {
  position: absolute;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: 2;
}

.file-label {
  display: block;
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  padding: 12px 20px;
  border-radius: 8px;
  text-align: center;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.file-label:hover {
  background: linear-gradient(135deg, #2980b9, #1c6ea4);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.file-info {
  font-size: 0.9rem;
  color: #7f8c8d;
  text-align: center;
  margin-top: 10px;
}

.selected-file {
  background: linear-gradient(135deg, #e8f4fc, #d6eaf8);
  border-radius: 8px;
  padding: 15px;
  margin-top: 15px;
  border-left: 4px solid #3498db;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.selected-file h4 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.selected-file p {
  font-size: 0.85rem;
  color: #546e7a;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.control-section {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 10px;
  padding: 20px;
  border: 1px solid #e0e0e0;
}

.control-section h3 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e0e0e0;
}

/* 参数控制 */
.param-control {
  margin-bottom: 15px;
}

.param-control label {
  display: block;
  color: #546e7a;
  font-size: 0.9rem;
  margin-bottom: 8px;
  font-weight: 500;
}

.param-input-wrapper {
  position: relative;
}

.param-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  background: white;
}

.param-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.param-hint {
  display: block;
  font-size: 0.8rem;
  color: #7f8c8d;
  margin-top: 5px;
}

.curve-control {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 10px;
  border-radius: 6px;
  transition: all 0.2s;
  background: white;
  border: 1px solid transparent;
}

.curve-control:hover {
  background-color: #f8f9fa;
  border-color: #e0e0e0;
  transform: translateX(5px);
}

.curve-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  margin-right: 12px;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  flex-shrink: 0;
}

.curve-control label {
  flex: 1;
  color: #546e7a;
  font-size: 0.95rem;
  cursor: pointer;
  font-weight: 500;
}

.checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
  flex-shrink: 0;
  accent-color: #3498db;
}

.btn {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border: none;
  padding: 12px 18px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  transition: all 0.3s ease;
  margin-top: 5px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn:hover {
  background: linear-gradient(135deg, #2980b9, #1c6ea4);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.btn:active {
  transform: translateY(0);
}

.btn-secondary {
  background: linear-gradient(135deg, #7f8c8d, #6c7a89);
}

.btn-secondary:hover {
  background: linear-gradient(135deg, #6c7a89, #5d6d7e);
  box-shadow: 0 4px 12px rgba(127, 140, 141, 0.3);
}

.btn-danger {
  background: linear-gradient(135deg, #e74c3c, #c0392b);
}

.btn-danger:hover {
  background: linear-gradient(135deg, #c0392b, #a93226);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
}

.data-info {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 10px;
  padding: 20px;
  font-size: 0.9rem;
  color: #546e7a;
  line-height: 1.6;
  border: 1px solid #e0e0e0;
}

.data-info h4 {
  color: #2c3e50;
  margin-bottom: 12px;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.data-info p {
  margin-bottom: 8px;
  padding-left: 5px;
}

.data-info strong {
  color: #2c3e50;
  display: inline-block;
  width: 50px;
}



.loading {
  display: none;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.95);
  z-index: 1000;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
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
  color: #3498db;
  font-weight: 500;
  font-size: 1.1rem;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #7f8c8d;
  text-align: center;
  padding: 40px;
  animation: fadeIn 0.8s ease;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.7;
}

.empty-state h3 {
  margin-bottom: 10px;
  color: #2c3e50;
  font-size: 1.6rem;
}

.empty-state p {
  margin-bottom: 15px;
  max-width: 500px;
  line-height: 1.6;
}

.empty-tip {
  font-size: 0.9rem;
  color: #95a5a6;
  background: #f8f9fa;
  padding: 10px 15px;
  border-radius: 8px;
  border-left: 4px solid #3498db;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .container {
    flex-direction: column;
  }

  .controls {
    width: 100%;
    min-width: auto;
  }

  .log-chart {
    min-height: 500px;
  }

  .analysis-charts-grid {
    grid-template-columns: 1fr;
  }

  .chart-wrapper {
    min-height: 300px;
  }
}

@media (max-width: 768px) {
  .container {
    gap: 15px;
  }

  .chart-container, .controls {
    padding: 15px;
  }

  .log-chart {
    min-height: 400px;
  }

  .analysis-tabs {
    flex-wrap: wrap;
  }

  .analysis-tab {
    flex: 1;
    min-width: 120px;
    text-align: center;
    font-size: 0.85rem;
    padding: 8px 12px;
  }

  .header h1 {
    font-size: 1.8rem;
  }

  .header p {
    font-size: 1rem;
  }
}

/* ==================== 报告相关样式 ==================== */

.report-section {
  margin-top: 20px;
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 10px;
  padding: 20px;
  border: 1px solid #e0e0e0;
}

.btn-knowledge {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  font-weight: 600;
  font-size: 1rem;
  padding: 14px 20px;
  width: 100%;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.btn-knowledge:hover {
  background: linear-gradient(135deg, #8e44ad, #7d3c98);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(155, 89, 182, 0.3);
}

.btn-icon {
  font-size: 1.2rem;
}

/* 报告弹窗样式 */
.report-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  padding: 20px;
}

.report-modal.active {
  opacity: 1;
  visibility: visible;
}

.report-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 1000px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.report-header {
  background: linear-gradient(135deg, #2c3e50, #3498db);
  color: white;
  padding: 20px 30px;
  border-radius: 12px 12px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.report-header h2 {
  margin: 0;
  font-size: 1.8rem;
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  font-size: 2rem;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 2.5rem;
  cursor: pointer;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

.report-body {
  flex: 1;
  overflow-y: auto;
  padding: 30px;
  background: #f9f9f9;
}

.report-footer {
  padding: 20px 30px;
  background: #f8f9fa;
  border-radius: 0 0 12px 12px;
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  border-top: 1px solid #e0e0e0;
}

/* 报告内容样式 */
.knowledge-report {
  font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
  color: #333;
}

.report-summary {
  background: white;
  border-radius: 10px;
  padding: 25px;
  margin-bottom: 30px;
  border-left: 5px solid #3498db;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.report-summary h3 {
  color: #2c3e50;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-stats {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.stat-item {
  flex: 1;
  text-align: center;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: bold;
  color: #3498db;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 0.9rem;
  color: #7f8c8d;
}

.section {
  background: white;
  border-radius: 10px;
  padding: 25px;
  margin-bottom: 30px;
  border: 1px solid #e0e0e0;
}

.section h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.section-desc {
  color: #546e7a;
  font-size: 1rem;
  margin-bottom: 25px;
  padding-left: 10px;
  font-style: italic;
}

.chart-explain {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #3498db;
}

.chart-explain h4 {
  color: #2c3e50;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e0e0e0;
}

.explain-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.explain-item {
  background: white;
  padding: 15px;
  border-radius: 6px;
  border: 1px solid #e0e0e0;
}

.item-title {
  font-weight: bold;
  color: #3498db;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.explain-item ul {
  margin: 0;
  padding-left: 20px;
}

.explain-item li {
  margin-bottom: 8px;
  line-height: 1.5;
  color: #546e7a;
}

/* 颜色点样式 */
.color-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
  vertical-align: middle;
}

.color-dot.mudstone { background-color: #8e44ad; }
.color-dot.sandstone { background-color: #3498db; }
.color-dot.carbonate { background-color: #e67e22; }
.color-dot.dolomite { background-color: #f39c12; }
.color-dot.oil { background-color: #2ecc71; }
.color-dot.water { background-color: #3498db; }
.color-dot.gas { background-color: #e74c3c; }

/* 线条样式 */
.line {
  display: inline-block;
  width: 20px;
  height: 3px;
  margin-right: 8px;
  vertical-align: middle;
}

.line.den-porosity { background-color: #e74c3c; }
.line.ac-porosity { background-color: #3498db; }
.line.cnl { background-color: #2ecc71; }
.line.cal { background-color: #3498db; }
.line.gr { background-color: #e74c3c; }
.line.rt { background-color: #9b59b6; }
.line.rxo { background-color: #1abc9c; }

/* 徽章样式 */
.badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
  margin-right: 5px;
}

.badge.success { background: #d4edda; color: #155724; }
.badge.warning { background: #fff3cd; color: #856404; }
.badge.info { background: #d1ecf1; color: #0c5460; }
.badge.danger { background: #f8d7da; color: #721c24; }

/* 工作流程样式 */
.workflow-steps {
  display: grid;
  gap: 20px;
  margin: 30px 0;
}

.step {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #3498db;
  transition: all 0.3s ease;
}

.step:hover {
  transform: translateX(5px);
  background: #e3f2fd;
}

.step-number {
  background: #3498db;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  font-weight: bold;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
}

.step-content h4 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.step-content ul {
  margin: 0;
  padding-left: 20px;
}

.step-content li {
  margin-bottom: 5px;
  color: #546e7a;
}

/* 案例研究样式 */
.case-study {
  background: #e8f4fc;
  border-radius: 8px;
  padding: 20px;
  margin-top: 30px;
  border-left: 4px solid #2ecc71;
}

.case-study h4 {
  color: #2c3e50;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.case-details {
  background: white;
  padding: 15px;
  border-radius: 6px;
}

.case-details p {
  margin-bottom: 10px;
  color: #546e7a;
}

.case-details ol {
  margin: 10px 0;
  padding-left: 25px;
}

.case-details li {
  margin-bottom: 8px;
  color: #546e7a;
}

/* 报告脚注 */
.report-footer-note {
  margin-top: 30px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #95a5a6;
  font-size: 0.9rem;
  color: #7f8c8d;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .report-content {
    max-height: 85vh;
    margin: 10px;
  }

  .report-header h2 {
    font-size: 1.5rem;
  }

  .explain-grid {
    grid-template-columns: 1fr;
  }

  .summary-stats {
    flex-direction: column;
  }

  .workflow-steps {
    grid-template-columns: 1fr;
  }

  .step {
    flex-direction: column;
    gap: 15px;
  }

  .step-number {
    align-self: center;
  }
}

@media (max-width: 480px) {
  .report-body {
    padding: 15px;
  }

  .report-header {
    padding: 15px 20px;
  }

  .report-footer {
    padding: 15px 20px;
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>