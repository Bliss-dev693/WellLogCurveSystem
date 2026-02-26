<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElDialog } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { userRegisterService, userLoginService } from '@/api/user'
import { useUserStore } from '@/stores'

const router = useRouter()
const UserStore = useUserStore()

// 弹窗控制
const loginDialogVisible = ref(false) // 登录注册弹窗是否显示
const isRegister = ref(false) // 是否切换到注册表单

// 表单数据
const formModel = ref({
  username: '',
  password: '',
  repassword: ''
})

// 表单验证规则（保留原有逻辑）
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 5, max: 10, message: '用户名必须是5-10位的字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    {
      pattern: /^\S{6,15}$/,
      message: '密码必须是6-15位的非空字符',
      trigger: 'blur'
    }
  ],
  repassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      pattern: /^\S{6,15}$/,
      message: '密码必须是6-15的非空字符',
      trigger: 'blur'
    },
    {
      validator: (rule, value, callback) => {
        if (value !== formModel.value.password) {
          callback(new Error('两次输入密码不一致!'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const form = ref(null)

// 注册逻辑（保留原有逻辑）
const register = async () => {
  await form.value.validate()
  try {
    await userRegisterService({
      username: formModel.value.username,
      password: formModel.value.password
    })
    ElMessage.success('注册成功')
    isRegister.value = false // 注册成功后切回登录表单
  } catch (error) {
    ElMessage.error('注册失败：' + (error.message || '服务器错误'))
  }
}

// 登录逻辑（保留原有逻辑）
const login = async () => {
  await form.value.validate()
  try {
    const res = await userLoginService({
      username: formModel.value.username,
      password: formModel.value.password
    })
    if (res.data.data) {
      UserStore.setToken(res.data.data)
      localStorage.setItem('token', res.data.data)
      ElMessage.success('登录成功')
      loginDialogVisible.value = false // 关闭弹窗
      router.push('/dashboard') // 登录成功跳转到系统首页
    } else {
      ElMessage.error('登录失败，无法获取 token')
    }
  } catch (error) {
    ElMessage.error('登录失败：' + (error.message || '用户名或密码错误'))
  }
}

// 切换登录/注册时重置表单
watch(isRegister, () => {
  formModel.value = {
    username: '',
    password: '',
    repassword: ''
  }
  if (form.value) {
    form.value.clearValidate() // 清除表单验证提示
  }
})

// 进入系统按钮点击事件
const entrance = () => {
  // 检查是否已登录（从store或localStorage判断）
  const token = localStorage.getItem('token') || UserStore.token
  if (token) {
    router.push('/dashboard')
  } else {
    ElMessage.info('请先登录后再进入系统！')
    loginDialogVisible.value = true // 弹出登录弹窗
  }
}

// 页面挂载时检查登录状态（可选）
onMounted(() => {
  const token = localStorage.getItem('token') || UserStore.token
  if (token) {
    // 已登录状态可直接跳转（可选）
    // router.push('/dashboard')
  }
})
</script>

<template>
  <div class="home-page">

    <!-- 主内容区 -->
    <div class="main">
      <div class="header">
        <h1 id="home">测井曲线智能重构系统</h1>
        <p class="subtitle">专业、高效、精准的测井曲线重构工具</p>
      </div>

      <div class="features">
        <div class="feature">
          <span class="feature-icon">📊</span>
          <h3>数据驱动</h3>
          <p>基于实时钻井参数进行重构</p>
        </div>
        <div class="feature">
          <span class="feature-icon">🔍</span>
          <h3>精准分析</h3>
          <p>先进算法确保重构结果准确可靠</p>
        </div>
        <div class="feature">
          <span class="feature-icon">⚡</span>
          <h3>快速响应</h3>
          <p>秒级响应，提高工作效率</p>
        </div>
      </div>

      <button id="enter" @click="entrance">点击进入系统</button>
    </div>

    <!-- 登录/注册弹窗（Element Plus Dialog） -->
    <el-dialog
      v-model="loginDialogVisible"
      title=""
      width="400px"
      class="login-dialog"
      :close-on-click-modal="false"
      header-close
    >
      <!-- 注册表单 -->
      <el-form
        ref="form"
        size="large"
        :model="formModel"
        autocomplete="off"
        :rules="rules"
        v-if="isRegister"
      >
        <div class="modal-title">注册</div>
        <el-form-item prop="username">
          <el-input
            :prefix-icon="User"
            placeholder="请输入用户名"
            v-model="formModel.username"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            :prefix-icon="Lock"
            type="password"
            placeholder="请输入密码"
            v-model="formModel.password"
          ></el-input>
        </el-form-item>
        <el-form-item prop="repassword">
          <el-input
            :prefix-icon="Lock"
            type="password"
            placeholder="请再次输入密码"
            v-model="formModel.repassword"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="register" class="button" type="primary" auto-insert-space>
            注册
          </el-button>
        </el-form-item>
        <el-form-item class="flex">
          <el-link type="info" :underline="false" @click="isRegister = false">
            ← 返回登录
          </el-link>
        </el-form-item>
      </el-form>

      <!-- 登录表单 -->
      <el-form
        ref="form"
        size="large"
        :model="formModel"
        autocomplete="off"
        :rules="rules"
        v-else
      >
        <div class="modal-title">登录</div>
        <el-form-item prop="username">
          <el-input
            :prefix-icon="User"
            placeholder="请输入用户名"
            v-model="formModel.username"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            :prefix-icon="Lock"
            type="password"
            placeholder="请输入密码"
            v-model="formModel.password"
          ></el-input>
        </el-form-item>
        <el-form-item class="flex">
          <div class="flex">
            <el-checkbox>记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button @click="login" class="button" type="primary" auto-insert-space>
            登录
          </el-button>
        </el-form-item>
        <el-form-item class="flex">
          <el-link type="info" :underline="false" @click="isRegister = true">
            注册 →
          </el-link>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.home-page {
  font-family: 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  margin: 0;
  min-height: 100vh;
  background: url('@/assets/login_bg.png') no-repeat center center / cover; 
  position: relative;

  // 背景遮罩
  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(107, 123, 150, 0.5);
    z-index: 0;
  }



  // 主内容区
  .main {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: space-between;
    min-height: calc(100vh - 70px);
    position: relative;
    z-index: 2;
    padding: 20px 0;

    .header {
      text-align: center;
      margin-top: 50px;

      #home {
        font-size: 50px;
        color: white;
        text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
        animation: fadeIn 2s;
        margin: 0;
      }

      .subtitle {
        font-size: 30px;
        color: white;
        text-shadow: 2px 2px 4px rgba(8, 8, 8, 0.8);
        font-weight: bold;
        margin-top: 10px;
        animation: fadeIn 2s;
      }
    }

    // 功能特点
    .features {
      display: flex;
      justify-content: space-around;
      width: 90%;
      margin: 80px 0 150px 0;
      text-align: center;

      .feature {
      background-color: var(--panel-contrast);
        backdrop-filter: blur(5px);
        padding: 20px;
        border-radius: 10px;
        width: 25%;
        height: 150px;
        color: rgb(106, 105, 105);
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        transition: all 0.5s ease;

        &:nth-child(1) {
          animation: fadeLeft 2s;
        }
        &:nth-child(2) {
          animation: fadeUp 2s;
        }
        &:nth-child(3) {
          animation: fadeRight 2s;
        }

        &:hover {
          transform: translateY(-10px) scale(1.05);
        }

        .feature-icon {
          font-size: 40px;
          display: block;
          margin-bottom: 10px;
        }

        h3 {
          margin: 10px 0;
          color: #2989d8;
        }

        p {
          font-size: 14px;
          opacity: 0.8;
          margin: 0;
          color: #2989d8;
        }
      }
    }

    // 进入系统按钮
    #enter {
      width: 500px;
      height: 50px;
      background: linear-gradient(45deg, #1e5799, #2989d8);
      color: white;
      font-weight: bold;
      border: none;
      border-radius: 30px;
      cursor: pointer;
      box-shadow: 0 4px 15px rgba(220, 220, 232, 0.3);
      font-size: 14px;
      position: fixed;
      bottom: 70px;
      transition: all 0.5s ease;

      &:hover {
        font-size: 15px;
        transform: scale(1.18);
        border-radius: 10px;
        background-color: rgb(6, 6, 113);
        box-shadow: 0 10px 25px rgba(0, 0, 100, 0.5);
      }
    }
  }

  // 登录弹窗样式
  .login-dialog {
    :deep(.el-dialog) {
      border-radius: 10px;
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    }

    :deep(.el-dialog__header) {
      display: none; // 隐藏默认标题栏
    }

    :deep(.el-dialog__body) {
      padding: 40px 20px;
    }

    .modal-title {
      text-align: center;
      color: #1e3a8a;
      margin-bottom: 30px;
      font-size: 28px;
      font-weight: bold;
    }

    .button {
      width: 100%;
    }

    .flex {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }
  }

  // 动画定义
  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(-20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes fadeLeft {
    from {
      opacity: 0.5;
      transform: translateX(-50px);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }

  @keyframes fadeUp {
    from {
      opacity: 0.5;
      transform: translateY(-50px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  @keyframes fadeRight {
    from {
      opacity: 0.5;
      transform: translateX(50px);
    }
    to {
      opacity: 1;
      transform: translateX(0);
    }
  }
}
</style>