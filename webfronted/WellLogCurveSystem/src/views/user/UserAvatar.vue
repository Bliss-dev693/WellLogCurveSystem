<script setup>
import { ref } from 'vue'
import { Plus, Upload } from '@element-plus/icons-vue'
import { userUpdateAvatarService } from '@/api/user'
import { uploadFileService } from '@/api/fileUpload'
import { useUserStore } from '@/stores'
import { ElMessage } from 'element-plus'
const userStore = useUserStore()

const uploadRef = ref()
const imgUrl = ref(userStore.user.userPic)

const onSelectFile = (uploadFile) => {
  // 基于 FileReader 读取图片做预览
  const reader = new FileReader();
  reader.readAsDataURL(uploadFile.raw);
  reader.onload = () => {
    imgUrl.value = reader.result;
  };
};

const onUpdateAvatar = async () => {
  if (!imgUrl.value) {
    ElMessage({ type: 'warning', message: '请先选择图片' })
    return
  }
  
  // 检查是否是新选择的文件（data:image/ 开头的是本地预览图）
  if (typeof imgUrl.value === 'string' && imgUrl.value.startsWith('data:image/')) {
    // 需要先上传文件
    try {
      // 创建一个临时的File对象用于上传
      const arr = imgUrl.value.split(',')
      const mime = arr[0].match(/:(.*?);/)[1]
      const bstr = atob(arr[1]) 
      let n = bstr.length
      const u8arr = new Uint8Array(n)
      while(n--){
          u8arr[n] = bstr.charCodeAt(n)
      }
      const file = new File([u8arr], `avatar_${Date.now()}.${mime.split('/')[1]}`, { type: mime })
      
      const res = await uploadFileService(file)
      const avatarUrl = res.data.data
      
      // 调用更新头像接口
      await userUpdateAvatarService(avatarUrl)
      await userStore.getUser()
      ElMessage({ type: 'success', message: '更换头像成功' })
    } catch (error) {
      console.error('上传头像失败', error)
      ElMessage({ type: 'error', message: '更换头像失败' })
    }
  } else if (typeof imgUrl.value === 'string' && !imgUrl.value.includes('localhost')) {
    // 如果是有效的外部URL，直接更新
    try {
      await userUpdateAvatarService(imgUrl.value)
      await userStore.getUser()
      ElMessage({ type: 'success', message: '更换头像成功' })
    } catch (error) {
      console.error('更新头像失败', error)
      ElMessage({ type: 'error', message: '更换头像失败' })
    }
  }
} 
</script>

<template>
  <page-container title="更换头像">
    <el-row>
      <el-col :span="12">
        <el-upload
          ref="uploadRef"
          class="avatar-uploader"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="onSelectFile"
        >
          <img v-if="imgUrl" :src="imgUrl" class="avatar" />
          <img v-else src="@/assets/avatar.jpg" width="278" />
        </el-upload>
        <br />
        <el-button
          @click="uploadRef.$el.querySelector('input').click()"
          type="primary"
          :icon="Plus"
          size="large"
          >选择图片</el-button
        >
        <el-button @click="onUpdateAvatar" type="success" :icon="Upload" size="large">
          上传头像
        </el-button>
      </el-col>
    </el-row>
  </page-container>
</template>

<style lang="scss" scoped>
.avatar-uploader {
  :deep() {
    .avatar {
      width: 278px;
      height: 278px;
      display: block;
    }
    .el-upload {
      border: 1px dashed var(--el-border-color);
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);
    }
    .el-upload:hover {
      border-color: var(--el-color-primary);
    }
    .el-icon.avatar-uploader-icon {
      font-size: 28px;
      color: var(--muted-color);
      width: 278px;
      height: 278px;
      text-align: center;
    }
  }
}
</style>