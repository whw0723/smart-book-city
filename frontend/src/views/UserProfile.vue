<template>
  <div class="profile-container">
    <h1 class="page-title">个人中心</h1>

    <!-- 功能选项卡 -->
    <el-tabs v-model="activeTab" class="profile-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="基本信息" name="basic">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>账户余额</span>
            </div>
          </template>

          <div class="wallet-balance" v-loading="walletStore.loading">
            <div class="balance-label">当前余额</div>
            <div class="balance-amount">¥{{ walletStore.balance.toFixed(2) }}</div>

            <div class="wallet-actions">
              <el-button type="primary" @click="showDepositDialog">充值</el-button>
              <el-button type="warning" @click="showWithdrawDialog">提现</el-button>
            </div>
          </div>
        </el-card>

        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button type="primary" size="small" @click="editMode = !editMode">
                {{ editMode ? '取消' : '编辑' }}
              </el-button>
            </div>
          </template>

          <el-form
            :model="profileForm"
            :rules="rules"
            ref="profileFormRef"
            label-width="100px"
            :disabled="!editMode"
          >
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled style="max-width: 300px;"></el-input>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" style="max-width: 300px;"></el-input>
            </el-form-item>

            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="profileForm.phone" style="max-width: 300px;"></el-input>
            </el-form-item>

            <el-form-item label="收货地址" prop="address">
              <el-input v-model="profileForm.address" type="textarea" rows="3" style="max-width: 400px;"></el-input>
            </el-form-item>

            <el-form-item v-if="editMode">
              <el-button type="primary" @click="saveProfile" :loading="loading">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>

          <el-form
            :model="passwordForm"
            :rules="passwordRules"
            ref="passwordFormRef"
            label-width="100px"
          >
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input v-model="passwordForm.currentPassword" type="password" show-password style="max-width: 300px;"></el-input>
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password style="max-width: 300px;"></el-input>
            </el-form-item>

            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password style="max-width: 300px;"></el-input>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="changePassword" :loading="passwordLoading">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>



      <!-- 阅读统计选项卡 -->
      <el-tab-pane label="阅读统计" name="stats">
        <el-card class="profile-card">
          <template #header>
            <div class="card-header">
              <span>阅读统计</span>
            </div>
          </template>
          <ReadingStats />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 充值对话框 -->
    <el-dialog v-model="depositDialogVisible" title="钱包充值" width="400px">
      <el-form :model="depositForm" label-width="80px">
        <el-form-item label="充值金额">
          <el-input-number
            v-model="depositForm.amount"
            :min="1"
            :precision="2"
            :step="10"
            style="width: 200px"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="depositDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleDeposit" :loading="depositLoading">
            确认充值
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 提现对话框 -->
    <el-dialog v-model="withdrawDialogVisible" title="钱包提现" width="400px">
      <el-form :model="withdrawForm" label-width="80px">
        <el-form-item label="提现金额">
          <el-input-number
            v-model="withdrawForm.amount"
            :min="1"
            :max="walletStore.balance"
            :precision="2"
            :step="10"
            style="width: 200px"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="withdrawDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="handleWithdraw" :loading="withdrawLoading">
            确认提现
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../store/user'
import { useWalletStore } from '../store/wallet'
import { useRouter, useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import axios from 'axios'
import ReadingStats from '../components/ReadingStats.vue'

const userStore = useUserStore()
const walletStore = useWalletStore()
const router = useRouter()
const route = useRoute()
const editMode = ref(false)
const loading = ref(false)
const passwordLoading = ref(false)
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()
const activeTab = ref('basic')

// 钱包相关
const depositDialogVisible = ref(false)
const depositLoading = ref(false)
const depositForm = ref({
  amount: 100,
  description: '钱包充值'
})

const withdrawDialogVisible = ref(false)
const withdrawLoading = ref(false)
const withdrawForm = ref({
  amount: 100,
  description: '钱包提现'
})

// 个人信息表单
const profileForm = reactive({
  username: '',
  email: '',
  phone: '',
  address: ''
})

// 修改密码表单
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = reactive<FormRules>({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ]
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
      validator: (_rule, value, callback) => {
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

// 处理标签切换
const handleTabChange = (tabName: string) => {
  // 更新URL查询参数，但不触发页面刷新
  router.replace({
    query: {
      ...route.query,
      tab: tabName
    }
  })
}

// 监听URL查询参数变化
watch(() => route.query.tab, (newTab) => {
  if (newTab && (newTab === 'basic' || newTab === 'stats')) {
    activeTab.value = newTab as string
  }
}, { immediate: true })

// 初始化用户信息
onMounted(async () => {
  // 从URL查询参数中获取当前标签
  const tabParam = route.query.tab
  if (tabParam && (tabParam === 'basic' || tabParam === 'stats')) {
    activeTab.value = tabParam as string
  }

  if (userStore.user) {
    profileForm.username = userStore.user.username
    profileForm.email = userStore.user.email || ''
    // 这里可以从用户存储中获取更多信息
    // 如果后端API支持，也可以在这里请求完整的用户信息
  }

  // 获取钱包余额
  await walletStore.fetchWallet()
})

// 保存个人信息
const saveProfile = async () => {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true

      try {
        // 这里应该调用API保存用户信息
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1000))

        // 更新本地用户信息
        if (userStore.user) {
          userStore.user.email = profileForm.email
          // 更新其他信息...

          // 保存到localStorage
          localStorage.setItem('user', JSON.stringify(userStore.user))
        }

        ElMessage.success('个人信息已更新')
        editMode.value = false
      } catch (error) {
        ElMessage.error('保存失败，请稍后再试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true

      try {
        // 调用后端API修改密码
        await axios.post(
          `http://localhost:8080/api/users/${userStore.user?.id}/change-password`,
          {
            currentPassword: passwordForm.currentPassword,
            newPassword: passwordForm.newPassword
          }
        )

        ElMessage.success('密码已修改，请重新登录')

        // 清空表单
        passwordForm.currentPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''

        // 登出用户
        setTimeout(() => {
          userStore.logout()
          window.location.href = '/login'
        }, 1500)
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '密码修改失败，请确认当前密码是否正确')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

// 显示充值对话框
const showDepositDialog = () => {
  depositForm.value.amount = 100
  depositForm.value.description = '钱包充值'
  depositDialogVisible.value = true
}

// 处理充值
const handleDeposit = async () => {
  if (depositForm.value.amount <= 0) {
    ElMessage.warning('充值金额必须大于0')
    return
  }

  depositLoading.value = true
  try {
    await walletStore.deposit(depositForm.value.amount)
    // 充值成功后，立即刷新钱包余额
    await walletStore.fetchWallet()
    ElMessage.success('充值成功')
    depositDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.response?.data || '充值失败')
  } finally {
    depositLoading.value = false
  }
}

// 显示提现对话框
const showWithdrawDialog = () => {
  withdrawForm.value.amount = Math.min(100, walletStore.balance)
  withdrawForm.value.description = '钱包提现'
  withdrawDialogVisible.value = true
}

// 处理提现
const handleWithdraw = async () => {
  if (withdrawForm.value.amount <= 0) {
    ElMessage.warning('提现金额必须大于0')
    return
  }

  if (withdrawForm.value.amount > walletStore.balance) {
    ElMessage.warning('提现金额不能超过余额')
    return
  }

  withdrawLoading.value = true
  try {
    await walletStore.withdraw(withdrawForm.value.amount)
    // 提现成功后，立即刷新钱包余额
    await walletStore.fetchWallet()
    ElMessage.success('提现成功')
    withdrawDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.response?.data || '提现失败')
  } finally {
    withdrawLoading.value = false
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  margin-bottom: 30px;
  font-size: 24px;
  color: #303133;
}

.profile-tabs {
  margin-bottom: 20px;
}

.profile-card {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.wallet-balance {
  text-align: center;
  padding: 20px 0;
}

.balance-label {
  font-size: 16px;
  color: #666;
  margin-bottom: 10px;
}

.balance-amount {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 20px;
}

.wallet-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>
