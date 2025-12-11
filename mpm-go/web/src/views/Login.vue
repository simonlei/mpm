<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>MPM 相册管理系统</h1>
        <p>Multi-Photo Manager</p>
      </div>
      
      <t-form
        ref="formRef"
        :data="formData"
        :rules="rules"
        label-align="top"
        @submit="onSubmit"
      >
        <t-form-item label="账号" name="account">
          <t-input
            v-model="formData.account"
            placeholder="请输入账号"
            size="large"
          />
        </t-form-item>
        
        <t-form-item label="密码" name="passwd">
          <t-input
            v-model="formData.passwd"
            type="password"
            placeholder="请输入密码"
            size="large"
            @keyup.enter="onSubmit"
          />
        </t-form-item>
        
        <t-form-item>
          <t-button
            theme="primary"
            type="submit"
            block
            size="large"
            :loading="loading"
          >
            登录
          </t-button>
        </t-form-item>
      </t-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { MessagePlugin } from 'tdesign-vue-next'
import { loginApi } from '@/api'
import { useUserStore } from '@/stores/user'
import CryptoJS from 'crypto-js'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const formData = reactive({
  account: '',
  passwd: ''
})

const rules = {
  account: [{ required: true, message: '请输入账号' }],
  passwd: [{ required: true, message: '请输入密码' }]
}

const onSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  loading.value = true
  
  try {
    const res = await loginApi(formData)
    
    if (res.code === 0) {
      userStore.login(res.data)
      MessagePlugin.success('登录成功')
      router.push('/')
    }
  } catch (error: any) {
    console.error('Login error:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 14px;
  color: #999;
}
</style>
