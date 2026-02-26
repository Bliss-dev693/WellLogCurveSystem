// src/api/systemMonitor.js
import request from '@/utils/request' // 导入你封装好的axios实例（注意路径匹配你的项目结构）

/**
 * 获取系统监控数据
 * @returns {Promise} 返回接口响应数据
 */
export function getSystemMonitorData() {
  return request({
    url: '/api/monitor/system', // 接口路径（baseURL已配置为http://localhost:8080/，此处拼接相对路径）
    method: 'get', // GET请求（获取数据用GET）
    // 如果需要跳过拦截器（比如测试），可以加skipIntercept: true
    // skipIntercept: true
  })
}

