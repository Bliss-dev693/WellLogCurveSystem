<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElDialog, ElInput, ElButton, ElMessage, ElScrollbar, ElIcon } from 'element-plus'
import avatar from '@/assets/default.png'
import { useUserStore } from '@/stores';
import { Microphone, Position, CircleClose } from '@element-plus/icons-vue'
import 'element-plus/dist/index.css'
// 引入封装的流式接口
import { chatStream } from '@/api/ai'

// 控制对话框显示
const dialogVisible = ref(false)
const userStore = useUserStore();

onMounted(() => {
  userStore.getUser()
})
// 输入框内容
const inputMessage = ref('')
// 快捷操作列表
const quickActions = ref([
  { text: '学习答疑', desc: '课程资料智能问答', icon: '📚' },
  { text: '资源推荐', desc: '海量优质资源一键直达', icon: '🔍' },
  { text: '代码助手', desc: '编程问题快速解答', icon: '💻' },
  { text: '翻译助手', desc: '多语言翻译支持', icon: '🌐' }
])
// 聊天记录（扩展字段：thinking 思考链内容，loading 加载状态）
const chatList = ref([
  { type: 'init', content: 'init' } // 初始化占位
])
// 取消流连接的方法（用于组件卸载/重复请求时终止旧连接）
let cancelStream = null
// 记录当前AI回复的索引（用于逐字拼接内容）
let currentAiIndex = -1

// 填充快捷操作到输入框
const fillQuickAction = (text) => {
  inputMessage.value = `请帮我${text.toLowerCase()}`
}

// 自动滚动到底部
const scrollToBottom = () => {
  const scrollbar = document.querySelector('.ai-chat-scrollbar .el-scrollbar__wrap')
  if (scrollbar) {
    scrollbar.scrollTop = scrollbar.scrollHeight
  }
}

// 发送消息（核心修改：替换为真实流式接口调用）
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message) {
    ElMessage.warning('请输入内容后发送')
    return
  }

  // 1. 终止上一次未完成的流式连接（防止重复请求）
  if (cancelStream) {
    cancelStream()
    cancelStream = null
  }

  // 2. 添加用户消息到聊天记录
  chatList.value.push({
    type: 'user',
    content: message
  })

  // 3. 清空输入框并滚动到最新消息
  inputMessage.value = ''
  await nextTick()
  scrollToBottom()

  // 4. 添加AI消息占位（用于逐字拼接内容和展示思考链）
  currentAiIndex = chatList.value.push({
    type: 'ai',
    content: '', // 生成内容逐字拼接在此
    thinking: '', // 思考链内容
    loading: true // 加载状态
  }) - 1

  // 5. 调用流式接口
  cancelStream = chatStream(
    // 接口请求参数
    {
      messages: [
        // 可扩展：传递历史聊天记录，实现多轮对话
        { role: 'user', content: message }
      ],
      enableThinking: true, // 启用思考链
      temperature: 0.7 // 温度参数
    },
    // 接收单条消息的回调（核心：处理流式数据）
    (msg) => {
      nextTick(async () => {
        const aiItem = chatList.value[currentAiIndex]
        if (!aiItem) return

        // 处理思考链消息
        if (msg.type === 'thinking') {
          aiItem.thinking = (aiItem.thinking || '') + msg.content
        }
        // 处理生成内容消息（逐字拼接）
        else if (msg.type === 'generation') {
          aiItem.content = (aiItem.content || '') + msg.content
        }
        // 处理错误消息
        else if (msg.type === 'error') {
          aiItem.content = `❌ 服务异常：${msg.content}`
          aiItem.loading = false
        }

        // 消息结束，更新加载状态
        if (msg.isDone) {
          aiItem.loading = false
          cancelStream = null
        }

        // 滚动到最新消息
        await nextTick()
        scrollToBottom()
      })
    },
    // 错误回调
    (error) => {
      ElMessage.error(error)
      const aiItem = chatList.value[currentAiIndex]
      if (aiItem) {
        aiItem.content = `❌ 对话失败：${error}`
        aiItem.loading = false
      }
      cancelStream = null
    },
    // 连接关闭回调
    () => {
      cancelStream = null
    }
  )
}

// 组件卸载时，终止流式连接（防止内存泄漏）
onUnmounted(() => {
  if (cancelStream) {
    cancelStream()
  }
})

// 初始化欢迎消息
onMounted(() => {
  chatList.value.push({
    type: 'ai',
    content: '您好！我是您的专属AI助手，可提供学习答疑、代码编写、资源推荐、翻译等服务。',
    thinking: '',
    loading: false
  })
  nextTick(() => {
    scrollToBottom()
  })
})
</script>

