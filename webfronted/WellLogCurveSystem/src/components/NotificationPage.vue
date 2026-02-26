<template>
  <div class="notification-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h3 class="header-title">系统通知</h3>
        </div>
      </template>

      <!-- 通知过滤栏 -->
      <el-form :model="filters" inline class="filter-form">
        <el-form-item label="通知类型">
          <el-select 
            v-model="filters.type" 
            placeholder="请选择通知类型" 
            clearable
            @change="fetchNotifications"
          >
            <el-option label="全部" value="" />
            <el-option label="系统通知" value="system" />
            <el-option label="预警通知" value="warning" />
            <el-option label="任务提醒" value="task" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="filters.status" 
            placeholder="请选择状态" 
            clearable
            @change="fetchNotifications"
          >
            <el-option label="全部" value="" />
            <el-option label="未读" value="unread" />
            <el-option label="已读" value="read" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchNotifications">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 批量操作按钮 -->
      <div class="batch-operation" style="margin-bottom: 15px;">
        <el-button 
          type="success" 
          @click="batchMarkAsRead(selectedRows)"
          :disabled="selectedRows.length === 0"
        >
          批量标记已读
        </el-button>
      </div>

      <!-- 通知列表 -->
      <el-table 
        :data="notificationList.records" 
        v-loading="loading"
        style="width: 100%"
        row-key="id"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="通知标题" min-width="200" show-tooltip-when-overflow align="center" />
        <el-table-column prop="type" label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容摘要" min-width="250" show-tooltip-when-overflow align="center">
          <template #default="{ row }">
            {{ row.content?.length > 50 ? row.content.substring(0, 50) + '...' : row.content || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button 
              size="small" 
              @click="viewDetail(row)"
              type="primary"
            >
              查看详情
            </el-button>
            <el-button 
              size="small" 
              @click="markAsRead(row)"
              :type="row.status === 'unread' ? 'success' : 'info'"
              :disabled="row.status === 'read'"
            >
              {{ row.status === 'unread' ? '标记已读' : '已读' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页：完全仿照示例写法，移除废弃用法 -->
      <el-pagination
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="notificationList.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="notificationList.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="notificationList.total"
      />

      <!-- 详情对话框 -->
      <el-dialog v-model="detailDialogVisible" title="通知详情" width="60%" top="5vh" draggable>
        <div v-if="selectedNotification" class="detail-content">
          <el-descriptions :column="2" border size="large">
            <el-descriptions-item label="通知ID" align="center">{{ selectedNotification.id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="通知标题" align="center">{{ selectedNotification.title || '-' }}</el-descriptions-item>
            <el-descriptions-item label="通知类型" align="center">
              <el-tag :type="getTypeTagType(selectedNotification.type)" size="large">
                {{ getTypeText(selectedNotification.type) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态" align="center">
              <el-tag :type="getStatusTagType(selectedNotification.status)" size="large">
                {{ getStatusText(selectedNotification.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" align="center">{{ formatTime(selectedNotification.createTime) || '-' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间" align="center">{{ formatTime(selectedNotification.updateTime) || '-' }}</el-descriptions-item>
          </el-descriptions>

          <div class="content-section">
            <h4 class="section-title">通知内容</h4>
            <div class="notification-content">
              {{ selectedNotification.content || '暂无内容' }}
            </div>
          </div>
        </div>
        <template #footer>
          <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores';
import { formatTime } from '@/utils/format.js';
// 导入通知API服务
import {
  getNotificationsService,
  getNotificationDetailService,
  markNotificationAsReadService,
  batchMarkNotificationsAsReadService,
  getUnreadNotificationCountService,
} from '@/api/notification.js';

// ========== 1. 基础状态定义 ==========
const userStore = useUserStore();
let userId = ref(null); // 仓库获取的用户ID

// 页面基础状态
const loading = ref(false);

// 通知数据和过滤条件 - 严格匹配示例的分页数据结构
const notificationList = ref({ 
  records: [], 
  total: 0, 
  pageNum: 1, 
  pageSize: 10 
});
const filters = reactive({
  type: '',
  status: '',
  pageNum: 1,
  pageSize: 10
});

// 详情弹窗状态
const detailDialogVisible = ref(false);
const selectedNotification = ref(null);

// 批量操作相关
const selectedRows = ref([]);

// ========== 2. 核心函数定义 ==========
/**
 * 获取通知列表 - 调用真实API接口（优化数据类型转换）
 */
const fetchNotifications = async () => {
  try {
    if (!userId.value) {
      ElMessage.warning('用户信息未加载完成，请稍候');
      return;
    }

    loading.value = true;
    
    // 调用真实API接口
    const response = await getNotificationsService({
      ...filters,
      userId: userId.value
    });

    
    // 关键修复：正确处理API返回的数据结构
    // API返回格式：{code: 0, message: '操作成功', data: {records: [...], total: 15, pageNum: 1, pageSize: 10}}
    if (response && response.data) {
      notificationList.value = {
        records: Array.isArray(response.data.records) ? response.data.records : [],
        total: Number(response.data.total) || 0,
        pageNum: Number(response.data.pageNum) || 1,
        pageSize: Number(response.data.pageSize) || 10
      };
   
    } else {
      // 如果响应结构不符合预期，使用空数据
      notificationList.value = {
        records: [],
        total: 0,
        pageNum: 1,
        pageSize: 10
      };
    }
    
  } catch (error) {
    console.error('获取通知列表失败：', error);
    ElMessage.error('获取通知列表失败：' + (error.message || '未知错误'));
    // 出错时也设置空数据，避免页面报错
    notificationList.value = {
      records: [],
      total: 0,
      pageNum: 1,
      pageSize: 10
    };
  } finally {
    loading.value = false;
  }
};
/**
 * 重置过滤条件
 */
const resetFilters = () => {
  filters.type = '';
  filters.status = '';
  filters.pageNum = 1;
  filters.pageSize = 10;
  fetchNotifications();
};

/**
 * 查看详情 - 调用真实API接口
 */
const viewDetail = async (row) => {
  try {
    if (!userId.value) {
      ElMessage.warning('用户信息未加载完成，请稍候');
      return;
    }

    loading.value = true;
    
    // 调用API获取详细信息
    const response = await getNotificationDetailService(row.id, userId.value);
    // 修复：优先取response.data中的详情数据
    selectedNotification.value = response.data || response; 
    detailDialogVisible.value = true;
    
    // 如果是未读状态，自动标记为已读
    if (row.status === 'unread') {
      await markAsRead(row, false);
    }
    
  } catch (error) {
    console.error('获取通知详情失败：', error);
    ElMessage.error('获取通知详情失败：' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
};
/**
 * 标记为已读 - 调用真实API接口
 */
const markAsRead = async (row, showMessage = true) => {
  try {
    if (!userId.value) {
      if (showMessage) {
        ElMessage.warning('用户信息未加载完成，请稍候');
      }
      return;
    }

    // 调用API标记为已读
    await markNotificationAsReadService(row.id, userId.value);
    
    // 更新本地状态
    row.status = 'read';
    row.updateTime = new Date();
    
    if (showMessage) {
      ElMessage.success('已标记为已读');
    }
    
    // 更新未读数量
    fetchUnreadCount();
    
  } catch (error) {
    console.error('标记已读失败：', error);
    if (showMessage) {
      ElMessage.error('标记已读失败：' + (error.message || '未知错误'));
    }
  }
};

/**
 * 获取未读通知数量
 */
const fetchUnreadCount = async () => {
  try {
    if (!userId.value) return;
    
    const response = await getUnreadNotificationCountService(userId.value);
    // 修复：从response.data中提取未读数量
    const unreadCount = response.data?.unreadCount || 0;
 
    // 如果需要更新全局状态，可以通过事件或store传递
    // emit('update-unread-count', unreadCount);
    
  } catch (error) {
    console.error('获取未读通知数量失败：', error);
  }
};


/**
 * 处理表格选择变化
 */
const handleSelectionChange = (rows) => {
  selectedRows.value = rows;
};

/**
 * 批量标记为已读
 */
const batchMarkAsRead = async (rows) => {
  try {
    if (!userId.value) {
      ElMessage.warning('用户信息未加载完成，请稍候');
      return;
    }

    if (!Array.isArray(rows) || rows.length === 0) {
      ElMessage.warning('请选择要标记的通知');
      return;
    }

    const ids = rows.map(row => row.id);
    await batchMarkNotificationsAsReadService(ids, userId.value);
    
    // 更新本地状态
    rows.forEach(row => {
      row.status = 'read';
      row.updateTime = new Date();
    });
    
    ElMessage.success(`成功标记 ${rows.length} 条通知为已读`);
    fetchUnreadCount(); // 更新未读数量
    
  } catch (error) {
    console.error('批量标记已读失败：', error);
    ElMessage.error('批量标记已读失败：' + (error.message || '未知错误'));
  }
};

/**
 * 分页相关函数 - 完全仿照示例写法，确保参数类型正确
 */
const handleSizeChange = (val) => {
  // 强制转换为数字，避免字符串类型导致警告
  const size = Number(val);
  filters.pageSize = size;
  filters.pageNum = 1;
  fetchNotifications();
};

const handleCurrentChange = (val) => {
  // 强制转换为数字，避免字符串类型导致警告
  const page = Number(val);
  filters.pageNum = page;
  fetchNotifications();
};

/**
 * 获取类型对应的标签类型（修复空值问题）
 */
const getTypeTagType = (type) => {
  // 处理空值/undefined/null 情况
  if (!type || type === '') {
    return 'info';
  }
  
  // 映射规则：全部使用ElTag支持的类型
  const typeMap = {
    'system': 'info',    // 系统通知 → info
    'warning': 'warning',// 预警通知 → warning
    'task': 'primary'    // 任务提醒 → primary
  };
  
  // 兜底：未匹配的类型默认用info
  return typeMap[type] || 'info';
};

/**
 * 获取类型对应的文本
 */
const getTypeText = (type) => {
  if (!type || type === '') {
    return '未知类型';
  }
  
  const typeMap = {
    'system': '系统通知',
    'warning': '预警通知',
    'task': '任务提醒'
  };
  
  return typeMap[type] || '未知类型';
};

/**
 * 获取状态对应的标签类型（补充空值处理）
 */
const getStatusTagType = (status) => {
  // 处理空值/undefined/null 情况
  if (!status || status === '') {
    return 'info';
  }
  
  const statusMap = {
    'unread': 'warning',
    'read': 'info'
  };
  
  return statusMap[status] || 'info';
};

/**
 * 获取状态对应的文本
 */
const getStatusText = (status) => {
  if (!status || status === '') {
    return '未知状态';
  }
  
  const statusMap = {
    'unread': '未读',
    'read': '已读'
  };
  
  return statusMap[status] || '未知状态';
};

// ========== 3. 生命周期/监听逻辑 ==========
/**
 * 监听仓库用户信息，ID就绪后自动查询数据
 */
watch(
  () => userStore.user,
  (newUser) => {
    if (newUser?.id) {
      userId.value = Number(newUser.id);
      fetchNotifications();
      fetchUnreadCount(); // 获取未读数量
    }
  },
  { deep: true, immediate: true }
);

/**
 * 页面初始化：主动加载用户信息
 */
onMounted(() => {
  if (!userStore.user?.id) {
    userStore.getUser().catch(err => {
      ElMessage.error(`用户信息加载失败：${err.message}，无法查询通知列表`);
    });
  }
});
</script>

<style scoped>
.notification-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.main-card {
  max-width: 1600px;
  margin: 0 auto;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.filter-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.filter-form .el-form-item {
  margin-right: 20px;
  margin-bottom: 10px;
  width: 15%;
}

.batch-operation {
  padding: 0 15px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.detail-content {
  padding: 20px 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #e5e7eb;
}

.content-section {
  margin-top: 20px;
}

.notification-content {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border-left: 4px solid #409eff;
  line-height: 1.6;
  color: #333;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>