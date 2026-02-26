<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { formatTime } from '@/utils/format.js'
import { useUserStore } from '@/stores'
import * as reportTemplateApi from '@/api/reportTemplate.js'
import { uploadFileService } from '@/api/fileUpload.js' // 导入文件上传服务
import router from '@/router'
import { Plus, Refresh, Search, Document, View, Edit, Delete, Upload, Warning, Check, Close } from '@element-plus/icons-vue'

defineOptions({
  name: 'ReportTemplates'
})

// 用户状态
const userStore = useUserStore()
const userId = computed(() => userStore.user?.id)

// 响应式数据
const templateList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const uploadRef = ref() // 添加上传组件引用
const documentPreviewUrl = ref('') // 文档预览URL

const searchKeyword = ref('')
const categoryFilter = ref('') // 修复：初始值改为空字符串，与后端保持一致

const formMode = ref('create')
const form = reactive({
  id: null,
  name: '',
  description: '',
  category: 'basic',
  documentUrl: '', // 存储上传后的文件URL
  variables: '{}',
  isPublic: false
})

const formRef = ref()

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择模板分类', trigger: 'change' }
  ],
  documentUrl: [
    { required: true, message: '请上传文档文件', trigger: 'change' }
  ]
}

// 模板分类选项
const categoryOptions = ref([
  { value: 'basic', label: '基础报告' },
  { value: 'technical', label: '技术分析' },
  { value: 'comprehensive', label: '综合报告' },
  { value: 'specialized', label: '专项分析' },
  { value: 'custom', label: '自定义模板' }
])

// 计算属性
const filteredTemplates = computed(() => {
  let templates = []
  
  // 确保templateList是数组
  if (Array.isArray(templateList.value)) {
    templates = templateList.value
  } else if (templateList.value && typeof templateList.value === 'object') {
    // 如果是对象，尝试获取records或list字段
    templates = templateList.value.records || templateList.value.list || []
  }
  
  // 应用搜索过滤（仅当前端需要额外过滤时使用）
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    templates = templates.filter(template => 
      template.name?.toLowerCase().includes(keyword) ||
      template.description?.toLowerCase().includes(keyword)
    )
  }
  
  // 注意：分类筛选已由后端处理，前端不再重复筛选
  // 如果需要前端筛选，可以取消下面的注释
  /*
  if (categoryFilter.value) {
    templates = templates.filter(template => 
      template.category === categoryFilter.value
    )
  }
  */
  
  return templates
})


