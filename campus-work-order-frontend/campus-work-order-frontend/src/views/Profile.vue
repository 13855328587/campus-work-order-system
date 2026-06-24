<template>
  <div class="profile-page">
    <div class="page-card profile-card">

      <!-- 标题 -->
      <div class="page-header">
        <div>
          <div class="page-title">用户信息</div>
          <div class="page-subtitle">
            查看当前登录账号的基本信息和系统身份。
          </div>
        </div>
      </div>

      <el-row :gutter="20" class="profile-row">

        <!-- 左侧 -->
        <el-col :xs="24" :sm="9" :md="8">
          <div class="profile-side">

            <!-- 上传头像 -->
            <el-upload
              :http-request="handleUpload"
              :show-file-list="false"
              :disabled="uploading"
              accept="image/jpeg,image/png,image/webp"
            >
              <div class="avatar" :class="{ 'is-uploading': uploading }">
                <img
                  v-if="userStore.avatarUrl"
                  :src="userStore.avatarUrl"
                  alt="用户头像"
                  class="avatar-img"
                />
                <span v-else>
                  {{ avatarText }}
                </span>
                <div class="avatar-mask">
                  {{ uploading ? '上传中…' : '更换头像' }}
                </div>
              </div>
            </el-upload>

            <div class="name">
              {{ userStore.realName || userStore.username }}
            </div>

            <el-tag type="primary" size="large">
              {{ roleText(userStore.role) }}
            </el-tag>

            <div class="avatar-hint">支持 JPG、PNG、WebP，最大 2MB</div>

          </div>
        </el-col>

        <!-- 右侧 -->
        <el-col :xs="24" :sm="15" :md="16">
          <div class="info-panel">

            <el-descriptions :column="2" border class="user-descriptions">

              <el-descriptions-item label="用户名">
                {{ userStore.username || '暂无' }}
              </el-descriptions-item>

              <el-descriptions-item label="真实姓名">
                {{ userStore.realName || '暂无' }}
              </el-descriptions-item>

              <el-descriptions-item label="手机号">
                {{ userStore.phone || '暂无' }}
              </el-descriptions-item>

              <el-descriptions-item label="角色">
                <el-tag type="primary">
                  {{ roleText(userStore.role) }}
                </el-tag>
              </el-descriptions-item>

            </el-descriptions>

            <div class="tips">
              <div class="tips-title">说明</div>
              <div class="tips-text">
                头像上传后会自动同步 OSS 并更新用户信息。
              </div>
            </div>

          </div>
        </el-col>
      </el-row>

    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { roleText } from '../utils/status'
import { uploadFile } from '../api/file'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const uploading = ref(false)

/* 头像首字母 */
const avatarText = computed(() => {
  const name = userStore.realName || userStore.username || '用'
  return name.slice(0, 1)
})

/* 上传头像 */
async function handleUpload(options) {
  const file = options.file

  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.warning('头像仅支持 JPG、PNG 或 WebP 格式')
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('头像大小不能超过 2MB')
    return
  }

  uploading.value = true

  try {
    const url = await uploadFile(file)
    userStore.setAvatarUrl(url)
    ElMessage.success('头像上传成功')
    options.onSuccess?.(url)
  } catch (error) {
    options.onError?.(error)
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.profile-page {
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.profile-card {
  height: 100%;
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.profile-row {
  row-gap: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 800;
}

.page-subtitle {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.profile-side {
  min-height: 230px;
  border-radius: 16px;
  background: linear-gradient(135deg, #409eff, #67c23a);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: rgba(255,255,255,0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  font-weight: 800;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 3px solid rgba(255, 255, 255, 0.45);
  transition: transform 0.2s ease;
}

.avatar:hover {
  transform: translateY(-2px);
}

.avatar.is-uploading {
  cursor: wait;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.58);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.avatar:hover .avatar-mask,
.avatar.is-uploading .avatar-mask {
  opacity: 1;
}

.name {
  font-size: 20px;
  font-weight: 800;
  margin: 12px 0;
}

.avatar-hint {
  margin-top: 12px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 12px;
}

.info-panel {
  border-radius: 16px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  padding: 18px;
}

.user-descriptions :deep(.el-descriptions__label) {
  width: 110px;
}

.tips {
  margin-top: 18px;
  padding: 14px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #eef2f7;
}

.tips-title {
  font-weight: 800;
  margin-bottom: 6px;
}

.tips-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

@media (max-width: 767px) {
  .profile-side {
    min-height: 240px;
  }

  .user-descriptions :deep(.el-descriptions__body .el-descriptions__table) {
    table-layout: auto;
  }
}
</style>
