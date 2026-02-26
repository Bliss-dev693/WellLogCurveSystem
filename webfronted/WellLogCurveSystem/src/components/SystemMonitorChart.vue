<template>
  <!-- 2×2卡片布局，适配原快捷操作区域 -->
  <div class="system-monitor-container">
    <!-- 卡片1：CPU使用率 -->
    <el-card class="monitor-card">
      <template #header>
        <div class="card-header">
          <span>CPU监控</span>
        </div>
      </template>
      <div ref="cpuChartRef" class="chart-content"></div>
    </el-card>

    <!-- 卡片2：JVM内存使用 -->
    <el-card class="monitor-card">
      <template #header>
        <div class="card-header">
          <span>JVM内存监控</span>
        </div>
      </template>
      <div ref="jvmChartRef" class="chart-content"></div>
    </el-card>

    <!-- 卡片3：磁盘使用率 -->
    <el-card class="monitor-card">
      <template #header>
        <div class="card-header">
          <span>磁盘监控</span>
        </div>
      </template>
      <div ref="diskChartRef" class="chart-content"></div>
    </el-card>

    <!-- 卡片4：系统概览（数字面板） -->
    <el-card class="monitor-card">
      <template #header>
        <div class="card-header">
          <span>系统概览</span>
        </div>
      </template>
      <div class="system-info-content">
        <div class="info-item">
          <label>操作系统</label>
          <span>{{ systemInfo.osName }} ({{ systemInfo.osArch }})</span>
        </div>
        <div class="info-item">
          <label>Java版本</label>
          <span>{{ systemInfo.javaVersion }}</span>
        </div>
        <div class="info-item">
          <label>运行时长</label>
          <span>{{ systemInfo.upTime }}</span>
        </div>
        <div class="info-item">
          <label>线程数</label>
          <span>{{ jvmInfo.threadCount }} (守护线程：{{ jvmInfo.daemonThreadCount }})</span>
        </div>
        <div class="info-item">
          <label>GC次数/耗时</label>
          <span>{{ jvmInfo.gcCount }} 次 / {{ jvmInfo.gcTime }} ms</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getSystemMonitorData } from '@/api/systemMonitor'

// 1. 定义DOM引用（用于挂载ECharts）
const cpuChartRef = ref(null)
const jvmChartRef = ref(null)
const diskChartRef = ref(null)

// 2. 定义变量存储监控数据
const systemInfo = ref({
  osName: '-',
  osArch: '-',
  javaVersion: '-',
  upTime: '-'
})
const jvmInfo = ref({
  threadCount: 0,
  daemonThreadCount: 0,
  gcCount: 0,
  gcTime: 0
})
const cpuInfo = ref({
  cpuCoreNum: 0,
  cpuUsage: 0
})
const memoryInfo = ref({
  heapUsedMB: 0,
  heapMaxMB: 0,
  nonHeapUsedMB: 0,
  heapUsage: 0
})
const diskInfoList = ref([])

// 3. 定义ECharts实例变量（用于销毁）
let cpuChart = null
let jvmChart = null
let diskChart = null

// 4. 获取系统监控数据
const fetchMonitorData = async () => {
  try {
    const res = await getSystemMonitorData()
    const data = res.data.data
    // 更新数据
    systemInfo.value = data.systemInfo
    jvmInfo.value = data.jvmInfo
    cpuInfo.value = data.cpuInfo
    memoryInfo.value = data.memoryInfo
    diskInfoList.value = data.diskInfoList
    // 渲染图表（数据更新后重新渲染）
    renderCpuChart()
    renderJvmChart()
    renderDiskChart()
  } catch (err) {
    console.error('获取系统监控数据失败：', err)
  }
}

// 5. 渲染CPU图表（环形图：展示使用率）
const renderCpuChart = () => {
  if (!cpuChartRef.value) return
  // 初始化ECharts实例
  cpuChart = echarts.init(cpuChartRef.value)
  // 配置项
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}% ({d}%)'
    },
    series: [
      {
        name: 'CPU使用率',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        label: {
          show: true,
          position: 'center',
          formatter: `${cpuInfo.value.cpuUsage}%`,
          fontSize: 18,
          fontWeight: 'bold'
        },
        labelLine: {
          show: false
        },
        data: [
          {
            name: '已使用',
            value: cpuInfo.value.cpuUsage,
            itemStyle: { color: cpuInfo.value.cpuUsage > 80 ? '#f56c6c' : cpuInfo.value.cpuUsage > 50 ? '#e6a23c' : '#67c23a' }
          },
          {
            name: '空闲',
            value: 100 - cpuInfo.value.cpuUsage,
            itemStyle: { color: '#e5e9f2' }
          }
        ]
      }
    ]
  }
  // 设置配置项并渲染
  cpuChart.setOption(option)
}

