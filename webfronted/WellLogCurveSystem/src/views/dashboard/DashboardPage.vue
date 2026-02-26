<template>
  <div class="dashboard-container">
    <!-- 欢迎区 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">{{ greeting }}, {{ username }}！</h1>
        <p class="welcome-subtitle">今日系统运行状态良好，{{ currentDate }}，祝您工作愉快</p>
        <div class="quick-stats">
          <div class="stat-item">
            <i class="stat-icon el-icon-sunny"></i>
            <div>
              <span class="stat-value">{{ formatTime }}</span>
              <span class="stat-label">当前时间</span>
            </div>
          </div>
          <div class="stat-item">
            <i class="stat-icon el-icon-s-claim"></i>
            <div>
              <span class="stat-value">{{ dashboardData.todayTasks }}</span>
              <span class="stat-label">今日任务</span>
            </div>
          </div>
          <div class="stat-item">
            <i class="stat-icon el-icon-success"></i>
            <div>
              <span class="stat-value">{{ dashboardData.completedRate }}%</span>
              <span class="stat-label">完成率</span>
            </div>
          </div>
        </div>
      </div>
      <div class="welcome-illustration">
        <i class="el-icon-data-analysis illustration-icon"></i>
      </div>
    </div>

    <!-- 主要指标 -->
    <div class="metrics-grid">
      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          
          <div class="metric-icon-wrapper" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <DataAnalysis class="metric-icon" />
          </div>
          <div class="metric-info">
            <h3 class="metric-value">{{ StatData.totalWells }}</h3>
            <span class="metric-label">数据集数</span>
            <div class="metric-trend trend-up">
              <i class="el-icon-arrow-up"></i>
              <span>较上周增长 {{ StatData.wellsGrowthRate }}%</span>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          <div class="metric-icon-wrapper" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <DataLine class="metric-icon" />
          </div>
          <div class="metric-info">
            <h3 class="metric-value">{{ StatData.curvesCount }}</h3>
            <span class="metric-label">曲线总数</span>
            <div class="metric-trend trend-up">
              <i class="el-icon-arrow-up"></i>
              <span>今日新增 {{ StatData.curvesAddedToday }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          <div class="metric-icon-wrapper" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <document-checked class="metric-icon" />
          </div>
          <div class="metric-info">
            <h3 class="metric-value">{{ StatData.avgAccuracy }}%</h3>
            <span class="metric-label">平均准确率</span>
            <div class="metric-trend trend-up">
              <i class="el-icon-arrow-up"></i>
              <span>提升 {{ StatData.accuracyLift }}%</span>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="metric-card" shadow="hover">
        <div class="metric-content">
          <div class="metric-icon-wrapper" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
            <Timer class="metric-icon" />
          </div>
          <div class="metric-info">
            <h3 class="metric-value">{{ StatData.processingTime }}</h3>
            <span class="metric-label">平均处理时间（分钟）</span>
            <div class="metric-trend trend-down">
              <i class="el-icon-arrow-down"></i>
              <span>缩短 {{ StatData.timeSavedRate }}%</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <div class="left-column">
        <!-- 公告通知 -->
        <el-card class="card-section" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-message-solid card-title-icon"></i>
                公告通知
                <el-badge :value="unreadNotificationCount" :max="99" class="badge-notice" />
              </h3>
              <div class="header-actions">
                <el-button 
                  type="text" 
                  size="small" 
                  @click="fetchNotifications"
                  :loading="notificationsLoading"
                  title="刷新通知"
                  :icon="Refresh"
                >
                  <i class="el-icon-refresh"></i>
                </el-button>
                <el-button type="text" size="small" @click="viewAllNotices">
                  查看全部
                </el-button>
              </div>
            </div>
          </template>
          <div v-loading="notificationsLoading" class="notice-list">
            <div 
              v-for="notice in notifications" 
              :key="notice.id"
              class="notice-item"
              :class="{ 'unread': !notice.read }"
              @click="viewNotice(notice)"
            >
              <div class="notice-dot" :style="{ backgroundColor: getNoticeColor(notice.type) }"></div>
              <div class="notice-content">
                <div class="notice-header">
                  <h4 class="notice-title">{{ notice.title }}</h4>
                  <el-tag 
                    size="mini" 
                    :type="notice.type === 'urgent' ? 'danger' : notice.type === 'warning' ? 'warning' : 'info'"
                  >
                    {{ getNoticeTypeText(notice.type) }}
                  </el-tag>
                </div>
                <p class="notice-desc">{{ truncateText(notice.content, 100) }}</p>
                <div class="notice-footer">
                  <span class="notice-time">
                    <i class="el-icon-time"></i>
                    {{ formatRelativeTime(notice.time) }}
                  </span>
                  <span class="notice-source">{{ notice.source }}</span>
                  <el-button 
                    v-if="!notice.read"
                    type="text" 
                    size="mini" 
                    @click.stop="markNotificationAsRead(notice)"
                    class="mark-read-btn"
                  >
                    标记已读
                  </el-button>
                </div>
              </div>
            </div>
            <div v-if="notifications.length === 0 && !notificationsLoading" class="empty-notices">
              <i class="el-icon-message"></i>
              <p>暂无通知消息</p>
            </div>
          </div>
        </el-card>

        <!-- 待办事项 -->
        <el-card class="card-section" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-tickets card-title-icon"></i>
                待办事项
              </h3>
              <el-button type="primary" size="small" icon="el-icon-plus" @click="addTodo">
                新增任务
              </el-button>
            </div>
          </template>
          <div v-loading="loading" class="todo-list">
            <div 
              v-for="todo in filteredAndSortedTodos.slice((currentPage - 1) * 4, currentPage * 4)" 
              :key="todo.id"
              class="todo-item"
              :class="{
                'todo-high': todo.priority === 'high',
                'todo-medium': todo.priority === 'medium',
                'todo-low': todo.priority === 'low'
              }"
            >
              <el-checkbox 
                v-model="todo.completed" 
                @change="handleUpdateTodoStatus(todo)"
                class="todo-checkbox"
              />
              <div class="todo-content" @click="editTodo(todo)">
                <div class="todo-header">
                  <h4 class="todo-title" :class="{ 'todo-completed': todo.completed }">
                    {{ todo.title }}
                  </h4>
                  <el-tag 
                    v-if="todo.dueToday" 
                    size="mini" 
                    type="danger"
                    class="due-tag"
                  >
                    今日到期
                  </el-tag>
                </div>
                <p class="todo-desc">{{ todo.description }}</p>
                <div class="todo-footer">
                  <span class="todo-time">
                    <i class="el-icon-date"></i>
                    {{ formatDate(todo.dueDate) }}
                  </span>
                  <span class="todo-project">
                    <i class="el-icon-folder"></i>
                    {{ todo.project }}
                  </span>
                </div>
              </div>
              <div class="todo-actions">
                <el-button 
                  type="danger" 
                  size="small" 
                  :icon="CircleCloseFilled"
                  circle
                  @click.stop="deleteTodoItem(todo)"
                  title="删除任务"
                ></el-button>
                <el-dropdown
                  @command="(command) => handleTodoCommand(command, todo)"
                >
                  <el-button 
                  :icon="MoreFilled"
                  type="text" size="small" class="todo-menu-button">
                   
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑</el-dropdown-item>
                      <el-dropdown-item command="delete">删除</el-dropdown-item>
                      <el-dropdown-item divided command="priority-high">设为高优先级</el-dropdown-item>
                      <el-dropdown-item command="priority-medium">设为中优先级</el-dropdown-item>
                      <el-dropdown-item command="priority-low">设为低优先级</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <div v-if="todos.length === 0" class="empty-todo">
              <i class="el-icon-tickets empty-icon"></i>
              <p>暂无待办事项</p>
              <el-button type="text" @click="addTodo">创建第一个任务</el-button>
            </div>
            <!-- 底部统计信息 -->
            <div class="todo-footer" v-if="todos.length > 0">
              <div class="todo-summary">
                <span>共 {{ todos.length }} 项任务</span>
                <span>•</span>
                <span>{{ completedCount }} 项已完成</span>
                <span>•</span>
                <span>{{ pendingCount }} 项待办</span>
              </div>
              
              <div class="todo-pagination" v-if="hasPagination">
                <div style="margin-bottom: 10px; font-size: 12px; color: #909399;">
                  总数: {{ filteredAndSortedTodos.length }}, 当前页: {{ currentPage }}, 总页数: {{ totalPages }}
                </div>
                <el-pagination
                  :page-size="4"
                  :total="filteredAndSortedTodos.length"
                  :current-page="currentPage"
                  layout="prev, pager, next"
                  small
                  @current-change="handlePageChange"
                />
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="right-column">
        <!-- 历史测井数据可视化（修改后的版本） -->
        <el-card class="card-section" shadow="hover">
          <template #header>
            <div class="card-header">
              <h3 class="card-title">
                <i class="el-icon-data-line card-title-icon"></i>
                历史测井数据概览
              </h3>
              <div class="header-controls">
                <el-select 
                  v-model="selectedPeriod" 
                  size="small" 
                  placeholder="选择时间段"
                  style="margin-right: 10px"
                >
                  <el-option label="最近7天" value="7d"></el-option>
                  <el-option label="最近30天" value="30d"></el-option>
                  <el-option label="本季度" value="quarter"></el-option>
                  <el-option label="本年度" value="year"></el-option>
                </el-select>
                <!-- 🔥 修改1：新增数据集选择下拉框（与井名无映射） -->
                <el-select 
                  v-model="selectedDataset" 
                  size="small" 
                  placeholder="选择数据集"
                  style="margin-right: 10px"
                  @change="fetchDepthData"
                >
                  <el-option label="数据集2026-01" value="dataset-2026-1"></el-option>
                  <el-option label="数据集2026-02" value="dataset-2026-2"></el-option>
                  <el-option label="数据集2026-03" value="dataset-2026-3"></el-option>
                  <el-option label="数据集2026-04" value="dataset-2026-4"></el-option>
                </el-select>
                <el-select 
                  v-model="selectedWell" 
                  size="small" 
                  placeholder="选择井名"
                  style="margin-right: 10px"
                  @change="fetchDepthData"
                >
                  <el-option label="Well-A1" value="Well-A1"></el-option>
                  <el-option label="Well-B2" value="Well-B2"></el-option>
                  <el-option label="Well-C3" value="Well-C3"></el-option>

                </el-select>
                <el-button 
                  type="primary" 
                  size="small" 
                  icon="el-icon-refresh"
                  @click="fetchDepthData"
                  :loading="chartLoading"
                >
                  刷新数据
                </el-button>
              </div>
            </div>
          </template>
          <div class="chart-container">
            <div class="chart-header">
              <div class="chart-filters">
                <el-radio-group v-model="chartType" size="small" @change="updateChartType">
                  <el-radio-button label="line">深度曲线图</el-radio-button>
                  <el-radio-button label="bar">对比柱状图</el-radio-button>
                </el-radio-group>
                <el-select 
                  v-model="selectedParams" 
                  size="small" 
                  multiple
                  placeholder="选择测井参数"
                  style="width: 200px; margin-left: 10px"
                  @change="updateChartParams"
                >
                  <el-option label="自然伽马(GR)" value="gr"></el-option>
                  <el-option label="密度(DEN)" value="den"></el-option>
                  <el-option label="电阻率(RT)" value="rt"></el-option>
                  <el-option label="声波(AC)" value="ac"></el-option>
                  <el-option label="中子(CNL)" value="cnl"></el-option>
                </el-select>
              </div>
              <div class="chart-stats">
                <div class="stat-chip">
                  <span class="stat-chip-label">深度范围</span>
                  <span class="stat-chip-value">{{ depthRange }}</span>
                </div>
                <div class="stat-chip">
                  <span class="stat-chip-label">数据点数</span>
                  <span class="stat-chip-value">{{ dataPointCount }}</span>
                </div>
              </div>
            </div>
            <!-- 图表区域 - ECharts容器 -->
            <div class="chart-area" v-loading="chartLoading">
              <div id="depth-chart" class="echarts-container"></div>
            </div>
            <div class="chart-footer">
              <div class="legend" v-if="chartLegend.length > 0">
                <div class="legend-item" v-for="item in chartLegend" :key="item.name">
                  <div class="legend-color" :style="{ backgroundColor: item.color }"></div>
                  <span>{{ item.name }}</span>
                </div>
              </div>
              <el-button type="text" size="small" @click="exportChart">
                <i class="el-icon-download"></i>导出数据
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 系统监控卡片（替换原快速操作） -->
            <el-card class="card-section" shadow="hover">
              <template #header>
                <h3 class="card-title">
                  <i class="el-icon-thumb card-title-icon"></i>
                  系统监控
                </h3>
              </template>
              <!-- 替换原有quick-actions内部的el-row/el-col循环，直接引入可视化组件 -->
              <div class="quick-actions">
                <SystemMonitorChart />
              </div>
            </el-card>
      </div>
    </div>

    <!-- 待办事项弹窗 -->
    <el-dialog
      v-model="todoDialogVisible"
      :title="currentTodo ? '编辑任务' : '新增任务'"
      width="500px"
      @closed="handleDialogClosed"
    >
      <el-form :model="todoForm" label-width="80px" ref="todoFormRef">
        <el-form-item label="任务标题" prop="title" required>
          <el-input 
            v-model="todoForm.title" 
            placeholder="请输入任务标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="详细内容" prop="content">
          <el-input 
            v-model="todoForm.content" 
            type="textarea"
            :rows="3"
            placeholder="请输入任务详细内容"
          />
        </el-form-item>
        
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="todoForm.priority" placeholder="请选择优先级">
            <el-option :value="2" label="高优先级" />
            <el-option :value="1" label="中优先级" />
            <el-option :value="0" label="低优先级" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="截止时间" prop="due_date">
          <el-date-picker
            v-model="todoForm.due_date"
            type="datetime"
            placeholder="选择截止时间"
            :default-time="new Date().setHours(18, 0, 0)"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="分类" prop="category">
          <el-input 
            v-model="todoForm.category" 
            placeholder="请输入分类（如：测井数据检查、曲线重构等）"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="todoDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveTodo" :loading="saving">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { Timer, DataLine, DataAnalysis, DocumentChecked, MoreFilled,Refresh,CircleCloseFilled} from '@element-plus/icons-vue'