<template>
  <!-- 悬浮小精灵按钮（保留原有代码） -->
  <div class="ai-assistant-float-btn" @click="dialogVisible = true">
    <div class="ai-icon-wrapper">
      <div class="ai-3d-icon">
        <img src="@/assets/小精灵1.gif" alt="AI小精灵" class="ai-3d-icon">
      </div>
    </div>
    <span class="ai-status">AI助手</span>
    <span class="ai-online">在线</span>
  </div>

  <!-- AI对话弹窗（保留原有结构，修改聊天消息展示） -->
  <ElDialog
    v-model="dialogVisible"
    title=""
    class="ai-assistant-dialog"
    :show-close="false"
    destroy-on-close
    :modal="true"
    custom-class="right-float-dialog"
  >
    <!-- 自定义标题栏（保留） -->
    <div class="custom-dialog-header">
      <h2 class="dialog-title">🤖 智能AI助手</h2>
      <div class="dialog-close-btn" @click="dialogVisible = false">
        <el-icon><CircleClose /></el-icon>
      </div>
    </div>

    <!-- 聊天区域（保留） -->
    <ElScrollbar class="ai-chat-scrollbar" height="calc(66vh - 80px)">
      <div class="chat-container">
        <!-- 欢迎卡片（保留） -->
        <div v-if="chatList.length === 1" class="chat-card">
          <div class="card-title">下午好！</div>
          <div class="card-content">我是您的专属AI助手，有什么可以帮您的吗？😊</div>
          <div class="card-actions">
            <div
              v-for="(action, idx) in quickActions"
              :key="idx"
              class="action-item"
              @click="fillQuickAction(action.text)"
            >
              <span class="action-icon">{{ action.icon }}</span>
              <div>
                <div class="action-text">{{ action.text }}</div>
                <div class="action-desc">{{ action.desc }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 聊天消息（核心修改：支持思考链、加载状态、逐字展示） -->
        <div
          v-for="(item, index) in chatList"
          :key="index"
          :class="{
            'chat-item': item.type === 'ai' || item.type === 'user',
            [item.type]: true
          }"
        >
          <template v-if="item.type === 'ai'">
            <div class="chat-avatar">
              <div class="ai-avatar-img">
                <img src="@/assets/小精灵1.gif" alt="AI小精灵" class="ai-avatar-img">
              </div>
            </div>
            <div class="chat-content-wrapper">
              <!-- 思考链展示（灰色小字，斜体） -->
              <div v-if="item.thinking" class="chat-thinking">{{ item.thinking }}...</div>
              <!-- AI生成内容（逐字拼接） -->
              <div class="chat-content">
                {{ item.content }}
                <!-- 加载中动画 -->
                <span v-if="item.loading" class="loading-dot">●●●</span>
              </div>
              <div class="ai-name">AI助手</div>
            </div>
          </template>
          <template v-else-if="item.type === 'user'">
            <div class="chat-avatar">
              <div class="user-avatar-img">
                <el-avatar :src="userStore.user.userPic || avatar" />
              </div>
            </div>
            <div class="chat-content-wrapper">
              <div class="chat-content">{{ item.content }}</div>
              <div class="user-name">您</div>
            </div>
          </template>
        </div>
      </div>
    </ElScrollbar>

    <!-- 输入区域（保留原有代码） -->
    <template #footer>
      <div class="ai-input-container">
        <ElInput
          v-model="inputMessage"
          placeholder="请输入问题..."
          @keyup.enter="sendMessage"
          clearable
          size="default"
          class="message-input"
          :disabled="!!cancelStream" 
        >
          <template #prefix>
            <el-icon style="font-size: 18px; color: #909399;"><Microphone /></el-icon>
          </template>
          <template #suffix>
            <ElButton
              type="primary"
              @click="sendMessage"
              size="small"
              circle
              :disabled="!inputMessage.trim() || !!cancelStream" 
              class="send-btn"
            >
              <el-icon><Position/></el-icon>
            </ElButton>
          </template>
        </ElInput>
      </div>
      <div class="tip-text">内容由AI生成，仅供参考</div>
    </template>
  </ElDialog>
</template>

<style scoped>
/* 原有样式全部保留，新增以下样式：思考链、加载中、禁用状态 */
/* 思考链样式 */
.chat-thinking {
  font-size: 12px;
  color: #909399;
  font-style: italic;
  margin-bottom: 4px;
  margin-left: 8px;
  line-height: 1.2;
}

/* 加载中点点动画 */
.loading-dot {
  margin-left: 6px;
  font-size: 14px;
  color: #409eff;
  animation: loading 1.5s infinite ease-in-out;
}

