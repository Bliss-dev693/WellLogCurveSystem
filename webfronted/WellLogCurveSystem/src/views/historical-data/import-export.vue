<script setup>
import PageContainer from '@/components/PageContainer.vue'
import { onMounted } from 'vue'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores' // 引入用户仓库（获取当前登录用户ID）
// 引入封装的导出接口和下载工具
import { exportHistoryToCsv } from '@/api/drilling'
import { downloadBlobFile } from '@/utils/download'

// 响应式变量：导出加载状态
const exportLoading = ref(false)
// 获取当前登录用户ID（从pinia仓库中获取，适配你的项目权限逻辑）
const userStore = useUserStore()
const currentUserId = userStore.userInfo?.id // 按实际仓库字段调整（如userStore.id）

onMounted(() => {
  if (!userStore.user?.id) {
    userStore.getUser().catch(err => {
      
      ElMessage.error(`用户信息加载失败：${err.message}，无法查询历史记录`);
    });
  }
});
/**
 * 处理CSV导出点击事件
 */
const handleExportCsv = async () => {
  // 1. 前置校验：用户ID是否存在（避免无效请求）
  if (!currentUserId) {
    ElMessage.warning('请先完成登录，再进行导出操作')
    return
  }

  exportLoading.value = true
  try {
    // 2. 调用封装的导出接口，获取blob二进制流
    const response = await exportHistoryToCsv(currentUserId)
    // 3. 调用下载工具，触发浏览器下载（指定自定义文件名）
    const isSuccess = downloadBlobFile(response.data, '钻井预测历史记录.csv')
    if (isSuccess) {
      ElMessage.success('CSV文件导出成功，已自动下载')
    }
  } catch (error) {
    // 4. 异常处理：接口失败/下载失败的友好提示
    ElMessage.error('CSV文件导出失败：' + (error.message || '服务端异常，请稍后重试'))
    console.error('导出失败详情：', error)
  } finally {
    // 5. 最终：关闭加载状态（无论成功/失败）
    exportLoading.value = false
  }
}
</script>

<template>
  <page-container title="导入导出">
    <el-card>
     <div class="history-export-page">
    <!-- 导出按钮：绑定点击事件，添加加载状态 -->
    <el-button
      type="primary"
      @click="handleExportCsv"
      :loading="exportLoading"
      icon="el-icon-download"
    >
      导出历史预测记录为CSV
    </el-button>
  </div>
    </el-card>
  </page-container>
</template>

<style scoped>
.history-export-page {
  padding: 20px;
}
</style>