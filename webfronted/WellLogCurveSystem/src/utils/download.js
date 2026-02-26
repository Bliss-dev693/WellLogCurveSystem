// src/utils/download.js
/**
 * 通用二进制流文件下载方法
 * @param {Blob} blob - 接口返回的blob二进制流
 * @param {String} fileName - 自定义下载文件名（默认：导出文件.csv）
 */
export const downloadBlobFile = (blob, fileName = '导出文件.csv') => {
  try {
    // 创建临时URL对象
    const url = window.URL.createObjectURL(blob)
    // 创建临时a标签
    const aLink = document.createElement('a')
    aLink.href = url
    // 设置下载文件名（后端已处理编码，前端可直接传中文）
    aLink.download = fileName
    // 模拟点击触发下载
    document.body.appendChild(aLink)
    aLink.click()
    // 清理资源：移除a标签、释放URL对象
    document.body.removeChild(aLink)
    window.URL.revokeObjectURL(url)
    return true
  } catch (error) {
    console.error('文件下载失败：', error)
    return false
  }
}