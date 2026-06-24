<template>
  <div class="user-manage-page">
    <div class="page-card user-card">
      <div class="page-header">
        <div>
          <div class="page-title">用户管理</div>
          <div class="page-subtitle">
            管理系统用户信息，可根据用户名、真实姓名、手机号、角色、状态和时间进行筛选。
          </div>
        </div>
      </div>

      <div class="search-panel">
        <el-input
          v-model="searchForm.username"
          placeholder="用户名"
          clearable
          class="search-item"
        />

        <el-input
          v-model="searchForm.realName"
          placeholder="真实姓名"
          clearable
          class="search-item"
        />

        <el-input
          v-model="searchForm.phone"
          placeholder="手机号"
          clearable
          class="search-item"
        />

        <el-select
          v-model="searchForm.role"
          placeholder="角色"
          clearable
          class="search-select"
        >
          <el-option label="超级管理员" value="SUPER_ADMIN" />
          <el-option label="管理员" value="ADMIN" />
          <el-option label="维修人员" value="WORKER" />
          <el-option label="学生" value="STUDENT" />
        </el-select>

        <el-select
          v-model="searchForm.status"
          placeholder="状态"
          clearable
          class="search-select"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>

        <el-date-picker
          v-model="searchForm.startTime"
          type="datetime"
          placeholder="开始时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          class="time-picker"
        />

        <span class="time-separator">至</span>

        <el-date-picker
          v-model="searchForm.endTime"
          type="datetime"
          placeholder="结束时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          class="time-picker"
        />

        <div class="search-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>

      <div class="table-header">
        <div class="table-title">用户列表</div>
        <div class="table-count">共 {{ page.total }} 条</div>
      </div>

      <el-table :data="users" border stripe height="100%">
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column prop="username" label="用户名" min-width="160" />

        <el-table-column prop="realName" label="真实姓名" min-width="160" />

        <el-table-column prop="phone" label="手机号" min-width="160" />

        <el-table-column label="角色" min-width="260">
          <template #default="{ row }">
            <el-select
              v-model="row.role"
              size="small"
              :disabled="!canEditRole(row)"
              style="width: 100%"
              @focus="row.oldRole = row.role"
              @change="changeRole(row)"
            >
              <el-option
                v-for="item in roleOptions(row)"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="180" />

        <el-table-column label="状态" width="160">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="!canEditStatus(row)"
              @change="changeStatus(row)"
            />
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page.pageNum"
          v-model:page-size="page.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="page.total"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getUsers, updateUserRole, updateUserStatus } from '../../api/user'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const users = ref([])

const page = reactive({
  pageNum: 1,
  pageSize: 5,
  total: 0
})

const searchForm = reactive({
  username: '',
  realName: '',
  phone: '',
  role: '',
  status: '',
  startTime: '',
  endTime: ''
})

async function loadData() {
  const res = await getUsers({
    pageNum: page.pageNum,
    pageSize: page.pageSize,
    ...searchForm
  })

  users.value = res.list
  page.total = res.total
}

function handleSearch() {
  page.pageNum = 1
  loadData()
}

function resetSearch() {
  Object.assign(searchForm, {
    username: '',
    realName: '',
    phone: '',
    role: '',
    status: '',
    startTime: '',
    endTime: ''
  })

  handleSearch()
}

function isSelf(row) {
  return String(row.id) === String(userStore.userId)
}

function canEditStatus(row) {
  if (isSelf(row)) {
    return false
  }

  if (userStore.role === 'SUPER_ADMIN') {
    return row.role !== 'SUPER_ADMIN'
  }

  if (userStore.role === 'ADMIN') {
    return row.role === 'WORKER' || row.role === 'STUDENT'
  }

  return false
}

function canEditRole(row) {
  if (isSelf(row)) {
    return false
  }

  if (userStore.role === 'SUPER_ADMIN') {
    return row.role !== 'SUPER_ADMIN'
  }

  if (userStore.role === 'ADMIN') {
    return row.role === 'WORKER' || row.role === 'STUDENT'
  }

  return false
}

function roleOptions(row) {
  if (userStore.role === 'SUPER_ADMIN') {
    return [
      { label: '管理员', value: 'ADMIN' },
      { label: '维修人员', value: 'WORKER' },
      { label: '学生', value: 'STUDENT' }
    ]
  }

  if (userStore.role === 'ADMIN') {
    return [
      { label: '维修人员', value: 'WORKER' },
      { label: '学生', value: 'STUDENT' }
    ]
  }

  return [
    { label: row.role, value: row.role }
  ]
}

async function changeStatus(row) {
  const oldStatus = row.status === 1 ? 0 : 1

  try {
    await updateUserStatus(row.id, row.status)
    ElMessage.success('状态已更新')
  } catch (e) {
    row.status = oldStatus
  }
}

async function changeRole(row) {
  try {
    await updateUserRole(row.id, row.role)
    ElMessage.success('角色已更新')
    await loadData()
  } catch (e) {
    row.role = row.oldRole
  }
}

onMounted(loadData)
</script>

<style scoped>
.user-manage-page {
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.user-card {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}

.page-title {
  font-size: 22px;
  font-weight: 800;
  color: #303133;
}

.page-subtitle {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.search-panel {
  display: flex;
  flex-wrap: nowrap;
  gap: 8px;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
  overflow: hidden;
}

.search-item {
  width: 145px;
  flex: 1 1 120px;
}

.search-select {
  width: 105px;
  flex: 0 1 105px;
}

.time-picker {
  width: 155px;
  flex: 0 1 155px;
}

.time-separator {
  align-self: center;
  color: #909399;
}

.search-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.table-title {
  font-size: 16px;
  font-weight: 800;
  color: #303133;
}

.table-count {
  color: #909399;
  font-size: 13px;
}

.pagination {
  margin-top: 10px;
  flex: 0 0 auto;
  display: flex;
  justify-content: flex-end;
}
</style>
