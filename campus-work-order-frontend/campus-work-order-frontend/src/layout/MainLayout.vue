<template>
  <el-container class="layout">

    <!-- 左侧 -->
    <el-aside width="220px" class="aside">
      <div class="logo">
        <div class="school-identity" aria-label="安徽师范大学">
          <div class="school-emblem">
            <img :src="schoolBrand" alt="安徽师范大学校徽" />
          </div>
          <div class="school-wordmark">
            <strong>安徽师范大学</strong>
            <small>ANHUI NORMAL UNIVERSITY</small>
          </div>
        </div>
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
        </template>

        <template v-if="userStore.role === 'STUDENT' || userStore.role === 'WORKER'">
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
          <el-button class="header-action profile-action" @click="goProfile">
            <el-icon><User /></el-icon>
            <span>用户信息</span>
          </el-button>

          <!-- 退出 -->
          <el-button class="header-action logout-action" @click="logout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-button>

        </div>
      </el-header>

      <!-- 标签导航 -->
      <div class="tags-view">
        <div class="tags-scroll">
          <div
            v-for="tag in visitedTags"
            :key="tag.path"
            class="tag-item"
            :class="{ active: tag.path === route.path }"
            @click="goTag(tag)"
            @contextmenu.prevent="openTagMenu($event, tag)"
          >
            <span>{{ tag.title }}</span>
            <el-icon
              v-if="!tag.affix"
              class="tag-close"
              @click.stop="closeTag(tag)"
            >
              <Close />
            </el-icon>
          </div>
        </div>

        <div
          v-if="tagMenu.visible"
          class="tag-context-menu"
          :style="{ left: `${tagMenu.left}px`, top: `${tagMenu.top}px` }"
        >
          <div
            class="context-menu-item"
            :class="{ disabled: tagMenu.tag?.affix }"
            @click="handleTagCommand('close-current')"
          >
            关闭当前
          </div>
          <div class="context-menu-item" @click="handleTagCommand('close-other')">关闭其他</div>
          <div class="context-menu-item" @click="handleTagCommand('close-all')">关闭全部</div>
        </div>
      </div>

      <!-- 内容 -->
      <el-main>
        <router-view />
      </el-main>

    </el-container>

    <!-- 👇 头像预览弹窗 -->
    <el-dialog
      v-model="avatarDialog"
      title="头像预览"
      width="420px"
      align-center
      :close-on-click-modal="true"
      class="avatar-preview-dialog"
    >
      <div class="avatar-preview-wrap">
        <el-image
          v-if="userStore.avatarUrl"
          :src="userStore.avatarUrl"
          :preview-src-list="[userStore.avatarUrl]"
          fit="cover"
          class="avatar-preview-image"
          preview-teleported
          hide-on-click-modal
        />
        <div v-else class="no-avatar">
          暂无头像
        </div>
      </div>
    </el-dialog>

  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { roleText } from '../utils/status'
import { logout as logoutApi } from '../api/auth'
import { Close, SwitchButton, User } from '@element-plus/icons-vue'
import schoolBrand from '../assets/anhui-normal-university-brand.png'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const avatarDialog = ref(false)
// 已访问标签页列表。首页固定保留，其它页面按访问顺序动态加入。
const visitedTags = ref([
  { path: '/dashboard', title: '首页仪表盘', affix: true }
])

// 右键标签弹出的快捷菜单状态。
const tagMenu = ref({
  visible: false,
  left: 0,
  top: 0,
  tag: null
})

const currentTag = computed(() => visitedTags.value.find(tag => tag.path === route.path))

watch(
  () => route.path,
  () => {
    addVisitedTag()
    closeTagMenu()
  },
  { immediate: true }
)

function getRouteTitle() {
  return route.meta?.title || route.name || route.path
}

function addVisitedTag() {
  // 登录、注册页不属于后台主布局，不加入标签导航。
  if (route.path === '/login' || route.path === '/register') {
    return
  }

  if (visitedTags.value.some(tag => tag.path === route.path)) {
    return
  }

  visitedTags.value.push({
    path: route.path,
    title: getRouteTitle(),
    affix: Boolean(route.meta?.affix)
  })
}

function goTag(tag) {
  if (tag.path !== route.path) {
    router.push(tag.path)
  }
}

function closeTag(tag) {
  // 固定标签不允许关闭，例如首页仪表盘。
  if (tag.affix) {
    return
  }

  const index = visitedTags.value.findIndex(item => item.path === tag.path)
  if (index === -1) {
    return
  }

  visitedTags.value.splice(index, 1)

  if (tag.path === route.path) {
    const nextTag = visitedTags.value[index] || visitedTags.value[index - 1] || visitedTags.value[0]
    router.push(nextTag?.path || '/dashboard')
  }
}

function handleTagCommand(command) {
  // 右键菜单操作默认作用于被右键点击的标签，而不是一定作用于当前页面。
  const targetTag = tagMenu.value.tag || currentTag.value

  if (command === 'close-current' && targetTag) {
    if (!targetTag.affix) {
      closeTag(targetTag)
    }
    closeTagMenu()
    return
  }

  if (command === 'close-other') {
    const keepPath = targetTag?.path || route.path
    visitedTags.value = visitedTags.value.filter(tag => tag.affix || tag.path === keepPath)
    if (route.path !== keepPath) {
      router.push(keepPath)
    }
    closeTagMenu()
    return
  }

  if (command === 'close-all') {
    visitedTags.value = visitedTags.value.filter(tag => tag.affix)
    if (route.path !== '/dashboard') {
      router.push('/dashboard')
    }
    closeTagMenu()
  }
}

