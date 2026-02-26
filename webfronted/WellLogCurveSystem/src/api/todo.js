import request from '@/utils/request'

/**
 * 获取待办事项列表（分页+筛选）
 * @param {Object} params 查询参数
 * @param {Number} params.userId 用户ID（必传）
 * @param {Number} [params.status] 状态：0-待完成，1-已完成，2-已取消
 * @param {Number} [params.priority] 优先级：0-低，1-中，2-高
 * @param {String} [params.category] 分类标签（模糊匹配）
 * @param {Number} [params.pageNum=1] 页码，默认1
 * @param {Number} [params.pageSize=10] 每页条数，默认10
 * @returns {Promise} 后端响应
 */
export const getTodoList = (params = {}) => {
  // 校验userId必传
  if (!params.userId || isNaN(Number(params.userId))) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  
  // 格式化参数
  const queryParams = {
    pageNum: 1,
    pageSize: 10,
    ...params,
    userId: Number(params.userId),
    pageNum: params.pageNum ? Number(params.pageNum) : 1,
    pageSize: params.pageSize ? Number(params.pageSize) : 10
  }
  
  // 移除undefined和空字符串参数
  Object.keys(queryParams).forEach(key => {
    if (queryParams[key] === undefined || queryParams[key] === '') {
      delete queryParams[key]
    }
  })
  
  return request.get('/todo/list', { params: queryParams }).then(res => res.data)
}

/**
 * 获取待办事项详情
 * @param {String|Number} id 待办事项ID
 * @param {Number} userId 用户ID（必传）
 * @returns {Promise} 后端响应
 */
export const getTodoDetail = (id, userId) => {
  // 校验参数
  const numericId = Number(id)
  const numericUserId = Number(userId)
  
  if (!numericId || isNaN(numericId)) {
    return Promise.reject(new Error('待办事项ID无效'))
  }
  if (!numericUserId || isNaN(numericUserId)) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  
  return request.get(`/todo/${numericId}`, { 
    params: { userId: numericUserId } 
  }).then(res => res.data)
}

/**
 * 新增待办事项
 * @param {Object} data 待办事项数据
 * @param {Number} data.userId 用户ID（必传）
 * @param {String} data.title 待办标题（必传，最大100字符）
 * @param {String} [data.content] 详细内容
 * @param {Number} [data.priority=1] 优先级：0-低，1-中，2-高，默认1
 * @param {String} [data.dueDate] 截止日期（格式：YYYY-MM-DDTHH:mm:ss）
 * @param {String} [data.category] 分类标签（最大20字符）
 * @returns {Promise} 后端响应
 */
export const createTodo = (data = {}) => {
  // 校验必填参数
  if (!data.userId || isNaN(Number(data.userId))) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  if (!data.title || data.title.trim() === '') {
    return Promise.reject(new Error('待办标题不能为空'))
  }
  if (data.title.length > 100) {
    return Promise.reject(new Error('待办标题不能超过100个字符'))
  }
  
  // 格式化数据
  const requestData = {
    userId: Number(data.userId),
    title: data.title.trim(),
    content: data.content ? data.content.trim() : '',
    priority: data.priority !== undefined ? Number(data.priority) : 1,
    dueDate: data.dueDate || null,
    category: data.category ? data.category.trim() : ''
  }
  
  // 验证优先级范围
  if (requestData.priority < 0 || requestData.priority > 2) {
    return Promise.reject(new Error('优先级参数无效，范围应为0-2'))
  }
  
  // 验证分类长度
  if (requestData.category.length > 20) {
    return Promise.reject(new Error('分类标签不能超过20个字符'))
  }
  
  return request.post('/todo/create', requestData, {
    headers: { 'Content-Type': 'application/json;charset=UTF-8' }
  }).then(res => res.data)
}

/**
 * 修改待办事项
 * @param {String|Number} id 待办事项ID
 * @param {Object} data 待办事项数据
 * @param {Number} data.userId 用户ID（必传）
 * @param {String} [data.title] 待办标题
 * @param {String} [data.content] 详细内容
 * @param {Number} [data.priority] 优先级
 * @param {String} [data.dueDate] 截止日期
 * @param {String} [data.category] 分类标签
 * @returns {Promise} 后端响应
 */
