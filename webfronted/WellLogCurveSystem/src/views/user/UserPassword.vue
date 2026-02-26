<script setup>
import { ref } from 'vue'
import { Lock, Key } from '@element-plus/icons-vue'
import { userUpdatePasswordService } from '@/api/user'

import { ElMessage } from 'element-plus'
// 表单数据
const form = ref({
  old_password: '',
  new_password: '',
  confirm_new_password: ''
})

// 表单引用
const formRef = ref()

// 表单验证规则
const rules = {
  old_password: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    {
      pattern: /^\S{6,15}$/,
      message: '密码必须是6-15的非空字符',
      trigger: 'blur'
    }
  ],
  new_password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      pattern: /^\S{6,15}$/,
      message: '密码必须是6-15的非空字符',
      trigger: 'blur'
    },
    {
      validator: (rule, value, callback) => {
        if (value === form.value.old_password) {
          callback(new Error('新密码不能与原密码相同!'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirm_new_password: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.value.new_password) {
          callback(new Error('两次输入的新密码不一致!'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 重置密码
const submitForm = async () => {
  try {
    // 验证表单
    await formRef.value.validate()
    
    // 调用更新密码的API（参数格式已匹配后端要求）
    await userUpdatePasswordService({
      old_pwd: form.value.old_password,
      new_pwd: form.value.new_password,
      re_pwd: form.value.confirm_new_password
    })
    
    ElMessage.success('密码修改成功')
    
    // 重置表单
    form.value = {
      old_password: '',
      new_password: '',
      confirm_new_password: ''
    }
  } catch (error) {
    console.error('密码修改失败', error)
    // 优化错误提示：优先显示后端返回的错误信息
    ElMessage.error(error.message || '密码修改失败')
  }
}
</script>

<template>
  <page-container title="重置密码">
    <el-card class="password-container">
      <div class="password-form-wrapper">
        <el-alert
          title="安全提醒"
          type="warning"
          description="为了账户安全，请定期更换密码，并确保使用高强度密码。"
          :closable="false"
          show-icon
          style="margin-bottom: 24px;"
        />
        
        <el-form 
          :model="form" 
          :rules="rules" 
          ref="formRef" 
          label-width="120px"
          class="password-form"
        >
          <el-form-item label="原密码" prop="old_password">
            <el-input 
              v-model="form.old_password" 
              type="password" 
              placeholder="请输入原密码"
              :prefix-icon="Key"
              show-password
            />
          </el-form-item>
          
          <el-form-item label="新密码" prop="new_password">
            <el-input 
              v-model="form.new_password" 
              type="password" 
              placeholder="请输入新密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item label="确认新密码" prop="confirm_new_password">
            <el-input 
              v-model="form.confirm_new_password" 
              type="password" 
              placeholder="请再次输入新密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <el-button 
              type="primary" 
              @click="submitForm" 
              style="width: 200px; margin-left: 120px;"
            >
              提交
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </page-container>
</template>

<style lang="scss" scoped>
.password-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;

  .password-form-wrapper {
    .password-form {
      margin-top: 20px;
    }
  }
}
</style>