<template>
  <div class="my-orders-page">
    <div class="page-card order-card">
      <div class="page-header">
        <div>
          <div class="page-title">我的工单</div>
          <div class="page-subtitle">查看你提交的维修工单，并按编号、标题、地点、状态和时间筛选。</div>
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

      <el-tabs
        v-model="searchForm.status"
        class="status-tabs"
        @tab-change="handleStatusChange"
      >
        <el-tab-pane label="待审核" name="PENDING_REVIEW" />
        <el-tab-pane label="待处理" name="PENDING_PROCESS" />
        <el-tab-pane label="处理中" name="PROCESSING" />
        <el-tab-pane label="已完成" name="COMPLETED" />
        <el-tab-pane label="已驳回" name="REJECTED" />
        <el-tab-pane label="已取消" name="CANCELLED" />
      </el-tabs>

      <div class="table-header">
        <div class="table-title">工单列表</div>
        <div class="table-count">共 {{ page.total }} 条</div>
      </div>

      <el-table :data="orders" border stripe height="100%">
        <el-table-column prop="orderNo" label="工单编号" width="170" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="location" label="地点" min-width="200" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="100" />

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" class="status-tag">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="180" />

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              plain
              @click="openDetail(row)"
            >
              详情
            </el-button>

            <el-button
              v-if="row.status === 'PENDING_REVIEW' || row.status === 'PENDING_PROCESS'"
              size="small"
              type="danger"
              @click="handleCancel(row.id)"
            >
              取消
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
      width="650px"
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
            {{ currentOrder.description || '暂无描述' }}
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="现场图片" :span="2">
          <div v-if="currentOrder.imageUrls?.length" class="detail-images">
            <el-image
              v-for="(url, index) in currentOrder.imageUrls"
              :key="url"
              :src="url"
              :preview-src-list="currentOrder.imageUrls"
              :initial-index="index"
              fit="cover"
              class="detail-image"
              preview-teleported
            />
          </div>
          <span v-else>暂无图片</span>
        </el-descriptions-item>

        <el-descriptions-item label="处理结果" :span="2">
          <div class="detail-text">
            {{ currentOrder.finishResult || '暂无处理结果' }}
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="驳回原因" :span="2">
          <div class="detail-text">
            {{ currentOrder.rejectReason || '暂无驳回原因' }}
          </div>
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
import { cancelWorkOrder, getMyOrders } from '../../api/workOrder'
import { statusText, statusType } from '../../utils/status'

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
  status: 'PENDING_REVIEW',
  startTime: '',
  endTime: ''
})

async function loadData() {
  const res = await getMyOrders({
    pageNum: page.pageNum,
    pageSize: page.pageSize,
    ...searchForm
  })

  orders.value = res.list
  page.total = res.total
}

function handleSearch() {
  if (!validateTimeRange()) {
    return
  }

  page.pageNum = 1
  loadData()
}

function handleStatusChange() {
  if (!validateTimeRange()) {
    return
  }

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

function resetSearch() {
  Object.assign(searchForm, {
    orderNo: '',
    title: '',
    location: '',
    priority: '',
    status: 'PENDING_REVIEW',
    startTime: '',
    endTime: ''
  })

  handleSearch()
}

function openDetail(row) {
  currentOrder.value = row
  detailDialog.value = true
}

async function handleCancel(id) {
  await ElMessageBox.confirm('确定取消该工单吗？', '提示')
  await cancelWorkOrder(id)
  ElMessage.success('取消成功')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.my-orders-page {
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.order-card {
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
  color: #606266;
  line-height: 32px;
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

.detail-text {
  line-height: 1.7;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-image {
  width: 96px;
  height: 96px;
  border-radius: 6px;
  cursor: zoom-in;
}

.pagination {
  margin-top: 10px;
  flex: 0 0 auto;
  display: flex;
  justify-content: flex-end;
}
</style>
