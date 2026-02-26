import request from '@/utils/request'

/**
 * 文件上传到七牛云
 * @param {File} file - 要上传的文件对象
 * @returns {Promise}
 */
export const uploadFileService = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }, // 文件上传必须指定该类型
    timeout: 60000 // 文件上传超时时间设为60秒
  })
}