export const updateTodo = (id, data = {}) => {
  // 校验参数
  const numericId = Number(id)
  if (!numericId || isNaN(numericId)) {
    return Promise.reject(new Error('待办事项ID无效'))
  }
  
  if (!data.userId || isNaN(Number(data.userId))) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  
  // 格式化数据
  const requestData = {}
  const numericUserId = Number(data.userId)
  
  // 只添加存在的字段
  if (data.title !== undefined) {
    if (data.title.trim() === '') {
      return Promise.reject(new Error('待办标题不能为空'))
    }
    if (data.title.length > 100) {
      return Promise.reject(new Error('待办标题不能超过100个字符'))
    }
    requestData.title = data.title.trim()
  }
  
  if (data.content !== undefined) {
    requestData.content = data.content.trim()
  }
  
  if (data.priority !== undefined) {
    const priority = Number(data.priority)
    if (priority < 0 || priority > 2) {
      return Promise.reject(new Error('优先级参数无效，范围应为0-2'))
    }
    requestData.priority = priority
  }
  
  if (data.dueDate !== undefined) {
    requestData.dueDate = data.dueDate
  }
  
  if (data.category !== undefined) {
    if (data.category.length > 20) {
      return Promise.reject(new Error('分类标签不能超过20个字符'))
    }
    requestData.category = data.category.trim()
  }
  
  // 如果没有要更新的字段，返回错误
  if (Object.keys(requestData).length === 0) {
    return Promise.reject(new Error('没有要更新的字段'))
  }
  
  return request.put(`/todo/update/${numericId}`, requestData, {
    params: { userId: numericUserId },
    headers: { 'Content-Type': 'application/json;charset=UTF-8' }
  }).then(res => res.data)
}

/**
 * 修改待办事项状态
 * @param {String|Number} id 待办事项ID
 * @param {Object} data 状态数据
 * @param {Number} data.userId 用户ID（必传）
 * @param {Number} data.status 状态：0-待完成，1-已完成，2-已取消
 * @returns {Promise} 后端响应
 */
export const updateTodoStatus = (id, data = {}) => {
  // 校验参数
  const numericId = Number(id)
  if (!numericId || isNaN(numericId)) {
    return Promise.reject(new Error('待办事项ID无效'))
  }
  
  if (!data.userId || isNaN(Number(data.userId))) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  
  if (data.status === undefined) {
    return Promise.reject(new Error('状态不能为空'))
  }
  
  const status = Number(data.status)
  if (status < 0 || status > 2) {
    return Promise.reject(new Error('状态参数无效，范围应为0-2'))
  }
  
  const numericUserId = Number(data.userId)
  
  return request.put(`/todo/updateStatus/${numericId}`, 
    { status: status },
    {
      params: { userId: numericUserId },
      headers: { 'Content-Type': 'application/json;charset=UTF-8' }
    }
  ).then(res => res.data)
}

/**
 * 删除待办事项
 * @param {String|Number} id 待办事项ID
 * @param {Number} userId 用户ID（必传）
 * @returns {Promise} 后端响应
 */
export const deleteTodo = (id, userId) => {
  // 校验参数
  const numericId = Number(id)
  const numericUserId = Number(userId)
  
  if (!numericId || isNaN(numericId)) {
    return Promise.reject(new Error('待办事项ID无效'))
  }
  if (!numericUserId || isNaN(numericUserId)) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  
  return request.delete(`/todo/delete/${numericId}`, { 
    params: { userId: numericUserId } 
  }).then(res => res.data)
}

/**
 * 获取待办事项统计
 * @param {Number} userId 用户ID（必传）
 * @returns {Promise} 后端响应
 */
export const getTodoStatistics = (userId) => {
  const numericUserId = Number(userId)
  if (!numericUserId || isNaN(numericUserId)) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  
  return request.get('/todo/statistics', {
    params: { userId: numericUserId }
  }).then(res => res.data)
}

/**
 * 获取用户临期待办提醒（3天内到期）
 * @param {Number} userId 用户ID（必传）
 * @returns {Promise} 后端响应
 */
export const getExpireReminder = (userId) => {
  const numericUserId = Number(userId)
  if (!numericUserId || isNaN(numericUserId)) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  
  return request.get('/todo/expireReminder', {
    params: { userId: numericUserId }
  }).then(res => res.data)
}

export default {
  getTodoList,
  getTodoDetail,
  createTodo,
  updateTodo,
  updateTodoStatus,
  deleteTodo,
  getTodoStatistics,
  getExpireReminder
}