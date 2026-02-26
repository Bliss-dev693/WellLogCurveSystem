<script setup>
import {
  Management,
  Promotion,
  UserFilled,
  User,
  Crop,
  EditPen,
  SwitchButton,
  CaretBottom,
  ChromeFilled,
  Files,
  UploadFilled,
  View,
  Histogram,
  Download,
  TrendCharts,
  Notebook,
  DocumentCopy,
  Moon,
  Sunny,
  Bell // 添加通知图标
} from '@element-plus/icons-vue' // 添加日间/夜间模式图标
import avatar from '@/assets/default.png'
import { useUserStore } from '@/stores';
import { onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router'
import AiAssistant from '@/components/AiAssistant.vue'
import { ElMessageBox } from 'element-plus';
// 导入通知API服务
import { getUnreadNotificationCountService } from '@/api/notification.js';

const router = useRouter()
const userStore = useUserStore();
const isDarkMode = ref(false); // 添加夜间模式状态
const notificationCount = ref(0); // 初始化为0，动态获取
// 系统信息

const systemVersion = ref('2.1.3')

// 获取未读通知数量
const fetchUnreadCount = async () => {
  try {
    if (userStore.user?.id) {
      const response = await getUnreadNotificationCountService(userStore.user.id);
      // 从response.data中提取未读数量
      notificationCount.value = response.data?.unreadCount || 0;
     
    }
  } catch (error) {
    console.error('获取未读通知数量失败：', error);
    notificationCount.value = 0; // 出错时设为0
  }
};

onMounted(() => {
  userStore.getUser()
  
  // 检查本地存储中的主题设置
  const savedTheme = localStorage.getItem('theme')
  if (savedTheme === 'dark') {
    setDarkMode(true)
  }
  
  // 页面加载后获取未读通知数量
  fetchUnreadCount();
})

// 监听用户信息变化，当用户信息加载完成后获取通知数量
watch(
  () => userStore.user,
  (newUser) => {
    if (newUser?.id) {
      fetchUnreadCount();
    }
  },
  { deep: true }
);

// 切换夜间模式
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
  setDarkMode(isDarkMode.value)
}

// 设置主题模式
const setDarkMode = (dark) => {
  if (dark) {
    document.documentElement.classList.add('dark')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.classList.remove('dark')
    localStorage.setItem('theme', 'light')
  }
  isDarkMode.value = dark
}

// 处理通知点击
const handleNotificationClick = () => {
  // 跳转到通知页面
  router.push('/notifications');
  // 跳转后可以考虑重置未读数量或刷新数据
  setTimeout(() => {
    fetchUnreadCount();
  }, 1000);
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    await ElMessageBox.confirm('你确认退出系统吗？', '温馨提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    userStore.removeToken()
    userStore.setUser({})
    router.push(`/login`)
  } else {
    // 适配新的个人中心路由命名
    router.push(`/user/${command}`)
  }
}
</script>