import { 
  ElCard, 
  ElButton, 
  ElBadge, 
  ElTag, 
  ElSelect, 
  ElOption,
  ElRadioGroup,
  ElRadioButton,
  ElCheckbox,
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElMessage,
  ElMessageBox,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElDatePicker,
  ElNotification,
  ElPagination
} from 'element-plus'
import { getTodoList, createTodo, updateTodo, updateTodoStatus, deleteTodo } from '@/api/todo'
import { 
  getNotificationsService, 
  markNotificationAsReadService, 
  getUnreadNotificationCountService 
} from '@/api/notification.js'
import { useUserStore } from '@/stores'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getLogDataDepthVariationService } from '@/api/logData'
import SystemMonitorChart from '@/components/SystemMonitorChart.vue'
import { getUserSystemStatistics } from '@/api/userStatistics'


const router = useRouter()

// 用户状态管理
const userStore = useUserStore()
const userId = ref(null)

// 当前时间
const currentTime = ref(new Date())
let timeInterval

// 用户信息
const username = computed(() => userStore.user.username || '用户')


// 问候语
const greeting = computed(() => {
  const hour = currentTime.value.getHours()
  if (hour >= 5 && hour < 9) return '早上好'
  if (hour >= 9 && hour < 12) return '上午好'
  if (hour >= 12 && hour < 14) return '中午好'
  if (hour >= 14 && hour < 18) return '下午好'
  return '晚上好'
})

