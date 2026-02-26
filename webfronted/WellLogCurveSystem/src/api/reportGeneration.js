import request from '@/utils/request'

/**
 * 报告生成相关API
 * 所有接口均需传递 userId 参数以确保数据访问权限控制
 */

// 生成报告预览
export const generateReportPreview = (data) => {
  // 将userId从data中提取出来作为params参数
  const { userId, ...reportData } = data
  return request({
    url: '/api/report/generate-preview',
    method: 'POST',
    data: reportData,
    params: { userId }
  })
}

// 生成最终报告
export const generateFinalReport = (data) => {
  // 将userId从data中提取出来作为params参数
  const { userId, ...reportData } = data
  return request({
    url: '/api/report/generate-final',
    method: 'POST',
    data: reportData,
    params: { userId }
  })
}

// 获取报告历史记录
export const getReportHistory = (userId) => {
  return request({
    url: '/api/report/history',
    method: 'GET',
    params: { userId }
  })
}

// 获取报告详情
export const getReportDetail = (id, userId) => {
  return request({
    url: `/api/report/${id}`,
    method: 'GET',
    params: { userId }
  })
}

// 下载报告
export const downloadReport = (id, userId, format = 'markdown') => {
  return request({
    url: `/api/report/${id}/download`,
    method: 'GET',
    params: { userId, format },
    responseType: 'blob'
  })
}

// 删除报告
export const deleteReport = (id, userId) => {
  return request({
    url: `/api/report/${id}`,
    method: 'DELETE',
    params: { userId }
  })
}

// 获取可用数据集列表
export const getAvailableDatasets = (params) => {
  return request({
    url: '/api/datasets/available',
    method: 'GET',
    params
  })
}

// 获取分析参数配置
export const getAnalysisConfig = () => {
  return request({
    url: '/api/report/analysis-config',
    method: 'GET'
  })
}

// 保存报告草稿
export const saveReportDraft = (data) => {
  return request({
    url: '/api/report/draft',
    method: 'POST',
    data
  })
}

// 获取报告草稿列表
export const getReportDrafts = (params) => {
  return request({
    url: '/api/report/drafts',
    method: 'GET',
    params
  })
}