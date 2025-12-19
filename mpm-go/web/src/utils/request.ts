import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { MessagePlugin } from 'tdesign-vue-next'
import { useUserStore } from '@/stores/user'
import router from '@/router'

export interface ApiResponse<T = any> {
  code: number
  data: T
  error?: {
    code: number
    message: string
  }
}

const service: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    if (userStore.signature && userStore.account) {
      config.headers['Signature'] = userStore.signature
      config.headers['Account'] = userStore.account
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    
    // 处理错误响应
    if (res.error) {
      MessagePlugin.error(res.error.message || '请求失败')
      if (res.error.code === -20001 && res.error.message?.includes('签名')) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      return Promise.reject(new Error(res.error.message || '请求失败'))
    }
    
    return response.data
  },
  (error) => {
    console.error('Response error:', error)
    
    if (error.response?.status === 403) {
      MessagePlugin.error('无权限访问')
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    } else if (error.response?.status === 401) {
      MessagePlugin.error('未登录或登录已过期')
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
    } else {
      MessagePlugin.error(error.message || '网络错误')
    }
    
    return Promise.reject(error)
  }
)

export default service
