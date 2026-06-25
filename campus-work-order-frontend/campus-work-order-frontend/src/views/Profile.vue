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
        <el-col :xs="24" :sm="8" :md="7" class="profile-col">
          <div class="profile-side">
            <img :src="schoolBrand" class="profile-watermark" alt="" />

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
        <el-col :xs="24" :sm="16" :md="17" class="profile-col">
          <div class="info-panel">

            <div class="panel-header">
              <div>
                <div class="panel-title">账号资料</div>
                <div class="panel-subtitle">维护当前账号的登录名、真实姓名和联系方式</div>
              </div>
              <el-button plain @click="openPasswordDialog">
                修改密码
              </el-button>
            </div>

            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="90px"
              class="profile-form"
            >
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="profileForm.username"
                  maxlength="20"
                  show-word-limit
                  clearable
                  placeholder="3-20位字母、数字或下划线"
                  @input="handleUsernameInput"
                />
              </el-form-item>

              <el-form-item label="真实姓名" prop="realName">
                <el-input
                  v-model="profileForm.realName"
                  maxlength="20"
                  show-word-limit
                  clearable
                  placeholder="2-20位，不能包含特殊符号"
                  @input="handleRealNameInput"
                />
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input
                  v-model="profileForm.phone"
                  maxlength="11"
                  clearable
                  placeholder="11位手机号，可不填写"
                  @input="handlePhoneInput"
                />
              </el-form-item>

              <el-form-item label="角色">
                <el-tag type="primary">
                  {{ roleText(userStore.role) }}
                </el-tag>
              </el-form-item>
            </el-form>

            <div class="profile-actions">
              <el-button type="primary" :loading="savingProfile" @click="submitProfile">
                保存资料
              </el-button>
            </div>

            <div class="tips">
              <div class="tips-title">说明</div>
              <div class="tips-text">
                头像上传后会自动同步 OSS；修改密码需要先输入原密码。
              </div>
            </div>

          </div>
        </el-col>
      </el-row>

    </div>

    <el-dialog v-model="passwordDialog" title="修改密码" width="460px" destroy-on-close>
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            maxlength="30"
            show-password
            placeholder="请输入当前密码"
            @input="handlePasswordInput('oldPassword', $event)"
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            maxlength="30"
            show-password
            placeholder="8-30位，必须包含字母和数字"
            @input="handlePasswordInput('newPassword', $event)"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            maxlength="30"
            show-password
            placeholder="再次输入新密码"
            @input="handlePasswordInput('confirmPassword', $event)"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="passwordDialog = false">取消</el-button>
        <el-button type="primary" :loading="savingPassword" @click="submitPassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useUserStore } from '../stores/user'
import { roleText } from '../utils/status'
import { uploadFile } from '../api/file'
import { changePassword, updateProfile } from '../api/user'
import { ElMessage } from 'element-plus'
import schoolBrand from '../assets/anhui-normal-university-brand.png'

const userStore = useUserStore()
const uploading = ref(false)
const savingProfile = ref(false)
const savingPassword = ref(false)
const passwordDialog = ref(false)
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const profileForm = reactive({
  username: userStore.username || '',
  realName: userStore.realName || '',
  phone: userStore.phone || ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = {
  username: [
    { required: true, whitespace: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度必须在3到20个字符之间', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [
    { required: true, whitespace: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度必须在2到20个字符之间', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5A-Za-z·]{2,20}$/, message: '真实姓名只能包含中文、字母或间隔点', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^$|^1\d{10}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
    return
  }
  callback()
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { max: 30, message: '原密码不能超过30个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 30, message: '新密码长度必须在8到30个字符之间', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d_@#$%^&*!.?-]+$/, message: '新密码必须包含字母和数字，可使用常见符号', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

/* 头像首字母 */
const avatarText = computed(() => {
  const name = userStore.realName || userStore.username || '用'
  return name.slice(0, 1)
})

/* 上传头像 */
async function handleUpload(options) {
  // 头像先上传到 OSS，后端保存头像 URL，前端再同步更新 Pinia 和 sessionStorage。
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

function handleUsernameInput(value) {
  profileForm.username = value.replace(/[^A-Za-z0-9_]/g, '').slice(0, 20)
}

function handlePhoneInput(value) {
  profileForm.phone = value.replace(/\D/g, '').slice(0, 11)
}

function handleRealNameInput(value) {
  profileForm.realName = value.replace(/[^\u4e00-\u9fa5A-Za-z·]/g, '').slice(0, 20)
}

function handlePasswordInput(field, value) {
  passwordForm[field] = value.replace(/\s/g, '').slice(0, 30)
}

async function submitProfile() {
  await profileFormRef.value.validate()
  savingProfile.value = true
  try {
    // 修改个人资料后同步本地登录信息，保证顶部欢迎语和头像区域立即刷新。
    const data = {
      username: profileForm.username.trim(),
      realName: profileForm.realName.trim(),
      phone: profileForm.phone.trim()
    }
    await updateProfile(data)
    userStore.updateBasicInfo(data.realName, data.phone, data.username)
    ElMessage.success('资料已保存')
  } finally {
    savingProfile.value = false
  }
}

function openPasswordDialog() {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
  passwordDialog.value = true
}

async function submitPassword() {
  // 修改密码需要原密码、新密码、确认密码；后端会再次校验原密码是否正确。
  await passwordFormRef.value.validate()
  savingPassword.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    })
    ElMessage.success('密码已修改')
    passwordDialog.value = false
  } finally {
    savingPassword.value = false
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
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.profile-row {
  flex: 1;
  min-height: 0;
  row-gap: 20px;
}

.profile-col {
  display: flex;
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
  width: 100%;
  min-height: 0;
  border-radius: 16px;
  background:
    radial-gradient(circle at 15% 15%, rgba(7, 102, 181, 0.14), transparent 32%),
    linear-gradient(145deg, #ffffff 0%, #f5f9ff 100%);
  color: #303133;
  border: 1px solid #eef2f7;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.04);
  padding: 22px 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  position: relative;
  overflow: hidden;
}

.profile-side > *:not(.profile-watermark) {
  position: relative;
  z-index: 1;
}

.profile-watermark {
  position: absolute;
  right: -24px;
  bottom: -28px;
  width: 155px;
  opacity: 0.08;
  pointer-events: none;
  user-select: none;
}

.avatar {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0766b5 0%, #004985 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34px;
  font-weight: 800;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  border: 4px solid #eef6ff;
  box-shadow: 0 8px 18px rgba(0, 75, 137, 0.18);
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
  margin: 14px 0 10px;
}

.avatar-hint {
  margin-top: 14px;
  padding-top: 14px;
  width: 100%;
  color: #909399;
  font-size: 12px;
  text-align: center;
  border-top: 1px solid #eef2f7;
}

.info-panel {
  width: 100%;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #eef2f7;
  padding: 20px;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.04);
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding-bottom: 14px;
  margin-bottom: 18px;
  border-bottom: 1px solid #eef2f7;
}

.panel-title {
  font-size: 16px;
  font-weight: 800;
  color: #303133;
}

.panel-subtitle {
  margin-top: 5px;
  color: #909399;
  font-size: 13px;
}

.profile-form {
  max-width: 430px;
  padding: 2px 4px 0;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #dbe3ec inset;
}

.profile-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #b8d7f4 inset;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.profile-actions {
  display: flex;
  gap: 10px;
  padding-left: 90px;
  padding-top: 2px;
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
