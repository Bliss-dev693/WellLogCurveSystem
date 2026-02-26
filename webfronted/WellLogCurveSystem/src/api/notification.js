import request from '@/utils/request'

/**
 * 获取通知列表（分页+筛选）
 * @param {Object} params 查询参数
 * @param {Number} [params.pageNum=1] 页码
 * @param {Number} [params.pageSize=10] 每页条数
 * @param {String} [params.type=''] 通知类型
 * @param {String} [params.status=''] 状态
 * @param {Number} params.userId 用户ID（必传）
 * @returns {Promise} 后端响应data
 */
export const getNotificationsService = (params = {}) => {
  // 校验userId必传
  if (!params.userId || isNaN(Number(params.userId))) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  // 格式化参数
  const queryParams = {
    pageNum: 1,
    pageSize: 10,
    type: '',
    status: '',
    ...params,
    pageNum: params.pageNum ? Number(params.pageNum) : 1,
    pageSize: params.pageSize ? Number(params.pageSize) : 10,
    userId: Number(params.userId)
  }
  return request.get('/api/notifications', { params: queryParams }).then(res => res.data)
}

/**
 * 获取通知详情
 * @param {String|Number} id 通知ID
 * @param {Number} userId 用户ID（必传）
 * @returns {Promise} 后端响应data
 */
export const getNotificationDetailService = (id, userId) => {
  // 校验参数
  const numericId = Number(id)
  const numericUserId = Number(userId)
  if (!numericId || isNaN(numericId)) {
    return Promise.reject(new Error('通知ID无效'))
  }
  if (!numericUserId || isNaN(numericUserId)) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  return request.get(`/api/notifications/${numericId}`, { 
    params: { userId: numericUserId } 
  }).then(res => res.data)
}

/**
 * 标记通知为已读
 * @param {String|Number} id 通知ID
 * @param {Number} userId 用户ID（必传）
 * @returns {Promise} 后端响应data
 */
export const markNotificationAsReadService = (id, userId) => {
  const numericId = Number(id)
  const numericUserId = Number(userId)
  if (!numericId || isNaN(numericId)) {
    return Promise.reject(new Error('通知ID无效'))
  }
  if (!numericUserId || isNaN(numericUserId)) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  return request.patch(`/api/notifications/${numericId}/read`, { status: 'read' }, {
    params: { userId: numericUserId },
    headers: { 'Content-Type': 'application/json;charset=UTF-8' }
  }).then(res => res.data)
}

/**
 * 批量标记通知为已读
 * @param {Array<Number|String>} ids 通知ID数组
 * @param {Number} userId 用户ID（必传）
 * @returns {Promise} 后端响应data
 */
export const batchMarkNotificationsAsReadService = (ids, userId) => {
  // 校验参数
  if (!Array.isArray(ids) || ids.length === 0) {
    return Promise.reject(new Error('请选择要标记的通知'))
  }
  const numericUserId = Number(userId)
  if (!numericUserId || isNaN(numericUserId)) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  const numericIds = ids.map(id => {
    const numId = Number(id)
    if (!numId || isNaN(numId)) {
      throw new Error(`通知ID ${id} 无效`)
    }
    return numId
  })
  return request.patch('/api/notifications/batch-read', { 
    ids: numericIds,
    userId: numericUserId // 放到请求体中
  }, {
    headers: { 'Content-Type': 'application/json;charset=UTF-8' }
  }).then(res => res.data)
}

/**
 * 获取当前用户未读通知数量
 * @param {Number} userId 用户ID（必传）
 * @returns {Promise} 后端响应data
 */
export const getUnreadNotificationCountService = (userId) => {
  const numericUserId = Number(userId)
  if (!numericUserId || isNaN(numericUserId)) {
    return Promise.reject(new Error('用户ID不能为空且必须为数字'))
  }
  return request.get('/api/notifications/unread-count', {
    params: { userId: numericUserId }
  }).then(res => res.data)
}