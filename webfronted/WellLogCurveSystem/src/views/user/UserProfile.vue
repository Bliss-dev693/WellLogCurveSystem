<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores'
import { userGetInfoService, userUpdateInfoService } from '@/api/user'
import { ElMessage } from 'element-plus'
// 引入Element Plus图标
import { Edit, User, Message } from '@element-plus/icons-vue'

const userStore = useUserStore()

// 表单数据
const userInfo = ref({
  id: null,
  nickname: '',
  email: '',
  gender: 1,
  birthday: '',
  user_id: '',
  username: ''
})

// 编辑状态
const isEditing = ref(false)
const formRef = ref()

// 获取用户信息
const getUserInfo = async () => {
  try {
    const res = await userGetInfoService()
    // 更新用户信息
    userInfo.value = { ...res.data.data }
    // 同时更新store中的用户信息
    userStore.setUser(res.data.data)
  } catch (error) {
    console.error('获取用户信息失败', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 表单验证规则
const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 10, message: '昵称长度应在2-10个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 编辑用户信息
const handleEdit = () => {
  isEditing.value = true
}

// 保存用户信息
const handleSave = async () => {
  try {
    // 调用更新用户信息的API，确保包含id字段
    await userUpdateInfoService({
      id: userInfo.value.id,
      username: userInfo.value.username,
      nickname: userInfo.value.nickname,
      email: userInfo.value.email
    })
    ElMessage.success('更新成功')
    isEditing.value = false
    // 更新store中的用户信息
    userStore.setUser({...userInfo.value})
  } catch (error) {
    console.error('更新用户信息失败', error)
    ElMessage.error('更新失败')
  }
}

// 取消编辑
const handleCancel = () => {
  isEditing.value = false
  // 重新获取原始数据
  getUserInfo()
}

// 日期格式化
const formatDate = (dateString) => {
  if (!dateString) return '未设置'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

onMounted(() => {
  getUserInfo()
})
</script>

<template>
  <page-container title="基本资料">
    <el-card class="user-profile-container">
      <div class="user-info-header">
        <div class="avatar-container">
          <el-avatar :size="100" :src="userStore.user.userPic || '/src/assets/default.png'" />
        </div>
        <div class="info-section">
          <div class="info-item">
            <el-icon :size="18"><User /></el-icon>
            <span class="label">用户名：</span>
            <span class="value">{{ userStore.user.username || userInfo.username }}</span>
          </div>
          <div class="info-item">
            <el-icon :size="18"><Message /></el-icon>
            <span class="label">用户ID：</span>
            <span class="value">{{ userInfo.user_id || userInfo.id || '暂无数据' }}</span>
          </div>
        </div>
      </div>

      <el-divider />

      <el-form 
        :model="userInfo" 
        :rules="rules" 
        ref="formRef" 
        label-width="100px"
        :disabled="!isEditing"
        class="user-form"
      >
        <el-form-item label="用户昵称" prop="nickname">
          <el-input 
            v-model="userInfo.nickname" 
            placeholder="请输入昵称"
            :disabled="!isEditing"
          />
        </el-form-item>
        
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="userInfo.gender" :disabled="!isEditing">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="0">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="userInfo.birthday"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled="!isEditing"
          />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="userInfo.email" 
            placeholder="请输入邮箱"
            :disabled="!isEditing"
          />
        </el-form-item>
      </el-form>
      
      <div class="button-group">
        <el-button 
          v-if="!isEditing" 
          type="primary" 
          @click="handleEdit"
          :icon="Edit"
        >
          编辑资料
        </el-button>
        
        <template v-else>
          <el-button @click="handleCancel">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleSave"
          >
            保存
          </el-button>
        </template>
      </div>
    </el-card>
  </page-container>
</template>

<style lang="scss" scoped>
.user-profile-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;

  .user-info-header {
    display: flex;
    align-items: center;
    gap: 20px;

    .avatar-container {
      margin-bottom: 15px;
    }

    .info-section {
      flex: 1;

      .info-item {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        font-size: 16px;

        .el-icon {
          margin-right: 8px;
          color: var(--primary-color);
        }

        .label {
          font-weight: bold;
          min-width: 70px;
        }

        .value {
          color: var(--text-color);
        }
      }
    }
  }

  .user-form {
    margin-top: 20px;
  }

  .button-group {
    display: flex;
    justify-content: center;
    margin-top: 30px;
    gap: 15px;
  }
}
</style>