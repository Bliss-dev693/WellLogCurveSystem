
import request from '@/utils/request' // 导入你封装好的axios实例（路径需匹配项目实际结构）

/**
 * 获取用户系统统计数据
 * @param {Object} params - 请求参数（URL参数）
 * @param {number} params.userId - 用户ID（必传，如15）
 * @returns {Promise} 返回接口响应数据
 */
export function getUserSystemStatistics(params) {
  return request({
    url: '/api/statistics/system', // 接口相对路径（baseURL已配置为http://localhost:8080/，自动拼接）
    method: 'get', // GET请求（查询统计数据用GET）
    params // 传递URL参数（最终拼接为?userId=xxx）
  })
}