@keyframes loading {
  0% { opacity: 0.2; }
  50% { opacity: 1; }
  100% { opacity: 0.2; }
}

/* 输入框禁用状态（可选，优化视觉） */
:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: #f5f7fa !important;
  cursor: not-allowed;
}

/* 按钮禁用状态（可选，优化视觉） */
:deep(.el-button.is-disabled) {
  opacity: 0.6 !important;
  cursor: not-allowed;
}

/* 原有所有样式（从原代码中复制过来，此处省略，保持不变） */
.ai-assistant-float-btn {
  position: fixed;
  right: 30px;
  bottom: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  z-index: 9999;
  padding: 8px;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  animation: float 3s ease-in-out infinite;
}
.ai-icon-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #e4e6e7, #f4f6f7);
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-3d-icon {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}
.ai-status {
  font-size: 12px;
  color: #409eff;
  margin-top: 4px;
  font-weight: 500;
}
.ai-online {
  font-size: 10px;
  color: #67c23a;
}
@keyframes float {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-6px); }
  100% { transform: translateY(0px); }
}
.ai-assistant-float-btn:hover {
  transform: scale(1.05) translateY(-3px);
  box-shadow: 0 6px 16px rgba(236, 237, 239, 0.3);
}
.right-float-dialog {
  position: fixed !important;
  right: 20px !important;
  top: 50% !important;
  transform: translateY(-50%) !important;
  height: 33vh !important;
  width: 400px !important;
  margin: 0 !important;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border: 1px solid #e8f4ff;
}
.custom-dialog-header {
  width: 100%;
  text-align: center;
  padding: 14px 0;
  background: linear-gradient(135deg, #79abe8, #7caceb);
  position: relative;
}
.dialog-title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  margin: 0;
  letter-spacing: 1px;
}
.dialog-close-btn {
  position: absolute;
  top: 12px;
  right: 16px;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s;
}
.dialog-close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}
.ai-chat-scrollbar {
  margin: 0 16px 12px;
  border-radius: 8px;
  background-color: #fafcff;
}
.chat-container {
  padding: 12px;
}
.chat-card {
  background: linear-gradient(135deg, #eef5ff, #f5faff);
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}
.card-content {
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
}
.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  background: #fff;
  border-radius: 6px;
  flex: 1;
  min-width: 120px;
  cursor: pointer;
  transition: all 0.3s;
}
.action-item:hover {
  background: #f0f7ff;
  transform: translateY(-1px);
}
.action-icon {
  font-size: 16px;
}
.action-text {
  font-size: 12px;
  font-weight: 500;
  color: #303133;
}
.action-desc {
  font-size: 10px;
  color: #909399;
}
.chat-item {
  display: flex;
  margin-bottom: 10px;
  align-items: flex-start;
}
.chat-item.ai {
  flex-direction: row;
}
.chat-item.user {
  flex-direction: row-reverse;
}
.chat-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #f5f7fa;
  margin: 0 6px;
  flex-shrink: 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-avatar-img, .user-avatar-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgb(255, 255, 255);
  font-size: 16px;
}
.ai-avatar-img {
  background: linear-gradient(135deg, #6877c1, #61bac2);
}
.user-avatar-img {
  background: #67c23a;
}
.chat-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 75%;
}
.chat-content {
  padding: 8px 12px;
  border-radius: 12px;
  line-height: 1.4;
  word-wrap: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  font-size: 13px;
  white-space: pre-wrap;
}
.chat-item.ai .chat-content {
  background: linear-gradient(to right, #eef5ff, #f0f7ff);
  color: #303133;
  border-bottom-left-radius: 4px;
}
.chat-item.user .chat-content {
  background: linear-gradient(to right, #409eff, #5dade2);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.ai-name, .user-name {
  font-size: 10px;
  color: #909399;
  margin-top: 2px;
  margin-left: 8px;
}
.chat-item.user .user-name {
  margin-left: auto;
  margin-right: 8px;
  text-align: right;
}
.ai-input-container {
  display: flex;
  gap: 6px;
  width: 100%;
  padding: 0 16px;
}
.message-input {
  flex: 1;
}
.message-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  padding-left: 12px;
  padding-right: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  background: #fff;
  font-size: 13px;
}
.send-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.tip-text {
  font-size: 10px;
  color: #909399;
  text-align: center;
  margin-top: 6px;
  padding-bottom: 8px;
}
@media (max-width: 768px) {
  .right-float-dialog {
    width: 90% !important;
    height: 70vh !important;
    right: 5% !important;
  }
}
</style>