function openTagMenu(event, tag) {
  tagMenu.value = {
    visible: true,
    left: event.clientX,
    top: event.clientY,
    tag
  }
}

function closeTagMenu() {
  tagMenu.value.visible = false
}

onMounted(() => {
  window.addEventListener('click', closeTagMenu)
})

onBeforeUnmount(() => {
  window.removeEventListener('click', closeTagMenu)
})

function goProfile() {
  router.push('/profile')
}

async function logout() {
  try {
    await logoutApi()
  } finally {
    userStore.logout()
    visitedTags.value = [{ path: '/dashboard', title: '首页仪表盘', affix: true }]
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
  min-height: 108px;
  padding: 13px 14px 11px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.12em;
  border-bottom: 1px solid rgba(255, 255, 255, 0.14);
}

.school-identity {
  box-sizing: border-box;
  width: 192px;
  min-height: 66px;
  padding: 7px 10px 7px 8px;
  display: flex;
  align-items: center;
  gap: 9px;
  overflow: hidden;
  border: 1px solid rgba(214, 167, 44, 0.38);
  border-radius: 11px;
  background: linear-gradient(145deg, #ffffff 0%, #f7fbff 100%);
  box-shadow: 0 7px 18px rgba(0, 22, 48, 0.22);
}

.school-emblem {
  width: 52px;
  height: 52px;
  flex: 0 0 52px;
  overflow: hidden;
}

.school-emblem img {
  width: auto;
  height: 52px;
  display: block;
  max-width: none;
}

.school-wordmark {
  min-width: 0;
  padding-left: 9px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  border-left: 1px solid rgba(214, 167, 44, 0.65);
  line-height: 1;
}

.school-wordmark strong {
  color: #073f77;
  font-family: "STKaiti", "KaiTi", "Noto Serif SC", serif;
  font-size: 17px;
  font-weight: 800;
  letter-spacing: 0.04em;
  white-space: nowrap;
}

.school-wordmark small {
  color: #a77a12;
  font-family: Georgia, "Times New Roman", serif;
  font-size: 5.5px;
  font-weight: 700;
  letter-spacing: 0.02em;
  white-space: nowrap;
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

.header-action.el-button {
  height: 36px;
  margin-left: 0;
  padding: 0 15px;
  gap: 6px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 700;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
}

.header-action.el-button:hover {
  transform: translateY(-1px);
}

.profile-action.el-button {
  color: #fff;
  border-color: #07589f;
  background: linear-gradient(135deg, #0766b5 0%, #004985 100%);
  box-shadow: 0 5px 13px rgba(0, 75, 137, 0.2);
}

.profile-action.el-button:hover,
.profile-action.el-button:focus {
  color: #fff;
  border-color: #0873c9;
  background: linear-gradient(135deg, #0873c9 0%, #00569b 100%);
  box-shadow: 0 7px 16px rgba(0, 75, 137, 0.28);
}

.logout-action.el-button {
  color: #64748b;
  border-color: #dbe3ec;
  background: #f8fafc;
}

.logout-action.el-button:hover,
.logout-action.el-button:focus {
  color: #d64242;
  border-color: #f2b8b8;
  background: #fff5f5;
  box-shadow: 0 5px 13px rgba(214, 66, 66, 0.12);
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
.avatar-preview-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 8px 0 4px;
}

.avatar-preview-image {
  width: 240px;
  height: 240px;
  border-radius: 18px;
  overflow: hidden;
  cursor: zoom-in;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.14);
}

.no-avatar {
  padding: 40px;
  color: #909399;
}

.tags-view {
  height: 42px;
  padding: 6px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border-bottom: 1px solid #e8edf3;
  box-sizing: border-box;
}

.tags-scroll {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  overflow-y: hidden;
}

.tags-scroll::-webkit-scrollbar {
  height: 0;
}

.tag-item {
  height: 28px;
  padding: 0 10px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex: 0 0 auto;
  border: 1px solid #dbe3ec;
  border-radius: 8px;
  background: #f8fafc;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.18s ease;
}

.tag-item:hover {
  color: #07589f;
  border-color: #bcd7f0;
  background: #f0f7ff;
}

.tag-item.active {
  color: #fff;
  border-color: #07589f;
  background: linear-gradient(135deg, #0766b5 0%, #004985 100%);
  box-shadow: 0 4px 10px rgba(0, 75, 137, 0.16);
}

.tag-close {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  font-size: 12px;
}

.tag-close:hover {
  background: rgba(255, 255, 255, 0.25);
}

.tag-context-menu {
  position: fixed;
  z-index: 3000;
  min-width: 116px;
  padding: 6px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.16);
}

.context-menu-item {
  height: 30px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  border-radius: 7px;
  color: #334155;
  font-size: 13px;
  cursor: pointer;
}

.context-menu-item:hover {
  color: #07589f;
  background: #f0f7ff;
}

.context-menu-item.disabled {
  color: #cbd5e1;
  cursor: not-allowed;
  background: transparent;
}

/* 内容 */
:deep(.el-main) {
  height: calc(100vh - 102px);
  padding: 14px;
  overflow: hidden;
  background: #f5f7fb;
}
</style>
