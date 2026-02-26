import request from '@/utils/request'
import { uploadFileService } from '@/api/fileUpload'

/**
 * 报告模板管理API
 * 所有需要userId的接口都会通过请求拦截器自动注入userId参数
 * 无需在调用时手动传递userId
 */

// 获取模板列表
export const getTemplateList = (params) => {
  return request({
    url: '/api/report/templates',
    method: 'GET',
    params: {
      ...params
    }
  })
}

// 获取模板详情
export const getTemplateDetail = (id, params) => {
  return request({
    url: `/api/report/templates/${id}`,
    method: 'GET',
    params: {
      ...params
    }
  })
}

// 创建模板
export const createTemplate = (data, params) => {
  return request({
    url: '/api/report/templates',
    method: 'POST',
    params: { // userId通过拦截器自动注入，此处可传递其他参数
      ...params
    },
    data: data
  })
}

// 更新模板
export const updateTemplate = (id, data, params) => {
  return request({
    url: `/api/report/templates/${id}`,
    method: 'PUT',
    params: { // userId通过拦截器自动注入，此处可传递其他参数
      ...params
    },
    data: data
  })
}

// 删除模板
export const deleteTemplate = (id, params) => {
  return request({
    url: `/api/report/templates/${id}`,
    method: 'DELETE',
    params: { // userId通过拦截器自动注入，此处可传递其他参数
      ...params
    }
  })
}

// 使用模板生成报告
export const generateReportFromTemplate = (id, data) => {
  return request({
    url: `/api/report/templates/${id}/generate`,
    method: 'POST',
    data: data
  })
}

// 获取模板分类列表
export const getTemplateCategories = () => {
  return request({
    url: '/api/report/templates/categories',
    method: 'GET'
  })
}

// 获取模板使用统计
export const getTemplateStats = (params) => {
  return request({
    url: '/api/report/templates/stats',
    method: 'GET',
    params: {
      ...params
    }
  })
}

// 导出模板
export const exportTemplate = (id) => {
  return request({
    url: `/api/report/templates/${id}/export`,
    method: 'GET',
    responseType: 'blob'
  })
}

// 导出文件上传服务供模板使用
export { uploadFileService as uploadTemplateFile }