import request from '@/utils/request'

/**
 * 钻井工程预测
 * @param {Object} params - 请求参数
 * @param {Number} params.userId - 用户ID（仓库自动传入）
 * @param {String} params.datasetName - 数据集名
 * @param {String} params.wellName - 井名
 * @param {String} params.depthRange - 深度范围
 * @param {Array} params.requestData - 5个时间步的测井参数列表
 * @returns {Promise}
 */
export const drillingPredictService = ({ userId, datasetName, wellName, depthRange, requestData }) => {
  return request.post('/api/drilling/predict', requestData, {
    params: { userId: Number(userId), datasetName, wellName, depthRange },
    headers: { 'Content-Type': 'application/json;charset=UTF-8' }
  }).then(res => res.data)
}

/**
 * 分页查询当前用户的预测历史记录（自动携带仓库userId）
 * @param {Object} queryParams - 查询条件
 * @returns {Promise}
 */
export const getDrillingHistoryService = (queryParams = {}) => {
  const params = {
    ...queryParams,
    userId: queryParams.userId ? Number(queryParams.userId) : undefined
  }
  return request.get('/api/drilling/history', { params }).then(res => res.data)
}

/**
 * 按井名筛选并分页查询预测历史记录
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getDrillingHistoryByWellService = (params = {}) => {
  const queryParams = {
    pageNum: 1,
    pageSize: 10,
    ...params,
    pageNum: params.pageNum ? Number(params.pageNum) : 1,
    pageSize: params.pageSize ? Number(params.pageSize) : 10
  }
  return request.get('/api/drilling/history/by-well', { params: queryParams }).then(res => res.data)
}

/**
 * 导出指定用户的所有预测历史记录为CSV文件
 * @param {Number} userId - 必传，用户ID
 * @returns {Promise<Blob>} 返回二进制流Blob数据，用于前端下载/预览
 */
export const exportHistoryToCsv = (userId) => {
  return request({
    url: '/api/export/history/csv', // 后端导出接口地址
    method: 'get',
    params: { userId }, // 拼接请求参数（?userId=xxx）
    responseType: 'blob', // 核心：指定响应类型为二进制流，处理CSV文件
    skipIntercept: true // 新增：跳过响应拦截器的业务码校验
  })
}