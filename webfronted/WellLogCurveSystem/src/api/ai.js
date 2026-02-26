// src/api/ai.js
/**
 * AI流式对话接口（适配非标准SSE响应）
 * @param {Object} params - 请求参数
 * @param {Array} params.messages - 对话消息数组
 * @param {String} [params.model] - AI模型名称
 * @param {Boolean} [params.enableThinking=true] - 是否启用思考链
 * @param {Number} [params.temperature=0.7] - 温度参数
 * @param {Number} [params.maxTokens] - 最大生成token数
 * @param {Function} onMessage - 接收单条消息的回调
 * @param {Function} [onError] - 错误回调
 * @param {Function} [onClose] - 连接关闭回调
 * @returns {Function} 取消连接的方法
 */
  import { useUserStore } from '@/stores/modules/user'
export function chatStream(params, onMessage, onError, onClose) {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  const url = `${baseUrl}/api/chat/stream`

  const userStore = useUserStore()

  const fetchOptions = {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      // 如需token认证，添加：Authorization: `Bearer ${localStorage.getItem('token')}`
      Authorization: userStore.token

    },
    body: JSON.stringify({
      enableThinking: true,
      temperature: 0.7,
      ...params
    })
  }

  fetch(url, fetchOptions)
    .then(async (res) => {
      if (!res.ok) {
        throw new Error(`接口请求失败，状态码：${res.status}`)
      }

      const reader = res.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let isAborted = false

      const readStream = async () => {
        try {
          while (!isAborted) {
            const { done, value } = await reader.read()
            if (done) {
              onClose?.()
              return
            }
            const rawText = decoder.decode(value).trim()
            if (rawText) {

              try {
                const message = JSON.parse(rawText)
                if (message.type === 'done') {
                  message.isDone = true
                }
                onMessage(message)
                // 收到结束标志，终止流
                if (message.isDone) {
                  reader.releaseLock()
                  onClose?.()
                  return
                }
              } catch (parseErr) {
                // 忽略解析失败的非JSON数据
                console.warn('SSE数据解析失败', parseErr, rawText)
              }
            }
          }
        } catch (err) {
          if (!isAborted) {
            onError?.(err.message || '流式连接异常')
          }
        }
      }

      readStream()

      return () => {
        isAborted = true
        reader.releaseLock()
        onClose?.()
      }
    })
    .catch((err) => {
      onError?.(err.message || '接口请求初始化失败')
    })
}