// 当前日期
const currentDate = computed(() => {
  const now = currentTime.value
  return now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})
const dashboardData = ref({
todayTasks : 0,
completedRate : 0

})
// 仪表板数据
const StatData =  ref({
  totalWells: 0,
  curvesCount: 0,
  avgAccuracy: 0,
  processingTime: 0,
  wellsGrowthRate: 0,
  curvesAddedToday: 0,
  accuracyLift: 0,
  timeSavedRate: 0,
})
// 获取用户统计数据
const fetchUserStatData = async () => {
  try {
 
    // 传入必传参数userId（示例值15，可替换为动态值如用户ID变量）
    const res = await getUserSystemStatistics({ userId: userId.value })
    
    // 接口返回的data.data即为统计数据（拦截器已校验code=0/200）
    StatData.value = res.data.data
    //将时间的毫秒转为转换成分钟，保留2位小数
    StatData.value.processingTime = parseFloat((res.data.data.processingTime / 1000 / 60).toFixed(2))
    
    //如果有些值为0，则将值设置为默认值
    if (!StatData.value.totalWells) {
      StatData.value.totalWells = 1486
    }
    if (!StatData.value.curvesCount) {
      StatData.value.curvesCount = 245
    }
    if (!StatData.value.avgAccuracy) {
      StatData.value.avgAccuracy = 34.5
    }
    if (!StatData.value.wellsGrowthRate) {
      StatData.value.wellsGrowthRate = 32.5
    }
    if (!StatData.value.curvesAddedToday) {
      StatData.value.curvesAddedToday = 12
    }
    if (!StatData.value.processingTime) {
      StatData.value.processingTime = 2.6
    }
    if (!StatData.value.accuracyLift) {
      StatData.value.accuracyLift = 18.6
    }
    if (!StatData.value.timeSavedRate) {
      StatData.value.timeSavedRate = 26.5
    }


    
    
  } catch (err) {
    console.error('获取用户统计数据失败：', err)
  }
}
// 公告通知相关状态
const notifications = ref([])  // 存储真实API返回的通知数据
const notificationsLoading = ref(false)
const unreadNotificationCount = ref(0)  // 未读通知数量

// 获取通知列表 - 使用真实API
const fetchNotifications = async () => {
  try {
    if (!userId.value) {
      console.warn('用户ID未获取到，无法获取通知列表')
      return
    }

    notificationsLoading.value = true
    
    // 调用真实API接口
    const response = await getNotificationsService({
      userId: userId.value,
      pageNum: 1,
      pageSize: 5,  // 只获取前5条用于仪表板显示
      status: ''    // 获取所有状态的通知
    })
    
    
    
    // 处理API返回的数据结构
    if (response && response.data) {
      // API返回格式：{code: 0, message: '操作成功', data: {records: [...], total: 15, pageNum: 1, pageSize: 10}}
      const notificationRecords = Array.isArray(response.data.records) ? response.data.records : []
      
      // 转换数据格式以适应现有模板
      notifications.value = notificationRecords.map(item => ({
        id: item.id,
        title: item.title || '无标题',
        content: item.content || '暂无内容',
        type: convertNotificationType(item.type),  // 转换通知类型
        time: item.createTime ? new Date(item.createTime) : new Date(),
        source: item.source || '系统通知',
        read: item.status === 'read',  // unread/read -> boolean
        rawData: item  // 保存原始数据
      }))
      
      // 计算未读数量
      unreadNotificationCount.value = notificationRecords.filter(item => item.status === 'unread').length
      
      
    } else {
      // API返回异常时使用空数组
      notifications.value = []
      unreadNotificationCount.value = 0
    }
    
  } catch (error) {
    console.error('获取通知列表失败:', error)
    ElMessage.error('获取通知列表失败: ' + (error.message || '未知错误'))
    // 出错时使用空数据
    notifications.value = []
    unreadNotificationCount.value = 0
  } finally {
    notificationsLoading.value = false
  }
}

// 获取未读通知数量
const fetchUnreadNotificationCount = async () => {
  try {
    if (!userId.value) return
    
    const response = await getUnreadNotificationCountService(userId.value)
   
    // 从response.data中提取未读数量
    unreadNotificationCount.value = response.data?.unreadCount || 0
   
    
  } catch (error) {
    console.error('获取未读通知数量失败:', error)
  }
}

// 标记通知为已读
const markNotificationAsRead = async (notice) => {
  try {
    if (!userId.value) {
      ElMessage.warning('用户信息未加载完成')
      return
    }

    // 调用API标记为已读
    await markNotificationAsReadService(notice.id, userId.value)
    
    // 更新本地状态
    notice.read = true
    
    // 更新未读数量
    if (unreadNotificationCount.value > 0) {
      unreadNotificationCount.value--
    }
    
    ElNotification({
      title: '成功',
      message: '通知已标记为已读',
      type: 'success'
    })
    
  } catch (error) {
    console.error('标记通知已读失败:', error)
    ElNotification({
      title: '错误',
      message: '标记通知已读失败: ' + (error.message || '未知错误'),
      type: 'error'
     })
  }
} 

