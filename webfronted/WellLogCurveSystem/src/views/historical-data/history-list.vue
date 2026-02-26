<template>
  <div class="history-list-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h3 class="header-title">钻井预测历史记录</h3>
        </div>
      </template>

      <!-- 搜索过滤栏：无用户ID输入项，仅从仓库自动获取 -->
      <el-form :model="filters" inline class="filter-form">
        <el-form-item label="井名">
          <el-input 
            v-model="filters.wellName" 
            placeholder="请输入井名" 
            @keyup.enter="fetchHistoryList"
            clearable
          />
        </el-form-item>
        <el-form-item label="数据集名称">
          <el-input 
            v-model="filters.datasetName" 
            placeholder="请输入数据集名称" 
            @keyup.enter="fetchHistoryList"
            clearable
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select 
            v-model="filters.status" 
            placeholder="请选择状态" 
            clearable
            @change="fetchHistoryList"
          >
            <el-option label="成功" value="success" />
            <el-option label="失败" value="fail" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchHistoryList">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="exportHistoryList" :loading="exportLoading">
          导出历史预测记录为CSV
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 历史记录表格 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部记录" name="all">
          <el-table 
            :data="historyList.records" 
            v-loading="loading"
            style="width: 100%"
            row-key="id"
            border
            stripe
          >
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="userId" label="用户ID" width="100" align="center" />
            <el-table-column prop="datasetName" label="数据集名称" width="150" show-tooltip-when-overflow align="center" />
            <el-table-column prop="wellName" label="井名" width="120" align="center" />
            <el-table-column prop="depthRange" label="深度范围" width="120" align="center" />
            <el-table-column prop="cnlPrediction" label="预测值" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="row.cnlPrediction > 0.5 ? 'success' : 'warning'">
                  {{ row.cnlPrediction || '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="executionTime" label="执行时间(ms)" width="120" align="center" />
            <el-table-column prop="createTime" label="创建时间" width="180" align="center" >
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180" align="center" >
              <template #default="{ row }">
                {{ formatTime(row.updateTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right" align="center">
              <template #default="{ row }">
                <el-button 
                  size="small" 
                  @click="viewDetail(row)"
                  type="primary"
                  icon="el-icon-view"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <!-- 分页 -->
      <el-pagination
        v-if="activeTab === 'all'"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="historyList.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="historyList.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="historyList.total"
      />

      <el-pagination
        v-else
        class="pagination"
        @size-change="handleWellSizeChange"
        @current-change="handleWellCurrentChange"
        :current-page="wellHistoryList.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="wellHistoryList.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="wellHistoryList.total"
      />

      <!-- 详情对话框 -->
      <el-dialog v-model="detailDialogVisible" title="历史记录详情" width="80%" top="5vh" draggable>
        <div v-if="selectedRecord" class="detail-content">
          <el-descriptions :column="2" border size="large">
            <el-descriptions-item label="记录ID" align="center">{{ selectedRecord.id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户ID" align="center">{{ selectedRecord.userId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="数据集名称" align="center">{{ selectedRecord.datasetName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="井名" align="center">{{ selectedRecord.wellName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="深度范围" align="center">{{ selectedRecord.depthRange || '-' }}</el-descriptions-item>
            <el-descriptions-item label="预测值" align="center">
              <el-tag :type="selectedRecord.cnlPrediction > 0.5 ? 'success' : 'warning'" size="large">
                {{ selectedRecord.cnlPrediction || '-' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="执行状态" align="center">
              <el-tag :type="getStatusTagType(selectedRecord.status)" size="large">
                {{ getStatusText(selectedRecord.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="执行耗时" align="center">{{ selectedRecord.executionTime || '-' }} ms</el-descriptions-item>
            <el-descriptions-item label="创建时间" align="center">{{ selectedRecord.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间" align="center">{{ selectedRecord.updateTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="错误信息" span="2" v-if="selectedRecord.errorMessage">
              <el-tag type="danger" size="large">{{ selectedRecord.errorMessage }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <div class="input-data-section" v-if="selectedRecord.inputPredictionData">
            <h4 class="section-title">输入预测数据</h4>
            <pre class="json-pre">{{ formatJson(selectedRecord.inputPredictionData) }}</pre>
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
import { 
  getDrillingHistoryService, 
  getDrillingHistoryByWellService
  ,exportHistoryToCsv
} from '@/api/drilling.js'
import { useUserStore } from '@/stores';
import { formatTime } from '@/utils/format.js';

// ========== 1. 先导入依赖和定义基础状态（无函数调用） ==========
const userStore = useUserStore();
let userId = ref(null); // 仓库获取的用户ID

// 页面基础状态（所有变量定义在前，函数调用在后）
const activeTab = ref('all');
const loading = ref(false);
const wellLoading = ref(false);

// 全部记录-数据和过滤条件
const historyList = ref({ records: [], total: 0, pageNum: 1, pageSize: 10 });
const filters = reactive({
  wellName: '',
  datasetName: '',
  status: '',
  pageNum: 1,
  pageSize: 10
});

// 按井名查询-数据和过滤条件
const wellHistoryList = ref({ records: [], total: 0, pageNum: 1, pageSize: 10 });
const wellFilters = reactive({
  wellName: '',
  datasetName: '',
  status: '',
  pageNum: 1,
  pageSize: 10
});

// 详情弹窗状态
const detailDialogVisible = ref(false);
const selectedRecord = ref(null);

// ========== 2. 核心函数定义（所有函数在调用前初始化完成） ==========
/**
 * 获取全部历史记录 - 自动携带仓库userId，必传参数
 */
const fetchHistoryList = async () => {
  try {
    if (!userId.value) {
      ElMessage.warning('用户ID未加载完成，请稍候重试');
      return;
    }

    loading.value = true;
    const params = {
      userId: userId.value,
      datasetName: filters.datasetName.trim() || undefined,
      wellName: filters.wellName.trim() || undefined,
      status: filters.status || undefined,
      pageNum: Number(filters.pageNum),
      pageSize: Number(filters.pageSize)
    };

    const response = await getDrillingHistoryService(params);
    if (response.code === 0 && response.data) {
      historyList.value = {
        records: response.data.records || [],
        total: response.data.total || 0,
        pageNum: response.data.pageNum || 1,
        pageSize: response.data.pageSize || 10
      };
      historyList.value.records.length === 0 && ElMessage.info('暂无匹配的历史记录');
    } else {
      ElMessage.error(response.message || '获取历史记录失败');
    }
  } catch (error) {
    console.error('获取全部历史记录失败:', error);
    const errMsg = error.response?.data?.message || error.message || '网络异常';
    ElMessage.error(`获取历史记录失败：${errMsg}`);
  } finally {
    loading.value = false;
  }
};

/**
 * 按井名查询历史记录
 */
const fetchWellHistoryList = async () => {
  try {
    if (!wellFilters.wellName.trim()) {
      ElMessage.warning('请输入井名后再查询');
      return;
    }

    wellLoading.value = true;
    const params = {
      wellName: wellFilters.wellName.trim(),
      datasetName: wellFilters.datasetName.trim() || undefined,
      status: wellFilters.status || undefined,
      pageNum: Number(wellFilters.pageNum),
      pageSize: Number(wellFilters.pageSize)
    };

    const response = await getDrillingHistoryByWellService(params);
    if (response.code === 0 && response.data) {
      wellHistoryList.value = {
        records: response.data.records || [],
        total: response.data.total || 0,
        pageNum: response.data.pageNum || 1,
        pageSize: response.data.pageSize || 10
      };
      wellHistoryList.value.records.length === 0 && ElMessage.info('暂无匹配的井名记录');
    } else {
      ElMessage.error(response.message || '获取井名记录失败');
    }
  } catch (error) {
    console.error('按井名查询失败:', error);
    const errMsg = error.response?.data?.message || error.message || '网络异常';
    ElMessage.error(`按井名查询失败：${errMsg}`);
  } finally {
    wellLoading.value = false;
  }
};

/**
 * 辅助方法（所有工具函数统一定义，避免分散）
 */
// 重置全部记录过滤条件
const resetFilters = () => {
  filters.wellName = '';
  filters.datasetName = '';
  filters.status = '';
  filters.pageNum = 1;
  fetchHistoryList();
};
// 重置按井名查询过滤条件
const resetWellFilters = () => {
  wellFilters.wellName = '';
  wellFilters.datasetName = '';
  wellFilters.status = '';
  wellFilters.pageNum = 1;
};
// 标签页切换
const handleTabChange = (tabName) => {
  activeTab.value = tabName;
  tabName === 'all' && fetchHistoryList();
};
// 全部记录分页-每页条数变化
const handleSizeChange = (val) => { 
  filters.pageSize = val; 
  fetchHistoryList(); 
};
// 全部记录分页-当前页变化
const handleCurrentChange = (val) => { 
  filters.pageNum = val; 
  fetchHistoryList(); 
};
// 按井名分页-每页条数变化
const handleWellSizeChange = (val) => { 
  wellFilters.pageSize = val; 
  fetchWellHistoryList(); 
};
// 按井名分页-当前页变化
const handleWellCurrentChange = (val) => { 
  wellFilters.pageNum = val; 
  fetchWellHistoryList(); 
};
// 状态转标签类型
const getStatusTagType = (status) => {
  return status === 'success' ? 'success' : status === 'fail' ? 'danger' : 'info';
};
// 状态转中文文本
const getStatusText = (status) => {
  return status === 'success' ? '执行成功' : status === 'fail' ? '执行失败' : status || '未知状态';
};
// 查看详情
const viewDetail = (record) => {
  selectedRecord.value = record;
  detailDialogVisible.value = true;
};
// 格式化JSON
const formatJson = (str) => {
  try {
    return JSON.stringify(JSON.parse(str), null, 2);
  } catch (e) {
    return str || '无输入数据' ;
  }
};

// ========== 3. 生命周期/监听逻辑（最后定义，确保所有函数已初始化） ==========
/**
 * 监听仓库用户信息，ID就绪后自动查询数据
 * （此时fetchHistoryList已定义完成，可正常调用）
 */
watch(
  () => userStore.user,
  (newUser) => {
    if (newUser?.id) {
      userId.value = Number(newUser.id); // 强制转数字，匹配后端Integer
      fetchHistoryList(); // 函数已初始化，无引用错误
      ElMessage.success('用户信息加载完成，已自动查询历史记录');
    }
  },
  { deep: true, immediate: true }
);

/**
 * 页面初始化：主动加载用户信息（防止仓库未初始化）
 */
onMounted(() => {
  if (!userStore.user?.id) {
    userStore.getUser().catch(err => {
      ElMessage.error(`用户信息加载失败：${err.message}，无法查询历史记录`);
    });
  }
});
// history-list.vue 中导出逻辑（最终版）
const exportLoading = ref(false);
const exportHistoryList = async () => {
  // 仅保留用户ID前置校验
  if (!userId.value) {
    ElMessage.warning('用户ID未加载完成，无法导出，请稍候重试');
    return;
  }

  exportLoading.value = true;
  try {
    // 调用带跳过拦截器的导出接口
    const response = await exportHistoryToCsv(userId.value);
    const blob = response.data;

    // 仅做最基础的非空校验（避免空文件）
    if (!blob || blob.size === 0) {
      throw new Error('服务端未返回任何导出数据');
    }

    // 原生浏览器下载逻辑（无任何多余操作）
    const downloadUrl = window.URL.createObjectURL(blob);
    const aLink = document.createElement('a');
    aLink.download = '钻井预测历史记录.csv'; // 与后端文件名一致
    aLink.href = downloadUrl;
    document.body.appendChild(aLink);
    aLink.click();
    // 强制清理临时资源，避免内存泄漏
    document.body.removeChild(aLink);
    window.URL.revokeObjectURL(downloadUrl);

    ElMessage.success('钻井预测历史记录导出成功，已自动下载');
  } catch (error) {
    // 仅捕获真正的异常（网络错误、空数据、接口404/500）
    console.error('CSV导出失败详情：', error);
    ElMessage.error(`导出失败：${error.message || '服务端异常，请稍后重试'}`);
  } finally {
    // 无论成败，关闭加载状态
    exportLoading.value = false;
  }
};
</script>

<style scoped>
.history-list-container {
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
  width: 200px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

/* 详情弹窗样式 */
.detail-content {
  padding: 10px 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 20px 0 10px 0;
  border-left: 4px solid #409eff;
  padding-left: 8px;
}

.json-pre {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 15px;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 400px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
  color: #1e293b;
}

/* 表格/弹窗样式优化 */
:deep(.el-table) {
  --el-table-header-text-color: #fff;
  --el-table-header-bg-color: #409eff;
  --el-table-row-hover-bg-color: #e8f4ff;
}

:deep(.el-table th) {
  background-color: #409eff !important;
  color: #fff !important;
  font-weight: 500;
}

:deep(.el-dialog__header) {
  background-color: #e8f4ff;
  border-bottom: 1px solid #e5e7eb;
  padding: 15px 20px;
}

:deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1989fa;
}
</style>