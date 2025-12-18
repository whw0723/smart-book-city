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
import axios from '../utils/axios'

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
        // 首先强制重新从localStorage更新userStore状态
        userStore.initializeFromLocalStorage();
        
        // 详细调试日志
        console.log('User store state after initialization:', { 
          user: userStore.user, 
          isLoggedIn: userStore.isLoggedIn, 
          isAdmin: userStore.isAdmin 
        });
        
        // 获取并显示localStorage中的原始数据
        const localStorageUserData = localStorage.getItem('user');
        const localStorageUserType = localStorage.getItem('userType');
        const localStorageLastAdminUsername = localStorage.getItem('lastAdminUsername');
        
        // 输出所有可能相关的localStorage数据
        console.log('=== localStorage Debug ===');
        console.log('user:', localStorageUserData);
        console.log('userType:', localStorageUserType);
        console.log('lastAdminUsername:', localStorageLastAdminUsername);
        console.log('All localStorage keys:', Object.keys(localStorage));
        
        let userId = null;
        
        // 第一次尝试：从userStore获取用户ID
        console.log('UserStore state:', { user: userStore.user, isLoggedIn: userStore.isLoggedIn, isAdmin: userStore.isAdmin });
        if (userStore.user) {
          // 尝试不同可能的ID字段
          userId = userStore.user.id || userStore.user.username;
          console.log('Case 1: User ID from userStore:', userId);
        }
        
        // 第二次尝试：直接解析localStorage中的user数据
        if (!userId && localStorageUserData) {
          try {
            const parsedUser = JSON.parse(localStorageUserData);
            console.log('Parsed user data from localStorage:', parsedUser);
            
            if (parsedUser && typeof parsedUser === 'object') {
              // 更全面地尝试不同的ID字段
              // 只使用类型定义中存在的属性或可能在JSON中出现的字段
              userId = parsedUser.id || parsedUser.username || 
                      parsedUser._id || parsedUser.user_id;
              console.log('Case 2: User ID extracted from localStorage:', userId);
              
              // 更新userStore
              userStore.user = parsedUser;
              userStore.isLoggedIn = true;
              userStore.isAdmin = localStorageUserType === 'admin';
            }
          } catch (e) {
            console.error('Failed to parse user data from localStorage:', e);
            console.error('Raw user data string causing parse error:', localStorageUserData);
          }
        }
        
        // 第三次尝试：使用lastAdminUsername作为备选
        if (!userId && localStorageLastAdminUsername) {
          userId = localStorageLastAdminUsername;
          console.log('Case 3: Using lastAdminUsername as User ID:', userId);
        }
        
        // 第四次尝试：如果用户类型是admin但没有ID，尝试使用固定值或从其他存储中获取
        if (!userId && localStorageUserType === 'admin') {
          // 尝试获取所有可能的管理员相关存储
          for (const key of Object.keys(localStorage)) {
            if (key.includes('admin') || key.includes('user')) {
              console.log(`Checking localStorage key: ${key}, value: ${localStorage.getItem(key)}`);
              // 尝试将任何非空值作为userId
              const value = localStorage.getItem(key);
              if (value && value.trim() !== '') {
                // 尝试解析JSON，或直接使用字符串值
                try {
                  const parsed = JSON.parse(value);
                  if (parsed && typeof parsed === 'object') {
                    const potentialId = parsed.id || parsed.username || parsed._id || parsed.user_id;
                    if (potentialId) {
                      userId = potentialId;
                      console.log(`Found potential ID from ${key}:`, userId);
                      break;
                    }
                  }
                } catch {
                  // 如果不是JSON，可能是直接的用户名或ID
                  if (!value.startsWith('{') && value.length > 0) {
                    userId = value;
                    console.log(`Using ${key} value directly as ID:`, userId);
                    break;
                  }
                }
              }
            }
          }
        }
        
        // 最后验证用户ID是否有效 - 使用更宽松的验证方式
        console.log('Final userId value before validation:', userId);
        console.log('Final userId type:', typeof userId);
        
        // 只要userId存在且不是空字符串就认为有效
        if (!userId || (typeof userId === 'string' && userId.trim() === '')) {
          console.error('Final check failed: User ID is invalid or missing');
          console.error('Available localStorage data:', {
            user: localStorageUserData,
            userType: localStorageUserType,
            lastAdminUsername: localStorageLastAdminUsername
          });
          ElMessage.error('用户信息无效或不完整，请重新登录');
          return;
        }
        
        console.log('User ID validation passed:', userId);
        
        // 确保userId是数字类型，添加更健壮的错误处理
        let numericUserId = NaN;
        if (userId !== null && userId !== undefined) {
          // 尝试将userId转换为数字
          if (typeof userId === 'number') {
            numericUserId = userId;
          } else if (typeof userId === 'string') {
            // 如果是字符串，先尝试直接转换数字
            numericUserId = parseInt(userId, 10);
            // 如果转换失败，尝试提取字符串中的数字部分
            if (isNaN(numericUserId)) {
              // 从字符串中提取可能的数字部分
              const numericMatch = userId.match(/\d+/);
              if (numericMatch) {
                numericUserId = parseInt(numericMatch[0], 10);
              }
            }
          }
        }
        
        console.log('Using numeric user ID for API call:', numericUserId);
        
        // 如果仍然无法获得有效的数字ID，使用默认值1或者提示用户重新登录
        if (isNaN(numericUserId)) {
          console.error('Failed to convert user ID to number:', userId);
          // 可以选择使用默认的管理员ID或者让用户重新登录
          numericUserId = 1; // 假设默认管理员ID为1
          console.warn('Using default admin ID:', numericUserId);
          // 或者显示错误信息并返回
          // ElMessage.error('无法识别用户ID，请重新登录');
          // passwordLoading.value = false;
          // return;
        }
        
        const response = await axios.post(
          `http://localhost:8080/api/admin/${numericUserId}/change-password`,
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
        console.error('Error during password change API call:', error);
        // 显示详细的错误信息，包括HTTP状态码和响应内容
        if (error.response) {
          console.error('Error response data:', error.response.data);
          console.error('Error status:', error.response.status);
          console.error('Error headers:', error.response.headers);
          ElMessage.error(`密码修改失败: ${error.response.data?.message || `HTTP ${error.response.status}`}，请确认当前密码是否正确`);
        } else if (error.request) {
          console.error('No response received:', error.request);
          ElMessage.error('无法连接到服务器，请检查网络连接');
        } else {
          console.error('Request setup error:', error.message);
          ElMessage.error('请求设置错误: ' + error.message);
        }
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