// 6. 渲染JVM内存图表（柱状图：堆内存使用）
const renderJvmChart = () => {
  if (!jvmChartRef.value) return
  jvmChart = echarts.init(jvmChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['堆内存已用', '堆内存最大', '非堆内存已用']
    },
    yAxis: {
      type: 'value',
      name: 'MB'
    },
    series: [
      {
        name: '内存大小(MB)',
        type: 'bar',
        data: [
          {
            value: memoryInfo.value.heapUsedMB,
            itemStyle: { color: memoryInfo.value.heapUsage > 80 ? '#f56c6c' : memoryInfo.value.heapUsage > 50 ? '#e6a23c' : '#67c23a' }
          },
          { value: memoryInfo.value.heapMaxMB, itemStyle: { color: '#909399' } },
          { value: memoryInfo.value.nonHeapUsedMB, itemStyle: { color: '#409eff' } }
        ]
      }
    ]
  }
  jvmChart.setOption(option)
}

// 7. 渲染磁盘图表（横向柱状图：各磁盘使用率）
const renderDiskChart = () => {
  if (!diskChartRef.value) return
  diskChart = echarts.init(diskChartRef.value)
  // 提取磁盘路径和使用率
  const diskPaths = diskInfoList.value.map(item => item.diskPath)
  const diskUsages = diskInfoList.value.map(item => ({
    value: item.usage,
    itemStyle: {
      color: item.usage > 90 ? '#f56c6c' : item.usage > 70 ? '#e6a23c' : '#67c23a'
    }
  }))
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b} <br/>使用率：{c}% <br/>已用：{d}GB / 总：{e}GB',
      // 自定义tooltip展示更多磁盘信息
      formatter: (params) => {
        const disk = diskInfoList.value.find(item => item.diskPath === params[0].name)
        return `${disk.diskPath} <br/>使用率：${disk.usage}% <br/>已用：${disk.usedSpaceGB}GB / 总：${disk.totalSpaceGB}GB`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      max: 100,
      name: '使用率(%)'
    },
    yAxis: {
      type: 'category',
      data: diskPaths
    },
    series: [
      {
        name: '磁盘使用率',
        type: 'bar',
        data: diskUsages
      }
    ]
  }
  diskChart.setOption(option)
}

// 8. 图表自适应（窗口变化时重绘）
const resizeCharts = () => {
  cpuChart && cpuChart.resize()
  jvmChart && jvmChart.resize()
  diskChart && diskChart.resize()
}

// 9. 生命周期钩子
onMounted(() => {
  // 初始加载数据
  fetchMonitorData()
  // 定时刷新（每10秒更新一次监控数据）
  const timer = setInterval(fetchMonitorData, 10000)
  // 监听窗口大小变化
  window.addEventListener('resize', resizeCharts)
  // 保存定时器（用于销毁）
  ref('timer', timer)
})

onUnmounted(() => {
  // 销毁ECharts实例（防止内存泄漏）
  cpuChart && cpuChart.dispose()
  jvmChart && jvmChart.dispose()
  diskChart && diskChart.dispose()
  // 清除定时器
  clearInterval(ref('timer'))
  // 移除窗口监听
  window.removeEventListener('resize', resizeCharts)
})
</script>

<style scoped>
/* 容器样式：2×2网格布局，适配原快捷操作区域 */
.system-monitor-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 8px 0;
}

/* 卡片样式 */
.monitor-card {
  height: 300px;
}

/* 卡片头部 */
.card-header {
  font-weight: bold;
  font-size: 14px;
  color: #303133;
}

/* 图表容器 */
.chart-content {
  width: 100%;
  height: 240px;
}

/* 系统概览面板样式 */
.system-info-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px;
  height: 240px;
  overflow: auto;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px solid #f0f2f5;
}

.info-item label {
  color: #606266;
  font-size: 13px;
}

.info-item span {
  color: #303133;
  font-size: 13px;
  font-weight: 500;
}
</style>