<template>
  <div class="login-container" ref="loginContainerRef">
    <!-- 移除粒子效果 -->

    <div class="login-content">
      <div class="login-title">
        <h1>智慧书城</h1>
        <p>探索知识的海洋，发现阅读的乐趣</p>
      </div>
      <el-card class="login-card">
        <template #header>
          <div class="card-header">
            <el-radio-group v-model="loginType" style="width: 100%;">
              <el-radio-button value="user" label="用户登录"></el-radio-button>
              <el-radio-button value="admin" label="管理员登录"></el-radio-button>
            </el-radio-group>
          </div>
        </template>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-position="left" label-width="70px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 220px;"
          ></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
            style="width: 220px;"
          ></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading" style="width: 150px; margin-left: 70px;">
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="form-footer">
        <p v-if="loginType === 'user'">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </p>
        <!-- 移除管理员账号密码提示 -->
      </div>
    </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import { useCartStore } from '../store/cart'
import type { FormInstance, FormRules } from 'element-plus'
// 已移除粒子效果导入

const userStore = useUserStore()
const cartStore = useCartStore()
const router = useRouter()

const loginFormRef = ref<FormInstance>()
const loginContainerRef = ref<HTMLElement | null>(null)
const loading = ref(false)
const loginType = ref('user') // 默认用户登录

const loginForm = reactive({
  username: '',
  password: ''
})

// 在组件挂载时，从localStorage加载上次登录的用户名
onMounted(() => {
  // 移除粒子动画初始化

  // 加载上次登录的用户名
  const savedUserUsername = localStorage.getItem('lastUserUsername')
  const savedAdminUsername = localStorage.getItem('lastAdminUsername')

  // 根据当前登录类型设置用户名
  if (loginType.value === 'user' && savedUserUsername) {
    loginForm.username = savedUserUsername
  } else if (loginType.value === 'admin' && savedAdminUsername) {
    loginForm.username = savedAdminUsername
  }
})

// 移除组件卸载时的粒子清理代码

// 监听登录类型变化，切换时加载对应的用户名
watch(loginType, (newType) => {
  // 切换登录类型时清空密码
  loginForm.password = ''

  if (newType === 'user') {
    const savedUsername = localStorage.getItem('lastUserUsername')
    loginForm.username = savedUsername || ''
  } else {
    const savedUsername = localStorage.getItem('lastAdminUsername')
    loginForm.username = savedUsername || ''
  }
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应为3到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应为6到20个字符', trigger: 'blur' }
  ]
})

const submitForm = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true

      try {
        const result = await userStore.login(loginForm.username, loginForm.password, loginType.value)

        if (result.success) {
          ElMessage.success({
            message: '登录成功',
            duration: 1000 // 设置为1秒
          })

          // 保存用户名到localStorage，但不保存密码
          if (loginType.value === 'user') {
            localStorage.setItem('lastUserUsername', loginForm.username)
          } else {
            localStorage.setItem('lastAdminUsername', loginForm.username)
          }

          // 如果是普通用户，同步购物车数据
          if (loginType.value === 'user' && !userStore.isAdmin) {
            // 先从本地加载购物车数据
            cartStore.loadFromLocalStorage()

            // 然后同步到服务器
            if (cartStore.items.length > 0) {
              try {
                await cartStore.syncCartToServer()
                console.log('购物车数据已同步到服务器')
              } catch (error) {
                console.error('同步购物车数据失败:', error)
              }
            } else {
              // 如果本地购物车为空，从服务器加载
              try {
                await cartStore.fetchCart()
                console.log('已从服务器加载购物车数据')
              } catch (error) {
                console.error('从服务器加载购物车数据失败:', error)
              }
            }
          }

          // 如果是管理员且登录成功，直接跳转到管理页面
          if (loginType.value === 'admin' && userStore.isAdmin) {
            router.push('/admin')
          } else {
            router.push('/home')
          }
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
/* 导入基础样式 */
/* 注意：这里的样式会覆盖login-background.css中的同名样式 */

.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('/bookshelf-bg.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* 移除粒子效果样式 */

.login-card {
  width: 100%;
  max-width: 350px;
  position: relative;
  z-index: 2;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  border-radius: 10px;
  background-color: rgba(255, 255, 255, 0.95);
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
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
  font-weight: bold;
  transition: color 0.3s;
}

.form-footer a:hover {
  color: #66b1ff;
}

.login-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.login-title h1 {
  font-size: 3rem;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.4);
  color: #fff;
  font-weight: bold;
  letter-spacing: 3px;
}

.login-title p {
  font-size: 1.3rem;
  color: #fff;
  font-weight: 500;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.5);
}

/* 适配暗色模式 */
@media (prefers-color-scheme: dark) {
  .login-container {
    /* 保持使用背景图片，但添加暗色滤镜 */
    background-image: url('/bookshelf-bg.jpg');
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    position: relative;
  }

  /* 添加暗色滤镜覆盖层 */
  .login-container::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    z-index: 1;
  }

  /* 确保内容在滤镜上方 */
  .login-content {
    z-index: 2;
  }

  .login-card {
    background-color: rgba(30, 30, 40, 0.95);
    border: 1px solid rgba(0, 0, 0, 0.3);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.4);
  }

  .form-footer a {
    color: #409eff;
  }

  .form-footer a:hover {
    color: #66b1ff;
  }
}
</style>