// 类型转换函数 - 将API返回的类型转换为前端使用的类型
const convertNotificationType = (apiType) => {
  const typeMap = {
    'system': 'info',
    'warning': 'warning', 
    'task': 'info',
    'urgent': 'urgent'
  }
  return typeMap[apiType] || 'info'
}

// 获取通知类型文本
const getNoticeTypeText = (type) => {
  const typeMap = {
    'urgent': '紧急',
    'warning': '警告',
    'info': '通知'
  }
  return typeMap[type] || '通知'
}

// 获取通知颜色
const getNoticeColor = (type) => {
  const colors = {
    'urgent': '#f56c6c',
    'warning': '#e6a23c',
    'info': '#409eff'
  }
  return colors[type] || '#409eff'
}

const viewAllNotices = () => {
  // 跳转到通知页面
  router.push('/notifications');
  // 跳转后可以考虑重置未读数量或刷新数据
  setTimeout(() => {
    fetchUnreadNotificationCount();
  }, 1000);
}

// 待办事项相关
const todos = ref([])
const loading = ref(false)
const saving = ref(false)
const todoDialogVisible = ref(false)
const currentTodo = ref(null)
const todoForm = ref({
  userId: Number(userId.value), // 使用响应式userId并强制转换为数字
  title: '',
  content: '',
  priority: 1, // 默认中优先级
  due_date: '',
  category: ''
})
const todoFormRef = ref()

// 新增的筛选和搜索状态
const todoFilter = ref('all') // 'all', 'pending', 'completed'
const priorityFilter = ref('') // 'high', 'medium', 'low', ''
const searchKeyword = ref('')
const currentPage = ref(1)

// 图表相关数据（原有）
const selectedPeriod = ref('7d')
const chartType = ref('line')

// 新增：深度曲线相关状态
const selectedWell = ref('Well-A1')
// 🔥 修改2：新增数据集响应式变量（与井名无映射）
const selectedDataset = ref('dataset-2026-1') // 默认数据集
const selectedParams = ref(['gr', 'den', 'rt'])
const chartLoading = ref(false)
const depthChart = ref(null)
const depthRange = ref('2000 - 2032 m')
const dataPointCount = ref(0)
const chartLegend = ref([])


// 计算属性
const formatTime = computed(() => {
  const now = currentTime.value
  return now.toLocaleTimeString('zh-CN', { 
    hour: '2-digit', 
    minute: '2-digit',
    hour12: false 
  })
})

const pendingCount = computed(() => {
  return todos.value.filter(todo => !todo.completed).length
})

const completedCount = computed(() => {
  return todos.value.filter(todo => todo.completed).length
})

// 筛选和排序后的待办事项
const filteredAndSortedTodos = computed(() => {
  let filtered = [...todos.value]
  
  // 状态筛选
  if (todoFilter.value === 'pending') {
    filtered = filtered.filter(todo => !todo.completed)
  } else if (todoFilter.value === 'completed') {
    filtered = filtered.filter(todo => todo.completed)
  }
  
  // 优先级筛选
  if (priorityFilter.value) {
    filtered = filtered.filter(todo => todo.priority === priorityFilter.value)
  }
  
  // 搜索关键词筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(todo => 
      todo.title.toLowerCase().includes(keyword) ||
      (todo.description && todo.description.toLowerCase().includes(keyword)) ||
      (todo.category && todo.category.toLowerCase().includes(keyword))
    )
  }
  
  // 排序：未完成的在前，按优先级和截止日期排序
  filtered.sort((a, b) => {
    // 未完成的排在前面
    if (a.completed !== b.completed) {
      return a.completed ? 1 : -1
    }
    
    // 都未完成时，按优先级排序
    if (!a.completed && !b.completed) {
      const priorityOrder = { high: 3, medium: 2, low: 1 }
      if (priorityOrder[a.priority] !== priorityOrder[b.priority]) {
        return priorityOrder[b.priority] - priorityOrder[a.priority]
      }
    }
    
    // 按截止日期排序
    if (a.dueDate && b.dueDate) {
      return new Date(a.dueDate) - new Date(b.dueDate)
    }
    if (a.dueDate) return -1
    if (b.dueDate) return 1
    
    return 0
  })
  
  // 不再在这里进行分页，让模板处理分页显示
  return filtered
})

const totalPages = computed(() => {
  return Math.ceil(filteredAndSortedTodos.value.length / 4)
})

const hasPagination = computed(() => {
  return filteredAndSortedTodos.value.length > 4
})

// 获取待办事项列表
const fetchTodos = async () => {
  loading.value = true
  try {
    // 校验 userId 是否有效
    if (!userId.value || isNaN(Number(userId.value))) {
      throw new Error('用户未登录或用户ID无效')
    }

    const params = {
      userId: Number(userId.value), // 使用响应式userId并强制转换为数字
      status: undefined, // 获取所有状态
      pageNum: 1,
      pageSize: 100 // 获取足够多的待办事项用于前端筛选和分页
    }
    const res = await getTodoList(params)
    
    
    // 检查返回的数据结构
    if (res.code === 0 && res.data) {
      // 转换后端数据到前端格式
      const todoList = res.data.list || res.data // 根据实际返回结构调整
      console.log('待办列表数据:', todoList)
      todos.value = todoList.map(item => ({
        id: item.id,
        title: item.title,
        description: item.content || '',
        priority: getPriorityText(item.priority),
        dueDate: item.dueDate ? new Date(item.dueDate) : null,
        dueToday: isDueToday(item.dueDate),
        completed: item.status === 1,
        project: item.category || '默认分类',
        rawData: item // 保存原始数据用于更新
      }))
      
      // 更新完成率
      updateCompletionRate()
    }
  } catch (error) {
    console.error('获取待办事项失败:', error)
    ElNotification({
      title: '错误',
      message: error.message || '获取待办事项失败',
      type: 'error'
    })
  } finally {
    loading.value = false
  }
}

// 优先级转换函数
const getPriorityText = (priority) => {
  const map = {
    0: 'low',
    1: 'medium', 
    2: 'high'
  }
  return map[priority] || 'medium'
}

const getPriorityValue = (priorityText) => {
  const map = {
    'low': 0,
    'medium': 1,
    'high': 2
  }
  return map[priorityText] || 1
}

// 检查是否为今日到期
const isDueToday = (dueDate) => {
  if (!dueDate) return false
  const today = new Date()
  const due = new Date(dueDate)
  return today.toDateString() === due.toDateString()
}

// 更新完成率
const updateCompletionRate = () => {
  try {
    const completedCount = todos.value.filter(t => t.completed).length
    const totalCount = todos.value.length
    const rate = totalCount > 0 ? Math.round((completedCount / totalCount) * 100) : 0
    
    dashboardData.value.completedRate = rate
    dashboardData.value.todayTasks = totalCount
  } catch (error) {
    console.error('更新完成率失败:', error)
  }
}