<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="el-aside__logo"></div>
      <el-menu
        active-text-color="#ffd04b"
        background-color="#232323"
        :default-active="$route.path"
        text-color="#fff"
        router
      >
        <!-- 你的菜单项保持不变 -->
        <el-menu-item index="/dashboard">
          <el-icon><Management /></el-icon>
          <span>工作台</span>
        </el-menu-item>

        <!-- 曲线重构子菜单 -->
        <el-sub-menu index="/curve-reconstruction">
          <template #title>
            <el-icon><ChromeFilled /></el-icon>
            <span>曲线重构</span>
          </template>
          <el-menu-item index="/curve-reconstruction/manual-input">
            <el-icon><Files /></el-icon>
            <span>手动录入</span>
          </el-menu-item>

          <!-- <el-menu-item index="/curve-reconstruction/batch-upload">
            <el-icon><UploadFilled /></el-icon>
            <span>批量上传</span>
          </el-menu-item> -->
          <el-menu-item index="/curve-reconstruction/data-view">
            <el-icon><View /></el-icon>
            <span>数据查看</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 报告子菜单 -->
        <el-sub-menu index="/report">
          <template #title>
            <el-icon><Promotion /></el-icon>
            <span>报告</span>
          </template>
          
          <el-menu-item index="/report/templates">
            <el-icon><Notebook /></el-icon>
            <span>报告模板</span>
          </el-menu-item>
          <el-menu-item index="/report/generation">
            <el-icon><DocumentCopy /></el-icon>
            <span>报告生成</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 历史数据子菜单 -->
        <el-sub-menu index="/historical-data">
          <template #title>
            <el-icon><Histogram /></el-icon>
            <span>历史数据</span>
          </template>
          <el-menu-item index="/historical-data/history-list">
            <el-icon><Files /></el-icon>
            <span>历史记录</span>
          </el-menu-item>
          <el-menu-item index="/report/visual-analysis">
            <el-icon><TrendCharts /></el-icon>
            <span>可视化分析</span>
          </el-menu-item>
          <!-- <el-menu-item index="/historical-data/import-export">
            <el-icon><Download /></el-icon>
            <span>报告导出</span>
          </el-menu-item> -->
        </el-sub-menu>

        <!-- 个人中心子菜单 -->
        <el-sub-menu index="/user">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>个人中心</span>
          </template>
          <el-menu-item index="/user/profile">
            <el-icon><User /></el-icon>
            <span>基本资料</span>
          </el-menu-item>
          <el-menu-item index="/user/avatar">
            <el-icon><Crop /></el-icon>
            <span>更换头像</span>
          </el-menu-item>
          <el-menu-item index="/user/password">
            <el-icon><EditPen /></el-icon>
            <span>重置密码</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div>欢迎回来<strong>{{ userStore.user.nickname || userStore.user.username }}</strong></div>
        <div class="header-controls">
          <!-- 通知按钮 -->
          <el-badge :value="notificationCount" :max="99" class="notification-badge">
            <el-button 
              :icon="Bell" 
              circle 
              @click="handleNotificationClick"
              class="notification-btn"
              :title="'您有' + notificationCount + '条未读通知'"
            />
          </el-badge>
          
          <!-- 夜间模式切换按钮 -->
          <el-button 
            :icon="isDarkMode ? Sunny : Moon" 
            circle 
            @click="toggleDarkMode"
            class="theme-toggle-btn"
            :title="isDarkMode ? '切换到日间模式' : '切换到夜间模式'"
          />
          
          <el-dropdown placement="bottom-end" @command="handleCommand">
            <span class="el-dropdown__box">
              <el-avatar :src="userStore.user.userPic || avatar" />
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" :icon="User">基本资料</el-dropdown-item>
                <el-dropdown-item command="avatar" :icon="Crop">更换头像</el-dropdown-item>
                <el-dropdown-item command="password" :icon="EditPen">重置密码</el-dropdown-item>
                <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view></router-view>
      </el-main>
      <el-footer>© 2026 测井曲线重构系统|系统版本: v{{ systemVersion }} | Created by 西南石油大学</el-footer>
    </el-container>
    <AiAssistant />
  </el-container>
</template>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  .el-aside {
    background-color: var(--aside-bg);
    &__logo {
      height: 120px;
      background: url('@/assets/image5.png') no-repeat center / 320px auto;
    }
    .el-menu {
      border-right: none;
    }
  }
  .el-header {
    background-color: var(--header-bg);
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .header-controls {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .theme-toggle-btn {
        margin-right: 10px;
      }
      
      .notification-badge {
        :deep(.el-badge__content) {
          background-color: var(--danger-color);
          border: none;
        }
      }
      
      .notification-btn {
        position: relative;
        
        &:hover {
          background-color: var(--hover-bg);
          color: var(--primary-color);
          transform: translateY(-1px);
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.12);
        }
      }
    }
    
    .el-dropdown__box {
      display: flex;
      align-items: center;
      .el-icon {
        color: var(--muted-color);
        margin-left: 10px;
      }

      &:active,
      &:focus {
        outline: none;
      }
    }
  }
  .el-footer {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    color: var(--muted-color);
  }
}

// 夜间模式样式
.dark {
  --bg-color: #1a1a1a;
  --text-color: #ffffff;
  --header-bg: #2d2d2d;
  --main-bg: #1e1e1e;
  --border-color: #444;
  --aside-bg: #1e1e1e;
  
  body {
    background-color: var(--bg-color);
    color: var(--text-color);
  }
  
  .el-header {
    background-color: var(--header-bg) !important;
  }
  
  .el-main {
    background-color: var(--main-bg) !important;
  }
  
  .el-aside {
    background-color: var(--aside-bg) !important;
  }
  
  // Element Plus 组件的夜间样式
  :deep(.el-menu) {
    background-color: var(--aside-bg) !important;
  }
  
  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    color: var(--muted-color) !important;
    background-color: var(--aside-bg) !important;
    
    &.is-active {
      background-color: var(--menu-active-bg) !important;
    }
  }
  
  :deep(.el-footer) {
    background-color: var(--header-bg) !important;
    color: #ccc !important;
  }
  
  .notification-btn {
    &:hover {
      background-color: var(--hover-bg);
      color: var(--primary-color);
      box-shadow: 0 2px 8px rgba(99, 179, 237, 0.12);
    }
  }
  
  :deep(.el-badge__content) {
    background-color: var(--danger-color) !important;
  }
}
</style>
