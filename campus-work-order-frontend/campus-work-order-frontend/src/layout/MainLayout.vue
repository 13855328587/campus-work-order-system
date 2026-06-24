<template>
  <el-container class="layout">

    <!-- 左侧 -->
    <el-aside width="220px" class="aside">
      <div class="logo">
        <img :src="schoolBrand" alt="安徽师范大学" />
        <span>校园智慧工单系统</span>
      </div>

      <el-menu
        router
        :default-active="$route.path"
        background-color="#003f78"
        text-color="#dbeafe"
        active-text-color="#f4c95d"
      >
        <el-menu-item index="/dashboard">首页仪表盘</el-menu-item>

        <template v-if="userStore.role === 'STUDENT'">
          <el-menu-item index="/student/create-order">新建工单</el-menu-item>
          <el-menu-item index="/student/my-orders">我的工单</el-menu-item>
        </template>

        <template v-if="userStore.role === 'ADMIN' || userStore.role === 'SUPER_ADMIN'">
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/orders">工单管理</el-menu-item>
        </template>

        <template v-if="userStore.role === 'WORKER'">
          <el-menu-item index="/worker/tasks">我的任务</el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 右侧 -->
    <el-container>

      <!-- 顶部 -->
      <el-header class="header">

        <!-- 左侧欢迎 -->
        <div class="welcome-text">
          欢迎，{{ userStore.realName || userStore.username }}
          （{{ roleText(userStore.role) }}）
        </div>

        <!-- 右侧 -->
        <div class="header-right">

          <!-- 头像（点击预览） -->
          <div class="avatar" @click="avatarDialog = true">
            <img
              v-if="userStore.avatarUrl"
              :src="userStore.avatarUrl"
            />
            <span v-else>
              {{ (userStore.realName || userStore.username || 'U').slice(0,1) }}
            </span>
          </div>

          <!-- 用户信息 -->
          <el-button type="primary" plain size="small" @click="goProfile">
            用户信息
          </el-button>

          <!-- 退出 -->
          <el-button type="danger" plain size="small" @click="logout">
            退出登录
          </el-button>

        </div>
      </el-header>

      <!-- 内容 -->
      <el-main>
        <router-view />
      </el-main>

    </el-container>

    <!-- 👇 头像预览弹窗 -->
    <el-dialog
      v-model="avatarDialog"
      title="头像预览"
      width="320px"
      align-center
    >
      <div class="avatar-preview">
        <img
          v-if="userStore.avatarUrl"
          :src="userStore.avatarUrl"
        />
        <div v-else class="no-avatar">
          暂无头像
        </div>
      </div>
    </el-dialog>

  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { roleText } from '../utils/status'
import { logout as logoutApi } from '../api/auth'
import schoolBrand from '../assets/anhui-normal-university-brand.png'

const router = useRouter()
const userStore = useUserStore()

const avatarDialog = ref(false)

function goProfile() {
  router.push('/profile')
}

async function logout() {
  try {
    await logoutApi()
  } finally {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
  overflow: hidden;
}

.layout > .el-container {
  height: 100vh;
  min-width: 0;
  overflow: hidden;
}

/* 左侧 */
.aside {
  background: linear-gradient(180deg, #003f78 0%, #002d57 100%);
}

.logo {
  min-height: 96px;
  padding: 12px 14px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.12em;
  border-bottom: 1px solid rgba(255, 255, 255, 0.14);
}

.logo img {
  width: 190px;
  padding: 5px 8px;
  border-radius: 8px;
  background: #fff;
}

:deep(.el-menu) {
  border-right: 0;
}

:deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.1) !important;
  border-right: 3px solid #d6a72c;
}

/* 顶部 */
.header {
  height: 60px;
  padding: 0 20px;
  background: #fff;
  border-bottom: 2px solid #d6a72c;

  display: flex;
  align-items: center;
  justify-content: space-between;
}

.welcome-text {
  font-size: 14px;
  color: #303133;
}

/* 右侧 */
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 头像 */
.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;

  display: flex;
  align-items: center;
  justify-content: center;

  font-weight: bold;
  overflow: hidden;
  cursor: pointer;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 弹窗头像 */
.avatar-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
}

.avatar-preview img {
  width: 220px;
  height: 220px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #409eff;
}

.no-avatar {
  padding: 40px;
  color: #909399;
}

/* 内容 */
:deep(.el-main) {
  height: calc(100vh - 60px);
  padding: 14px;
  overflow: hidden;
  background: #f5f7fb;
}
</style>
