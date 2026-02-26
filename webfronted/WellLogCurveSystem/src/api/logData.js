import request from '@/utils/request'

/**
 * 添加测井数据记录
 * @param {Object} logData 测井数据实体（所有字段同接口文档）
 * @returns {Promise} 后端响应data
 */
export const addLogDataService = (logData) => {
  // 强制转换数字类型，匹配后端参数要求
  const data = {
    ...logData,
    userId: Number(logData.userId),
    depth: Number(logData.depth),
    ac: Number(logData.ac),
    cal: Number(logData.cal),
    gr: Number(logData.gr),
    den: Number(logData.den),
    rt: Number(logData.rt),
    rxo: Number(logData.rxo),
    isOriginal: Number(logData.isOriginal),
    confidence: Number(logData.confidence)
  }
  return request.post('/api/log-data', data, {
    headers: { 'Content-Type': 'application/json;charset=UTF-8' }
  }).then(res => res.data)
}

/**
 * 根据ID删除测井数据记录
 * @param {String|Number} id 测井数据ID
 * @returns {Promise} 后端响应data
 */
export const deleteLogDataService = (id) => {
  return request.delete(`/api/log-data/${id}`).then(res => res.data)
}

/**
 * 更新测井数据记录
 * @param {Object} logData 测井数据实体（必须包含id）
 * @returns {Promise} 后端响应data
 */
export const updateLogDataService = (logData) => {
  const data = {
    ...logData,
    id: Number(logData.id),
    userId: Number(logData.userId),
    depth: Number(logData.depth),
    ac: Number(logData.ac),
    cal: Number(logData.cal),
    gr: Number(logData.gr),
    den: Number(logData.den),
    rt: Number(logData.rt),
    rxo: Number(logData.rxo),
    isOriginal: Number(logData.isOriginal),
    confidence: Number(logData.confidence)
  }
  return request.put('/api/log-data', data, {
    headers: { 'Content-Type': 'application/json;charset=UTF-8' }
  }).then(res => res.data)
}

/**
 * 根据ID查询单条测井数据记录
 * @param {String|Number} id 测井数据ID
 * @returns {Promise} 后端响应data
 */
export const getLogDataByIdService = (id) => {
  // 强制转换为数字，避免传递字符串/undefined
  const numericId = Number(id)
  if (!numericId || isNaN(numericId)) {
    return Promise.reject(new Error('测井数据ID无效'))
  }
  return request.get(`/api/log-data/${numericId}`).then(res => res.data)
}


/**
 * 根据用户ID分页查询测井数据（userId必传）
 * @param {Object} params 查询参数
 * @returns {Promise} 后端响应data
 */
export const getLogDataByUserService = (params = {}) => {
  const queryParams = {
    userId: params.userId ? Number(params.userId) : undefined,
    minDepth: params.minDepth ? Number(params.minDepth) : undefined,
    maxDepth: params.maxDepth ? Number(params.maxDepth) : undefined,
    isOriginal: params.isOriginal ? Number(params.isOriginal) : undefined,
    pageNum: params.pageNum ? Number(params.pageNum) : 1,
    pageSize: params.pageSize ? Number(params.pageSize) : 10
  }
  return request.get('/api/log-data/by-user', { params: queryParams }).then(res => res.data)
}

/**
 * 根据井名分页查询测井数据（wellName、userId必传）
 * @param {Object} params 查询参数
 * @returns {Promise} 后端响应data
 */
export const getLogDataByWellService = (params = {}) => {
  const queryParams = {
    userId: params.userId ? Number(params.userId) : undefined,
    minDepth: params.minDepth ? Number(params.minDepth) : undefined,
    maxDepth: params.maxDepth ? Number(params.maxDepth) : undefined,
    isOriginal: params.isOriginal ? Number(params.isOriginal) : undefined,
    pageNum: params.pageNum ? Number(params.pageNum) : 1,
    pageSize: params.pageSize ? Number(params.pageSize) : 10
  }
  return request.get('/api/log-data/by-well', { params: queryParams }).then(res => res.data)
}

/**
 * 根据数据集名分页查询测井数据（datasetName、userId必传）
 * @param {Object} params 查询参数
 * @returns {Promise} 后端响应data
 */
