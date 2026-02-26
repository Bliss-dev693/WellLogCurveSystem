import * as reportTemplate from './reportTemplate'
import * as reportGeneration from './reportGeneration'

/**
 * 报告模块统一API入口
 * 整合报告模板和报告生成相关接口
 */

// 导出报告模板相关接口
export const template = {
  getList: reportTemplate.getTemplateList,
  getDetail: reportTemplate.getTemplateDetail,
  create: reportTemplate.createTemplate,
  update: reportTemplate.updateTemplate,
  delete: reportTemplate.deleteTemplate,
  generateReport: reportTemplate.generateReportFromTemplate,
  getCategories: reportTemplate.getTemplateCategories,
  getUsageStats: reportTemplate.getTemplateUsageStats,
  export: reportTemplate.exportTemplate,
  import: reportTemplate.importTemplate
}

// 导出报告生成相关接口
export const generation = {
  generatePreview: reportGeneration.generateReportPreview,
  generateFinal: reportGeneration.generateFinalReport,
  getHistory: reportGeneration.getReportHistory,
  getDetail: reportGeneration.getReportDetail,
  download: reportGeneration.downloadReport,
  delete: reportGeneration.deleteReport,
  getAvailableDatasets: reportGeneration.getAvailableDatasets,
  getAnalysisConfig: reportGeneration.getAnalysisConfig,
  saveDraft: reportGeneration.saveReportDraft,
  getDrafts: reportGeneration.getReportDrafts
}

// 默认导出所有接口
export default {
  template,
  generation
}