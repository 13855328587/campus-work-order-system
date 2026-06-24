<template>
  <div class="dashboard-page">
    <div class="dashboard-heading">
      <div>
        <div class="page-title">首页仪表盘</div>
        <div class="campus-subtitle">安徽师范大学校园智慧运维服务</div>
      </div>
      <img :src="schoolBrand" alt="安徽师范大学" />
    </div>

    <!-- 管理员 / 超级管理员专属欢迎横幅 -->
    <div v-if="isAdminRole" class="admin-hero">
      <div class="hero-left">
        <div class="hero-badge">
          {{ userStore.role === 'SUPER_ADMIN' ? '超级管理控制台' : '管理员工作台' }}
        </div>
        <div class="hero-title">
          欢迎回来，{{ userStore.realName || userStore.username }}
        </div>
        <div class="hero-subtitle">
          你可以审核学生工单、分配维修人员、管理用户状态，并查看整体工单处理情况。
        </div>

        <div class="hero-actions">
          <el-button type="primary" size="small" @click="$router.push('/admin/orders')">
            工单管理
          </el-button>
          <el-button plain size="small" @click="$router.push('/admin/users')">
            用户管理
          </el-button>
          <el-button plain size="small" @click="reloadDashboard">
            刷新概览
          </el-button>
        </div>
      </div>

      <div class="hero-right">
        <div class="hero-circle"></div>
        <div class="hero-icon">🧑‍💼</div>
        <div class="hero-mini mini-one">
          待审核 {{ statistics.PENDING_REVIEW || 0 }}
        </div>
        <div class="hero-mini mini-two">
          处理中 {{ statistics.PROCESSING || 0 }}
        </div>
      </div>
    </div>

    <!-- 学生专属欢迎横幅 -->
    <div v-if="userStore.role === 'STUDENT'" class="student-hero">
      <div class="hero-left">
        <div class="hero-badge">学生服务台</div>
        <div class="hero-title">
          欢迎回来，{{ userStore.realName || userStore.username }}
        </div>
        <div class="hero-subtitle">
          你可以在线提交校园维修工单，实时查看审核、处理和完成进度。
        </div>

        <div class="hero-actions">
          <el-button type="primary" size="small" @click="$router.push('/student/create-order')">
            新建工单
          </el-button>
          <el-button plain size="small" @click="$router.push('/student/my-orders')">
            查看我的工单
          </el-button>
        </div>
      </div>

      <div class="hero-right">
        <div class="hero-circle"></div>
        <div class="hero-icon">🎓</div>
        <div class="hero-mini mini-one">
          待审核 {{ statistics.PENDING_REVIEW || 0 }}
        </div>
        <div class="hero-mini mini-two">
          处理中 {{ statistics.PROCESSING || 0 }}
        </div>
      </div>
    </div>

    <!-- 维修人员专属欢迎横幅 -->
    <div v-if="userStore.role === 'WORKER'" class="worker-hero">
      <div class="hero-left">
        <div class="hero-badge">维修工作台</div>
        <div class="hero-title">
          欢迎回来，{{ userStore.realName || userStore.username }}
        </div>
        <div class="hero-subtitle">
          优先关注高优先级工单，接单后及时处理，完成后填写处理结果。
        </div>

        <div class="hero-actions">
          <el-button type="primary" size="small" @click="$router.push('/worker/tasks')">
            查看我的任务
          </el-button>
          <el-button plain size="small" @click="reloadDashboard">
            刷新概览
          </el-button>
        </div>
      </div>

      <div class="hero-right">
        <div class="hero-circle"></div>
        <div class="hero-icon">🛠️</div>
        <div class="hero-mini mini-one">
          待处理 {{ statistics.PENDING_PROCESS || 0 }}
        </div>
        <div class="hero-mini mini-two">
          处理中 {{ statistics.PROCESSING || 0 }}
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="14">
      <el-col :span="cardSpan" v-for="item in cards" :key="item.label">
        <el-card class="stat-card" :class="item.className">
          <div class="stat-icon">{{ item.icon }}</div>
          <div class="stat-info">
            <div class="stat-value">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <DashboardCharts :orders="analyticsOrders" />

    <!-- 管理员 / 超级管理员专属内容 -->
    <template v-if="isAdminRole">
      <el-row :gutter="14" class="role-section">
        <el-col :span="14">
          <div class="page-card clean-card">
            <div class="section-header">
              <div>
                <h3>待审核工单提醒</h3>
                <p>优先处理学生新提交的工单，避免长时间等待。</p>
              </div>
              <div class="section-icon purple-icon">📝</div>
            </div>

            <el-table :data="adminPendingReviewOrders" border size="small" height="180">
              <el-table-column prop="orderNo" label="工单编号" width="160" />
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="location" label="地点" />
              <el-table-column prop="priority" label="优先级" width="90" />
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="statusType(row.status)" size="small">
                    {{ statusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>

            <div class="more-link" @click="$router.push('/admin/orders')">
              查看全部工单 →
            </div>
          </div>
        </el-col>

        <el-col :span="10">
          <div class="page-card clean-card">
            <div class="section-header">
              <div>
                <h3>管理快捷入口</h3>
                <p>常用管理操作集中入口。</p>
              </div>
              <div class="section-icon blue-icon">⚙️</div>
            </div>

            <div class="quick-list">
              <div class="quick-item" @click="$router.push('/admin/orders')">
                <div class="quick-icon blue-quick">审</div>
                <div>
                  <div class="quick-title">审核与分配工单</div>
                  <div class="quick-desc">审核学生提交的工单，并分配维修人员。</div>
                </div>
              </div>

              <div class="quick-item" @click="$router.push('/admin/users')">
                <div class="quick-icon orange-quick">人</div>
                <div>
                  <div class="quick-title">用户权限管理</div>
                  <div class="quick-desc">管理学生、维修人员和管理员账号状态。</div>
                </div>
              </div>

              <div class="quick-item" @click="reloadDashboard">
                <div class="quick-icon green-quick">刷</div>
                <div>
                  <div class="quick-title">刷新统计数据</div>
                  <div class="quick-desc">重新加载当前工单统计和最新记录。</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="14" class="role-section">
        <el-col :span="12">
          <div class="page-card clean-card progress-card">
            <div class="section-header">
              <div>
                <h3>工单处理概览</h3>
                <p>根据全系统工单状态自动统计。</p>
              </div>
              <div class="section-icon orange-icon">📊</div>
            </div>

            <div class="progress-block">
              <div class="progress-row">
                <div class="progress-top">
                  <span>处理中工单占比</span>
                  <strong>{{ statistics.PROCESSING || 0 }}</strong>
                </div>
                <el-progress :percentage="adminProcessingPercent" :stroke-width="12" />
              </div>

              <div class="progress-row">
                <div class="progress-top">
                  <span>已完成工单占比</span>
                  <strong>{{ statistics.COMPLETED || 0 }}</strong>
                </div>
                <el-progress
                  :percentage="adminCompletedPercent"
                  :stroke-width="12"
                  status="success"
                />
              </div>

              <div class="progress-summary">
                <div>
                  <span>工单总数</span>
                  <strong>{{ totalAdminOrders }}</strong>
                </div>
                <div>
                  <span>等待审核</span>
                  <strong>{{ statistics.PENDING_REVIEW || 0 }}</strong>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="page-card clean-card">
            <div class="section-header">
              <div>
                <h3>最近完成工单</h3>
                <p>展示系统中最近完成的工单记录。</p>
              </div>
              <div class="section-icon green-icon">✅</div>
            </div>

            <div v-if="adminCompletedOrders.length > 0" class="history-list">
              <div
                v-for="item in adminCompletedOrders"
                :key="item.id"
                class="history-item"
              >
                <div class="history-main">
                  <div class="history-title">{{ item.title }}</div>
                  <div class="history-meta">
                    {{ item.orderNo }} ｜ {{ item.location }}
                  </div>
                </div>

                <div class="history-result">
                  <div class="result-label">处理结果</div>
                  <div class="result-text">{{ item.finishResult || '暂无说明' }}</div>
                </div>
              </div>
            </div>

            <el-empty v-else description="暂无完成记录" :image-size="60" />
          </div>
        </el-col>
      </el-row>
    </template>

    <!-- 学生专属内容 -->
    <template v-if="userStore.role === 'STUDENT'">
      <el-row :gutter="14" class="role-section">
        <el-col :span="14">
          <div class="page-card clean-card">
            <div class="section-header">
              <div>
                <h3>最近提交工单</h3>
                <p>展示你最近提交的工单和处理状态。</p>
              </div>
              <div class="section-icon blue-icon">📋</div>
            </div>

            <el-table :data="studentRecentOrders" border size="small" height="180">
              <el-table-column prop="orderNo" label="工单编号" width="160" />
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="location" label="地点" />
              <el-table-column prop="priority" label="优先级" width="90" />
              <el-table-column label="状态" width="110">
                <template #default="{ row }">
                  <el-tag :type="statusType(row.status)" size="small">
                    {{ statusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>

            <div class="more-link" @click="$router.push('/student/my-orders')">
              查看全部工单 →
            </div>
          </div>
        </el-col>

        <el-col :span="10">
          <div class="page-card clean-card">
            <div class="section-header">
              <div>
                <h3>快捷操作</h3>
                <p>常用入口和处理提示。</p>
              </div>
              <div class="section-icon green-icon">⚡</div>
            </div>

            <div class="quick-list">
              <div class="quick-item" @click="$router.push('/student/create-order')">
                <div class="quick-icon blue-quick">＋</div>
                <div>
                  <div class="quick-title">提交新工单</div>
                  <div class="quick-desc">宿舍、网络、设备问题都可以在线反馈。</div>
                </div>
              </div>

              <div class="quick-item" @click="$router.push('/student/my-orders')">
                <div class="quick-icon orange-quick">查</div>
                <div>
                  <div class="quick-title">查看处理进度</div>
                  <div class="quick-desc">查看审核、处理和完成状态。</div>
                </div>
              </div>

              <div class="quick-item">
                <div class="quick-icon green-quick">✓</div>
                <div>
                  <div class="quick-title">填写清晰描述</div>
                  <div class="quick-desc">地点、问题现象越清楚，处理越高效。</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="14" class="role-section">
        <el-col :span="12">
          <div class="page-card clean-card progress-card">
            <div class="section-header">
              <div>
                <h3>我的工单进度</h3>
                <p>根据你的工单状态自动统计。</p>
              </div>
              <div class="section-icon orange-icon">📊</div>
            </div>

            <div class="progress-block">
              <div class="progress-row">
                <div class="progress-top">
                  <span>处理中工单</span>
                  <strong>{{ statistics.PROCESSING || 0 }}</strong>
                </div>
                <el-progress :percentage="studentProcessingPercent" :stroke-width="12" />
              </div>

              <div class="progress-row">
                <div class="progress-top">
                  <span>已完成工单占比</span>
                  <strong>{{ statistics.COMPLETED || 0 }}</strong>
                </div>
                <el-progress
                  :percentage="studentCompletedPercent"
                  :stroke-width="12"
                  status="success"
                />
              </div>

              <div class="progress-summary">
                <div>
                  <span>我的工单总数</span>
                  <strong>{{ totalStudentCount }}</strong>
                </div>
                <div>
                  <span>等待处理</span>
                  <strong>{{ statistics.PENDING_REVIEW + statistics.PENDING_PROCESS }}</strong>
                </div>
              </div>
            </div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="page-card clean-card">
            <div class="section-header">
              <div>
                <h3>提交建议</h3>
                <p>规范填写可以提高处理效率。</p>
              </div>
              <div class="section-icon purple-icon">💡</div>
            </div>

            <div class="step-list">
              <div class="step-item">
                <div class="step-num blue-step">1</div>
                <div>
                  <div class="step-title">写清问题地点</div>
                  <div class="step-desc">例如：3号宿舍楼502、教学楼A区203。</div>
                </div>
              </div>

              <div class="step-item">
                <div class="step-num orange-step">2</div>
                <div>
                  <div class="step-title">描述具体现象</div>
                  <div class="step-desc">说明无法启动、断网、漏水等具体情况。</div>
                </div>
              </div>

              <div class="step-item">
                <div class="step-num green-step">3</div>
                <div>
                  <div class="step-title">及时查看进度</div>
                  <div class="step-desc">工单审核和处理结果会在系统中更新。</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </template>

    <!-- 维修人员专属内容 -->
    <template v-if="userStore.role === 'WORKER'">
      <el-row :gutter="14" class="role-section">
        <el-col :span="14">
          <div class="page-card clean-card">
            <div class="section-header">
              <div>
                <h3>待处理任务提醒</h3>
                <p>最近需要关注的工单。</p>
              </div>
              <div class="section-icon blue-icon">📌</div>
            </div>

            <el-table :data="pendingPreview" border size="small" height="145">
              <el-table-column prop="orderNo" label="工单编号" width="160" />
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="location" label="地点" />
              <el-table-column prop="priority" label="优先级" width="90" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="statusType(row.status)" size="small">
                    {{ statusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>

            <div class="more-link" @click="$router.push('/worker/tasks')">
              查看全部任务 →
            </div>
          </div>
        </el-col>

        <el-col :span="10">
          <div class="page-card clean-card advice-card">
            <div class="section-header">
              <div>
                <h3>处理建议</h3>
                <p>按标准流程处理工单。</p>
              </div>
              <div class="section-icon green-icon">✅</div>
            </div>

            <div class="step-list">
              <div class="step-item">
                <div class="step-num blue-step">1</div>
                <div>
                  <div class="step-title">查看任务</div>
                  <div class="step-desc">优先处理高优先级工单。</div>
                </div>
              </div>

              <div class="step-item">
                <div class="step-num orange-step">2</div>
                <div>
                  <div class="step-title">接单处理</div>
                  <div class="step-desc">接单后及时处理并同步状态。</div>
                </div>
              </div>

              <div class="step-item">
                <div class="step-num green-step">3</div>
                <div>
                  <div class="step-title">填写结果</div>
                  <div class="step-desc">完成后填写处理说明。</div>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="14" class="role-section">
        <el-col :span="12">
          <div class="page-card clean-card">
            <div class="section-header">
              <div>
                <h3>最近完成任务</h3>
                <p>最近完成的维修记录。</p>
              </div>
              <div class="section-icon purple-icon">📄</div>
            </div>

            <div v-if="historyPreview.length > 0" class="history-list">
              <div
                v-for="item in historyPreview"
                :key="item.id"
                class="history-item"
              >
                <div class="history-main">
                  <div class="history-title">{{ item.title }}</div>
                  <div class="history-meta">
                    {{ item.orderNo }} ｜ {{ item.location }}
                  </div>
                </div>

                <div class="history-result">
                  <div class="result-label">处理结果</div>
                  <div class="result-text">{{ item.finishResult || '暂无说明' }}</div>
                </div>
              </div>
            </div>

            <el-empty v-else description="暂无完成记录" :image-size="60" />
          </div>
        </el-col>

        <el-col :span="12">
          <div class="page-card clean-card progress-card">
            <div class="section-header">
              <div>
                <h3>个人任务状态</h3>
                <p>展示当前处理进度与完成占比。</p>
              </div>
              <div class="section-icon orange-icon">📊</div>
            </div>

            <div class="progress-block">
              <div class="progress-row">
                <div class="progress-top">
                  <span>当前处理中任务</span>
                  <strong>{{ statistics.PROCESSING || 0 }}</strong>
                </div>
                <el-progress :percentage="processingPercent" :stroke-width="12" />
              </div>

              <div class="progress-row">
                <div class="progress-top">
                  <span>已完成任务占比</span>
                  <strong>{{ statistics.COMPLETED || 0 }}</strong>
                </div>
                <el-progress
                  :percentage="completedPercent"
                  :stroke-width="12"
                  status="success"
                />
              </div>

              <div class="progress-summary">
                <div>
                  <span>总相关任务</span>
                  <strong>{{ totalWorkerCount }}</strong>
                </div>
                <div>
                  <span>当前待处理</span>
                  <strong>{{ statistics.PENDING_PROCESS || 0 }}</strong>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </template>

    <div class="system-strip">
      <div>
        <strong>系统说明：</strong>
        安徽师范大学校园工单管理系统支持学生提交工单、管理员审核分配、维修人员接单并完成处理。
      </div>
      <div>
        当前登录角色：<strong>{{ roleText(userStore.role) }}</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '../stores/user'
import {
  getStatistics,
  getWorkerTasks,
  getWorkerHistory,
  getMyOrders,
  getAllOrders
} from '../api/workOrder'
import { roleText, statusText, statusType } from '../utils/status'
import schoolBrand from '../assets/anhui-normal-university-brand.png'
import DashboardCharts from '../components/DashboardCharts.vue'

const userStore = useUserStore()

const statistics = ref({
  PENDING_REVIEW: 0,
  PENDING_PROCESS: 0,
  PROCESSING: 0,
  COMPLETED: 0
})

const workerTasks = ref([])
const workerHistory = ref([])
const studentOrders = ref([])
const adminOrders = ref([])

const isAdminRole = computed(() => {
  return userStore.role === 'ADMIN' || userStore.role === 'SUPER_ADMIN'
})

const analyticsOrders = computed(() => {
  if (isAdminRole.value) return adminOrders.value
  if (userStore.role === 'STUDENT') return studentOrders.value

  const uniqueOrders = new Map()
  ;[...workerTasks.value, ...workerHistory.value].forEach(order => uniqueOrders.set(order.id, order))
  return [...uniqueOrders.values()]
})

const cards = computed(() => {
  if (userStore.role === 'WORKER') {
    return [
      {
        label: '待处理',
        value: statistics.value.PENDING_PROCESS || 0,
        icon: '📥',
        className: 'blue-card'
      },
      {
        label: '处理中',
        value: statistics.value.PROCESSING || 0,
        icon: '🔧',
        className: 'orange-card'
      },
      {
        label: '已完成',
        value: statistics.value.COMPLETED || 0,
        icon: '✅',
        className: 'green-card'
      }
    ]
  }

  return [
    {
      label: '待审核',
      value: statistics.value.PENDING_REVIEW || 0,
      icon: '📝',
      className: 'purple-card'
    },
    {
      label: '待处理',
      value: statistics.value.PENDING_PROCESS || 0,
      icon: '📥',
      className: 'blue-card'
    },
    {
      label: '处理中',
      value: statistics.value.PROCESSING || 0,
      icon: '🔧',
      className: 'orange-card'
    },
    {
      label: '已完成',
      value: statistics.value.COMPLETED || 0,
      icon: '✅',
      className: 'green-card'
    }
  ]
})

const cardSpan = computed(() => {
  return userStore.role === 'WORKER' ? 8 : 6
})

const adminPendingReviewOrders = computed(() => {
  return adminOrders.value
    .filter(item => item.status === 'PENDING_REVIEW')
    .slice(0, 4)
})

const adminCompletedOrders = computed(() => {
  return adminOrders.value
    .filter(item => item.status === 'COMPLETED')
    .slice(0, 3)
})

const totalAdminOrders = computed(() => {
  return adminOrders.value.length
})

const adminProcessingPercent = computed(() => {
  if (totalAdminOrders.value === 0) {
    return 0
  }
  return Math.round(((statistics.value.PROCESSING || 0) / totalAdminOrders.value) * 100)
})

const adminCompletedPercent = computed(() => {
  if (totalAdminOrders.value === 0) {
    return 0
  }
  return Math.round(((statistics.value.COMPLETED || 0) / totalAdminOrders.value) * 100)
})

const pendingPreview = computed(() => {
  return workerTasks.value
    .filter(item => item.status === 'PENDING_PROCESS' || item.status === 'PROCESSING')
    .slice(0, 4)
})

const historyPreview = computed(() => {
  return workerHistory.value.slice(0, 3)
})

const studentRecentOrders = computed(() => {
  return studentOrders.value.slice(0, 4)
})

const totalWorkerCount = computed(() => {
  return (
    (statistics.value.PENDING_PROCESS || 0) +
    (statistics.value.PROCESSING || 0) +
    (statistics.value.COMPLETED || 0)
  )
})

const totalStudentCount = computed(() => {
  return studentOrders.value.length
})

const processingPercent = computed(() => {
  if (totalWorkerCount.value === 0) {
    return 0
  }
  return Math.round(((statistics.value.PROCESSING || 0) / totalWorkerCount.value) * 100)
})

const completedPercent = computed(() => {
  if (totalWorkerCount.value === 0) {
    return 0
  }
  return Math.round(((statistics.value.COMPLETED || 0) / totalWorkerCount.value) * 100)
})

const studentProcessingPercent = computed(() => {
  if (totalStudentCount.value === 0) {
    return 0
  }
  return Math.round(((statistics.value.PROCESSING || 0) / totalStudentCount.value) * 100)
})

const studentCompletedPercent = computed(() => {
  if (totalStudentCount.value === 0) {
    return 0
  }
  return Math.round(((statistics.value.COMPLETED || 0) / totalStudentCount.value) * 100)
})

async function loadDashboard() {
  if (userStore.role === 'ADMIN' || userStore.role === 'SUPER_ADMIN') {
    statistics.value = await getStatistics()
    adminOrders.value = await getAllOrders()
    return
  }

  if (userStore.role === 'WORKER') {
    const taskResult = await getWorkerTasks({ pageNum: 1, pageSize: 1000 })
    const historyResult = await getWorkerHistory({ pageNum: 1, pageSize: 1000 })
    const tasks = Array.isArray(taskResult) ? taskResult : taskResult.list
    const history = Array.isArray(historyResult) ? historyResult : historyResult.list

    workerTasks.value = tasks
    workerHistory.value = history

    statistics.value = {
      PENDING_REVIEW: 0,
      PENDING_PROCESS: tasks.filter(item => item.status === 'PENDING_PROCESS').length,
      PROCESSING: tasks.filter(item => item.status === 'PROCESSING').length,
      COMPLETED: history.filter(item => item.status === 'COMPLETED').length
    }
    return
  }

  if (userStore.role === 'STUDENT') {
    const orderPage = await getMyOrders({ pageNum: 1, pageSize: 1000 })
    const orders = orderPage.list
    studentOrders.value = orders

    statistics.value = {
      PENDING_REVIEW: orders.filter(item => item.status === 'PENDING_REVIEW').length,
      PENDING_PROCESS: orders.filter(item => item.status === 'PENDING_PROCESS').length,
      PROCESSING: orders.filter(item => item.status === 'PROCESSING').length,
      COMPLETED: orders.filter(item => item.status === 'COMPLETED').length
    }
  }
}

function reloadDashboard() {
  loadDashboard()
}

onMounted(loadDashboard)
</script>

<style scoped>
.dashboard-page {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dashboard-heading {
  min-height: 54px;
  margin-bottom: 10px;
  padding: 5px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-left: 4px solid #d6a72c;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 6px 18px rgba(0, 63, 120, 0.06);
}

.dashboard-heading .page-title {
  margin-bottom: 4px;
  color: #004f96;
}

.campus-subtitle {
  color: #718096;
  font-size: 13px;
}

.dashboard-heading img {
  width: 190px;
  max-height: 44px;
  object-fit: contain;
}

@media (max-width: 760px) {
  .dashboard-heading img {
    width: 150px;
  }
}

.stat-card {
  min-height: 68px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  border: none;
  border-radius: 14px;
  color: #ffffff;
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.1);
}

.stat-icon {
  font-size: 25px;
}

.stat-info {
  text-align: left;
}

.stat-value {
  font-size: 25px;
  font-weight: 800;
  line-height: 1;
}

.stat-label {
  margin-top: 6px;
  font-size: 14px;
}

.blue-card {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.orange-card {
  background: linear-gradient(135deg, #e6a23c, #f3c36b);
}

.green-card {
  background: linear-gradient(135deg, #67c23a, #95d475);
}

.purple-card {
  background: linear-gradient(135deg, #8e44ad, #b37feb);
}

.worker-hero,
.student-hero,
.admin-hero {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 112px;
  padding: 14px 24px;
  margin-bottom: 10px;
  border-radius: 18px;
  color: #ffffff;
  overflow: hidden;
  box-shadow: 0 14px 30px rgba(31, 111, 235, 0.2);
}

.worker-hero {
  background:
    radial-gradient(circle at 88% 25%, rgba(255, 255, 255, 0.28), transparent 26%),
    linear-gradient(135deg, #1f6feb, #67c23a);
}

.student-hero {
  background:
    radial-gradient(circle at 88% 25%, rgba(255, 255, 255, 0.28), transparent 26%),
    linear-gradient(135deg, #8e44ad, #409eff);
}

.admin-hero {
  background:
    radial-gradient(circle at 88% 25%, rgba(255, 255, 255, 0.28), transparent 26%),
    linear-gradient(135deg, #003f78, #0878c9);
}

.hero-badge {
  display: inline-block;
  padding: 4px 10px;
  margin-bottom: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  font-size: 12px;
  font-weight: 700;
}

.hero-title {
  font-size: 22px;
  font-weight: 800;
  margin-bottom: 8px;
}

.hero-subtitle {
  max-width: 760px;
  line-height: 1.6;
  opacity: 0.96;
}

.hero-actions {
  margin-top: 8px;
}

.hero-right {
  position: relative;
  width: 230px;
  height: 96px;
}

.hero-circle {
  position: absolute;
  right: 30px;
  top: 2px;
  width: 108px;
  height: 108px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
}

.hero-icon {
  position: absolute;
  right: 68px;
  top: 28px;
  font-size: 48px;
}

.hero-mini {
  position: absolute;
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.96);
  color: #303133;
  font-weight: 700;
  font-size: 13px;
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.13);
}

.mini-one {
  right: 112px;
  top: 6px;
}

.mini-two {
  right: 6px;
  bottom: 8px;
}

.role-section {
  display: none;
}

.clean-card {
  height: 100%;
  border-radius: 16px;
  border: 1px solid #eef2f7;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.055);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0 0 4px 0;
  font-size: 17px;
  font-weight: 800;
}

.section-header p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.section-icon {
  width: 40px;
  height: 40px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 21px;
}

.blue-icon {
  background: #eef6ff;
}

.green-icon {
  background: #f0f9eb;
}

.orange-icon {
  background: #fff7e8;
}

.purple-icon {
  background: #f5f0ff;
}

.more-link {
  margin-top: 8px;
  color: #409eff;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
}

.more-link:hover {
  text-decoration: underline;
}

.step-list,
.quick-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.step-item,
.quick-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 10px;
  border-radius: 13px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
}

.quick-item {
  cursor: pointer;
  transition: 0.2s;
}

.quick-item:hover {
  transform: translateY(-2px);
  background: #f3f8ff;
}

.step-num,
.quick-icon {
  width: 25px;
  height: 25px;
  border-radius: 50%;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 13px;
  flex-shrink: 0;
}

.blue-step,
.blue-quick {
  background: #409eff;
}

.orange-step,
.orange-quick {
  background: #e6a23c;
}

.green-step,
.green-quick {
  background: #67c23a;
}

.step-title,
.quick-title {
  font-weight: 800;
  margin-bottom: 3px;
}

.step-desc,
.quick-desc {
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 14px;
  align-items: center;
  padding: 12px 14px;
  border-radius: 13px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
}

.history-title {
  font-weight: 800;
  color: #303133;
  margin-bottom: 5px;
}

.history-meta {
  font-size: 12px;
  color: #909399;
}

.result-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.result-text {
  font-weight: 700;
  color: #606266;
}

.progress-card {
  min-height: 180px;
}

.progress-block {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.progress-row {
  padding: 4px 0;
}

.progress-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  color: #303133;
  font-weight: 700;
}

.progress-top strong {
  color: #409eff;
  font-size: 18px;
}

.progress-summary {
  margin-top: 4px;
  padding: 12px 14px;
  border-radius: 13px;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-summary div {
  display: flex;
  justify-content: space-between;
  color: #606266;
}

.progress-summary strong {
  color: #409eff;
}

.system-strip {
  display: none;
  margin-top: 14px;
  padding: 14px 18px;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid rgba(0, 87, 168, 0.14);
  border-top: 3px solid #d6a72c;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.045);
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #606266;
  line-height: 1.6;
}
</style>
