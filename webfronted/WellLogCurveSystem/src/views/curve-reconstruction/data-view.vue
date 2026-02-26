<template>
  <PageContainer title="测井数据管理">
    <!-- 筛选查询栏 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :model="filterForm" inline class="filter-form" @submit.prevent="onSearch">
        <el-form-item label="数据集名">
          <el-input
            v-model="filterForm.datasetName"
            placeholder="请输入数据集名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="井名">
          <el-input
            v-model="filterForm.wellName"
            placeholder="请输入井名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select
            v-model="filterForm.isOriginal"
            placeholder="请选择数据类型"
            clearable
            style="width: 180px"
          >
            <el-option label="原始数据" :value="1" />
            <el-option label="预测数据" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="深度范围(m)">
          <el-input
            v-model="filterForm.minDepth"
            placeholder="最小深度"
            clearable
            type="number"
            step="0.1"
            style="width: 120px; margin-right: 8px"
          />
          <span>-</span>
          <el-input
            v-model="filterForm.maxDepth"
            placeholder="最大深度"
            clearable
            type="number"
            step="0.1"
            style="width: 120px; margin-left: 8px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="onSearch">查询</el-button>
          <el-button icon="el-icon-refresh-left" @click="resetFilter">重置</el-button>
          <el-button type="success" icon="el-icon-plus" @click="openFormDialog()">新增数据</el-button>
          <el-button 
            type="primary" 
            icon="el-icon-download" 
            @click="exportLogData"
            :loading="exportLoading"
          >
            导出所有测井数据为CSV
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="hover" style="margin-top: 16px;">
      <el-table
        :data="logDataList.records"
        v-loading="loading"
        border
        stripe
        row-key="id"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="datasetName" label="数据集名" width="150" align="center" />
        <el-table-column prop="wellName" label="井名" width="120" align="center" />
        <el-table-column prop="depth" label="深度(m)" width="120" align="center" />
        <el-table-column prop="ac" label="声波(ac)" width="100" align="center" />
        <el-table-column prop="cal" label="井径(cal)" width="100" align="center" />
        <el-table-column prop="gr" label="伽马(gr)" width="100" align="center" />
        <el-table-column prop="den" label="密度(den)" width="100" align="center" />
        <el-table-column prop="rt" label="深阻(rt)" width="100" align="center" />
        <el-table-column prop="rxo" label="浅阻(rxo)" width="100" align="center" />
        <el-table-column prop="confidence" label="置信度" width="100" align="center" />
        <el-table-column prop="isOriginal" label="数据类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isOriginal === 1 ? 'success' : 'primary'">
              {{ row.isOriginal === 1 ? '原始数据' : '预测数据' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间"  width="180" align="center" >
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.id && !isNaN(Number(row.id))"
              size="small" 
              type="primary" 
              icon="el-icon-edit" 
              @click="openFormDialog(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.id && !isNaN(Number(row.id))"
              size="small"
              type="danger"
              icon="el-icon-delete"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-if="logDataList.total > 0"
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="logDataList.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="logDataList.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="logDataList.total"
        style="margin-top: 20px; text-align: center"
      />
      <div v-else class="no-data" style="text-align: center; padding: 40px; color: #999;">
        暂无测井数据，请点击"新增数据"添加
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="formDialogTitle"
      width="70%"
      draggable
      destroy-on-close
    >
      <el-form
        :model="formData"
        ref="formRef"
        label-width="100px"
        :rules="formRules"
        class="form-container"
      >
        <!-- 编辑时隐藏存储ID，新增时无此字段，彻底隔离 -->
        <el-input v-model="formData.id" type="hidden" v-if="isEdit" />
        <el-input v-model="formData.userId" type="hidden" />
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="数据集名" prop="datasetName">
              <el-input
                v-model="formData.datasetName"
                placeholder="请输入数据集名"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="井名" prop="wellName">
              <el-input
                v-model="formData.wellName"
                placeholder="请输入井名"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="深度(m)" prop="depth">
              <el-input
                v-model="formData.depth"
                placeholder="请输入深度"
                type="number"
                step="0.1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="声波(ac)" prop="ac">
              <el-input
                v-model="formData.ac"
                placeholder="请输入声波值"
                type="number"
                step="0.1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="井径(cal)" prop="cal">
              <el-input
                v-model="formData.cal"
                placeholder="请输入井径值"
                type="number"
                step="0.1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="伽马(gr)" prop="gr">
              <el-input
                v-model="formData.gr"
                placeholder="请输入伽马值"
                type="number"
                step="0.1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="密度(den)" prop="den">
              <el-input
                v-model="formData.den"
                placeholder="请输入密度值"
                type="number"
                step="0.1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="深阻(rt)" prop="rt">
              <el-input
                v-model="formData.rt"
                placeholder="请输入深阻值"
                type="number"
                step="0.1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="浅阻(rxo)" prop="rxo">
              <el-input
                v-model="formData.rxo"
                placeholder="请输入浅阻值"
                type="number"
                step="0.1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="数据类型" prop="isOriginal">
              <el-select
                v-model="formData.isOriginal"
                placeholder="请选择数据类型"
                style="width: 100%"
              >
                <el-option label="原始数据" :value="1" />
                <el-option label="预测数据" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="置信度" prop="confidence">
              <el-input
                v-model="formData.confidence"
                placeholder="请输入置信度(0-1)"
                type="number"
                step="0.01"
                min="0"
                max="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleFormSubmit">确认{{ isEdit ? '编辑' : '新增' }}</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatTime } from '@/utils/format.js'
import PageContainer from '@/components/PageContainer.vue'
import { useUserStore } from '@/stores'
import {
  addLogDataService,
  deleteLogDataService,
  updateLogDataService,
  getLogDataByIdService,
  getAllLogDataService
} from '@/api/logData.js'

// ========== 1. 基础状态定义（新增id字段，编辑专用，新增时置空） ==========
const userStore = useUserStore()
const userId = ref(null)
const loading = ref(false)
const formRef = ref(null)
const formDialogVisible = ref(false)
const isEdit = ref(false)
const formDialogTitle = ref('新增测井数据')

const filterForm = reactive({
  datasetName: '',
  wellName: '',
  isOriginal: undefined,
  minDepth: undefined,
  maxDepth: undefined
})

const logDataList = reactive({
  records: [],
  total: 0,
  pageNum: 1,
  pageSize: 10
})

// 显式定义id字段（编辑专用），新增时始终置空，适配后端自增规范
const formData = reactive({
  id: '', // 编辑时赋值，新增时为空，彻底隔离
  userId: '',
  datasetName: '',
  wellName: '',
  depth: '',
  ac: '',
  cal: '',
  gr: '',
  den: '',
  rt: '',
  rxo: '',
  isOriginal: 1,
  confidence: 0.95
})

const formRules = ref({
  datasetName: [{ required: true, message: '请输入数据集名', trigger: 'blur' }],
  wellName: [{ required: true, message: '请输入井名', trigger: 'blur' }],
  depth: [{ required: true, message: '请输入深度', trigger: 'blur' }],
  ac: [{ required: true, message: '请输入声波值', trigger: 'blur' }],
  cal: [{ required: true, message: '请输入井径值', trigger: 'blur' }],
  gr: [{ required: true, message: '请输入伽马值', trigger: 'blur' }],
  den: [{ required: true, message: '请输入密度值', trigger: 'blur' }],
  rt: [{ required: true, message: '请输入深阻值', trigger: 'blur' }],
  rxo: [{ required: true, message: '请输入浅阻值', trigger: 'blur' }],
  isOriginal: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  confidence: [{ required: true, message: '请输入置信度', trigger: 'blur' }]
})

// ========== 2. 核心方法定义（彻底分离新增/编辑逻辑，新增无任何ID操作） ==========
const loadLogData = async () => {
  try {
    if (!userId.value) {
      ElMessage.warning('用户信息未加载完成，请稍候重试')
      return
    }
    loading.value = true
    const queryParams = {
      userId: userId.value,
      ...filterForm,
      pageNum: logDataList.pageNum,
      pageSize: logDataList.pageSize
    }
    const res = await getAllLogDataService(queryParams)
    if (res.code === 0) {
      logDataList.records = res.data.records || []
      logDataList.total = res.data.total || 0
      logDataList.pageNum = res.data.pageNum || 1
      logDataList.pageSize = res.data.pageSize || 10
    } else {
      ElMessage.error(res.message || '查询测井数据失败')
    }
  } catch (error) {
    console.error('查询测井数据异常:', error)
    ElMessage.error(error.response?.data?.message || '网络异常，查询失败')
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  logDataList.pageNum = 1
  loadLogData()
}

const resetFilter = () => {
  filterForm.datasetName = ''
  filterForm.wellName = ''
  filterForm.isOriginal = undefined
  filterForm.minDepth = undefined
  filterForm.maxDepth = undefined
  logDataList.pageNum = 1
  loadLogData()
}

/**
 * 打开新增/编辑弹窗 - 核心：新增逻辑完全独立，无任何ID相关操作；编辑严格校验ID
 * @param {Object} row 表格行数据（仅编辑时传入，新增时不传）
 */
const openFormDialog = async (row = null) => {
  // 重置表单校验状态
  if (formRef.value) {
    formRef.value.resetFields()
  }
  // 强制重置表单数据，新增时id置空，彻底杜绝残留
  Object.assign(formData, {
    id: '',
    userId: userId.value,
    datasetName: '',
    wellName: '',
    depth: '',
    ac: '',
    cal: '',
    gr: '',
    den: '',
    rt: '',
    rxo: '',
    isOriginal: 1,
    confidence: 0.95
  })

  // 编辑模式：仅传入row时执行，严格ID校验
  if (row) {
    const rowId = Number(row.id)
    // 双重校验ID有效性：非空 + 是有效数字 + 大于0（后端自增ID从1开始）
    if (!rowId || isNaN(rowId) || rowId <= 0) {
      ElMessage.error('数据ID无效，无法编辑')
      return
    }
    isEdit.value = true
    formDialogTitle.value = '编辑测井数据'
    try {
      loading.value = true
      const res = await getLogDataByIdService(rowId)
      if (res.code === 0) {
        Object.assign(formData, res.data) // 回显数据（含有效ID）
      } else {
        ElMessage.error(res.message || '获取数据详情失败')
        return
      }
    } catch (error) {
      console.error('获取数据详情异常:', error)
      ElMessage.error(`获取数据详情失败：${error.response?.data?.message || 'ID无效或数据已删除'}`)
      return
    } finally {
      loading.value = false
    }
  } else {
    // 新增模式：完全独立，无任何ID校验/操作，直接打开弹窗
    isEdit.value = false
    formDialogTitle.value = '新增测井数据'
    formData.userId = userId.value
  }
  // 打开弹窗（新增时无任何前置拦截）
  formDialogVisible.value = true
}

/**
 * 提交表单 - 核心：新增时剔除id字段，完全适配后端自增规范；编辑时带id提交
 */
const handleFormSubmit = async () => {
  try {
    // 表单前置校验
    await formRef.value.validate()
    loading.value = true
    let res
    if (isEdit.value) {
      // 编辑：带id提交，走更新接口
      res = await updateLogDataService(formData)
    } else {
      // 新增：剔除id字段，避免传递空id给后端，严格匹配后端请求参数规范
      const { id, ...addRequestData } = formData
      res = await addLogDataService(addRequestData)
    }
    if (res.code === 0) {
      ElMessage.success(res.message || (isEdit.value ? '编辑成功' : '新增成功'))
      formDialogVisible.value = false
      loadLogData() // 刷新表格，展示最新数据
    } else {
      ElMessage.error(res.message || (isEdit.value ? '编辑失败' : '新增失败'))
    }
  } catch (error) {
    console.error(isEdit.value ? '编辑数据异常:' : '新增数据异常:', error)
    ElMessage.error(
      error.response?.data?.message || 
      (isEdit.value ? '网络异常，编辑失败' : '网络异常，新增失败')
    )
  } finally {
    loading.value = false
  }
}

/**
 * 删除数据 - 严格ID校验，传递有效数字ID
 */
const handleDelete = async (id) => {
  const delId = Number(id)
  if (!delId || isNaN(delId) || delId <= 0) {
    ElMessage.error('数据ID无效，无法删除')
    return
  }
  try {
    await ElMessageBox.confirm(
      '确定要删除这条测井数据吗？删除后不可恢复！',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    loading.value = true
    const res = await deleteLogDataService(delId)
    if (res.code === 0) {
      ElMessage.success(res.message || '删除成功')
      loadLogData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除数据异常:', error)
      ElMessage.error(error.response?.data?.message || '网络异常，删除失败')
    }
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (val) => {
  logDataList.pageSize = val
  logDataList.pageNum = 1
  loadLogData()
}

const handleCurrentChange = (val) => {
  logDataList.pageNum = val
  loadLogData()
}

// ========== 3. 生命周期/监听逻辑 ==========
// 监听用户信息，获取有效用户ID（数字类型）
watch(
  () => userStore.user,
  (newUser) => {
    if (newUser?.id) {
      userId.value = Number(newUser.id)
      formData.userId = userId.value
      loadLogData() // 用户ID就绪后自动加载数据
    }
  },
  { deep: true, immediate: true }
)

// 页面初始化，主动获取用户信息
onMounted(() => {
  if (!userStore.user?.id) {
    userStore.getUser().catch(err => {
      ElMessage.error(`用户信息加载失败：${err.message}，无法操作测井数据`)
    })
  }
})

import { exportLogDataCsvService } from '@/api/logData'

const exportLoading = ref(false)
const currentUserId = userStore.user?.id // 从仓库获取当前用户ID

// 导出测井数据CSV
const exportLogData = async () => {
  if (!currentUserId) {
    ElMessage.warning('请先登录，再进行导出操作')
    return
  }

  exportLoading.value = true
  try {
    const response = await exportLogDataCsvService(currentUserId)
    const blob = response.data

    if (!blob || blob.size === 0) {
      throw new Error('服务端未返回任何测井数据')
    }

    // 浏览器原生下载逻辑
    const downloadUrl = window.URL.createObjectURL(blob)
    const aLink = document.createElement('a')
    aLink.download = '测井数据全量导出.csv'
    aLink.href = downloadUrl
    document.body.appendChild(aLink)
    aLink.click()
    document.body.removeChild(aLink)
    window.URL.revokeObjectURL(downloadUrl)

    ElMessage.success('测井数据导出成功，已自动下载')
  } catch (error) {
    console.error('测井数据导出失败：', error)
    ElMessage.error(`导出失败：${error.message || '服务端异常，请稍后重试'}`)
  } finally {
    exportLoading.value = false
  }
}
</script>

<style scoped>
.filter-card {
  margin-bottom: 0;
}

.filter-form {
  padding: 10px 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.table-card {
  min-height: 600px;
}

.form-container {
  padding: 10px 0;
}

.pagination {
  margin-top: 16px !important;
}

@media (max-width: 1400px) {
  .filter-form {
    gap: 10px 0;
  }
}
</style>