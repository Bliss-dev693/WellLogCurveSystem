import axios from 'axios'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'
import router from '@/router'

const baseURL = 'http://localhost:8080/'
const instance = axios.create({
  baseURL,
  timeout: 5000
})

instance.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    
    // 携带token
    if (userStore.token) {
      config.headers.Authorization = userStore.token
    }
    
    // 自动注入userId参数（如果用户已登录）
    if (userStore.user?.id) {
      // 确保params对象存在
      if (!config.params) {
        config.params = {}
      }
      
      // 如果params中没有userId，则自动注入
      if (!config.params.userId) {
        config.params.userId = userStore.user.id
      }
      
      // 如果data中包含userId但params中没有，则移动到params中
      if (config.data && config.data.userId && !config.params.userId) {
        config.params.userId = config.data.userId
        // 从data中移除userId避免重复
        delete config.data.userId
      }
    }
    
    return config
  },
  (err) => Promise.reject(err)
)

instance.interceptors.response.use(
  (res) => {
    // 新增：如果配置了skipIntercept，直接返回响应（跳过业务码校验）
    if (res.config.skipIntercept) {
      return res
    }
    // 原有JSON接口的业务码校验逻辑，保留不变
    if(res.data.code === 0||res.data.code === 200){
        return res
    }
    ElMessage.error(res.data.message||'服务异常')
    return Promise.reject(res.data)
  },
  (err) => {
    if(err.response?.status === 401){
        ElMessage.error('请先登录')
        const userStore = useUserStore()
        userStore.removeToken()
        router.push('/login')
    }else {
        ElMessage.error(err.response?.data?.message || '服务异常')
    }
    return Promise.reject(err)
  }
)

export { baseURL }
export default instance