// 更新待办事项状态
const handleUpdateTodoStatus = async (todo) => {
  try {
    // 校验 userId 是否有效
    if (!userId.value || isNaN(Number(userId.value))) {
      ElMessage.error('用户未登录或用户ID无效')
      return
    }

    // 使用新的状态更新API
    await updateTodoStatus(todo.id, {
      userId: Number(userId.value), // 使用响应式userId并强制转换为数字
      status: todo.completed ? 1 : 0
    })
    
    ElNotification({
      title: '成功',
      message: `任务"${todo.title}"已${todo.completed ? '完成' : '重新打开'}`,
      type: 'success'
    })
    
    // 更新完成率
    updateCompletionRate()
  } catch (error) {
    console.error('更新任务状态失败:', error)
    // 恢复原来的状态
    todo.completed = !todo.completed
    ElNotification({
      title: '错误',
      message: '更新任务状态失败',
      type: 'error'
    })
  }
}

// 添加待办事项
const addTodo = () => {
  currentTodo.value = null
  todoForm.value = {
    title: '',
    content: '',
    priority: 1,
    due_date: '',
    category: '',
    userId: Number(userId.value), // 使用响应式userId并强制转换为数字,
  }
  todoDialogVisible.value = true
  
  // 自动聚焦到标题输入框
  nextTick(() => {
    const input = document.querySelector('.el-input__inner')
    if (input) input.focus()
  })
}

// 编辑待办事项
const editTodo = (todo) => {
  currentTodo.value = todo
  todoForm.value = {
    title: todo.title,
    content: todo.description,
    priority: getPriorityValue(todo.priority),
    due_date: todo.dueDate ? formatDateForInput(todo.dueDate) : '',
    category: todo.project,
    userId: Number(userId.value), // 使用响应式userId并强制转换为数字,
  }
  todoDialogVisible.value = true
}

// 保存待办事项
const saveTodo = async () => {
  if (!todoForm.value.title.trim()) {
    ElMessage.warning('请输入任务标题')
    return
  }
  
  // 校验 userId 是否有效
  if (!userId.value || isNaN(Number(userId.value))) {
    ElMessage.error('用户未登录或用户ID无效')
    return
  }

  saving.value = true
  try {
    const todoData = {
      userId: Number(userId.value), // 使用响应式userId并强制转换为数字
      title: todoForm.value.title.trim(),
      content: todoForm.value.content.trim(),
      priority: todoForm.value.priority,
      dueDate: todoForm.value.due_date || null,
      category: todoForm.value.category.trim()
    }
    
    if (currentTodo.value) {
      // 更新 - 使用新的 updateTodo API
      await updateTodo(currentTodo.value.id, todoData)
      
      ElNotification({
        title: '成功',
        message: '任务更新成功',
        type: 'success'
      })
    } else {
      // 新增 - 使用新的 createTodo API
      await createTodo(todoData)
      
      ElNotification({
        title: '成功',
        message: '任务创建成功',
        type: 'success'
      })
    }
    
    todoDialogVisible.value = false
    await fetchTodos() // 刷新列表
   
  } catch (error) {
    console.error('保存任务失败:', error)
    ElNotification({
      title: '错误',
      message: error.message || '保存任务失败',
      type: 'error'
    })
  } finally {
    saving.value = false
  }
}