export const getLogDataByDatasetService = (params = {}) => {
  const queryParams = {
    userId: params.userId ? Number(params.userId) : undefined,
    minDepth: params.minDepth ? Number(params.minDepth) : undefined,
    maxDepth: params.maxDepth ? Number(params.maxDepth) : undefined,
    isOriginal: params.isOriginal ? Number(params.isOriginal) : undefined,
    pageNum: params.pageNum ? Number(params.pageNum) : 1,
    pageSize: params.pageSize ? Number(params.pageSize) : 10
  }
  return request.get('/api/log-data/by-dataset', { params: queryParams }).then(res => res.data)
}

/**
 * 通用分页查询所有测井数据（所有参数可选）
 * @param {Object} params 筛选+分页参数
 * @returns {Promise} 后端响应data
 */
export const getAllLogDataService = (params = {}) => {
  const queryParams = {
    userId: params.userId ? Number(params.userId) : undefined,
    datasetName: params.datasetName || undefined, // 添加数据集名筛选
    wellName: params.wellName || undefined,       // 添加井名筛选
    minDepth: params.minDepth ? Number(params.minDepth) : undefined,
    maxDepth: params.maxDepth ? Number(params.maxDepth) : undefined,
    isOriginal: params.isOriginal ? Number(params.isOriginal) : undefined,
    pageNum: params.pageNum ? Number(params.pageNum) : 1,
    pageSize: params.pageSize ? Number(params.pageSize) : 10
  }
  return request.get('/api/log-data/all', { params: queryParams }).then(res => res.data)
}

/**
 * 获取用户数据集列表（新接口）
 * @param {Number} userId - 用户ID
 * @returns {Promise} 后端响应data（数据集名称数组）
 */
export const getUserDatasetsService = (userId) => {
  if (!userId) {
    return Promise.reject(new Error('用户ID不能为空'))
  }
  
  return request.get('/api/log-data/datasets', { 
    params: { userId: Number(userId) } 
  }).then(res => res.data)
}

// 新增：导出用户所有测井数据为CSV
export const exportLogDataCsvService = (userId) => {
  return request({
    url: '/api/log-data/export/csv',
    method: 'get',
    params: { userId },
    responseType: 'blob', // 关键：二进制流
    skipIntercept: true   // 关键：跳过JSON业务码校验，避免拦截器误判
  })
}

/**
 * 查询单井测井参数随深度变化的数据（适配ECharts可视化）
 * @param {Object} params 查询参数
 * @param {Number} params.userId - 必传，用户ID
 * @param {String} params.datasetName - 必传，数据集名
 * @param {String} params.wellName - 必传，井名
 * @param {Number} [params.minDepth] - 可选，最小深度
 * @param {Number} [params.maxDepth] - 可选，最大深度
 * @param {String} [params.params=ac,cal,gr,den,rt,rxo] - 可选，返回参数（逗号分隔）
 * @param {Number} [params.sampleRate=1] - 可选，采样率（越大数据越少）
 * @param {Number} [params.isOriginal] - 可选，是否原始数据：1-是/0-否
 * @returns {Promise} 后端响应data（包含ECharts可视化数据）
 */
export const getLogDataDepthVariationService = (params = {}) => {
  // 1. 参数校验：必传参数检查
  if (!params.userId) {
    return Promise.reject(new Error('用户ID不能为空'))
  }
  if (!params.datasetName) {
    return Promise.reject(new Error('数据集名不能为空'))
  }
  if (!params.wellName) {
    return Promise.reject(new Error('井名不能为空'))
  }

  // 2. 构建查询参数（类型转换+默认值处理）
  const queryParams = {
    // 必传参数（强制转数字/字符串）
    userId: Number(params.userId),
    datasetName: String(params.datasetName).trim(),
    wellName: String(params.wellName).trim(),
    // 可选参数（数字类型转换，无则设为undefined）
    minDepth: params.minDepth ? Number(params.minDepth) : undefined,
    maxDepth: params.maxDepth ? Number(params.maxDepth) : undefined,
    sampleRate: params.sampleRate ? Number(params.sampleRate) : 1, // 默认采样率1
    isOriginal: params.isOriginal ? Number(params.isOriginal) : undefined,
    // 参数列表默认值：ac,cal,gr,den,rt,rxo
    params: params.params ? String(params.params).trim() : 'ac,cal,gr,den,rt,rxo'
  }

  // 3. 采样率范围限制（1-100）
  if (queryParams.sampleRate < 1) {
    queryParams.sampleRate = 1
  } else if (queryParams.sampleRate > 100) {
    queryParams.sampleRate = 100
  }

  // 4. 发起请求（保持与现有接口一致的风格）
  return request.get('/api/log-data/depth-variation', { 
    params: queryParams 
  }).then(res => res.data)
}