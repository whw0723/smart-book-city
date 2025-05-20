<template>
  <el-header class="navbar">
    <div class="nav-container">
      <div class="logo">
        <a @click.prevent="goToDashboard" href="/admin" class="home-link">智慧书城管理系统</a>
      </div>

      <div class="nav-right">
        <template v-if="userStore.isLoggedIn">
          <button @click="showChangePasswordDialog" class="nav-button">修改密码</button>
          <button @click="logout" class="nav-button logout-button">退出登录</button>
        </template>
      </div>
    </div>
  </el-header>

  <!-- 修改密码对话框 -->
  <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
    <el-form
      :model="passwordForm"
      :rules="passwordRules"
      ref="passwordFormRef"
      label-width="100px"
    >
      <el-form-item label="当前密码" prop="currentPassword">
        <el-input v-model="passwordForm.currentPassword" type="password" show-password></el-input>
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
      </el-form-item>

      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="changePassword" :loading="passwordLoading">确认修改</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'
import type { FormInstance, FormRules } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref<FormInstance>()

// 修改密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码表单验证规则
const passwordRules = reactive<FormRules>({
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

onMounted(() => {
  userStore.initializeFromLocalStorage()
})

const goToDashboard = () => {
  router.push('/admin')
}

// 显示修改密码对话框
const showChangePasswordDialog = () => {
  passwordDialogVisible.value = true
  // 重置表单
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true

      try {
        // 调用后端API修改管理员密码
        const response = await axios.post(
          `http://localhost:8080/api/admin/${userStore.user?.id}/change-password`,
          {
            currentPassword: passwordForm.currentPassword,
            newPassword: passwordForm.newPassword
          }
        )

        ElMessage.success('密码已修改，请重新登录')
        passwordDialogVisible.value = false

        // 清空表单
        passwordForm.currentPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''

        // 登出用户
        setTimeout(() => {
          userStore.logout()
          router.push('/login')
        }, 1500)
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '密码修改失败，请确认当前密码是否正确')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

const logout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    ElMessage.success({
      message: '已退出登录',
      duration: 1000 // 设置为1秒
    })
    router.push('/login')
  }).catch(() => {
    // 用户取消退出登录，不做任何操作
  })
}
</script>

<style scoped>
.navbar {
  background-color: #545c64;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 999;
}

.nav-container {
  max-width: 1800px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 10px;
}

.logo a {
  font-size: 24px;
  font-weight: bold;
  color: #ffffff;
  text-decoration: none;
}

.nav-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  cursor: pointer;
  margin-left: 10px;
  display: flex;
  align-items: center;
  color: #ffffff;
}

.nav-button {
  background-color: transparent;
  color: #ffffff;
  border: none;
  padding: 8px 15px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 4px;
  margin-left: 10px;
}

.nav-button:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.logout-button {
  color: #ffffff;
}
</style>
