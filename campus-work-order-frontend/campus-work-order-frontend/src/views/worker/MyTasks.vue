<template>
  <div class="my-tasks-page">
    <div class="page-card task-card">
      <div class="page-header">
        <div>
          <div class="page-title">我的任务</div>
          <div class="page-subtitle">
            查看当前待处理、处理中任务，也可以查看已完成的历史任务记录。
          </div>
        </div>
      </div>

      <div class="search-panel">
        <el-input
          v-model="searchForm.orderNo"
          placeholder="工单编号"
          :maxlength="20"
          show-word-limit
          clearable
          class="search-item"
          @input="handleOrderNoInput"
        />

        <el-input
          v-model="searchForm.title"
          placeholder="标题"
          :maxlength="50"
          show-word-limit
          clearable
          class="search-item"
        />

        <el-input
          v-model="searchForm.location"
          placeholder="地点"
          :maxlength="100"
          show-word-limit
          clearable
          class="search-item"
        />

        <el-select
          v-model="searchForm.priority"
          placeholder="优先级"
          clearable
          class="search-select"
        >
          <el-option label="低" value="LOW" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="高" value="HIGH" />
          <el-option label="紧急" value="URGENT" />
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

      <el-tabs v-model="activeStatus" class="status-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="待处理" name="PENDING_PROCESS" />
        <el-tab-pane label="处理中" name="PROCESSING" />
        <el-tab-pane label="已完成" name="COMPLETED" />
      </el-tabs>

      <div class="table-header">
        <div class="table-title">{{ statusText(activeStatus) }}任务列表</div>
        <div class="table-count">共 {{ page.total }} 条</div>
      </div>

      <el-table :data="orders" border stripe height="100%">
        <el-table-column prop="orderNo" label="工单编号" width="170" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="location" label="地点" min-width="180" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="100" />

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column
          v-if="activeStatus === 'COMPLETED'"
          prop="updatedAt"
          label="完成时间"
          width="180"
        />

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="openDetail(row)">
              详情
            </el-button>

            <el-button
              v-if="row.status === 'PENDING_PROCESS'"
              size="small"
              type="primary"
              @click="accept(row.id)"
            >
              接单
            </el-button>

            <el-button
              v-if="row.status === 'PROCESSING'"
              size="small"
              type="success"
              @click="finish(row.id)"
            >
              完成
            </el-button>
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

    <el-dialog
      v-model="detailDialog"
      title="工单详情"
      width="680px"
    >
      <el-descriptions
        v-if="currentOrder"
        :column="2"
        border
      >
        <el-descriptions-item label="工单编号">
          {{ currentOrder.orderNo }}
        </el-descriptions-item>

        <el-descriptions-item label="状态">
          <el-tag :type="statusType(currentOrder.status)">
            {{ statusText(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="标题">
          {{ currentOrder.title }}
        </el-descriptions-item>

        <el-descriptions-item label="优先级">
          {{ currentOrder.priority }}
        </el-descriptions-item>

        <el-descriptions-item label="地点">
          {{ currentOrder.location }}
        </el-descriptions-item>

        <el-descriptions-item label="创建时间">
          {{ currentOrder.createdAt || '暂无' }}
        </el-descriptions-item>

        <el-descriptions-item label="问题描述" :span="2">
          <div class="detail-text">
            {{ currentOrder.description || '暂无问题描述' }}
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="处理结果" :span="2">
          <div class="detail-text">
            {{ currentOrder.finishResult || '暂无处理结果' }}
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="完成时间" :span="2">
          {{ currentOrder.updatedAt || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  acceptWorkOrder,
  finishWorkOrder,
  getWorkerTasks,
  getWorkerHistory
} from '../../api/workOrder'
import { statusText, statusType } from '../../utils/status'

const activeStatus = ref('PENDING_PROCESS')
const orders = ref([])

const detailDialog = ref(false)
const currentOrder = ref(null)

const page = reactive({
  pageNum: 1,
  pageSize: 5,
  total: 0
})

const searchForm = reactive({
  orderNo: '',
  title: '',
  location: '',
  priority: '',
  startTime: '',
  endTime: ''
})

const queryForm = reactive({
  orderNo: '',
  title: '',
  location: '',
  priority: '',
  startTime: '',
  endTime: ''
})

function filterLocalOrders(list) {
  const orderNo = queryForm.orderNo.trim().toLowerCase()
  const title = queryForm.title.trim().toLowerCase()
  const location = queryForm.location.trim().toLowerCase()
  const priority = queryForm.priority

  return list.filter(item => {
    const matchOrderNo =
      !orderNo || String(item.orderNo || '').toLowerCase().includes(orderNo)

    const matchTitle =
      !title || String(item.title || '').toLowerCase().includes(title)

    const matchLocation =
      !location || String(item.location || '').toLowerCase().includes(location)

    const matchPriority =
      !priority || item.priority === priority

    const matchStatus =
      item.status === activeStatus.value

    let matchTime = true
    if ((queryForm.startTime || queryForm.endTime) && item.createdAt) {
      const createdTime = new Date(item.createdAt).getTime()

      if (queryForm.startTime) {
        matchTime = matchTime && createdTime >= new Date(queryForm.startTime).getTime()
      }

      if (queryForm.endTime) {
        matchTime = matchTime && createdTime <= new Date(queryForm.endTime).getTime()
      }
    }

    return matchOrderNo && matchTitle && matchLocation && matchPriority && matchStatus && matchTime
  })
}

async function loadData() {
  const params = {
    pageNum: page.pageNum,
    pageSize: page.pageSize,
    status: activeStatus.value,
    ...queryForm
  }

  const res = activeStatus.value === 'COMPLETED'
    ? await getWorkerHistory(params)
    : await getWorkerTasks(params)

  if (Array.isArray(res)) {
    const filtered = filterLocalOrders(res)
    const start = (page.pageNum - 1) * page.pageSize
    orders.value = filtered.slice(start, start + page.pageSize)
    page.total = filtered.length
  } else {
    orders.value = res.list || []
    page.total = res.total || 0
  }
}

async function handleTabChange() {
  page.pageNum = 1
  await loadData()
}

function handleSearch() {
  if (!validateTimeRange()) {
    return
  }

  Object.assign(queryForm, searchForm)
  page.pageNum = 1
  loadData()
}

function resetSearch() {
  Object.assign(searchForm, {
    orderNo: '',
    title: '',
    location: '',
    priority: '',
    startTime: '',
    endTime: ''
  })

  Object.assign(queryForm, searchForm)
  page.pageNum = 1
  loadData()
}

function validateTimeRange() {
  if (
    searchForm.startTime &&
    searchForm.endTime &&
    new Date(searchForm.startTime).getTime() > new Date(searchForm.endTime).getTime()
  ) {
    ElMessage.warning('开始时间不能晚于结束时间')
    return false
  }

  return true
}

function handleOrderNoInput(value) {
  searchForm.orderNo = value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase()
}

function openDetail(row) {
  currentOrder.value = row
  detailDialog.value = true
}

async function accept(id) {
  await acceptWorkOrder(id)
  ElMessage.success('接单成功')
  await loadData()
}

async function finish(id) {
  const { value } = await ElMessageBox.prompt('请输入处理结果', '完成工单')
  await finishWorkOrder(id, { finishResult: value })
  ElMessage.success('工单已完成')

  activeStatus.value = 'COMPLETED'
  page.pageNum = 1
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.my-tasks-page {
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.task-card {
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
  width: 155px;
  flex: 1 1 130px;
}

.search-select {
  width: 110px;
  flex: 0 1 110px;
}

.time-picker {
  width: 165px;
  flex: 0 1 165px;
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

.status-tabs {
  margin-bottom: 10px;
}

.status-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 6px 0 10px;
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

.detail-text {
  line-height: 1.7;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
}

.pagination {
  margin-top: 10px;
  flex: 0 0 auto;
  display: flex;
  justify-content: flex-end;
}
</style>
