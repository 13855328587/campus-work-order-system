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

      <div class="user-content">
        <aside class="user-tree-card">
          <div class="tree-title">用户分类</div>
          <el-tree
            :data="roleTreeData"
            :props="roleTreeProps"
            node-key="key"
            highlight-current
            default-expand-all
            :current-node-key="selectedRoleKey"
            @node-click="handleRoleNodeClick"
          />
        </aside>

        <section class="user-table-section">
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
              @change="syncRoleTree"
              @clear="syncRoleTree"
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
            <div class="table-title">{{ currentRoleTitle }}</div>
            <div class="table-tools">
              <div class="table-count">共 {{ page.total }} 条</div>
              <el-tooltip content="刷新" placement="top">
                <el-icon
                  class="table-refresh"
                  :class="{ 'is-loading': loading }"
                  role="button"
                  tabindex="0"
                  aria-label="刷新用户列表"
                  @click="loadData"
                  @keydown.enter="loadData"
                ><Refresh /></el-icon>
              </el-tooltip>
            </div>
          </div>

          <div class="batch-toolbar">
            <span class="batch-tip">已选 {{ selectedRows.length }} 项</span>
            <el-button
              type="warning"
              plain
              size="small"
              :disabled="!batchResetCount"
              @click="handleBatchResetPassword"
            >批量重置密码</el-button>
            <el-button
              type="danger"
              plain
              size="small"
              :disabled="!batchDeleteCount"
              @click="handleBatchDelete"
            >批量删除</el-button>
          </div>

          <el-table
            v-loading="loading"
            :data="users"
            border
            stripe
            height="100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="48" :selectable="canSelectUser" />

            <el-table-column prop="id" label="ID" width="80" />

            <el-table-column label="头像" width="90" align="center">
              <template #default="{ row }">
                <el-avatar :size="42" :src="row.avatarUrl || ''">
                  {{ getAvatarText(row) }}
                </el-avatar>
              </template>
            </el-table-column>

            <el-table-column prop="username" label="用户名" min-width="160" />

            <el-table-column prop="realName" label="真实姓名" min-width="160" />

            <el-table-column prop="phone" label="手机号" min-width="145" />

            <el-table-column label="角色" width="128" align="center">
              <template #default="{ row }">
                <span class="role-pill" :class="`role-${row.role}`">
                  {{ roleText(row.role) }}
                </span>
              </template>
            </el-table-column>

            <el-table-column label="状态" width="82" align="center">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  :disabled="!canEditStatus(row)"
                  @change="handleStatusChange(row)"
                />
              </template>
            </el-table-column>

            <el-table-column label="操作" width="300" fixed="right" align="center">
              <template #default="{ row }">
                <el-button
                  type="info"
                  plain
                  size="small"
                  @click="openView(row)"
                >查看</el-button>
                <template v-if="canEditBasic(row) || canDeleteUser(row)">
                  <el-button
                    v-if="canEditBasic(row)"
                    type="primary"
                    plain
                    size="small"
                    @click="openEdit(row)"
                  >修改</el-button>
                  <el-button
                    v-if="canResetPassword(row)"
                    type="warning"
                    plain
                    size="small"
                    @click="handleResetPassword(row)"
                  >重置密码</el-button>
                  <el-button
                    v-if="canDeleteUser(row)"
                    type="danger"
                    plain
                    size="small"
                    @click="handleDeleteUser(row)"
                  >删除</el-button>
                </template>
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
        </section>
      </div>
    </div>

    <el-dialog v-model="editDialog" title="修改用户" width="480px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="90px">
        <el-form-item label="用户ID">
          <el-input :model-value="editForm.id" disabled />
        </el-form-item>

        <el-form-item label="用户名">
          <el-input :model-value="editForm.username" disabled />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="editForm.realName"
            maxlength="20"
            show-word-limit
            clearable
            placeholder="2-20位，不能包含特殊符号"
            @input="handleEditRealNameInput"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="editForm.phone"
            maxlength="11"
            clearable
            placeholder="11位手机号，可不填写"
            @input="handleEditPhoneInput"
          />
        </el-form-item>

        <el-form-item label="角色">
          <el-select
            v-model="editForm.role"
            :disabled="!canEditRole(editForm)"
            style="width: 100%"
          >
            <el-option
              v-for="item in roleOptions(editForm)"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-switch
            v-model="editForm.status"
            :active-value="1"
            :inactive-value="0"
            :disabled="!canEditStatus(editForm)"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存修改</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewDialog" title="查看用户" width="520px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ viewUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ viewUser.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ viewUser.realName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ viewUser.phone || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <span class="role-pill" :class="`role-${viewUser.role}`">
            {{ roleText(viewUser.role) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="userStatusType(viewUser.status)" size="small">
            {{ userStatusText(viewUser.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ viewUser.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ viewUser.updatedAt || '暂无' }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button type="primary" @click="viewDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import {
  batchDeleteUser,
  batchResetUserPassword,
  deleteUser,
  getUsers,
  resetUserPassword,
  updateUser,
  updateUserRole,
  updateUserStatus
} from '../../api/user'
import { useUserStore } from '../../stores/user'
import { roleText } from '../../utils/status'

const userStore = useUserStore()
const users = ref([])
const selectedRows = ref([])
const loading = ref(false)
const editDialog = ref(false)
const viewDialog = ref(false)
const editFormRef = ref(null)
const saving = ref(false)
const selectedRoleKey = ref('all')
const selectedRoleValues = ref([])
const selectedRoleLabel = ref('用户列表')

const roleTreeProps = {
  label: 'label',
  children: 'children'
}

const roleTreeData = [
  {
    key: 'all',
    label: '全部用户',
    role: '',
    children: [
      {
        key: 'manage-users',
        label: '管理端用户',
        roles: ['SUPER_ADMIN', 'ADMIN'],
        children: [
          { key: 'SUPER_ADMIN', label: '超级管理员', role: 'SUPER_ADMIN' },
          { key: 'ADMIN', label: '管理员', role: 'ADMIN' }
        ]
      },
      {
        key: 'service-users',
        label: '服务人员',
        roles: ['WORKER'],
        children: [
          { key: 'WORKER', label: '维修人员', role: 'WORKER' }
        ]
      },
      {
        key: 'student-users',
        label: '学生用户',
        roles: ['STUDENT'],
        children: [
          { key: 'STUDENT', label: '学生', role: 'STUDENT' }
        ]
      }
    ]
  }
]

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

const editForm = reactive({
  id: '',
  username: '',
  realName: '',
  phone: '',
  role: '',
  status: 1,
  oldRole: '',
  oldStatus: 1
})

const viewUser = reactive({
  id: '',
  username: '',
  realName: '',
  phone: '',
  role: '',
  status: 1,
  createdAt: '',
  updatedAt: ''
})

const editRules = {
  realName: [
    { required: true, whitespace: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度必须在2到20个字符之间', trigger: 'blur' },
    { pattern: /^[\u4e00-\u9fa5A-Za-z·]{2,20}$/, message: '真实姓名只能包含中文、字母或间隔点', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^$|^1\d{10}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ]
}

const currentRoleTitle = computed(() => {
  if (searchForm.role) {
    return `${roleText(searchForm.role)}列表`
  }
  return selectedRoleLabel.value
})

const batchResetCount = computed(() => selectedRows.value.filter(canResetPassword).length)
const batchDeleteCount = computed(() => selectedRows.value.filter(canDeleteUser).length)

async function loadData() {
  if (loading.value) return
  loading.value = true
  try {
    // 用户列表统一从后端分页查询；左侧树节点会转换为 role/roles 参数。
    const res = await getUsers({
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      roles: selectedRoleValues.value.join(',') || undefined,
      ...searchForm
    })

    users.value = res.list
    selectedRows.value = []
    page.total = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  syncRoleTree()
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

function syncRoleTree() {
  selectedRoleKey.value = searchForm.role || 'all'
  selectedRoleValues.value = []
  selectedRoleLabel.value = searchForm.role ? `${roleText(searchForm.role)}列表` : '用户列表'
}

function handleRoleNodeClick(data) {
  // 点击左侧用户分类树时，自动同步角色筛选条件并重新查询。
  selectedRoleKey.value = data.key
  searchForm.role = data.role || ''
  selectedRoleValues.value = data.roles || []
  selectedRoleLabel.value = data.key === 'all' ? '用户列表' : `${data.label}列表`
  page.pageNum = 1
  loadData()
}

function isSelf(row) {
  return String(row.id) === String(userStore.userId)
}

function getAvatarText(row) {
  const name = row.realName || row.username || '?'
  return name.trim().charAt(0).toUpperCase()
}

function handleEditRealNameInput(value) {
  editForm.realName = value.replace(/[^\u4e00-\u9fa5A-Za-z·]/g, '').slice(0, 20)
}

function handleEditPhoneInput(value) {
  editForm.phone = value.replace(/\D/g, '').slice(0, 11)
}

function userStatusText(status) {
  return status === 1 ? '启用' : '禁用'
}

function userStatusType(status) {
  return status === 1 ? 'success' : 'danger'
}

function canEditStatus(row) {
  // 状态修改权限和删除权限一致：不能操作自己，管理员只能操作普通用户。
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

function canEditBasic(row) {
  // 基础资料修改权限更宽：用户可修改自己，管理员可修改权限范围内的用户。
  if (isSelf(row) || userStore.role === 'SUPER_ADMIN') {
    return true
  }
  return userStore.role === 'ADMIN' && (row.role === 'WORKER' || row.role === 'STUDENT')
}

function canResetPassword(row) {
  return !isSelf(row) && canEditBasic(row)
}

function canDeleteUser(row) {
  return canEditStatus(row)
}

function canSelectUser(row) {
  return canResetPassword(row) || canDeleteUser(row)
}

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

async function handleStatusChange(row) {
  const oldStatus = row.status === 1 ? 0 : 1

  try {
    await updateUserStatus(row.id, row.status)
    ElMessage.success('状态已更新')
  } catch (e) {
    row.status = oldStatus
  }
}

async function handleResetPassword(row) {
  await ElMessageBox.confirm(
    `确定将用户“${row.username}”的密码重置为 123456 吗？`,
    '重置密码',
    {
      confirmButtonText: '确定重置',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )

  await resetUserPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

async function handleBatchResetPassword() {
  // 批量重置密码走后端批量接口，避免前端逐个请求造成效率和一致性问题。
  const rows = selectedRows.value.filter(canResetPassword)
  if (!rows.length) {
    ElMessage.warning('请选择可重置密码的用户')
    return
  }

  await ElMessageBox.confirm(
    `确定将选中的 ${rows.length} 个用户密码重置为 123456 吗？`,
    '批量重置密码',
    {
      confirmButtonText: '确定重置',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )

  await batchResetUserPassword(rows.map(row => row.id))
  ElMessage.success(`已重置 ${rows.length} 个用户密码为 123456`)
  await loadData()
}

async function handleDeleteUser(row) {
  const message = row.role === 'WORKER'
    ? `确定删除维修人员“${row.username}”吗？该用户待处理和处理中的工单会解除维修人员，并退回待处理。`
    : `确定删除用户“${row.username}”吗？`

  await ElMessageBox.confirm(
    message,
    '删除用户',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )

  await deleteUser(row.id)
  ElMessage.success('用户已删除')

  if (users.value.length === 1 && page.pageNum > 1) {
    page.pageNum -= 1
  }
  await loadData()
}

async function handleBatchDelete() {
  // 批量删除为逻辑删除；如果包含维修人员，后端会统一释放其待处理/处理中工单。
  const rows = selectedRows.value.filter(canDeleteUser)
  if (!rows.length) {
    ElMessage.warning('请选择可删除的用户')
    return
  }

  const workerCount = rows.filter(row => row.role === 'WORKER').length
  const message = workerCount > 0
    ? `确定删除选中的 ${rows.length} 个用户吗？其中 ${workerCount} 个维修人员待处理和处理中的工单会解除维修人员，并退回待处理。`
    : `确定删除选中的 ${rows.length} 个用户吗？`

  await ElMessageBox.confirm(
    message,
    '批量删除用户',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )

  await batchDeleteUser(rows.map(row => row.id))
  ElMessage.success(`已删除 ${rows.length} 个用户`)

  if (users.value.length === rows.length && page.pageNum > 1) {
    page.pageNum -= 1
  }
  await loadData()
}

function openView(row) {
  Object.assign(viewUser, {
    id: row.id,
    username: row.username,
    realName: row.realName || '',
    phone: row.phone || '',
    role: row.role,
    status: row.status,
    createdAt: row.createdAt || '',
    updatedAt: row.updatedAt || ''
  })
  viewDialog.value = true
}

function openEdit(row) {
  Object.assign(editForm, {
    id: row.id,
    username: row.username,
    realName: row.realName || '',
    phone: row.phone || '',
    role: row.role,
    status: row.status,
    oldRole: row.role,
    oldStatus: row.status
  })
  editDialog.value = true
}

async function submitEdit() {
  await editFormRef.value.validate()
  saving.value = true
  try {
    // 修改弹窗一次提交基础信息、角色和状态；后端分别做权限校验。
    const realName = editForm.realName.trim()
    const phone = editForm.phone.trim()
    await updateUser(editForm.id, { realName, phone })

    if (editForm.role !== editForm.oldRole && canEditRole(editForm)) {
      await updateUserRole(editForm.id, editForm.role)
    }

    if (editForm.status !== editForm.oldStatus && canEditStatus(editForm)) {
      await updateUserStatus(editForm.id, editForm.status)
    }

    if (String(editForm.id) === String(userStore.userId)) {
      userStore.updateBasicInfo(realName, phone)
    }

    ElMessage.success('用户信息已修改')
    editDialog.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

function roleOptions(row) {
  if (row.role === 'SUPER_ADMIN') {
    return [
      { label: '超级管理员', value: 'SUPER_ADMIN' }
    ]
  }

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
    { label: roleText(row.role), value: row.role }
  ]
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

.user-content {
  flex: 1;
  min-height: 0;
  display: flex;
  gap: 14px;
}

.user-tree-card {
  width: 210px;
  flex: 0 0 210px;
  min-height: 0;
  padding: 14px 10px;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
  overflow: auto;
}

.tree-title {
  margin: 0 8px 10px;
  font-size: 15px;
  font-weight: 800;
  color: #303133;
}

.user-tree-card :deep(.el-tree) {
  background: transparent;
}

.user-tree-card :deep(.el-tree-node__content) {
  height: 34px;
  border-radius: 10px;
  color: #4b5563;
}

.user-tree-card :deep(.el-tree-node__content:hover),
.user-tree-card :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background: #eef5ff;
  color: #1d4ed8;
  font-weight: 700;
}

.user-table-section {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.user-table-section :deep(.el-table) {
  flex: 1;
  min-height: 0;
}

.user-table-section :deep(.el-table__inner-wrapper) {
  min-width: 1040px;
}

.role-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 84px;
  height: 26px;
  padding: 0 11px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  color: #374151;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  box-shadow: inset 3px 0 0 var(--role-color);
}

.role-SUPER_ADMIN {
  --role-color: #f59e0b;
}

.role-ADMIN {
  --role-color: #3b82f6;
}

.role-WORKER {
  --role-color: #10b981;
}

.role-STUDENT {
  --role-color: #8b5cf6;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 48px;
  height: 24px;
  padding: 0 9px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
}

.status-enabled {
  color: #059669;
}

.status-disabled {
  color: #dc2626;
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

.table-tools {
  display: flex;
  align-items: center;
  gap: 12px;
}

.batch-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 34px;
  padding: 6px 10px;
  margin-bottom: 8px;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  background: #fbfdff;
}

.batch-tip {
  margin-right: 4px;
  color: #64748b;
  font-size: 13px;
}

.pagination {
  margin-top: 10px;
  flex: 0 0 auto;
  display: flex;
  justify-content: flex-end;
}
</style>