// 获取模板列表
const loadTemplates = async () => {
  if (!userId.value) {
    console.warn('用户未登录，无法加载模板列表')
    templateList.value = [] // 初始化为空数组
    return
  }
  
  loading.value = true
  try {
    const params = {
      userId: Number(userId.value),
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    // 添加搜索关键字参数
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    // 添加分类筛选参数（修复：传递实际选择的分类值）
    if (categoryFilter.value) {
      params.category = categoryFilter.value
    }
    
    const response = await reportTemplateApi.getTemplateList(params)
    
    if (response.data.code === 0 || response.data.code === 200) {
      // 处理分页数据结构
      const data = response.data.data || response.data
      templateList.value = data.records || data.list || data || []
      
      // 更新分页信息
      if (data.total !== undefined) {
        pagination.total = data.total
      }
      
      console.log('模板列表加载成功:', templateList.value.length, '条数据')
    } else {
      throw new Error(response.data.message || response.data.msg || '获取模板列表失败')
    }
  } catch (error) {
    console.error('加载模板失败:', error)
    templateList.value = [] // 出错时初始化为空数组
    ElMessage.error('加载模板失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}



// 获取分类列表
const loadCategories = async () => {
  if (!userId.value) return
  
  try {
    const response = await reportTemplateApi.getTemplateCategories()
    if (response.code === 0 || response.code === 200) {
      categoryOptions.value = response.data?.map(cat => ({
        value: cat,
        label: getCategoryLabel(cat)
      })) || categoryOptions.value
    }
  } catch (error) {
    console.warn('获取分类列表失败，使用默认分类:', error)
  }
}

// 获取分类标签
const getCategoryLabel = (category) => {
  const labels = {
    'basic': '基础报告',
    'technical': '技术分析',
    'comprehensive': '综合报告',
    'specialized': '专项分析',
    'custom': '自定义模板'
  }
  return labels[category] || category
}

// 新建模板
const handleCreate = () => {
  formMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

// 编辑模板
const handleEdit = (template) => {
  formMode.value = 'edit'
  Object.assign(form, {
    id: template.id,
    name: template.name,
    description: template.description,
    category: template.category,
    documentUrl: template.documentUrl,
    isPublic: template.isPublic
  })
  dialogVisible.value = true
}

// 删除模板
const handleDelete = async (template) => {
  if (!userId.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除模板 "${template.name}" 吗？此操作不可撤销。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
    
    const response = await reportTemplateApi.deleteTemplate(template.id)
   
    
    if (response.data.code === 0 || response.data.code === 200) {
      const index = templateList.value.findIndex(item => item.id === template.id)
      if (index > -1) {
        templateList.value.splice(index, 1)
      }
      ElMessage.success('模板删除成功')
    } else {
      throw new Error(response.data.message || response.data.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除模板失败:', error)
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 使用模板
const handleUseTemplate = async (template) => {
  if (!userId.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    // 跳转到报告生成页面并传递模板ID
    window.location.hash = `#/report/generation?templateId=${template.id}`
    ElMessage.success(`已选择模板: ${template.name}，正在跳转到报告生成页面...`)
    router.push({ path: '/report/generation', query: { templateId: template.id } })
  } catch (error) {
    console.error('使用模板失败:', error)
    ElMessage.error('使用模板失败：' + (error.message || '未知错误'))
  }
}

// 预览模板
const handlePreview = (template) => {
  console.log('预览模板:', template.documentUrl)
  // 显示文档URL而不是内容预览
  ElMessageBox.alert(
    `<div style="padding: 20px;">
      <p><strong>文档地址：</strong></p>
      <p><a href="${template.documentUrl}" target="_blank" style="color: #409eff;">${template.documentUrl}</a></p>
      <p style="margin-top: 15px; font-size: 14px; color: #666;">
        点击链接可在新窗口中查看模板文档
      </p>
    </div>`,
    `模板预览: ${template.name}`,
    {
      dangerouslyUseHTMLString: true,
      customClass: 'template-preview-dialog'
    }
  )
}

// 文件上传相关方法
const onFileChange = (file) => {
  // 显示文件名预览
  form.documentUrl = file.name
  documentPreviewUrl.value = ''
}

const onFileRemove = () => {
  form.documentUrl = ''
  documentPreviewUrl.value = ''
}

const uploadDocument = async (file) => {
  try {
    const res = await uploadFileService(file.raw)
    if (res.data.code === 0 || res.data.code === 200) {
      const fileUrl = res.data.data // 从正确的路径获取文件URL
      form.documentUrl = fileUrl
      documentPreviewUrl.value = fileUrl
      ElMessage.success('文件上传成功')
      return fileUrl
    } else {
      throw new Error(res.data.message || res.data.msg || '文件上传失败')
    }
  } catch (error) {
    console.error('文件上传失败:', error)
    ElMessage.error('文件上传失败: ' + (error.message || '网络错误'))
    throw error
  }
}

// 保存模板
const handleSave = async () => {
  if (!userId.value) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    // 表单验证
    if (formRef.value) {
      await formRef.value.validate()
    }
    
    // 如果有文件需要上传
    let documentUrl = form.documentUrl
    if (uploadRef.value && uploadRef.value.$el.querySelector('input').files.length > 0) {
      const file = uploadRef.value.$el.querySelector('input').files[0]
      documentUrl = await uploadDocument({ raw: file })
    }
    
    const templateData = {
      userId: userId.value,
      name: form.name,
      description: form.description,
      category: form.category,
      documentUrl: documentUrl,
      isPublic: form.isPublic,
      variables: '{}' // 默认空的变量定义
    }
    
    // 确保userId为有效数字
    const userIdNumber = Number(userId.value)
    if (isNaN(userIdNumber) || userIdNumber <= 0) {
      ElMessage.error('用户ID无效，请重新登录')
      return
    }
    
    let response
    if (formMode.value === 'edit') {
      response = await reportTemplateApi.updateTemplate(form.id, templateData, { userId: userIdNumber })
      
    } else {
      response = await reportTemplateApi.createTemplate(templateData, { userId: userIdNumber })
   
    }
    
    if (response.data.code === 0 || response.data.code === 200) {
      ElMessage.success(formMode.value === 'edit' ? '模板更新成功' : '模板创建成功')
      dialogVisible.value = false
      await loadTemplates() // 重新加载列表
    } else {
      throw new Error(response.data.message || response.data.msg || '保存失败')
    }
  } catch (error) {
    if (error instanceof Error) {
      ElMessage.error((formMode.value === 'edit' ? '更新' : '创建') + '失败：' + error.message)
    } else {
      console.error('保存模板失败:', error)
      ElMessage.error((formMode.value === 'edit' ? '更新' : '创建') + '失败：未知错误')
    }
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    category: 'basic',
    documentUrl: '',
    isPublic: false
  })
  documentPreviewUrl.value = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
  // 重置文件上传组件
  if (uploadRef.value) {
    const input = uploadRef.value.$el.querySelector('input')
    if (input) {
      input.value = ''
    }
  }
}

// 格式化分类显示
const getCategoryTagType = (category) => {
  const typeMap = {
    basic: 'primary',
    technical: 'success',
    comprehensive: 'warning',
    specialized: 'danger',
    custom: 'info'
  }
  return typeMap[category] || 'info'
}

// 获取分类名称
const getCategoryName = (category) => {
  return categoryOptions.value.find(opt => opt.value === category)?.label || category
}

// 监听用户状态变化
watch(() => userStore.user, (newUser) => {
  if (newUser?.id) {
    loadTemplates()
    loadCategories()
  }
}, { immediate: true })

// 监听筛选条件变化，自动重新加载数据
watch([searchKeyword, categoryFilter], () => {
  // 重置到第一页
  pagination.currentPage = 1
  loadTemplates()
}, { deep: true })

// 组件挂载
onMounted(async () => {
  if (userId.value) {
    await Promise.all([
      loadCategories(),
      loadTemplates()
    ])
  }
})
</script>

<template>
  <page-container title="报告模板管理">
    <!-- 操作工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建模板
        </el-button>
        <el-button @click="loadTemplates" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      
      <div class="toolbar-right">
        <el-select 
          v-model="categoryFilter" 
          placeholder="全部分类" 
          clearable
          style="width: 150px; margin-right: 15px;"
        >
          <el-option label="全部分类" value="" />
          <el-option
            v-for="option in categoryOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        
        <el-input
          v-model="searchKeyword"
          placeholder="搜索模板名称、描述"
          style="width: 300px;"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 模板统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ templateList.length }}</div>
          <div class="stat-label">总模板数</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ templateList.filter(t => t.isPublic).length }}</div>
          <div class="stat-label">公开模板</div>
        </div>
      </el-card>
      <el-card class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ templateList.reduce((sum, t) => sum + (t.usageCount || 0), 0) }}</div>
          <div class="stat-label">总使用次数</div>
        </div>
      </el-card>
    </div>

    <!-- 模板列表 -->
    <el-table 
      :data="filteredTemplates" 
      v-loading="loading"
      stripe
      style="width: 100%; margin-top: 20px;"
      row-class-name="template-row"
    >
      <el-table-column prop="name" label="模板名称" min-width="200">
        <template #default="{ row }">
          <div class="template-info">
            <div class="template-name-wrapper">
              <el-tag 
                :type="row.isPublic ? 'success' : 'info'" 
                size="small"
                effect="dark"
              >
                {{ row.isPublic ? '公开' : '私有' }}
              </el-tag>
              <span class="template-name">{{ row.name }}</span>
            </div>
            <div class="template-desc">{{ row.description }}</div>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="categoryName" label="分类" width="120">
        <template #default="{ row }">
          <el-tag :type="getCategoryTagType(row.category)" size="small">
            {{ row.categoryName || getCategoryName(row.category) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <!-- <el-table-column prop="creator" label="创建人" width="120" /> -->
      
      <el-table-column prop="usageCount" label="使用次数" width="120" sortable>
        <template #default="{ row }">
          <span class="usage-count">{{ row.usageCount || 0 }}</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="updateTime" label="更新时间" width="180" sortable>
        <template #default="{ row }">
          {{ formatTime(row.updateTime) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <div class="operation-buttons">
            <el-button size="small" type="primary" @click="handleUseTemplate(row)">
              <el-icon><Document /></el-icon>
              使用
            </el-button>
            <el-button size="small" @click="handlePreview(row)">
              <el-icon><View /></el-icon>
              预览
            </el-button>
            <el-button size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <div v-if="filteredTemplates.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无匹配的模板">
        <el-button type="primary" @click="handleCreate">创建第一个模板</el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="filteredTemplates.length > 0">
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        :total="filteredTemplates.length"
        :page-size="10"
        @current-change="() => {}"
      />
    </div>

    <!-- 模板编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="formMode === 'edit' ? '编辑模板' : '新建模板'"
      width="700px"
      :before-close="() => { dialogVisible = false; resetForm() }"
      class="template-dialog"
    >
      <el-form
        :model="form"
        :rules="rules"
        ref="formRef"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="模板名称" prop="name">
              <el-input 
                v-model="form.name" 
                placeholder="请输入模板名称"
                maxlength="50"
                show-word-limit
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模板分类" prop="category">
              <el-select 
                v-model="form.category" 
                placeholder="请选择分类" 
                style="width: 100%"
              >
                <el-option
                  v-for="option in categoryOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="模板描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入模板描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="模板文档" prop="documentUrl">
          <el-upload
            ref="uploadRef"
            class="document-uploader"
            :auto-upload="false"
            :show-file-list="true"
            :on-change="onFileChange"
            :on-remove="onFileRemove"
            :limit="1"
            accept=".doc,.docx,.pdf,.xls,.xlsx,.ppt,.pptx"
          >
            <el-button type="primary" :icon="Upload">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传Word、Excel、PowerPoint或PDF文档，大小不超过10MB
              </div>
            </template>
          </el-upload>
          
          <!-- 文件预览 -->
          <div v-if="documentPreviewUrl" class="document-preview">
            <el-link 
              :href="documentPreviewUrl" 
              target="_blank" 
              type="primary"
              :underline="false"
            >
              <el-icon><Document /></el-icon>
              {{ form.documentUrl }}
            </el-link>
          </div>
        </el-form-item>
        
        <el-form-item label="公开状态">
          <el-switch
            v-model="form.isPublic"
            active-text="公开"
            inactive-text="私有"
          />
          <div class="form-tip">
            <el-icon><Warning /></el-icon>
            公开模板可供所有用户使用，私有模板仅自己可见
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false; resetForm()">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
          <el-button 
            type="primary" 
            @click="handleSave"
            :loading="false"
          >
            <el-icon><Check /></el-icon>
            {{ formMode === 'edit' ? '更新模板' : '创建模板' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </page-container>
</template>

<style scoped lang="scss">
// 工具栏样式
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.toolbar-left, .toolbar-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

// 统计卡片样式
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.stat-content {
  text-align: center;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

// 模板信息样式
.template-info {
  .template-name-wrapper {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 5px;
  }
  
  .template-name {
    font-weight: 500;
    font-size: 15px;
  }
  
  .template-desc {
    font-size: 13px;
    color: #909399;
    line-height: 1.4;
  }
}

.usage-count {
  font-weight: 500;
  color: #409eff;
}

// 空状态样式
.empty-state {
  text-align: center;
  padding: 60px 0;
}

// 分页样式
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

// 表单提示样式
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  display: flex;
  align-items: center;
  gap: 5px;
}

// 对话框样式
.template-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
}

.template-url-input {
  :deep(.el-input__inner) {
    font-family: 'Consolas', 'Monaco', monospace;
    font-size: 13px;
  }
}

// 文件上传样式
.document-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    
    &:hover {
      border-color: var(--el-color-primary);
    }
  }
  
  :deep(.el-upload-dragger) {
    padding: 20px;
  }
}

.document-preview {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  
  :deep(.el-link) {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon {
      font-size: 16px;
    }
  }
}

// 对话框底部按钮
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 表格行样式
:deep(.template-row) {
  &:hover {
    background-color: #f5f7fa;
  }
}

:deep(.el-table) {
  .el-table__cell {
    padding: 15px 0;
  }
}

// 操作按钮样式
.operation-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
  
  .el-button {
    margin: 0;
    padding: 8px 12px;
    
    .el-icon {
      margin-right: 4px;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .toolbar-left, .toolbar-right {
    width: 100%;
    justify-content: center;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  :deep(.el-table) {
    font-size: 12px;
    
    .el-table__cell {
      padding: 10px 0;
    }
  }
}

// 模板预览对话框样式
:global(.template-preview-dialog) {
  .el-message-box__content {
    max-height: 400px;
    overflow-y: auto;
  }
  
  pre {
    margin: 0;
    font-size: 13px;
    line-height: 1.5;
  }
}
</style>