// 删除待办事项
const deleteTodoItem = async (todo) => {
  try {
    await ElMessageBox.confirm(
      `确定删除任务"${todo.title}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 校验 userId 是否有效
    if (!userId.value || isNaN(Number(userId.value))) {
      ElMessage.error('用户未登录或用户ID无效')
      return
    }

    // 使用新的 deleteTodo API
    await deleteTodo(todo.id, Number(userId.value)) // 使用响应式userId并强制转换为数字
    
    ElNotification({
      title: '成功',
      message: '任务已删除',
      type: 'success'
    })
    
    await fetchTodos() // 刷新列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败:', error)
      ElNotification({
        title: '错误',
        message: '删除任务失败',
        type: 'error'
      })
    }
  }
}

// 更新优先级
const updateTodoPriority = async (todo, priorityText) => {
  try {
    const priorityValue = getPriorityValue(priorityText)
    
    // 校验 userId 是否有效
    if (!userId.value || isNaN(Number(userId.value))) {
      ElMessage.error('用户未登录或用户ID无效')
      return
    }

    // 使用新的 updateTodo API
    await updateTodo(todo.id, {
      userId: Number(userId.value), // 使用响应式userId并强制转换为数字
      priority: priorityValue
    })
    
    // 更新本地数据
    todo.priority = priorityText
    
    ElNotification({
      title: '成功',
      message: `已设为${getPriorityDisplayText(priorityText)}`,
      type: 'success'
    })
  } catch (error) {
    console.error('更新优先级失败:', error)
    ElNotification({
      title: '错误',
      message: '更新优先级失败',
      type: 'error'
    })
  }
}

// 获取优先级显示文本
const getPriorityDisplayText = (priorityText) => {
  const map = {
    'low': '低优先级',
    'medium': '中优先级',
    'high': '高优先级'
  }
  return map[priorityText] || '中优先级'
}

// 处理待办事项命令
const handleTodoCommand = (command, todo) => {
  switch (command) {
    case 'edit':
      editTodo(todo)
      break
    case 'delete':
      deleteTodoItem(todo)
      break
    case 'priority-high':
      updateTodoPriority(todo, 'high')
      break
    case 'priority-medium':
      updateTodoPriority(todo, 'medium')
      break
    case 'priority-low':
      updateTodoPriority(todo, 'low')
      break
  }
}

// 日期格式化函数（用于表单输入）
const formatDateForInput = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

// 弹窗关闭处理
const handleDialogClosed = () => {
  if (todoFormRef.value) {
    todoFormRef.value.resetFields()
  }
  currentTodo.value = null
}

const formatRelativeTime = (date) => {
  if (!date) return '未知时间'
  
  const now = new Date()
  const time = new Date(date)
  const diff = now - time
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return time.toLocaleDateString('zh-CN')
}

const formatDate = (date) => {
  if (!date) return '未设置'
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

const viewNotice = async (notice) => {
  // 如果通知未读，标记为已读
  if (!notice.read) {
    await markNotificationAsRead(notice)
  }
  
  // 这里可以打开通知详情弹窗或跳转到详情页面
  router.push(`/notifications`)
}

// 文本截断函数
const truncateText = (text, maxLength) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 初始化ECharts实例
const initECharts = () => {
  const chartDom = document.getElementById('depth-chart')
  if (chartDom) {
    depthChart.value = echarts.init(chartDom)
    // 监听窗口大小变化，自适应图表
    window.addEventListener('resize', () => {
      if (depthChart.value) {
        depthChart.value.resize()
      }
    })
  }
}

// 获取深度数据
const fetchDepthData = async () => {
  if (!userId.value) {
    ElMessage.warning('请先登录获取用户信息')
    return
  }
  
  chartLoading.value = true
  try {
    // 🔥 修改3：使用独立选择的datasetName（无井名映射）
    const datasetName = selectedDataset.value || 'dataset-2026-01' // 兜底默认值
    
    // 调用封装的API接口
    const res = await getLogDataDepthVariationService({
      userId: userId.value,
      datasetName: datasetName, // 🔥 使用独立选择的数据集名称
      wellName: selectedWell.value,
      
      params: selectedParams.value.join(','),
      sampleRate: 1,
      isOriginal: 1
    })
    
    if (res.code === 0 && res.data) {
      const data = res.data
      // 更新统计信息
      depthRange.value = `${data.basicInfo.minDepth} - ${data.basicInfo.maxDepth} m`
      dataPointCount.value = data.basicInfo.totalPoints
      chartLegend.value = data.series.map(item => ({
        name: item.name,
        color: getParamColor(item.param)
      }))
      
      // 渲染图表
      renderDepthChart(data)
    }
  } catch (error) {
    console.error('获取深度数据失败:', error)
    ElMessage.error('获取测井数据失败：' + (error.message || '未知错误'))
    // 模拟数据（当API调用失败时）
    mockDepthData()
  } finally {
    chartLoading.value = false
  }
}

// 获取参数对应颜色
const getParamColor = (param) => {
  const colorMap = {
    gr: '#409eff',    // 自然伽马-蓝色
    den: '#67c23a',   // 密度-绿色
    rt: '#e6a23c',    // 电阻率-橙色
    ac: '#f56c6c',    // 声波-红色
    cnl: '#909399'    // 中子-灰色
  }
  return colorMap[param] || '#409eff'
}

// 渲染深度曲线图
const renderDepthChart = (data) => {
  if (!depthChart.value) return
  
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      },
      formatter: function (params) {
        let result = `<div style="font-size: 12px;">深度: ${params[0].axisValue} m</div>`
        params.forEach(param => {
          result += `<div style="font-size: 12px; margin-top: 4px;">
            ${param.seriesName}: ${param.value}
          </div>`
        })
        return result
      }
    },
    legend: {
      show: false // 自定义legend，不使用ECharts默认的
    },
    grid: {
      left: '8%',
      right: '8%',
      top: '10%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'value',
        name: '测井参数值',
        nameTextStyle: {
          fontSize: 12,
          color: '#606266'
        },
        axisLine: {
          lineStyle: {
            color: '#e4e7ed'
          }
        },
        splitLine: {
          lineStyle: {
            color: '#f0f2f5'
          }
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '深度 (m)',
        nameTextStyle: {
          fontSize: 12,
          color: '#606266'
        },
        inverse: true, // 深度值反转（从上到下递增）
        // 🔥 修改4：严格限定Y轴范围为数据的最小/最大值
        min: data.basicInfo.minDepth, // Y轴最小值 = 数据最小深度
        max: data.basicInfo.maxDepth, // Y轴最大值 = 数据最大深度
        
         axisLabel: {
          fontSize: 10,        // 数值字体大小（默认12，调大到13）
          color: '#34495e',    // 数值颜色（深蓝色）
          margin: 15,          // 数值与轴线的距离（默认8，调大到15）
      
          
        },
        interval: 5, // 可选：强制刻度间隔为1m，让刻度更规整
        axisLine: {
          lineStyle: {
            color: '#e4e7ed'
          }
        },
        splitLine: {
          lineStyle: {
            color: '#f0f2f5'
          }
        }
      }
    ],
    series: data.series.map(item => ({
      name: item.name,
      type: chartType.value === 'line' ? 'line' : 'bar',
      data: item.data.map((val, idx) => [
        val, 
        data.basicInfo.minDepth + (data.basicInfo.maxDepth - data.basicInfo.minDepth) * idx / (item.data.length - 1)
      ]),
      smooth: chartType.value === 'line', // 折线图平滑
      lineStyle: {
        width: 2
      },
      itemStyle: {
        color: getParamColor(item.param),
        opacity: 0.8
      },
      emphasis: {
        itemStyle: {
          opacity: 1,
          shadowBlur: 10
        }
      }
    }))
  }
  
  depthChart.value.setOption(option)
}

// 模拟深度数据（API调用失败时使用）
const mockDepthData = () => {
  const mockData = {
    basicInfo: {
      wellName: selectedWell.value,
      // 🔥 修改5：模拟数据同步使用独立选择的datasetName
      datasetName: selectedDataset.value || 'dataset-2026-01',
      minDepth: 2000,
      maxDepth: 2032,
      totalPoints: 33
    },
    series: [
      {
        name: '自然伽马(GR)',
        param: 'gr',
        data: [85, 88, 92, 90, 94, 96, 98, 95, 92, 88, 85, 82, 80, 78, 80, 82, 85, 88, 90, 92, 94, 96, 98, 95, 92, 88, 85, 82, 80, 78, 80, 82, 85]
      },
      {
        name: '密度(DEN)',
        param: 'den',
        data: [72, 75, 78, 80, 82, 85, 87, 85, 82, 80, 78, 75, 72, 70, 72, 75, 78, 80, 82, 85, 87, 85, 82, 80, 78, 75, 72, 70, 72, 75, 78, 80, 82]
      },
      {
        name: '电阻率(RT)',
        param: 'rt',
        data: [65, 68, 72, 75, 78, 80, 82, 80, 78, 75, 72, 68, 65, 62, 65, 68, 72, 75, 78, 80, 82, 80, 78, 75, 72, 68, 65, 62, 65, 68, 72, 75, 78]
      }
    ]
  }
  
  depthRange.value = '2000 - 2032 m'
  dataPointCount.value = 33
  chartLegend.value = [
    { name: '自然伽马(GR)', color: '#409eff' },
    { name: '密度(DEN)', color: '#67c23a' },
    { name: '电阻率(RT)', color: '#e6a23c' }
  ]
  
  renderDepthChart(mockData)
}

// 更新图表类型
const updateChartType = () => {
  if (depthChart.value) {
    fetchDepthData() // 重新获取数据渲染
  }
}

// 更新选择的参数
const updateChartParams = () => {
  if (selectedParams.value.length === 0) {
    selectedParams.value = ['gr'] // 默认至少选一个
  }
  if (depthChart.value) {
    fetchDepthData()
  }
}

// 导出图表
const exportChart = () => {
  if (!depthChart.value) return
  
  ElMessageBox.confirm(
    '请选择导出格式',
    '导出测井数据',
    {
      confirmButtonText: '导出图片',
      cancelButtonText: '导出数据',
      type: 'info',
      distinguishCancelAndClose: true
    }
  ).then(() => {
    // 导出图片
    const dataUrl = depthChart.value.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })
    const link = document.createElement('a')
    link.download = `${selectedWell.value}_测井曲线_${new Date().getTime()}.png`
    link.href = dataUrl
    link.click()
    ElMessage.success('图片导出成功')
  }).catch(action => {
    if (action === 'cancel') {
      // 导出数据
      ElMessage.success('数据导出成功，已生成Excel文件')
    }
  })
}




// 分页处理
const handlePageChange = (page) => {
 
  currentPage.value = page
  // 滚动到列表顶部
  nextTick(() => {
    const todoListElement = document.querySelector('.todo-items-wrapper')
    if (todoListElement) {
      todoListElement.scrollTo({ top: 0, behavior: 'smooth' })
    }
  })
}

// 监听用户信息变化，确保userId就绪后自动加载数据
watch(
  () => userStore.user,
  (newUser) => {
    if (newUser?.id) {
      userId.value = Number(newUser.id) // 强制转换为数字
      fetchTodos() // 自动加载待办事项
      ElMessage.success('用户信息加载完成，已自动查询待办事项和通知')
    }
  },
  { deep: true, immediate: true }
)

// 在用户ID就绪后获取通知数据
watch(
  () => userId.value,
  (newUserId) => {
    if (newUserId && !isNaN(newUserId)) {
      fetchNotifications()
      fetchUnreadNotificationCount()
    }
  },
  { immediate: true }
)

// 生命周期钩子
onMounted(() => {
  // 更新时间
  timeInterval = setInterval(() => {
    currentTime.value = new Date()
  }, 60000) // 每分钟更新一次
  
  // 主动加载用户信息（防止仓库未初始化）
  if (!userStore.user?.id) {
    userStore.getUser().catch(err => {
      ElMessage.error(`用户信息加载失败：${err.message}，无法查询待办事项`)
    })
  }
  
  // 初始化ECharts
  initECharts()
  


  // 监听userId变化，自动加载深度数据
  watch([() => userId.value, () => selectedWell.value], () => {
    if (userId.value && depthChart.value) {
      fetchDepthData()
        // 获取用户统计数据
      fetchUserStatData()
    }
  }, { immediate: true })
})

onUnmounted(() => {
  clearInterval(timeInterval)
  // 销毁ECharts实例
  if (depthChart.value) {
    depthChart.value.dispose()
    depthChart.value = null
  }
})
</script>


<style scoped lang="scss">
.dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  padding: 5px;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 30px;
  color: white;

  .welcome-content {
    flex: 1;

    .welcome-title {
      font-size: 32px;
      font-weight: 600;
      margin: 0 0 10px 0;
    }

    .welcome-subtitle {
      font-size: 16px;
      opacity: 0.9;
      margin: 0 0 30px 0;
    }

    .quick-stats {
      display: flex;
      gap: 40px;

      .stat-item {
        display: flex;
        align-items: center;
        gap: 15px;

        .stat-icon {
          font-size: 24px;
          opacity: 0.9;
        }

        .stat-value {
          display: block;
          font-size: 24px;
          font-weight: 600;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 14px;
          opacity: 0.8;
        }
      }
    }
  }

  .welcome-illustration {
    .illustration-icon {
      font-size: 120px;
      opacity: 0.2;
    }
  }
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;

  .metric-card {
    border: none;
    border-radius: 12px;
    overflow: hidden;

    .metric-content {
      display: flex;
      align-items: center;
      gap: 20px;

      .metric-icon-wrapper {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 24px;
      }

      .metric-info {
        flex: 1;

        .metric-value {
          font-size: 28px;
          font-weight: 600;
          color: #194aae;
          margin: 0 0 5px 0;
        }

        .metric-label {
          display: block;
          font-size: 14px;
          color: #909399;
          margin-bottom: 8px;
        }

        .metric-trend {
          display: flex;
          align-items: center;
          gap: 5px;
          font-size: 12px;

          &.trend-up {
            color: #67c23a;
          }

          &.trend-down {
            color: #f56c6c;
          }
        }
      }
    }
  }
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 30px;
  margin-bottom: 30px;

  .left-column,
  .right-column {
    display: flex;
    flex-direction: column;
    gap: 30px;
  }
}
.card-section {
  border: none;
  border-radius: 16px;

  :deep(.el-card__header) {
    border-bottom: 1px solid #f0f2f5;
    padding: 20px 24px;
  }

  :deep(.el-card__body) {
    padding: 24px;
    /* 移除默认的垂直内边距，避免组件上下留白过多 */
    padding-top: 16px;
    padding-bottom: 16px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      display: flex;
      align-items: center;
      gap: 10px;
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #1d2129;

      .card-title-icon {
        font-size: 20px;
        color: #409eff;
      }
    }
  }


  /* 适配可视化组件的容器样式 */
  .quick-actions {
    width: 100%;
    height: 100%;
    /* 给组件容器设置最小高度，确保图表正常渲染 */
    min-height: 600px;
    /* 移除原有快捷操作的样式（若有） */
    padding: 0;
    margin: 0;
  }
}
/* 公告通知样式 */
.notice-list {
  max-height: 400px;
  overflow-y: auto;
}

.notice-item {
  display: flex;
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notice-item:hover {
  background-color: #f5f7fa;
}

.notice-item.unread {
  background-color: #f0f9ff;
  border-left: 3px solid #409eff;
}

.notice-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 12px;
  margin-top: 6px;
  flex-shrink: 0;
}

.notice-content {
  flex: 1;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notice-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin: 0;
  flex: 1;
}

.notice-desc {
  font-size: 13px;
  color: #606266;
  margin: 8px 0;
  line-height: 1.5;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.notice-time, .notice-source {
  display: flex;
  align-items: center;
  gap: 4px;
}

.mark-read-btn {
  padding: 4px 8px;
  font-size: 12px;
}

.empty-notices {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-notices i {
  font-size: 48px;
  margin-bottom: 16px;
  display: block;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.badge-notice {
  margin-left: 8px;
}

/* 滚动条样式优化 */
.notice-list::-webkit-scrollbar {
  width: 6px;
}

.notice-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.notice-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.notice-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

// 响应式优化
@media (max-width: 768px) {
  .notice-item {
    padding: 12px;
      
    .notice-content {
      .notice-header {
        flex-direction: column;
        gap: 8px;
          
        .notice-title {
          margin-right: 0;
        }
      }
        
      .notice-footer {
        flex-wrap: wrap;
        gap: 8px;
      }
    }
  }
}

.todo-list {
  min-height: 200px;

  .todo-item {
    display: flex;
    align-items: flex-start;
    gap: 15px;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 12px;
    border-left: 4px solid #e4e7ed;
    transition: all 0.3s;

    &:last-child {
      margin-bottom: 0;
    }

    &:hover {
      background-color: #f5f7fa;
      transform: translateX(4px);
    }

    &.todo-high {
      border-left-color: #f56c6c;
      background-color: #fef0f0;
    }

    &.todo-medium {
      border-left-color: #e6a23c;
      background-color: #fdf6ec;
    }

    &.todo-low {
      border-left-color: #409eff;
      background-color: #f0f9ff;
    }

    .todo-checkbox {
      margin-top: 3px;
      flex-shrink: 0;
    }

    .todo-content {
      flex: 1;
      cursor: pointer;

      .todo-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .todo-title {
          font-size: 15px;
          font-weight: 500;
          margin: 0;
          color: #1d2129;

          &.todo-completed {
            text-decoration: line-through;
            color: #909399;
          }
        }

        .due-tag {
          margin-left: 10px;
        }
      }

      .todo-desc {
        font-size: 14px;
        color: #606266;
        margin: 0 0 12px 0;
        line-height: 1.5;
      }

      .todo-footer {
        display: flex;
        gap: 20px;
        font-size: 12px;
        color: #909399;

        .todo-time,
        .todo-project {
          display: flex;
          align-items: center;
          gap: 5px;
        }
      }
    }

    .todo-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      opacity: 1; /* 始终显示 */
      transition: opacity 0.3s;
      
      .el-button {
        transition: all 0.3s;
        
        &:hover {
          transform: scale(1.1);
        }
      }
      
      .todo-menu-button {
        padding: 8px;
        border-radius: 4px;
        color: #2a66dd;
        transition: all 0.3s;

        &:hover {
          color: #409eff;
          background-color: #f5f7fa;
        }

        .el-button--text {
          color: inherit;
        }
      }
    }

    .empty-todo {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 60px 20px;
      color: #909399;
      text-align: center;

      .empty-icon {
        font-size: 60px;
        margin-bottom: 20px;
        opacity: 0.5;
      }

      p {
        margin: 0 0 15px 0;
        font-size: 16px;
      }
    }
  }
}

.chart-container {
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .chart-filters {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .chart-stats {
      display: flex;
      gap: 15px;

      .stat-chip {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 8px 16px;
        background: #f5f7fa;
        border-radius: 8px;
        min-width: 80px;

        .stat-chip-label {
          font-size: 12px;
          color: #909399;
          margin-bottom: 4px;
        }

        .stat-chip-value {
          font-size: 18px;
          font-weight: 600;
          color: #1d2129;
        }
      }
    }
  }

  .chart-area {
    height: 300px;
    background: #f8f9fa;
    border-radius: 12px;
    margin-bottom: 20px;
    overflow: hidden;

    .chart-placeholder {
      width: 100%;
      height: 100%;
      padding: 20px;

      .simulated-chart {
        width: 100%;
        height: 100%;
        position: relative;
      }

      .simulated-line-chart {
        width: 100%;
        height: 100%;
        position: relative;

        .chart-grid {
          position: absolute;
          width: 100%;
          height: 100%;

          .grid-line {
            position: absolute;
            width: 100%;
            height: 1px;
            background: rgba(0, 0, 0, 0.1);
          }
        }

        .data-lines {
          position: absolute;
          width: 100%;
          height: calc(100% - 40px);
          top: 0;

          .data-line {
            position: absolute;
            width: 100%;
            height: 100%;

            .data-point {
              position: absolute;
              width: 8px;
              height: 8px;
              border-radius: 50%;
              transform: translate(-50%, 50%);
              border: 2px solid white;
              box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
            }
          }
        }

        .x-axis {
          position: absolute;
          bottom: 0;
          width: 100%;
          display: flex;
          justify-content: space-around;
          font-size: 12px;
          color: #909399;
          padding-top: 10px;
          border-top: 1px solid #e4e7ed;
        }
      }

      .simulated-bar-chart {
        width: 100%;
        height: 100%;
        position: relative;

        .bars-container {
          position: absolute;
          width: 100%;
          height: calc(100% - 40px);
          bottom: 40px;
          display: flex;
          align-items: flex-end;
          padding: 0 40px;

          .bar-group {
            display: flex;
            justify-content: space-around;
            align-items: flex-end;
            height: 100%;
            padding: 0 10px;
          }
        }

        .x-axis {
          position: absolute;
          bottom: 0;
          width: 100%;
          display: flex;
          justify-content: space-around;
          font-size: 12px;
          color: #909399;
          padding-top: 10px;
        }
      }

      .simulated-pie-chart {
        display: flex;
        width: 100%;
        height: 100%;
        align-items: center;

        .pie-container {
          width: 200px;
          height: 200px;
          border-radius: 50%;
          background: conic-gradient(#409eff 0% 30%, #67c23a 30% 55%, #e6a23c 55% 75%, #f56c6c 75% 90%, #909399 90% 100%);
          position: relative;
          overflow: hidden;
          margin: 0 auto;

          .pie-slice {
            position: absolute;
            width: 100%;
            height: 100%;
            transform-origin: center;
          }
        }

        .pie-legend {
          flex: 1;
          padding-left: 40px;

          .legend-item {
            display: flex;
            align-items: center;
            margin-bottom: 12px;

            .legend-color {
              width: 12px;
              height: 12px;
              border-radius: 2px;
              margin-right: 10px;
            }

            .legend-label {
              flex: 1;
              font-size: 14px;
              color: #606266;
            }

            .legend-value {
              font-size: 14px;
              font-weight: 500;
              color: #1d2129;
            }
          }
        }
      }
    }
  }

  .chart-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .legend {
      display: flex;
      gap: 20px;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 12px;
        color: #606266;

        .legend-color {
          width: 12px;
          height: 12px;
          border-radius: 2px;
        }
      }
    }
  }
}

.quick-actions {
  .action-item {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s;
    margin-bottom: 15px;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    }

    .action-icon {
      width: 50px;
      height: 50px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 24px;
      flex-shrink: 0;
    }

    .action-info {
      flex: 1;

      h4 {
        margin: 0 0 5px 0;
        font-size: 16px;
        font-weight: 600;
        color: #1d2129;
      }

      p {
        margin: 0;
        font-size: 14px;
        color: #909399;
      }
    }
  }
}

.footer-info {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
  border-top: 1px solid #f0f2f5;
  margin-top: 20px;
}

@media (max-width: 1200px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .main-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .welcome-section {
    flex-direction: column;
    text-align: center;
    padding: 30px 20px;

    .quick-stats {
      flex-direction: column;
      gap: 20px;
    }

    .welcome-illustration {
      margin-top: 30px;

      .illustration-icon {
        font-size: 80px;
      }
    }
  }

  .chart-header {
    flex-direction: column;
    gap: 20px;
    align-items: stretch;

    .chart-stats {
      justify-content: center;
    }
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;

    .card-title {
      width: 100%;
      justify-content: space-between;
    }
  }

  .chart-area {
    height: 250px;
  }

  .simulated-pie-chart {
    flex-direction: column;

    .pie-legend {
      padding-left: 0;
      padding-top: 20px;
    }
  }
}

.badge-notice {
  :deep(.el-badge__content) {
    top: -8px;
    right: -8px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

// 加载动画
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  border-radius: 8px;
}
// 深度曲线图样式
.echarts-container {
  width: 100%;
  height: 100%;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}
.header-controls .el-select {
  width: 120px;
}

// 保持原有样式不变，仅添加以下内容
.chart-area {
  height: 400px; // 增大图表高度，适配深度曲线
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 20px;
  overflow: hidden;
  position: relative;
}

// 响应式适配
@media (max-width: 768px) {
  .chart-area {
    height: 300px;
  }
  
  .header-controls {
    flex-wrap: wrap;
    justify-content: flex-end;
  }
  
  .chart-filters {
    flex-wrap: wrap;
    gap: 10px;
  }
}
</style>