import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 1. 登录页路由（保持不变）
    { path: '/login', component: () => import('@/views/login/LoginPage.vue') },

    // 2. 主布局路由（核心业务：所有路由均仿照个人中心写法，直接写完整路径）
    {
      path: '/',
      component: () => import('@/views/layout/LayoutContainer.vue'),
      redirect: '/dashboard', // 默认重定向到工作台
      children: [
        // 2.1 工作台（完整路径写法）
        { path: '/dashboard', component: () => import('@/views/dashboard/DashboardPage.vue') },

        // 2.2 曲线重构（仿照个人中心，每个子菜单单独写完整路径）
        { path: '/curve-reconstruction/manual-input', component: () => import('@/views/curve-reconstruction/manual-input.vue') },
        { path: '/curve-reconstruction/batch-upload', component: () => import('@/views/curve-reconstruction/batch-upload.vue') },
        { path: '/curve-reconstruction/data-view', component: () => import('@/views/curve-reconstruction/data-view.vue') },

        // 2.3 报告模块（匹配侧边栏index路径，去掉多余的report-generation层级）
        { path: '/report/visual-analysis', component: () => import('@/views/report/visual-analysis.vue') },
        { path: '/report/templates', component: () => import('@/views/report/templates.vue') },
        { path: '/report/generation', component: () => import('@/views/report/generation.vue') },

        // 2.4 历史数据模块（仿照个人中心写法，单独写完整路径）
        { path: '/historical-data/history-list', component: () => import('@/views/historical-data/history-list.vue') },
        { path: '/historical-data/import-export', component: () => import('@/views/report-generation/ReportGeneration1.vue') },
        
        // 2.5 数据上传（完整路径写法）
        { path: '/data-upload', component: () => import('@/views/data-upload/DataUpload.vue') },

        // 2.6 个人中心相关路由（完全保留，不修改）
        { path: '/user/profile', component: () => import('@/views/user/UserProfile.vue') },
        { path: '/user/password', component: () => import('@/views/user/UserPassword.vue') },
        { path: '/user/avatar', component: () => import('@/views/user/UserAvatar.vue') },
        
        // 2.7 通知页面路由
        { path: '/notifications', component: () => import('@/components/NotificationPage.vue') },
      ]
    },

    // 3. 404兜底路由（补充：匹配所有未定义路径，跳转到工作台）
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
  ],
})

// 路由守卫：登录验证（完全保留，不修改）
router.beforeEach((to) => {

  const userStore = useUserStore()
  // 关键：刷新后从 localStorage 恢复 token 到 store
  if (!userStore.token) {
    const storedToken = localStorage.getItem('token')
    if (storedToken) {
      userStore.setToken(storedToken)
    }
  }
  // 未登录且访问非登录页，重定向到登录页
  if (!userStore.token && to.path !== '/login') {
    return '/login'
  }
  // 已登录访问登录页，重定向到工作台
  if (userStore.token && to.path === '/login') {
    return '/dashboard'
  }
})

export default router