import { defineStore } from 'pinia'
import { ref } from 'vue'

interface User {
  id: number
  account: string
  name: string
  is_admin: boolean
  face_id: number
  signature: string
}

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const signature = ref<string>('')
  const account = ref<string>('')
  const isAuthenticated = ref<boolean>(false)

  // 从localStorage恢复用户信息
  const checkAuth = () => {
    const storedUser = localStorage.getItem('user')
    const storedSignature = localStorage.getItem('signature')
    const storedAccount = localStorage.getItem('account')
    
    if (storedUser && storedSignature && storedAccount) {
      user.value = JSON.parse(storedUser)
      signature.value = storedSignature
      account.value = storedAccount
      isAuthenticated.value = true
    }
  }

  // 登录
  const login = (userData: User) => {
    user.value = userData
    signature.value = userData.signature
    account.value = userData.account
    isAuthenticated.value = true
    
    localStorage.setItem('user', JSON.stringify(userData))
    localStorage.setItem('signature', userData.signature)
    localStorage.setItem('account', userData.account)
  }

  // 登出
  const logout = () => {
    user.value = null
    signature.value = ''
    account.value = ''
    isAuthenticated.value = false
    
    localStorage.removeItem('user')
    localStorage.removeItem('signature')
    localStorage.removeItem('account')
  }

  return {
    user,
    signature,
    account,
    isAuthenticated,
    checkAuth,
    login,
    logout
  }
})
