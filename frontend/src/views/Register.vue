<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>用户注册</h2>
        </div>
      </template>

      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-position="left" label-width="80px" @submit.prevent="submitForm">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 220px;"
            ref="usernameInputRef"
          >
            <template #suffix>
              <el-icon v-if="checkingUsername" class="is-loading"><Loading /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
            style="width: 220px;"
          ></el-input>
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            show-password
            clearable
            style="width: 220px;"
          ></el-input>
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            clearable
            style="width: 220px;"
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 150px; margin-left: 80px;" native-type="submit">
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="form-footer">
        <p>
          已有账号？
          <router-link to="/login">去登录</router-link>
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import type { FormInstance, FormRules } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import axios from '../utils/axios'

const userStore = useUserStore()
const router = useRouter()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const checkingUsername = ref(false)
const usernameInputRef = ref<HTMLElement | null>(null)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const validatePass = (_rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 检查用户名是否已存在
const validateUsername = (_rule: any, value: string, callback: any) => {
  if (value === '') {
    callback(new Error('请输入用户名'))
    return
  }

  if (value.length < 3 || value.length > 20) {
    callback(new Error('用户名长度应为3到20个字符'))
    return
  }

  // 检查用户名是否已存在
  checkingUsername.value = true
  axios.post('http://localhost:8080/api/users/check-username', { username: value })
    .then(response => {
      checkingUsername.value = false
      if (response.data && response.data.exists) {
        callback(new Error('用户名已存在，请更换其他用户名'))
      } else {
        callback()
      }
    })
    .catch(() => {
      checkingUsername.value = false
      // 如果接口调用失败，允许表单提交，后端会再次验证
      callback()
    })
}

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3到20个字符', trigger: 'blur' },
    { validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

// 在组件挂载时，将焦点设置到用户名输入框
onMounted(async () => {
  // 确保DOM已渲染完成
  await nextTick()
  
  // 将焦点设置到用户名输入框
  if (usernameInputRef.value) {
    // 使用Element Plus的focus方法
    (usernameInputRef.value as any).focus()
  }
})

const submitForm = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true

      try {
        const result = await userStore.register(
          registerForm.username,
          registerForm.password,
          registerForm.email
        )

        if (result.success) {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(result.message)
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 350px;
}

.card-header {
  text-align: center;
}

.form-footer {
  margin-top: 20px;
  text-align: center;
}

.form-footer a {
  color: #409eff;
  text-decoration: none;
}
</style>